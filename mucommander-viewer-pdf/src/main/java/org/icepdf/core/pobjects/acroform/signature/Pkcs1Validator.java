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

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.jce.provider.X509CertParser;
import org.icepdf.core.pobjects.StringObject;
import org.icepdf.core.pobjects.acroform.SignatureDictionary;
import org.icepdf.core.pobjects.acroform.SignatureFieldDictionary;
import org.icepdf.core.pobjects.acroform.signature.exceptions.SignatureIntegrityException;
import org.icepdf.core.util.Utils;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.security.cert.X509Certificate;

/**
 * "adbe.x509.rsa_sha1"
 * In this case, the Contents key contains a DER-encoded PKCS#1 [11]
 * binary data object representing the signature obtained as the RSA
 * encryption of the byte range SHA-1 digest with the signer's
 * private key.  When using PKCS#1, the certificate chain of the
 * signer is included with other signature information in the signed
 * document.
 */
public class Pkcs1Validator extends AbstractPkcsValidator {

    public Pkcs1Validator(SignatureFieldDictionary signatureFieldDictionary) throws SignatureIntegrityException {
        super(signatureFieldDictionary);
    }

    public void init() throws SignatureIntegrityException {
        SignatureDictionary signatureDictionary = signatureFieldDictionary.getSignatureDictionary();
        announceSignatureType(signatureDictionary);
        // start the decode of the raw type.
        StringObject stringObject = signatureDictionary.getContents();
        // make sure we don't loose any bytes converting the string in the raw.
        byte[] cmsData = Utils.convertByteCharSequenceToByteArray(stringObject.getLiteralString());
        // get the certificate
        stringObject = signatureDictionary.getCertString();
        // make sure we don't loose any bytes converting the string in the raw.
        byte[] certsKey = Utils.convertByteCharSequenceToByteArray(stringObject.getLiteralString());

        try {
            X509CertParser x509CertParser = new X509CertParser();
            x509CertParser.engineInit(new ByteArrayInputStream(certsKey));
            certificateChain = x509CertParser.engineReadAll();
            signerCertificate = (X509Certificate) certificateChain.iterator().next();

            // content data is encrypted using the cert above.
            ASN1InputStream asn1InputStream = new ASN1InputStream(new ByteArrayInputStream(cmsData));
            ASN1Primitive tmp = asn1InputStream.readObject();
            messageDigest = ((ASN1OctetString) tmp).getOctets();

            String provider = signatureDictionary.getFilter().getName();
            digestAlgorithmIdentifier = OIWObjectIdentifiers.idSHA1.getId();
            signatureAlgorithmIdentifier = PKCSObjectIdentifiers.rsaEncryption.getId();
            // basic creation and public key check which should throw any format errors.
            createSignature(signerCertificate.getPublicKey(), provider,
                    signatureAlgorithmIdentifier, digestAlgorithmIdentifier);

            // Use RSA/ECB/NoPadding do decrypt the message digest
            Cipher asymmetricCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            // initialize your cipher
            asymmetricCipher.init(Cipher.DECRYPT_MODE, signerCertificate.getPublicKey());
            // assuming, cipherText is a byte array containing your encrypted message
            messageDigest = asymmetricCipher.doFinal(messageDigest);
            // trim the padding bytes
            if (messageDigest.length > 20) {
                // You can create the ASN.1 BER encoding of an MD5, SHA-1, or SHA-256 value by prepending these strings to
                // the 16-byte or 20-byte hash values, respectively:
                // We always assume sha1 which is:
                //    ref: sha1   : X'30213009 06052B0E 03021A05 000414'
                //    ref: SHA-256: X'3031300D 06096086 48016503 04020105 000420'
                //    ref: MD5:     X'3020300C 06082A86 4886F70D 02050500 0410'
                byte[] trunkedMD = new byte[20];
                System.arraycopy(messageDigest, 15, trunkedMD, 0, 20);
                messageDigest = trunkedMD;
            }
        } catch (Exception e) {
            throw new SignatureIntegrityException(e);
        }
        initialized = true;
    }

    @Override
    public void validate() throws SignatureIntegrityException {
        validateDocument();
    }

}
