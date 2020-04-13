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
import org.icepdf.core.pobjects.annotations.AbstractWidgetAnnotation;
import org.icepdf.core.util.Library;
import org.icepdf.core.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Each field in a document’s interactive form shall be defined by a field
 * dictionary, which shall be an indirect object. The field dictionaries may be
 * organized hierarchically into one or more tree structures. Many field
 * attributes are inheritable, meaning that if they are not explicitly specified
 * for a given field, their values are taken from those of its parent in the field
 * hierarchy. Such inheritable attributes shall be designated as such in the
 * Tables 220 and 221. The designation (Required; inheritable) means that an
 * attribute shall be defined for every field, whether explicitly in its own
 * field dictionary or by inheritance from an ancestor in the hierarchy. Table
 * 220 shows those entries that are common to all field dictionaries, regardless
 * of type. Entries that pertain only to a particular type of field are described
 * in the relevant sub-clauses in Table 220.
 *
 * @since 5.0
 */
public class FieldDictionary extends Dictionary {

    private static final Logger logger =
            Logger.getLogger(FieldDictionary.class.toString());

    /**
     * Required for terminal fields; inheritable) The type of field that this
     * dictionary describes:
     * <p/>
     * Button -> Button (see 12.7.4.2, “Button Fields”)
     * <p/>
     * Text -> Text (see 12.7.4.3, “Text Fields”)
     * <p/>
     * Choice -> Choice (see 12.7.4.4, “Choice Fields”)
     * <p/>
     * Signature(PDF 1.3) -> Signature (see 12.7.4.5, “Signature Fields”)
     * <p/>
     * This entry may be present in a non-terminal field (one whose descendants
     * are fields) to provide an inheritable FT value. However, a non-terminal
     * field does not logically have a type of its own; it is merely a container
     * for inheritable attributes that are intended for descendant terminal
     * fields of any type.
     */
    public static final Name FT_KEY = new Name("FT");

    /**
     * (Sometimes required, as described below) An array of indirect references
     * to the immediate children of this field.
     * <p/>
     * In a non-terminal field, the Kids array shall refer to field dictionaries
     * that are immediate descendants of this field. In a terminal field, the Kids
     * array ordinarily shall refer to one or more separate widget annotations that
     * are associated with this field. However, if there is only one associated
     * widget annotation, and its contents have been merged into the field dictionary,
     * Kids shall be omitted.
     */
    public static final Name KIDS_KEY = new Name("Kids");
    /**
     * (Required if this field is the child of another in the field hierarchy;
     * absent otherwise) The field that is the immediate parent of this one (the
     * field, if any, whose Kids array includes this field). A field can have at
     * most one parent; that is, it can be included in the Kids array of at most
     * one other field.
     */
    public static final Name PARENT_KEY = new Name("Parent");
    /**
     * (Optional) The partial field name (see 12.7.3.2, “Field Names”).
     */
    public static final Name T_KEY = new Name("T");
    /**
     * (Optional; PDF 1.3) An alternate field name that shall be used in place
     * of the actual field name wherever the field shall be identified in the
     * user interface (such as in error or status messages referring to the field).
     * This text is also useful when extracting the document’s contents in support
     * of accessibility to users with disabilities or for other purposes
     * (see 14.9.3, “Alternate Descriptions”).
     */
    public static final Name TU_KEY = new Name("TU");
    /**
     * (Optional; PDF 1.3) The mapping name that shall be used when exporting
     * interactive form field data from the document.
     */
    public static final Name TM_KEY = new Name("TM");
    /**
     * (Optional; inheritable) A set of flags specifying various characteristics
     * of the field (see Table 221). Default value: 0.
     */
    public static final Name Ff_KEY = new Name("Ff");
    /**
     * (Optional; inheritable) The field’s value, whose format varies depending
     * on the field type. See the descriptions of individual field types for
     * further information.
     */
    public static final Name V_KEY = new Name("V");
    /**
     * (Optional; inheritable) The default value to which the field reverts when
     * a reset-form action is executed (see 12.7.5.3, “Reset-Form Action”). The
     * format of this value is the same as that of V.
     */
    public static final Name DV_KEY = new Name("DV");
    /**
     * (Optional; PDF 1.2) An additional-actions dictionary defining the field’s
     * behaviour in response to various trigger events (see 12.6.3, “Trigger Events”).
     * This entry has exactly the same meaning as the AA entry in an annotation
     * dictionary (see 12.5.2, “Annotation Dictionaries”).
     */
    public static final Name AA_KEY = new Name("AA");

