package com.mucommander.auth;

import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;
import com.mucommander.io.BackupInputStream;
import com.mucommander.file.FileURL;
import com.mucommander.Debug;
import com.mucommander.bookmark.XORCipher;

import java.util.Hashtable;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * This class takes care of parsing the credentials XML file and adding parsed {@link MappedCredentials} instances
 * to {@link CredentialsManager}'s persistent credentials list.
 *
 * @author Maxence Bernard
 */
class CredentialsParser implements ContentHandler, CredentialsConstants {

    /** Variable used for XML parsing */
    private FileURL url;
    /** Variable used for XML parsing */
    private String login;
    /** Variable used for XML parsing */
    private String password;
    /** Variable used for XML parsing */
    private String characters;

    /** Contains the encryption method used to encrypt/decrypt passwords */
    private String encryptionMethod;


    /**
     * Creates a new CredentialsParser.
     */
    public CredentialsParser() {
    }


    /**
     * Parses the given XML credentials file. Should only be called by CredentialsManager.
     */
    void parse(File file) throws Exception {
        InputStream fin = new BackupInputStream(file);
        new Parser().parse(fin, this, "UTF-8");
        fin.close();
    }


    ///////////////////////////////////
    // ContentHandler implementation //
    ///////////////////////////////////

    public void startElement(String uri, String name, Hashtable attValues, Hashtable attURIs) throws Exception {
        this.characters = null;

        if(name.equals(ELEMENT_CREDENTIALS)) {
            // Reset parsing variables
            url = null;
            login = null;
            password = null;
        }
        // Root element, the 'encryption' attribute specifies which encoding was used to encrypt passwords
        else if(name.equals(ELEMENT_ROOT)) {
            encryptionMethod = (String)attValues.get("encryption");
        }
    }

    public void endElement(String uri, String name) throws Exception {
        if(name.equals(ELEMENT_CREDENTIALS)) {
            if(url ==null || login ==null || password ==null) {
                if(Debug.ON) Debug.trace("Missing value, credentials ignored: url="+ url +" login="+ login +" password="+ password);
                return;
            }

            // Decrypt password
            try {
                password = XORCipher.decryptXORBase64(password);
            }
            catch(IOException e) {
                if(Debug.ON) Debug.trace("Password could not be decrypted: "+ password +", credentials will be ignored");
                return;
            }

            // Add credentials to persistent credentials list
            CredentialsManager.getPersistentCredentials().add(new MappedCredentials(login, password, url, true));
        }
        else if(name.equals(ELEMENT_URL)) {
            try {
                url = new FileURL(characters);
            }
            catch(MalformedURLException e) {
                if(Debug.ON) Debug.trace("Malformed URL: "+characters+", location will be ignored");
            }
        }
        else if(name.equals(ELEMENT_LOGIN)) {
            login = characters;
        }
        else if(name.equals(ELEMENT_PASSWORD)) {
            password = characters;
        }
    }

    public void characters(String s) throws Exception {
        this.characters = s.trim();
    }

    public void startDocument() throws Exception {
    }

    public void endDocument() throws Exception {
    }
}
