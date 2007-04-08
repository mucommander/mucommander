package com.mucommander.auth;

import com.mucommander.Debug;
import com.mucommander.bookmark.XORCipher;
import com.mucommander.file.FileURL;
import com.mucommander.io.BackupInputStream;
import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class takes care of parsing the credentials XML file and adding parsed {@link MappedCredentials} instances
 * to {@link CredentialsManager}'s persistent credentials list.
 *
 * @author Maxence Bernard
 */
class CredentialsParser implements ContentHandler, CredentialsConstants {

    // Variables used for XML parsing
    private FileURL url;
    private Hashtable urlProperties;
    private String login;
    private String password;
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
            urlProperties = null;
            login = null;
            password = null;
        }
        // Property element (properties will be set when credentials element ends
        else if(name.equals(ELEMENT_PROPERTY)) {
            if(urlProperties==null)
                urlProperties = new Hashtable();
            urlProperties.put(attValues.get(ATTRIBUTE_NAME), attValues.get(ATTRIBUTE_VALUE));
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

            // Copy properties into FileURL instance (if any)
            if(urlProperties!=null) {
                Enumeration propertyKeys = urlProperties.keys();
                String key;
                while(propertyKeys.hasMoreElements()) {
                    key = (String)propertyKeys.nextElement();
                    url.setProperty(key, (String)urlProperties.get(key));
                }
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
