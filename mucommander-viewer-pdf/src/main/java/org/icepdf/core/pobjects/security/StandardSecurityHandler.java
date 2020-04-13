/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.core.pobjects.security;

import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.Reference;

import java.io.InputStream;
import java.util.HashMap;

/**
 * <p>ICEpdf's standard security handler allows access permissions and up to two passwords
 * to be specified for a document: an owner password and a user password. An
 * application's decision to encrypt a document is based on whether the user
 * creating the document specifies any passwords or access restrictions (for example, in a
 * security settings dialog that the user can invoke before saving the PDF file); if so,
 * the document is encrypted, and the permissions and information required to validate
 * the passwords are stored in the encryption dictionary. (An application may
 * also create an encrypted document without any user interaction, if it has some
 * other source of information about what passwords and permissions to use.)</p>
 * <p/>
 * <p>If a user attempts to open an encrypted document that has a user password, the
 * viewer application should prompt for a password. Correctly supplying either
 * password allows the user to open the document, decrypt it, and display it on the
 * screen. If the document does not have a user password, no password is requested;
 * the viewer application can simply open, decrypt, and display the document.
 * Whether additional operations are allowed on a decrypted document depends on
 * which password (if any) was supplied when the document was opened and on
 * any access restrictions that were specified when the document was created:
 * <ul>
 * <li>Opening the document with the correct owner password (assuming it is not
 * the same as the user password) allows full (owner) access to the
 * document. This unlimited access includes the ability to change the
 * document's passwords and access permissions.</li>
 * <p/>
 * <li>Opening the document with the correct user password (or opening a
 * document that does not have a user password) allows additional operations
 * to be performed according to the user access permissions specified in the
 * document's encryption dictionary.</li>
 * </ul>
 * <p/>
 * <p>Access permissions are specified in the form of flags corresponding to the
 * various operations, and the set of operations to which they correspond,
 * depends in turn on the security handler's revision number (also stored in the
 * encryption dictionary). If the revision number is 2 or greater, the
 * operations to which user access can be controlled are as follows:
 * <p/>
 * <ul>
 * <li>Modifying the document's contents</li>
 * <p/>
 * <li>Copying or otherwise extracting text and graphics from the document,
 * including extraction for accessibility purposes (that is, to make the
 * contents of the document accessible through assistive technologies such
 * as screen readers or Braille output devices</li>
 * <p/>
 * <li>Adding or modifying text annotations and interactive form fields</li>
 * <p/>
 * <li>Printing the document</li>
 * </ul>
 * <p/>
 * <p>If the security handler's revision number is 3 or greater, user access to the
 * following operations can be controlled more selectively:
 * <ul>
 * <li>Filling in forms (that is, filling in existing interactive form fields)
 * and signing the document (which amounts to filling in existing signature
 * fields, a type of interactive form field)</li>
 * <p/>
 * <li>Assembling the document: inserting, rotating, or deleting pages and
 * creating navigation elements such as bookmarks or thumbnail images </li>
 * <p/>
 * <li>Printing to a representation from which a faithful digital copy of the
 * PDF content could be generated. Disallowing such printing may result in
 * degradation of output quality (a feature implemented as "Print As Image"
 * in Acrobat)</li>
 * </ul>
 * <p>In addition, revision 3 enables the extraction of text and graphics (in
 * support of accessibility to disabled users or for other purposes) to be
 * controlled separately. Beginning with revision 4, the standard security
 * handler supports crypt filters. The support is limited to the Identity crypt
 * filter and crypt filters named StdCF whose dictionaries contain a CFM value
 * of V2 and an AuthEvent value of DocOpen.</p>
 *
 * @since 1.1
 */
public class StandardSecurityHandler extends SecurityHandler {

    public static final Name NAME_KEY = new Name("Name");
    public static final Name IDENTITY_KEY = new Name("Identity");

    // StandardEncryption holds algorithms specific to adobe standard encryption
    private StandardEncryption standardEncryption = null;

    // encryption key used for encryption,  Standard encryption is symmetric, so
    // only one key is needed.
    private byte[] encryptionKey;

    // initiated flag
    private boolean initiated;

    // string to store password used for decoding, the user password is always
    // used for encryption, never the user password.
    private String password;

    public StandardSecurityHandler(EncryptionDictionary encryptionDictionary) {
        super(encryptionDictionary);
        // Full name of handler
        handlerName = "Adobe Standard Security";
    }

    public boolean isAuthorized(String password) {
        if (encryptionDictionary.getRevisionNumber() < 5) {
            boolean value = standardEncryption.authenticateUserPassword(password);
            // check password against user password
            if (!value) {
                // check password against owner password
                value = standardEncryption.authenticateOwnerPassword(password);
                // Get user, password, as it is used for generating encryption keys
                if (value) {
                    this.password = standardEncryption.getUserPassword();
                }
            } else {
                // assign password for future use
                this.password = password;
            }
            return value;
        } else if (encryptionDictionary.getRevisionNumber() == 5) {
            // try and calculate the document key.
            byte[] encryptionKey = standardEncryption.encryptionKeyAlgorithm(
                    password,
                    encryptionDictionary.getKeyLength());
            this.password = password;
            return encryptionKey != null;
        } else {
            return false;
        }
    }

