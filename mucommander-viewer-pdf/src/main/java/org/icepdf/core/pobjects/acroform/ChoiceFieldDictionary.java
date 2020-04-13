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

import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.Reference;
import org.icepdf.core.pobjects.StringObject;
import org.icepdf.core.util.Library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The ChoiceFieldDictionary contains all the dictionary entries specific to
 * the choice widget.
 *
 * @since 5.1
 */
public class ChoiceFieldDictionary extends VariableTextFieldDictionary {

    /**
     * (Optional) An array of options that shall be presented to the user. Each
     * element of the array is either a text string representing one of the
     * available options or an array consisting of two text strings: the option’s
     * export value and the text that shall be displayed as the name of the option.
     * <p/>
     * If this entry is not present, no choices should be presented to the user.
     */
    public static final Name OPT_KEY = new Name("Opt");

    /**
     * (Optional) For scrollable list boxes, the top index (the index in the Opt
     * array of the first option visible in the list). Default value: 0.
     */
    public static final Name TI_KEY = new Name("TI");

    /**
     * (Sometimes required, otherwise optional; PDF 1.4) For choice fields that
     * allow multiple selection (MultiSelect flag set), an array of integers,
     * sorted in ascending order, representing the zero-based indices in the Opt
     * array of the currently selected option items. This entry shall be used
     * when two or more elements in the Opt array have different names but the
     * same export value or when the value of the choice field is an array.
     * This entry should not be used for choice fields that do not allow multiple
     * selection. If the items identified by this entry differ from those in the
     * V entry of the field dictionary (see discussion following this Table),
     * the V entry shall be used.
     */
    public static final Name I_KEY = new Name("I");

    /**
     * If set, the field is a combo box; if clear, the field is a list box.
     */
    public static final int COMBO_BIT_FLAG = 0x20000;

    /**
     * If set, the combo box shall include an editable text box as well as a
     * drop-down list; if clear, it shall include only a drop-down list. This
     * flag shall be used only if the Combo flag is set.
     */
    public static final int EDIT_BIT_FLAG = 0x40000;

    /**
     * If set, the field’s option items shall be sorted alphabetically. This flag
     * is intended for use by writers, not by readers. Conforming readers shall
     * display the options in the order in which they occur in the Opt array
     * (see Table 231).
     */
    public static final int SORT_BIT_FLAG = 0x80000;

    /**
     * (PDF 1.4) If set, more than one of the field’s option items may be selected
     * simultaneously; if clear, at most one item shall be selected.
     */
    public static final int MULTI_SELECT_BIT_FLAG = 0x200000;

    /**
     * (PDF 1.4) If set, text entered in the field shall not be spell-checked.
     * This flag shall not be used unless the Combo and Edit flags are both set.
     */
    public static final int CHOICE_DO_NOT_SPELL_CHECK_BIT_FLAG = 0x400000;

    /**
     * (PDF 1.5) If set, the new value shall be committed as soon as a selection
     * is made (commonly with the pointing device). In this case, supplying a
     * value for a field involves three actions: selecting the field for fill-in,
     * selecting a choice for the fill-in value, and leaving that field, which
     * finalizes or “commits” the data choice and triggers any actions associated
     * with the entry or changing of this data. If this flag is on, then processing
     * does not wait for leaving the field action to occur, but immediately
     * proceeds to the third step.
     * <p/>
     * This option enables applications to perform an action once a selection is
     * made, without requiring the user to exit the field. If clear, the new
     * value is not committed until the user exits the field.
     */
    public static final int COMMIT_ON_SEL_CHANGE_BIT_FLAG = 0x4000000;

    public enum ChoiceFieldType {
        CHOICE_COMBO, CHOICE_EDITABLE_COMBO,
        CHOICE_LIST_SINGLE_SELECT, CHOICE_LIST_MULTIPLE_SELECT
    }

    protected ChoiceFieldType choiceFieldType;
    protected ArrayList<ChoiceOption> options;
    protected int topIndex;
    protected ArrayList<Integer> indexes;

