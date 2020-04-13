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
import java.util.List;
import java.util.Set;

/**
 * <P>The trailer of a PDF file enables an application reading the file to quickly
 * find the cross-reference table and certain special objects. Applications
 * should read a PDF file from its end. The last line of the file contains only
 * the end-of-file marker, %%EOF.</p>
 * <p/>
 * <p>A document can have more then one trailer reference.  It is important to use
 * the addTrailer() method if a subsequent trailer is found, or the
 * addPreviousTrailer() method if a previous trailer is found, depending on if
 * the PDF file is being read linearly, or via random access seeking.</p>
 * <p/>
 * <p>If the Prev key entry is present then the document has more then one
 * cross-reference section.  There is a numerical value, which is typically
 * associated with the trailer, that comes after startxref, and before %%EOF.
 * It is byte offset from the beginning of the file to the beginning of the last
 * cross-reference section.</p>
 * <p/>
 * <p>In a regular PDF, it's the address of the current xref table.  In a linearized
 * PDF, it's the address of the xref table at the file beginning, or zero.
 * In an updated PDF, it's the address of the current xref table. In all cases,
 * the LastCrossReferenceSection field, at the end of the PDF file, points
 * to the byte offset from the beginning of the file, of the "last" xref section,
 * which means the xref section with the highest precedence. For each xref section,
 * its following trailer section has a Prev field, which points to the byte
 * offset from the beginning of the file, of the xref section with one less
 * degree of precedence.</p>
 *
 * @since 1.1
 */
public class PTrailer extends Dictionary {

    public static final Name SIZE_KEY = new Name("Size");
    public static final Name PREV_KEY = new Name("Prev");
    public static final Name ROOT_KEY = new Name("Root");
    public static final Name ENCRYPT_KEY = new Name("Encrypt");
    public static final Name INFO_KEY = new Name("Info");
    public static final Name ID_KEY = new Name("ID");
    public static final Name XREFSTM_KEY = new Name("XRefStm");

    // Position in the file. The LazyObjectLoader typically keeps this info
    // for all PDF objects, but the bootstrapping PTrialer is an exception,
    // and we need its location for writing incremental updates, so for
    // consistency we'll have all PTrailers maintain their position.
    private long position;

    // documents cross reference table
    private CrossReference crossReferenceTable;

    // documents cross reference stream.
    private CrossReference crossReferenceStream;

    /**
     * Create a new PTrailer object
     *
     * @param dictionary dictionary associated with the trailer
     */
    public PTrailer(Library library, HashMap dictionary, CrossReference xrefTable, CrossReference xrefStream) {
        super(library, dictionary);

        crossReferenceTable = xrefTable;
        crossReferenceStream = xrefStream;
        if (crossReferenceTable != null)
            crossReferenceTable.setTrailer(this);
        if (crossReferenceStream != null)
            crossReferenceStream.setTrailer(this);
    }

    /**
     * Gets the total number of entries in the file's cross-reference table, as
     * defined by the combination of the original section and all updated sections.
     * Equivalently, this value is 1 greater than the highest object number
     * used in the file.
     * <ul>
     * <li>Note: Any object in a cross-reference section whose number is
     * greater than this value is ignored and considered missing.</li>
     * </ul>
     * <p/>
     * <b>Required : </b> must not be an indirect reference
     *
     * @return total number of entries in the file's cross-reference table
     */
    public int getNumberOfObjects() {
        return library.getInt(entries, SIZE_KEY);
    }

    /**
     * Gets the byte offset from the beginning of the file to the beginning of the
     * previous cross-reference section.
     * <p/>
     * (Present only if the file has more than one cross-reference section; must
     * not be an indirect reference)
     *
     * @return byte offset from beginning of the file to the beginning of the
     * previous cross-reference section
     */
    public long getPrev() {
        return library.getLong(entries, PREV_KEY);
    }

    /**
     * Depending on if the PDF file is version 1.4 or before, or 1.5 or after,
     * it may have a cross reference table, or cross reference stream, or both.
     * If there are both, then the cross reference table has precedence.
     *
     * @return the cross reference object with the highest precedence, for this trailer
     */
    protected CrossReference getPrimaryCrossReference() {
        if (crossReferenceTable != null)
            return crossReferenceTable;
        loadXRefStmIfApplicable();
        return crossReferenceStream;
    }

    /**
     * Gets the cross reference table.
     *
     * @return cross reference table object; null, if one does not exist.
     */
    protected CrossReference getCrossReferenceTable() {
        return crossReferenceTable;
    }

    /**
     * Gets the cross reference stream.
     *
     * @return cross reference stream object; null, if one does not exist.
     */
    protected CrossReference getCrossReferenceStream() {
        return crossReferenceStream;
    }

    /**
     * Gets the catalog reference for the PDF document contained in the file.
     * <p/>
     * <b>Required : </b> must not be an indirect reference
     *
     * @return reference number of catalog reference.
     */
    public Reference getRootCatalogReference() {
        return library.getObjectReference(entries, ROOT_KEY);
    }

