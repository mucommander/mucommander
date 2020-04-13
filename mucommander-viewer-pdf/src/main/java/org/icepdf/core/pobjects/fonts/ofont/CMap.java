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
package org.icepdf.core.pobjects.fonts.ofont;

import org.icepdf.core.io.SeekableInput;
import org.icepdf.core.pobjects.Dictionary;
import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.Stream;
import org.icepdf.core.pobjects.StringObject;
import org.icepdf.core.util.Library;
import org.icepdf.core.util.Parser;
import org.icepdf.core.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The purpose of the class is to parse a CMap file.  A CMap specifies the
 * mapping from character codes to character selectors.  A CMap file defines
 * the relationship between a character code and the character description
 * <p/>
 * Character selectors are always CIDs in a CIDFont. A CMap serves a function
 * analogous to the Encoding dictionary for a simple font. The CMap does not
 * refer directly to a specific CIDFont; instead, it is combined with it as part
 * of a CIDkeyed font, represented in PDF as a Type 0 font dictionary.   Within
 * the CMap, the character mappings refer to the associated CIDFont by font
 * number, which in PDF is always 0.
 *
 * @since 1.0
 */
class CMap extends Dictionary implements org.icepdf.core.pobjects.fonts.CMap {

    private static final Logger logger =
            Logger.getLogger(CMap.class.toString());

    /**
     * Dictionary containing entries that define the character collection  for
     * the CIDFont or CIDFonts associate with the CMap.  Specifically the
     * character collections registry, ordering and supplement is defined.
     */
    private HashMap cIdSystemInfo;

    /**
     * PostScript name of the CMap.
     */
    private String cMapName;

    /**
     * defines changes to the internal organization of CMap files or the
     * semantics of CMap operators. The CMapType of CMaps described in
     * this document.
     * cMapType = 2 - indicates a ToUnicode cmap
     * cMapType = 1 - indicates a CMap object
     * cMapType = 0 - not sure yet, maybe CMap with external CMap reference
     */
    private float cMapType;

    /**
     * The name of a predefined CMap, or a stream containing a CMap, that
     * is to be used as the base for this CMap. This allows the CMap to
     * be defined differentially, specifying only the character mappings
     * that differ from the base CMap.
     */
    private Object useCMap;

    /**
     * The WMode dictionary entry controls whether the CID-keyed font writes
     * horizontally or vertically. It indicates which set of metrics will be
     * used when a base font is shown. An entry of 0 defines horizontal
     * writing from left to right; an entry of 1 defines vertical writing
     * from top to bottom.
     */
    private int wMode;

    /**
     * Defines the source character code range.  Source CMap references must
     * be in this range.
     */
    private int[][] codeSpaceRange;

    // determine if cmap is using one or two byte character maps.
    private boolean oneByte;

    /**
     * Defines mappings from character codes to Unicode characters in the
     * associated font. Expressed in UTF-16BE encoding.
     */
    private HashMap<Integer, char[]> bfChars;

    /**
     * Defines mappings from character codes to Unicode character ranges.
     * Expressed in UTF-16BE encoding.
     */
    private List<CMapRange> bfRange;

    /**
     * Define mappings of individual input character codes to CIDS in the
     * associated CIDFont.
     */
    private HashMap cIdChars;

    /**
     * Similar to cIdChars but defines ranges of input codes.
     */
    private HashMap cIdRange;

    /**
     * Define mappings if the normal mapping produces a CID for which no glyph
     * in the associated CIDFont
     */
    private HashMap notDefChars;

    /**
     * Similar to notDefChars but defines ranges of input codes.
     */
    private HashMap notDefRange;

    /**
     * Stream containing the embbeded CMap
     */
    private Stream cMapStream;
    private InputStream cMapInputStream;


    /**
     * Create a new CMap instance.  If the CMap is created from a named object
     * the dictionary property will be populated with values for the keys
     * Type, CMapName and CIDSystemInfo which are also repeated in the CMap
     * file itself. If the CMap file was created from a Font object then they
     * previously mentioned keys values must be parsed from the CMap file.
     *
     * @param library    pointer to default library containing all document objects
     * @param entries    HashMap containing all of the dictionary properties associated
     *                   with this object.  The HashMap will be empty if this object
     *                   was created via a Font objects ToUnicode key.
     * @param cMapStream stream containing CMap data.
     */
    public CMap(Library library, HashMap entries, Stream cMapStream) {
        super(library, entries);
        this.cMapStream = cMapStream;
    }

    public CMap(Library l, HashMap h, InputStream cMapInputStream) {
        super(l, h);
        this.cMapInputStream = cMapInputStream;
    }

