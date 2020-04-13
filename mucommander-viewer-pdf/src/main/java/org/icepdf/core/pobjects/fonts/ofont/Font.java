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

import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.Reference;
import org.icepdf.core.pobjects.Stream;
import org.icepdf.core.pobjects.fonts.AFM;
import org.icepdf.core.pobjects.fonts.FontDescriptor;
import org.icepdf.core.util.FontUtil;
import org.icepdf.core.util.Library;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 */
public class Font extends org.icepdf.core.pobjects.fonts.Font {

    private static final Logger logger =
            Logger.getLogger(Font.class.toString());

    public static final Name BASE_ENCODING_KEY = new Name("BaseEncoding");
    public static final Name ENCODING_KEY = new Name("Encoding");
    public static final Name TOUNICODE_KEY = new Name("ToUnicode");
    public static final Name DIFFERENCES_KEY = new Name("Differences");
    public static final Name WIDTHS_KEY = new Name("Widths");
    public static final Name FIRST_CHAR_KEY = new Name("FirstChar");
    public static final Name W_KEY = new Name("W");
    public static final Name FONT_DESCRIPTOR_KEY = new Name("FontDescriptor");
    public static final Name DESCENDANT_FONTS_KEY = new Name("DescendantFonts");
    public static final Name NONE_KEY = new Name("none");
    public static final Name STANDARD_ENCODING_KEY = new Name("StandardEncoding");
    public static final Name MACROMAN_ENCODING_KEY = new Name("MacRomanEncoding");
    public static final Name WINANSI_ENCODING_KEY = new Name("WinAnsiEncoding");
    public static final Name PDF_DOC_ENCODING_KEY = new Name("PDFDocEncoding");

    // A specification of the font's character encoding, if different from its
    // built-in encoding. The value of Encoding may be either the name of a predefined
    // encoding (MacRomanEncoding, MacExpertEncoding, or WinAnsi- Encoding, as
    // described in Appendix D) or an encoding dictionary that specifies
    // differences from the font's built-in encoding or from a specified predefined
    // encoding
    private Encoding encoding;
    // encoding name for debugging reasons;
    private Name encodingName;

    // An array of (LastChar ? FirstChar + 1) widths, each element being the
    // glyph width for the character code that equals FirstChar plus the array index.
    // For character codes outside the range FirstChar to LastChar, the value
    // of MissingWidth from the FontDescriptor entry for this font is used.
    private List widths;

    // widths for cid fonts, substitution specific, font files actually have
    // correct glyph widths.
    private HashMap<Integer, Float> cidWidths;

    // Base character mapping of 256 chars
    private char[] cMap;

    // ToUnicode CMap object stores any mapping information
    private CMap toUnicodeCMap;

    // Base 14 AFM fonts
    protected AFM afm;

    // awt font style reference, ITALIC or BOLD|ITALIC
    protected int style;

    // get list of all available fonts.
    private static final java.awt.Font[] fonts =
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();

