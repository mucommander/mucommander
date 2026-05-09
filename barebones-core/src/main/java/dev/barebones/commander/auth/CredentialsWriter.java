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

import dev.barebones.commander.RuntimeConstants;
import dev.barebones.commander.commons.file.Credentials;
import dev.barebones.commander.commons.file.FileURL;
import dev.barebones.commander.commons.util.xml.XmlAttributes;
import dev.barebones.commander.commons.util.xml.XmlWriter;
import dev.barebones.commander.secret.SecretRef;
import dev.barebones.commander.secret.SecretStore;
import dev.barebones.commander.secret.SecretStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Writes the persistent credentials list to {@code credentials.xml}.
 * Phase 12: secrets live in the {@link SecretStore} (OS keychain /
 * libsecret / AES-GCM file), not in the XML. The XML now holds only:
 * <ul>
 *   <li>the URL,</li>
 *   <li>the login,</li>
 *   <li>a {@code <secret-ref/>} marker telling
 *       {@link CredentialsParser} to look the password up in the
 *       SecretStore using the URL as the lookup key,</li>
 *   <li>any URL properties.</li>
 * </ul>
 *
 * If no SecretStore is installed (CI / headless), entries are still
 * persisted to XML — minus the secret material, which means they
 * won't be usable until the user re-enters the password. This is
 * preferable to silently dropping the URL/login entirely.
 *
 * @author Maxence Bernard
 * @see CredentialsParser
 */
public class CredentialsWriter implements CredentialsConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsWriter.class);

    static void write(OutputStream stream) throws IOException {
        XmlWriter out = new XmlWriter(stream);
        SecretStore secrets = SecretStoreService.store();

        XmlAttributes attributes = new XmlAttributes();
        attributes.add(ATTRIBUTE_ENCRYPTION,
            secrets != null ? SECRET_STORE_METHOD : WEAK_ENCRYPTION_METHOD);
        attributes.add(ATTRIBUTE_VERSION, RuntimeConstants.VERSION);
        out.startElement(ELEMENT_ROOT, attributes);
        out.println();

        Iterator<CredentialsMapping> iterator =
            CredentialsManager.getPersistentCredentialMappings().iterator();

        while (iterator.hasNext()) {
            CredentialsMapping credentialsMapping = iterator.next();
            FileURL realm = credentialsMapping.getRealm();
            Credentials credentials = credentialsMapping.getCredentials();

            // Push the secret into the keychain BEFORE writing the
            // XML, so the XML always references something real.
            if (secrets != null) {
                try {
                    secrets.store(
                        new SecretRef(SECRET_STORE_SERVICE, realm.toString(false)),
                        credentials.getPassword().toCharArray());
                } catch (IOException e) {
                    LOGGER.warn("SecretStore store failed for {}; XML will still be " +
                        "written, but the secret won't be present at next read",
                        realm, e);
                }
            }

            out.startElement(ELEMENT_CREDENTIALS);
            out.println();

            out.startElement(ELEMENT_URL);
            out.writeCData(realm.toString(false));
            out.endElement(ELEMENT_URL);

            out.startElement(ELEMENT_LOGIN);
            out.writeCData(credentials.getLogin());
            out.endElement(ELEMENT_LOGIN);

            // Phase-12 marker — empty body. The parser uses the
            // ELEMENT_URL above to build the SecretRef.
            out.startElement(ELEMENT_SECRET_REF);
            out.endElement(ELEMENT_SECRET_REF);

            // Per-URL properties (region, useHttps, etc).
            Enumeration<String> propertyKeys = realm.getPropertyNames();
            while (propertyKeys.hasMoreElements()) {
                String name = propertyKeys.nextElement();
                attributes = new XmlAttributes();
                attributes.add(ATTRIBUTE_NAME, name);
                attributes.add(ATTRIBUTE_VALUE, realm.getProperty(name));
                out.startElement(ELEMENT_PROPERTY, attributes);
                out.endElement(ELEMENT_PROPERTY);
            }

            out.endElement(ELEMENT_CREDENTIALS);
        }

        out.endElement(ELEMENT_ROOT);
    }
}
