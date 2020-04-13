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

import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.io.*;
import org.icepdf.core.pobjects.*;
import org.icepdf.core.pobjects.annotations.Annotation;
import org.icepdf.core.pobjects.fonts.CMap;
import org.icepdf.core.pobjects.fonts.Font;
import org.icepdf.core.pobjects.fonts.FontDescriptor;
import org.icepdf.core.pobjects.fonts.FontFactory;
import org.icepdf.core.pobjects.graphics.TilingPattern;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * put your documentation comment here
 */
public class Parser {

    private static final Logger logger =
            Logger.getLogger(Parser.class.toString());

    public static final int PARSE_MODE_NORMAL = 0;
    public static final int PARSE_MODE_OBJECT_STREAM = 1;

    // InputStream has to support mark(), reset(), and markSupported()
    // DO NOT close this, since we have two cases: read everything up front, and progressive reads
//    private BufferedMarkedInputStream reader;

    private InputStream reader;
    boolean lastTokenHString = false;
    private Stack<Object> stack = new Stack<Object>();
    private int parseMode;
    private boolean isTrailer;
    private int linearTraversalOffset;

    public Parser(SeekableInput r) {
        this(r, PARSE_MODE_NORMAL);
    }

    public Parser(SeekableInput r, int pm) {
//        reader = new BufferedMarkedInputStream(r.getInputStream());
        reader = r.getInputStream();
        parseMode = pm;
    }

    public Parser(InputStream r) {
        this(r, PARSE_MODE_NORMAL);
    }

    public Parser(InputStream r, int pm) {
        reader = new BufferedMarkedInputStream(r);
        parseMode = pm;
    }

