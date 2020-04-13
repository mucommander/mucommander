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

import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.cms.CMSSignedDataGenerator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;

/**
 * Utility method for mapping the hard to read DigestAlgorithmIdentifier to the more descriptive layman form.
 */
public class AlgorithmIdentifier {

    private static final HashMap<String, String> encryptionAlgorithms = new HashMap<String, String>();
    private static final HashMap<String, String> digestAlgorithms = new HashMap<String, String>();

    static {
        // common encryption numbers/names used in PDF
        encryptionAlgorithms.put(X9ObjectIdentifiers.id_dsa_with_sha1.getId(), "DSA");
        encryptionAlgorithms.put(X9ObjectIdentifiers.id_dsa.getId(), "DSA");
        encryptionAlgorithms.put(OIWObjectIdentifiers.dsaWithSHA1.getId(), "DSA");
        encryptionAlgorithms.put(PKCSObjectIdentifiers.rsaEncryption.getId(), "RSA");
        encryptionAlgorithms.put(PKCSObjectIdentifiers.sha1WithRSAEncryption.getId(), "RSA");
        encryptionAlgorithms.put(TeleTrusTObjectIdentifiers.teleTrusTRSAsignatureAlgorithm.getId(), "RSA");
        encryptionAlgorithms.put(X509ObjectIdentifiers.id_ea_rsa.getId(), "RSA");
        encryptionAlgorithms.put(CMSSignedDataGenerator.ENCRYPTION_ECDSA, "ECDSA");
        encryptionAlgorithms.put(X9ObjectIdentifiers.ecdsa_with_SHA2.getId(), "ECDSA");
        encryptionAlgorithms.put(X9ObjectIdentifiers.ecdsa_with_SHA224.getId(), "ECDSA");
        encryptionAlgorithms.put(X9ObjectIdentifiers.ecdsa_with_SHA256.getId(), "ECDSA");
        encryptionAlgorithms.put(X9ObjectIdentifiers.ecdsa_with_SHA384.getId(), "ECDSA");
        encryptionAlgorithms.put(X9ObjectIdentifiers.ecdsa_with_SHA512.getId(), "ECDSA");
        encryptionAlgorithms.put(CMSSignedDataGenerator.ENCRYPTION_RSA_PSS, "RSAandMGF1");
        encryptionAlgorithms.put(CryptoProObjectIdentifiers.gostR3410_94.getId(), "GOST3410");
        encryptionAlgorithms.put(CryptoProObjectIdentifiers.gostR3410_2001.getId(), "ECGOST3410");
        encryptionAlgorithms.put("1.3.6.1.4.1.5849.1.6.2", "ECGOST3410");
        encryptionAlgorithms.put("1.3.6.1.4.1.5849.1.1.5", "GOST3410");
        encryptionAlgorithms.put("1.2.840.113549.1.1.11", "RSA");
        // common digest numbesr/names used in PDF.
        digestAlgorithms.put(PKCSObjectIdentifiers.md5.getId(), "MD5");
        digestAlgorithms.put(OIWObjectIdentifiers.idSHA1.getId(), "SHA1");
        digestAlgorithms.put(NISTObjectIdentifiers.id_sha224.getId(), "SHA224");
        digestAlgorithms.put(NISTObjectIdentifiers.id_sha256.getId(), "SHA256");
        digestAlgorithms.put(NISTObjectIdentifiers.id_sha384.getId(), "SHA384");
        digestAlgorithms.put(NISTObjectIdentifiers.id_sha512.getId(), "SHA512");
        digestAlgorithms.put(PKCSObjectIdentifiers.sha1WithRSAEncryption.getId(), "SHA1");
        digestAlgorithms.put(PKCSObjectIdentifiers.sha224WithRSAEncryption.getId(), "SHA224");
        digestAlgorithms.put(PKCSObjectIdentifiers.sha256WithRSAEncryption.getId(), "SHA256");
        digestAlgorithms.put(PKCSObjectIdentifiers.sha384WithRSAEncryption.getId(), "SHA384");
        digestAlgorithms.put(PKCSObjectIdentifiers.sha512WithRSAEncryption.getId(), "SHA512");
        digestAlgorithms.put(TeleTrusTObjectIdentifiers.ripemd128.getId(), "RIPEMD128");
        digestAlgorithms.put(TeleTrusTObjectIdentifiers.ripemd160.getId(), "RIPEMD160");
        digestAlgorithms.put(TeleTrusTObjectIdentifiers.ripemd256.getId(), "RIPEMD256");
        digestAlgorithms.put(CryptoProObjectIdentifiers.gostR3411.getId(), "GOST3411");
        digestAlgorithms.put("1.3.6.1.4.1.5849.1.2.1", "GOST3411");
    }

    /**
     * Gets an instance of a MessageDigest for the given algorithm and provider.
     *
     * @param algorithm algorithm reference number as a string.
     * @param provider  provider, optional, can be null.
     * @return message digest
     * @throws NoSuchProviderException  provider could not be found associated with a MD.
     * @throws NoSuchAlgorithmException algorithm could not be found associated with a MD.
     */
    public static MessageDigest getDigestInstance(String algorithm, String provider)
            throws NoSuchProviderException, NoSuchAlgorithmException {
        if (provider != null) {
            try {
                return MessageDigest.getInstance(algorithm, provider);
            } catch (NoSuchAlgorithmException e) {
                return MessageDigest.getInstance(algorithm);
            } catch (NoSuchProviderException e) {
                return MessageDigest.getInstance(algorithm);
            }
        } else {
            return MessageDigest.getInstance(algorithm);
        }
    }

    /**
     * Gets the encryption algorithm name for the specified encryption object ID.
     *
     * @param encryptionAlgorithmOID object id to reference.
     * @return algorithm name in layman form.
     */
    public static String getEncryptionAlgorithmName(String encryptionAlgorithmOID) {
        String algName = encryptionAlgorithms.get(encryptionAlgorithmOID);
        if (algName != null) {
            return algName;
        }
        return encryptionAlgorithmOID;
    }

    /**
     * Get the digest algorithm name for the given object ID.
     *
     * @param digestAlgorithmOID OID of the digest.
     * @return algorithm name in layman form.
     */
    public static String getDigestAlgorithmName(String digestAlgorithmOID) {
        String name = digestAlgorithms.get(digestAlgorithmOID);
        if (name != null) {
            return name;
        }
        return digestAlgorithmOID;
    }

}
