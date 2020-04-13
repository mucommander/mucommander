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

import java.awt.*;
import java.util.HashMap;

/**
 * ColorUtil is a utra effecient named color parser.  The class has a list
 * of all html named colours and their matching hex values.
 *
 * @since 2.7.1
 */
public class ColorUtil {

    private static final int[] defaultColors = {
            0xf0f8ff, // aliceblue
            0xfaebd7, // antiquewhite
            0x00ffff, // aqua
            0x7fffd4, // aquamarine
            0xf0ffff, // azure
            0xf5f5dc, // beige
            0xffe4c4, // bisque
            0x000000, // black
            0xffebcd, // blanchedalmond
            0x0000ff, // blue
            0x8a2be2, // blueviolet
            0xa52a2a, // brown
            0xdeb887, // burlywood
            0x5f9ea0, // cadetblue
            0x7fff00, // chartreuse
            0xd2691e, // chocolate
            0xff7f50, // coral
            0x6495ed, // cornflowerblue
            0xfff8dc, // cornsilk
            0xdc143c, // crimson
            0x00ffff, // cyan
            0x00008b, // darkblue
            0x008b8b, // darkcyan
            0xb8860b, // darkgoldenrod
            0xa9a9a9, // darkgray
            0x006400, // darkgreen
            0xa9a9a9, // darkgrey
            0xbdb76b, // darkkhaki
            0x8b008b, // darkmagenta
            0x556b2f, // darkolivegreen
            0xff8c00, // darkorange
            0x9932cc, // darkorchid
            0x8b0000, // darkred
            0xe9967a, // darksalmon
            0x8fbc8f, // darkseagreen
            0x483d8b, // darkslateblue
            0x2f4f4f, // darkslategray
            0x2f4f4f, // darkslategrey
            0x00ced1, // darkturquoise
            0x9400d3, // darkviolet
            0xff1493, // deeppink
            0x00bfff, // deepskyblue
            0x696969, // dimgray
            0x696969, // dimgrey
            0x1e90ff, // dodgerblue
            0xb22222, // firebrick
            0xfffaf0, // floralwhite
            0x228b22, // forestgreen
            0xff00ff, // fuchsia
            0xdcdcdc, // gainsboro
            0xf8f8ff, // ghostwhite
            0xffd700, // gold
            0xdaa520, // goldenrod
            0x808080, // gray
            0x808080, // grey
            0x008000, // green
            0xadff2f, // greenyellow
            0xf0fff0, // honeydew
            0xff69b4, // hotpink
            0xcd5c5c, // indianred
            0x4b0082, // indigo
            0xfffff0, // ivory
            0xf0e68c, // khaki
            0xe6e6fa, // lavender
            0xfff0f5, // lavenderblush
            0x7cfc00, // lawngreen
            0xfffacd, // lemonchiffon
            0xadd8e6, // lightblue
            0xf08080, // lightcoral
            0xe0ffff, // lightcyan
            0xfafad2, // lightgoldenrodyellow
            0xd3d3d3, // lightgray
            0x90ee90, // lightgreen
            0xd3d3d3, // lightgrey
            0xffb6c1, // lightpink
            0xffa07a, // lightsalmon
            0x20b2aa, // lightseagreen
            0x87cefa, // lightskyblue
            0x778899, // lightslategray
            0x778899, // lightslategrey
            0xb0c4de, // lightsteelblue
            0xffffe0, // lightyellow
            0x00ff00, // lime
            0x32cd32, // limegreen
            0xfaf0e6, // linen
            0xff00ff, // magenta
            0x800000, // maroon
            0x66cdaa, // mediumaquamarine
            0x0000cd, // mediumblue
            0xba55d3, // mediumorchid
            0x9370db, // mediumpurple
            0x3cb371, // mediumseagreen
            0x7b68ee, // mediumslateblue
            0x00fa9a, // mediumspringgreen
            0x48d1cc, // mediumturquoise
            0xc71585, // mediumvioletred
            0x191970, // midnightblue
            0xf5fffa, // mintcream
            0xffe4e1, // mistyrose
            0xffe4b5, // moccasin
            0xffdead, // navajowhite
            0x000080, // navy
            0xfdf5e6, // oldlace
            0x808000, // olive
            0x6b8e23, // olivedrab
            0xffa500, // orange
            0xff4500, // orangered
            0xda70d6, // orchid
            0xeee8aa, // palegoldenrod
            0x98fb98, // palegreen
            0xafeeee, // paleturquoise
            0xdb7093, // palevioletred
            0xffefd5, // papayawhip
            0xffdab9, // peachpuff
            0xcd853f, // peru
            0xffc0cb, // pink
            0xdda0dd, // plum
            0xb0e0e6, // powderblue
            0x800080, // purple
            0xff0000, // red
            0xbc8f8f, // rosybrown
            0x4169e1, // royalblue
            0x8b4513, // saddlebrown
            0xfa8072, // salmon
            0xf4a460, // sandybrown
            0x2e8b57, // seagreen
            0xfff5ee, // seashell
            0xa0522d, // sienna
            0xc0c0c0, // silver
            0x87ceeb, // skyblue
            0x6a5acd, // slateblue
            0x708090, // slategray
            0x708090, // slategrey
            0xfffafa, // snow
            0x00ff7f, // springgreen
            0x4682b4, // steelblue
            0xd2b48c, // tan
            0x008080, // teal
            0xd8bfd8, // thistle
            0xff6347, // tomato
            0x40e0d0, // turquoise
            0xee82ee, // violet
            0xf5deb3, // wheat
            0xffffff, // white
            0xf5f5f5, // whitesmoke
            0xffff00, // yellow
            0x9acd32, // yellowgreen
    };