    /** general field flags **/

    /**
     * If set, the user may not change the value of the field. Any associated
     * widget annotations will not interact with the user; that is, they will
     * not respond to mouse clicks or change their appearance in response to mouse
     * motions. This flag is useful for fields whose values are computed or
     * imported from a database.
     */
    public static final int READ_ONLY_BIT_FLAG = 0x1;

    /**
     * If set, the field shall have a value at the time it is exported by a
     * submit-form action (see 12.7.5.2, “Submit-Form Action”).
     */
    public static final int REQUIRED_BIT_FLAG = 0x2;
    /**
     * If set, the field shall not be exported by a submit-form action (see 12.7.5.2, “Submit-Form Action”).
     */
    public static final int NO_EXPORT_BIT_FLAG = 0x4;

    protected Name fieldType;
    protected FieldDictionary parentField;
    protected ArrayList<Object> kids;

    protected String partialFieldName;
    protected String alternativeFieldName;
    protected String exportMappingName;
    private int flags;
    protected Object fieldValue;
    protected Object defaultFieldValue;
    protected AdditionalActionsDictionary additionalActionsDictionary;

    @SuppressWarnings("unchecked")
    public FieldDictionary(Library library, HashMap entries) {
        super(library, entries);

        // field name
        Object value = library.getObject(entries, T_KEY);
        if (value != null && value instanceof StringObject) {
            StringObject text = (StringObject) value;
            partialFieldName = Utils.convertStringObject(library, text);
        } else if (value instanceof String) {
            partialFieldName = (String) value;
        }
        // alternate field name.
        value = library.getObject(entries, TU_KEY);
        if (value != null && value instanceof StringObject) {
            StringObject text = (StringObject) value;
            alternativeFieldName = Utils.convertStringObject(library, text);
        } else if (value instanceof String) {
            alternativeFieldName = (String) value;
        }
        // mapping name for data export.
        value = library.getObject(entries, TM_KEY);
        if (value != null && value instanceof StringObject) {
            StringObject text = (StringObject) value;
            exportMappingName = Utils.convertStringObject(library, text);
        } else if (value instanceof String) {
            exportMappingName = (String) value;
        }

        // value field
//        getFieldValue();
        // todo default value, see 12.7.5.3, Reset-Form Action.
        value = library.getObject(entries, DV_KEY);
        if (value != null) {
            defaultFieldValue = value;
        }

        value = library.getObject(entries, AA_KEY);
        if (value != null && value instanceof HashMap) {
            additionalActionsDictionary = new AdditionalActionsDictionary(library, (HashMap)value);
        }

    }