    /**
     * Gets the Catalog entry for this PDF document.
     *
     * @return Catalog entry.
     */
    @SuppressWarnings("unchecked")
    public Catalog getRootCatalog() {
        Object tmp = library.getObject(entries, ROOT_KEY);
        // specification states the the root entry must be a indirect
        if (tmp instanceof Catalog) {
            return (Catalog) tmp;
        }
        // there are however a few instances where the dictionary is specified
        // directly
        else if (tmp instanceof HashMap) {
            return new Catalog(library, (HashMap<Object, Object>) tmp);
        }
        // if no root was found we return so that the use will be notified
        // of the problem which is the PDF can not be loaded.
        else {
            return null;
        }
    }

    /**
     * The document's encryption dictionary
     * <p/>
     * <b>Required : </b> if document is encrypted; PDF 1.1
     *
     * @return encryption dictionary
     */
    @SuppressWarnings("unchecked")
    public HashMap<Object, Object> getEncrypt() {
        Object encryptParams = library.getObject(entries, ENCRYPT_KEY);
        if (encryptParams instanceof HashMap) {
            return (HashMap) encryptParams;
        } else {
            return null;
        }
    }

    /**
     * The document's information dictionary
     * <p/>
     * <b>Optional : </b> must be an indirect reference.
     *
     * @return information dictionary
     */
    public PInfo getInfo() {
        Object info = library.getObject(entries, INFO_KEY);
        if (info instanceof HashMap) {
            return new PInfo(library, (HashMap) info);
        } else {
            return null;
        }
    }

    /**
     * A vector of two strings constituting a file identifier
     * <p/>
     * <b>Optional : </b> PDF 1.1.
     *
     * @return vector containing constituting file identifier
     */
    public List getID() {
        return (List) library.getObject(entries, ID_KEY);
    }

    /**
     * @return The position in te file where this trailer is located
     */
    public long getPosition() {
        return position;
    }

    /**
     * After this PTrailer is parsed, we store it's location within the PDF
     * here, for future use.
     */
    public void setPosition(long pos) {
        position = pos;
    }

    /**
     * Add the trailer dictionary to the current trailer object's dictionary.
     *
     * @param nextTrailer document trailer object
     */
    @SuppressWarnings("unchecked")
    protected void addNextTrailer(PTrailer nextTrailer) {
        nextTrailer.getPrimaryCrossReference().addToEndOfChainOfPreviousXRefs(getPrimaryCrossReference());

        // Later key,value pairs take precedence over previous entries
        HashMap nextDictionary = nextTrailer.getDictionary();
        HashMap currDictionary = getDictionary();
        Set currKeys = currDictionary.keySet();
        for (Object currKey : currKeys) {
            if (!nextDictionary.containsKey(currKey)) {
                Object currValue = currDictionary.get(currKey);
                nextDictionary.put(currKey, currValue);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void addPreviousTrailer(PTrailer previousTrailer) {
//System.out.println("PTrailer.addPreviousTrailer()");
        getPrimaryCrossReference().addToEndOfChainOfPreviousXRefs(previousTrailer.getPrimaryCrossReference());

        // Later key,value pairs take precedence over previous entries
        HashMap currDictionary = getDictionary();
        HashMap prevDictionary = previousTrailer.getDictionary();
        Set prevKeys = prevDictionary.keySet();
        for (Object prevKey : prevKeys) {
            if (!currDictionary.containsKey(prevKey)) {
                Object prevValue = prevDictionary.get(prevKey);
                currDictionary.put(prevKey, prevValue);
            }
        }
    }

    protected void onDemandLoadAndSetupPreviousTrailer() {
//System.out.println("PTrailer.onDemandLoadAndSetupPreviousTrailer() : " + this);
//try { throw new RuntimeException(); } catch(Exception e) { e.printStackTrace(); }
        long position = getPrev();
        if (position > 0L) {
            PTrailer prevTrailer = library.getTrailerByFilePosition(position);
            if (prevTrailer != null)
                addPreviousTrailer(prevTrailer);
        }
    }

    protected void loadXRefStmIfApplicable() {
        if (crossReferenceStream == null) {
            long xrefStreamPosition = library.getLong(entries, XREFSTM_KEY);
            if (xrefStreamPosition > 0L) {
                // OK, this is a little weird, but basically, any XRef stream
                //  dictionary is also a Trailer dictionary, so our Parser
                //  makes both of the objects.
                // Now, we don't actually want to chain the trailer as our
                //  previous, but only want its CrossReferenceStream to make
                //  our own
                PTrailer trailer = library.getTrailerByFilePosition(xrefStreamPosition);
                if (trailer != null)
                    crossReferenceStream = trailer.getCrossReferenceStream();
            }
        }
    }

    /**
     * Get the trailer object's dictionary.
     *
     * @return dictionary
     */
    public HashMap getDictionary() {
        return entries;
    }

    /**
     * Returns a summary of the PTrailer dictionary values.
     *
     * @return dictionary values.
     */
    public String toString() {
        return "PTRAILER= " + entries.toString() + " xref table=" + crossReferenceTable + "  xref stream=" + crossReferenceStream;
    }
}
