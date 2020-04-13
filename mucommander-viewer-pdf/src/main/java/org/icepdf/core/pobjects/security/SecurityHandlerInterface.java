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

import org.icepdf.core.pobjects.Reference;

import java.io.InputStream;
import java.util.HashMap;

/**
 * The interface for objects which defines a Security Handler for a PDF
 * document.  A custom Security Handlers should implement this interface.
 *
 * @since 1.1
 */
public interface SecurityHandlerInterface {

    /**
     * Determines whether the supplied password is authorized to view the
     * PDF document.  If a password is rejected, the user should be restricted
     * from viewing the document.
     *
     * @param password password to authorize
     * @return true, if the password was authorized successfully; false, otherwise.
     */
    public boolean isAuthorized(String password);

    /**
     * Determines whether the supplied user password is authorized to view the
     * PDF document.  If a password is rejected, the user should be restricted
     * from viewing the document.
     *
     * @param password password to authorize
     * @return true, if the password was authorized successfully; false, otherwise.
     */
    public boolean isUserAuthorized(String password);

    /**
     * Determines whether the supplied owner password is authorized to view the
     * PDF document.  If a password is rejected, the user should be restricted
     * from viewing the document.
     *
     * @param password password to authorize
     * @return true, if the password was authorized successfully; false, otherwise.
     */
    public boolean isOwnerAuthorized(String password);

    /**
     * Encrypt the PDF data bytestream or string.
     *
     * @param objectReference reference to PDF object being encrypted; this object
     *                        contains the PDF object number and revision.
     * @param encryptionKey   encryption key used by encryption algorithm.
     * @param data            byte data to be encrypted;  either represents an object stream
     *                        or string value.
     * @return the encrypted stream or string  byte data
     */
    public byte[] encrypt(Reference objectReference,
                          byte[] encryptionKey,
                          byte[] data);

    /**
     * Decrypt the PDF data bytestream or string.
     *
     * @param objectReference reference to PDF object being encrypted; this object
     *                        contains the PDF object number and revision.
     * @param encryptionKey   encryption key used by decryption algorithm.
     * @param data            byte data to be decrypted;  either represents an object stream
     *                        or string value.
     * @return the decrypted stream or string byte data
     */
    public byte[] decrypt(Reference objectReference,
                          byte[] encryptionKey,
                          byte[] data);

    /**
     * Encrypt the PDF data byteStream.
     *
     * @param objectReference reference to PDF object being encrypted; this object
     *                        contains the PDF object number and revision.
     * @param encryptionKey   encryption key used by decryption algorithm.
     * @param input           inputStream data to be decrypted;  either represents an object stream
     *                        or string value.
     * @return the ecrypted stream or string byte data
     */
    InputStream encryptInputStream(
            Reference objectReference,
            byte[] encryptionKey,
            HashMap decodeParams,
            InputStream input);

    /**
     * Decrypt the PDF data byteStream.
     *
     * @param objectReference reference to PDF object being encrypted; this object
     *                        contains the PDF object number and revision.
     * @param encryptionKey   encryption key used by decryption algorithm.
     * @param input           inputStream data to be decrypted;  either represents an object stream
     *                        or string value.
     * @return the decrypted stream or string byte data
     */
    InputStream decryptInputStream(
            Reference objectReference,
            byte[] encryptionKey,
            HashMap decodeParams,
            InputStream input);

    /**
     * Gets the encryption key used by the security handler for encrypting data.
     *
     * @return byte data representing encryption key
     */
    public byte[] getEncryptionKey();

    /**
     * Gets the encryption key used by the security handler for decryption data.
     *
     * @return byte data representing encryption key
     */
    public byte[] getDecryptionKey();

    /**
     * Gets the name of the default security handler.
     *
     * @return string representing security handler name
     */
    public String getHandlerName();

    /**
     * Gets the PDF permissions object associated with this document's
     * security handler.
     *
     * @return security handlers permissions object
     */
    public Permissions getPermissions();

    /**
     * Initiate the security handler
     */
    public void init();

    /**
     * Dispose of the security handler.
     */
    public void dispose();
}