    /**
     * Gets the kids of of terminal and non terminal fields.  For non-terminal this
     * can be a mix of field data and widget annotations.  And for terminal notes
     * the array will only contain widget annotations.
     *
     * @return list of child elements.
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Object> getKids() {
        // find some kids.
        if (kids == null) {
            Object value = library.getObject(entries, KIDS_KEY);
            if (value != null && value instanceof List) {
                List<Reference> children = (List<Reference>) value;
                kids = new ArrayList(children.size());
                Object tmp;
                for (Reference aChildren : children) {
                    tmp = library.getObject(aChildren);
                    // have a deeper structure,  shouldn't happen though or at least no examples yet.
                    if (tmp instanceof PObject){
                        tmp = ((PObject)tmp).getObject();
                    }
                    if (tmp instanceof HashMap) {
                        kids.add(FieldDictionaryFactory.buildField(library, (HashMap) tmp));
                    } else if (tmp instanceof AbstractWidgetAnnotation) {
                        kids.add(tmp);
                    }
                }
            }
        }
        return kids;
    }

    public FieldDictionary getParent() {
        // parent field there is an intermediary parent field dictionary define,  not present
        // if the widget annotation and dictionary have been flattened.
        if (parentField == null) {
            Object value = library.getObject(entries, PARENT_KEY);
            if (value instanceof HashMap) {
                parentField = FieldDictionaryFactory.buildField(library, (HashMap) value);
                if (parentField != null) {
                    parentField.setPObjectReference((Reference) entries.get(PARENT_KEY));
                }
            }
        }
        return parentField;
    }

    public Name getFieldType() {
        // get teh field type.
        if (fieldType == null) {
            Object value = library.getName(entries, FT_KEY);
            if (value != null) {
                fieldType = (Name) value;
            } else {
                if (getParent() != null) {
                    fieldType = parentField.getFieldType();
                }
            }
        }
        return fieldType;
    }

    public int getFlags() {
        // behaviour flags
        flags = library.getInt(entries, Ff_KEY);
        // check parent for flags value.
        if (flags == 0) {
            FieldDictionary parent = getParent();
            if (parent != null) {
                flags = parent.getFlags();
            }
        }
        return flags;
    }

    public String getPartialFieldName() {
        return partialFieldName;
    }

    public String getAlternativeFieldName() {
        return alternativeFieldName;
    }

    public String getExportMappingName() {
        return exportMappingName;
    }

    /**
     * If set, the user may not change the value of the field. Any associated widget annotations will not interact with
     * the user; that is, they will not respond to mouse clicks or change their appearance in response to mouse motions.
     * This flag is useful for fields whose values are computed or imported from a database.
     *
     * @return true if read only, otherwise false.
     */
    public boolean isReadOnly() {
        return ((flags & READ_ONLY_BIT_FLAG)
                == READ_ONLY_BIT_FLAG);
    }

    /**
     * If set, the field shall have a value at the time it is exported by a submit-form action
     *
     * @return true if value is required, otherwise false.
     */
    public boolean isRequired() {
        return ((flags & REQUIRED_BIT_FLAG)
                == REQUIRED_BIT_FLAG);
    }

    /**
     * If set, the field shall not be exported by a submit-form action.
     *
     * @return true if no value should be exported, otherwise false.
     */
    public boolean isNoExport() {
        return ((flags & NO_EXPORT_BIT_FLAG)
                == NO_EXPORT_BIT_FLAG);
    }

    /**
     * The T entry in the field dictionary (see Table 220) holds a text string defining the field’s partial field name.
     * The fully qualified field name is not explicitly defined but shall be constructed from the partial field names
     * of the field and all of its ancestors.
     * <p/>
     * This method will climb back up the inheritance tree to build the name.
     *
     * @return fully quality name of the field.
     */
    public String getFullyQualifiedFieldName() {
        String qualifiedFieldName = partialFieldName;
        if (parentField != null) {
            return parentField.getFullyQualifiedFieldName().concat(".").concat(partialFieldName);
        }
        return qualifiedFieldName;
    }

    public Object getFieldValue() {
        Object value = library.getObject(entries, V_KEY);
        if (value instanceof Name) {
            fieldValue = value;
        } else if (value instanceof StringObject) {
            StringObject text = (StringObject) value;
            fieldValue = Utils.convertStringObject(library, text);
        } else if (value instanceof String) {
            fieldValue = value;
        } else {
            fieldValue = "";
        }
        return fieldValue;
    }

    public boolean hasFieldValue(){
        return entries.containsKey(V_KEY);
    }

    public boolean hasDefaultValue(){
        return entries.containsKey(DV_KEY);
    }

    /**
     * Set the value field of the field dictionary (/V).  The value can be anything but special attention is given
     * to Strings especially if the document is already encrypted.  We need to stored String in an Encrypted environment
     * as encrypted so that we can write it correctly later.
     *
     * @param fieldValue      value to write.
     * @param parentReference parent reference.
     */
    public void setFieldValue(Object fieldValue, Reference parentReference) {
        this.fieldValue = fieldValue;
        if (fieldValue instanceof String) {
            // make sure we store an encrypted documents string as encrypted
            setString(V_KEY, (String) fieldValue);
        } else {
            entries.put(V_KEY, fieldValue);
        }
    }


    public Object getDefaultFieldValue() {
        return defaultFieldValue;
    }

    public AdditionalActionsDictionary getAdditionalActionsDictionary() {
        return additionalActionsDictionary;
    }
}