    @SuppressWarnings("unchecked")
    public ChoiceFieldDictionary(Library library, HashMap entries) {
        super(library, entries);

        // options/list times.
        org.icepdf.core.pobjects.security.SecurityManager securityManager = library.getSecurityManager();
        Object value = library.getArray(entries, OPT_KEY);
        if (value == null) {
            FieldDictionary parent = getParent();
            value = library.getArray(parent.getEntries(), OPT_KEY);
        }
        if (value != null) {
            ArrayList opts = (ArrayList) value;
            options = new ArrayList<ChoiceOption>(opts.size());
            for (Object opt : opts) {
                if (opt instanceof StringObject) {
                    StringObject tmp = (StringObject) opt;
                    String tmpString = tmp.getDecryptedLiteralString(securityManager);
                    options.add(new ChoiceOption(tmpString, tmpString));
                } else if (opt instanceof List) {
                    List tmp = (List) opt;
                    StringObject tmp1StingObject = (StringObject) tmp.get(0);
                    String tmpString1 = tmp1StingObject.getDecryptedLiteralString(securityManager);
                    StringObject tmp2StingObject = (StringObject) tmp.get(1);
                    String tmpString2 = tmp2StingObject.getDecryptedLiteralString(securityManager);
                    options.add(new ChoiceOption(tmpString1, tmpString2));
                }
            }
        } else {
            options = null;
        }

        // determine combo or list
        int flags = getFlags();
        if ((flags & COMBO_BIT_FLAG) ==
                COMBO_BIT_FLAG) {
            // check for editable
            if ((flags & EDIT_BIT_FLAG) ==
                    EDIT_BIT_FLAG) {
                choiceFieldType = ChoiceFieldType.CHOICE_EDITABLE_COMBO;
            } else {
                choiceFieldType = ChoiceFieldType.CHOICE_COMBO;
            }
        } else {
            // check for selection mode
            if ((flags & MULTI_SELECT_BIT_FLAG) ==
                    MULTI_SELECT_BIT_FLAG) {
                choiceFieldType = ChoiceFieldType.CHOICE_LIST_MULTIPLE_SELECT;
            } else {
                choiceFieldType = ChoiceFieldType.CHOICE_LIST_SINGLE_SELECT;
            }
        }
        // select the selected index.
        if (choiceFieldType == ChoiceFieldType.CHOICE_LIST_SINGLE_SELECT) {
            value = library.getObject(entries, TI_KEY);
            if (value instanceof Number) {
                topIndex = ((Number) value).intValue();
            }
        }
        value = library.getObject(entries, I_KEY);
        if (value instanceof ArrayList) {
            ArrayList<Number> tmp = (ArrayList) value;
            indexes = new ArrayList<Integer>(tmp.size());
            for (Number aTmp : tmp) {
                indexes.add(aTmp.intValue());
            }
        }
        // we might not have an I_key but should have a value to work with if so we build the index our self.
        if (indexes == null && options != null) {
            indexes = new ArrayList<Integer>(1);
            for (int i = 0, j = 0, max = options.size(); i < max; i++) {
                if (options.get(i).getLabel().equals(value)) {
                    indexes.set(j, i);
                    j++;
                }
            }
        }
    }

    /**
     * Regular field value writing takes place as well as the update of the I (indexes) entry in the dictionary.
     * TODO: Further work is needed to fully support multiSelect values.
     * @param fieldValue value to write.
     * @param parentReference parent reference.
     */
    @Override
    public void setFieldValue(Object fieldValue, Reference parentReference) {
        // update the index to reflect the change,
        String selectedValue = null;
        if (fieldValue instanceof String) {
            selectedValue = (String) fieldValue;
            super.setFieldValue(selectedValue, parentReference);
        } else if (fieldValue instanceof StringObject) {
            StringObject tmp = (StringObject) fieldValue;
            selectedValue = tmp.getDecryptedLiteralString(library.getSecurityManager());
            super.setFieldValue(selectedValue, parentReference);
        }else if (fieldValue instanceof ChoiceOption) {
            ChoiceOption tmp = (ChoiceOption) fieldValue;
            selectedValue = tmp.getValue();
            super.setFieldValue(selectedValue, parentReference);
        }
        if (indexes != null) {
            indexes.clear();
        }else{
            indexes = new ArrayList<Integer>();
        }
        for (int i = 0, j = 0, max = options.size(); i < max; i++) {
            if (options.get(i).getLabel().equals(selectedValue)) {
                indexes.add(j, i);
            }
        }
        indexes.trimToSize();
        // store the new indexes in the dictionary.
        entries.put(I_KEY, indexes);
    }

