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
import java.util.Set;

/**
 * PDF 1.5 introduces crypt filters, which provide finer granularity control of
 * encryption within a PDF file. The use of crypt filters involves the following
 * structures:
 * <ul>
 * <li>The encryption dictionary (see Table 20) contains entries that enumerate
 * the crypt filters in the document (CF) and specify which ones are used by
 * default to decrypt all the streams (StmF) and strings (StrF) in the document.
 * In addition, the value of the V entry shall be 4 to use crypt filters.</li>
 * <li>Each crypt filter specified in the CF entry of the encryption dictionary
 * shall be represented by a crypt filter dictionary, whose entries are shown
 * in Table 25.</li>
 * <li>A stream filter type, the Crypt filter (see 7.4.10, "Crypt Filter")
 * can be specified for any stream in the document to override the default
 * filter for streams. A conforming reader shall provide a standard Identity
 * filter which shall pass the data unchanged (see Table 26) to allow specific
 * streams, such as document metadata, to be unencrypted in an otherwise
 * encrypted document. The stream's DecodeParms entry shall contain a
 * Crypt filter decode parameters dictionary (see Table 14) whose Name entry
 * specifies the particular crypt filter to be used (if missing, Identity is
 * used). Different streams may specify different crypt filters.</li>
 * </ul>
 * Authorization to decrypt a stream shall always be obtained before the stream
 * can be accessed. This typically occurs when the document is opened, as specified
 * by a value of DocOpen for the AuthEvent entry in the crypt filter dictionary.
 * Conforming readers and security handlers shall treat any attempt to access a
 * stream for which authorization has failed as an error. AuthEvent can also be
 * EFOpen, which indicates the presence of an embedded file that is encrypted
 * with a crypt filter that may be different from the crypt filters used by
 * default to encrypt strings and streams in the document.
 */
public class CryptFilter extends Dictionary {


    // listing of crypt filters associated with the CF dictionary, one or more.
    public HashMap<Name, CryptFilterEntry> cryptFilters;


    public CryptFilter(Library library, HashMap entries) {
        super(library, entries);
    }

    /**
     * Gets a crypt filters definition as defined in its dictionary by name.
     *
     * @param cryptFilterName name of crypt filter to find.
     * @return crypt filter entry specified by the given name.  if not found
     *         null is returned.
     */
    public CryptFilterEntry getCryptFilterByName(Name cryptFilterName) {
        // check if need to initialize the dictionary
        if (cryptFilters == null) {
            cryptFilters = new HashMap<Name, CryptFilterEntry>(1);
            Set cryptKeys = entries.keySet();
            for (Object name : cryptKeys) {
                cryptFilters.put((Name) name, new CryptFilterEntry(library,
                        (HashMap) entries.get(name)));
            }
        }
        return cryptFilters.get(cryptFilterName);
    }

}
