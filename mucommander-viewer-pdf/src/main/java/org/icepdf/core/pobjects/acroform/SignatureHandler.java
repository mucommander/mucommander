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
package org.icepdf.core.pobjects.acroform;

import org.icepdf.core.pobjects.acroform.signature.DigitalSignatureFactory;
import org.icepdf.core.pobjects.acroform.signature.SignatureValidator;
import org.icepdf.core.pobjects.acroform.signature.exceptions.SignatureIntegrityException;
import org.icepdf.core.util.Defs;

import java.security.Provider;
import java.security.Security;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The signature handler is responsible for returning validation results for a given Digital signature's
 * signature field dictionary.  The returned Validation objected can be interrogated to see which properties
 * are considered valid.
 */
public class SignatureHandler {

    private static final Logger logger =
            Logger.getLogger(SignatureHandler.class.toString());

    static {
        // Load security handler from system property if possible
        String defaultSecurityProvider =
                "org.bouncycastle.jce.provider.BouncyCastleProvider";

        // check system property security provider
        String customSecurityProvider =
                Defs.sysProperty("org.icepdf.core.security.jceProvider");

        // if no custom security provider load default security provider
        if (customSecurityProvider != null) {
            defaultSecurityProvider = customSecurityProvider;
        }
        try {
            // try and create a new provider
            Object provider = Class.forName(defaultSecurityProvider).newInstance();
            Security.insertProviderAt((Provider) provider, 2);
        } catch (ClassNotFoundException e) {
            logger.log(Level.FINE, "Optional BouncyCastle security provider not found");
        } catch (InstantiationException e) {
            logger.log(Level.FINE, "Optional BouncyCastle security provider could not be instantiated");
        } catch (IllegalAccessException e) {
            logger.log(Level.FINE, "Optional BouncyCastle security provider could not be created");
        }
    }

    public SignatureHandler() {
    }

    /**
     * Validates the given SignatureFieldDictionary.
     *
     * @param signatureFieldDictionary signature to validate
     * @return SignatureValidator object if cert and public key verified, null otherwise.
     */
    public SignatureValidator validateSignature(SignatureFieldDictionary signatureFieldDictionary) {

        SignatureDictionary signatureDictionary = signatureFieldDictionary.getSignatureDictionary();
        if (signatureDictionary != null) {
            // Generate the correct validator and try to validate the signature.
            try {
                SignatureValidator signatureValidator = DigitalSignatureFactory.getInstance().getValidatorInstance(signatureFieldDictionary);
                return signatureValidator;
            } catch (SignatureIntegrityException e) {
                logger.log(Level.WARNING, "Signature certificate could not be initialized.", e);
            } catch (Throwable e) {
                logger.log(Level.WARNING, "Signature validation was unsuccessful.", e);
            }
        }
        return null;
    }
}
