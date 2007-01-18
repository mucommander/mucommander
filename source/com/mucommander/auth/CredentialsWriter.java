package com.mucommander.auth;

import com.mucommander.xml.writer.XmlAttributes;
import com.mucommander.xml.writer.XmlWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * This class provides a method to write the credentials XML file.
 *
 * @author Maxence Bernard
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
        out.startElement(ELEMENT_ROOT, attributes);
        out.println();

        // Add muCommander version
        out.startElement(ELEMENT_VERSION);
        out.writeCData(com.mucommander.RuntimeConstants.VERSION);
        out.endElement(ELEMENT_VERSION);

        Iterator iterator = CredentialsManager.getPersistentCredentials().iterator();
        MappedCredentials credentials;

        while(iterator.hasNext()) {
            credentials = (MappedCredentials)iterator.next();

            // Start credentials element
            out.startElement(ELEMENT_CREDENTIALS);
            out.println();

            // Write URL
            out.startElement(ELEMENT_URL);
            out.writeCData(credentials.getRealm().toString(false));
            out.endElement(ELEMENT_URL);

            // Write login
            out.startElement(ELEMENT_LOGIN);
            out.writeCData(credentials.getLogin());
            out.endElement(ELEMENT_LOGIN);

            // Write password
            out.startElement(ELEMENT_PASSWORD);
            out.writeCData(credentials.getEncryptedPassword());
            out.endElement(ELEMENT_PASSWORD);

            // End credentials element
            out.endElement(ELEMENT_CREDENTIALS);
        }

        // End root element
        out.endElement(ELEMENT_ROOT);
    }
}
