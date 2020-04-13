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
import org.icepdf.core.util.Parser;
import org.icepdf.core.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Mark Collette
 * @since 2.0
 */

public class CrossReference {

    private static final Logger logger =
            Logger.getLogger(CrossReference.class.toString());

    public static final Name SIZE_KEY = new Name("Size");
    public static final Name INDEX_KEY = new Name("Index");
    public static final Name W_KEY = new Name("W");

    /**
     * Map of all the objects in reference by the CrossReference table.  Ojbects
     * are retrieved by object number.
     */
    private ConcurrentHashMap<Number, Entry> hObjectNumber2Entry;
    /**
     * In a Linearized PDF, we don't want to load all Trailers and their XRefs
     * upfront, but would rather load the first upfront, and then lazily load
     * the rest.
     * If xrefPrevious != null, Then just use it
     * If xrefPrevious == null And pTrailer == null,
     * Then we can't do anything
     * If xrefPrevious == null And pTrailer != null,
     * Then use pTrailer to setup xrefPrevious
     */
    private PTrailer pTrailer;
    private CrossReference xrefPrevious;
    private CrossReference xrefPeer;
    //
    private boolean bIsCrossReferenceTable;
    private boolean bHaveTriedLoadingPrevious;
    private boolean bHaveTriedLoadingPeer;

    // offset error for simple file error issue.
    protected int offset;

    public CrossReference() {
        hObjectNumber2Entry = new ConcurrentHashMap<Number, Entry>(4096);
    }

    public void setTrailer(PTrailer trailer) {
        pTrailer = trailer;
    }

    /**
     * Starts the parsing of an xRef table entries as found when using the
     * Parser to Parse out an object via Parser.getObject().
     * <p/>
     * All entries are taken into consideration except for ones that are marked
     * free.
     *
     * @param parser content parser
     */

