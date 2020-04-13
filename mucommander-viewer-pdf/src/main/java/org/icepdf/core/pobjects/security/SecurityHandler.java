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
 * Defines common behaviors for Security Handlers.
 *
 * @since 1.1
 */
public abstract class SecurityHandler implements SecurityHandlerInterface {

    protected String handlerName = null;

    protected EncryptionDictionary encryptionDictionary = null;

    protected Permissions permissions = null;

    public SecurityHandler(EncryptionDictionary encryptionDictionary) {
        this.encryptionDictionary = encryptionDictionary;
    }

    public abstract boolean isAuthorized(String password);

    public abstract boolean isUserAuthorized(String password);

    public abstract boolean isOwnerAuthorized(String password);

    public abstract byte[] encrypt(Reference objectReference,
                                   byte[] encryptionKey,
                                   byte[] data);

    public abstract byte[] decrypt(Reference objectReference,
                                   byte[] encryptionKey,
                                   byte[] data);

    public abstract InputStream encryptInputStream(
            Reference objectReference,
            byte[] encryptionKey,
            HashMap decodeParams,
            InputStream input);

    public abstract InputStream decryptInputStream(
            Reference objectReference,
            byte[] encryptionKey,
            HashMap decodeParams,
            InputStream input);

    public abstract byte[] getEncryptionKey();

    public abstract byte[] getDecryptionKey();

    public abstract String getHandlerName();

    public abstract Permissions getPermissions();

    public abstract void init();

    public abstract void dispose();
}