    // Array of type1 font differences based on family names.
    static final String type1Diff[][] =
            {{"Bookman-Demi", "URWBookmanL-DemiBold", "Arial"}, {
                    "Bookman-DemiItalic", "URWBookmanL-DemiBoldItal", "Arial"}, {
                    "Bookman-Light", "URWBookmanL-Ligh", "Arial"}, {
                    "Bookman-LightItalic", "URWBookmanL-LighItal", "Arial"}, {
                    "Courier", "Nimbus Mono L Regular", "Nimbus Mono L"}, {
                    "Courier-Oblique",
                    "Nimbus Mono L Regular Oblique",
                    "Nimbus Mono L"},
                    {
                            "Courier-Bold", "Nimbus Mono L Bold", "Nimbus Mono L"}, {
                    "Courier-BoldOblique",
                    "Nimbus Mono L Bold Oblique",
                    "Nimbus Mono L"},
                    {
                            "AvantGarde-Book", "URWGothicL-Book", "Arial"}, {
                    "AvantGarde-BookOblique", "URWGothicL-BookObli", "Arial"}, {
                    "AvantGarde-Demi", "URWGothicL-Demi", "Arial"}, {
                    "AvantGarde-DemiOblique", "URWGothicL-DemiObli", "Arial"}, {
                    "Helvetica", "Nimbus Sans L Regular", "Nimbus Sans L"}, {
                    "Helvetica-Oblique",
                    "Nimbus Sans L Regular Italic",
                    "Nimbus Sans L"},
                    {
                            "Helvetica-Bold", "Nimbus Sans L Bold", "Nimbus Sans L"}, {
                    "Helvetica-BoldOblique",
                    "Nimbus Sans L Bold Italic",
                    "Nimbus Sans L"},
                    {
                            "Helvetica-Narrow",
                            "Nimbus Sans L Regular Condensed",
                            "Nimbus Sans L"},
                    {
                            "Helvetica-Narrow-Oblique",
                            "Nimbus Sans L Regular Condensed Italic",
                            "Nimbus Sans L"},
                    {
                            "Helvetica-Narrow-Bold",
                            "Nimbus Sans L Bold Condensed",
                            "Nimbus Sans L"},
                    {
                            "Helvetica-Narrow-BoldOblique",
                            "Nimbus Sans L Bold Condensed Italic",
                            "Nimbus Sans L"},
                    {
                            "Helvetica-Condensed",
                            "Nimbus Sans L Regular Condensed",
                            "Nimbus Sans L"},
                    {
                            "Helvetica-Condensed-Oblique",
                            "Nimbus Sans L Regular Condensed Italic",
                            "Nimbus Sans L"},
                    {
                            "Helvetica-Condensed-Bold",
                            "Nimbus Sans L Bold Condensed",
                            "Nimbus Sans L"},
                    {
                            "Helvetica-Condensed-BoldOblique",
                            "Nimbus Sans L Bold Condensed Italic",
                            "Nimbus Sans L"},
                    {
                            "Palatino-Roman", "URWPalladioL-Roma", "Arial"}, {
                    "Palatino-Italic", "URWPalladioL-Ital", "Arial"}, {
                    "Palatino-Bold", "URWPalladioL-Bold", "Arial"}, {
                    "Palatino-BoldItalic", "URWPalladioL-BoldItal", "Arial"}, {
                    "NewCenturySchlbk-Roman", "CenturySchL-Roma", "Arial"}, {
                    "NewCenturySchlbk-Italic", "CenturySchL-Ital", "Arial"}, {
                    "NewCenturySchlbk-Bold", "CenturySchL-Bold", "Arial"}, {
                    "NewCenturySchlbk-BoldItalic", "CenturySchL-BoldItal", "Arial"}, {
                    "Times-Roman",
                    "Nimbus Roman No9 L Regular",
                    "Nimbus Roman No9 L"},
                    {
                            "Times-Italic",
                            "Nimbus Roman No9 L Regular Italic",
                            "Nimbus Roman No9 L"},
                    {
                            "Times-Bold",
                            "Nimbus Roman No9 L Medium",
                            "Nimbus Roman No9 L"},
                    {
                            "Times-BoldItalic",
                            "Nimbus Roman No9 L Medium Italic",
                            "Nimbus Roman No9 L"},
                    {
                            "Symbol", "Standard Symbols L", "Standard Symbols L"}, {
                    "ZapfChancery-MediumItalic", "URWChanceryL-MediItal", "Arial"}, {
                    "ZapfDingbats", "Dingbats", "Dingbats"}
            };

    public Font(Library library, HashMap entries) {
        super(library, entries);

        // initialize cMap array with base characters
        cMap = new char[256];
        for (char i = 0; i < 256; i++) {
            cMap[i] = i;
        }

        // get font style value.
        style = FontUtil.guessAWTFontStyle(basefont);

        // strip font name clean ready for processing
        basefont = cleanFontName(basefont);

        // on a null type default to Type1
        if (subtype == null) {
            subtype = new Name("Type1");
        }

        // This should help with figuring out special symbols
        if (subtype.equals("Type3")) {
            basefont = "Symbol";
            encoding = Encoding.getSymbol();

        }
        // Setup encoding for type 1 fonts
        if (subtype.equals("Type1")) {
            // symbol
            if (basefont.equals("Symbol")) {
                encoding = Encoding.getSymbol();
            }
            // ZapfDingbats
            else if (basefont.equalsIgnoreCase("ZapfDingbats") &&
                    subtype.equals("Type1")) {
                encoding = Encoding.getZapfDingBats();
            }
            // check type1Diff table against base font and assign encoding of found
            else {
                for (String[] aType1Diff : type1Diff) {
                    if (basefont.equals(aType1Diff[0])) {
                        encodingName = STANDARD_ENCODING_KEY;
                        encoding = Encoding.getStandard();
                        break;
                    }
                }
            }
        }
        // TrueType fonts with a Symbol name get WinAnsi encoding
        if (subtype.equals("TrueType")) {
            if (basefont.equals("Symbol")) {
                encodingName = WINANSI_ENCODING_KEY;
                encoding = Encoding.getWinAnsi();
            }
        }

    }