    private static final HashMap colors = new HashMap();

    /**
     * Converts a named colour to hex rgb notation.  For example black is
     * converted to #000000 and white to #FFFFFF.
     *
     * @param name know colour name to be converted
     * @return name of converted string, the same name is returned if their was
     *         a conversion failure.
     */
    public static final String convertColorNameToRGB(String name) {
        int c = convertNamedColor(name.toLowerCase());
        if (c >= 0) {
            //int rgb = c.getRGB();
            char[] buf = new char[7];
            buf[0] = '#';
            for (int pos = 1, shift = 20; shift >= 0; ++pos, shift -= 4) {
                int d = 0xF & (c >> shift);
                buf[pos] = (char) ((d < 10) ? d + '0' : d + 'A' - 10);
            }
            name = new String(buf, 0, 7);
        }
        return name;
    }

    /**
     * Converts a colour to hex rgb notation.  For example black is
     * converted to #000000 and white to #FFFFFF.
     *
     * @param color know colour to be converted
     * @return name of converted string, the same name is returned if their was
     *         a conversion failure.
     */
    public static final String convertColorToRGB(Color color) {
        int c = color.getRGB();
        return String.format("#%06X", (0xFFFFFF & c));
    }

    /**
     * Converts the colour to an integer value.
     *
     * @param name colour value in either hex or string format.
     * @return valid int colour value or -1 if no colour could be resolved
     */
    public static int convertColor(String name) {
        try {
            // see if string starts with #
            if (name.startsWith("#")) {
                name = name.substring(1);
            }
            return Integer.parseInt(name, 16);
        } catch (NumberFormatException e) {
            // intentionally left empty
        }
        // otherwise try and pare the colour name.
        return convertNamedColor(name);
    }


    /**
     * Converts the named colour to an integer value.  This integer value can
     * then be used to generate a valid java.awt.Color object.
     *
     * @param name name of colour to convert.
     * @return integer >= 0 if named colour was converted successfully, -1
     *         otherwise.
     */
    public static final int convertNamedColor(String name) {
        int index = getDefaultColorIndex(name);

        if (index >= 0) {
            return defaultColors[index];
        }

        Integer ii = (Integer) colors.get(name);
        if (ii != null) {
            return ii.intValue();
        }

        return -1;

        //return (index >= 0) ? defaultColors[index] : ((Integer)colors.get(name)).intValue();
    }

