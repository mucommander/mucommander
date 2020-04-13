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

import org.bouncycastle.asn1.ASN1Sequence;
import org.icepdf.core.pobjects.HexStringObject;
import org.icepdf.core.pobjects.acroform.SignatureDictionary;
import org.icepdf.core.pobjects.acroform.SignatureFieldDictionary;
import org.icepdf.core.pobjects.acroform.signature.exceptions.SignatureIntegrityException;
import org.icepdf.core.util.Utils;

import java.util.logging.Logger;

/**
 * Pkcs7Validator is based on the RFC3852 specification for Cryptographic Message Syntax (CMS).  The
 * Digital SignatureFactory is responsible for creating any SignatureValidator implementation and does so based on the
 * subFilter value of the SignatureFieldDictionary.  In this particular cas the validation takes place when the
 * subFilter is equal to "adbe.pkcs7.detached".
 * <p/>
 * Also the subfilter "adbe.pkcs7.sha1".  PKCS#7 The SHA1 digest of the document's byte range shall be encapsulated in
 * the PKCS#7 SignedData field with ContentInfo of type Data. The digest of that SignedData shall be incorporated as
 * the normal PKCS#7 digest.
 */
public class Pkcs7Validator extends AbstractPkcsValidator {

    private static final Logger logger =
            Logger.getLogger(Pkcs7Validator.class.toString());

    public Pkcs7Validator(SignatureFieldDictionary signatureFieldDictionary) throws SignatureIntegrityException {
        super(signatureFieldDictionary);
    }

    public void init() throws SignatureIntegrityException {
        SignatureDictionary signatureDictionary = signatureFieldDictionary.getSignatureDictionary();
        announceSignatureType(signatureDictionary);

        // get the signature bytes.
        HexStringObject hexStringObject = signatureDictionary.getContents();
        // make sure we don't loose any bytes converting the string in the raw.
        byte[] cmsData = Utils.convertByteCharSequenceToByteArray(hexStringObject.getLiteralString());

        // Signed-data content type -- start of parsing
        ASN1Sequence signedData = captureSignedData(cmsData);

        // parse out the singer data.
        parseSignerData(signedData, cmsData);

        /**
         * End of signature validation checking and data gather;
         * This section should be moved to a base class and extened for the SHA1 and 7Detatched.
         */
        initialized = true;
    }

    public void validate() throws SignatureIntegrityException {
        validateDocument();
    }

}
