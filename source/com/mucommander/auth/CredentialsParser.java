/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.auth;

import com.mucommander.Debug;
import com.mucommander.bookmark.XORCipher;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileURL;
import com.mucommander.io.BackupInputStream;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
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
class CredentialsParser extends DefaultHandler implements CredentialsConstants {

    // Variables used for XML parsing
    private FileURL url;
    private Hashtable urlProperties;
    private String login;
    private String password;
    private StringBuffer characters;

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
    void parse(AbstractFile file) throws Exception {
        InputStream in;

        in = null;
        characters = new StringBuffer();
        try {SAXParserFactory.newInstance().newSAXParser().parse(in = new BackupInputStream(file), this);}
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
        }
    }


    ///////////////////////////////////
    // ContentHandler implementation //
    ///////////////////////////////////

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        characters.setLength(0);

        if(qName.equals(ELEMENT_CREDENTIALS)) {
            // Reset parsing variables
            url = null;
            urlProperties = null;
            login = null;
            password = null;
        }
        // Property element (properties will be set when credentials element ends
        else if(qName.equals(ELEMENT_PROPERTY)) {
            if(urlProperties==null)
                urlProperties = new Hashtable();
            urlProperties.put(attributes.getValue(ATTRIBUTE_NAME), attributes.getValue(ATTRIBUTE_VALUE));
        }
        // Root element, the 'encryption' attribute specifies which encoding was used to encrypt passwords
        else if(qName.equals(ELEMENT_ROOT)) {
            encryptionMethod = attributes.getValue("encryption");
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equals(ELEMENT_CREDENTIALS)) {
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
            try {password = XORCipher.decryptXORBase64(password);}
            catch(IOException e) {
                if(Debug.ON) Debug.trace("Password could not be decrypted: "+ password +", credentials will be ignored");
                return;
            }

            // Add credentials to persistent credentials list
            CredentialsManager.getPersistentCredentials().add(new MappedCredentials(login, password, url, true));
        }
        else if(qName.equals(ELEMENT_URL)) {
            try {url = new FileURL(characters.toString().trim());}
            catch(MalformedURLException e) {if(Debug.ON) Debug.trace("Malformed URL: "+characters+", location will be ignored");}
        }
        else if(qName.equals(ELEMENT_LOGIN))
            login = characters.toString().trim();
        else if(qName.equals(ELEMENT_PASSWORD))
            password = characters.toString().trim();
    }

    public void characters(char[] ch, int offset, int length) {characters.append(ch, offset, length);}
}