    private static final int getDefaultColorIndex(String s) {
        int id;
        final int
// #string_id_map#
// Should be real index plus one due to auto switch generator limitations
                Id_aliceblue = 1,
                Id_antiquewhite = 2,
                Id_aqua = 3,
                Id_aquamarine = 4,
                Id_azure = 5,
                Id_beige = 6,
                Id_bisque = 7,
                Id_black = 8,
                Id_blanchedalmond = 9,
                Id_blue = 10,
                Id_blueviolet = 11,
                Id_brown = 12,
                Id_burlywood = 13,
                Id_cadetblue = 14,
                Id_chartreuse = 15,
                Id_chocolate = 16,
                Id_coral = 17,
                Id_cornflowerblue = 18,
                Id_cornsilk = 19,
                Id_crimson = 20,
                Id_cyan = 21,
                Id_darkblue = 22,
                Id_darkcyan = 23,
                Id_darkgoldenrod = 24,
                Id_darkgray = 25,
                Id_darkgreen = 26,
                Id_darkgrey = 27,
                Id_darkkhaki = 28,
                Id_darkmagenta = 29,
                Id_darkolivegreen = 30,
                Id_darkorange = 31,
                Id_darkorchid = 32,
                Id_darkred = 33,
                Id_darksalmon = 34,
                Id_darkseagreen = 35,
                Id_darkslateblue = 36,
                Id_darkslategray = 37,
                Id_darkslategrey = 38,
                Id_darkturquoise = 39,
                Id_darkviolet = 40,
                Id_deeppink = 41,
                Id_deepskyblue = 42,
                Id_dimgray = 43,
                Id_dimgrey = 44,
                Id_dodgerblue = 45,
                Id_firebrick = 46,
                Id_floralwhite = 47,
                Id_forestgreen = 48,
                Id_fuchsia = 49,
                Id_gainsboro = 50,
                Id_ghostwhite = 51,
                Id_gold = 52,
                Id_goldenrod = 53,
                Id_gray = 54,
                Id_grey = 55,
                Id_green = 56,
                Id_greenyellow = 57,
                Id_honeydew = 58,
                Id_hotpink = 59,
                Id_indianred = 60,
                Id_indigo = 61,
                Id_ivory = 62,
                Id_khaki = 63,
                Id_lavender = 64,
                Id_lavenderblush = 65,
                Id_lawngreen = 66,
                Id_lemonchiffon = 67,
                Id_lightblue = 68,
                Id_lightcoral = 69,
                Id_lightcyan = 70,
                Id_lightgoldenrodyellow = 71,
                Id_lightgray = 72,
                Id_lightgreen = 73,
                Id_lightgrey = 74,
                Id_lightpink = 75,
                Id_lightsalmon = 76,
                Id_lightseagreen = 77,
                Id_lightskyblue = 78,
                Id_lightslategray = 79,
                Id_lightslategrey = 80,
                Id_lightsteelblue = 81,
                Id_lightyellow = 82,
                Id_lime = 83,
                Id_limegreen = 84,
                Id_linen = 85,
                Id_magenta = 86,
                Id_maroon = 87,
                Id_mediumaquamarine = 88,
                Id_mediumblue = 89,
                Id_mediumorchid = 90,
                Id_mediumpurple = 91,
                Id_mediumseagreen = 92,
                Id_mediumslateblue = 93,
                Id_mediumspringgreen = 94,
                Id_mediumturquoise = 95,
                Id_mediumvioletred = 96,
                Id_midnightblue = 97,
                Id_mintcream = 98,
                Id_mistyrose = 99,
                Id_moccasin = 100,
                Id_navajowhite = 101,
                Id_navy = 102,
                Id_oldlace = 103,
                Id_olive = 104,
                Id_olivedrab = 105,
                Id_orange = 106,
                Id_orangered = 107,
                Id_orchid = 108,
                Id_palegoldenrod = 109,
                Id_palegreen = 110,
                Id_paleturquoise = 111,
                Id_palevioletred = 112,
                Id_papayawhip = 113,
                Id_peachpuff = 114,
                Id_peru = 115,
                Id_pink = 116,
                Id_plum = 117,
                Id_powderblue = 118,
                Id_purple = 119,
                Id_red = 120,
                Id_rosybrown = 121,
                Id_royalblue = 122,
                Id_saddlebrown = 123,
                Id_salmon = 124,
                Id_sandybrown = 125,
                Id_seagreen = 126,
                Id_seashell = 127,
                Id_sienna = 128,
                Id_silver = 129,
                Id_skyblue = 130,
                Id_slateblue = 131,
                Id_slategray = 132,
                Id_slategrey = 133,
                Id_snow = 134,
                Id_springgreen = 135,
                Id_steelblue = 136,
                Id_tan = 137,
                Id_teal = 138,
                Id_thistle = 139,
                Id_tomato = 140,
                Id_turquoise = 141,
                Id_violet = 142,
                Id_wheat = 143,
                Id_white = 144,
                Id_whitesmoke = 145,
                Id_yellow = 146,
                Id_yellowgreen = 147;

        // This is really cool as the look up is based string index commonality
        // which greatly reduces the parse time. Should really update our
        // main content parser to work this way.

// #generated# Last update: 2001-10-19 16:09:43 CEST
        L0:
        {
            id = 0;
            String X = null;
            int c;
            L:
            switch (s.length()) {
                case 3:
                    c = s.charAt(0);
                    if (c == 'r') {
                        if (s.charAt(2) == 'd' && s.charAt(1) == 'e') {
                            id = Id_red;
                            break L0;
                        }
                    } else if (c == 't') {
                        if (s.charAt(2) == 'n' && s.charAt(1) == 'a') {
                            id = Id_tan;
                            break L0;
                        }
                    }
                    break L;
                case 4:
                    switch (s.charAt(3)) {
                        case 'a':
                            X = "aqua";
                            id = Id_aqua;
                            break L;
                        case 'd':
                            X = "gold";
                            id = Id_gold;
                            break L;
                        case 'e':
                            c = s.charAt(0);
                            if (c == 'b') {
                                if (s.charAt(2) == 'u' && s.charAt(1) == 'l') {
                                    id = Id_blue;
                                    break L0;
                                }
                            } else if (c == 'l') {
                                if (s.charAt(2) == 'm' && s.charAt(1) == 'i') {
                                    id = Id_lime;
                                    break L0;
                                }
                            }
                            break L;
                        case 'k':
                            X = "pink";
                            id = Id_pink;
                            break L;
                        case 'l':
                            X = "teal";
                            id = Id_teal;
                            break L;
                        case 'm':
                            X = "plum";
                            id = Id_plum;
                            break L;
                        case 'n':
                            X = "cyan";
                            id = Id_cyan;
                            break L;
                        case 'u':
                            X = "peru";
                            id = Id_peru;
                            break L;
                        case 'w':
                            X = "snow";
                            id = Id_snow;
                            break L;
                        case 'y':
                            c = s.charAt(2);
                            if (c == 'a') {
                                if (s.charAt(0) == 'g' && s.charAt(1) == 'r') {
                                    id = Id_gray;
                                    break L0;
                                }
                            } else if (c == 'e') {
                                if (s.charAt(0) == 'g' && s.charAt(1) == 'r') {
                                    id = Id_grey;
                                    break L0;
                                }
                            } else if (c == 'v') {
                                if (s.charAt(0) == 'n' && s.charAt(1) == 'a') {
                                    id = Id_navy;
                                    break L0;
                                }
                            }
                            break L;
                    }
                    break L;
                case 5:
                    switch (s.charAt(0)) {
                        case 'a':
                            X = "azure";
                            id = Id_azure;
                            break L;
                        case 'b':
                            c = s.charAt(4);
                            if (c == 'e') {
                                X = "beige";
                                id = Id_beige;
                            } else if (c == 'k') {
                                X = "black";
                                id = Id_black;
                            } else if (c == 'n') {
                                X = "brown";
                                id = Id_brown;
                            }
                            break L;
                        case 'c':
                            X = "coral";
                            id = Id_coral;
                            break L;
                        case 'g':
                            X = "green";
                            id = Id_green;
                            break L;
                        case 'i':
                            X = "ivory";
                            id = Id_ivory;
                            break L;
                        case 'k':
                            X = "khaki";
                            id = Id_khaki;
                            break L;
                        case 'l':
                            X = "linen";
                            id = Id_linen;
                            break L;
                        case 'o':
                            X = "olive";
                            id = Id_olive;
                            break L;
                        case 'w':
                            c = s.charAt(4);
                            if (c == 'e') {
                                X = "white";
                                id = Id_white;
                            } else if (c == 't') {
                                X = "wheat";
                                id = Id_wheat;
                            }
                            break L;
                    }
                    break L;
                case 6:
                    switch (s.charAt(3)) {
                        case 'a':
                            X = "tomato";
                            id = Id_tomato;
                            break L;
                        case 'h':
                            X = "orchid";
                            id = Id_orchid;
                            break L;
                        case 'i':
                            X = "indigo";
                            id = Id_indigo;
                            break L;
                        case 'l':
                            c = s.charAt(0);
                            if (c == 'v') {
                                X = "violet";
                                id = Id_violet;
                            } else if (c == 'y') {
                                X = "yellow";
                                id = Id_yellow;
                            }
                            break L;
                        case 'm':
                            X = "salmon";
                            id = Id_salmon;
                            break L;
                        case 'n':
                            c = s.charAt(0);
                            if (c == 'o') {
                                X = "orange";
                                id = Id_orange;
                            } else if (c == 's') {
                                X = "sienna";
                                id = Id_sienna;
                            }
                            break L;
                        case 'o':
                            X = "maroon";
                            id = Id_maroon;
                            break L;
                        case 'p':
                            X = "purple";
                            id = Id_purple;
                            break L;
                        case 'q':
                            X = "bisque";
                            id = Id_bisque;
                            break L;
                        case 'v':
                            X = "silver";
                            id = Id_silver;
                            break L;
                    }
                    break L;
                case 7:
                    switch (s.charAt(3)) {
                        case 'b':
                            X = "skyblue";
                            id = Id_skyblue;
                            break L;
                        case 'e':
                            X = "magenta";
                            id = Id_magenta;
                            break L;
                        case 'g':
                            c = s.charAt(5);
                            if (c == 'a') {
                                X = "dimgray";
                                id = Id_dimgray;
                            } else if (c == 'e') {
                                X = "dimgrey";
                                id = Id_dimgrey;
                            }
                            break L;
                        case 'h':
                            X = "fuchsia";
                            id = Id_fuchsia;
                            break L;
                        case 'k':
                            X = "darkred";
                            id = Id_darkred;
                            break L;
                        case 'l':
                            X = "oldlace";
                            id = Id_oldlace;
                            break L;
                        case 'm':
                            X = "crimson";
                            id = Id_crimson;
                            break L;
                        case 'p':
                            X = "hotpink";
                            id = Id_hotpink;
                            break L;
                        case 's':
                            X = "thistle";
                            id = Id_thistle;
                            break L;
                    }
                    break L;
                case 8:
                    switch (s.charAt(4)) {
                        case 'a':
                            X = "moccasin";
                            id = Id_moccasin;
                            break L;
                        case 'b':
                            X = "darkblue";
                            id = Id_darkblue;
                            break L;
                        case 'c':
                            X = "darkcyan";
                            id = Id_darkcyan;
                            break L;
                        case 'g':
                            c = s.charAt(6);
                            if (c == 'a') {
                                X = "darkgray";
                                id = Id_darkgray;
                            } else if (c == 'e') {
                                X = "darkgrey";
                                id = Id_darkgrey;
                            }
                            break L;
                        case 'h':
                            X = "seashell";
                            id = Id_seashell;
                            break L;
                        case 'n':
                            X = "lavender";
                            id = Id_lavender;
                            break L;
                        case 'p':
                            X = "deeppink";
                            id = Id_deeppink;
                            break L;
                        case 'r':
                            X = "seagreen";
                            id = Id_seagreen;
                            break L;
                        case 's':
                            X = "cornsilk";
                            id = Id_cornsilk;
                            break L;
                        case 'y':
                            X = "honeydew";
                            id = Id_honeydew;
                            break L;
                    }
                    break L;
                case 9:
                    switch (s.charAt(0)) {
                        case 'a':
                            X = "aliceblue";
                            id = Id_aliceblue;
                            break L;
                        case 'b':
                            X = "burlywood";
                            id = Id_burlywood;
                            break L;
                        case 'c':
                            c = s.charAt(1);
                            if (c == 'a') {
                                X = "cadetblue";
                                id = Id_cadetblue;
                            } else if (c == 'h') {
                                X = "chocolate";
                                id = Id_chocolate;
                            }
                            break L;
                        case 'd':
                            c = s.charAt(8);
                            if (c == 'i') {
                                X = "darkkhaki";
                                id = Id_darkkhaki;
                            } else if (c == 'n') {
                                X = "darkgreen";
                                id = Id_darkgreen;
                            }
                            break L;
                        case 'f':
                            X = "firebrick";
                            id = Id_firebrick;
                            break L;
                        case 'g':
                            c = s.charAt(8);
                            if (c == 'd') {
                                X = "goldenrod";
                                id = Id_goldenrod;
                            } else if (c == 'o') {
                                X = "gainsboro";
                                id = Id_gainsboro;
                            }
                            break L;
                        case 'i':
                            X = "indianred";
                            id = Id_indianred;
                            break L;
                        case 'l':
                            switch (s.charAt(5)) {
                                case 'b':
                                    X = "lightblue";
                                    id = Id_lightblue;
                                    break L;
                                case 'c':
                                    X = "lightcyan";
                                    id = Id_lightcyan;
                                    break L;
                                case 'g':
                                    c = s.charAt(7);
                                    if (c == 'a') {
                                        X = "lightgray";
                                        id = Id_lightgray;
                                    } else if (c == 'e') {
                                        X = "lightgrey";
                                        id = Id_lightgrey;
                                    }
                                    break L;
                                case 'p':
                                    X = "lightpink";
                                    id = Id_lightpink;
                                    break L;
                                case 'r':
                                    c = s.charAt(1);
                                    if (c == 'a') {
                                        X = "lawngreen";
                                        id = Id_lawngreen;
                                    } else if (c == 'i') {
                                        X = "limegreen";
                                        id = Id_limegreen;
                                    }
                                    break L;
                            }
                            break L;
                        case 'm':
                            c = s.charAt(8);
                            if (c == 'e') {
                                X = "mistyrose";
                                id = Id_mistyrose;
                            } else if (c == 'm') {
                                X = "mintcream";
                                id = Id_mintcream;
                            }
                            break L;
                        case 'o':
                            c = s.charAt(8);
                            if (c == 'b') {
                                X = "olivedrab";
                                id = Id_olivedrab;
                            } else if (c == 'd') {
                                X = "orangered";
                                id = Id_orangered;
                            }
                            break L;
                        case 'p':
                            c = s.charAt(8);
                            if (c == 'f') {
                                X = "peachpuff";
                                id = Id_peachpuff;
                            } else if (c == 'n') {
                                X = "palegreen";
                                id = Id_palegreen;
                            }
                            break L;
                        case 'r':
                            c = s.charAt(8);
                            if (c == 'e') {
                                X = "royalblue";
                                id = Id_royalblue;
                            } else if (c == 'n') {
                                X = "rosybrown";
                                id = Id_rosybrown;
                            }
                            break L;
                        case 's':
                            c = s.charAt(7);
                            if (c == 'a') {
                                X = "slategray";
                                id = Id_slategray;
                            } else if (c == 'e') {
                                X = "slategrey";
                                id = Id_slategrey;
                            } else if (c == 'u') {
                                c = s.charAt(1);
                                if (c == 'l') {
                                    X = "slateblue";
                                    id = Id_slateblue;
                                } else if (c == 't') {
                                    X = "steelblue";
                                    id = Id_steelblue;
                                }
                            }
                            break L;
                        case 't':
                            X = "turquoise";
                            id = Id_turquoise;
                            break L;
                    }
                    break L;
                case 10:
                    switch (s.charAt(8)) {
                        case 'a':
                            X = "lightcoral";
                            id = Id_lightcoral;
                            break L;
                        case 'e':
                            c = s.charAt(0);
                            if (c == 'b') {
                                X = "blueviolet";
                                id = Id_blueviolet;
                            } else if (c == 'd') {
                                X = "darkviolet";
                                id = Id_darkviolet;
                            } else if (c == 'l') {
                                X = "lightgreen";
                                id = Id_lightgreen;
                            }
                            break L;
                        case 'g':
                            X = "darkorange";
                            id = Id_darkorange;
                            break L;
                        case 'i':
                            c = s.charAt(0);
                            if (c == 'd') {
                                X = "darkorchid";
                                id = Id_darkorchid;
                            } else if (c == 'p') {
                                X = "papayawhip";
                                id = Id_papayawhip;
                            }
                            break L;
                        case 'k':
                            X = "whitesmoke";
                            id = Id_whitesmoke;
                            break L;
                        case 'n':
                            X = "aquamarine";
                            id = Id_aquamarine;
                            break L;
                        case 'o':
                            X = "darksalmon";
                            id = Id_darksalmon;
                            break L;
                        case 's':
                            X = "chartreuse";
                            id = Id_chartreuse;
                            break L;
                        case 't':
                            X = "ghostwhite";
                            id = Id_ghostwhite;
                            break L;
                        case 'u':
                            c = s.charAt(0);
                            if (c == 'd') {
                                X = "dodgerblue";
                                id = Id_dodgerblue;
                            } else if (c == 'm') {
                                X = "mediumblue";
                                id = Id_mediumblue;
                            } else if (c == 'p') {
                                X = "powderblue";
                                id = Id_powderblue;
                            }
                            break L;
                        case 'w':
                            X = "sandybrown";
                            id = Id_sandybrown;
                            break L;
                    }
                    break L;
                case 11:
                    switch (s.charAt(5)) {
                        case 'a':
                            X = "darkmagenta";
                            id = Id_darkmagenta;
                            break L;
                        case 'e':
                            X = "saddlebrown";
                            id = Id_saddlebrown;
                            break L;
                        case 'g':
                            X = "springgreen";
                            id = Id_springgreen;
                            break L;
                        case 'k':
                            X = "deepskyblue";
                            id = Id_deepskyblue;
                            break L;
                        case 'l':
                            X = "floralwhite";
                            id = Id_floralwhite;
                            break L;
                        case 'o':
                            X = "navajowhite";
                            id = Id_navajowhite;
                            break L;
                        case 's':
                            X = "lightsalmon";
                            id = Id_lightsalmon;
                            break L;
                        case 't':
                            X = "forestgreen";
                            id = Id_forestgreen;
                            break L;
                        case 'w':
                            X = "yellowgreen";
                            id = Id_yellowgreen;
                            break L;
                        case 'y':
                            c = s.charAt(0);
                            if (c == 'g') {
                                X = "greenyellow";
                                id = Id_greenyellow;
                            } else if (c == 'l') {
                                X = "lightyellow";
                                id = Id_lightyellow;
                            }
                            break L;
                    }
                    break L;
                case 12:
                    switch (s.charAt(7)) {
                        case 'g':
                            X = "darkseagreen";
                            id = Id_darkseagreen;
                            break L;
                        case 'i':
                            X = "lemonchiffon";
                            id = Id_lemonchiffon;
                            break L;
                        case 'r':
                            X = "mediumorchid";
                            id = Id_mediumorchid;
                            break L;
                        case 't':
                            X = "midnightblue";
                            id = Id_midnightblue;
                            break L;
                        case 'u':
                            X = "mediumpurple";
                            id = Id_mediumpurple;
                            break L;
                        case 'w':
                            X = "antiquewhite";
                            id = Id_antiquewhite;
                            break L;
                        case 'y':
                            X = "lightskyblue";
                            id = Id_lightskyblue;
                            break L;
                    }
                    break L;
                case 13:
                    switch (s.charAt(9)) {
                        case 'b':
                            X = "darkslateblue";
                            id = Id_darkslateblue;
                            break L;
                        case 'g':
                            c = s.charAt(11);
                            if (c == 'a') {
                                X = "darkslategray";
                                id = Id_darkslategray;
                            } else if (c == 'e') {
                                X = "darkslategrey";
                                id = Id_darkslategrey;
                            }
                            break L;
                        case 'l':
                            X = "lavenderblush";
                            id = Id_lavenderblush;
                            break L;
                        case 'n':
                            c = s.charAt(0);
                            if (c == 'd') {
                                X = "darkgoldenrod";
                                id = Id_darkgoldenrod;
                            } else if (c == 'p') {
                                X = "palegoldenrod";
                                id = Id_palegoldenrod;
                            }
                            break L;
                        case 'o':
                            c = s.charAt(0);
                            if (c == 'd') {
                                X = "darkturquoise";
                                id = Id_darkturquoise;
                            } else if (c == 'p') {
                                X = "paleturquoise";
                                id = Id_paleturquoise;
                            }
                            break L;
                        case 'r':
                            X = "lightseagreen";
                            id = Id_lightseagreen;
                            break L;
                        case 't':
                            X = "palevioletred";
                            id = Id_palevioletred;
                            break L;
                    }
                    break L;
                case 14:
                    switch (s.charAt(6)) {
                        case 'e':
                            X = "blanchedalmond";
                            id = Id_blanchedalmond;
                            break L;
                        case 'i':
                            X = "darkolivegreen";
                            id = Id_darkolivegreen;
                            break L;
                        case 'l':
                            c = s.charAt(12);
                            if (c == 'a') {
                                X = "lightslategray";
                                id = Id_lightslategray;
                            } else if (c == 'e') {
                                X = "lightslategrey";
                                id = Id_lightslategrey;
                            }
                            break L;
                        case 'o':
                            X = "cornflowerblue";
                            id = Id_cornflowerblue;
                            break L;
                        case 's':
                            X = "mediumseagreen";
                            id = Id_mediumseagreen;
                            break L;
                        case 't':
                            X = "lightsteelblue";
                            id = Id_lightsteelblue;
                            break L;
                    }
                    break L;
                case 15:
                    c = s.charAt(6);
                    if (c == 's') {
                        X = "mediumslateblue";
                        id = Id_mediumslateblue;
                    } else if (c == 't') {
                        X = "mediumturquoise";
                        id = Id_mediumturquoise;
                    } else if (c == 'v') {
                        X = "mediumvioletred";
                        id = Id_mediumvioletred;
                    }
                    break L;
                case 16:
                    X = "mediumaquamarine";
                    id = Id_mediumaquamarine;
                    break L;
                case 17:
                    X = "mediumspringgreen";
                    id = Id_mediumspringgreen;
                    break L;
                case 20:
                    X = "lightgoldenrodyellow";
                    id = Id_lightgoldenrodyellow;
                    break L;
            }
            if (X != null && X != s && !X.equals(s)) id = 0;
        }
// #/generated#
// #/string_id_map#
        return id - 1;
    }
}
