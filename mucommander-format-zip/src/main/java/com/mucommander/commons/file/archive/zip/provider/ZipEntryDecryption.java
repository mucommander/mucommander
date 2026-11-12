/**
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.file.archive.zip.provider;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.ZipException;

import com.mucommander.commons.io.StreamUtils;

import net.lingala.zip4j.crypto.AESDecrypter;
import net.lingala.zip4j.crypto.StandardDecrypter;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.enums.AesKeyStrength;

/**
 * Creates streams that decrypt the data of encrypted Zip entries. Both the WinZip AES format (AE-1 / AE-2) and the
 * traditional PKWARE encryption (ZipCrypto) are supported. The cryptographic primitives are provided by the
 * <a href="https://github.com/srikanth-lingala/zip4j">Zip4j</a> library.
 *
 * <p>Passwords are encoded in UTF-8 to derive the encryption keys, like WinZip, 7-Zip and Zip4j do.</p>
 *
 * @see <a href="https://www.winzip.com/en/support/aes-encryption/">WinZip AES encryption specification</a>
 */
final class ZipEntryDecryption {

    /** Length of the password verification value that precedes AES-encrypted data */
    private static final int AES_PASSWORD_VERIFIER_LENGTH = 2;

    /** Length of the authentication code that follows AES-encrypted data */
    private static final int AES_AUTH_CODE_LENGTH = 10;

    /** Length of the encryption header that precedes ZipCrypto-encrypted data */
    private static final int ZIP_CRYPTO_HEADER_LENGTH = 12;

    /** Encode passwords in UTF-8 when deriving the encryption keys */
    private static final boolean USE_UTF8_FOR_PASSWORD = true;

    private ZipEntryDecryption() {
    }

    /**
     * Returns a stream that decrypts the data of the given encrypted entry, read from the given stream. The password is
     * verified before this method returns, so a wrong password is detected without reading the entry's data.
     *
     * @param ze the encrypted entry
     * @param rawIn a stream positioned at the beginning of the entry's (encrypted) data, and bounded to the entry's
     * compressed size
     * @param password the password used to encrypt the entry
     * @return a stream that returns the decrypted (but still compressed) data of the entry
     * @throws ZipException if no password was provided, or if the entry's encryption method is not supported
     * @throws net.lingala.zip4j.exception.ZipException of type
     * {@link net.lingala.zip4j.exception.ZipException.Type#WRONG_PASSWORD WRONG_PASSWORD} if the password is wrong
     * @throws IOException if an I/O error occurred while reading the encryption header
     */
    static InputStream createDecryptingStream(ZipEntry ze, InputStream rawIn, String password) throws IOException {
        if (password == null)
            throw new ZipException("A password is required to read encrypted entry " + ze.getName());

        switch (ze.getEntryInfo().encryption) {
            case AES:
                return createAesDecryptingStream(ze, rawIn, password);
            case ZIP_CRYPTO:
                return createZipCryptoDecryptingStream(ze, rawIn, password);
            default:
                throw new ZipException("Unsupported encryption method for entry " + ze.getName());
        }
    }

    /**
     * Returns a stream that reads the given uncompressed data stream and, once its end has been reached, reads the
     * remaining data from the given decrypting stream. An InflaterInputStream stops reading once the end of the deflated
     * data is reached, which may come before the end of the encrypted data (if the recorded compressed size is larger):
     * reading the remaining data ensures that the AES authentication code is always verified.
     *
     * @param uncompressedIn the stream that returns the uncompressed data, read from <code>decryptedIn</code>
     * @param decryptedIn the stream that decrypts the entry's data
     * @return a stream that returns the uncompressed data
     */
    static InputStream readAllOnEndOfStream(InputStream uncompressedIn, InputStream decryptedIn) {
        return new FilterInputStream(uncompressedIn) {
            @Override
            public int read() throws IOException {
                int b = super.read();
                if (b == -1)
                    StreamUtils.readUntilEOF(decryptedIn);
                return b;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int nbRead = super.read(b, off, len);
                if (nbRead == -1)
                    StreamUtils.readUntilEOF(decryptedIn);
                return nbRead;
            }
        };
    }

    /**
     * Returns a stream that decrypts the data of an AES-encrypted entry. The entry's data is laid out as follows:
     * salt (8, 12 or 16 bytes depending on the key strength), password verification value (2 bytes), encrypted data,
     * authentication code (10 bytes).
     */
    private static InputStream createAesDecryptingStream(ZipEntry ze, InputStream rawIn, String password) throws IOException {
        AesKeyStrength keyStrength = ze.getEntryInfo().aesKeyStrength;
        long encryptedDataLength = ze.getCompressedSize() - keyStrength.getSaltLength() - AES_PASSWORD_VERIFIER_LENGTH - AES_AUTH_CODE_LENGTH;
        if (encryptedDataLength < 0)
            throw new ZipException("Invalid size for AES-encrypted entry " + ze.getName());

        byte[] salt = StreamUtils.readFully(rawIn, new byte[keyStrength.getSaltLength()]);
        byte[] passwordVerifier = StreamUtils.readFully(rawIn, new byte[AES_PASSWORD_VERIFIER_LENGTH]);

        // The decrypter derives the keys from the password and salt, and throws an exception if the derived
        // password verification value does not match the stored one, i.e. if the password is wrong
        AESExtraDataRecord aesExtraDataRecord = new AESExtraDataRecord();
        aesExtraDataRecord.setAesKeyStrength(keyStrength);
        AESDecrypter decrypter = new AESDecrypter(aesExtraDataRecord, password.toCharArray(), salt, passwordVerifier, USE_UTF8_FOR_PASSWORD);

        return new AesDecryptingInputStream(rawIn, decrypter, encryptedDataLength, ze.getName());
    }

