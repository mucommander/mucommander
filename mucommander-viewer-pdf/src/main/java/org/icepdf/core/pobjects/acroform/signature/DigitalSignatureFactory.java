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
package org.icepdf.core.pobjects.acroform.signature;

import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.acroform.SignatureDictionary;
import org.icepdf.core.pobjects.acroform.SignatureFieldDictionary;
import org.icepdf.core.pobjects.acroform.signature.exceptions.SignatureIntegrityException;

/**
 * DigitalSignatureFactory which takes a SignatureDictionary and returns the appropriate validator
 * or signer implementation.
 */
public class DigitalSignatureFactory {

    // current list of sub filter types, suspect a stronger then sha1 will be in the spec soon.
    public static final Name DSS_SUB_FILTER_PKCS7_DETACHED = new Name("adbe.pkcs7.detached");
    public static final Name DSS_SUB_FILTER_PKCS7_SHA1 = new Name("adbe.pkcs7.sha1");
    public static final Name DSS_SUB_FILTER_PKCS7__SHA1 = new Name("adbe.x509.rsa_sha1");
    // few examples with alternate ras sha1 name
    public static final Name DSS_SUB_FILTER_RSA_SHA1 = new Name("adbe.x509.rsa.sha1");

    private static DigitalSignatureFactory digitalSignatureFactory;

    private DigitalSignatureFactory() {
    }

    public static DigitalSignatureFactory getInstance() {
        if (digitalSignatureFactory == null) {
            digitalSignatureFactory = new DigitalSignatureFactory();
        }
        return digitalSignatureFactory;
    }

    // TODO: implement singer stance creation, likely just go with adbe.x509.rsa.sha1 for forget about adbe.pkcs7.detached
    public SignatureSigner getSignerInstance(SignatureFieldDictionary signatureFieldDictionary) {
        return null;
    }

    /**
     * Returns an appropriate validator instance for the the specified SignatureFieldDictionary.
     * The returned SignatureValidator can then be used to validate the respective signature against the
     * current document.
     *
     * @param signatureFieldDictionary documents signature dictionary.
     * @return validator for the given implementation.
     * @throws SignatureIntegrityException can occur if the signature dictionary certificate and
     *                                     public key are invalid or can not be verified.
     */
    public SignatureValidator getValidatorInstance(SignatureFieldDictionary signatureFieldDictionary) throws SignatureIntegrityException {
        SignatureDictionary signatureDictionary = signatureFieldDictionary.getSignatureDictionary();
        // PKCS#7 detached and sha-1 digest method
        if (signatureDictionary.getSubFilter().equals(DSS_SUB_FILTER_PKCS7_DETACHED) ||
                signatureDictionary.getSubFilter().equals(DSS_SUB_FILTER_PKCS7_SHA1)) {
            return new Pkcs7Validator(signatureFieldDictionary);
        }
        // PKCS#1 RSA encryption and SHA-1 digest method
        else if (signatureDictionary.getSubFilter().equals(DSS_SUB_FILTER_RSA_SHA1) ||
                signatureDictionary.getSubFilter().equals(DSS_SUB_FILTER_PKCS7__SHA1)) {
            return new Pkcs1Validator(signatureFieldDictionary);
        }

        return null;
    }

}
