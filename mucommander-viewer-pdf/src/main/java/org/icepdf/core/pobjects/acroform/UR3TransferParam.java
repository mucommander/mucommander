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
import org.icepdf.core.pobjects.StringObject;
import org.icepdf.core.util.Library;
import org.icepdf.core.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The UR transform method shall be used to detect changes to a document that shall invalidate a usage rightssignature,
 * which is referred to from the UR3 entry in the permissions dictionary (see 12.8.4, Permissions). Usage rights
 * signatures shall be used to enable additional interactive features that may not available by default in a conforming
 * reader. The signature shall be used to validate that the permissions have been granted by a bonafide granting
 * authority. The transform parameters dictionary (see Table 255) specifies the additional rights that shall be enabled
 * if the signature is valid. If the signature is invalid because the document has been modified in a way that is not
 * permitted or the identity of the signer is not granted the extended permissions, additional rights shall not be granted.
 * <br />
 * EXAMPLE<br />
 * Adobe Systems grants permissions to enable additional features in Adobe Reader, using public-key cryptography. It
 * uses certificate authorities to issue public key certificateChain to document creators with which it has entered into a
 * business relationship. Adobe Reader verifies that the rights-enabling signature uses a certificate from an
 * Adobe-authorized certificate authority. Other conforming readers are free to use this same mechanism for their
 * own purposes.
 * <br />
 * UR3 (PDF 1.6): The ByteRange entry in the signature dictionary (see Table 252) shall be present. First, a conforming
 * reader shall verify the byte range digest to determine whether the portion of the document specified by ByteRange
 * corresponds to the state of the document at the time of signing. Next,
 * a conforming reader shall examine the current
 * version of the document to see whether there have been modifications to any objects that are not permitted by the
 * transform parameters.
 */
public class UR3TransferParam extends Dictionary implements TransformParams {

    /**
     * An array of names specifying additional document-wide usage rights for the document.
     */
    public static final Name DOCUMENT_KEY = new Name("Document");

    /**
     * The UR transform parameters dictionary version. The value shall be 2.2.
     */
    public static final Name VERSION_DEFAULT_VALUE = new Name("2.2");

    /**
     * (Optional) A text string that may be used to specify any arbitrary information, such as the reason for adding
     * usage rights to the document.
     */
    public static final Name MSG_KEY = new Name("Msg");


    /**
     * (Optional) An array of names specifying additional annotation-related usage rights for the document. Valid names
     * (PDF 1.5) are Create, Delete, Modify, Copy, Import, and Export, which shall permit the user to perform the named
     * operation on annotations.
     */
    public static final Name ANNOTATION_KEY = new Name("Annots");

    // PDF 1.5 Valid names
    public static final Name ANNOTATION_VALUE_CREATE = new Name("Create");
    public static final Name ANNOTATION_VALUE_DELETE = new Name("Delete");
    public static final Name ANNOTATION_VALUE_MODIFY = new Name("Modify");
    public static final Name ANNOTATION_VALUE_COPY = new Name("Copy");
    public static final Name ANNOTATION_VALUE_IMPORT = new Name("Import");
    public static final Name ANNOTATION_VALUE_EXPORT = new Name("Export");

    // PDF 1.6 Valid names

    /**
     * Permits online commenting; that is, the ability to upload or download markup annotations from a server.
     */
    public static final Name ANNOTATION_VALUE_ONLINE = new Name("Online");

    /**
     * Permits a user interface to be shown that summarizes the comments (markup annotations) in a document.
     */
    public static final Name ANNOTATION_VALUE_SUMMARY_VIEW = new Name("SummaryView");

    /**
     * (Optional) An array of names specifying additional form-field-related usage rights for the document.
     */
    public static final Name FORM_KEY = new Name("Form");

    /**
     * Permits the user to add form fields to the document.
     */
    public static final Name FORM_VALUE_ADD = new Name("Add");

    /**
     * Permits the user to delete form fields to the document.
     */
    public static final Name FORM_VALUE_DELETE = new Name("Delete");

    /**
     * Permits the user to save a document on which form fill-in has been done.
     */
    public static final Name FORM_VALUE_FILL_IN = new Name("FillIn");

    /**
     * Permits the user to import form data files in FDF, XFDF and text (CSV/TSV) formats.
     */
    public static final Name FORM_VALUE_IMPORT = new Name("Import");

    /**
     * Permits the user to export form data files as FDF or XFDF.
     */
    public static final Name FORM_VALUE_EXPORT = new Name("Export");

    /**
     * Permits the user to submit form data when the document is not open in a Web browser.
     */
    public static final Name FORM_VALUE_SUBMIT_STANDALONE = new Name("SubmitStandalone");

    /**
     * Permits new pages to be instantiated from named page templates.
     */
    public static final Name FORM_VALUE_SPAWN_TEMPLATE = new Name("SpawnTemplate");

    /**
     * The following names (PDF 1.6) shall be permitted only when the signature dictionary is referenced from the UR3
     * entry of the permissions dictionary;
     */

