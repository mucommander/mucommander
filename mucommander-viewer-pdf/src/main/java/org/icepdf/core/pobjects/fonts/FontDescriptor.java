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
package org.icepdf.core.pobjects.fonts;

import org.icepdf.core.pobjects.*;
import org.icepdf.core.util.Library;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a PDF <code>FontDescriptor</code>.  A FontDescriptor object
 * holds extra information about a particular parent Font object.  In particular
 * information on font widths, flags, to unicode and embedded font program streams.
 *
 * @see org.icepdf.core.pobjects.fonts.Font
 */
public class FontDescriptor extends Dictionary {

    private static final Logger logger =
            Logger.getLogger(FontDescriptor.class.toString());

    private FontFile font;

    public static final Name TYPE = new Name("FontDescriptor");
    public static final Name FONT_NAME = new Name("FontName");
    public static final Name FONT_FAMILY = new Name("FontFamily");
    public static final Name MISSING_Stretch = new Name("FontStretch");
    public static final Name FONT_WEIGHT = new Name("FontWeight");
    public static final Name FLAGS = new Name("Flags");
    public static final Name FONT_BBOX = new Name("FontBBox");
    public static final Name ITALIC_ANGLE = new Name("ItalicAngle");
    public static final Name ASCENT = new Name("Ascent");
    public static final Name DESCENT = new Name("Descent");
    public static final Name LEADING = new Name("Leading");
    public static final Name CAP_HEIGHT = new Name("CapHeight");
    public static final Name X_HEIGHT = new Name("XHeight");
    public static final Name STEM_V = new Name("StemV");
    public static final Name STEM_H = new Name("StemH");
    public static final Name AVG_WIDTH = new Name("AvgWidth");
    public static final Name MAX_WIDTH = new Name("MaxWidth");
    public static final Name MISSING_WIDTH = new Name("MissingWidth");
    public static final Name FONT_FILE = new Name("FontFile");
    public static final Name FONT_FILE_2 = new Name("FontFile2");
    public static final Name FONT_FILE_3 = new Name("FontFile3");
    public static final Name FONT_FILE_3_TYPE_1C = new Name("Type1C");
    public static final Name FONT_FILE_3_CID_FONT_TYPE_0 = new Name("CIDFontType0");
    public static final Name FONT_FILE_3_CID_FONT_TYPE_2 = new Name("CIDFontType2");
    public static final Name FONT_FILE_3_CID_FONT_TYPE_0C = new Name("CIDFontType0C");
    public static final Name FONT_FILE_3_OPEN_TYPE = new Name("OpenType");

    /**
     * Creates a new instance of a FontDescriptor.
     *
     * @param l Libaray of all objects in PDF
     * @param h hash of parsed FontDescriptor attributes
     */
    public FontDescriptor(Library l, HashMap h) {
        super(l, h);
    }

    /**
     * Utility method for creating a FontDescriptor based on the font metrics
     * of the <code>AFM</code>
     *
     * @param library document library
     * @param afm     adobe font metrics data
     * @return new instance of a <code>FontDescriptor</code>
     */
    public static FontDescriptor createDescriptor(Library library, AFM afm) {
        HashMap<Name, Object> properties = new HashMap<Name, Object>(7);
        properties.put(FONT_NAME, afm.getFontName());
        properties.put(FONT_FAMILY, afm.getFamilyName());
        properties.put(FONT_BBOX, afm.getFontBBox());
        properties.put(ITALIC_ANGLE, afm.getItalicAngle());
        properties.put(MAX_WIDTH, afm.getMaxWidth());
        properties.put(AVG_WIDTH, afm.getAvgWidth());
        properties.put(FLAGS, afm.getFlags());
        return new FontDescriptor(library, properties);
    }

