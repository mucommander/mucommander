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

import java.util.logging.Logger;

/**
 * <p>A <code>name</code> class is an atomic symbol uniquely defined by a sequence of
 * characters. Uniquely defined means that any two name objects made up of the
 * same sequence of characters are identical. Atomic means
 * that a name has no internal structure, although it is defined by a sequence
 * of characters, those characters are not "elements" of the name. </p>
 * <p/>
 * <p>A slash character (/) introduces a name. The slash is not part of the name
 * itself, but a prefix indicating that the following sequence of characters
 * constitutes a name. There can be no white-space characters between the slash
 * and the first character in the name. The name may include any regular
 * characters, but no delimiter or white-space characters. Uppercase and
 * lowercase letters are considered distinct forexample,
 * /A and /a are different names.</p>
 * <p/>
 * <p>Names are similar to References in that objects in a PDF document can be
 * accessed by their use.  The Library class can result in any Name object and return
 * the corresponding PDF object.</p>
 *
 * @since 1.1
 */
public class Name {

    private static final Logger logger =
            Logger.getLogger(Name.class.toString());

    private static final int HEX_CHAR = 0X23;

    // String representing the name of the name
    private String name;

    /**
     * Create a new instance of a Name object.
     *
     * @param name the name value of the Name object
     */
    public Name(String name) {
        if (name != null) {
            this.name = convertHexChars(new StringBuilder(name));
        }
    }

    /**
     * Create a new instance of a Name object.
     *
     * @param name the name value of the Name object
     */
    public Name(StringBuilder name) {

        this.name = convertHexChars(name);
    }

    /**
     * Gets the name of the Name object.
     *
     * @return name of the object
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the string value of the Name object.  This is the same as getName()
     *
     * @return string representation of the name.
     * @see #getName()
     */
    public String toString() {
        return name;
    }

    /**
     * Indicates whether some other Name object is "equal to" this one
     *
     * @param obj name object that this Name object is compared against
     * @return true, if this object is the same as the obj argument;
     *         false, otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Name) {
            return equals((Name) obj);
        } else {
            return obj != null && name.equals(obj);
        }
    }

    /**
     * Indicates whether some other Name object is "equal to" this one
     *
     * @param obj name object that this Name object is compared against
     * @return true, if this object is the same as the obj argument;
     *         false, otherwise.
     */
    public boolean equals(Name obj) {
        return obj != null && name.equals(obj.getName());
    }

    /**
     * Indicates whether some other String object is "equal to" this one
     *
     * @param obj string object that this Name object is compared against
     * @return true, if this object is the same as the obj argument;
     *         false, otherwise.
     */
    public boolean equals(String obj) {
        return obj != null && name.equals(obj);
    }

    /**
     * Returns a hash code value for the Name object.  This hash is based
     * on the String representation of name of the Name object.
     *
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Utility Method converting Name object hext notation to ascii.  For
     * example #41 should be represented as 'A'.  The hex format will always
     * be #XX where XX is a 2 digit hex value.  The spec says that # can't be
     * used in a string but I guess we'll see.
     *
     * @param name PDF name object string to be checked for hex codes.
     * @return full ascii encoded name string.
     */
    private String convertHexChars(StringBuilder name) {
        // we need to search for an instance of # and try and convert to hex
        try {
            for (int i = 0; i < name.length(); i++) {
                if (name.charAt(i) == HEX_CHAR) {
                    // convert digits to hex.
                    String hex = name.substring(i + 1, i + 3);
                    name.delete(i, i + 3);
                    // convert digits to hex.
                    int charDd = Integer.parseInt(hex, 16);
                    if (charDd <= 127) {
                        name.insert(i, (char) charDd);
                    } else {
                        name.insert(i, convert(hex));
                    }
                }
            }
        } catch (Throwable e) {
            logger.finer("Error parsing hexadecimal characters.");
            //  we are going to bail on any exception and just return the original
            // string.
            return name.toString();
        }
        return name.toString();
    }

    /**
     * Converts a hex string to formatted unicode string.
     *
     * @param hex 2-digit hex number.
     * @return hex represented as unicode.
     */
    private String convert(String hex) {
        StringBuilder output = new StringBuilder();
        output.append("\\u"); // standard unicode format.
        for (int j = 0, max = 4 - hex.length(); j < max; j++) {
            output.append("0");
        }
        output.append(hex.toLowerCase());
        return output.toString();

    }
}
