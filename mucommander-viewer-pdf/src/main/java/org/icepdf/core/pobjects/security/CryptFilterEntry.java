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

import org.icepdf.core.pobjects.Dictionary;
import org.icepdf.core.pobjects.Name;
import org.icepdf.core.util.Library;

import java.util.HashMap;

/**
 * Individual Crypt filter definition.  A filter is associated with a name
 * key in the CryptFilter definition.  A stream or string that uses a crypt
 * filter will references it by its name.
 */
public class CryptFilterEntry extends Dictionary {

    public static final Name TYPE = new Name("CryptFilter");
    public static final Name AUTHEVENT_KEY = new Name("AuthEvent");
    public static final Name CFM_KEY = new Name("CFM");
    public static final Name LENGTH_KEY = new Name("Length");

    public CryptFilterEntry(Library library, HashMap entries) {
        super(library, entries);
    }

    /**
     * If present, shall be CryptFilter for a crypt filter dictionary.
     *
     * @return dictionary type, CryptFilter
     */
    public Name getType() {
        return TYPE;
    }

    /**
     * The method used, if any, by the conforming reader to decrypt data. The
     * following values shall be supported:
     * <ul>
     * <li>None - The application shall not decrypt data but shall direct the
     * input stream to the security handler for decryption.</li>
     * <li>V2 - The application shall ask the security handler for the
     * encryption key and shall implicitly decrypt data with "Algorithm 1:
     * Encryption of data using the RC4 or AES algorithms", using the RC4 algorithm.</li>
     * <li>AESV2 - (PDF 1.6) The application shall ask the security handler for
     * the encryption key and shall implicitly decrypt data with "Algorithm 1:
     * Encryption of data using the RC4 or AES algorithms", using the AES
     * algorithm in Cipher Block Chaining (CBC) mode with a 16-byte block size
     * and an initialization vector that shall be randomly generated and
     * placed as the first 16 bytes in the stream or string.</li>
     * </ul>
     * <p/>
     * When the value is V2 or AESV2, the application may ask once for this
     * encryption key and cache the key for subsequent use for streams that use
     * the same crypt filter. Therefore, there shall be a one-to-one relationship
     * between a crypt filter name and the corresponding encryption key.
     * <p/>
     * Only the values listed here shall be supported. Applications that encounter
     * other values shall report that the file is encrypted with an unsupported algorithm.
     * <p/>
     * Default value: None.
     *
     * @return name of crypt filter method.
     */
    public Name getCryptFilterMethod() {
        Object tmp = library.getObject(entries, CFM_KEY);
        if (tmp instanceof Name) {
            return (Name) tmp;
        }
        return null;
    }

    /**
     * The event to be used to trigger the authorization that is required to
     * access encryption keys used by this filter. If authorization fails, the
     * event shall fail. Valid values shall be:
     * <ul>
     * <li>DocOpen: Authorization shall be required when a document is opened.</li>
     * <li>EFOpen: Authorization shall be required when accessing embedded files.</li>
     * </ul>
     * Default value: DocOpen.
     * <p/>
     * If this filter is used as the value of StrF or StmF in the encryption
     * dictionary (see Table 20), the conforming reader shall ignore this key
     * and behave as if the value is DocOpen.
     *
     * @return authorization event.
     */
    public Name getAuthEvent() {
        Object tmp = library.getObject(entries, AUTHEVENT_KEY);
        if (tmp instanceof Name) {
            return (Name) tmp;
        }
        return null;
    }

    /**
     * (Optional) The bit length of the encryption key. It shall be a multiple
     * of 8 in the range of 40 to 128.  Security handlers may define their own
     * use of the Length entry and should use it to define the bit length of
     * the encryption key. Standard security handler expresses the length in
     * multiples of 8 (16 means 128) and public-key security handler expresses
     * it as is (128 means 128).
     *
     * @return lenth of encryption key
     */
    public int getLength() {
        int length = library.getInt(entries, LENGTH_KEY);
        return Math.min(length * 8, 128);
    }
}