    /**
     * Returns the PostScript name of the font.
     *
     * @return PostScript name of font.
     */
    public String getFontName() {
        Object value = library.getObject(entries, FONT_NAME);
        if (value instanceof Name) {
            return ((Name) value).getName();
        } else if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    /**
     * Gets a string specifying the preferred font family name.  For example, the font
     * "Time Bold Italic" would have a font family of Times.
     *
     * @return preferred font family name.
     */
    public String getFontFamily() {
        Object value = library.getObject(entries, FONT_FAMILY);
        if (value instanceof StringObject) {
            StringObject familyName = (StringObject) value;
            return familyName.getDecryptedLiteralString(library.getSecurityManager());
        }
        return FONT_NAME.getName();
    }

    /**
     * Gets the weight (thickness) component of the fully-qualified font name or
     * font specifier.  The default value is zero.
     *
     * @return the weight of the font name.
     */
    public float getFontWeight() {
        Object value = library.getObject(entries, FONT_WEIGHT);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return 0.0f;
    }

    /**
     * Gets the width to use for character codes whose widths are not specifed in
     * the font's dictionary.   The default value is zero.
     *
     * @return width of non-specified characters.
     */
    public float getMissingWidth() {
        Object value = library.getObject(entries, MISSING_WIDTH);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return 0.0f;
    }

    /**
     * Gets the average width of glyphs in the font. The default value is zero.
     *
     * @return average width of glyphs.
     */
    public float getAverageWidth() {
        Object value = library.getObject(entries, AVG_WIDTH);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return 0.0f;
    }

    /**
     * Gets the maximum width of glyphs in the font. The default value is zero.
     *
     * @return maximum width of glyphs.
     */
    public float getMaxWidth() {
        Object value = library.getObject(entries, MAX_WIDTH);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return 0.0f;
    }

    /**
     * Gets the ascent of glyphs in the font. The default value is zero.
     *
     * @return ascent of glyphs.
     */
    public float getAscent() {
        Object value = library.getObject(entries, ASCENT);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return 0.0f;
    }

    /**
     * Gets the descent of glyphs in the font. The default value is zero.
     *
     * @return descent of glyphs.
     */
    public float getDescent() {
        Object value = library.getObject(entries, DESCENT);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return 0.0f;
    }

    /**
     * Gets the embeddedFont if any.
     *
     * @return embedded font; null, if there is no valid embedded font.
     */
    public FontFile getEmbeddedFont() {
        return font;
    }

    /**
     * Gets the fonts bounding box.
     *
     * @return bounding box in PDF coordinate space.
     */
    public PRectangle getFontBBox() {
        Object value = library.getObject(entries, FONT_BBOX);
        if (value instanceof List) {
            List rectangle = (List) value;
            return new PRectangle(rectangle);
        }
        return null;
    }

    /**
     * Gets the font flag value, which is a collection of various characteristics
     * that describe the font.
     *
     * @return int value representing the flags; bits must be looked at to get
     *         attribute values.
     */
    public int getFlags() {
        Object value = library.getObject(entries, FLAGS);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    /**
     * Initiate the Font Descriptor object. Reads embedded font programs
     * or CMap streams.
     */
    public synchronized void init() {

        if (inited) {
            return;
        }

        /**
         * FontFile1 = A stream containing a Type 1 font program
         * FontFile2 = A stream containing a TrueType font program
         * FontFile3 = A stream containing a font program other than Type 1 or
         * TrueType. The format of the font program is specified by the Subtype entry
         * in the stream dictionary
         */
        try {

            // get an instance of our font factory
            FontFactory fontFactory = FontFactory.getInstance();

            if (entries.containsKey(FONT_FILE)) {
                Stream fontStream = (Stream) library.getObject(entries, FONT_FILE);
                if (fontStream != null) {
                    font = fontFactory.createFontFile(
                            fontStream, FontFactory.FONT_TYPE_1, null);
                }
            }

            if (entries.containsKey(FONT_FILE_2)) {
                Stream fontStream = (Stream) library.getObject(entries, FONT_FILE_2);
                if (fontStream != null) {
                    font = fontFactory.createFontFile(
                            fontStream, FontFactory.FONT_TRUE_TYPE, null);
                }
            }

            if (entries.containsKey(FONT_FILE_3)) {

                Stream fontStream = (Stream) library.getObject(entries, FONT_FILE_3);
                Name subType = (Name) fontStream.getObject(SUBTYPE_KEY);
                if (subType != null &&
                        (subType.equals(FONT_FILE_3_TYPE_1C) ||
                                subType.equals(FONT_FILE_3_CID_FONT_TYPE_0) ||
                                subType.equals(FONT_FILE_3_CID_FONT_TYPE_0C))
                        ) {
                    font = fontFactory.createFontFile(
                            fontStream, FontFactory.FONT_TYPE_1, subType.getName());
                }
                if (subType != null && subType.equals(FONT_FILE_3_OPEN_TYPE)) {
//                        font = new NFontOpenType(fontStreamBytes);
                    font = fontFactory.createFontFile(
                            fontStream, FontFactory.FONT_OPEN_TYPE, subType.getName());
                }
            }
        }
        // catch everything, we can fall back to font substitution if a failure
        // occurs.
        catch (Throwable e) {
            logger.log(Level.FINE, "Error Reading Embedded Font ", e);
        }

        inited = true;
    }

    /**
     * Return a string representation of the all the FontDescriptor object's
     * parsed attributes.
     *
     * @return all of FontDescriptors parsed attributes.
     */
    public String toString() {
        String name = null;
        if (font != null)
            name = font.getName();
        return super.getPObjectReference() + " FONTDESCRIPTOR= " + entries.toString() + " - " + name;
    }
}