    public boolean isOneByte() {
        return oneByte;
    }

    public boolean isTwoByte() {
        return !oneByte;
    }

    public boolean isMixedByte() {
        return false;
    }

    public boolean isEmptyMapping() {
        return false;
    }

    /**
     * Start the parsing of the CMap file.  Once completed, all necessary data
     * should be captured from the CMap file.
     * <p/>
     * Simple CMap
     * /CIDInit /ProcSet findresource
     * begin
     * 12 dict begin
     * begincmap
     * /CIDSystemInfo <<
     * /Registry (Adobe)
     * /Ordering (UCS)
     * /Supplement 0
     * >> def
     * /CMapName /Adobe-Identity-UCS def
     * /CMapType 2 def
     * 1 begincodespacerange
     * <00> <FF>
     * endcodespacerange
     * 7 beginbfchar
     * <01> <0054>
     * <02> <0065>
     * <03> <0073>
     * <04> <0074>
     * <05> <0069>
     * <06> <006E>
     * <07> <0067>
     * endbfchar
     * 2 beginbfrange
     * <0000> <005E> <0020>
     * <005F> <0061>[<00660066> <0066069> <00660066006C>]
     * endbfrange
     * endcmap
     * CMapName currentdict /CMap defineresource pop
     * end
     * end
     */
    public void init() {
        try {
            // get the byes and push them through the parser to get objects in CMap
            if (cMapInputStream == null) {
                cMapInputStream = cMapStream.getDecodedByteArrayInputStream();
            }

            // Print CMap ASCII
            if (logger.isLoggable(Level.FINER)) {
                String content;
                if (cMapInputStream instanceof SeekableInput) {
                    content = Utils.getContentFromSeekableInput((SeekableInput) cMapInputStream, false);
                } else {
                    InputStream[] inArray = new InputStream[]{cMapInputStream};
                    content = Utils.getContentAndReplaceInputStream(inArray, false);
                    cMapInputStream = inArray[0];
                }

                logger.finer("<------------------------ CMap");
                logger.finer(content);
                logger.finer("CMap ------------------------>  ");
            }

            Parser parser = new Parser(cMapInputStream);

            /**
             * Start gathering the data from the CMap objects,  the CMap file
             * is fixed in format so this routine doesn't have to be to
             * complicated
             */
            Object previousToken = null;
            while (true) {
                Object token = parser.getStreamObject();
                // break out and the end of the stream
                if (token == null) {
                    break;
                }
                // find cIdSystemInfo, not always a named attribute
                String nameString = token.toString();
                if (nameString.toLowerCase().indexOf("cidsysteminfo") >= 0) {
                    // CIDSystemInfo only has one property which should be
                    // always be hash by definition and our parser result
                    token = parser.getStreamObject();
                    if (token instanceof HashMap) {
                        cIdSystemInfo = (HashMap) token;
                        // always followed by a def token;
                        token = parser.getStreamObject();
                    }
                    // ignore any other format that isn't a hash
                }
                // find main CMap descriptors
                if (token instanceof Name) {
                    nameString = token.toString();
                    // find cMapName
                    if (nameString.toLowerCase().indexOf("cmapname") >= 0) {
                        // cmapname will always be a Name object
                        token = parser.getStreamObject();
                        cMapName = token.toString();
                        // always followed by a def token;
                        token = parser.getStreamObject();
                    }
                    // find cMapType
                    if (nameString.toLowerCase().indexOf("cmaptype") >= 0) {
                        // cmapname will always be a float
                        token = parser.getStreamObject();
                        cMapType = Float.parseFloat(token.toString());
                        // always followed by a def token;
                        token = parser.getStreamObject();
                    }
                    // find UseMap
                    if (nameString.toLowerCase().indexOf("usemap") >= 0) {
                        // nothing for now
                    }
                }
                // record the actual CMap mappings
                if (token instanceof String) {
                    String stringToken = (String) token;
                    // find codeSpaceRange
                    if (stringToken.equalsIgnoreCase("begincodespacerange")) {
                        // before begincodespacerange, the number of ranges is defined
                        int numberOfRanges = (int) Float.parseFloat(previousToken.toString());
                        // a range will always have two hex numbers
                        codeSpaceRange = new int[numberOfRanges][2];
                        for (int i = 0; i < numberOfRanges; i++) {
                            // low end of range
                            token = parser.getStreamObject();
                            StringObject hexToken = (StringObject) token;
                            int startRange = hexToken.getUnsignedInt(0, hexToken.getLength());

                            // high end of range
                            token = parser.getStreamObject();
                            hexToken = (StringObject) token;
                            int length = hexToken.getLength();
                            int endRange = hexToken.getUnsignedInt(0, length);
                            codeSpaceRange[i][0] = startRange;
                            codeSpaceRange[i][1] = endRange;
                            if (length == 2) {
                                oneByte = true;
                            }
                        }
                    }
                    // find bfChars
                    if (stringToken.equalsIgnoreCase("beginbfchar")) {
                        // before beginbfchar, the number of ranges is defined
                        int numberOfbfChar = (int) Float.parseFloat(previousToken.toString());
                        // there can be multiple char maps so we don't want to override previous values. 
                        if (bfChars == null) {
                            bfChars = new HashMap<Integer, char[]>(numberOfbfChar);
                        }
                        // a range will always have two hex numbers
                        for (int i = 0; i < numberOfbfChar; i++) {
                            // cid value
                            token = parser.getStreamObject();
                            StringObject hexToken = (StringObject) token;
                            Integer key = hexToken.getUnsignedInt(0, hexToken.getLength());

                            // cid mapping value
                            token = parser.getStreamObject();
                            hexToken = (StringObject) token;
                            char[] value = null;
                            try {
                                value = convertToString(hexToken.getLiteralStringBuffer());
                            } catch (NumberFormatException e) {
                                logger.log(Level.FINE, "CMAP: ", e);
                            }
                            bfChars.put(key, value);
                        }
                    }
                    // find bfRange
                    if (stringToken.equalsIgnoreCase("beginbfrange")) {
                        int numberOfbfRanges = (int) Float.parseFloat(previousToken.toString());
                        if (bfRange == null) {
                            bfRange = new ArrayList<CMapRange>(numberOfbfRanges);
                        }
                        StringObject hexToken;
                        Integer startRange;
                        Integer endRange;
                        // work through each range
                        for (int i = 0; i < numberOfbfRanges; i++) {
                            // look for start range.
                            token = parser.getStreamObject();
                            if (token instanceof StringObject) {
                                hexToken = (StringObject) token;
                                startRange = hexToken.getUnsignedInt(0, hexToken.getLength());
                            } else {
                                // likely a malformed cmap
                                break;
                            }
                            // end range
                            token = parser.getStreamObject();
                            if (token instanceof StringObject) {
                                hexToken = (StringObject) token;
                                endRange = hexToken.getUnsignedInt(0, hexToken.getLength());
                            } else {
                                // likely a malformed cmap
                                break;
                            }

                            // the next token will be vector or another Integer
                            token = parser.getStreamObject();
                            if (token instanceof List) {
                                bfRange.add(new CMapRange(startRange,
                                        endRange,
                                        (List) token));
                            } else {
                                hexToken = (StringObject) token;
                                Integer offset = hexToken.getUnsignedInt(0, hexToken.getLength());
                                bfRange.add(new CMapRange(startRange,
                                        endRange,
                                        offset));
                            }
                        }
                    }

                    /**
                     * CID mappings still need to be implemented but I have
                     * no examples of yet to check.  The CID mappings are little
                     * bit different then the bf ranges.
                     */

                    // find cIdChars
                    if (stringToken.equalsIgnoreCase("begincidchar")) {

                    }
                    // find cIdRange
                    if (stringToken.equalsIgnoreCase("begincidrange")) {

                    }
                    // find notDefChars
                    if (stringToken.equalsIgnoreCase("beginnotdefchar")) {

                    }
                    // find notDefRange
                    if (stringToken.equalsIgnoreCase("beginnotdefrange")) {

                    }

                }
                previousToken = token;
            }
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.SEVERE, "CMap parsing error", e);
        } catch (IOException e) {
            // eat it, end of file stream
        } finally {
            if (cMapInputStream != null) {
                try {
                    cMapInputStream.close();
                } catch (IOException e) {
                    logger.log(Level.FINE, "Error clossing cmap stream", e);
                }
            }
        }
    }

    public String toUnicode(char ch) {
        // check bfChar
        if (bfChars != null) {
            char[] tmp = bfChars.get((int) ch);
            if (tmp != null) {
                return String.valueOf(tmp);
            }
        }
        // check bfRange for matches, there may be many ranges to check
        if (bfRange != null) {
            for (CMapRange aBfRange : bfRange) {
                if (aBfRange.inRange(ch)) {
                    return String.valueOf(aBfRange.getCMapValue(ch));
                }
            }
        }
        return String.valueOf(ch);
    }

    /**
     * The method is called when ever a character code is incounter that has a
     * FontDescriptor that defines a ToUnicode CMap.  The <code>charMap</code>
     * is mapped according to the CMap rules and a mapped character code is
     * returned.
     *
     * @param charMap value to map against the ToUnicode CMap
     * @return mapped character value.
     */
    public char toSelector(char charMap) {
        // print out a mapping for a particular character
//        if (charMap == 42){
//            System.out.println("mapping " + (int)charMap + " " + bfChars);
//            System.out.println(cIdSystemInfo);
//            System.out.println(cMapType);
//        }

        // for ToUnicode we only need to look at bfChar and bfRange.
        // bfChar values have a higher precedent then bfRange.

        // check bfChar
        if (bfChars != null) {
            char[] tmp = bfChars.get((int) charMap);
            if (tmp != null) {
                return tmp[0];
            }
        }
        // check bfRange for matches, there may be many ranges to check
        if (bfRange != null) {
            for (CMapRange aBfRange : bfRange) {
                if (aBfRange.inRange(charMap)) {
                    return aBfRange.getCMapValue(charMap)[0];
                }
            }
        }
        return charMap;
    }

    public char toSelector(char charMap, boolean isCFF) {
        return toSelector(charMap);
    }

    /**
     * Help class to store data for a CMap bfrange value.  CMap bfranges come
     * in two flavours but there both share a start and end range value.
     * Characters that fall in this range are mapped with wither the offset
     * value or to an offset vector.
     * <p/>
     * Basic offset Mapping
     * <0000> <005E> <0020>  -  values that are between <0000> and <005E> are
     * offset by <0020> ie  <0001> maps to <0021>, <004f> maps to <006f> and
     * <0006F> would not be mapped by this range.
     * <p/>
     * Vector offset Mapping
     * <005F> <0061>[<00660066> <0066069> <00660066006C>] - values that are
     * between <005f> and <0067> are mapped directly to an offset index in the
     * array.  ie <005f> maps to <00660066> and <0060> maps to <0066069> and
     * finally <0061> maps to <00660066006C>.
     */
    class CMapRange {

        // start value for a bfrange
        int startRange = 0;
        // end value for a bfrange
        int endRange = 0;
        // offset mapping
        int offsetValue = 0;
        // offset vector
        List offsetVecor = null;

        /**
         * Create a new instance of a CMapRange, when it is a simple range
         * mapping with an offset value.
         *
         * @param startRange  start range of mapping
         * @param endRange    end range of mapping
         * @param offsetValue value to offset a mapping by
         */
        public CMapRange(int startRange, int endRange, int offsetValue) {
            this.startRange = startRange;
            this.endRange = endRange;
            this.offsetValue = offsetValue;
        }

        /**
         * Creat new instance of a CMapRange, when it is a more vector range
         * mapping.  Each valid number in the range maps the a corresponding
         * value in the vector based on the numbers offset from the start range.
         *
         * @param startRange  start range of mapping
         * @param endRange    end range of the mapping
         * @param offsetVecor offset mappped vector
         */
        public CMapRange(int startRange, int endRange, List offsetVecor) {
            this.startRange = startRange;
            this.endRange = endRange;
            this.offsetVecor = offsetVecor;
        }

        /**
         * Checks if a <code>value</code> is in the CMap bfrange.
         *
         * @param value value to check for containment
         * @return true if the cmap falls inside one of the bfranges, false
         *         otherwise.
         */
        public boolean inRange(int value) {
            return (value >= startRange && value <= endRange);
        }

        /**
         * Get the mapped value of <code>value</code>.  It is assumed that
         * inRange is called before this method is called.  If the
         * <code>value</code> is not in the range then a value of -1 is returned
         *
         * @param value value to find corresponding CMap for
         * @return the mapped CMap value for <code>value</code>, -1 if the
         *         <code>value</code> can not be mapped.
         */
        public char[] getCMapValue(int value) {

            // case of float offset
            if (offsetVecor == null) {
                return new char[]{(char) (offsetValue + (value - startRange))};//value + offsetValue;
            } else {// case of vector offset
                // value - startRange will give the index in the vector of the desired
                // mapping value
                StringObject hexToken = (StringObject) offsetVecor.get(value - startRange);
                char[] test = convertToString(hexToken.getLiteralStringBuffer());
                return test;
            }
        }

    }

    // convert to characters.
    private char[] convertToString(CharSequence s) {
        if (s == null || s.length() % 2 != 0) {
            throw new IllegalArgumentException();
        }
        int len = s.length();
        if (len == 1) {
            return new char[]{s.charAt(0)};
        }
        char[] dest = new char[len / 2];
        for (int i = 0, j = 0; i < len; i += 2, j++) {
            dest[j] = (char) ((s.charAt(i) << 8) | s.charAt(i + 1));
        }
        return dest;
    }
}