    /**
     * Initiate the Font. Retrieve any needed attributes, basically setup the
     * font so it can be used by the content parser.
     */
    public synchronized void init() {
        // flag for initiated fonts
        if (inited) {
            return;
        }

        // re-initialize the char mapping array based on the encoding of the font
        if (encoding != null) {
            for (char i = 0; i < 256; i++) {
                cMap[i] = encoding.get(i);
            }
        }

        // ToUnicode indicates that we now have CMap stream that need to be parsed
        Object objectUnicode = library.getObject(entries, TOUNICODE_KEY);
        if (objectUnicode != null && objectUnicode instanceof Stream) {
            toUnicodeCMap = new CMap(library, new HashMap(), (Stream) objectUnicode);
            toUnicodeCMap.init();
        }

        // Find any special encoding information, not used very often
        Object o = library.getObject(entries, ENCODING_KEY);
        if (o != null) {
            if (o instanceof HashMap) {
                HashMap encoding = (HashMap) o;
                setBaseEncoding(library.getName(encoding, BASE_ENCODING_KEY));
                List differences = (List) library.getObject(encoding, DIFFERENCES_KEY);
                if (differences != null) {
                    int c = 0;
                    for (Object oo : differences) {
                        if (oo instanceof Number) {
                            c = ((Number) oo).intValue();
                        } else if (oo instanceof Name) {
                            String n = oo.toString();
                            int c1 = Encoding.getUV(n);
                            if (c1 == -1) {
                                if (n.charAt(0) == 'a') {
                                    n = n.substring(1);
                                    try {
                                        c1 = Integer.parseInt(n);
                                    } catch (Exception ex) {
                                        logger.log(Level.FINE, "Error parings font differences");
                                    }
                                }
                            }
                            cMap[c] = (char) c1;
                            c++;
                        }
                    }
                }
            } else if (o instanceof Name) {
                setBaseEncoding((Name) o);
            }
        }

        // An array of (LastChar ? FirstChar + 1) widths, each element being the
        // glyph width for the character code that equals FirstChar plus the array index.
        widths = (List) (library.getObject(entries, WIDTHS_KEY));
        if (widths != null) {

            // Assigns the First character code defined in the font's Widths array
            o = library.getObject(entries, FIRST_CHAR_KEY);
            if (o != null) {
                firstchar = ((Number) o).intValue();
            }
        }
        // check of a cid font
        else if (library.getObject(entries, W_KEY) != null) {
            // calculate CID widths
            cidWidths = calculateCIDWidths();
            // first char is likely 1.
            firstchar = 0;
            // cid fonts are not afm...
            isAFMFont = false;
        }
        // afm fonts don't have widths.
        else {
            isAFMFont = false;
        }


        // Assign the font descriptor
        Object of = library.getObject(entries, FONT_DESCRIPTOR_KEY);
        if (of instanceof FontDescriptor) {
            fontDescriptor = (FontDescriptor) of;
            fontDescriptor.init();
        }

        // If there is no FontDescriptor then we most likely have a core afm
        // font and we should get the matrix so that we can derive the correct
        // font.
        if (fontDescriptor == null && basefont != null) {
            // see if the baseFont name matches one of the AFM names
            Object afm = AFM.AFMs.get(basefont.toLowerCase());
            if (afm != null && afm instanceof AFM) {
                AFM fontMetrix = (AFM) afm;
                // finally create a fontDescriptor based on AFM data.
                fontDescriptor = FontDescriptor.createDescriptor(library, fontMetrix);
                fontDescriptor.init();
            }
        }

        // assign font name for descriptor
        if (fontDescriptor != null) {
            String name = fontDescriptor.getFontName();
            if (name != null && name.length() > 0) {
                basefont = cleanFontName(name);
            }
        }

        // checking flags for set bits.
        if (fontDescriptor != null && (fontDescriptor.getFlags() & 64) != 0
                && encoding == null) {
            encodingName = STANDARD_ENCODING_KEY;
            encoding = Encoding.getStandard();
        }

        // this is a test to basic CIDFont support.  The current font class
        // is not setup to deal with this type of font,  however we can still
        // located the descendant font described by the CIDFont's data and try
        // and cMap the properties over to the type1 font
        Object descendant = library.getObject(entries, DESCENDANT_FONTS_KEY);
        if (descendant != null) {
            List tmp = (List) descendant;
            if (tmp.get(0) instanceof Reference) {
                // locate the font reference
                Object fontReference = library.getObject((Reference) tmp.get(0));
                if (fontReference instanceof Font) {
                    // create and initiate the font based on the stream data
                    Font desendant = (Font) fontReference;
                    desendant.toUnicodeCMap = this.toUnicodeCMap;
                    desendant.init();
                    this.cidWidths = desendant.cidWidths;
                    // point the DescendantFont font Descriptor to this Font object
                    // this may help improve the display of some fonts.
                    if (fontDescriptor == null) {
                        fontDescriptor = desendant.fontDescriptor;
                        String name = fontDescriptor.getFontName();
                        if (name != null) {
                            basefont = cleanFontName(name);
                        }
                    }
                }
            }
        }

        // check to see if the the font subtype is Type 1 and see if it matches
        // one of the base 14 types included with the document.
        if (subtype.equals("Type1")) {
            AFM a = AFM.AFMs.get(basefont.toLowerCase());
            if (a != null && a.getFontName() != null) {
                afm = a;
            }
        }
        // Create a new true type font based on the named basefont.
        if (subtype.equals("Type1")) {
            for (String[] aType1Diff : type1Diff) {
                if (basefont.equals(aType1Diff[0])) {
                    java.awt.Font f =
                            new java.awt.Font(
                                    aType1Diff[1],
                                    style,
                                    12);
                    if (f.getFamily().equals(aType1Diff[2])) {
                        basefont = aType1Diff[1];
                        break;
                    }
                }
            }
        }

        // font substitution found flag
        isFontSubstitution = true;
//        isAFMFont = true;

        // get most types of embedded fonts from here
        if (fontDescriptor != null && fontDescriptor.getEmbeddedFont() != null) {
            font = fontDescriptor.getEmbeddedFont();
            isFontSubstitution = false;
            isAFMFont = false;
        }

        // look at all PS font names and try and find a match
        if (font == null && basefont != null) {
            // Check to see if any of the system fonts match the basefont name
            for (java.awt.Font font1 : fonts) {

                // remove white space
                StringTokenizer st = new StringTokenizer(font1.getPSName(), " ", false);
                String fontName = "";
                while (st.hasMoreElements()) fontName += st.nextElement();

                // if a match is found assign it as the real font
                if (fontName.equalsIgnoreCase(basefont)) {
                    font = new OFont(new java.awt.Font(font1.getFamily(), style, 1));
                    basefont = font1.getPSName();
                    isFontSubstitution = true;
                    break;
                }
            }
        }

        // look at font family name matches against system fonts
        if (font == null && basefont != null) {

            // clean the base name so that is has just the font family
            String fontFamily = FontUtil.guessFamily(basefont);

            for (java.awt.Font font1 : fonts) {
                // find font family match
                if (FontUtil.normalizeString(
                        font1.getFamily()).equalsIgnoreCase(fontFamily)) {
                    // create new font with font family name and style
                    font = new OFont(new java.awt.Font(font1.getFamily(), style, 1));
                    basefont = font1.getFontName();
                    isFontSubstitution = true;
                    break;
                }
            }
        }
        // if still null, shouldn't be, assigned the basefont name
        if (font == null) {
            try {
                font = new OFont(java.awt.Font.getFont(basefont,
                        new java.awt.Font(basefont,
                                style,
                                12)));
                basefont = font.getName();
            } catch (Exception e) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.warning("Error creating awt.font for: " + entries);
                }
            }
        }
        // If the font substitutions failed then we want to try and pick the proper
        // font family based on what the font name best matches up with none
        // font family font names.  if all else fails use serif as it is the most'
        // common font.
        if (!isFontSubstitution && font != null &&
                !font.getName().toLowerCase().contains(font.getFamily().toLowerCase())) {
            // see if we working with a sans serif font
            if ((font.getName().toLowerCase().contains("times new roman") ||
                    font.getName().toLowerCase().contains("timesnewroman") ||
                    font.getName().toLowerCase().contains("bodoni") ||
                    font.getName().toLowerCase().contains("garamond") ||
                    font.getName().toLowerCase().contains("minion web") ||
                    font.getName().toLowerCase().contains("stone serif") ||
                    font.getName().toLowerCase().contains("stoneserif") ||
                    font.getName().toLowerCase().contains("georgia") ||
                    font.getName().toLowerCase().contains("bitstream cyberbit"))) {
                font = new OFont(new java.awt.Font("serif",
                        font.getStyle(), (int) font.getSize()));
                basefont = "serif";
            }
            // see if we working with a monospaced font
            else if ((font.getName().toLowerCase().contains("helvetica") ||
                    font.getName().toLowerCase().contains("arial") ||
                    font.getName().toLowerCase().contains("trebuchet") ||
                    font.getName().toLowerCase().contains("avant garde gothic") ||
                    font.getName().toLowerCase().contains("avantgardegothic") ||
                    font.getName().toLowerCase().contains("verdana") ||
                    font.getName().toLowerCase().contains("univers") ||
                    font.getName().toLowerCase().contains("futura") ||
                    font.getName().toLowerCase().contains("stone sans") ||
                    font.getName().toLowerCase().contains("stonesans") ||
                    font.getName().toLowerCase().contains("gill sans") ||
                    font.getName().toLowerCase().contains("gillsans") ||
                    font.getName().toLowerCase().contains("akzidenz") ||
                    font.getName().toLowerCase().contains("grotesk"))) {
                font = new OFont(new java.awt.Font("sansserif",
                        font.getStyle(), (int) font.getSize()));
                basefont = "sansserif";
            }
            // see if we working with a mono spaced font
            else if ((font.getName().toLowerCase().contains("courier") ||
                    font.getName().toLowerCase().contains("courier new") ||
                    font.getName().toLowerCase().contains("couriernew") ||
                    font.getName().toLowerCase().contains("prestige") ||
                    font.getName().toLowerCase().contains("eversonmono") ||
                    font.getName().toLowerCase().contains("Everson Mono"))) {
                font = new OFont(new java.awt.Font("monospaced",
                        font.getStyle(), (int) font.getSize()));
                basefont = "monospaced";
            }
            // if all else fails go with the serif as it is the most common font family
            else {
                font = new OFont(new java.awt.Font("serif",
                        font.getStyle(), (int) font.getSize()));
                basefont = "serif";
            }
        }
        // finally if we have an empty font then we default to serif so that
        // we can try and render the character codes.
        if (font == null) {
            font = new OFont(new java.awt.Font("serif", style, 12));
            basefont = "serif";
        }

        // setup encoding and widths.
        setWidth();
        font = font.deriveFont(encoding, toUnicodeCMap);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine(name + " - " + encodingName + " " + basefont + " " +
                    font.toString() + " " + isFontSubstitution);
        }

        inited = true;
    }

    /**
     * Sets the encoding of the font
     *
     * @param baseEncoding encoding name ususally MacRomanEncoding,
     *                     MacExpertEncoding, or WinAnsi- Encoding
     */
    private void setBaseEncoding(Name baseEncoding) {
        if (baseEncoding == null) {
            encodingName = NONE_KEY;
            return;
        }
        encodingName = baseEncoding;
        if (baseEncoding.equals(STANDARD_ENCODING_KEY)) {
            encoding = Encoding.getStandard();
        } else if (baseEncoding.equals(MACROMAN_ENCODING_KEY)) {
            encoding = Encoding.getMacRoman();
        } else if (baseEncoding.equals(WINANSI_ENCODING_KEY)) {
            encoding = Encoding.getWinAnsi();
        } else if (baseEncoding.equals(PDF_DOC_ENCODING_KEY)) {
            encoding = Encoding.getPDFDoc();
        }
        // initiate encoding cMap.
        if (encoding != null) {
            for (char i = 0; i < 256; i++) {
                cMap[i] = encoding.get(i);
            }
        }
    }

    /**
     * String representation of the Font object.
     *
     * @return string representing Font object attributes.
     */
    public String toString() {
        return "FONT= " + encodingName + " " + entries.toString();
    }


    /**
     * Gets the widths of the given <code>character</code> and appends it to the
     * current <code>advance</code>
     *
     * @param character character to find width of
     * @param advance   current advance of the character
     * @return width of specfied character.

    private float getWidth(int character, float advance) {
    character -= firstchar;
    if (widths != null) {
    if (character >= 0 && character < widths.size()) {
    return ((Number) widths.elementAt(character)).floatValue() / 1000f;
    }
    }
    // get any necessary afm widths
    else if (afm != null) {
    Float i = afm.getWidths()[(character)];
    if (i != null) {
    return i / 1000f;
    }
    }
    // find any widths in the font descriptor
    else if (fontDescriptor != null) {
    if (fontDescriptor.getMissingWidth() > 0)
    return fontDescriptor.getMissingWidth() / 1000f;
    }
    return advance;
    }*/

    /**
     * Utility method for setting the widths for a particular font given the
     * specified encoding.
     */
    private void setWidth() {
        float missingWidth = 0;
        float ascent = 0.0f;
        float descent = 0.0f;
        if (fontDescriptor != null) {
            if (fontDescriptor.getMissingWidth() > 0) {
                missingWidth = fontDescriptor.getMissingWidth() / 1000f;
                ascent = fontDescriptor.getAscent() / 1000f;
                descent = fontDescriptor.getDescent() / 1000f;
            }
        }
        if (widths != null) {
            float[] newWidth = new float[256 - firstchar];
            for (int i = 0, max = widths.size(); i < max; i++) {
                if (widths.get(i) != null) {
                    newWidth[i] = ((Number) widths.get(i)).floatValue() / 1000f;
                }
            }
            font = font.deriveFont(newWidth, firstchar, missingWidth, ascent, descent, cMap);
        } else if (cidWidths != null) {
            // cidWidth are already scaled correct to .001
            font = font.deriveFont(cidWidths, firstchar, missingWidth, ascent, descent, null);
        } else if (afm != null && isAFMFont) {
            font = font.deriveFont(afm.getWidths(), firstchar, missingWidth, ascent, descent, cMap);
        }

    }

    private String cleanFontName(String fontName) {

        // crystal report ecoding specific, this will have to made more
        // robust when more examples are found.
        fontName = FontUtil.removeBaseFontSubset(fontName);

        // strip commas from basefont name and replace with dashes
        if (subtype != null && (subtype.equals("Type0")
                || subtype.equals("Type1")
                || subtype.equals("MMType1")
                || subtype.equals("TrueType"))) {
            if (fontName != null) {
                // normalize so that java.awt.decode will work correctly
                fontName = fontName.replace(',', '-');
            }
        }
        return fontName;
    }

    private HashMap<Integer, Float> calculateCIDWidths() {
        HashMap<Integer, Float> cidWidths = new HashMap<Integer, Float>(75);
        // get width vector
        Object o = library.getObject(entries, W_KEY);
        if (o instanceof List) {
            List cidWidth = (List) o;
            Object current;
            Object peek;
            List subWidth;
            int currentChar;
            for (int i = 0, max = cidWidth.size() - 1; i < max; i++) {
                current = cidWidth.get(i);
                peek = cidWidth.get(i + 1);
                // found format c[w1, w2 ... wn]
                if (current instanceof Integer &&
                        peek instanceof List) {
                    // apply Unicode mapping if any
                    currentChar = (Integer) current;
                    subWidth = (List) peek;
                    for (int j = 0, subMax = subWidth.size(); j < subMax; j++) {
                        if (subWidth.get(j) instanceof Integer) {
                            cidWidths.put(currentChar + j, (Integer) subWidth.get(j) / 1000f);
                        } else if (subWidth.get(j) instanceof Float) {
                            cidWidths.put(currentChar + j, (Float) subWidth.get(j) / 1000f);
                        }
                    }
                    i++;
                }
                if (current instanceof Integer &&
                        peek instanceof Integer) {
                    for (int j = (Integer) current; j <= (Integer) peek; j++) {
                        // apply Unicode mapping if any
                        currentChar = j;
                        if (cidWidth.get(i + 2) instanceof Integer) {
                            cidWidths.put(currentChar, (Integer) cidWidth.get(i + 2) / 1000f);
                        } else if (cidWidth.get(i + 2) instanceof Float) {
                            cidWidths.put(currentChar, (Float) cidWidth.get(i + 2) / 1000f);
                        }
                    }
                    i += 2;
                }
            }
        }
        return cidWidths;
    }
}
