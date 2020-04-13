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

/**
 * Font utility contains a bunch of commonly used font utility methods.
 *
 * @since 3.1
 */
public class FontUtil {

    // awt font style lookup style tokens
    private static final String AWT_STYLE_BOLD_ITAL = "boldital";
    private static final String AWT_STYLE_DEMI_ITAL = "demiital";
    private static final String AWT_STYLE_ITAL = "ital";
    private static final String AWT_STYLE_OBLI = "obli";

    // Font style names used to derive family names.
    private static final String STYLE_BOLD_ITALIC = "bolditalic";
    private static final String STYLE_DEMI_ITALIC = "demiitalic";
    private static final String STYLE_BOLD = "bold";
    private static final String STYLE_DEMI = "demi";
    private static final String STYLE_ITALIC = "italic";
    private static final String STYLE_BLACK = "black";

    /**
     * Utility method which maps know style strings to an AWT font style constants.
     * The style attribute read as follows from the java.awt.font constructor:
     * <ul>
     * the style constant for the Font The style argument is an integer bitmask
     * that may be PLAIN, or a bitwise union of BOLD and/or ITALIC
     * (for example, ITALIC or BOLD|ITALIC). If the style argument does not
     * conform to one of the expected integer bitmasks then the style is set to PLAIN.
     * </ul>
     *
     * @param name base name of font.
     * @return integer representing dffs
     */
    public static int guessAWTFontStyle(String name) {
        name = name.toLowerCase();
        int decorations = 0;
        if (name.indexOf(AWT_STYLE_BOLD_ITAL) > 0 ||
                name.indexOf(AWT_STYLE_DEMI_ITAL) > 0) {
            decorations |= java.awt.Font.BOLD | java.awt.Font.ITALIC;
        } else if (name.indexOf(STYLE_BOLD) > 0 ||
                name.indexOf(STYLE_BLACK) > 0 ||
                name.indexOf(STYLE_DEMI) > 0) {
            decorations |= java.awt.Font.BOLD;
        } else if (name.indexOf(AWT_STYLE_ITAL) > 0 ||
                name.indexOf(AWT_STYLE_OBLI) > 0) {
            decorations |= java.awt.Font.ITALIC;
        } else {
            decorations |= java.awt.Font.PLAIN;
        }
        return decorations;
    }

    /**
     * Utility method for guessing a font family name from its base name. Font
     * names are usually made up of a familyName followed by a style
     * name.  For example:
     * <p/>
     * <ul>
     * <li>Arial,BoldItalic</li>
     * <li>Times-Bold"</li>
     * <li>Arial BoldItalic</li>
     * <li>TimesNewRomansBold</li>
     * </ul>
     *
     * @param name base name of font.
     * @return guess of the base fonts name.
     */
    public static String guessFamily(String name) {
        String fam = name;
        int inx;
        // Family name usually precedes a common, ie. "Arial,BoldItalic"
        if ((inx = fam.indexOf(',')) > 0)
            fam = fam.substring(0, inx);
        // Family name usually precedes a dash, example "Times-Bold",
        if ((inx = fam.lastIndexOf('-')) > 0)
            fam = fam.substring(0, inx);
        // Family name with no dash or commas, example "TimesNewRomansBold" or
        // "CalibriBoldItalic"
        if ((inx = fam.toLowerCase().lastIndexOf(STYLE_BOLD_ITALIC)) > 0) {
            fam = fam.substring(0, inx);
        } else if ((inx = fam.toLowerCase().lastIndexOf(STYLE_DEMI_ITALIC)) > 0) {
            fam = fam.substring(0, inx);
        } else if ((inx = fam.toLowerCase().lastIndexOf(STYLE_BOLD)) > 0) {
            fam = fam.substring(0, inx);
        } else if ((inx = fam.toLowerCase().lastIndexOf(STYLE_ITALIC)) > 0) {
            fam = fam.substring(0, inx);
        } else if ((inx = fam.toLowerCase().lastIndexOf(STYLE_BLACK)) > 0) {
            fam = fam.substring(0, inx);
        }
        return fam;
    }

    /**
     * For a font subset, the PostScript name of the font—the value of the font’s
     * BaseFont entry and the font descriptor’s FontName entry shall begin with
     * a tag followed by a plus sign (+). The tag shall consist of exactly six
     * uppercase letters; the choice of letters is arbitrary, but different
     * subsets in the same PDF file shall have different tags
     * <p/>
     * This method will strip the font subset from the font name and return
     * the font name.
     *
     * @param name font name to strip of subset name.
     * @return bare font name.
     */
    public static String removeBaseFontSubset(String name) {
        if (name != null && name.length() > 7) {
            int i = name.indexOf('+') + 1;
            return name.substring(i, name.length());
        } else {
            return name;
        }
    }

    /**
     * Utility method for normailing strings, to lowercase and remove any spaces.
     *
     * @param name base name of font
     * @return normalized copy of string.
     */
    public static String normalizeString(String name) {
        name = guessFamily(name);
        StringBuilder normalized = new StringBuilder(name.toLowerCase());
        for (int k = normalized.length() - 1; k >= 0; k--) {
            if (normalized.charAt(k) == 32) {
                normalized.deleteCharAt(k);
                k--;
            }
        }
        return normalized.toString();
    }

}
