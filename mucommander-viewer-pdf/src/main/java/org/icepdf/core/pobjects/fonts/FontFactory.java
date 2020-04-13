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

import org.icepdf.core.pobjects.Stream;
import org.icepdf.core.pobjects.fonts.ofont.OFont;
import org.icepdf.core.util.Defs;
import org.icepdf.core.util.Library;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple Factory for loading of font library if present.
 */
public class FontFactory {

    private static final Logger logger =
            Logger.getLogger(FontFactory.class.toString());

    // allow scaling of large images to improve clarity on screen
    private static boolean awtFontLoading;

    // dynamic property to switch between font engine and awt font substitution. 
    private static boolean awtFontSubstitution;

    static {
        // turn on font file loading using awt, can cause the jvm to crash
        // if the font file is corrupt.
        awtFontLoading =
                Defs.sysPropertyBoolean("org.icepdf.core.awtFontLoading",
                        false);

    }

    public static final int FONT_OPEN_TYPE = 5;
    public static final int FONT_TRUE_TYPE = java.awt.Font.TRUETYPE_FONT;
    public static final int FONT_TYPE_0 = 6;
    public static final int FONT_TYPE_1 = java.awt.Font.TYPE1_FONT;
    public static final int FONT_TYPE_3 = 7;

    // Singleton instance of class
    private static FontFactory fontFactory;

    // NFont class path
    private static final String FONT_CLASS =
            "org.icepdf.core.pobjects.fonts.nfont.Font";
    private static final String NFONT_CLASS =
            "org.icepdf.core.pobjects.fonts.nfont.NFont";
    private static final String NFONT_OPEN_TYPE =
            "org.icepdf.core.pobjects.fonts.nfont.NFontOpenType";
    private static final String NFONT_TRUE_TYPE =
            "org.icepdf.core.pobjects.fonts.nfont.NFontTrueType";
    private static final String NFONT_TRUE_TYPE_0 =
            "org.icepdf.core.pobjects.fonts.nfont.NFontType0";
    private static final String NFONT_TRUE_TYPE_1 =
            "org.icepdf.core.pobjects.fonts.nfont.NFontType1";
    private static final String NFONT_TRUE_TYPE_3 =
            "org.icepdf.core.pobjects.fonts.nfont.NFontType3";
    static {
        // check class bath for NFont library, and declare results.
        try {
            Class.forName(NFONT_CLASS);
        } catch (ClassNotFoundException e) {
            logger.log(Level.FINE, "NFont font library was not found on the class path");
        }
    }

    private static boolean foundNFont;


    /**
     * <p>Returns a static instance of the FontManager class.</p>
     *
     * @return instance of the FontManager.
     */
    public static FontFactory getInstance() {
        // make sure we have initialized the manager
        if (fontFactory == null) {
            fontFactory = new FontFactory();
        }
        return fontFactory;
    }


    private FontFactory() {
    }

    public Font getFont(Library library, HashMap entries) {

        Font fontDictionary = null;

        if (foundFontEngine()) {
            // load each know file type reflectively.
            try {
                Class<?> fontClass = Class.forName(FONT_CLASS);
                Class[] fontArgs = {Library.class, HashMap.class};
                Constructor fontClassConstructor =
                        fontClass.getDeclaredConstructor(fontArgs);
                Object[] fontUrl = {library, entries};
                fontDictionary = (Font) fontClassConstructor.newInstance(fontUrl);
            } catch (Throwable e) {
                logger.log(Level.FINE, "Could not load font dictionary class", e);
            }
        } else {
            // create OFont implementation. 
            fontDictionary =
                    new org.icepdf.core.pobjects.fonts.ofont.Font(library, entries);
        }
        return fontDictionary;
    }