    public boolean isOwnerAuthorized(String password) {
        // owner password is not stored as it is not used for decryption
        if (encryptionDictionary.getRevisionNumber() < 5) {
            return standardEncryption.authenticateOwnerPassword(password);
        } else {
            return encryptionDictionary.isAuthenticatedOwnerPassword();
        }
    }

    public boolean isUserAuthorized(String password) {
        // owner password is not stored as it is not used for decryption
        if (encryptionDictionary.getRevisionNumber() < 5) {
            boolean value = standardEncryption.authenticateUserPassword(password);
            if (value) {
                this.password = password;
            }
            return value;
        } else {
            return encryptionDictionary.isAuthenticatedUserPassword();
        }
    }

    public byte[] encrypt(Reference objectReference,
                          byte[] encryptionKey,
                          byte[] data) {

        // check if crypt filters are being used and find out if V2 or AESV2
        String algorithmType = getAlgorithmType();

        // use the general encryption algorithm for encryption
        return standardEncryption.generalEncryptionAlgorithm(
                objectReference, encryptionKey, algorithmType, data, true);
    }


    public byte[] decrypt(Reference objectReference,
                          byte[] encryptionKey,
                          byte[] data) {
        // check if crypt filters are being used and find out if V2 or AESV2
        String algorithmType = getAlgorithmType();

        // use the general encryption algorithm for encryption
        return standardEncryption.generalEncryptionAlgorithm(
                objectReference, encryptionKey, algorithmType, data, false);
    }

    /**
     * Utility to determine encryption type used.
     */
    private String getAlgorithmType() {
        String algorithmType;
        if (encryptionDictionary.getCryptFilter() != null) {
            CryptFilterEntry cryptFilterEntry =
                    encryptionDictionary.getCryptFilter().getCryptFilterByName(
                            encryptionDictionary.getStrF());

            algorithmType = cryptFilterEntry.getCryptFilterMethod().getName();
        } else {
            algorithmType = StandardEncryption.ENCRYPTION_TYPE_V2;
        }
        return algorithmType;
    }

    public InputStream decryptInputStream(
            Reference objectReference,
            byte[] encryptionKey,
            HashMap decodeParams,
            InputStream input) {
        return getInputStream(objectReference, encryptionKey, decodeParams, input, false);
    }

    public InputStream encryptInputStream(
            Reference objectReference,
            byte[] encryptionKey,
            HashMap decodeParams,
            InputStream input) {
        return getInputStream(objectReference, encryptionKey, decodeParams, input, true);
    }

    public InputStream getInputStream(
            Reference objectReference,
            byte[] encryptionKey,
            HashMap decodeParams,
            InputStream input, boolean encrypted) {

        // find the name of the crypt filter used in the CF dictionary
        CryptFilterEntry cryptFilter = null;
        if (decodeParams != null) {
            Name filterName = (Name) decodeParams.get(NAME_KEY);
            if (filterName != null) {
                // identity means don't use the cryprt filter or encryption at all
                // for the stream.
                if (filterName.equals(IDENTITY_KEY)) {
                    return input;
                } else {
                    // find the filter name in the encryption dictionary
                    cryptFilter = encryptionDictionary.
                            getCryptFilter().getCryptFilterByName(filterName);
                }
            } else if (encryptionDictionary.getCryptFilter() != null) {
                // corner case, some images treams also use the "decodeParams"
                // dictionary, if it doesn't contain a filter name then we
                // want to make sure we assign the standard one so the steam
                // can be unencrypted.
                cryptFilter = encryptionDictionary.getCryptFilter().getCryptFilterByName(
                        encryptionDictionary.getStmF());
            }
        }
        // We default to the method specified in by StrmF in the security dictionary
        else if (encryptionDictionary.getCryptFilter() != null) {
            cryptFilter = encryptionDictionary.getCryptFilter().getCryptFilterByName(
                    encryptionDictionary.getStmF());
        }

        // get the method used for the general encryption algorithm
        String algorithmType;
        if (cryptFilter != null) {
            algorithmType = cryptFilter.getCryptFilterMethod().getName();
        } else {
            algorithmType = StandardEncryption.ENCRYPTION_TYPE_V2;
        }

        return standardEncryption.generalEncryptionInputStream(
                objectReference, encryptionKey, algorithmType, input, encrypted);
    }

    public byte[] getEncryptionKey() {

        if (!initiated) {
            // make sure class instance var have been setup
            this.init();
        }
        // calculate the encryptionKey based on the given user name
        encryptionKey = standardEncryption.encryptionKeyAlgorithm(
                password,
                encryptionDictionary.getKeyLength());

        return encryptionKey;
    }

    public byte[] getDecryptionKey() {
        return getEncryptionKey();
    }

    public Permissions getPermissions() {
        if (!initiated) {
            // make sure class instance var have been setup
            this.init();
        }
        return permissions;
    }

    public String getHandlerName() {
        return this.handlerName;
    }

    public void init() {
        // initiate a new instance
        standardEncryption = new StandardEncryption(encryptionDictionary);
        // initiate permissions
        permissions = new Permissions(encryptionDictionary);
        permissions.init();
        // update flag
        initiated = true;
    }

    public void dispose() {
        standardEncryption = null;
        encryptionKey = null;
        permissions = null;
        // update flag
        initiated = false;
    }
}
