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

import org.icepdf.core.pobjects.*;
import org.icepdf.core.pobjects.acroform.signature.exceptions.SignatureIntegrityException;
import org.icepdf.core.pobjects.annotations.SignatureWidgetAnnotation;
import org.icepdf.core.util.Library;
import org.icepdf.core.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * An interactive form (PDF 1.2)—sometimes referred to as an AcroForm is a
 * collection of fields for gathering information interactively from the user.
 * A PDF document may contain any number of fields appearing on any combination
 * of pages, all of which make up a single, global interactive form spanning
 * the entire document.
 * <p/>
 * Each field in a document’s interactive form shall be defined by a field
 * dictionary (see 12.7.3, “Field Dictionaries”). For purposes of definition and
 * naming, the fields can be organized hierarchically and can inherit attributes
 * from their ancestors in the field hierarchy. A field’s children in the hierarchy
 * may also include widget annotations (see 12.5.6.19, “Widget Annotations”) that
 * define its appearance on the page. A field that has children that are fields
 * is called a non-terminal field. A field that does not have children that are
 * fields is called a terminal field.
 * <p/>
 * The contents and properties of a document’s interactive form shall be defined
 * by an interactive form dictionary that shall be referenced from the AcroForm
 * entry in the document catalogue (see 7.7.2, “Document Catalog”).
 *
 * @since 5.1
 */
public class InteractiveForm extends Dictionary {

    /**
     * (Required) An array of references to the document’s root fields(those with
     * no ancestors in the field hierarchy).
     */
    public static final Name FIELDS_KEY = new Name("Fields");
    /**
     * (Optional) A flag specifying whether to construct appearance streams and
     * appearance dictionaries for all widget annotations in the document (see
     * 12.7.3.3, “Variable Text”). Default value: false.
     */
    public static final Name NEEDS_APPEARANCES_KEY = new Name("NeedAppearances");
    /**
     * (Optional; PDF 1.3) A set of flags specifying various document-level
     * characteristics related to signature fields (see Table 219, and 12.7.4.5,
     * “Signature Fields”). Default value: 0.
     */
    public static final Name SIG_FLAGS_KEY = new Name("SigFlags");
    /**
     * (Required if any fields in the document have additional-actions
     * dictionaries containing a C entry; PDF 1.3) An array of indirect
     * references to field dictionaries with calculation actions, defining the
     * calculation order in which their values will be recalculated when the
     * value of any field changes (see 12.6.3, “Trigger Events”).
     */
    public static final Name CO_KEY = new Name("CO");
    /**
     * (Optional) A resource dictionary (see 7.8.3, “Resource Dictionaries”)
     * containing default resources (such as fonts, patterns, or colour spaces)
     * that shall be used by form field appearance streams. At a minimum, this
     * dictionary shall contain a Font entry specifying the resource name and
     * font dictionary of the default font for displaying text.
     */
    public static final Name DR_KEY = new Name("DR");
    /**
     * (Optional) A document-wide default value for the DA attribute of variable
     * text fields (see 12.7.3.3, “Variable Text”).
     */
    public static final Name DA_KEY = new Name("DA");
    /**
     * (Optional) A document-wide default value for the Q attribute of variable
     * text fields (see 12.7.3.3, “Variable Text”).
     */
    public static final Name Q_KEY = new Name("Q");
    /**
     * If set, the document contains at least one signature field. This flag
     * allows a conforming reader to enable user interface items (such as menu
     * items or pushbuttons) related to signature processing without having to
     * scan the entire document for the presence of signature fields.
     */
    public static final int SIG_FLAGS_SIGNATURES_EXIST = 1;
    /**
     * If set, the document contains signatures that may be invalidated if the
     * file is saved (written) in a way that alters its previous contents, as
     * opposed to an incremental update. Merely updating the file by appending
     * new information to the end of the previous version is safe
     * (see H.7, “Updating Example”). Conforming readers may use this flag to
     * inform a user requesting a full save that signatures will be invalidated
     * and require explicit confirmation before continuing with the operation.
     */
    public static final int SIG_FLAGS_APPEND_ONLY = 2;

