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
package org.icepdf.core.util;

import org.icepdf.core.io.SeekableInput;
import org.icepdf.core.pobjects.*;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Mark Collette
 * @since 2.0
 */
public class LazyObjectLoader {

    private static final Logger logger =
            Logger.getLogger(LazyObjectLoader.class.toString());

    private Library library;
    private SeekableInput seekableInput;
    private CrossReference crossReference;
    protected SoftLRUCache<Reference, ObjectStream> leastRecentlyUsed;
    private final Object leastRectlyUsedLock = new Object();
    private final Object streamLock = new Object();

    public LazyObjectLoader(Library lib, SeekableInput seekableInput, CrossReference xref) {
        library = lib;
        this.seekableInput = seekableInput;
        crossReference = xref;
        leastRecentlyUsed = new SoftLRUCache<Reference, ObjectStream>(256);
    }

    /**
     * Loads an object via it's reference. Stream object data is initialized
     * so that we can release the file lock more quickly.
     *
     * @param reference object reference
     * @return found object; dictionary, stream or pobject.
     */
    public Object loadObject(Reference reference) {
        if (reference == null || library == null || crossReference == null)
            return null;
        int objNum = reference.getObjectNumber();
        CrossReference.Entry entry = crossReference.getEntryForObject(objNum);
        if (entry == null)
            return null;
        // base cross reference lookup.

        if (entry instanceof CrossReference.UsedEntry) {
            try {
                if (seekableInput != null) {
                    synchronized (streamLock) {
                        CrossReference.UsedEntry usedEntry = (CrossReference.UsedEntry) entry;
                        long position = usedEntry.getFilePositionOfObject();
                        seekableInput.beginThreadAccess();
                        long savedPosition = seekableInput.getAbsolutePosition();
                        seekableInput.seekAbsolute(position);
                        Parser parser = new Parser(seekableInput);
                        Object ob = parser.getObject(library);
                        seekableInput.seekAbsolute(savedPosition);
                        return ob;
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE,
                        "Error loading object instance: " + reference.toString(), e);
            } finally {
                if (seekableInput != null)
                    seekableInput.endThreadAccess();
            }
        }
        // compressed cross reference.
        else if (entry instanceof CrossReference.CompressedEntry) {
            try {
                CrossReference.CompressedEntry compressedEntry = (CrossReference.CompressedEntry) entry;
                int objectStreamsObjectNumber = compressedEntry.getObjectNumberOfContainingObjectStream();
                int objectIndex = compressedEntry.getIndexWithinObjectStream();
                Reference objectStreamRef = new Reference(objectStreamsObjectNumber, 0);
                ObjectStream objectStream;
                synchronized (leastRectlyUsedLock) {
                    objectStream = leastRecentlyUsed.get(objectStreamRef);
                }

                if (objectStream == null) {
                    synchronized (streamLock) {
                        objectStream = (ObjectStream) library.getObject(objectStreamRef);
                    }
                    if (objectStream != null) {
                        synchronized (leastRectlyUsedLock) {
                            leastRecentlyUsed.put(objectStreamRef, objectStream);
                        }
                    }
                }

                if (objectStream != null) {
                    synchronized (streamLock) {
                        return objectStream.loadObject(library, objectIndex);
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE,
                        "Error loading object instance: " + reference.toString(), e);
            }
        }

        return null;
    }

    public boolean haveEntry(Reference reference) {
        if (reference == null || crossReference == null)
            return false;
        int objNum = reference.getObjectNumber();
        CrossReference.Entry entry = crossReference.getEntryForObject(objNum);
        return (entry != null);
    }

    public PTrailer loadTrailer(long position) {
        PTrailer trailer = null;
        try {
            if (seekableInput != null) {
                seekableInput.beginThreadAccess();
                long savedPosition = seekableInput.getAbsolutePosition();
                seekableInput.seekAbsolute(position);
                Parser parser = new Parser(seekableInput);
                Object obj = parser.getObject(library);
                if (obj instanceof PObject)
                    obj = ((PObject) obj).getObject();
                trailer = (PTrailer) obj;
                if (trailer != null)
                    trailer.setPosition(position);
                seekableInput.seekAbsolute(savedPosition);
            }
        } catch (Exception e) {
            logger.log(Level.FINE,
                    "Error loading PTrailer instance: " + position, e);
        } finally {
            if (seekableInput != null)
                seekableInput.endThreadAccess();
        }
        return trailer;
    }

    /**
     * Get the documents library object.
     *
     * @return documents library object.
     */
    public Library getLibrary() {
        return library;
    }
}
