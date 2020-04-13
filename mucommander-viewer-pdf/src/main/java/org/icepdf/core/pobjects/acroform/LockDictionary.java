package org.icepdf.core.pobjects.acroform;

import org.icepdf.core.pobjects.Dictionary;
import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.StringObject;
import org.icepdf.core.util.Library;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The signature field lock dictionary (described in Table 233) contains field names from the signature seed value
 * dictionary (described in Table 234) that cannot be changed through the user interface of a conforming reader.
 *
 * @since 6.1
 */
public class LockDictionary extends Dictionary {

    /**
     * (Optional) The type of PDF object that this dictionary describes; if present, shall be SigFieldLock for a
     * signature field lock dictionary.
     */
    public static final Name LOCK_TYPE_VALUE = new Name("SigFieldLock");

    /**
     * (Required) A name which, in conjunction with Fields, indicates the set of fields that should be locked.
     * The value shall be one of the following:
     * <ul>
     * <li>AllAll fields in the document</li>
     * <li>IncludeAll fields specified in Fields</li>
     * <li>ExcludeAll fields except those specified in Fields</li>
     * </ul>
     */
    public static final Name ACTION_KEY = new Name("Action");

    public static final Name ACTION_VALUE_ALL = new Name("All");
    public static final Name ACTION_VALUE_INCLUDE = new Name("Include");
    public static final Name ACTION_VALUE_EXCLUDE = new Name("Exclude");

    /**
     * (Required if the value of Action is Include or Exclude) An array of text strings containing field names.
     */
    public static final Name FIELDS_KEY = new Name("Fields");

    private Name action;

    private ArrayList<StringObject> fields;

    public LockDictionary(Library library, HashMap entries) {
        super(library, entries);
    }

    /**
     * If All fields in the document should be locked.
     *
     * @return true if all should be locked, otherwise false.
     */
    public boolean isAllLocked() {
        return action != null && action.equals(ACTION_VALUE_ALL);
    }

    /**
     * All fields specified by "fields" should be locked.
     *
     * @return true if fields should be locked, otherwise false.
     */
    public boolean isIncludeLocked() {
        return action != null && action.equals(ACTION_VALUE_INCLUDE);
    }

    /**
     * All fields specified by "fields" should should not be locked,  all others should be locked. .
     *
     * @return true if fields should be excluded, otherwise false.
     */
    public boolean isExcludeLocked() {
        return action != null && action.equals(ACTION_VALUE_EXCLUDE);
    }

    public Name getAction() {
        return action;
    }

    public void setAction(Name action) {
        this.action = action;
    }

    public ArrayList<StringObject> getFields() {
        return fields;
    }

    public void setFields(ArrayList<StringObject> fields) {
        this.fields = fields;
    }
}
