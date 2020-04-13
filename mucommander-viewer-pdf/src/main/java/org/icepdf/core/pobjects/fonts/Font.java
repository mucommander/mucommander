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


import org.icepdf.core.pobjects.Dictionary;
import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.Resources;
import org.icepdf.core.util.Library;

import java.util.HashMap;

/**
 * <p>This class represents a PDF object which has a subtype value equal to "Font".
 * The class does the necessary encoding and Cmap manipulation to allow the proper
 * display of text that uses this font object.</p>
 * <p/>
 * <p>This class is generally only used by the ContentParser for laying out
 * text for display and for text extraction.  There are two categories of PDF fonts:
 * Simple and Composite.<p>
 * <p/>
 * <h3>Simple Fonts</h3>
 * <p>There are several types of simple font; all of which have the following
 * properties:</p>
 * <ul>
 * <li>Glyphs in the font are selected by single-byte character codes obtained from a
 * string that is shown by the text-showing operators. Logically, these codes
 * index into a table of 256 glyphs; the mapping from codes to glyphs is called
 * the font's encoding. Each font program has a built-in encoding. Under some
 * circumstances, the encoding can be altered by means described in Section
 * 5.5.5, "Character Encoding."</li>
 * <p/>
 * <li>Each glyph has a single set of metrics, including a horizontal displacement
 * or width, as described in Section 5.1.3, "Glyph Positioning and Metrics."
 * That is, simple fonts support only horizontal writing mode.</li>
 * <p/>
 * <li>Except for Type 3 fonts and certain standard Type 1 fonts, every font
 * dictionary contains a subsidiary dictionary, the font descriptor,
 * containing fontwide metrics and other attributes of the font; see Section
 * 5.7, "Font Descriptors." Among those attributes is an optional font file
 * stream containing the font program itself.</li>
 * </ul>
 * <p/>
 * <h3>Composite Fonts</h3>
 * <p>A composite font, also called Type0 font, is one whose glyphs are obtained
 * from a font like object called a CIDFont.  A composite font is represented by
 * a font dictionary whose Subtype value is Type0.  The Type 0 font is known as
 * the root font, and its associated CID Font is called its descendant.</p>
 *
 * @since 1.0
 */
public abstract class Font extends Dictionary {

    public static final Name TYPE = new Name("Font");

    public static final Name NAME_KEY = new Name("Name");
    public static final Name BASEFONT_KEY = new Name("BaseFont");
    public static final Name ENCODING_KEY = new Name("Encoding");
    public static final Name FIRST_CHAR_KEY = new Name("FirstChar");
    public static final Name LAST_CHAR_KEY = new Name("LastChar");

    /**
     * All glyphs have the same width (as opposed to proportional or
     * variable-pitch fonts, which have different widths).
     */
    public static final int FONT_FLAG_FIXED_PITCH = 0x1;  // bit 1

    /**
     * Glyphs have serifs, which are short strokes drawn at an angle on the top
     * and bottom of glyph stems. (Sans serif fonts do not have serifs.)
     */
    public static final int FONT_FLAG_SERIF = 0x2;  // bit 2

    /**
     * Font contains glyphs outside the Adobe standard Latin character set.
     * This flag and the Nonsymbolic flag shall not both be set or both be clear.
     */
    public static final int FONT_FLAG_SYMBOLIC = 0x4; // bit 3

    /**
     * Glyphs resemble cursive handwriting.
     */
    public static final int FONT_FLAG_SCRIPT = 0x8;  // bit 4

    /**
     * Font uses the Adobe standard Latin character set or a subset of it.
     */
    public static final int FONT_FLAG_NON_SYMBOLIC = 0x20;   // bit 6

    /**
     * Glyphs have dominant vertical strokes that are slanted.
     */
    public static final int FONT_FLAG_ITALIC = 0x40;  // bit 7

    /**
     * Font contains no lowercase letters; typically used for display purposes,
     * such as for titles or headlines.
     */
    public static final int FONT_FLAG_ALL_CAP = 0x10000;   // bit 17

    /**
     * Glyphs have dominant vertical strokes that are slanted.
     */
    public static final int FONT_FLAG_SMALL_CAP = 0x20000;   // bit 18

    /**
     * Font contains no lowercase letters; typically used for display purposes,
     * such as for titles or headlines.
     */
    public static final int FONT_FLAG_FORCE_BOLD = 0x40000;   // bit 19

    // Object name always "Font"
    protected Name name;

    // The name of the object, Font
    protected String basefont;

    // The font subtype, type 0, 1, 2 etc.
    protected Name subtype;

    // the encoding name associated with font.
    protected Name encoding;

    /**
     * <p>Indicates that the font used to render this String object is in the
     * Simple Font family and thus each glyph is represented by one byte.</p>
     */
    public static final int SIMPLE_FORMAT = 1;

