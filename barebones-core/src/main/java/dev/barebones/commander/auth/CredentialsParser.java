/*
 * Copyright (C) 2002-2026 muCommander contributors
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */

package dev.barebones.commander.auth;

import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.commons.file.Credentials;
import dev.barebones.commander.commons.file.FileURL;
import dev.barebones.commander.commons.io.security.SecureXml;
import dev.barebones.commander.io.backup.BackupInputStream;
import dev.barebones.commander.secret.LegacyXorCodec;
import dev.barebones.commander.secret.SecretRef;
import dev.barebones.commander.secret.SecretStore;
import dev.barebones.commander.secret.SecretStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;

/**
 * Parses the credentials XML file and adds parsed
 * {@link CredentialsMapping} instances to {@link CredentialsManager}.
 *
 * Two on-disk formats are accepted:
 * <ul>
 *   <li><b>Legacy (pre-Phase-12)</b>: {@code <password>XOR_BASE64</password>}
 *       — decrypted via {@link LegacyXorCodec} and migrated into the
 *       {@link SecretStoreService} on the fly. The file is rewritten
 *       in the new format on the next save (the legacy elements just
 *       go away — {@link CredentialsWriter} never emits {@code
 *       <password>} anymore).</li>
 *   <li><b>Phase-12</b>: {@code <secret-ref/>} — the secret lives in
 *       the OS keychain / libsecret / AES-GCM file, looked up by
 *       (service="barebones-commander", account=URL).</li>
 * </ul>
 *
 * If no SecretStore is installed (e.g. headless CI), legacy passwords
 * are decrypted but kept only in memory; new-format entries can't be
 * resolved and are dropped with a log line.
 *
 * @author Maxence Bernard
 * @see CredentialsWriter
 */
class CredentialsParser extends DefaultHandler implements CredentialsConstants {
    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsParser.class);

    private FileURL url;
    private Map<String, String> urlProperties;
    private String login;
    /** Holds the legacy XOR-Base64 ciphertext between
     *  {@code endElement(<password>)} and {@code endElement(<credentials>)}. */
    private String legacyEncryptedPassword;
    /** True when this entry uses the Phase-12 {@code <secret-ref/>}
     *  marker — the password should be looked up in the SecretStore. */
    private boolean secretRefSeen;
    private StringBuilder characters;

    private String version;
    private String encryptionMethod;

    public CredentialsParser() {
    }

    /** Parses the given XML credentials file. Should only be called by CredentialsManager. */
    void parse(AbstractFile file) throws Exception {
        InputStream in = null;
        characters = new StringBuilder();
        try {
            SecureXml.newSafeSaxParser().parse(in = new BackupInputStream(file), this);
        } finally {
            if (in != null) {
                try { in.close(); }
                catch (Exception e) { /* ignored */ }
            }
        }
    }

    public String getVersion() {
        return version;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        characters.setLength(0);

        if (qName.equals(ELEMENT_CREDENTIALS)) {
            url = null;
            urlProperties = null;
            login = null;
            legacyEncryptedPassword = null;
            secretRefSeen = false;
        } else if (qName.equals(ELEMENT_PROPERTY)) {
            if (urlProperties == null) {
                urlProperties = new Hashtable<>();
            }
            urlProperties.put(attributes.getValue(ATTRIBUTE_NAME), attributes.getValue(ATTRIBUTE_VALUE));
        } else if (qName.equals(ELEMENT_ROOT)) {
            encryptionMethod = attributes.getValue("encryption");
            version = attributes.getValue(ATTRIBUTE_VERSION);
        } else if (qName.equals(ELEMENT_SECRET_REF)) {
            secretRefSeen = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals(ELEMENT_CREDENTIALS)) {
            if (url == null || login == null) {
                LOGGER.info("Missing url or login, credentials ignored: url=" + url + " login=" + login);
                return;
            }
            if (urlProperties != null) {
                for (String key : urlProperties.keySet()) {
                    url.setProperty(key, urlProperties.get(key));
                }
            }
            String password = resolvePassword();
            if (password == null) {
                return; // already logged inside resolvePassword
            }
            CredentialsManager.getPersistentCredentialMappings().add(
                new CredentialsMapping(new Credentials(login, password), url, true));
        } else if (qName.equals(ELEMENT_URL)) {
            try { url = FileURL.getFileURL(characters.toString().trim()); }
            catch (MalformedURLException e) {
                LOGGER.info("Malformed URL: " + characters + ", location will be ignored");
            }
        } else if (qName.equals(ELEMENT_LOGIN)) {
            login = characters.toString().trim();
        } else if (qName.equals(ELEMENT_PASSWORD)) {
            legacyEncryptedPassword = characters.toString().trim();
        }
    }

    private String resolvePassword() {
        SecretStore secrets = SecretStoreService.store();
        SecretRef ref = new SecretRef(SECRET_STORE_SERVICE, url.toString(false));

        // Legacy XOR password takes precedence — decrypt and migrate.
        if (legacyEncryptedPassword != null) {
            String plaintext;
            try {
                plaintext = LegacyXorCodec.decryptXorBase64(legacyEncryptedPassword);
            } catch (IOException e) {
                LOGGER.info("Legacy password could not be decrypted; credentials ignored: " + url);
                return null;
            }
            if (secrets != null) {
                try {
                    secrets.store(ref, plaintext.toCharArray());
                    LOGGER.info("Migrated legacy XOR-encrypted password for {} into {}",
                        url, secrets.backendName());
                } catch (IOException e) {
                    LOGGER.warn("Could not migrate legacy password for {} into {}; " +
                        "the entry will be re-migrated on next start",
                        url, secrets.backendName(), e);
                }
            }
            return plaintext;
        }

        if (secretRefSeen) {
            if (secrets == null) {
                LOGGER.info("No SecretStore installed; cannot resolve secret-ref for {}", url);
                return null;
            }
            try {
                Optional<char[]> got = secrets.lookup(ref);
                if (got.isEmpty()) {
                    LOGGER.info("SecretStore had no entry for {}; credentials ignored", url);
                    return null;
                }
                return new String(got.get());
            } catch (IOException e) {
                LOGGER.warn("SecretStore lookup failed for {}; credentials ignored", url, e);
                return null;
            }
        }

        LOGGER.info("Credentials entry for {} has no <password> and no <secret-ref/>; ignored", url);
        return null;
    }

    @Override
    public void characters(char[] ch, int offset, int length) {
        characters.append(ch, offset, length);
    }
}
