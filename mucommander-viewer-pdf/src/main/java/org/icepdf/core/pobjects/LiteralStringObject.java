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
package org.icepdf.core.pobjects;

import org.icepdf.core.pobjects.fonts.Font;
import org.icepdf.core.pobjects.fonts.FontFile;
import org.icepdf.core.pobjects.fonts.ofont.OFont;
import org.icepdf.core.pobjects.security.SecurityManager;
import org.icepdf.core.util.Utils;

/**
 * <p>This class represents a PDF Literal String Object.  Literal String
 * objects are written as a sequence of literal characters enclosed in
 * parentheses ().</p>
 *
 * @since 2.0
 */
public class LiteralStringObject implements StringObject {

    // core data used to represent the literal string information
    private StringBuilder stringData;

    private static char[] hexChar = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f'};
    // Reference is need for standard encryption
    Reference reference;

    /**
     * <p>Creates a new literal string object so that it represents the same
     * sequence of bytes as in the bytes argument.  In other words, the
     * initial content of the literal string is the characters represented
     * by the byte data.</p>
     *
     * @param bytes array of bytes which will be interpreted as literal
     *              character data.
     */
    public LiteralStringObject(byte[] bytes) {
        this(new StringBuilder(bytes.length).append(new String(bytes)));
    }

    public LiteralStringObject(StringBuilder chars, boolean dif) {
        stringData = chars;
    }

    /**
     * <p>Creates a new literal string object so that it represents the same
     * sequence of character data specifed by the argument.</p>
     *
     * @param string the initial contents of the literal string object
     */
    public LiteralStringObject(String string) {
        // append string data
        // escape the string for any special characters.
        //   \(  - left parenthesis
        //   \)  - right parenthesis
        //   \\  - backslash
        stringData = new StringBuilder(string.replaceAll("(?=[()\\\\])", "\\\\"));
    }

    /**
     * <p>Creates a new literal string object so that it represents the same
     * sequence of character data specified by the arguments.  The string
     * value is assumed to be unencrypted and will be encrypted.  The
     * method #LiteralStringObject(String string) should be used if the string
     * is all ready encrypted. This method is used for creating new
     * LiteralStringObject's that are created post document parse, like annotation
     * property values. </p>
     *
     * @param string          the initial contents of the literal string object,
     *                        unencrypted.
     * @param reference       of parent PObject
     * @param securityManager security manager used ot encrypt the string.
     */
    public LiteralStringObject(String string, Reference reference,
                               SecurityManager securityManager) {
        // append string data
        this.reference = reference;
        // convert string to octal encoded.
        string = Utils.convertStringToOctal(string);
        // decrypt the string.
        stringData = new StringBuilder(
                encryption(string, false, securityManager));
    }

    /**
     * <p>Creates a new literal string object so that it represents the same
     * sequence of character data specifed by the argument. The first and last
     * characters of the StringBuffer are removed.  This constructor should
     * only be used in the context of the parser which has leading and ending
     * parentheses which are removed by this method.</p>
     *
     * @param stringBuffer the initial contents of the literal string object
     */
    public LiteralStringObject(StringBuilder stringBuffer) {
        // remove parentheses, passed in by parser
        stringBuffer.deleteCharAt(0);
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        // append string data
        stringData = new StringBuilder(stringBuffer.length());
        stringData.append(stringBuffer.toString());
    }

    /**
     * Gets the integer value of the hexidecimal data specified by the start and
     * offset parameters.
     *
     * @param start  the begining index, inclusive
     * @param offset the length of bytes to process
     * @return unsigned integer value of the specifed data range
     */
    public int getUnsignedInt(int start, int offset) {
        if (start < 0 || stringData.length() < (start + offset))
            return stringData.charAt(0);

        if (offset == 1) {
            return stringData.charAt(start);
        }
        if (offset == 2) {
            return ((stringData.charAt(start) & 0xFF) << 8) |
                    ((stringData.charAt(start + 1)) & 0xFF);
        } else if (offset == 4) {
            return ((stringData.charAt(start) & 0xFF) << 24) |
                    ((stringData.charAt(start + 1) & 0xFF) << 16) |
                    ((stringData.charAt(start + 2) & 0xFF) << 8) |
                    ((stringData.charAt(start + 3)) & 0xFF);
        } else {
            return 0;
        }
    }

    /**
     * <p>Returns a string representation of the object.</p>
     *
     * @return a string representing the object.
     */
    public String toString() {
        return stringData.toString();
    }

    /**
     * <p>Gets a hexadecimal String representation of this object's data, which
     * is converted to hexadecimal form.</p>
     *
     * @return a String representation of the objects data.
     */
    public String getHexString() {
        return stringToHex(stringData).toString();
    }

    /**
     * <p>Gets a hexadecimal StringBuffer representation of this object's data,
     * which is converted to hexadecimal form.</p>
     *
     * @return a StringBufffer representation of the object's data in hexadecimal
     *         notation.
     */
    public StringBuilder getHexStringBuffer() {
        return stringToHex(stringData);
    }

