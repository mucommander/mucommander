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
import org.icepdf.core.util.Utils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PDF's standard security handler allows access permissions and up to two passwords
 * to be specified for a document.  The purpose of this class is to encapsulate
 * the algorithms used by the Standard Security Handler.
 * <p/>
 * All of the algorithms used for encryption related calculations are based
 * on the suto code described in the Adobe PDF Specification 1.5.
 *
 * @since 1.1
 */
class StandardEncryption {

    private static final Logger logger =
            Logger.getLogger(StandardEncryption.class.toString());

    /**
     * The application shall not decrypt data but shall direct the input stream
     * to the security handler for decryption (NO SUPPORT)
     */
    public static final String ENCRYPTION_TYPE_NONE = "None";
    /**
     * The application shall ask the security handler for the encryption key and
     * shall implicitly decrypt data with "Algorithm 1: Encryption of data using
     * the RC4 or AES algorithms", using the RC4 algorithm.
     */
    public static final String ENCRYPTION_TYPE_V2 = "V2";
    public static final String ENCRYPTION_TYPE_V3 = "V3";
    /**
     * (PDF 1.6) The application shall ask the security handler for the
     * encryption key and shall implicitly decrypt data with "Algorithm 1:
     * Encryption of data using the RC4 or AES algorithms", using the AES
     * algorithm in Cipher Block Chaining (CBC) mode with a 16-byte block size
     * and an initialization vector that shall be randomly generated and placed
     * as the first 16 bytes in the stream or string.
     */
    public static final String ENCRYPTION_TYPE_AES_V2 = "AESV2";

    /**
     * Padding String used in PDF encryption related algorithms
     * < 28 BF 4E 5E 4E 75 8A 41 64 00 4E 56 FF FA 01 08
     * 2E 2E 00 B6 D0 68 3E 80 2F 0C A9 FE 64 53 69 7A >
     */
    private static final byte[] PADDING = {
            (byte) 0x28, (byte) 0xBF, (byte) 0x4E,
            (byte) 0x5E, (byte) 0x4E, (byte) 0x75,
            (byte) 0x8A, (byte) 0x41, (byte) 0x64,
            (byte) 0x00, (byte) 0x4E, (byte) 0x56,
            (byte) 0xFF, (byte) 0xFA, (byte) 0x01,
            (byte) 0x08, (byte) 0x2E, (byte) 0x2E,
            (byte) 0x00, (byte) 0xB6, (byte) 0xD0,
            (byte) 0x68, (byte) 0x3E, (byte) 0x80,
            (byte) 0x2F, (byte) 0x0C, (byte) 0xA9,
            (byte) 0xFE, (byte) 0x64, (byte) 0x53,
            (byte) 0x69, (byte) 0x7A};

    private static final byte[] AES_sAIT = {
            (byte) 0x73, // s
            (byte) 0x41, // A
            (byte) 0x6C, // I
            (byte) 0x54  // T
    };

    // block size of aes key.
    private static final int BLOCK_SIZE = 16;

    // Stores data about encryption
    private EncryptionDictionary encryptionDictionary;

    // Standard encryption key
    private byte[] encryptionKey;

    // last used object reference
    private Reference objectReference;

    // last used RC4 encryption key
    private byte[] rc4Key = null;

    // user password;
    private String userPassword = "";

    // user password;
    private String ownerPassword = "";

    /**
     * Create a new instance of the StandardEncryption object.
     *
     * @param encryptionDictionary standard encryption dictionary values
     */
    public StandardEncryption(EncryptionDictionary encryptionDictionary) {
        this.encryptionDictionary = encryptionDictionary;
    }

