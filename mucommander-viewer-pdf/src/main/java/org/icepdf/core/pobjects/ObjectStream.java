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

import org.icepdf.core.io.SeekableByteArrayInputStream;
import org.icepdf.core.io.SeekableInput;
import org.icepdf.core.io.SeekableInputConstrainedWrapper;
import org.icepdf.core.util.Library;
import org.icepdf.core.util.Parser;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Mark Collette
 * @since 2.0
 */
public class ObjectStream extends Stream {

    private static final Logger logger =
            Logger.getLogger(Form.class.toString());

    public static final Name N_KEY = new Name("N");
    public static final Name FIRST_KEY = new Name("First");

    private boolean init;
    private SeekableInput decodedStream;
    private int[] objectNumbers;
    private long[] objectOffset;

    /**
     * Create a new instance of a Stream.
     *
     * @param l                  library containing a hash of all document objects
     * @param h                  HashMap of parameters specific to the Stream object.
     * @param streamInputWrapper Accessor to stream byte data
     */
    public ObjectStream(Library l, HashMap h, SeekableInputConstrainedWrapper streamInputWrapper) {
        super(l, h, streamInputWrapper);
    }

    public synchronized void init() {
        if (init)
            return;
        init = true;
        int numObjects = library.getInt(entries, N_KEY);
        long firstObjectsOffset = library.getLong(entries, FIRST_KEY);
        // get the stream data
        decodedStream = new SeekableByteArrayInputStream(getDecodedStreamBytes(0));
//        decodedStream.beginThreadAccess();
        objectNumbers = new int[numObjects];
        objectOffset = new long[numObjects];
        try {
            Parser parser = new Parser(decodedStream);
            for (int i = 0; i < numObjects; i++) {
                objectNumbers[i] = parser.getIntSurroundedByWhitespace();
                objectOffset[i] = parser.getLongSurroundedByWhitespace() + firstObjectsOffset;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,
                    "Error loading object stream instance: ", e);
        }
//        finally {
//            decodedStream.endThreadAccess();
//        }

    }

    public Object loadObject(Library library, int objectIndex) {
//System.out.println("ObjectStream.loadObject()  objectIndex: " + objectIndex);
        init();
        if (objectNumbers == null ||
                objectOffset == null ||
                objectNumbers.length != objectOffset.length ||
                objectIndex < 0 ||
                objectIndex >= objectNumbers.length) {
//System.out.println("ObjectStream.loadObject()  init failed");
            return null;
        }

        try {
            int objectNumber = objectNumbers[objectIndex];
            long position = objectOffset[objectIndex];
//System.out.println("ObjectStream.loadObject()  objectNumber: " + objectNumber + ", position: " + position);
            decodedStream.beginThreadAccess();
            decodedStream.seekAbsolute(position);
            Parser parser = new Parser(decodedStream, Parser.PARSE_MODE_OBJECT_STREAM);
            // Parser.getObject() either does 1 of 3 things:
            // 1. Gets a core object (Dictionary or Stream), adds it to Library
            //    by object Reference, returns PObject
            // 2. Gets a non-core-object, leaves it on stack, returns null
            // 3. Gets a non-core-object, returns it
            Object ob = parser.getObject(library);
            if (ob == null) {
                Reference ref = new Reference(objectNumber, 0);
                PObject pObject = parser.addPObject(library, ref);
                ob = pObject.getObject();
            } else if (!(ob instanceof PObject)) {
                Reference ref = new Reference(objectNumber, 0);
                library.addObject(ob, ref);
            }
            // assign object reference, needed for encrypting and state saving
            if (ob != null && ob instanceof Dictionary) {
                ((Dictionary) ob).setPObjectReference(
                        new Reference(objectNumber, 0));
            }

//System.out.println("ObjectStream.loadObject()  ob: " + ob + ",  ob.class: " + ob.getClass().getName());
            return ob;
        } catch (Exception e) {
            logger.log(Level.FINE, "Error loading PDF object.", e);
            return null;
        } finally {
            decodedStream.endThreadAccess();
        }
    }
}
