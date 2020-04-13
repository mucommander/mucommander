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

import java.io.*;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class parses and stores data on the 14 PostScript AFM files.
 *
 * @since 1.0
 */
public class AFM {

    private static final Logger logger =
            Logger.getLogger(AFM.class.toString());

    public static final int COURIER = 0;
    public static final int COURIER_BOLD = 1;
    public static final int COURIER_OBLIQUE = 2;
    public static final int COURIER_BOLD_OBLIQUE = 3;
    public static final int HELVETICA = 4;
    public static final int HELVETICA_BOLD = 5;
    public static final int HELVETICA_OBLIQUE = 6;
    public static final int HELVETICA_BOLD_OBLIQUE = 7;
    public static final int TIMES_ROMAN = 8;
    public static final int TIMES_BOLD = 9;
    public static final int TIMES_ITALIC = 10;
    public static final int TIMES_BOLD_ITALIC = 11;
    public static final int ZAPF_DINGBATS = 12;
    public static final int SYMBOL = 13;


    public static String[] AFMnames = {
            "Courier.afm",
            "Courier-Bold.afm",
            "Courier-Oblique.afm",
            "Courier-BoldOblique.afm",

            "Helvetica.afm",
            "Helvetica-Bold.afm",
            "Helvetica-Oblique.afm",
            "Helvetica-BoldOblique.afm",

            "Times-Roman.afm",
            "Times-Bold.afm",
            "Times-Italic.afm",
            "Times-BoldItalic.afm",

            "ZapfDingbats.afm",
            "Symbol.afm"
    };
    /**
     * <p>The value of the <b>Flags</b> entry in a font descriptor is an
     * unsized 32-bit integer containg flags specifying various characteristics
     * of the font.</p>
     * <table border="1" cellpadding="1">
     * <tr>
     * <td><b>Bit Position</b></td>
     * <td><b>Name</b></td>
     * <td><b>Meaning</b></td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>FixedPitch</td>
     * <td>All glyphs have the same width (as opposed to proportional or
     * variable-pitch fonts, which have different widths).</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>Serif</td>
     * <td>Glyphs have serifs, which are short strokes drawn at an angle on
     * the top and bottom of glyph stems. ( Sans serif fonts do not have serifs.)</td>
     * </tr>
     * <tr>
     * <td>3</td>
     * <td>Symbolic</td>
     * <td>Font contains glyphs outside the Adobe standard Latin character
     * set. This flag and the Nonsymbolic flag cannot both be set or both be clear.</td>
     * </tr>
     * <tr>
     * <td>4</td>
     * <td>Script</td>
     * <td>Glyphs resemble cursive handwriting.</td>
     * </tr>
     * <tr>
     * <td>6</td>
     * <td>Nonsymbolic</td>
     * <td>Font uses the Adobe standard Latin character set or a subset of it.</td>
     * </tr>
     * <tr>
     * <td>7</td>
     * <td>Italic</td>
     * <td>Glyphs have dominant vertical strokes that are slanted.</td>
     * </tr>
     * <tr>
     * <td>17</td>
     * <td>AllCap</td>
     * <td>Font contains no lowercase letters; typically used for display
     * purposes, such as for titles or headlines.</td>
     * </tr>
     * <tr>
     * <td>18</td>
     * <td>SmallCap</td>
     * <td>Font contains both uppercase and lowercase letters. The uppercase
     * letters are similar to those in the regular version of the same
     * typeface family. The glyphs for the lowercase letters have the same
     * shapes as the corresponding uppercase letters, but they are sized
     * and their proportions adjusted so that they have the same size and
     * stroke weight as lowercase glyphs in the same typeface family.</td>
     * </tr>
     * <tr>
     * <td>19</td>
     * <td>ForceBold</td>
     * <td></td>
     * </tr>
     * </table>
     * Bit Position    name    Meaning
     */

