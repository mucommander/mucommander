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
import org.icepdf.core.pobjects.actions.Action;
import org.icepdf.core.util.Library;

import java.util.HashMap;

/**
 * An annotation, page object, or (beginning with PDF 1.3) interactive form field may include an entry named AA that
 * specifies an additional-actions dictionary (PDF 1.2) that extends the set of events that can trigger the execution
 * of an action. In PDF 1.4, the document catalogue dictionary (see 7.7.2, “Document Catalog”) may also contain an AA
 * entry for trigger events affecting the document as a whole. Tables 194 to 197 show the contents of this type of
 * dictionary.
 * <p/>
 * PDF 1.5 introduces four trigger events in annotation’s additional-actions dictionary to support multimedia
 * presentations:
 * <ul>
 * <li>The PO and PC entries have a similar function to the O and C entries in the page object’s additional-actions
 * dictionary (see Table 194). However, associating these triggers with annotations allows annotation objects to be
 * self-contained.</li>
 * <li>The PV and PI entries allow a distinction between pages that are open and pages that are visible. At any one
 * time, while more than one page may be visible, depending on the page layout.</li>
 * </ul>
 *
 * @since 5.2
 */
public class AdditionalActionsDictionary extends Dictionary {

    // Table 194 – Entries in an annotation’s additional-actions dictionary

    /**
     * (Optional; PDF 1.2) An action that shall be performed when the cursor enters the annotation’s active area.
     * An E (enter) event may occur only when the mouse button is up.
     */
    public static final Name ANNOTATION_E_KEY = new Name("E");

    /**
     * (Optional; PDF 1.2) An action that shall be performed when the cursor exits the annotation’s active area.
     * An X (exit) event may not occur without a preceding E event.
     */
    public static final Name ANNOTATION_X_KEY = new Name("X");

    /**
     * Optional; PDF 1.2) An action that shall be performed when the mouse button is pressed inside the annotation’s
     * active area.
     */
    public static final Name ANNOTATION_D_KEY = new Name("D");

    /**
     * (Optional; PDF 1.2) An action that shall be performed when the mouse button is released inside the annotation’s
     * active area.
     * <p/>
     * For backward compatibility, the A entry in an annotation dictionary, if present, takes precedence over this
     * entry (see Table 168).
     * A U (up) event may not occur without a preceding E and D event.
     */
    public static final Name ANNOTATION_U_KEY = new Name("U");

    /**
     * (Optional; PDF 1.2; widget annotations only) An action that shall be performed when the annotation receives
     * the input focus.
     */
    public static final Name ANNOTATION_FO_KEY = new Name("Fo");

    /**
     * (Optional; PDF 1.2; widget annotations only) (Uppercase B, lowercase L) An action that shall be performed when
     * the annotation loses the input focus.
     */
    public static final Name ANNOTATION_Bl_KEY = new Name("Bl");

    /**
     * (Optional; PDF 1.5) An action that shall be performed when the page containing the annotation is opened.
     * <p/>
     * EXAMPLE 1<br />
     * When the user navigates to it from the next or previous page or by means of a link annotation or outline item.
     * <p/>
     * The action shall be executed after the O action in the page’s additional-actions dictionary (see Table 195) and
     * the OpenAction entry in the document Catalog(see Table 28), if such actions are present.
     */
    public static final Name ANNOTATION_PO_KEY = new Name("Bl");

    /**
     * (Optional; PDF 1.5) An action that shall be performed when the page containing the annotation is closed.
     * <p/>
     * EXAMPLE 2<br />
     * When the user navigates to the next or previous page, or follows a link annotation or outline item.
     * <p/>
     * The action shall be executed before the C action in the page’s additional-actions dictionary (see Table 195),
     * if present.
     */
    public static final Name ANNOTATION_PC_KEY = new Name("PC");

    /**
     * (Optional; PDF 1.5) An action that shall be performed when the page containing the annotation becomes visible.
     */
    public static final Name ANNOTATION_PV_KEY = new Name("PV");

