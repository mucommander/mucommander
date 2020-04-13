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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The FieldMDP transform method shall be used to detect changes to the values of a list of form fields.
 * <p/>
 * On behalf of a document author creating a document containing both form fields and signatures the following shall be
 * supported by conforming writers:
 * <ul>
 * <li>The author specifies that form fields shall be filled in without invalidating the approval or certification
 * signature. The P entry of the DocMDP transform parameters dictionary shall be set to either 2 or 3 (see Table 254).</li>
 * <li>The author can also specify that after a specific recipient has signed the document, any modifications to
 * specific form fields shall invalidate that recipient's signature. There shall be a separate signature field for
 * each designated recipient, each having an associated signature field lock dictionary (see Table 233) specifying
 * the form fields that shall be locked for that user.</li>
 * <li>When the recipient signs the field, the signature, signature reference, and transform parameters dictionaries
 * shall be created. The Action and Fields entries in the transform parameters dictionary shall be copied from the
 * corresponding fields in the signature field lock dictionary.</li>
 * </ul>
 * NOTE <br />
 * This copying is done because all objects in a signature dictionary must be direct objects if the dictionary contains
 * a byte range signature. Therefore, the transform parameters dictionary cannot reference the signature field lock
 * dictionary indirectly.
 * <p/>
 * FieldMDP signatures shall be validated in a similar manner to DocMDP signatures. See Validating Signatures That Use
 * the DocMDP Transform Method in 12.8.2.2, DocMDP for details.
 */
public class FieldMDPTransferParam extends Dictionary implements TransformParams {

    /**
     * A name that, along with the Fields array, describes which form fields do not permit changes after the signature
     * is applied.
     */
    public static final Name ACTION_KEY = new Name("Action");

    /**
     * All form fields.
     */
    public static final Name ACTION_VALUE_ALL = new Name("ALL");

    /**
     * Only those form fields that specified in Fields.
     */
    public static final Name ACTION_VALUE_INCLUDE = new Name("Include");

    /**
     * Only those form fields not specified in Fields.
     */
    public static final Name ACTION_VALUE_EXCLUDE = new Name("Exclude");

    /**
     * (Required if Action is Include or Exclude) An array of text strings containing field names.
     */
    public static final Name FIELDS_KEY = new Name("Fields");

    public FieldMDPTransferParam(Library library, HashMap entries) {
        super(library, entries);
    }

    /**
     * A name that, along with the Fields array, describes which form fields do not permit changes after the signature
     * is applied.
     *
     * @return one of the action values if set,  otherwise null.
     */
    public Name getAction() {
        return library.getName(entries, ACTION_KEY);
    }

    /**
     * (Required if Action is Include or Exclude) An array of text strings containing field names.
     *
     * @return array of text string,  null if not set.
     */
    public ArrayList<Name> getFields() {
        return (ArrayList) library.getArray(entries, FIELDS_KEY);
    }

    /**
     * (Optional: PDF 1.5 required) The transform parameters dictionary version. The value for PDF 1.5 and
     * later shall be 1.2.
     * NOTE<br />
     * this value is a name object, not a number.
     *
     * @return Default value: 1.2.
     */
    public Name getVersion() {
        return library.getName(entries, VERSION_KEY);
    }

}