    /**
     * General encryption algorithm 3.1 for encryption of data using an
     * encryption key.
     *
     * @param objectReference object number of object being encrypted
     * @param encryptionKey   encryption key for document
     * @param algorithmType   V2 or AESV2 standard encryption encryption types.
     * @param inputData       date to encrypted/decrypt.
     * @return encrypted/decrypted data.
     */
    public byte[] generalEncryptionAlgorithm(Reference objectReference,
                                             byte[] encryptionKey,
                                             final String algorithmType,
                                             byte[] inputData,
                                             boolean encrypt) {

        if (objectReference == null || encryptionKey == null ||
                inputData == null) {
            // throw security exception
            return null;
        }

        // Algorithm 3.1, version 1-4
        if (encryptionDictionary.getVersion() < 5) {

            // RC4 or AES algorithm detection
            boolean isRc4 = algorithmType.equals(ENCRYPTION_TYPE_V2);

            // optimization, if the encryptionKey and objectReference are the
            // same there is no reason to calculate a new key.
            if (rc4Key == null || this.encryptionKey != encryptionKey ||
                    this.objectReference != objectReference) {

                this.objectReference = objectReference;

                // Step 1 to 3, bytes
                byte[] step3Bytes = resetObjectReference(objectReference, isRc4);

                // Step 4: Use the first (n+5) byes, up to a max of 16 from the MD5
                // hash
                int n = encryptionKey.length;
                rc4Key = new byte[Math.min(n + 5, BLOCK_SIZE)];
                System.arraycopy(step3Bytes, 0, rc4Key, 0, rc4Key.length);
            }

            // if we are encrypting we need to properly pad the byte array.
            int encryptionMode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;

            // Set up an RC4 cipher and try to decrypt:
            byte[] finalData = null; // return data if all goes well
            try {
                // Use above as key for the RC4 encryption function.
                if (isRc4) {
                    // Use above as key for the RC4 encryption function.
                    SecretKeySpec key = new SecretKeySpec(rc4Key, "RC4");
                    Cipher rc4 = Cipher.getInstance("RC4");
                    rc4.init(encryptionMode, key);
                    // finally add the stream or string data
                    finalData = rc4.doFinal(inputData);
                } else {
                    SecretKeySpec key = new SecretKeySpec(rc4Key, "AES");
                    Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");

                    // decrypt the data.
                    if (encryptionMode == Cipher.DECRYPT_MODE) {
                        // calculate 16 byte initialization vector.
                        byte[] initialisationVector = new byte[BLOCK_SIZE];
                        //  should never happen as it would mean a string that won't encrypted properly as it
                        // would be missing full length 16 byte public key.
                        if (inputData.length < BLOCK_SIZE) {
                            byte[] tmp = new byte[BLOCK_SIZE];
                            System.arraycopy(inputData, 0, tmp, 0, inputData.length);
                            inputData = tmp;
                        }
                        // grab the public key.
                        System.arraycopy(inputData, 0, initialisationVector, 0, BLOCK_SIZE);
                        final IvParameterSpec iVParameterSpec =
                                new IvParameterSpec(initialisationVector);

                        // trim the input, get rid of the key and expose the data to decrypt
                        byte[] intermData = new byte[inputData.length - BLOCK_SIZE];
                        System.arraycopy(inputData, BLOCK_SIZE, intermData, 0, intermData.length);

                        // finally add the stream or string data
                        aes.init(encryptionMode, key, iVParameterSpec);
                        finalData = aes.doFinal(intermData);
                    } else {
                        // padding is taken care of by PKCS5Padding, so we don't have to touch the data.
                        final IvParameterSpec iVParameterSpec = new IvParameterSpec(generateIv());
                        aes.init(encryptionMode, key, iVParameterSpec);
                        finalData = aes.doFinal(inputData);

                        // add randomness to the start
                        byte[] output = new byte[iVParameterSpec.getIV().length + finalData.length];
                        System.arraycopy(iVParameterSpec.getIV(), 0, output, 0, BLOCK_SIZE);
                        System.arraycopy(finalData, 0, output, BLOCK_SIZE, finalData.length);
                        finalData = output;
                    }
                }

            } catch (NoSuchAlgorithmException ex) {
                logger.log(Level.FINE, "NoSuchAlgorithmException.", ex);
            } catch (IllegalBlockSizeException ex) {
                logger.log(Level.FINE, "IllegalBlockSizeException.", ex);
            } catch (BadPaddingException ex) {
                logger.log(Level.FINE, "BadPaddingException.", ex);
            } catch (NoSuchPaddingException ex) {
                logger.log(Level.FINE, "NoSuchPaddingException.", ex);
            } catch (InvalidKeyException ex) {
                logger.log(Level.FINE, "InvalidKeyException.", ex);
            } catch (InvalidAlgorithmParameterException ex) {
                logger.log(Level.FINE, "InvalidAlgorithmParameterException", ex);
            }

            return finalData;
        }
        // Algorithm 3.1a, version 5
        else if (encryptionDictionary.getVersion() == 5) {
            // Use the 32-byte file encryption key for the AES-256 symmetric
            // key algorithm, along with the string or stream data to be encrypted.

            // Use the AES algorithm in Cipher Block Chaining (CBC) mode, which
            // requires an initialization vector. The block size parameter is
            // set to 16 bytes, and the initialization vector is a 16-byte random
            // number that is stored as the first 16 bytes of the encrypted
            // stream or string.
            try {
                SecretKeySpec key = new SecretKeySpec(encryptionKey, "AES");
                Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");

                // calculate 16 byte initialization vector.
                byte[] initialisationVector = new byte[BLOCK_SIZE];
                System.arraycopy(inputData, 0, initialisationVector, 0, BLOCK_SIZE);

                // trim the input
                byte[] intermData = new byte[inputData.length - BLOCK_SIZE];
                System.arraycopy(inputData, BLOCK_SIZE, intermData, 0, intermData.length);

                final IvParameterSpec iVParameterSpec =
                        new IvParameterSpec(initialisationVector);

                aes.init(Cipher.DECRYPT_MODE, key, iVParameterSpec);

                // finally add the stream or string data
                byte[] finalData = aes.doFinal(intermData);
                return finalData;

            } catch (NoSuchAlgorithmException ex) {
                logger.log(Level.FINE, "NoSuchAlgorithmException.", ex);
            } catch (IllegalBlockSizeException ex) {
                logger.log(Level.FINE, "IllegalBlockSizeException.", ex);
            } catch (BadPaddingException ex) {
                logger.log(Level.FINE, "BadPaddingException.", ex);
            } catch (NoSuchPaddingException ex) {
                logger.log(Level.FINE, "NoSuchPaddingException.", ex);
            } catch (InvalidKeyException ex) {
                logger.log(Level.FINE, "InvalidKeyException.", ex);
            } catch (InvalidAlgorithmParameterException ex) {
                logger.log(Level.FINE, "InvalidAlgorithmParameterException", ex);
            }
        }
        return null;
    }

    /**
     * Generates a recure random 16 byte (128 bit) public key for string to be
     * encryped using AES.
     *
     * @return 16 byte public key.
     */
    private byte[] generateIv() {
        SecureRandom random = new SecureRandom();
        byte[] ivBytes = new byte[BLOCK_SIZE];
        random.nextBytes(ivBytes);
        return ivBytes;
    }

