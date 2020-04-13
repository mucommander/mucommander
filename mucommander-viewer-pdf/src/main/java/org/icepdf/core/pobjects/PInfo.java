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

import org.icepdf.core.pobjects.fonts.ofont.Encoding;
import org.icepdf.core.pobjects.security.SecurityManager;
import org.icepdf.core.util.Library;

import java.util.HashMap;

/**
 * <p>This class represents the data stored in a File trailers optional "info"
 * entry.</p>
 * <p/>
 * <p>Any entry whose value is not known should be omitted from the dictionary,
 * rather than included with an empty string as its value.</p>
 * <p/>
 * <p>Some plug-in extensions may choose to permit searches on the contents of the
 * document information dictionary. To facilitate browsing and editing, all keys
 * in the dictionary are fully spelled out, not abbreviated. New keys should be
 * chosen with care so that they make sense to users.</p>
 *
 * @since 1.1
 */
public class PInfo extends Dictionary {

    public static final Name RESOURCES_KEY = new Name("Resources");
    public static final Name TITLE_KEY = new Name("Title");
    public static final Name AUTHOR_KEY = new Name("Author");
    public static final Name SUBJECT_KEY = new Name("Subject");
    public static final Name KEYWORDS_KEY = new Name("Keywords");
    public static final Name CREATOR_KEY = new Name("Creator");
    public static final Name PRODUCER_KEY = new Name("Producer");
    public static final Name CREATIONDATE_KEY = new Name("CreationDate");
    public static final Name MODDATE_KEY = new Name("ModDate");
    public static final Name TRAPPED_KEY = new Name("Trapped");

    // security manager need for decrypting strings.
    private SecurityManager securityManager;

    /**
     * Create a new instance of a <code>PInfo</code> object.
     *
     * @param library document library
     * @param entries entries for this object dictionary.
     */
    public PInfo(Library library, HashMap entries) {
        super(library, entries);
        securityManager = library.getSecurityManager();
    }

    /**
     * Gets the value of the custom extension specified by <code>name</code>.
     *
     * @param name som plug-in extensions name.
     * @return value of the plug-in extension.
     */
    public Object getCustomExtension(Name name) {
        Object value = library.getObject(entries, name);
        if (value != null && value instanceof StringObject) {
            StringObject text = (StringObject) value;
            return cleanString(text.getDecryptedLiteralString(securityManager));
        }
        return value;
    }

    /**
     * Gets the title of the document.
     *
     * @return the documents title.
     */
    public String getTitle() {
        Object value = library.getObject(entries, TITLE_KEY);
        if (value != null && value instanceof StringObject) {
            StringObject text = (StringObject) value;
            return cleanString(text.getDecryptedLiteralString(securityManager));
        } else if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    /**
     * Gets the name of the person who created the document.
     *
     * @return author name.
     */
    public String getAuthor() {
        Object value = library.getObject(entries, AUTHOR_KEY);
        if (value != null && value instanceof StringObject) {
            StringObject text = (StringObject) value;
            return cleanString(text.getDecryptedLiteralString(securityManager));
        } else if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    /**
     * Gets the subject of the document.
     *
     * @return documents subject.
     */
    public String getSubject() {
        Object value = library.getObject(entries, SUBJECT_KEY);
        if (value != null && value instanceof StringObject) {
            StringObject text = (StringObject) value;
            return cleanString(text.getDecryptedLiteralString(securityManager));
        } else if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    /**
     * Gets the keywords associated with the document.
     *
     * @return documents keywords.
     */
    public String getKeywords() {
        Object value = library.getObject(entries, KEYWORDS_KEY);
        if (value != null && value instanceof StringObject) {
            StringObject text = (StringObject) value;
            return cleanString(text.getDecryptedLiteralString(securityManager));
        } else if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    /**
     * Gets the name of the application. If the PDF document was converted from
     * another format that <b>created</b> the original document.
     *
     * @return creator name.
     */
    public String getCreator() {
        Object value = library.getObject(entries, CREATOR_KEY);
        if (value != null && value instanceof StringObject) {
            StringObject text = (StringObject) value;
            return cleanString(text.getDecryptedLiteralString(securityManager));
        } else if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    /**
     * Gets the name of the application. If the PDF document was converted from
     * another format that <b>converted</b> the original document.
     *
     * @return producer name.
     */
    public String getProducer() {
        Object value = library.getObject(entries, PRODUCER_KEY);
        if (value != null && value instanceof StringObject) {
            StringObject text = (StringObject) value;
            return cleanString(text.getDecryptedLiteralString(securityManager));
        } else if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    /**
     * Gets the date and time the document was created.
     *
     * @return creation date.
     */
    public PDate getCreationDate() {
        Object value = library.getObject(entries, CREATIONDATE_KEY);
        if (value != null && value instanceof StringObject) {
            StringObject text = (StringObject) value;
            return new PDate(securityManager, text.getDecryptedLiteralString(securityManager));
        }
        return null;
    }

    /**
     * Gets the date and time the document was most recently modified.
     *
     * @return modification date.
     */
    public PDate getModDate() {
        Object value = library.getObject(entries, MODDATE_KEY);
        if (value != null && value instanceof StringObject) {
            StringObject text = (StringObject) value;
            return new PDate(securityManager, text.getDecryptedLiteralString(securityManager));
        }
        return null;
    }

    /**
     * Get the name object indicating whether the document has been modified to
     * include trapping information:
     * <ul>
     * <li><b>False</b> - The document has not yet been trapped; any desired
     * trapping must still be done.</li>
     * <li><b>Unknown</b> - (default) Either it is unknown whether the document has
     * been trapped or it has been partly but not yet fully
     * trapped; some additional trapping may still be needed.</li>
     * </ul>
     *
     * @return trapped name.
     */
    public String getTrappingInformation() {
        Object value = library.getObject(entries, TRAPPED_KEY);
        if (value != null && value instanceof StringObject) {
            StringObject text = (StringObject) value;
            return cleanString(text.getDecryptedLiteralString(securityManager));
        } else if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    /**
     * Utility method for removing extra characters associated with 4 byte
     * characters codes.
     *
     * @param text string to clean
     * @return cleaned
     */
    private String cleanString(String text) {
        if (text != null && text.length() > 0) {
            if (((int) text.charAt(0)) == 254 && ((int) text.charAt(1)) == 255) {
                StringBuilder sb1 = new StringBuilder();

                // strip and white space, as the will offset the below algorithm
                // which assumes the string is made up of two byte chars.
                String hexTmp = "";
                for (int i = 0; i < text.length(); i++) {
                    char c = text.charAt(i);
                    if (!((c == '\t') || (c == '\r') || (c == '\n'))) {
                        hexTmp = hexTmp + text.charAt(i);
                    }
                }
                byte title1[] = hexTmp.getBytes();

                for (int i = 2; i < title1.length; i += 2) {
                    try {
                        int b1 = (((int) title1[i] & 0xFF) << 8) |
                                (int) title1[i + 1] & 0xFF;
                        sb1.append((char) (b1));
                    } catch (Exception ex) {
                        // intentionally left empty
                    }
                }
                text = sb1.toString();
            } else {
                StringBuilder sb = new StringBuilder();
                Encoding enc = Encoding.getPDFDoc();
                for (int i = 0; i < text.length(); i++) {
                    sb.append(enc.get(text.charAt(i)));
                }
                text = sb.toString();
            }
            return text;
        } else {
            return "";
        }
    }

}