    /**
     * (Optional; PDF 1.5) An action that shall be performed when the page containing the annotation is no longer
     * visible in the conforming reader’s user interface.
     */
    public static final Name ANNOTATION_PI_KEY = new Name("PI");

    // Table 195 – Entries in a page object’s additional-actions dictionary

    /**
     * Optional; PDF 1.2) An action that shall be performed when the page is opened (for example, when the user
     * navigates to it from the next or previous page or by means of a link annotation or outline item). This action is
     * independent of any that may be defined by the OpenAction entry in the document Catalog (see 7.7.2, “Document Catalog”)
     * and shall be executed after such an action.
     */
    public static final Name PAGE_0_KEY = new Name("O");

    /**
     * (Optional; PDF 1.2) An action that shall be performed when the page is closed (for example, when the user
     * navigates to the next or previous page or follows a link annotation or an outline item). This action applies to
     * the page being closed and shall be executed before any other page is opened.
     */
    public static final Name PAGE_C_KEY = new Name("C");

    // Table 196 – Entries in a form field’s additional-actions dictionary

    /**
     * (Optional; PDF 1.3) A JavaScript action that shall be performed when the user modifies a character in a text
     * field or combo box or modifies the selection in a scrollable list box. This action may check the added text for
     * validity and reject or modify it.
     */
    public static final Name FORM_K_KEY = new Name("K");

    /**
     * (Optional; PDF 1.3) A JavaScript action that shall be performed before the field is formatted to display its
     * value. This action may modify the field’s value before formatting.
     */
    public static final Name FORM_F_KEY = new Name("F");

    /**
     * (Optional; PDF 1.3) A JavaScript action that shall be performed when the field’s value is changed. This action
     * may check the new value for validity. (The name V stands for “validate.”)
     */
    public static final Name FORM_V_KEY = new Name("V");

    /**
     * (Optional; PDF 1.3) A JavaScript action that shall be performed to recalculate the value of this field when that
     * of another field changes. (The name C stands for “calculate.”) The order in which the document’s fields are
     * recalculated shall be defined by the CO entry in the interactive form dictionary
     * (see 12.7.2, “Interactive Form Dictionary”).
     */
    public static final Name FORM_C_KEY = new Name("C");

    // Table 197 – Entries in the document catalog’s additional-actions dictionary

    /**
     * (Optional; PDF 1.4) A JavaScript action that shall be performed before closing a document.
     * (The name WC stands for “will close.”)
     */
    public static final Name CATALOG_WC_KEY = new Name("WC");

    /**
     * (Optional; PDF 1.4) A JavaScript action that shall be performed before saving a document.
     * (The name WS stands for “will save.”)
     */
    public static final Name CATALOG_WS_KEY = new Name("WS");

    /**
     * (Optional; PDF 1.4) A JavaScript action that shall be performed after saving a document.
     * (The name DS stands for “did save.”)
     */
    public static final Name CATALOG_DS_KEY = new Name("DS");

    /**
     * (Optional; PDF 1.4) A JavaScript action that shall be performed before printing a document.
     * (The name WP stands for “will print.”)
     */
    public static final Name CATALOG_WP_KEY = new Name("WP");

    /**
     * (Optional; PDF 1.4) A JavaScript action that shall be performed after printing a document.
     * (The name DP stands for “did print.”)
     */
    public static final Name CATALOG_DP_KEY = new Name("DP");

    public AdditionalActionsDictionary(Library library, HashMap entries) {
        super(library, entries);
    }

    public Action getAction(Name actionNameKey) {
        Object tmp = library.getObject(entries, actionNameKey);
        if (tmp != null && tmp instanceof HashMap) {
            return Action.buildAction(library, (HashMap) tmp);
        }
        return null;
    }

    public boolean isAnnotationValue(Name actionKey) {
        return library.isValidEntry(entries, actionKey);
//        return library.getObject(entries, actionKey) != null;
    }