    /**
     * General encryption algorithm 3.1 for encryption of data using an
     * encryption key.
     *
     * Must be synchronized for stream decoding.
     */
    public synchronized InputStream generalEncryptionInputStream(
            Reference objectReference,
            byte[] encryptionKey,
            final String algorithmType,
            InputStream input, boolean encrypt) {
        if (objectReference == null || encryptionKey == null || input == null) {
            // throw security exception
            return null;
        }

        // Algorithm 3.1, version 1-4
        if (encryptionDictionary.getVersion() < 5) {
            // RC4 or AES algorithm detection
            boolean isRc4 = algorithmType.equals(ENCRYPTION_TYPE_V2);

            // optimization, if the encryptionKey and objectReference are the
            // same there is no reason to calculate a new key.
            if (rc4Key == null || this.encryptionKey != encryptionKey ||
                    this.objectReference != objectReference) {

                this.objectReference = objectReference;

                // Step 1 to 3, bytes
                byte[] step3Bytes = resetObjectReference(objectReference, isRc4);

                // Step 4: Use the first (n+5) byes, up to a max of 16 from the MD5
                // hash
                int n = encryptionKey.length;
                rc4Key = new byte[Math.min(n + 5, BLOCK_SIZE)];
                System.arraycopy(step3Bytes, 0, rc4Key, 0, rc4Key.length);
            }

            // if we are encrypting we need to properly pad the byte array.
            int encryptionMode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
            // Set up an RC4 cipher and try to decrypt:
            try {
                SecretKeySpec key = new SecretKeySpec(rc4Key, "AES");

                // Use above as key for the RC4 encryption function.
                if (isRc4) {
                    Cipher rc4 = Cipher.getInstance("RC4");
                    rc4.init(Cipher.DECRYPT_MODE, key);
                    // finally add the stream or string data
                    CipherInputStream cin = new CipherInputStream(input, rc4);
                    return cin;
                }
                // use above a key for the AES encryption function.
                else {
                    Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    if (encryptionMode == Cipher.DECRYPT_MODE) {
                        // calculate 16 byte initialization vector.
                        byte[] initialisationVector = new byte[BLOCK_SIZE];
                        input.read(initialisationVector);
                        final IvParameterSpec iVParameterSpec = new IvParameterSpec(initialisationVector);
                        aes.init(encryptionMode, key, iVParameterSpec);
                        // finally add the stream or string data
                        CipherInputStream cin = new CipherInputStream(input, aes);
                        return cin;
                    } else {
                        final IvParameterSpec iVParameterSpec = new IvParameterSpec(generateIv());
                        aes.init(encryptionMode, key, iVParameterSpec);
                        ByteArrayOutputStream outputByteArray = new ByteArrayOutputStream();
                        // finally add the stream or string data
                        CipherOutputStream cos = new CipherOutputStream(outputByteArray, aes);
                        try {
                            byte[] data = new byte[4096];
                            int read;
                            while ((read = input.read(data)) != -1) {
                                cos.write(data, 0, read);
                            }
                        } finally {
                            cos.close();
                            input.close();
                        }
                        byte[] finalData = outputByteArray.toByteArray();
                        // add randomness to the start
                        byte[] output = new byte[iVParameterSpec.getIV().length + finalData.length];
                        System.arraycopy(iVParameterSpec.getIV(), 0, output, 0, BLOCK_SIZE);
                        System.arraycopy(finalData, 0, output, BLOCK_SIZE, finalData.length);
                        finalData = output;
                        return new ByteArrayInputStream(finalData);

                    }

                }
            } catch (NoSuchAlgorithmException ex) {
                logger.log(Level.FINE, "NoSuchAlgorithmException.", ex);
            } catch (NoSuchPaddingException ex) {
                logger.log(Level.FINE, "NoSuchPaddingException.", ex);
            } catch (InvalidKeyException ex) {
                logger.log(Level.FINE, "InvalidKeyException.", ex);
            } catch (InvalidAlgorithmParameterException ex) {
                logger.log(Level.FINE, "InvalidAlgorithmParameterException", ex);
            } catch (IOException ex) {
                logger.log(Level.FINE, "InvalidAlgorithmParameterException", ex);
            }
        }
        // Algorithm 3.1a, version 5
        else if (encryptionDictionary.getVersion() == 5) {
            // Use the 32-byte file encryption key for the AES-256 symmetric
            // key algorithm, along with the string or stream data to be encrypted.

            // Use the AES algorithm in Cipher Block Chaining (CBC) mode, which
            // requires an initialization vector. The block size parameter is
            // set to 16 bytes, and the initialization vector is a 16-byte random
            // number that is stored as the first 16 bytes of the encrypted
            // stream or string.
            try {
                // use above a key for the AES encryption function.
                SecretKeySpec key = new SecretKeySpec(encryptionKey, "AES");
                Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");

                // calculate 16 byte initialization vector.
                byte[] initialisationVector = new byte[BLOCK_SIZE];
                input.read(initialisationVector);

                final IvParameterSpec iVParameterSpec =
                        new IvParameterSpec(initialisationVector);

                aes.init(Cipher.DECRYPT_MODE, key, iVParameterSpec);

                // finally add the stream or string data
                CipherInputStream cin = new CipherInputStream(input, aes);
                return cin;

            } catch (NoSuchAlgorithmException ex) {
                logger.log(Level.FINE, "NoSuchAlgorithmException.", ex);
            } catch (NoSuchPaddingException ex) {
                logger.log(Level.FINE, "NoSuchPaddingException.", ex);
            } catch (InvalidKeyException ex) {
                logger.log(Level.FINE, "InvalidKeyException.", ex);
            } catch (InvalidAlgorithmParameterException ex) {
                logger.log(Level.FINE, "InvalidAlgorithmParameterException", ex);
            } catch (IOException ex) {
                logger.log(Level.FINE, "InvalidAlgorithmParameterException", ex);
            }
        }
        return null;
    }