    private static int[] AFMFlags = {
            35, //  0x100011   "Courier.afm",
            35, //  0x100011   "Courier-Bold.afm",
            99, //  0x1100011  "Courier-Oblique.afm",
            99, //  0x1100011  "Courier-BoldOblique.afm",
            //
            32, //  0x100000   "Helvetica.afm",
            32, //  0x100000   "Helvetica-Bold.afm",
            96, //  0x1100000  "Helvetica-Oblique.afm",
            96, //  0x1100000  "Helvetica-BoldOblique.afm",
            //
            34, //  0x100010   "Times-Roman.afm",
            34, //  0x100010   "Times-Bold.afm",
            98, //  0x1100010  "Times-Italic.afm",
            98, //  0x1100010  "Times-BoldItalic.afm",
            //
            4, //   0x100     "ZapfDingbats.afm",
            4  //   0x100     "Symbol.afm"
    };


    public static final HashMap<String, AFM> AFMs = new HashMap<String, AFM>(14);


    private String fontName;
    private String familyName;
    private String fullName;
    private float[] widths = new float[255];
    private int[] fontBBox = new int[4];
    private float italicAngle = 0;
    private float maxWidth = 0;
    private int avgWidth = 0;
    private int flags = 0;

    /**
     * Reader and parse all the core 14 AFM font descriptors
     */
    static {
        try {
            for (int i = 0; i < AFMnames.length; i++) {
                AFM afm = AFM.loadFont("afm/" + AFMnames[i]);
                if (afm != null) {
                    afm.setFlags(AFMFlags[i]);
                    AFMs.put(afm.fontName.toLowerCase(), afm);
                }
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error load AFM CMap files", ex);
        }
    }

    /**
     * Creates a new AFM file based on the
     *
     * @param resource name of desired resource.
     * @throws IOException if the specified resource could not be found or o
     *                     pened.
     */
    public static AFM loadFont(String resource) throws IOException {
        InputStream in = AFM.class.getResourceAsStream(resource);
        if (in != null) {
            AFM afm = new AFM();
            afm.parse(new InputStreamReader(in));
            return afm;
        } else {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Could not find AFM File: " + resource);
            }
            return null;
        }
    }

    private AFM() {
    }

    public String getFontName() {
        return fontName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public int[] getFontBBox() {
        return fontBBox;
    }

    public float getItalicAngle() {
        return italicAngle;
    }

    public float[] getWidths() {
        return widths;
    }

    public float getMaxWidth() {
        return maxWidth;
    }

    public int getAvgWidth() {
        return avgWidth;
    }

    public int getFlags() {
        return flags;
    }

    private void setFlags(int value) {
        flags = value;
    }

    /**
     * Utility class for parsing the contents of the care AFM files.
     *
     * @param i stream to read
     * @throws java.io.IOException if the reader can not find the specified
     *                             afm file.
     */
    private void parse(Reader i) throws IOException {
        BufferedReader r = new BufferedReader(i);
        String s;
        int count = 0;
        avgWidth = 0;
        maxWidth = 0;
        while ((s = r.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(s, " ;\t\n\r\f");
            String s1 = st.nextToken();
            if (s1.equalsIgnoreCase("FontName")) {
                fontName = st.nextToken();
            } else if (s1.equalsIgnoreCase("FullName")) {
                fullName = st.nextToken();
            } else if (s1.equalsIgnoreCase("FamilyName")) {
                familyName = st.nextToken();
            } else if (s1.equalsIgnoreCase("FontBBox")) {
                fontBBox[0] = new Integer(st.nextToken());
                fontBBox[1] = new Integer(st.nextToken());
                fontBBox[2] = new Integer(st.nextToken());
                fontBBox[3] = new Integer(st.nextToken());
            } else if (s1.equalsIgnoreCase("ItalicAngle")) {
                italicAngle = new Float(st.nextToken());
            }
            // font width data
            else if (s1.equalsIgnoreCase("C")) {
                int c = Integer.parseInt(st.nextToken());
                while (!st.nextToken().equals("WX")) ;
                float wx = Integer.parseInt(st.nextToken()) / 1000f;
                if (c >= 0 && c < 255) {
                    widths[count] = wx;
                    // update max
                    if (wx > maxWidth) {
                        maxWidth = wx;
                    }
                    // update average
                    avgWidth += wx;
                    count++;
                }
            }
        }
        // finalized average
        avgWidth = avgWidth / count;
    }
}