    public void addXRefTableEntries(Parser parser) {
        bIsCrossReferenceTable = true;
        try {
            while (true) {
                Object startingObjectNumberOrTrailer = parser.getNumberOrStringWithMark(16);
                if (!(startingObjectNumberOrTrailer instanceof Number)) {
                    parser.ungetNumberOrStringWithReset();
                    break;
                }

                int startingObjectNumber = ((Number) startingObjectNumberOrTrailer).intValue();
                int numEntries = ((Number) parser.getToken()).intValue();
                int currNumber = startingObjectNumber;
                for (int i = 0; i < numEntries; i++) {
                    long filePosition = parser.getIntSurroundedByWhitespace();  // ( (Number) getToken() ).longValue();
                    int generationNum = parser.getIntSurroundedByWhitespace(); // ( (Number) getToken() ).intValue();
                    char usedOrFree = parser.getCharSurroundedByWhitespace();  // ( (String) getToken() ).charAt( 0 );
                    if (usedOrFree == 'n') {         // Used
                        addUsedEntry(currNumber, filePosition, generationNum);
                    }
                    // ignore any free entries.
                    else if (usedOrFree == 'f') {    // Free
                        // check for the first entry 0000000000 65535 f  and
                        // a object range where the first entry isn't zero.  The
                        // code below will treat the first entry as zero and then
                        // start counting.
                        if (startingObjectNumber > 0 &&
                                filePosition == 0 && generationNum == 65535) {
                            // offset the count so we start counting after the zeroed entry
                            currNumber--;
                        }
//                        addFreeEntry(currNumber, (int) filePosition, generationNum);
                    }
                    currNumber++;
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error parsing xRef table entries.", e);
        }
    }

    /**
     * Once a XRef stream is found, the decoded streamInput is itereated over
     * to build out the Xref structure.
     *
     * @param library        The Document's Library
     * @param xrefStreamHash Dictionary for XRef stream
     * @param streamInput    Decoded stream bytes for XRef stream
     */
    @SuppressWarnings("unchecked")
    public void addXRefStreamEntries(Library library, HashMap xrefStreamHash, InputStream streamInput) {
        try {
            // number +1 represented the highest object number.
            int size = library.getInt(xrefStreamHash, SIZE_KEY);
            // pair of integers for each subsection in this section. The first
            // int is the first object number in this section and the second
            // is the number of entries.
            List<Number> objNumAndEntriesCountPairs =
                    (List) library.getObject(xrefStreamHash, INDEX_KEY);
            if (objNumAndEntriesCountPairs == null) {
                objNumAndEntriesCountPairs = new ArrayList<Number>(2);
                objNumAndEntriesCountPairs.add(0);
                objNumAndEntriesCountPairs.add(size);
            }
            // three int's: field values, x,y and z bytes in length.
            List fieldSizesVec = (List) library.getObject(xrefStreamHash, W_KEY);
            int[] fieldSizes = null;
            if (fieldSizesVec != null) {
                fieldSizes = new int[fieldSizesVec.size()];
                for (int i = 0; i < fieldSizesVec.size(); i++)
                    fieldSizes[i] = ((Number) fieldSizesVec.get(i)).intValue();
            }
            // not doing anything with PREV.

            int fieldTypeSize = fieldSizes[0];
            int fieldTwoSize = fieldSizes[1];
            int fieldThreeSize = fieldSizes[2];
            // parse out the object data.
            for (int xrefSubsection = 0; xrefSubsection < objNumAndEntriesCountPairs.size(); xrefSubsection += 2) {
                int startingObjectNumber = objNumAndEntriesCountPairs.get(xrefSubsection).intValue();
                int entriesCount = objNumAndEntriesCountPairs.get(xrefSubsection + 1).intValue();
                int afterObjectNumber = startingObjectNumber + entriesCount;
                for (int objectNumber = startingObjectNumber; objectNumber < afterObjectNumber; objectNumber++) {
                    int entryType = Entry.TYPE_USED;    // Default value is 1
                    if (fieldTypeSize > 0)
                        entryType = Utils.readIntWithVaryingBytesBE(streamInput, fieldTypeSize);
                    // used object but not compressed
                    if (entryType == Entry.TYPE_USED) {
                        long filePositionOfObject = Utils.readLongWithVaryingBytesBE(
                                streamInput, fieldTwoSize);
                        int generationNumber = 0;       // Default value is 0
                        if (fieldThreeSize > 0) {
                            generationNumber = Utils.readIntWithVaryingBytesBE(
                                    streamInput, fieldThreeSize);
                        }
                        addUsedEntry(objectNumber, filePositionOfObject, generationNumber);
                    }
                    // entries define compress objects.
                    else if (entryType == Entry.TYPE_COMPRESSED) {
                        int objectNumberOfContainingObjectStream = Utils.readIntWithVaryingBytesBE(
                                streamInput, fieldTwoSize);
                        int indexWithinObjectStream = Utils.readIntWithVaryingBytesBE(
                                streamInput, fieldThreeSize);
                        addCompressedEntry(
                                objectNumber, objectNumberOfContainingObjectStream, indexWithinObjectStream);

                    }
                    // free objects, no used.
                    else if (entryType == Entry.TYPE_FREE) {
                        // we do nothing but we still need to move the cursor.
                        Utils.readIntWithVaryingBytesBE(
                                streamInput, fieldTwoSize);
                        Utils.readIntWithVaryingBytesBE(
                                streamInput, fieldThreeSize);
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error parsing xRef stream entries.", e);
        }
    }

    public Entry getEntryForObject(Integer objectNumber) {
        Entry entry = hObjectNumber2Entry.get(objectNumber);
        if (entry != null)
            return entry;
        /// fall back code to look for another xref table.
        if (bIsCrossReferenceTable && !bHaveTriedLoadingPeer &&
                xrefPeer == null && pTrailer != null) {
            // Lazily load xrefPeer, using pTrailer
            pTrailer.loadXRefStmIfApplicable();
            xrefPeer = pTrailer.getCrossReferenceStream();
            bHaveTriedLoadingPeer = true;
        }
        if (xrefPeer != null) {
            entry = xrefPeer.getEntryForObject(objectNumber);
            if (entry != null)
                return entry;
        }

        if (!bHaveTriedLoadingPrevious &&
                xrefPrevious == null && pTrailer != null) {
            // Lazily load xrefPrevious, using pTrailer
            pTrailer.onDemandLoadAndSetupPreviousTrailer();
            bHaveTriedLoadingPrevious = true;
        }
        if (xrefPrevious != null) {
            entry = xrefPrevious.getEntryForObject(objectNumber);
            if (entry != null)
                return entry;
        }
        return entry;
    }

    public void addToEndOfChainOfPreviousXRefs(CrossReference prev) {
        if (xrefPrevious == null)
            xrefPrevious = prev;
        else
            xrefPrevious.addToEndOfChainOfPreviousXRefs(prev);
    }

    protected void addFreeEntry(int objectNumber, int nextFreeObjectNumber, int generationNumberIfReused) {
        FreeEntry entry = new FreeEntry(objectNumber, nextFreeObjectNumber, generationNumberIfReused);
//        m_vXRefEntries.add(entry);
    }

    protected void addUsedEntry(int objectNumber, long filePositionOfObject, int generationNumber) {
        UsedEntry entry = new UsedEntry(objectNumber, filePositionOfObject, generationNumber);
        hObjectNumber2Entry.put(objectNumber, entry);
    }

    protected void addCompressedEntry(int objectNumber, int objectNumberOfContainingObjectStream, int indexWithinObjectStream) {
        CompressedEntry entry = new CompressedEntry(objectNumber, objectNumberOfContainingObjectStream, indexWithinObjectStream);
        hObjectNumber2Entry.put(objectNumber, entry);
    }


    public static class Entry {
        public static final int TYPE_FREE = 0;
        public static final int TYPE_USED = 1;
        public static final int TYPE_COMPRESSED = 2;

        private int Type;
        private int objectNumber;

        Entry(int type, int objectNumber) {
            Type = type;
            this.objectNumber = objectNumber;
        }

        int getType() {
            return Type;
        }

        int getObjectNumber() {
            return objectNumber;
        }
    }

    public static class FreeEntry extends Entry {
        private int nextFreeObjectNumber;
        private int generationNumberIfReused;

        FreeEntry(int objectNumber, int nextFreeObjectNumber, int generationNumberIfReused) {
            super(TYPE_FREE, objectNumber);
            this.nextFreeObjectNumber = nextFreeObjectNumber;
            this.generationNumberIfReused = generationNumberIfReused;
        }

        public int getNextFreeObjectNumber() {
            return nextFreeObjectNumber;
        }

        public int getGenerationNumberIfReused() {
            return generationNumberIfReused;
        }
    }

    public class UsedEntry extends Entry {
        private long filePositionOfObject;
        private int generationNumber;

        UsedEntry(int objectNumber, long filePositionOfObject, int generationNumber) {
            super(TYPE_USED, objectNumber);
            this.filePositionOfObject = filePositionOfObject;
            this.generationNumber = generationNumber;
        }

        public long getFilePositionOfObject() {
            return filePositionOfObject + offset;
        }

        public int getGenerationNumber() {
            return generationNumber;
        }

        public void setFilePositionOfObject(long filePositionOfObject) {
            this.filePositionOfObject = filePositionOfObject;
        }
    }

    public static class CompressedEntry extends Entry {
        private int objectNumberOfContainingObjectStream;
        private int indexWithinObjectStream;

        CompressedEntry(int objectNumber, int objectNumberOfContainingObjectStream, int indexWithinObjectStream) {
            super(TYPE_COMPRESSED, objectNumber);
            this.objectNumberOfContainingObjectStream = objectNumberOfContainingObjectStream;
            this.indexWithinObjectStream = indexWithinObjectStream;
        }

        public int getObjectNumberOfContainingObjectStream() {
            return objectNumberOfContainingObjectStream;
        }

        public int getIndexWithinObjectStream() {
            return indexWithinObjectStream;
        }
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