    /**
     * Step 1-3 of the general encryption algorithm 3.1.  The procedure
     * is as follows:
     * <ul>
     * Treat the object number and generation number as binary integers, extend
     * the original n-byte encryption key to n + 5 bytes by appending the
     * low-order 3 bytes of the object number and the low-order 2 bytes of the
     * generation number in that order, low-order byte first. (n is 5 unless
     * the value of V in the encryption dictionary is greater than 1, in which
     * case the n is the value of Length divided by 8.)
     * <br />
     * If using the AES algorithm, extend the encryption key an additional
     * 4 bytes by adding the value "sAlT", which corresponds to the hexadecimal
     * values 0x73, 0x41, 0x6C, 0x54. (This addition is done for backward
     * compatibility and is not intended to provide additional security.)
     * </ul>
     *
     * @param objectReference pdf object reference or the identifier of the
     *                        inderect object in the case of a string.
     * @param isRc4           if true use the RC4 stream cipher, if false use the AES
     *                        symmetric block cipher.
     * @return Byte [] manipulated as specified.
     */
    public byte[] resetObjectReference(Reference objectReference, boolean isRc4) {

        // Step 1: separate object and generation numbers for objectReference
        int objectNumber = objectReference.getObjectNumber();
        int generationNumber = objectReference.getGenerationNumber();

        // Step 2:
        // v > 1 n is the value of Length divided by 8.
        int n = 5;
        if (encryptionDictionary.getVersion() > 1) {
            n = encryptionDictionary.getKeyLength() / 8;//enencryptionKey.length;
        }
        // extend the original n-byte encryption key to n + 5 bytes

        int paddingLength = 5;
        if (!isRc4) {
            paddingLength += 4;
        }

        byte[] step2Bytes = new byte[n + paddingLength];

        // make the copy
        System.arraycopy(encryptionKey, 0, step2Bytes, 0, n);

        // appending the low-order 3 bytes of the object number
        step2Bytes[n] = (byte) (objectNumber & 0xff);
        step2Bytes[n + 1] = (byte) (objectNumber >> 8 & 0xff);
        step2Bytes[n + 2] = (byte) (objectNumber >> 16 & 0xff);
        // appending low-order 2 bytes of the generation number low-order
        step2Bytes[n + 3] = (byte) (generationNumber & 0xff);
        step2Bytes[n + 4] = (byte) (generationNumber >> 8 & 0xff);

        // if using AES algorithm extend by four bytes "sAIT" (0x73, 0x41, 0x6c, 0x54)
        if (!isRc4) {
            step2Bytes[n + 5] = AES_sAIT[0];
            step2Bytes[n + 6] = AES_sAIT[1];
            step2Bytes[n + 7] = AES_sAIT[2];
            step2Bytes[n + 8] = AES_sAIT[3];
        }

        // Step 3: Initialize the MD5 hash function and pass in step2Bytes
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException builtin) {
        }
        // and pass in padded password from step 1
        md5.update(step2Bytes);