    /**
     * Get an object from the pdf input DataInputStream.
     *
     * @param library all found objects in the pdf document
     * @return the next object in the DataInputStream.  Null is returned
     * if there are no more objects left in the DataInputStream or
     * a I/O error is encountered.
     * @throws PDFException error getting object from library
     */
    public Object getObject(Library library) throws PDFException {
        int deepnessCount = 0;
        boolean inObject = false; // currently parsing tokens in an object
        boolean complete = false; // flag used for do loop.
        Object nextToken;
        Reference objectReference = null;
        try {
            reader.mark(1);
            // capture the byte offset of this object so we can rebuild
            // the cross reference entries for lazy loading after CG.
            if (library.isLinearTraversal() && reader instanceof BufferedMarkedInputStream) {
                linearTraversalOffset = ((BufferedMarkedInputStream) reader).getMarkedPosition();
            }
            do { //while (!complete);
                // keep track of currently parsed objects reference
                // get the next token inside the object stream
                try {
                    nextToken = getToken();
                    // commented out for performance reasons
                    //Thread.yield();
                } catch (IOException e) {
                    // eat it as it is what is expected
//                    logger.warning("IO reading error.");
                    return null;
                }

                // check for specific primative object types returned by getToken()
                if (nextToken instanceof StringObject
                        || nextToken instanceof Name
                        || nextToken instanceof Number) {
                    // Very Important, store the PDF object reference information,
                    // as it is needed when to decrypt an encrypted string.
                    if (nextToken instanceof StringObject) {
                        StringObject tmp = (StringObject) nextToken;
                        tmp.setReference(objectReference);
                    }
                    stack.push(nextToken);
                }
                // mark that we have entered a object declaration
                else if (nextToken.equals("obj")) {
                    // a rare parsing error is that endobj is missing, so we need
                    // to make sure if an object has been parsed that we don't loose it.
                    if (inObject) {
                        // pop off the object and ref number
                        stack.pop();
                        stack.pop();
                        // return the passed over object on the stack.
                        return addPObject(library, objectReference);
                    }
                    // Since we can return objects on "endstream", then we can
                    //  leave straggling "endobj", which would deepnessCount--,
                    //  even though they're done in a separate method invocation
                    // Hence, "obj" does /deepnessCount = 1/ instead of /deepnessCount++/
                    deepnessCount = 0;
                    inObject = true;
                    Number generationNumber = (Number) (stack.pop());
                    Number objectNumber = (Number) (stack.pop());
                    objectReference = new Reference(objectNumber,
                            generationNumber);
                }
                // mark that we have reached the end of the object
                else if (nextToken.equals("endobj") || nextToken.equals("endobject")
                        || nextToken.equals("enbobj")) {
                    if (inObject) {
                        // set flag to false, as we are done parsing an Object
                        inObject = false;
                        // return PObject,
                        return addPObject(library, objectReference);
                        // else, we ignore as the endStream token also returns a
                        // PObject.
                    } else {
//                        return null;
                    }
                }
                // found endstream object, we will return the PObject containing
                // the stream as there can be no further tokens.  This addresses
                // an incorrect a syntax error with OpenOffice document where
                // the endobj tag is missing on some Stream objects.
                else if (nextToken.equals("endstream")) {
                    deepnessCount--;
                    // do nothing, but don't add it to the stack
                    if (inObject) {
                        inObject = false;
                        // return PObject,
                        return addPObject(library, objectReference);
                    }
                }

                // found a stream object, streams are allways defined inside
                // of a object so we will always have a dictionary (hash) that
                // has the length and filter definitions in it
                else if (nextToken.equals("stream")) {
                    deepnessCount++;
                    // pop dictionary that defines the stream
                    Object tmp = stack.pop();
                    HashMap streamHash;
                    if (tmp instanceof Dictionary) {
                        streamHash = ((Dictionary) tmp).getEntries();
                    } else {
                        streamHash = (HashMap) tmp;
                    }
                    // find the length of the stream
                    int streamLength = library.getInt(streamHash, Dictionary.LENGTH_KEY);

                    SeekableInputConstrainedWrapper streamInputWrapper;
                    try {
                        // a stream token's end of line marker can be either:
                        // - a carriage return and a line feed
                        // - just a line feed, and not by a carriage return alone.

                        // check for carriage return and line feed, but reset if
                        // just a carriage return as it is a valid stream byte
                        reader.mark(2);

                        // alway eat a 13,against the spec but we have several examples of this.
                        int curChar = reader.read();
                        if (curChar == 13) {
                            reader.mark(1);
                            if (reader.read() != 10) {
                                reader.reset();
                            }
                        }
                        // always eat a 10
                        else if (curChar == 10) {
                            // eat the stream character
                        }
                        // reset the rest
                        else {
                            reader.reset();
                        }

                        if (reader instanceof SeekableInput) {
                            SeekableInput streamDataInput = (SeekableInput) reader;
                            long filePositionOfStreamData = streamDataInput.getAbsolutePosition();
                            long lengthOfStreamData;
                            // If the stream has a length that we can currently use
                            // such as a R that has been parsed or an integer
                            if (streamLength > 0) {
                                lengthOfStreamData = streamLength;
                                streamDataInput.seekRelative(streamLength);
                                // Read any extraneous data coming after the length, but before endstream
                                lengthOfStreamData += skipUntilEndstream(null);
                            } else {
                                lengthOfStreamData = captureStreamData(null);
                            }
                            streamInputWrapper = new SeekableInputConstrainedWrapper(
                                    streamDataInput, filePositionOfStreamData, lengthOfStreamData);
                        } else { // reader is just regular InputStream (BufferedInputStream)
                            // stream  NOT SeekableInput
                            ConservativeSizingByteArrayOutputStream out;
                            // If the stream in from a regular InputStream,
                            //  then the PDF was probably linearly traversed,
                            //  in which case it doesn't matter if they have
                            //  specified the stream length, because we can't
                            //  trust that anyway
                            if (!library.isLinearTraversal() && streamLength > 0) {
                                byte[] buffer = new byte[streamLength];
                                int totalRead = 0;
                                while (totalRead < buffer.length) {
                                    int currRead = reader.read(buffer, totalRead, buffer.length - totalRead);
                                    if (currRead <= 0)
                                        break;
                                    totalRead += currRead;
                                }
                                out = new ConservativeSizingByteArrayOutputStream(
                                        buffer);
                                // Read any extraneous data coming after the length, but before endstream
                                skipUntilEndstream(out);
                            }
                            // if stream doesn't have a length, read the stream
                            // until end stream has been found
                            else {
                                //  stream  NOT SeekableInput  No trusted streamLength");
                                out = new ConservativeSizingByteArrayOutputStream(
                                        16 * 1024);
                                captureStreamData(out);
                            }

                            int size = out.size();
                            out.trim();
                            byte[] buffer = out.relinquishByteArray();

                            SeekableInput streamDataInput = new SeekableByteArrayInputStream(buffer);
                            long filePositionOfStreamData = 0L;
                            streamInputWrapper = new SeekableInputConstrainedWrapper(
                                    streamDataInput, filePositionOfStreamData, size);
                        }
                    } catch (IOException e) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.log(Level.FINE, "Error getting next object", e);
                        }
                        return null;
                    }
                    PTrailer trailer = null;
                    // set the stream know objects if possible
                    Stream stream = null;
                    Name type = (Name) library.getObject(streamHash, Dictionary.TYPE_KEY);
                    Name subtype = (Name) library.getObject(streamHash, Dictionary.SUBTYPE_KEY);
                    if (type != null) {
                        // found a xref stream which is made up it's own entry format
                        // different then an standard xref table, mainly used to
                        // access cross-reference entries but also to compress xref tables.
                        if (type.equals("XRef")) {
                            stream = new Stream(library, streamHash, streamInputWrapper);
                            stream.init();
                            InputStream in = stream.getDecodedByteArrayInputStream();
                            CrossReference xrefStream = new CrossReference();
                            if (in != null) {
                                try {
                                    xrefStream.addXRefStreamEntries(library, streamHash, in);
                                } finally {
                                    try {
                                        in.close();
                                    } catch (Throwable e) {
                                        logger.log(Level.WARNING, "Error appending stream entries.", e);
                                    }
                                }
                            }

                            // XRef dict is both Trailer dict and XRef stream dict.
                            // PTrailer alters its dict, so copy it to keep everything sane
                            HashMap trailerHash = (HashMap) streamHash.clone();
                            trailer = new PTrailer(library, trailerHash, null, xrefStream);
                        } else if (type.equals("ObjStm")) {
                            stream = new ObjectStream(library, streamHash, streamInputWrapper);
                        } else if (type.equals("XObject") && subtype.equals("Image")) {
                            stream = new ImageStream(library, streamHash, streamInputWrapper);
                        }
                        // new Tiling Pattern Object, will have a stream.
                        else if (type.equals("Pattern")) {
                            stream = new TilingPattern(library, streamHash, streamInputWrapper);
                        }
                    }
                    if (stream == null && subtype != null) {
                        // new form object
                        if (subtype.equals("Image")) {
                            stream = new ImageStream(library, streamHash, streamInputWrapper);
                        } else if (subtype.equals("Form") && !"Pattern".equals(type)) {
                            stream = new Form(library, streamHash, streamInputWrapper);
                        } else if (subtype.equals("Form") && "Pattern".equals(type)) {
                            stream = new TilingPattern(library, streamHash, streamInputWrapper);
                        }
                    }
                    if (trailer != null) {
                        stack.push(trailer);
                    } else {
                        // finally create a generic stream object which will be parsed
                        // at a later time
                        if (stream == null) {
                            stream = new Stream(library, streamHash, streamInputWrapper);
                        }
                        stack.push(stream);
                        // forcing a object return just encase the length is wrong
                        // and we don't get to the endstream.
                        return addPObject(library, objectReference);
                    }
                }
                // end if (stream)

                // boolean objects are added to stack
                else if (nextToken.equals("true")) {
                    stack.push(true);
                } else if (nextToken.equals("false")) {
                    stack.push(false);
                }
                // Indirect Reference object found
                else if (nextToken.equals("R")) {
                    // generationNumber number important for revisions
                    Number generationNumber = (Number) (stack.pop());
                    Number objectNumber = (Number) (stack.pop());
                    stack.push(new Reference(objectNumber,
                            generationNumber));
                } else if (nextToken.equals("[")) {
                    deepnessCount++;
                    stack.push(nextToken);
                }
                // Found an array
                else if (nextToken.equals("]")) {
                    deepnessCount--;
                    final int searchPosition = stack.search("[");
                    int size = searchPosition - 1;
                    if (size < 0) {
                        logger.warning("Negative array size, a  malformed content " +
                                "stream has likely been encountered.");
                        size = 0;
                    }
                    List<Object> v = new ArrayList<Object>(size);
                    Object[] tmp = new Object[size];
                    if (searchPosition > 0) {
                        for (int i = size - 1; i >= 0; i--) {
                            tmp[i] = stack.pop();
                        }
                        // we need a mutable array so copy into an arrayList
                        // so we can't use Arrays.asList().
                        for (int i = 0; i < size; i++) {
                            v.add(tmp[i]);
                        }
                        stack.pop(); // "["
                    } else {
                        stack.clear();
                    }
                    stack.push(v);
                } else if (nextToken.equals("<<")) {
                    deepnessCount++;
                    stack.push(nextToken);
                }
                // Found a Dictionary
                else if (nextToken.equals(">>")) {
                    deepnessCount--;
                    // check for extra >> which we want to ignore
                    if (!isTrailer && deepnessCount >= 0) {
                        if (!stack.isEmpty()) {
                            HashMap<Object, Object> hashMap = new HashMap<Object, Object>();
                            Object obj = stack.pop();
                            // put all of the dictionary definistion into the
                            // the hashTabl
                            while (!((obj instanceof String)
                                    && (obj.equals("<<"))) && !stack.isEmpty()) {
                                Object key = stack.pop();
                                hashMap.put(key, obj);
                                if (!stack.isEmpty()) {
                                    obj = stack.pop();
                                } else {
                                    break;
                                }
                            }
                            obj = hashMap.get(Dictionary.TYPE_KEY);
                            if (obj == null) {
                                // PDF-927,  incorrect /type def.
                                obj = hashMap.get(new Name("type"));
                            }
                            // Process the know first level dictionaries.
                            if (obj != null && obj instanceof Name) {
                                Name n = (Name) obj;
                                if (n.equals(Catalog.TYPE)) {
                                    stack.push(new Catalog(library, hashMap));
                                } else if (n.equals(PageTree.TYPE)) {
                                    stack.push(new PageTree(library, hashMap));
                                } else if (n.equals(Page.TYPE)) {
                                    stack.push(new Page(library, hashMap));
                                } else if (n.equals(Font.TYPE)) {
                                    // do a quick check to make sure we don't have a fontDescriptor
                                    // FontFile is specific to font descriptors.
                                    boolean fontDescriptor = hashMap.get(FontDescriptor.FONT_FILE) != null ||
                                            hashMap.get(FontDescriptor.FONT_FILE_2) != null ||
                                            hashMap.get(FontDescriptor.FONT_FILE_3) != null;
                                    if (!fontDescriptor) {
                                        stack.push(FontFactory.getInstance()
                                                .getFont(library, hashMap));
                                    } else {
                                        stack.push(new FontDescriptor(library, hashMap));
                                    }
                                } else if (n.equals(FontDescriptor.TYPE)) {
                                    stack.push(new FontDescriptor(library, hashMap));
                                } else if (n.equals(CMap.TYPE)) {
                                    stack.push(hashMap);
                                } else if (n.equals(Annotation.TYPE)) {
                                    stack.push(Annotation.buildAnnotation(library, hashMap));
                                } else if (n.equals(OptionalContentGroup.TYPE)) {
                                    stack.push(new OptionalContentGroup(library, hashMap));
                                } else if (n.equals(OptionalContentMembership.TYPE)) {
                                    stack.push(new OptionalContentMembership(library, hashMap));
                                } else
                                    stack.push(hashMap);
                            }
                            // everything else gets pushed onto the stack
                            else {
                                stack.push(hashMap);
                            }
                        }
                    } else if (isTrailer && deepnessCount == 0) {
                        // we have an xref entry
                        HashMap<Object, Object> hashMap = new HashMap<Object, Object>();
                        Object obj = stack.pop();
                        // put all of the dictionary definition into the
                        // the new map.
                        while (!((obj instanceof String)
                                && (obj.equals("<<"))) && !stack.isEmpty()) {
                            Object key = stack.pop();
                            hashMap.put(key, obj);
                            if (!stack.isEmpty()) {
                                obj = stack.pop();
                            } else {
                                break;
                            }
                        }
                        return hashMap;
                    }
                }
                // found traditional XrefTable found in all documents.
                else if (nextToken.equals("xref")) {
                    // parse out hte traditional
                    CrossReference xrefTable = new CrossReference();
                    xrefTable.addXRefTableEntries(this);
                    stack.push(xrefTable);
                } else if (nextToken.equals("trailer")) {
                    CrossReference xrefTable = null;
                    if (stack.peek() instanceof CrossReference)
                        xrefTable = (CrossReference) stack.pop();
                    stack.clear();
                    isTrailer = true;
                    HashMap trailerDictionary = (HashMap) getObject(library);
                    isTrailer = false;
                    return new PTrailer(library, trailerDictionary, xrefTable, null);
                }
                // comments
                else if (nextToken instanceof String &&
                        ((String) nextToken).startsWith("%")) {
                    // Comment, ignored for now
                }
                // corner case for encoder error "endobjxref"
                else if (nextToken instanceof String &&
                        ((String) nextToken).startsWith("endobj")) {
                    if (inObject) {
                        // set flag to false, as we are done parsing an Object
                        inObject = false;
                        // return PObject,
                        return addPObject(library, objectReference);
                    }
                }
                // everything else gets pushed onto the stack
                else {
                    stack.push(nextToken);
                }
                if (parseMode == PARSE_MODE_OBJECT_STREAM && deepnessCount == 0 && stack.size() > 0) {
                    return stack.pop();
                }
            }
            while (!complete);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Fatal error parsing PDF file stream.", e);
            return null;
        }
        // return the top of the stack
        return stack.pop();
    }

    /**
     *
     */
    public String peek2() throws IOException {
        reader.mark(2);
        char c[] = new char[2];
        c[0] = (char) reader.read();
        c[1] = (char) reader.read();
        String s = new String(c);
        reader.reset();
        return s;
    }

    /**
     * @return true if ate the ending EI delimiter
     * @throws java.io.IOException
     */
    public boolean readLineForInlineImage(OutputStream out) throws IOException {
        // The encoder might not have put EI on its own line (as it should),
        //  but might just put it right after the data
        final int STATE_PRE_E = 0;
        final int STATE_PRE_I = 1;
        final int STATE_PRE_WHITESPACE = 2;
        int state = STATE_PRE_E;

        while (true) {
            int c = reader.read();
            if (c < 0)
                break;
            if (state == STATE_PRE_E && c == 'E') {
                state++;
            } else if (state == STATE_PRE_I && c == 'I') {
                state++;
            } else if (state == STATE_PRE_WHITESPACE && isWhitespace((char) (0xFF & c))) {
                // It's hard to tell if the EI + whitespace is part of the
                //  image data or not, given that many PDFs are mis-encoded,
                //  and don't give whitespace when necessary. So, instead of
                //  assuming the need for whitespace, we're going to assume
                //  that this is the real EI, and apply a heuristic to prove
                //  ourselves wrong.
                boolean imageDataFound = isStillInlineImageData(reader, 32);
                if (imageDataFound) {
                    out.write('E');
                    out.write('I');
                    out.write(c);
                    state = STATE_PRE_E;

                    if (c == '\r' || c == '\n') {
                        break;
                    }
                } else
                    return true;
            } else {
                // If we got a fragment of the EI<whitespace> sequence, then we withheld
                //  what we had so far.  But if we're here, that fragment was incomplete,
                //  so that was actual embedded data, and not the delimiter, so we have
                //  to write it out.
                if (state > STATE_PRE_E)
                    out.write('E');
                if (state > STATE_PRE_I)
                    out.write('I');
                state = STATE_PRE_E;

                out.write((byte) c);
                if (c == '\r' || c == '\n') {
                    break;
                }
            }
        }
        // If the input ends right after the EI, but with no whitespace,
        //  then we're still done
        return state == STATE_PRE_WHITESPACE;
    }

    /**
     * We want to be conservative in deciding that we're still in the inline
     * image, since we haven't found any of these cases before now.
     */
    private static boolean isStillInlineImageData(
            InputStream reader, int numBytesToCheck)
            throws IOException {
        boolean imageDataFound = false;
        boolean onlyWhitespaceSoFar = true;
        reader.mark(numBytesToCheck);
        byte[] toCheck = new byte[numBytesToCheck];
        int numReadToCheck = reader.read(toCheck);
        for (int i = 0; i < numReadToCheck; i++) {
            char charToCheck = (char) (((int) toCheck[i]) & 0xFF);

            // If the very first thing we read is a Q or S token
            boolean typicalTextTokenInContentStream =
                    (charToCheck == 'Q' || charToCheck == 'q' ||
                            charToCheck == 'S' || charToCheck == 's');
            if (onlyWhitespaceSoFar &&
                    typicalTextTokenInContentStream &&
                    (i + 1 < numReadToCheck) &&
                    isWhitespace((char) (((int) toCheck[i + 1]) & 0xFF))) {
                break;
            }
            if (!isWhitespace(charToCheck))
                onlyWhitespaceSoFar = false;

            // If we find some binary image data
            if (!isExpectedInContentStream(charToCheck)) {
                imageDataFound = true;
                break;
            }
        }
        reader.reset();
        return imageDataFound;
    }

    /**
     * This is not necessarily an exhaustive list of characters one would
     * expect in a Content Stream, it's a heuristic for whether the data
     * might still be part of an inline image, or the lattercontent stream
     */
    private static boolean isExpectedInContentStream(char c) {
        return ((c >= 'a' && c <= 'Z') ||
                (c >= 'A' && c <= 'Z') ||
                (c >= '0' && c <= '9') ||
                isWhitespace(c) ||
                isDelimiter(c) ||
                (c == '\\') ||
                (c == '\'') ||
                (c == '\"') ||
                (c == '*') ||
                (c == '.'));
    }


    /**
     * Utility Method for getting a PObject from the stack and adding it to the
     * library.  The retrieved PObject has an ObjectReference added to it for
     * decryption purposes.
     *
     * @param library         HashMap of all objects in document
     * @param objectReference PObjet indirect reference data
     * @return a valid PObject.
     */
    public PObject addPObject(Library library, Reference objectReference) {
        Object o = stack.pop();

        // Add the streams object reference which is needed for
        // decrypting encrypted streams
        if (o instanceof Stream) {
            Stream tmp = (Stream) o;
            tmp.setPObjectReference(objectReference);
        }

        // Add the dictionary object reference which is needed for
        // decrypting encrypted string contained in the dictionary
        else if (o instanceof Dictionary) {
            Dictionary tmp = (Dictionary) o;
            tmp.setPObjectReference(objectReference);
        }

        // the the object to the library
        library.addObject(o, objectReference);

        return new PObject(o, objectReference);
    }

    /**
     * Returns the next object found in a content stream.
     *
     * @return next object in the input stream
     * @throws java.io.IOException when the end of the <code>InputStream</code>
     *                             has been encountered.
     */
    public Object getStreamObject() throws IOException {

        Object o = getToken();
        if (o instanceof String) {
            if (o.equals("<<")) {
                HashMap<Object, Object> h = new HashMap<Object, Object>();
                Object o1 = getStreamObject();
                while (!o1.equals(">>")) {
                    h.put(o1, getStreamObject());
                    o1 = getStreamObject();
                }
                o = h;
            }
            // arrays are only used for CID mappings, the hex decoding is delayed
            // as a result using the CID_STREAM flag
            else if (o.equals("[")) {
                List<Object> v = new ArrayList<Object>();
                Object o1 = getStreamObject();
                while (!o1.equals("]")) {
                    v.add(o1);
                    o1 = getStreamObject();
                }
                o = v;
            }
        }
        return o;
    }

    /**
     * Utility method used to parse a valid pdf token from an DataIinputStream.
     * Each call to this method return one pdf token.  The Reader object is
     * used to "mark" the location of the last "read".
     *
     * @return the next token in the pdf data stream
     * @throws java.io.IOException if an I/O error occurs.
     */
    public Object getToken() throws IOException {

        int currentByte;
        char currentChar;
        boolean inString = false;  // currently parsing a string
        boolean hexString = false;
        boolean inNumber = false;
        lastTokenHString = false;

        // strip all white space characters
        do {
            currentByte = reader.read();
            // input stream interrupted
            if (currentByte < 0) {
                throw new IOException();
            }
            currentChar = (char) currentByte;
        }
        while (isWhitespace(currentChar));

        /**
         *  look the start of different primitive pdf objects
         * ( - strints
         * [ - arrays
         * % - comments
         * numbers.
         */
        if (currentChar == '(') {
            // mark that we are currrently processing a string
            inString = true;
        } else if (currentChar == ']') {
            // fount end of an array
            return "]";
        } else if (currentChar == '[') {
            // fount begining of an array
            return "[";
        } else if (currentChar == '%') {
            // ignore all the characters after a comment token until
            // we get to the end of the line
            StringBuilder stringBuffer = new StringBuilder();
            do {
                stringBuffer.append(currentChar);
                currentByte = reader.read();
                if (currentByte < 0) {
                    // Final %%EOF might not have CR LF afterwards
                    if (stringBuffer.length() > 0)
                        return stringBuffer.toString();
                    throw new IOException();
                }
                currentChar = (char) currentByte;
            }
            while (currentChar != 13 && currentChar != 10);
            // return all the text that is in the comment
            return stringBuffer.toString();
        } else if ((currentChar >= '0' && currentChar <= '9') ||
                currentChar == '-' || currentChar == '+' || currentChar == '.') {
            inNumber = true;
        }

        // mark this location in the input stream
        reader.mark(1);

        // read the next char from the reader
        char nextChar = (char) reader.read();

        // Check for dictionaries, start '<<' and end '>>'
        if (currentChar == '>' && nextChar == '>') {
            return ">>";
        }
        if (currentChar == '<') {
            // if two "<<" then we have a dictionary
            if (nextChar == '<') {
                return "<<";
            }
            // Otherwise we have a hex number
            else {
                inString = true;
                hexString = true;
            }
        }

        // return to the previous mark
        reader.reset();

        // store the parsed char in the token buffer.
        StringBuilder stringBuffer = new StringBuilder();

        stringBuffer.append(currentChar);

        /**
         * Finally parse the contents of a complex token
         */

        int parenthesisCount = 0;
        boolean complete = false;
        // indicates that the current char should be ignored and not added to
        // the current string.
        boolean ignoreChar = false;

        do { // while !complete

            // if we are not parsing a string mark the location
            if (!inString) {
                reader.mark(1);
            }

            // get the next byte and corresponding char
            currentByte = reader.read();
            if (currentByte >= 0) {
                currentChar = (char) currentByte;
            } else {
                // if there are no more bytes (-1) then we must have reached the end of this token,
                // though maybe without appropriate termination of a string object. We'll just treat
                // them as if they were.
                break;
            }

            // if we are parsing a token that is a string, (...)
            if (inString) {
                if (hexString) {
                    // found the end of a dictionary
                    if (currentChar == '>') {
                        stringBuffer.append(currentChar);
                        break;
                    }
                } else {
                    // look for embedded strings
                    if (currentChar == '(') {
                        parenthesisCount++;
                    }
                    if (currentChar == ')') {
                        if (parenthesisCount == 0) {
                            stringBuffer.append(currentChar);
                            break;
                        } else {
                            parenthesisCount--;
                        }
                    }
                    // look for  "\" character
                    /**
                     * The escape sequences can be as follows:
                     *   \n  - line feed (LF)
                     *   \r  - Carriage return (CR)
                     *   \t  - Horizontal tab  (HT)
                     *   \b  - backspace (BS)
                     *   \f  - form feed (FF)
                     *   \(  - left parenthesis
                     *   \)  - right parenthesis
                     *   \\  - backslash
                     *   \ddd - character code ddd (octal)
                     *
                     * Note: (\0053) denotes a string containing two characters,
                     *       \005 (Control-E) followed by the digit 3.
                     */
                    if (currentChar == '\\') {
                        // read next char
                        currentChar = (char) reader.read();

                        // check for a digit, if so we have an octal
                        // and we need to handle it correctly
                        if (Character.isDigit(currentChar)) {
                            // store the read digits
                            StringBuilder digit = new StringBuilder();
                            digit.append(currentChar);
                            // octals have a max size of 3 digits, we already
                            // have one, so there can be up 2 more digits.
                            for (int i = 0; i < 2; i++) {
                                // mark the reader incase the next read is not
                                // a digit.
                                reader.mark(1);
                                // read next char
                                currentChar = (char) reader.read();
                                if (Character.isDigit(currentChar)) {
                                    digit.append(currentChar);
                                } else {
                                    // back up the reader just incase
                                    // thre is only 1 or 2 digits in the octal
                                    reader.reset();
                                    break;
                                }
                            }

                            // finally convert digit to a character
                            int charNumber = 0;
                            try {
                                charNumber = Integer.parseInt(digit.toString(), 8);
                            } catch (NumberFormatException e) {
                                logger.log(Level.FINE, "Integer parse error ", e);
                            }
                            // convert the interger from octal to dec.
                            currentChar = (char) charNumber;
                        }
                        // do nothing
                        else if (currentChar == '(' || currentChar == ')'
                                || currentChar == '\\') {
                            // do nothing
                        }
                        // capture the horizontal tab (HT), tab character is hard
                        // to find, only appears in files with font substitution and
                        // as a result we ahve better luck drawing a space character.
                        else if (currentChar == 't') {
                            currentChar = '\t';
                        }
                        // capture the carriage return (CR)
                        else if (currentChar == 'r') {
                            currentChar = '\r';
                        }
                        // capture the line feed (LF)
                        else if (currentChar == 'n') {
                            currentChar = '\n';
                        }
                        // capture the backspace (BS)
                        else if (currentChar == 'b') {
                            currentChar = '\b';
                        }
                        // capture the form feed (FF)
                        else if (currentChar == 'f') {
                            currentChar = '\f';
                        }
                        // ignor CF, which indicate a '\' lone split line token
                        else if (currentChar == 13) {
                            ignoreChar = true;
                        }
                        // otherwise report the file format error
                        else {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.warning("C=" + ((int) currentChar));
                            }
                        }
                    }
                }
            }
            // if we are not in a string definition we want to break
            // and return the current token, as white spaces or other elements
            // would mean that we are on the next token
            else if (isWhitespace(currentChar)) {
                // we need to return the CR LR, as it is need by stream parsing
                if (currentByte == 13 || currentByte == 10) {
                    reader.reset();
                    break;
                }
                // break on any whitespace
                else {
                    // return  stringBuffer.toString();
                    break;
                }
            } else if (isDelimiter(currentChar)) {
                // reset the reader so we start on this token on the next parse
                reader.reset();
                break;
            }
            // append the current char and keep parsing if needed
            // IgnoreChar is set by the the line split char '\'
            if (!ignoreChar) {
                if (inString) {
                    stringBuffer.append(currentChar);
                }
                // eat any junk characters
                else if (currentChar < 128) {
                    stringBuffer.append(currentChar);
                }
            }
            // reset the ignorChar flag
            else {
                ignoreChar = false;
            }
        }
        while (!complete);

        /**
         * Return what we found
         */
        // if a hex string decode it as needed
        if (hexString) {
            lastTokenHString = true;
            return new HexStringObject(stringBuffer);
        }

        // do a little clean up for any object that may have been missed..
        // this mainly for the the document trailer information
        // a orphaned string
        if (inString) {
            return new LiteralStringObject(stringBuffer);
        }
        // return a new name
        else if (stringBuffer.charAt(0) == '/') {
            return new Name(stringBuffer.deleteCharAt(0));
        }
        // if a number try and parse it
        else if (inNumber) {
            return getNumber(stringBuffer);
        }
        return stringBuffer.toString();
    }

    public Object getNumberOrStringWithMark(int maxLength) throws IOException {
        reader.mark(maxLength);

        StringBuilder sb = new StringBuilder(maxLength);
        boolean readNonWhitespaceYet = false;
        boolean foundDigit = false;
        boolean foundDecimal = false;

        for (int i = 0; i < maxLength; i++) {
            int curr = reader.read();
            if (curr < 0)
                break;
            char currChar = (char) curr;
            if (isWhitespace(currChar)) {
                if (readNonWhitespaceYet)
                    break;
            } else if (isDelimiter(currChar)) {
                // Number or string has delimiter immediately after it,
                //   which we'll have to unread.
                // Had hoped it would be whitespace, so wouldn't have to unread
                reader.reset();
                reader.mark(maxLength);
                for (int j = 0; j < i; j++) {
                    reader.read();
                }
                readNonWhitespaceYet = true;
                break;
            } else {
                readNonWhitespaceYet = true;
                if (currChar == '.')
                    foundDecimal = true;
                else if (currChar >= '0' && curr <= '9')
                    foundDigit = true;
                sb.append(currChar);
            }
        }

        // Only bother trying to interpret as a number if contains a digit somewhere,
        //   to reduce NumberFormatExceptions
        if (foundDigit) {
            return getNumber(sb);
        }

        if (sb.length() > 0)
            return sb.toString();
        return null;
    }

    public void ungetNumberOrStringWithReset() throws IOException {
        reader.reset();
    }

    public int getIntSurroundedByWhitespace() {
        int num = 0;
        boolean makeNegative = false;
        boolean readNonWhitespace = false;
        try {
            while (true) {
                int curr = reader.read();
                if (curr < 0)
                    break;
                if (Character.isWhitespace((char) curr)) {
                    if (readNonWhitespace)
                        break;
                } else if (curr == '-') {
                    makeNegative = true;
                    readNonWhitespace = true;
                } else if (curr >= '0' && curr <= '9') {
                    num *= 10;
                    num += (curr - '0');
                    readNonWhitespace = true;
                } else {
                    // break as we've hit a none digit and should bail
                    break;
                }
            }
        } catch (IOException e) {
            logger.log(Level.FINE, "Error detecting int.", e);
        }
        if (makeNegative)
            num = num * -1;
        return num;
    }

    public Number getNumber(StringBuilder value) {
        int digit = 0;
        float decimal = 0;
        float divisor = 10;
        boolean isDigit;
        boolean isDecimal = false;
        byte[] streamBytes = value.toString().getBytes();
        int startTokenPos = 0;
        boolean singed = streamBytes[startTokenPos] == '-';
        boolean positive = streamBytes[startTokenPos] == '+';
        startTokenPos = singed || positive ? startTokenPos + 1 : startTokenPos;
        // check for  double sign, thanks oracle forms!
        if (singed && streamBytes[startTokenPos] == '-') {
            startTokenPos++;
        }
        int current;
        for (int i = startTokenPos, max = streamBytes.length; i < max; i++) {
            current = streamBytes[i] - 48;
            isDigit = streamBytes[i] >= 48 && streamBytes[i] <= 57;
            if (!isDecimal && isDigit) {
                digit = (digit * 10) + current;
            } else if (isDecimal && isDigit) {
                decimal += (current / divisor);
                divisor *= 10;
            } else if (streamBytes[i] == 46) {
                isDecimal = true;
            } else {
                // anything else we can assume malformed and should break.
                break;
            }
        }
        if (singed) {
            if (isDecimal) {
                return -(digit + decimal);
            } else {
                return -digit;
            }
        } else {
            if (isDecimal) {
                return digit + decimal;
            } else {
                return digit;
            }
        }
    }

    public long getLongSurroundedByWhitespace() {
        long num = 0L;
        boolean makeNegative = false;
        boolean readNonWhitespace = false;
        try {
            while (true) {
                int curr = reader.read();
                if (curr < 0)
                    break;
                if (Character.isWhitespace((char) curr)) {
                    if (readNonWhitespace)
                        break;
                } else if (curr == '-') {
                    makeNegative = true;
                    readNonWhitespace = true;
                } else if (curr >= '0' && curr <= '9') {
                    num *= 10L;
                    num += ((long) (curr - '0'));
                    readNonWhitespace = true;
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            logger.log(Level.FINER, "Error detecting long.", e);
        }
        if (makeNegative)
            num = num * -1L;
        return num;
    }

    public int getLinearTraversalOffset() {
        return linearTraversalOffset;
    }

    public char getCharSurroundedByWhitespace() {
        char alpha = 0;
        try {
            while (true) {
                int curr = reader.read();
                if (curr < 0)
                    break;
                char c = (char) curr;
                if (!Character.isWhitespace(c)) {
                    alpha = c;
                    break;
                }
            }
        } catch (IOException e) {
            logger.log(Level.FINE, "Error detecting char.", e);
        }
        return alpha;
    }


    /**
     * White space characters defined by ' ', '\t', '\r', '\n', '\f'
     *
     * @param c true if character is white space
     */
    public static boolean isWhitespace(char c) {
        return ((c == ' ') || (c == '\t') || (c == '\r') ||
                (c == '\n') || (c == '\f') || (c == 0));
    }

    private static boolean isDelimiter(char c) {
        return ((c == '[') || (c == ']') ||
                (c == '(') || (c == ')') ||
                (c == '<') || (c == '>') ||
                (c == '{') || (c == '}') ||
                (c == '/') || (c == '%'));
    }

    private long captureStreamData(OutputStream out) throws IOException {
        long numBytes = 0;
        while (true) {
            // read bytes
            int nextByte = reader.read();
            // look to see if we have the ending tag
            if (nextByte == 'e') {
                reader.mark(10);
                if (reader.read() == 'n' &&
                        reader.read() == 'd' &&
                        reader.read() == 's' &&
                        reader.read() == 't' &&
                        reader.read() == 'r' &&
                        reader.read() == 'e' &&
                        reader.read() == 'a' &&
                        reader.read() == 'm') {
                    break;
                } else {
                    reader.reset();
                }
            } else if (nextByte < 0)
                break;
            // write the bytes
            if (out != null)
                out.write(nextByte);
            numBytes++;
        }
        return numBytes;
    }

    private long skipUntilEndstream(OutputStream out) throws IOException {
        long skipped = 0L;
        while (true) {
            reader.mark(10);
            // read bytes
            int nextByte = reader.read();
            if (nextByte == 'e' &&
                    reader.read() == 'n' &&
                    reader.read() == 'd' &&
                    reader.read() == 's' &&
                    reader.read() == 't' &&
                    reader.read() == 'r' &&
                    reader.read() == 'e' &&
                    reader.read() == 'a' &&
                    reader.read() == 'm') {
                reader.reset();
                break;
            } else if (nextByte < 0)
                break;
            else {
                if (nextByte == 0x0A || nextByte == 0x0D || nextByte == 0x20)
                    continue;
                if (out != null)
                    out.write(nextByte);
            }
            skipped++;
        }
        return skipped;
    }

    private float parseNumber(StringBuilder stringBuilder) {
        float digit = 0;
        float divisor = 10;
        boolean isDigit;
        boolean isDecimal = false;
        int startTokenPos = 0;
        int length = stringBuilder.length();
        char[] streamBytes = new char[length];
        stringBuilder.getChars(0, length, streamBytes, 0);
        boolean singed = streamBytes[startTokenPos] == '-';
        startTokenPos = singed ? startTokenPos + 1 : startTokenPos;
        int current;
        for (int i = startTokenPos; i < length; i++) {
            current = streamBytes[i] - 48;
            isDigit = streamBytes[i] >= 48 && streamBytes[i] <= 57;
            if (!isDecimal && isDigit) {
                digit = (digit * 10) + current;
            } else if (isDecimal && isDigit) {
                digit += (current / divisor);
                divisor *= 10;
            } else if (streamBytes[i] == 46) {
                isDecimal = true;
            } else {
                // anything else we can assume malformed and should break.
                break;
            }
        }
        if (singed) {
            return -digit;
        } else {
            return digit;
        }
    }
}