    public FontFile createFontFile(Stream fontStream, int fontType, String fontSubType) {
        FontFile fontFile = null;
        if (foundFontEngine()) {
            try {
                Class<?> fontClass = getNFontClass(fontType);
                if (fontClass != null) {
                    // convert the stream to byte[]
                    Class[] bytArrayArg = {byte[].class, String.class};
                    Constructor fontClassConstructor =
                            fontClass.getDeclaredConstructor(bytArrayArg);
                    byte[] data = fontStream.getDecodedStreamBytes(0);
                    Object[] fontStreamBytes = {data, fontSubType};
                    if (data.length > 0) {
                        fontFile = (FontFile) fontClassConstructor
                                .newInstance(fontStreamBytes);
                    }
                }
            } catch (Throwable e) {
                logger.log(Level.FINE, "Could not create instance of font file " + fontType);
                if (fontType == FONT_TRUE_TYPE) {
                    // we might have a very rare corner case where the file2 definition is actually a Open type font
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Trying to reload TrueType definition as OpenType.");
                    }
                    try {
                        // force a OpentType font load.
                        Class<?> fontClass = getNFontClass(FONT_OPEN_TYPE);
                        if (fontClass != null) {
                            // convert the stream to byte[]
                            Class[] bytArrayArg = {byte[].class, String.class};
                            Constructor fontClassConstructor =
                                    fontClass.getDeclaredConstructor(bytArrayArg);
                            byte[] data = fontStream.getDecodedStreamBytes(0);
                            Object[] fontStreamBytes = {data, fontSubType};
                            if (data.length > 0) {
                                fontFile = (FontFile) fontClassConstructor
                                        .newInstance(fontStreamBytes);
                            }
                        }
                    } catch (Exception ex) {
                        logger.log(Level.FINE, "Could not create instance of font file as OpenType." + fontType);
                    }
                }
            }
        } else if (awtFontLoading) {
            // see if the font file can be loaded with Java Fonts
            InputStream in = null;
            try {
                in = fontStream.getDecodedByteArrayInputStream();
                // make sure we try to load open type fonts as well, done as true type.
                if (fontType == FONT_OPEN_TYPE) fontType = FONT_TRUE_TYPE;
                java.awt.Font javaFont = java.awt.Font.createFont(fontType, in);
                if (javaFont != null) {
                    // create instance of OFont.
                    fontFile = new OFont(javaFont);
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Successfully created embedded OFont: " + fontTypeToString(fontType));
                    }
                    try {
                        in.close();
                    } catch (IOException e) {
                        logger.log(Level.FINE, "Error closing font stream.", e);
                    }
                }
            } catch (Throwable e) {
                logger.log(Level.FINE, "Error reading font file with ", e);
                try {
                    if (in != null) in.close();
                } catch (Throwable e1) {
                    logger.log(Level.FINE, "Error closing font stream.", e);
                }
            }
        }
        return fontFile;
    }

    public FontFile createFontFile(File file, int fontType, String fontSubType) {
        try {
            return createFontFile(file.toURI().toURL(), fontType, fontSubType);
        } catch (Throwable e) {
            logger.log(Level.FINE, "Could not create instance of font file " + fontType, e);
        }
        return null;
    }

    public FontFile createFontFile(URL url, int fontType, String fontSubType) {
        FontFile fontFile = null;
        if (foundFontEngine()) {
            try {
                Class<?> fontClass = getNFontClass(fontType);
                if (fontClass != null) {
                    // convert the stream to byte[]
                    Class[] urlArg = {URL.class, String.class};
                    Constructor fontClassConstructor =
                            fontClass.getDeclaredConstructor(urlArg);
                    Object[] fontUrl = {url, fontSubType};
                    fontFile = (FontFile) fontClassConstructor.newInstance(fontUrl);
                }
            } catch (Throwable e) {
                logger.log(Level.FINE, "Could not create instance of font file " + fontType, e);
            }
        } else {
            // see if the font file can be loaded with Java Fonts
            try {
                // make sure we try to load open type fonts as well, done as true type.
                if (fontType == FONT_OPEN_TYPE) fontType = FONT_TRUE_TYPE;
                java.awt.Font javaFont = java.awt.Font.createFont(fontType, url.openStream());
                if (javaFont != null) {

                    // create instance of OFont.
                    fontFile = new OFont(javaFont);

                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Successfully loaded OFont: " + url);
                    }
                }
            } catch (Throwable e) {
                logger.log(Level.FINE, "Error reading font file with ", e);
            }
        }
        return fontFile;
    }

    public boolean isAwtFontSubstitution() {
        return awtFontSubstitution;
    }

    public void setAwtFontSubstitution(boolean awtFontSubstitution) {
        FontFactory.awtFontSubstitution = awtFontSubstitution;
    }

    public void toggleAwtFontSubstitution() {
        FontFactory.awtFontSubstitution = !FontFactory.awtFontSubstitution;
    }

    private Class getNFontClass(int fontType) throws ClassNotFoundException {
        Class fontClass = null;
        if (FONT_OPEN_TYPE == fontType) {
            fontClass = Class.forName(NFONT_OPEN_TYPE);
        } else if (FONT_TRUE_TYPE == fontType) {
            fontClass = Class.forName(NFONT_TRUE_TYPE);
        } else if (FONT_TYPE_0 == fontType) {
            fontClass = Class.forName(NFONT_TRUE_TYPE_0);
        } else if (FONT_TYPE_1 == fontType) {
            fontClass = Class.forName(NFONT_TRUE_TYPE_1);
        } else if (FONT_TYPE_3 == fontType) {
            fontClass = Class.forName(NFONT_TRUE_TYPE_3);
        }
        return fontClass;
    }

    private String fontTypeToString(int fontType) {

        if (fontType == FONT_OPEN_TYPE) {
            return "Open Type Font";
        } else if (fontType == FONT_TRUE_TYPE) {
            return "True Type Font";
        } else if (fontType == FONT_TYPE_0) {
            return "Type 0 Font";
        } else if (fontType == FONT_TYPE_1) {
            return "Type 1 Font";
        } else if (fontType == FONT_TYPE_3) {
            return "Type 3 Font";
        } else {
            return "unknown font type: " + fontType;
        }
    }

    /**
     * Test if font engine is available on the class path and it has been
     * disabled with the property awtFontSubstitution.
     *
     * @return true if font engine was found, false otherwise.
     */
    public boolean foundFontEngine() {
        // check class bath for NFont library
        try {
            Class.forName(NFONT_CLASS);
            foundNFont = true;
        } catch (ClassNotFoundException e) {
            // keep quiet
        }

        return foundNFont && !awtFontSubstitution;
    }

}