        // finally return the modified object reference
        return md5.digest();
    }

    /**
     * Encryption key algorithm 3.2 for computing an encryption key given
     * a password string.
     */
    public byte[] encryptionKeyAlgorithm(String password, int keyLength) {

        if (encryptionDictionary.getRevisionNumber() < 5) {
            // Step 1:  pad the password
            byte[] paddedPassword = padPassword(password);

            // Step 2: initialize the MD5 hash function
            MessageDigest md5 = null;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException ex) {
                logger.log(Level.FINE, "NoSuchAlgorithmException.", ex);
            }
            // and pass in padded password from step 1
            md5.update(paddedPassword);

            // Step 3: Pass the value of the encryption dictionary's 0 entry
            byte[] bigO = Utils.convertByteCharSequenceToByteArray(
                    encryptionDictionary.getBigO());
            md5.update(bigO);

            // Step 4: treat P as an unsigned 4-byte integer
            for (int i = 0, p = encryptionDictionary.getPermissions(); i < 4; i++,
                    p >>= 8) {
                md5.update((byte) (p & 0xFF));
            }

            // Step 5: Pass in the first element of the file's file identifies array
            String firstFileID = encryptionDictionary.getLiteralString(encryptionDictionary.getFileID().get(0));
            byte[] fileID = Utils.convertByteCharSequenceToByteArray(firstFileID);
            md5.update(fileID);

            // Step 6: If document metadata is not being encrypted, pass 4 bytes with
            // the value of 0xFFFFFFFF to the MD5 hash, Security handlers of revision 4 or greater)
            if (encryptionDictionary.getRevisionNumber() >= 4 &&
                    !encryptionDictionary.isEncryptMetaData()) {
                for (int i = 0; i < 4; ++i) {
                    md5.update((byte) 0xFF);
                }
            }

            // Step 7: Finish Hash.
            paddedPassword = md5.digest();

            // key length
            int keySize = encryptionDictionary.getRevisionNumber() == 2 ? 5 : keyLength / 8;
            if (keySize > paddedPassword.length) {
                keySize = paddedPassword.length;
            }
            byte[] out = new byte[keySize];

            // Step 8: Do the following 50 times: take the output from the previous
            // MD5 hash and pass it as a input into a new MD5 hash;
            // only for R >= 3
            try {
                if (encryptionDictionary.getRevisionNumber() >= 3) {
                    for (int i = 0; i < 50; i++) {
                        md5.update(paddedPassword, 0, keySize);
                        md5.digest(paddedPassword, 0, paddedPassword.length);
                    }
                }
            } catch (DigestException e) {
                logger.log(Level.WARNING, "Error creating MD5 digest.", e);
            }

            // Step 9: Set the encryption key to the first n bytes of the output from
            // the MD5 hash

            // truncate out to the appropriate value
            System.arraycopy(paddedPassword,
                    0,
                    out,
                    0,
                    keySize);
            // assign instance
            encryptionKey = out;

            return out;
        }
        // algorithm 3.2a for Revision 5
        else if (encryptionDictionary.getRevisionNumber() == 5) {
            try {
                byte[] passwordBytes = Utils.convertByteCharSequenceToByteArray(password);
                if (passwordBytes == null) {
                    passwordBytes = new byte[0];
                }

                byte[] ownerPassword = Utils.convertByteCharSequenceToByteArray(
                        encryptionDictionary.getBigO());
                byte[] userPassword = Utils.convertByteCharSequenceToByteArray(
                        encryptionDictionary.getBigU());
                // To understand the algorithm below, it is necessary to treat
                // the O and U strings in the Encrypt dictionary as made up of
                // three sections. The first 32 bytes are a hash value .
                // The next 8 bytes are called the Validation Salt.
                // The final 8 bytes are called the Key Salt.
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                // 3.) computing the SHA-256 hash of the UTF-8 password, 127
                // bytes if it is longer than 127 bytes.
                md.update(passwordBytes, 0, Math.min(passwordBytes.length, 127));
                // concatenated with the 8 bytes of owner Validation Salt,
                md.update(ownerPassword, 32, 8);
                // concatenated with the 48-byte U string
                md.update(userPassword, 0, 48);
                // calculate the 32 bit result
                byte[] hash = md.digest();
                // Check if the 32-byte result matches the first 32 bytes of the
                // O string, this is the owner password.
                boolean isOwnerPassword = byteCompare(hash, ownerPassword, 32);
                encryptionDictionary.setAuthenticatedOwnerPassword(isOwnerPassword);
                if (isOwnerPassword) {
                    // calculate an intermediate owner key
                    md.update(passwordBytes, 0, Math.min(passwordBytes.length, 127));
                    // concatenate  8 bytes of owner key salt
                    md.update(ownerPassword, 32, 8);
                    // concatenated with the 48-byte u string
                    md.update(userPassword, 0, 48);
                    hash = md.digest();
                    // the 32 byte hash result is the key to decrypt the 32byte
                    // oe string using AES-256 in CBC mode with no padding and an
                    // initialization vector of zero.
                    // the 32byte result is the file encryption key.
                    byte[] oePassword = Utils.convertByteCharSequenceToByteArray(
                            encryptionDictionary.getBigOE());
                    encryptionKey = AES256CBC(hash, oePassword);
                }
                // 4.)test the password against the user password.
                else {
                    // concatenate password
                    md.update(passwordBytes, 0, Math.min(passwordBytes.length, 127));
                    // concatenated with the 8 bytes of user Validation Salt
                    md.update(userPassword, 32, 8);
                    hash = md.digest();
                    // test first 32 bytes against the user string.
                    boolean isUserPassword = byteCompare(hash, userPassword, 32);
                    encryptionDictionary.setAuthenticatedUserPassword(isUserPassword);
                    if (isUserPassword) {
                        // calculate an intermediate owner key
                        md.update(passwordBytes, 0, Math.min(passwordBytes.length, 127));
                        // concatenate  8 bytes of owner key salt
                        md.update(userPassword, 40, 8);
                        hash = md.digest();
                        // the 32 byte hash result is the key to decrypt the 32byte
                        // ue string using AES-256 in CBC mode with no padding and an
                        // initialization vector of zero.
                        // the 32byte result is the file encryption key.
                        byte[] uePassword = Utils.convertByteCharSequenceToByteArray(
                                encryptionDictionary.getBigUE());
                        encryptionKey = AES256CBC(hash, uePassword);
                    } else {
                        logger.warning("User password is incorrect. ");
                    }
                }
                // 5.)Decrypt the 16-byte Perms string using AES-256 in ECB mode
                // with an initialization vector of zero and the file encryption
                // key as the key.
                byte[] perms = Utils.convertByteCharSequenceToByteArray(
                        encryptionDictionary.getPerms());
                byte[] decryptedPerms = AES256CBC(encryptionKey, perms);

                // Verify that bytes 9-11 of the result are the characters 'a', 'd', 'b'.
                if (decryptedPerms[9] != (byte) 'a' ||
                        decryptedPerms[10] != (byte) 'd' ||
                        decryptedPerms[11] != (byte) 'b') {
                    logger.warning("User password is incorrect.");
                    return null;
                }
                // Bytes 0-3 of the decrypted Perms entry, treated as a
                // little-endian integer, are the user permissions. They should
                // match the value in the P key.
                int permissions = (decryptedPerms[0] & 0xff) |
                        ((decryptedPerms[1] & 0xff) << 8) |
                        ((decryptedPerms[2] & 0xff) << 16) |
                        ((decryptedPerms[2] & 0xff) << 24);
                int pPermissions = encryptionDictionary.getPermissions();
                if (pPermissions != permissions) {
                    logger.warning("Perms and P do not match");
                }
                return encryptionKey;
            } catch (NoSuchAlgorithmException e) {
                logger.warning("Error computing the the 3.2a Encryption key.");
            }
        } else {
            logger.warning("Adobe standard Encryption R = 6 is not supported.");
        }
        return null;
    }

    /**
     * ToDo: xjava.security.Padding,  look at class for interface to see
     * if PDFPadding class could/should be built
     * <p/>
     * Pad or truncate the password string to exactly 32 bytes.  If the
     * password is more than 32 bytes long, use only its first 32 bytes; if it
     * is less than 32 bytes long, pad it by appending the required number of
     * additional bytes from the beginning of the PADDING string.
     * <p/>
     * NOTE: This is algorithm is the <b>1st</b> step of <b>algorithm 3.2</b>
     * and is commonly used by other methods in this class
     *
     * @param password password to padded
     * @return returned updated password with appropriate padding applied
     */
    protected static byte[] padPassword(String password) {

        // create the standard 32 byte password
        byte[] paddedPassword = new byte[32];

        // Passwords can be null, if so set it to an empty string
        if (password == null || "".equals(password)) {
            return PADDING;
        }


        int passwordLength = Math.min(password.length(), 32);

        byte[] bytePassword =
                Utils.convertByteCharSequenceToByteArray(password);
        // copy passwords bytes, but truncate the password is > 32 bytes
        System.arraycopy(bytePassword, 0, paddedPassword, 0, passwordLength);

        // pad the password if it is < 32 bytes
        System.arraycopy(PADDING,
                0,
                paddedPassword,
                // start copy at end of string
                passwordLength,
                // append need bytes from PADDING
                32 - passwordLength);

        return paddedPassword;
    }

    /**
     * Computing Owner password value, Algorithm 3.3.
     * <p/>
     * AESv3 passwords are not handle by this method, instead use
     * {@link #generalEncryptionAlgorithm(org.icepdf.core.pobjects.Reference, byte[], String, byte[], boolean)}
     * If the result is not null then the encryptionDictionary will container
     * values for isAuthenticatedOwnerPassword and isAuthenticatedUserPassword.
     *
     * @param ownerPassword    owner pasword string. If there is no owner,
     *                         password use the user password instead.
     * @param userPassword     user password.
     * @param isAuthentication if true, only steps 1-4 of the algorithm will be
     *                         completed.  If false, all 8 steps of the algorithm will be
     *                         completed
     *                         <b>Note : </b><br />
     *                         There may be a bug in this algorithm when all 8 steps are called.
     *                         1-4 are work properly, but 1-8 can not generate an O value that is
     *                         the same as the orgional documents O.  This is not a currently a
     *                         problem as we do not author PDF documents.
     */
    public byte[] calculateOwnerPassword(String ownerPassword,
                                         String userPassword,
                                         boolean isAuthentication) {
        // Step 1:  padd the owner password, use the userPassword if empty.
        if ("".equals(ownerPassword) && !"".equals(userPassword)) {
            ownerPassword = userPassword;
        }
        byte[] paddedOwnerPassword = padPassword(ownerPassword);

        // Step 2: Initialize the MD5 hash function and pass in step 2.
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.FINE, "Could not fint MD5 Digest", e);
        }
        // and pass in padded password from step 1
        paddedOwnerPassword = md5.digest(paddedOwnerPassword);

        // Step 3: Do the following 50 times: take the output from the previous
        // MD5 hash and pass it as input into a new MD5 hash;
        // only for R = 3
        if (encryptionDictionary.getRevisionNumber() >= 3) {
            for (int i = 0; i < 50; i++) {
                paddedOwnerPassword = md5.digest(paddedOwnerPassword);
            }
        }

        // Step 4: Create an RC4 encryption key using the first n bytes of the
        // final MD5 hash, where n is always 5 for revision 2 and the value
        // of the encryption dictionary's Length entry for revision 3.
        // Set up an RC4 cipher and try to encrypt:

        // grap the needed n bytes.
        int dataSize = 5; // default for R == 2
        if (encryptionDictionary.getRevisionNumber() >= 3) {
            dataSize = encryptionDictionary.getKeyLength() / 8;
        }
        if (dataSize > paddedOwnerPassword.length) {
            dataSize = paddedOwnerPassword.length;
        }

        // truncate the byte array RC4 encryption key
        byte[] encryptionKey = new byte[dataSize];

        System.arraycopy(paddedOwnerPassword, 0, encryptionKey, 0, dataSize);

        // Key is needed by algorithm 3.7, Authenticating owner password
        if (isAuthentication) {
            return encryptionKey;
        }

        // Step 5: Pad or truncate the user password string
        byte[] paddedUserPassword = padPassword(userPassword);

        // Step 6: Encrypt the result of step 4, using the RC4 encryption
        // function with the encryption key obtained in step 4
        byte[] finalData = null;
        try {
            // Use above as key for the RC4 encryption function.
            SecretKeySpec key = new SecretKeySpec(encryptionKey, "RC4");
            Cipher rc4 = Cipher.getInstance("RC4");
            rc4.init(Cipher.ENCRYPT_MODE, key);

            // finally add the stream or string data
            finalData = rc4.update(paddedUserPassword);


            // Step 7: Do the following 19 times: Take the output from the previous
            // invocation of the RC4 function and pass it as input to a new
            // invocation of the function; use an encryption key generated by taking
            // each byte of the encryption key in step 4 and performing an XOR
            // operation between that byte and the single-byte value of the
            // iteration counter
            if (encryptionDictionary.getRevisionNumber() >= 3) {

                // key to be made on each interaction
                byte[] indexedKey = new byte[encryptionKey.length];
                // start the 19? interactions
                for (int i = 1; i <= 19; i++) {

                    // build new key for each i xor on each byte
                    for (int j = 0; j < encryptionKey.length; j++) {
                        indexedKey[j] = (byte) (encryptionKey[j] ^ i);
                    }
                    // create new key and init rc4
                    key = new SecretKeySpec(indexedKey, "RC4");
                    //Cipher tmpRc4 = Cipher.getInstance("RC4");
                    rc4.init(Cipher.ENCRYPT_MODE, key);
                    // encrypt the old data with the new key
                    finalData = rc4.update(finalData);
                }
            }

        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.FINE, "NoSuchAlgorithmException.", ex);
        } catch (NoSuchPaddingException ex) {
            logger.log(Level.FINE, "NoSuchPaddingException.", ex);
        } catch (InvalidKeyException ex) {
            logger.log(Level.FINE, "InvalidKeyException.", ex);
        }


        // Debug Code.