    /**
     * <p>Indicates that the font used to render this String object is in the
     * Composite Font family and thus each glyph is represented by at least
     * one byte.</p>
     */
    public static final int CID_FORMAT = 2;

    // supType Format, either simple or CID.
    protected int subTypeFormat = SIMPLE_FORMAT;

    // The actual Java font that will be used to display the Glyphs
    protected FontFile font;

    // The first character code defined in the font's Widths array.
    protected int firstchar = 32;
    protected int lastchar = 255;

    // Font Descriptor used
    protected FontDescriptor fontDescriptor;

    // initiated flag
    protected boolean inited;

    // AFM flag
    protected boolean isAFMFont;

    // vertical writing flag;
    protected boolean isVerticalWriting;

    // font substitution being used
    protected boolean isFontSubstitution;

    // parent resource, needed by some type3 fonts to access resources.
    protected Resources parentResource;

    /**
     * Map named CMap to Unicode mapping.
     */
    protected static final String[][] TO_UNICODE = {
            // format: <canonical> <map1> <map2> ...
            // Chinese (Simplified)
            {"GBpc-EUC-UCS2", "GBpc-EUC-H", "GBpc-EUC-V"},
            {"GBK-EUC-UCS2", "GBK-EUC-H", "GBK-EUC-V"},
            {"UniGB-UCS2-H", "GB-EUC-H", "GBT-EUC-H", "GBK2K-H", "GBKp-EUC-H"},
            {"UniGB-UCS2-V", "GB-EUC-V", "GBT-EUC-V", "GBK2K-V", "GBKp-EUC-V"},

            // Chinese (Traditional)
            {"B5pc-UCS2", "B5pc-H", "B5pc-V"},
            {"ETen-B5-UCS2", "ETen-B5-H", "ETen-B5-V", "ETenms-B5-H", "ETenms-B5-V"},
            {"UniCNS-UCS2-H", "HKscs-B5-H", "CNS-EUC-H"},
            {"UniCNS-UCS2-V", "HKscs-B5-V", "CNS-EUC-V"},

            // Japanese
            {"90pv-RKSJ-UCS2", "90pv-RKSJ-H", "83pv-RKSJ-H"},
            {"90ms-RKSJ-UCS2", "90ms-RKSJ-H", "90ms-RKSJ-V", "90msp-RKSJ-H", "90msp-RKSJ-V"},
            {"UniJIS-UCS2-H", "Ext-RKSJ-H", "H", "Add-RKSJ-H", "EUC-H"},
            {"UniJIS-UCS2-V", "Ext-RKSJ-V", "V", "Add-RKSJ-V", "EUC-V"},

            // Korean
            {"KSCms-UHC-UCS2", "KSCms-UHC-H", "KSCms-UHC-V", "KSCms-UHC-HW-H", "KSCms-UHC-HW-V"},
            {"KSCpc-EUC-UCS2", "KSCpc-EUC-H"},
            {"UniKS-UCS2-H", "KSC-EUC-H"},
            {"UniKS-UCS2-V", "KSC-EUC-V"}
    };

    // core 14 AFM names
    protected static final String[] CORE14 = {
            "Times-Roman", "Times-Bold", "Times-Italic", "Times-BoldItalic",
            "Helvetica", "Helvetica-Bold", "Helvetica-Oblique", "Helvetica-BoldOblique",
            "Courier", "Courier-Bold", "Courier-Oblique", "Courier-BoldOblique",
            "Symbol",
            "ZapfDingbats"
    };

    // type1 font names.
    protected static final String[][] TYPE1_FONT_NAME = {
            {"Times-Roman", "Times New Roman", "TimesNewRoman", "TimesNewRomanPS", "TimesNewRomanPSMT"},
            {"Times-Bold", "TimesNewRoman,Bold", "TimesNewRoman-Bold", "TimesNewRomanPS-Bold", "TimesNewRomanPS-BoldMT"},
            {"Times-Italic", "TimesNewRoman,Italic", "TimesNewRoman-Italic", "TimesNewRomanPS-Italic", "TimesNewRomanPS-ItalicMT"},
            {"Times-BoldItalic", "TimesNewRoman,BoldItalic", "TimesNewRoman-BoldItalic", "TimesNewRomanPS-BoldItalic", "TimesNewRomanPS-BoldItalicMT"},
            {"Helvetica", "Arial", "ArialMT"},
            {"Helvetica-Bold", "Helvetica,Bold", "Arial,Bold", "Arial-Bold", "Arial-BoldMT"},
            {"Helvetica-Oblique", "Helvetica,Italic", "Helvetica-Italic", "Arial,Italic", "Arial-Italic", "Arial-ItalicMT"},
            {"Helvetica-BoldOblique", "Helvetica,BoldItalic", "Helvetica-BoldItalic", "Arial,BoldItalic", "Arial-BoldItalic", "Arial-BoldItalicMT"},
            {"Courier", "CourierNew", "CourierNewPSMT"},
            {"Courier-Bold", "Courier,Bold", "CourierNew,Bold", "CourierNew-Bold", "CourierNewPS-BoldMT"},
            {"Courier-Oblique", "Courier,Italic", "CourierNew-Italic", "CourierNew,Italic", "CourierNewPS-ItalicMT"},
            {"Courier-BoldOblique", "Courier,BoldItalic", "CourierNew-BoldItalic", "CourierNew,BoldItalic", "CourierNewPS-BoldItalicMT"},
            {"Symbol"},
            {"ZapfDingbats", "Zapf-Dingbats", "Dingbats"}
    };

