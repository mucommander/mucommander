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

import org.icepdf.core.pobjects.acroform.SignatureDictionary;
import org.icepdf.core.util.Library;

import java.util.HashMap;

/**
 * The Perms entry in the document catalogue (see Table 28) shall specify a permissions dictionary (PDF 1.5). Each entry
 * in this dictionary (see Table 258 for the currently defined entries) shall specify the name of a permission handler
 * that controls access permissions for the document. These permissions are similar to those defined by security handlers
 * (see Table 22) but do not require that the document be encrypted. For a permission to be actually granted for a
 * document, it shall be allowed by each permission handler that is present in the permissions dictionary as well as
 * by the security handler.
 */
public class Permissions extends Dictionary {

    /**
     * This dictionary shall contain a Reference entry that shall be a signature reference dictionary (see Table 252)
     * that has a DocMDP transform method (see 12.8.2.2, DocMDP) and corresponding transform parameters.
     * <p/>
     * If this entry is present, consumer applications shall enforce the permissions specified by the P attribute in
     * the DocMDP transform parameters dictionary and shall also validate the corresponding signature based on whether
     * any of these permissions have been violated.
     */
    public static final Name DOC_MDP_KEY = new Name("DocMDP");

    /**
     * (Optional) A signature dictionary that shall be used to specify and validate additional capabilities (usage rights)
     * granted for this document; that is, the enabling of interactive features of the conforming reader that are not
     * available by default.
     * <br />
     * For example, A conforming reader does not permit saving documents by default, but an agent may grant permissions
     * that enable saving specific documents. The signature shall be used to validate that the permissions have been
     * granted by the agent that did the signing.
     * <br />
     * The signature dictionary shall contain a Reference entry that shall be a signature reference dictionary that has
     * a UR transform method (see 12.8.2.3, UR). The transform parameter dictionary for this method indicates which
     * additional permissions shall be granted for the document. If the signature is valid, the conforming reader shall
     * allow the specified permissions for the document, in addition to the application's default permissions.
     */
    public static final Name UR3_KEY = new Name("UR3");

    // todo need to find some info this key
    public static final Name UR_KEY = new Name("UR");

    public Permissions(Library library, HashMap entries) {
        super(library, entries);
    }

    /**
     * Indication if DocMDP handler should be used.
     *
     * @return true if DocMDP handler should be used.
     */
    public boolean isDocMDP() {
        return library.getObject(entries, DOC_MDP_KEY) != null;
    }

    /**
     * Indication if UR3 handler should be used.
     *
     * @return true if UR3 handler should be used.
     */
    public boolean isUR3() {
        return library.getObject(entries, UR3_KEY) != null;
    }

    /**
     * Indication if UR3 handler should be used.
     *
     * @return true if UR3 handler should be used.
     */
    public boolean isUR() {
        return library.getObject(entries, UR_KEY) != null;
    }

    public SignatureDictionary getSignatureDictionary() {
        if (isDocMDP()) {
            return new SignatureDictionary(library, library.getDictionary(entries, DOC_MDP_KEY));
        } else if (isUR3()) {
            return new SignatureDictionary(library, library.getDictionary(entries, UR3_KEY));
        } else if (isUR()) {
            return new SignatureDictionary(library, library.getDictionary(entries, UR_KEY));
        } else {
            return null;
        }
    }
}