    private static final Logger logger =
            Logger.getLogger(InteractiveForm.class.toString());

    // field list, we keep reference as we don't want these garbage collected.
    private ArrayList<Object> fields;

    // A flag specifying whether to construct appearance streams and appearance dictionaries for all
    // widget annotations in the document (see 12.7.3.3, “Variable Text”). Default value: false.
    private boolean needAppearances;

    // A set of flags specifying various document-level characteristics related to signature fields .
    private int sigFlags;

    // An array of indirect references to field dictionaries with calculation actions, defining the calculation order in
    // which their values will be recalculated when the value of any field changes
    private List<Reference> calculationOrder;

    // A resource dictionary containing default resources (such as fonts, patterns, or colour spaces) that shall be used
    // by form field appearance streams.
    private Resources resources;

    // A document-wide default value for the DA attribute of variable text fields
    private String defaultVariableTextDAField;

    // A document-wide default value for the Q attribute of variable text fields
    // 0 - left-justified, 1 Centered, 2 right-justified.
    private int defaultVariableTextQField;

    // todo XFA entry stream or array processing.
    // important to test for data import reasons.

    public InteractiveForm(Library library, HashMap entries) {
        super(library, entries);
    }

    @SuppressWarnings("unchecked")
    public void init() {

        // load the resources
        needAppearances = library.getBoolean(entries, NEEDS_APPEARANCES_KEY);

        // sig flags.
        Object tmp = library.getObject(entries, SIG_FLAGS_KEY);
        if (tmp instanceof HashMap) {
            sigFlags = library.getInt(entries, SIG_FLAGS_KEY);
        }

        // load the resources
        tmp = library.getObject(entries, DR_KEY);
        if (tmp instanceof HashMap) {
            resources = library.getResources(entries, DR_KEY);
        }

        // load the resources,  useful for rebuilding form elements.
        tmp = library.getObject(entries, SIG_FLAGS_KEY);
        if (tmp instanceof HashMap) {
            resources = library.getResources(entries, DR_KEY);
        }

        // get the calculation order array.
        tmp = library.getObject(entries, CO_KEY);
        if (tmp instanceof List) {
            calculationOrder = library.getArray(entries, CO_KEY);
        }

        tmp = library.getObject(entries, Q_KEY);
        if (tmp instanceof List) {
            defaultVariableTextQField = library.getInt(entries, Q_KEY);
        }

        // load the default appearance.
        tmp = library.getObject(entries, DA_KEY);
        if (tmp instanceof StringObject) {
            defaultVariableTextDAField = Utils.convertStringObject(library, (StringObject) tmp);
        }

        // get the fields in the document, keeping hierarchy intact.
        tmp = library.getObject(entries, FIELDS_KEY);
        if (tmp instanceof List) {
            List tmpFields = (List) tmp;
            fields = new ArrayList(tmpFields.size());
            Object annotObj;
            for (Object fieldRef : tmpFields) {
                if (fieldRef instanceof Reference) {
                    // add them all as we find them.
                    annotObj = library.getObject((Reference) fieldRef);
                    if (annotObj instanceof HashMap) {
                        annotObj = FieldDictionaryFactory.buildField(library, (HashMap) annotObj);
                    }
                    if (annotObj != null) {
                        fields.add(annotObj);
                    }
                }
            }
        }
    }

    /**
     * Gets the fields associated with this form.
     *
     * @return array of fields.
     */
    public ArrayList<Object> getFields() {
        return fields;
    }

    /**
     * Gets the signature fields associated with this form.  A new array that references the forms signature annotations.
     * If no fields are found an empty list is returned.
     *
     * @return a list of form signature objects.
     */
    public ArrayList<SignatureWidgetAnnotation> getSignatureFields() {
        // capture the document signatures.
        ArrayList<SignatureWidgetAnnotation> signatures = new ArrayList<SignatureWidgetAnnotation>();
        if (fields != null) {
            for (Object field : fields) {
                if (field instanceof SignatureWidgetAnnotation) {
                    signatures.add((SignatureWidgetAnnotation) field);
                }
            }
        }
        return signatures;
    }