    /**
     * Returns a stream that decrypts the data of a ZipCrypto-encrypted entry. The entry's data is preceded by a 12-byte
     * encryption header whose last byte is used to verify the password.
     */
    private static InputStream createZipCryptoDecryptingStream(ZipEntry ze, InputStream rawIn, String password) throws IOException {
        byte[] header = StreamUtils.readFully(rawIn, new byte[ZIP_CRYPTO_HEADER_LENGTH]);

        // The decrypter initializes the keys from the password and throws an exception if the verification byte of
        // the decrypted header matches neither the entry's CRC nor its DOS time, i.e. if the password is wrong
        StandardDecrypter decrypter = new StandardDecrypter(password.toCharArray(), ze.getCrc(), ze.getDosTime(), header, USE_UTF8_FOR_PASSWORD);

        // ZipCrypto is a stream cipher, data can be decrypted in chunks of any size
        return new FilterInputStream(rawIn) {
            private final byte[] singleByte = new byte[1];

            @Override
            public int read() throws IOException {
                return read(singleByte, 0, 1) == -1 ? -1 : singleByte[0] & 0xFF;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int nbRead = in.read(b, off, len);
                if (nbRead > 0)
                    decrypter.decryptData(b, off, nbRead);
                return nbRead;
            }

            @Override
            public long skip(long n) throws IOException {
                // Skipping would desynchronize the cipher, bytes have to be read and decrypted
                byte[] b = new byte[(int) Math.min(n, 8192)];
                int nbRead = read(b, 0, b.length);
                return Math.max(nbRead, 0);
            }

            @Override
            public boolean markSupported() {
                return false;
            }
        };
    }

    /**
     * Decrypts the data of an AES-encrypted entry and verifies its authentication code once all data has been read.
     *
     * <p>AES is used in CTR mode where each call to {@link AESDecrypter#decryptData(byte[], int, int)} starts with a
     * new 16-byte counter block, so data has to be decrypted in chunks whose size is a multiple of 16 bytes (except for
     * the last one). This is why data is read and decrypted through an internal buffer.</p>
     */
    private static class AesDecryptingInputStream extends InputStream {

        /** Size of the internal buffer, must be a multiple of the AES block size (16 bytes) */
        private static final int BUFFER_SIZE = 8192;

        private final InputStream in;
        private final AESDecrypter decrypter;
        private final String entryName;

        /** Number of encrypted bytes that haven't been read from the underlying stream yet */
        private long remaining;

        private final byte[] buffer = new byte[BUFFER_SIZE];
        private int bufferPos;
        private int bufferLimit;

        /** <code>true</code> once the authentication code has been verified */
        private boolean authenticated;

        AesDecryptingInputStream(InputStream in, AESDecrypter decrypter, long encryptedDataLength, String entryName) {
            this.in = in;
            this.decrypter = decrypter;
            this.remaining = encryptedDataLength;
            this.entryName = entryName;
        }

        /**
         * Reads and decrypts the next chunk of data into the buffer.
         *
         * @return <code>false</code> if all data has been read already
         */
        private boolean fillBuffer() throws IOException {
            if (remaining == 0) {
                verifyAuthenticationCode();
                return false;
            }

            int chunkLength = (int) Math.min(BUFFER_SIZE, remaining);
            StreamUtils.readFully(in, buffer, 0, chunkLength);
            decrypter.decryptData(buffer, 0, chunkLength);
            remaining -= chunkLength;
            bufferPos = 0;
            bufferLimit = chunkLength;

            // Verify the authentication code as soon as the last chunk has been decrypted: a consumer such as an
            // InflaterInputStream may stop reading once it has all the data it needs, without hitting the end of stream
            if (remaining == 0)
                verifyAuthenticationCode();

            return true;
        }

        /**
         * Compares the authentication code stored after the encrypted data with the one calculated over the data.
         *
         * @throws ZipException if the authentication codes do not match, i.e. if the data is corrupted
         */
        private void verifyAuthenticationCode() throws IOException {
            if (authenticated)
                return;

            byte[] storedAuthCode = StreamUtils.readFully(in, new byte[AES_AUTH_CODE_LENGTH]);
            byte[] calculatedAuthCode = Arrays.copyOf(decrypter.getCalculatedAuthenticationBytes(0), AES_AUTH_CODE_LENGTH);
            if (!Arrays.equals(storedAuthCode, calculatedAuthCode))
                throw new ZipException("AES authentication failed, entry " + entryName + " is corrupted");

            authenticated = true;
        }

        @Override
        public int read() throws IOException {
            if (bufferPos == bufferLimit && !fillBuffer())
                return -1;

            return buffer[bufferPos++] & 0xFF;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (len == 0)
                return 0;

            if (bufferPos == bufferLimit && !fillBuffer())
                return -1;

            int nbBytes = Math.min(len, bufferLimit - bufferPos);
            System.arraycopy(buffer, bufferPos, b, off, nbBytes);
            bufferPos += nbBytes;
            return nbBytes;
        }

        @Override
        public int available() {
            return bufferLimit - bufferPos;
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }
}