//        String O = encryptionDictionary.getBigO();
//        System.out.print("Original O " + O.length() + " ");
//        byte[] bigO = new byte[O.length()];
//        for (int i=0; i < bigO.length; i++){
//            //bigO[i] = (byte)O.charAt(i);
//            System.out.print((int)O.charAt(i));
//        }
//        System.out.println();
//
//        System.out.print("new      O " + finalData.length + " ");
//        for (int i=0; i < finalData.length; i++){
//            System.out.print((int)finalData[i]);
//        }
//        System.out.println();

        // Step 8: return the final invocation of the RC4 function as O
        return finalData;
    }

    /**
     * Computing Owner password value, Algorithm 3.4 is respected for
     * Revision = 2 and Algorithm 3.5 is respected for Revisison = 3, null
     * otherwise.
     * <p/>
     * AESv3 passwords are not handle by this method, instead use
     * {@link #generalEncryptionAlgorithm(org.icepdf.core.pobjects.Reference, byte[], String, byte[], boolean)}
     * If the result is not null then the encryptionDictionary will container
     * values for isAuthenticatedOwnerPassword and isAuthenticatedUserPassword.
     *
     * @param userPassword user password.
     * @return byte array representing the U value for the encryption dictionary
     */
    public byte[] calculateUserPassword(String userPassword) {

        // Step 1: Create an encryption key based on the user password String,
        // as described in Algorithm 3.2
        byte[] encryptionKey = encryptionKeyAlgorithm(
                userPassword,
                encryptionDictionary.getKeyLength());

        // Algorithm 3.4 steps, 2 - 3
        if (encryptionDictionary.getRevisionNumber() == 2) {
            // Step 2: Encrypt the 32-byte padding string show in step 1, using
            // an RC4 encryption function with the encryption key from the
            // preceding step

            // 32-byte padding string
            byte[] paddedUserPassword = PADDING.clone();
            // encrypt the data
            byte[] finalData = null;
            try {
                // Use above as key for the RC4 encryption function.
                SecretKeySpec key = new SecretKeySpec(encryptionKey, "RC4");
                Cipher rc4 = Cipher.getInstance("RC4");
                rc4.init(Cipher.ENCRYPT_MODE, key);

                // finally encrypt the padding string
                finalData = rc4.doFinal(paddedUserPassword);

            } catch (NoSuchAlgorithmException ex) {
                logger.log(Level.FINE, "NoSuchAlgorithmException.", ex);
            } catch (IllegalBlockSizeException ex) {
                logger.log(Level.FINE, "IllegalBlockSizeException.", ex);
            } catch (BadPaddingException ex) {
                logger.log(Level.FINE, "BadPaddingException.", ex);
            } catch (NoSuchPaddingException ex) {
                logger.log(Level.FINE, "NoSuchPaddingException.", ex);
            } catch (InvalidKeyException ex) {
                logger.log(Level.FINE, "InvalidKeyException.", ex);
            }
            // Step 3: return the result of step 2 as the value of the U entry
            return finalData;
        }
        // algorithm 3.5 steps, 2 - 6
        else if (encryptionDictionary.getRevisionNumber() >= 3 &&
                encryptionDictionary.getRevisionNumber() < 5) {
            // Step 2: Initialize the MD5 hash function and pass the 32-byte
            // padding string shown in step 1 of Algorithm 3.2 as input to
            // this function
            byte[] paddedUserPassword = PADDING.clone();
            MessageDigest md5 = null;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                logger.log(Level.FINE, "MD5 digester could not be found", e);
            }
            // and pass in padded password 32-byte padding string
            md5.update(paddedUserPassword);

            // Step 3: Pass the first element of the files identify array to the
            // hash function and finish the hash.
            String firstFileID = encryptionDictionary.getLiteralString(encryptionDictionary.getFileID().get(0));
            byte[] fileID = Utils.convertByteCharSequenceToByteArray(firstFileID);
            byte[] encryptData = md5.digest(fileID);

            // Step 4: Encrypt the 16 byte result of the hash, using an RC4
            // encryption function with the encryption key from step 1
            //System.out.println("R=3 " + encryptData.length);

            // The final data should be 16 bytes long
            // currently no checking for this.

            try {
                // Use above as key for the RC4 encryption function.
                SecretKeySpec key = new SecretKeySpec(encryptionKey, "RC4");
                Cipher rc4 = Cipher.getInstance("RC4");
                rc4.init(Cipher.ENCRYPT_MODE, key);

                // finally encrypt the padding string
                encryptData = rc4.update(encryptData);

                // Step 5: Do the following 19 times: Take the output from the previous
                // invocation of the RC4 function and pass it as input to a new
                // invocation of the function; use an encryption key generated by taking
                // each byte of the encryption key in step 4 and performing an XOR
                // operation between that byte and the single-byte value of the
                // iteration counter

                // key to be made on each interaction
                byte[] indexedKey = new byte[encryptionKey.length];
                // start the 19? interactions
                for (int i = 1; i <= 19; i++) {

                    // build new key for each i xor on each byte
                    for (int j = 0; j < encryptionKey.length; j++) {
                        indexedKey[j] = (byte) (encryptionKey[j] ^ (byte) i);
                    }
                    // create new key and init rc4
                    key = new SecretKeySpec(indexedKey, "RC4");
                    rc4.init(Cipher.ENCRYPT_MODE, key);
                    // encrypt the old data with the new key
                    encryptData = rc4.update(encryptData);
                }

            } catch (NoSuchAlgorithmException ex) {
                logger.log(Level.FINE, "NoSuchAlgorithmException.", ex);
            } catch (NoSuchPaddingException ex) {
                logger.log(Level.FINE, "NoSuchPaddingException.", ex);
            } catch (InvalidKeyException ex) {
                logger.log(Level.FINE, "InvalidKeyException.", ex);
            }
            // Step 6: Append 16 bytes of arbitrary padding to the output from
            // the final invocation of the RC4 function and return the 32-byte
            // result as the value of the U entry.
            byte[] finalData = new byte[32];
            System.arraycopy(encryptData, 0, finalData, 0, BLOCK_SIZE);
            System.arraycopy(PADDING, 0, finalData, BLOCK_SIZE, BLOCK_SIZE);

            return finalData;
        } else {
            return null;
        }
    }

    /**
     * Authenticating the user password,  algorithm 3.6
     *
     * @param userPassword user password to check for authenticity
     * @return true if the userPassword matches the value the encryption
     * dictionary U value, false otherwise.
     */
    public boolean authenticateUserPassword(String userPassword) {
        // Step 1: Perform all but the last step of Algorithm 3.4(Revision 2) or
        // Algorithm 3.5 (Revision 3) using the supplied password string.
        byte[] tmpUValue = calculateUserPassword(userPassword);

        byte[] bigU = Utils.convertByteCharSequenceToByteArray(
                encryptionDictionary.getBigU());

        byte[] trunkUValue;
        // compare all 32 bytes.
        if (encryptionDictionary.getRevisionNumber() == 2) {
            trunkUValue = new byte[32];
            System.arraycopy(tmpUValue, 0, trunkUValue, 0, trunkUValue.length);
        }
        // truncate to first 16 bytes for R >= 3
        else if (encryptionDictionary.getRevisionNumber() >= 3 &&
                encryptionDictionary.getRevisionNumber() < 5) {
            trunkUValue = new byte[BLOCK_SIZE];
            System.arraycopy(tmpUValue, 0, trunkUValue, 0, trunkUValue.length);
        } else {
            return false;
        }

        // Step 2: If the result of step 1 is equal o the value of the
        // encryption dictionary's U entry, the password supplied is the correct
        // user password.

        boolean found = true;
        for (int i = 0; i < trunkUValue.length; i++) {
            if (trunkUValue[i] != bigU[i]) {
                found = false;
                break;
            }
        }
        return found;
    }

    /**
     * Authenticating the owner password,  algorithm 3.7
     */
    public boolean authenticateOwnerPassword(String ownerPassword) {
        // Step 1: Computer an encryption key from the supplied password string,
        // as described in steps 1 to 4 of algorithm 3.3.
        byte[] encryptionKey = calculateOwnerPassword(ownerPassword,
                "", true);

        // Step 2: start decryption of O
        byte[] decryptedO = null;
        try {
            // get bigO value
            byte[] bigO = Utils.convertByteCharSequenceToByteArray(
                    encryptionDictionary.getBigO());
            if (encryptionDictionary.getRevisionNumber() == 2) {
                // Step 2 (R == 2):  decrypt the value of the encryption dictionary
                // O entry, using an RC4 encryption function with the encryption
                // key computed in step 1.

                // Use above as key for the RC4 encryption function.
                SecretKeySpec key = new SecretKeySpec(encryptionKey, "RC4");
                Cipher rc4 = Cipher.getInstance("RC4");
                rc4.init(Cipher.DECRYPT_MODE, key);
                decryptedO = rc4.doFinal(bigO);
            }
            // Step 2 (R >= 3): Do the following 19 times: Take the output from the previous
            // invocation of the RC4 function and pass it as input to a new
            // invocation of the function; use an encryption key generated by taking
            // each byte of the encryption key in step 4 and performing an XOR
            // operation between that byte and the single-byte value of the
            // iteration counter
            else {//if (encryptionDictionary.getRevisionNumber() >= 3){
                // key to be made on each interaction
                byte[] indexedKey = new byte[encryptionKey.length];

                decryptedO = bigO;
                // start the 19->0? interactions
                for (int i = 19; i >= 0; i--) {

                    // build new key for each i xor on each byte
                    for (int j = 0; j < indexedKey.length; j++) {
                        indexedKey[j] = (byte) (encryptionKey[j] ^ (byte) i);
                    }
                    // create new key and init rc4
                    SecretKeySpec key = new SecretKeySpec(indexedKey, "RC4");
                    Cipher rc4 = Cipher.getInstance("RC4");
                    rc4.init(Cipher.ENCRYPT_MODE, key);
                    // encrypt the old data with the new key
                    decryptedO = rc4.update(decryptedO);
                }
            }

            // Step 3: The result of step 2 purports to be the user password.
            // Authenticate this user password using Algorithm 3.6.  If it is found
            // to be correct, the password supplied is the correct owner password.

            String tmpUserPassword = Utils.convertByteArrayToByteString(decryptedO);
            //System.out.println("tmp user password " + tmpUserPassword);
            boolean isValid = authenticateUserPassword(tmpUserPassword);

            if (isValid) {
                userPassword = tmpUserPassword;
                this.ownerPassword = ownerPassword;
                // setup permissions if valid
            }

            return isValid;
        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.FINE, "NoSuchAlgorithmException.", ex);
        } catch (IllegalBlockSizeException ex) {
            logger.log(Level.FINE, "IllegalBlockSizeException.", ex);
        } catch (BadPaddingException ex) {
            logger.log(Level.FINE, "BadPaddingException.", ex);
        } catch (NoSuchPaddingException ex) {
            logger.log(Level.FINE, "NoSuchPaddingException.", ex);
        } catch (InvalidKeyException ex) {
            logger.log(Level.FINE, "InvalidKeyException.", ex);
        }

        return false;

    }

    public String getUserPassword() {
        return userPassword;
    }

    public String getOwnerPassword() {
        return ownerPassword;
    }

    /**
     * Utility to decrypt the encryptedString via the intermediateKey.  AES
     * encryption with cypher block chaining and no padding.
     *
     * @param intermediateKey key to use for decryption
     * @param encryptedString byte[] to decrypt
     * @return
     */
    private static byte[] AES256CBC(byte[] intermediateKey, byte[] encryptedString) {
        byte[] finalData = null;
        try {
            // AES with cipher block chaining and no padding
            SecretKeySpec key = new SecretKeySpec(intermediateKey, "AES");
            Cipher aes = Cipher.getInstance("AES/CBC/NoPadding");
            // empty initialization vector
            final IvParameterSpec iVParameterSpec =
                    new IvParameterSpec(new byte[BLOCK_SIZE]);
            // go!
            aes.init(Cipher.DECRYPT_MODE, key, iVParameterSpec);
            // finally add the stream or string data
            finalData = aes.doFinal(encryptedString);
        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.FINE, "NoSuchAlgorithmException.", ex);
        } catch (IllegalBlockSizeException ex) {
            logger.log(Level.FINE, "IllegalBlockSizeException.", ex);
        } catch (BadPaddingException ex) {
            logger.log(Level.FINE, "BadPaddingException.", ex);
        } catch (NoSuchPaddingException ex) {
            logger.log(Level.FINE, "NoSuchPaddingException.", ex);
        } catch (InvalidKeyException ex) {
            logger.log(Level.FINE, "InvalidKeyException.", ex);
        } catch (InvalidAlgorithmParameterException ex) {
            logger.log(Level.FINE, "InvalidAlgorithmParameterException", ex);
        }
        return finalData;
    }

    /**
     * Compare two byte arrays to the specified max index.  No check is made
     * for an index out of bounds error.
     *
     * @param byteArray1 byte array to compare
     * @param byteArray2 byte array to compare
     * @param range      number of elements to compare starting at zero.
     * @return true if the
     */
    private static boolean byteCompare(byte[] byteArray1, byte[] byteArray2, int range) {
        for (int i = 0; i < range; i++) {
            if (byteArray1[i] != byteArray2[i]) {
                return false;
            }
        }
        return true;
    }
}