    public ChoiceOption buildChoiceOption(String label, String value) {
        return new ChoiceOption(label, value);
    }

    public ChoiceFieldType getChoiceFieldType() {
        return choiceFieldType;
    }

    /**
     * For scrollable list boxes, the top index (the index in the Opt array of the first option visible in the list).
     * Default value: 0.
     *
     * @return the top index of a scrollable list boxes.
     */
    public int getTopIndex() {
        return topIndex;
    }

    public void setTopIndex(int topIndex) {
        this.topIndex = topIndex;
    }

    public ArrayList<Integer> getIndexes() {
        return indexes;
    }

    /**
     * For choice fields that allow multiple selection (MultiSelect flag set), an array of integers, sorted in
     * ascending order, representing the zero-based indices in the Opt array of the currently selected option items.
     * This entry shall be used when two or more elements in the Opt array have different names but the same export
     * value or when the value of the choice field is an array. This entry should not be used for choice fields that
     * do not allow multiple selection. If the items identified by this entry differ from those in the V entry of the
     * field dictionary (see discussion following this Table), the V entry shall be used.
     *
     * @param indexes list of selected indexes for multiple selection.
     */
    public void setIndexes(ArrayList<Integer> indexes) {
        this.indexes = indexes;
    }

    /**
     * An array of options that shall be presented to the user. Each element of the array is either a text
     * string representing one of the available options or an array consisting of two text strings: the option’s
     * export value and the text that shall be displayed as the name of the option.
     * <p/>
     * If this entry is not present, no choices should be presented to the user.
     *
     * @return choice options.
     */
    public ArrayList<ChoiceOption> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<ChoiceOption> options) {
        this.options = options;
    }

    public class ChoiceOption {
        private String label;
        private String value;
        private boolean isSelected;

        public ChoiceOption(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return label;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setIsSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }
    }

    /**
     * If set, the field’s option items shall be sorted alphabetically. This flag is intended for use by
     * writers, not by readers. Conforming readers shall display the options in the order in which they
     * occur in the Opt array.
     *
     * @return true if field items are to be sorted.
     */
    public boolean isSortFields() {
        return (getFlags() & SORT_BIT_FLAG) == SORT_BIT_FLAG;
    }

    /**
     * If set, more than one of the field’s option items may be selected simultaneously; if clear, at most
     * one item shall be selected.
     *
     * @return true if more then one field can be selected, otherwise false.
     */
    public boolean isMultiSelect() {
        return (getFlags() & MULTI_SELECT_BIT_FLAG) == MULTI_SELECT_BIT_FLAG;
    }

    /**
     * If set, the new value shall be committed as soon as a selection is made (commonly with the pointing device).
     * In this case, supplying a value for a field involves three actions: selecting the field for fill-in, selecting
     * a choice for the fill-in value, and leaving that field, which finalizes or “commits” the data choice and triggers
     * any actions associated with the entry or changing of this data. If this flag is on, then processing does not wait
     * for leaving the field action to occur, but immediately proceeds to the third step.
     * <p/>
     * This option enables applications to perform an action once a selection is made, without requiring the user to
     * exit the field. If clear, the new value is not committed until the user exits the field.
     *
     * @return true if commit on set change, otherwise false.
     */
    public boolean isCommitOnSetChange() {
        return (getFlags() & COMMIT_ON_SEL_CHANGE_BIT_FLAG) == COMMIT_ON_SEL_CHANGE_BIT_FLAG;
    }
}