    /**
     * Permits (PDF 1.6) text form field data to be encoded as a plaintext two-dimensional barcode.
     */
    public static final Name FORM_VALUE_BARCODE_PLAIN_TEXT = new Name("BarcodePlainText");

    /**
     * Permits (PDF 1.6) the use of forms-specific online mechanisms such as SOAP or Active Data Object.
     */
    public static final Name FORM_VALUE_ONLINE = new Name("Online");

    /**
     * (Optional) An array of names specifying additional signature-related usage rights for the document. The only
     * defined value shall be Modify, which permits a user to apply a digital signature to an existing signature form
     * field or clear a signed signature form field.
     */
    public static final Name SIGNATURE_KEY = new Name("Signature");

    /**
     * Modify, which permits a user to apply a digital signature to an existing signature form
     * field or clear a signed signature form field.
     */
    public static final Name SIGNATURE_VALUE_MODIFY = new Name("Modify");

    /**
     * (Optional; PDF 1.6) An array of names specifying additional usage rights for named embedded files in the
     * document. Valid names shall be Create, Delete, Modify, and Import, which shall permit the user to perform the
     * named operation on named embedded files.
     */
    public static final Name EMBEDDED_FILES_KEY = new Name("EF");

    public static final Name EMBEDDED_FILES_VALUE_CREATE = new Name("Create");
    public static final Name EMBEDDED_FILES_VALUE_DELETE = new Name("Delete");
    public static final Name EMBEDDED_FILES_VALUE_MODIFY = new Name("Modify");
    public static final Name EMBEDDED_FILES_VALUE_IMPORT = new Name("Import");

    public static final Name PERMISSION_KEY = new Name("P");

    public UR3TransferParam(Library library, HashMap entries) {
        super(library, entries);
    }

    /**
     * (Optional) An array of names specifying additional document-wide usage rights for the document. The only defined
     * value shall be FullSave, which permits a user to save the document along with modified form and/or annotation
     * data. (PDF 1.5) Any usage right that permits the document to be modified implicitly shall enable the FullSave
     * right.
     * <br />
     * If the PDF document contains a UR3 dictionary, only rights specified by the Annots entry that permit the document
     * to be modified shall implicitly enable the FullSave right. For all other rights, FullSave shall be explicitly
     * enabled in order to save the document. (Signature rights shall permit saving as part of the signing process but
     * not otherwise).
     * <br />
     * If the P entry in the UR transform parameters dictionary is true (PDF 1.6) and greater conforming readers shall
     * permit only those rights that are enabled by the entries in the dictionary. However, conforming readers shall
     * permit saving the document as long as any rights that permit modifying the document are enabled.
     *
     * @return array of named documents rights.
     */
    public ArrayList<Name> getDocumentRights() {
        return (ArrayList) library.getArray(entries, DOCUMENT_KEY);
    }

    /**
     * A text string that may be used to specify any arbitrary information, such as the reason for adding
     * usage rights to the document.
     *
     * @return arbitrary info if any, null otherwise.
     */
    public String getMsg() {
        Object value = library.getObject(entries, MSG_KEY);
        if (value != null && value instanceof StringObject) {
            StringObject text = (StringObject) value;
            return Utils.convertStringObject(library, text);
        } else {
            return null;
        }
    }

    /**
     * Gets the UR transform parameters dictionary version. The value shall be 2.2. If an unknown version is present,
     * no rights shall be enabled.
     * NOTE<br />
     * this value is a name object, not a number.
     *
     * @return a name value of 2.2 if present, otherwise specified value.
     */
    public Name getVersion() {
        return library.getName(entries, VERSION_KEY);
    }

    /**
     * An array of names specifying additional annotation-related usage rights for the document.
     *
     * @return Array of annotation rights names, null if not set.
     */
    public ArrayList<Name> getAnnotationRights() {
        return (ArrayList) library.getArray(entries, ANNOTATION_KEY);
    }

    /**
     * An array of names specifying additional form-field-related usage rights for the document.
     *
     * @return Array of forms rights names, null if not set.
     */
    public ArrayList<Name> getFormRights() {
        return (ArrayList) library.getArray(entries, FORM_KEY);
    }

    /**
     * An array of names specifying additional signature-related usage rights for the document.
     *
     * @return if the value if isn't Modify, will will be null.
     */
    public ArrayList<Name> getSignatureRights() {
        return (ArrayList) library.getArray(entries, SIGNATURE_KEY);
    }

    /**
     * An array of names specifying additional usage rights for named embedded files in the document.
     *
     * @return list of EMBEDDED_FILES_* names or null if not set.
     */
    public ArrayList<Name> getEmbeddedFilesRights() {
        return (ArrayList) library.getArray(entries, EMBEDDED_FILES_KEY);
    }

    /**
     * Default value: false.
     *
     * @return If true, permissions for the document shall be restricted in all consumer applications to those permissions
     * granted by a conforming reader, while allowing permissions for rights enabled by other entries in this dictionary.
     */
    public boolean getPermission() {
        return library.getBoolean(entries, PERMISSION_KEY);
    }

}