    /**
     * Creates a new instance of a PDF Font.
     *
     * @param library Libaray of all objects in PDF
     * @param entries hash of parsed font attributes
     */
    public Font(Library library, HashMap entries) {
        super(library, entries);

        // name of object  "Font"
        name = library.getName(entries, NAME_KEY);

        // Type of the font, type 0, 1, 2, 3 etc.
        subtype = library.getName(entries, SUBTYPE_KEY);

        encoding = library.getName(entries, ENCODING_KEY);

        // figure out type
        if (subtype != null) {
            subTypeFormat = (subtype.getName().toLowerCase().equals("type0") ||
                    subtype.getName().toLowerCase().contains("cid")) ?
                    CID_FORMAT : SIMPLE_FORMAT;
        }

        int tmpInt = library.getInt(entries, FIRST_CHAR_KEY);
        if (tmpInt != 0) {
            firstchar = tmpInt;
        }
        tmpInt = library.getInt(entries, LAST_CHAR_KEY);
        if (tmpInt != 0) {
            lastchar = tmpInt;
        }


        // font name, SanSerif is used as it has a a robust CID, and it
        // is the most commonly used font family for pdfs
        basefont = "Serif";
        Object tmp = entries.get(BASEFONT_KEY);
        if (tmp != null && tmp instanceof Name) {
            basefont = ((Name) tmp).getName();
        }
    }

    /**
     * Initiate the font. Retrieve any needed attributes, basically set up the
     * font so it can be used by the content parser.
     */
    public abstract void init();

    /**
     * Gets the base name of the core 14 fonts, null if it does not match
     *
     * @param name name of font to search for canonical name
     */
    protected String getCanonicalName(String name) {
        for (String[] aTYPE1_FONT_NAME : TYPE1_FONT_NAME) {
            for (String anATYPE1_FONT_NAME : aTYPE1_FONT_NAME) {
                if (name.startsWith(anATYPE1_FONT_NAME)) {
                    return aTYPE1_FONT_NAME[0];
                }
            }
        }
        return null;
    }

    /**
     * Gets the fonts base name.
     *
     * @return fonts base name, "Serif" if none specified.
     */
    public String getBaseFont() {
        return basefont;
    }

    /**
     * Gets the font name.
     *
     * @return string representing the font name
     */
    public Name getName() {
        return name;
    }

    /**
     * Gets the font subtype value.
     *
     * @return string representing the font subtype
     */
    public Name getSubType() {
        return subtype;
    }

    /**
     * Gets the font subtype format
     *
     * @return SIMPLE_FORMAT or CID_FORMAT.
     */
    public int getSubTypeFormat() {
        return subTypeFormat;
    }

    /**
     * Gets the font encoding name.
     *
     * @return font encoding name.
     */
    public Name getEncoding() {
        return encoding;
    }

    /**
     * <p>Returns a font which can be used to paint the glyphs in the character
     * set.</p>
     *
     * @return value of embedded font.
     */
    public FontFile getFont() {
        return font;
    }

    /**
     * <p>Returns true if the writing mode is vertical; false, otherwise</p>
     *
     * @return true if the writing mode is vertical; false, otherwise.
     */
    public boolean isVerticalWriting() {
        return isVerticalWriting;
    }

    /**
     * <p>Indicates that this font is an Adobe Core 14 font. </p>
     *
     * @return true, if font is a core 14 font; false otherwise.
     */
    public boolean isAFMFont() {
        return isAFMFont;
    }

    public boolean isFontSubstitution() {
        return isFontSubstitution;
    }

    /**
     * <p>Returns true if the font name is one of the core 14 fonts specified by
     * Adobe.</p>
     *
     * @param fontName name to test if a core 14 font.
     * @return true, if font name is a core 14 font; false, otherwise.
     */
    public boolean isCore14(String fontName) {
        for (String aCORE14 : CORE14) {
            if (fontName.startsWith(aCORE14)) {
                return true;
            }
        }
        return false;
    }

    /**
     * String representation of the Font object.
     *
     * @return string representing Font object attributes.
     */
    public String toString() {
        return getPObjectReference() + " FONT= " + basefont + " " + entries.toString();
    }

    public Resources getParentResource() {
        return parentResource;
    }

    public void setParentResource(Resources parentResource) {
        this.parentResource = parentResource;
    }
}
