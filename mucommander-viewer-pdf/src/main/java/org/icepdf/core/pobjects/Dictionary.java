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

import org.icepdf.core.util.Library;

import java.util.HashMap;

/**
 * <p>This class represents a PDF document's <i>Dictionary</i> object.  A
 * <i>Dictionary</i> object is an associative table containing pairs of objects,
 * known as the dictionary's entries.  The first element of each entry is the key
 * and the second element is the value.  Dictionary objects are the main building
 * blocks of a PDF document.  They are commonly used to collect and tie together
 * the attributes of complex objects such as fonts or pages within a
 * document. </p>
 * <p>Most of the Objects found in the package org.icepdf.core.pobject.* extend
 * this class.  Dictionary objects by convention have a "Type" entry which
 * identifies the type of object the dictionary describes. Classes that extend
 * Dictionary add functionality based on the specified Dictionary type.</p>
 *
 * @since 1.0
 */
public class Dictionary {

    public static final Name TYPE_KEY = new Name("Type");

    public static final Name SUBTYPE_KEY = new Name("Subtype");

    public static final Name LENGTH_KEY = new Name("Length");

    public static final Name FORM_TYPE_KEY = new Name("FormType");

    /**
     * Pointer to the documents <code>Library</code> object which
     * acts a central repository for the access of PDF object in the document.
     */
    protected Library library;

    /**
     * Table of associative pairs of objects.
     */
    protected HashMap<Object, Object> entries;

    /**
     * Indicates if Dictionary has been initiated.
     */
    protected boolean inited;

    /**
     * Flag to indicate this object has been flaged for deletion.
     */
    protected boolean isDeleted;

    /**
     * Flags the object as new and not previously saved in the file
     */
    protected boolean isNew;

    // reference of stream, needed for encryption support
    private Reference pObjectReference;

    /**
     * Creates a new instance of a Dictionary.
     *
     * @param library document library.
     * @param entries dictionary entries.
     */
    @SuppressWarnings("unchecked")
    public Dictionary(Library library, HashMap entries) {
        this.library = library;
        this.entries = entries;
        if (this.entries == null) {
            this.entries = new HashMap<Object, Object>();
        }
    }

    /**
     * <p>Sets the reference used to identify this Dictionary in the PDF document.
     * The reference number and generation number of this reference is needed by
     * the encryption algorithm to correctly decrypt this object.</p>
     * <p>This method should only be used by the PDF Parser.  Use of this method
     * outside the context of the PDF Parser may result in unpredictable
     * behavior. </p>
     *
     * @param reference Reference used to identify this Dictionary in the PDF
     *                  document.
     * @see #getPObjectReference()
     */
    public void setPObjectReference(Reference reference) {
        pObjectReference = reference;
    }

    /**
     * <p>Gets the reference used to identify this Dictionary in the PDF
     * document.  The reference number and generation number of this reference
     * is needed by the encryption algorithm to correctly decrypt this object.</p>
     *
     * @return Reference used to identify this Dictionary in a PDF document.
     * @see #setPObjectReference(org.icepdf.core.pobjects.Reference)
     */
    public Reference getPObjectReference() {
        return pObjectReference;
    }

    /**
     * Initiate the Dictionary. Retrieve any needed attributes.
     */
    public void init() throws InterruptedException {
    }

    /**
     * Gets a copy of the entries that make up the Dictionary.
     *
     * @return a copy of the Dictionary's entries.
     */
    public HashMap<Object, Object> getEntries() {
        return entries;
    }

    public Object getObject(Name key) {
        return library.getObject(entries, key);
    }

    /**
     * Gets a Number specified by the <code>key</code> in the dictionary
     * entries.  If the value is a reference, the Number object that the
     * reference points to is returned.  If the key cannot be found,
     * or the resulting object is not a Number, then null is returned.
     *
     * @param key key to find in entries HashMap.
     * @return Number that the key refers to
     */
    protected Number getNumber(Name key) {
        return library.getNumber(entries, key);
    }

    /**
     * Gets an int specified by the <code>key</code> in the dictionary
     * entries.  If the value is a reference, the int value that the
     * reference points to is returned.
     *
     * @param key key to find in entries HashMap.
     * @return int value if a valid key,  else zero if the key does not point
     * to an int or is invalid.
     */
    public int getInt(Name key) {
        return library.getInt(entries, key);
    }

    /**
     * Gets a float specified by the <code>key</code> in the dictionary
     * entries.  If the value is a reference, the float value that the
     * reference points to is returned.
     *
     * @param key key to find in entries HashMap.
     * @return float value if a valid key,  else zero if the key does not point
     * to a float or is invalid.
     */
    public float getFloat(Name key) {
        return library.getFloat(entries, key);
    }

    /**
     * Gets the PDF Documents Library.  A Library object is the central repository
     * of all objects that make up the PDF document hierarchy.
     *
     * @return documents library.
     */
    public Library getLibrary() {
        return library;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    /**
     * Sets the dictionary key value, handling any encryption so dictionary can be written correctly.
     *
     * @param key   dictionary key
     * @param value key value.
     * @return string value of the newly set string which will always be decrypted.
     */
    protected String setString(final Name key, String value) {
        // make sure we store an encrypted documents string as encrypted
        entries.put(key, new LiteralStringObject(value, getPObjectReference(), library.getSecurityManager()));
        return value;
    }

    /**
     * Sets the dictionary key value, handling any encryption so dictionary can be written correctly.
     *
     * @param key   dictionary key
     * @param value key value.
     * @return string value of the newly set string which will always be decrypted.
     */
    protected String setHexString(final Name key, String value) {
        // make sure we store an encrypted documents string as encrypted
        entries.put(key, new HexStringObject(value, getPObjectReference(), library.getSecurityManager()));
        return value;
    }

    /**
     * Returns a summary of the dictionary entries.
     *
     * @return dictionary values.
     */
    public String toString() {
        return getClass().getName() + "=" + entries.toString();
    }
}