    /**
     * @see #ANNOTATION_Bl_KEY
     */
    public boolean isAnnotationBIValue() {
        return library.getObject(entries, ANNOTATION_Bl_KEY) != null;
    }

    /**
     * @see #ANNOTATION_D_KEY
     */
    public boolean isAnnotationDValue() {
        return library.getObject(entries, ANNOTATION_D_KEY) != null;
    }

    /**
     * @see #ANNOTATION_E_KEY
     */
    public boolean isAnnotationEValue() {
        return library.getObject(entries, ANNOTATION_E_KEY) != null;
    }

    /**
     * @see #ANNOTATION_FO_KEY
     */
    public boolean isAnnotationFOValue() {
        return library.getObject(entries, ANNOTATION_FO_KEY) != null;
    }

    /**
     * @see #ANNOTATION_PC_KEY
     */
    public boolean isAnnotationPCValue() {
        return library.getObject(entries, ANNOTATION_PC_KEY) != null;
    }

    /**
     * @see #ANNOTATION_PI_KEY
     */
    public boolean isAnnotationPIValue() {
        return library.getObject(entries, ANNOTATION_PI_KEY) != null;
    }

    /**
     * @see #ANNOTATION_PO_KEY
     */
    public boolean isAnnotationPOValue() {
        return library.getObject(entries, ANNOTATION_PO_KEY) != null;
    }

    /**
     * @see #ANNOTATION_PV_KEY
     */
    public boolean isAnnotationPVValue() {
        return library.getObject(entries, ANNOTATION_PV_KEY) != null;
    }

    /**
     * @see #ANNOTATION_U_KEY
     */
    public boolean isAnnotationUValue() {
        return library.getObject(entries, ANNOTATION_U_KEY) != null;
    }

    /**
     * @see #ANNOTATION_X_KEY
     */
    public boolean isAnnotationXValue() {
        return library.getObject(entries, ANNOTATION_X_KEY) != null;
    }

    /**
     * @see #CATALOG_DP_KEY
     */
    public boolean isCatalogDPValue() {
        return library.getObject(entries, CATALOG_DP_KEY) != null;
    }

    /**
     * @see #CATALOG_DS_KEY
     */
    public boolean isCatalogDSValue() {
        return library.getObject(entries, CATALOG_DS_KEY) != null;
    }

    /**
     * @see #CATALOG_WC_KEY
     */
    public boolean isCatalogWCValue() {
        return library.getObject(entries, CATALOG_WC_KEY) != null;
    }

    /**
     * @see #CATALOG_WP_KEY
     */
    public boolean isCatalogWPValue() {
        return library.getObject(entries, CATALOG_WP_KEY) != null;
    }

    /**
     * @see #CATALOG_WS_KEY
     */
    public boolean isCatalogWSValue() {
        return library.getObject(entries, CATALOG_WS_KEY) != null;
    }

    /**
     * @see #FORM_C_KEY
     */
    public boolean isFormCValue() {
        return library.getObject(entries, FORM_C_KEY) != null;
    }

    /**
     * @see #FORM_F_KEY
     */
    public boolean isFormFValue() {
        return library.getObject(entries, FORM_F_KEY) != null;
    }

    /**
     * @see #FORM_K_KEY
     */
    public boolean isFormKValue() {
        return library.getObject(entries, FORM_K_KEY) != null;
    }

    /**
     * @see #FORM_V_KEY
     */
    public boolean isFormVValue() {
        return library.getObject(entries, FORM_V_KEY) != null;
    }

    /**
     * @see #PAGE_0_KEY
     */
    public boolean isPageOValue() {
        return library.getObject(entries, PAGE_0_KEY) != null;
    }

    /**
     * @see #PAGE_C_KEY
     */
    public boolean isPageCValue() {
        return library.getObject(entries, PAGE_C_KEY) != null;
    }
}