    /**
     * <p>Gets a literal StringBuffer representation of this object's data
     * which is in fact, the raw data contained in this object.</p>
     *
     * @return a StringBuffer representation of the object's data.
     */
    public StringBuilder getLiteralStringBuffer() {
        return stringData;
    }

    /**
     * <p>Gets a literal String representation of this object's data,
     * which is in fact, the raw data contained in this object.</p>
     *
     * @return a String representation of the object's data.
     */
    public String getLiteralString() {
        return stringData.toString();
    }

    /**
     * <p>Gets a literal String representation of this object's data using the
     * specifed font and format.  The font is used to verify that the
     * specific character codes can be rendered; if they cannot they may be
     * removed or combined with the next character code to get a displayable
     * character code.
     *
     * @param fontFormat the type of pdf font which will be used to display
     *                   the text.  Valid values are CID_FORMAT and SIMPLE_FORMAT for Adobe
     *                   Composite and Simple font types respectively
     * @param font       font used to render the the literal string data.
     * @return StringBuffer which contains all renderable characters for the
     *         given font.
     */
    public StringBuilder getLiteralStringBuffer(final int fontFormat, FontFile font) {

        if (fontFormat == Font.SIMPLE_FORMAT
                || (font.getByteEncoding() == FontFile.ByteEncoding.ONE_BYTE && !(font instanceof OFont))) {
            return stringData;
        } else if (fontFormat == Font.CID_FORMAT) {
            int length = getLength();
            int charValue;
            StringBuilder tmp = new StringBuilder(length);
            if (font.getByteEncoding() == FontFile.ByteEncoding.MIXED_BYTE) {
                int charOffset = 1;
                for (int i = 0; i < length; i += charOffset) {
                    // check range for possible 2 byte char.
                    charValue = getUnsignedInt(i, 1);
                    if (font.canDisplayEchar((char) charValue)) {
                        tmp.append((char) charValue);
                    } else {
                        int charValue2 = getUnsignedInt(i, 2);
                        if (font.canDisplayEchar((char) charValue2)) {
                            tmp.append((char) charValue2);
                            i += 1;
                        }
                    }
                }
            } else {
                // we have default 2bytes.
                int charOffset = 2;
                for (int i = 0; i < length; i += charOffset) {
                    int charValue2 = getUnsignedInt(i, 2);
                    if (font.canDisplayEchar((char) charValue2)) {
                        tmp.append((char) charValue2);
                    }
                }
            }
            return tmp;
        }
        return null;
    }

    /**
     * The length of the the underlying object's data.
     *
     * @return length of objcts data.
     */
    public int getLength() {
        return stringData.length();
    }

    /**
     * Utility method for converting literal strings to hexadecimal.
     *
     * @param string StringBuffer in literal form
     * @return StringBuffer in hexadecial form
     */
    private StringBuilder stringToHex(StringBuilder string) {
        StringBuilder hh = new StringBuilder(string.length() * 2);
        int charCode;
        for (int i = 0, max = string.length(); i < max; i++) {
            charCode = string.charAt(i);
            hh.append(hexChar[(charCode & 0xf0) >>> 4]);
            hh.append(hexChar[charCode & 0x0f]);
        }
        return hh;
    }

    /**
     * Sets the parent PDF object's reference.
     *
     * @param reference parent object reference.
     */
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    /**
     * Sets the parent PDF object's reference.
     *
     * @return returns the reference used for encryption.
     */
    public Reference getReference() {
        return reference;
    }

    /**
     * Gets the decrypted literal string value of the data using the key provided by the
     * security manager.
     *
     * @param securityManager security manager associated with parent document.
     */
    public String getDecryptedLiteralString(SecurityManager securityManager) {
        return encryption(stringData.toString(), true, securityManager);
    }

    /**
     * Decrypts or encrypts a string.
     *
     * @param string          string to encrypt or decrypt
     * @param decrypt         true to decrypt string, false otherwise;
     * @param securityManager security manager for document.
     * @return encrypted or decrypted string, depends on value of decrypt param.
     */
    public String encryption(String string, boolean decrypt,
                             SecurityManager securityManager) {
        // get the security manager instance
        if (securityManager != null && reference != null) {
            // get the key
            byte[] key = securityManager.getDecryptionKey();

            // convert string to bytes.
            byte[] textBytes =
                    Utils.convertByteCharSequenceToByteArray(string);

            // Decrypt String
            if (decrypt) {
                textBytes = securityManager.decrypt(reference,
                        key,
                        textBytes);
            } else {
                textBytes = securityManager.encrypt(reference,
                        key,
                        textBytes);
            }

            // convert back to a string
            return Utils.convertByteArrayToByteString(textBytes);
        }
        return string;
    }

}