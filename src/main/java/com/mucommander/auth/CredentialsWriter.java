/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import com.mucommander.RuntimeConstants;
import com.mucommander.bookmark.XORCipher;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURL;
import com.mucommander.xml.XmlAttributes;
import com.mucommander.xml.XmlWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * This class provides a method to write persistent credentials contained by {@link CredentialsManager} to an XML file.
 *
 * @author Maxence Bernard
 * @see CredentialsParser
 */
public class CredentialsWriter implements CredentialsConstants {

    /**
     * Writes the credentials XML file in the user's preferences folder.
     * This method should only be called by {@link CredentialsManager}.
     */
    static void write(OutputStream stream) throws IOException {

        XmlWriter out  = new XmlWriter(stream);

        // Root element, add the encryption method used
        XmlAttributes attributes = new XmlAttributes();
        attributes.add(ATTRIBUTE_ENCRYPTION, WEAK_ENCRYPTION_METHOD);
        // Version the file
        attributes.add(ATTRIBUTE_VERSION, RuntimeConstants.VERSION);
        out.startElement(ELEMENT_ROOT, attributes);
        out.println();

        Iterator<CredentialsMapping> iterator = CredentialsManager.getPersistentCredentialMappings().iterator();
        CredentialsMapping credentialsMapping;
        FileURL realm;
        Enumeration<String> propertyKeys;
        String name;

        while(iterator.hasNext()) {
            credentialsMapping = iterator.next();
            realm = credentialsMapping.getRealm();

            // Start credentials element
            out.startElement(ELEMENT_CREDENTIALS);
            out.println();

            // Write URL
            out.startElement(ELEMENT_URL);
            out.writeCData(realm.toString(false));
            out.endElement(ELEMENT_URL);

            Credentials credentials = credentialsMapping.getCredentials();

            // Write login
            out.startElement(ELEMENT_LOGIN);
            out.writeCData(credentials.getLogin());
            out.endElement(ELEMENT_LOGIN);

            // Write password (XOR encrypted)
            out.startElement(ELEMENT_PASSWORD);
            out.writeCData(XORCipher.encryptXORBase64(credentials.getPassword()));
            out.endElement(ELEMENT_PASSWORD);

            // Write properties, each property is stored in a separate 'property' element
            propertyKeys = realm.getPropertyNames();
            while(propertyKeys.hasMoreElements()) {
                name = propertyKeys.nextElement();
                attributes = new XmlAttributes();
                attributes.add(ATTRIBUTE_NAME, name);
                attributes.add(ATTRIBUTE_VALUE, realm.getProperty(name));
                out.startElement(ELEMENT_PROPERTY, attributes);
                out.endElement(ELEMENT_PROPERTY);
            }

            // End credentials element
            out.endElement(ELEMENT_CREDENTIALS);
        }

        // End root element
        out.endElement(ELEMENT_ROOT);
    }
}
