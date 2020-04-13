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
package org.icepdf.core.pobjects.acroform;

import org.icepdf.core.pobjects.Dictionary;
import org.icepdf.core.pobjects.Name;
import org.icepdf.core.util.Library;

import java.util.HashMap;

/**
 * The DocMDP transform method shall be used to detect modifications relative to a signature field that is signed by
 * the author of a document (the person applying the first signature). A document can contain only one signature field
 * that contains a DocMDP transform method; it shall be the first signed field in the document. It enables the author
 * to specify what changes shall be permitted to be made the document and what changes invalidate the author's signature.
 * <p/>
 * NOTE <br />
 * As discussed earlier, MDP stands for modification detection and prevention. Certification signatures that use the
 * DocMDP transform method enable detection of disallowed changes specified by the author. In addition, disallowed
 * changes can also be prevented when the signature dictionary is referred to by the DocMDP entry in the permissions
 * dictionary (see 12.8.4, Permissions).
 * <p/>
 * A certification signature should have a legal attestation dictionary (see 12.8.5, Legal Content Attestations) that
 * specifies all content that might result in unexpected rendering of the document contents, along with the author's
 * attestation to such content. This dictionary may be used to establish an author's intent if the integrity of the
 * document is questioned.
 * <p/>
 * The P entry in the DocMDP transform parameters dictionary (see Table 254) shall indicate the author's specification
 * of which changes to the document will invalidate the signature. (These changes to the document shall also be
 * prevented if the signature dictionary is referred from the DocMDP entry in the permissions dictionary.) A value of 1
 * for P indicates that the document shall be final; that is, any changes shall invalidate the signature. The values 2
 * and 3 shall permit modifications that are appropriate for form field or comment work flows.
 */
public class DocMDPTransferParam extends Dictionary implements TransformParams {

    /**
     * The access permissions granted for this document.
     * Default value: 2.
     */
    public static final Name PERMISSION_KEY = new Name("P");

    /**
     * No changes to the document shall be permitted; any change to the document shall invalidate the signature.
     */
    public static final int PERMISSION_VALUE_NO_CHANGES = 1;

    /**
     * 2Permitted changes shall be filling in forms, instantiating page templates, and signing; other changes shall
     * invalidate the signature.
     */
    public static final int PERMISSION_VALUE_FORMS_SIGNING = 2;

    /**
     * Permitted changes shall be the same as for 2, as well as annotation creation, deletion, and modification; other
     * changes shall invalidate the signature.
     */
    public static final int PERMISSION_VALUE_ANNOTATION_CRUD = 3;

    public DocMDPTransferParam(Library library, HashMap entries) {
        super(library, entries);
    }

    /**
     * Get the access permissions granted for this document.
     *
     * @return PERMISSION_VALUE_NO_CHANGES, PERMISSION_VALUE_FORMS_SIGNING, PERMISSION_VALUE_ANNOTATION_CRUD or zero
     * if not set.
     */
    public int getPermissions() {
        return library.getInt(entries, PERMISSION_KEY);
    }

    /**
     * Gets the DocMDP transform parameters dictionary version. The only valid value shall be 1.2.
     * NOTE<br />
     * this value is a name object, not a number.
     *
     * @return always returns 1.2 as a name.
     */
    public Name getVersion() {
        return new Name("1.2");
    }

}