    /**
     * Test the byte range of the signature in this form to see if they cover the document in it's entirety.  This
     * should to be confused with validating a signature this just indicates that there are bytes that have been
     * written to the file that aren't covered by one of the documents signature.
     *
     * @return true if signatures cover the length of the document or false if the signatures don't dover the document
     * or there are no signatures.
     */
    public boolean isSignaturesCoverDocumentLength() {
        SignatureWidgetAnnotation signatureWidgetAnnotation;
        try {
            if (fields != null) {
                boolean isValidByteRange = false;
                for (Object field : fields) {
                    if (field instanceof SignatureWidgetAnnotation) {
                        signatureWidgetAnnotation = (SignatureWidgetAnnotation) field;
                        if (signatureWidgetAnnotation.getSignatureValidator() != null &&
                                signatureWidgetAnnotation.getSignatureValidator().checkByteRange()) {
                            isValidByteRange = true;
                            break;
                        }
                    }
                }
                if (isValidByteRange) {
                    for (Object field : fields) {
                        if (field instanceof SignatureWidgetAnnotation) {
                            signatureWidgetAnnotation = (SignatureWidgetAnnotation) field;
                            signatureWidgetAnnotation.getSignatureValidator().setSignaturesCoverDocumentLength(true);
                        }
                    }
                }
            }
        } catch (SignatureIntegrityException e) {
            logger.warning("Signature validation error has occurred");
        }
        return false;
    }

    /**
     * Checks to see if the fields list contains any signature anntoations.
     *
     * @return true if there are any signatures, otherwise false.
     */
    public boolean isSignatureFields() {
        boolean foundSignature = false;
        ArrayList<Object> fields = getFields();
        if (fields != null) {
            for (Object field : fields) {
                if (field instanceof SignatureWidgetAnnotation) {
                    foundSignature = true;
                    break;
                }
            }
        }
        return foundSignature;
    }

    /**
     * A set of flags specifying various document-level characteristics related to signature fields.  It should
     * be noted that this filed is not used very often and {@see isSignatureFields} should be used instead.
     *
     * @return true if enabled, false otherwise.
     */
    public boolean signatureExists() {
        return ((sigFlags & SIG_FLAGS_SIGNATURES_EXIST)
                == SIG_FLAGS_SIGNATURES_EXIST);
    }

    /**
     * If set, the document contains signatures that may be invalidated if the
     * file is saved (written) in a way that alters its previous contents, as
     * opposed to an incremental update
     *
     * @return true if enabled, false otherwise.
     */
    public boolean signatureAppendOnly() {
        return ((sigFlags & SIG_FLAGS_APPEND_ONLY)
                == SIG_FLAGS_APPEND_ONLY);
    }

    /**
     * A flag specifying whether to construct appearance streams and appearance dictionaries for all
     * widget annotations in the document. Default value: false.
     *
     * @return true if appearance streams need to be generated, otherwise false.
     */
    public boolean needAppearances() {
        return needAppearances;
    }

    /**
     * Gets a list of indirect references to fields that have calculation actions, defining the calculation
     * order in which values should be calculated when the value of any field changes.
     *
     * @return list of indirect references if present, otherwise null;
     */
    public List<Reference> getCalculationOrder() {
        return calculationOrder;
    }

    /**
     * Get the resources associated with the child widgets.
     *
     * @return common resources to all child widgets. Can be null.
     */
    public Resources getResources() {
        return resources;
    }

    /**
     * Gest the default variable text DA entry which can be helpful for rebuilding field data appearance streams.
     *
     * @return default da values, null if not present.
     */
    public String getDefaultVariableTextDAField() {
        return defaultVariableTextDAField;
    }

    /**
     * Ges the default variable text quadding rule.
     *
     * @return integer represented by VariableTextFieldDictionary.QUADDING_LEFT_JUSTIFIED, VariableTextFieldDictionary.QUADDING_CENTERED or
     * VariableTextFieldDictionary.QUADDING_RIGHT_JUSTIFIED.
     */
    public int getDefaultVariableTextQField() {
        return defaultVariableTextQField;
    }
}
