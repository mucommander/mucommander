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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.Map;

/**
 * Font file interfaces.  Common methods which encapsulate NFont and OFont
 * font rendering libraries.
 *
 * @since 3.0
 */
public interface FontFile {

    /**
     * Possible encoding format of string that was designed to work with this
     * font.  Type is determined by queues in the parent Cmap definition.
     */
    public enum ByteEncoding {
        ONE_BYTE, TWO_BYTE, MIXED_BYTE
    }

    public static final long LAYOUT_NONE = 0;

    public Point2D echarAdvance(char ech);

    public FontFile deriveFont(AffineTransform at);

    public FontFile deriveFont(Encoding encoding, CMap toUnicode);

    public FontFile deriveFont(float[] widths, int firstCh, float missingWidth,
                               float ascent, float descent, char[] diff);

    public FontFile deriveFont(Map<Integer, Float> widths, int firstCh, float missingWidth,
                               float ascent, float descent, char[] diff);

    /**
     * Can the character <var>ch</var> in the nfont's encoding be rendered?
     */
    public boolean canDisplayEchar(char ech);

    public void setIsCid();

    /**
     * Creates nfont a new <var>pointsize</var>, assuming 72 ppi.
     * Note to subclassers: you must make a complete independent instance of the nfont here,
     * even if pointsize and everything else is the same, as other <code>deriveFont</code> methods use this to make a clone and might make subsequent changes.
     */
    public FontFile deriveFont(float pointsize);

    public CMap getToUnicode();

    public String toUnicode(String displayText);

    public String toUnicode(char displayChar);

    /**
     * Returns name of nfont, such as "Times".
     */
    public String getFamily();

    public float getSize();

    /**
     * Returns maximum ascent glyphs above baseline.
     */
    public double getAscent();

    /**
     * Returns maximum descent of glyphs below baseline.
     */
    public double getDescent();

    /**
     * Returns left in rectangle's x, ascent in y, width in width, height in height.
     */
    public Rectangle2D getMaxCharBounds();

    /**
     * Returns a copy of the transform associated with this font file.
     */
    public AffineTransform getTransform();

    /**
     * Returns nfont usage rights bit mask.
     */
    public int getRights();

    /**
     * Returns name of nfont, such as "Times-Roman", which is different than the filename.
     */
    public String getName();

    /**
     * Returns <code>true</code> iff nfont has hinted outlines, which is Type 1 and TrueType is a sign of higher quality.
     */
    public boolean isHinted();

    /**
     * Returns number of glyphs defined in nfont.
     */
    public int getNumGlyphs();

    public int getStyle();

    /**
     * Returns the character that seems to be used as a space in the current encoding, or NOTDEF_CHAR if no such character.
     */
    public char getSpaceEchar();

    public Rectangle2D getEstringBounds(String estr, int beginIndex, int limit);

    /**
     * Returns primary format, such as "Type1" or "OpenType".
     */
    public String getFormat();

    public abstract void drawEstring(Graphics2D g, String estr, float x,
                                     float y, long layout, int mode,
                                     Color strokecolor);

    /**
     * Get the glyph outline shape for the given estr translated to x,y.
     *
     * @param estr text to calculate glyph outline shape
     * @param x    x coordinate to translate outline shape.
     * @param y    y coordinate to translate outline shape.
     * @return glyph outline of the estr.
     */
    public Shape getEstringOutline(String estr, float x, float y);

    public ByteEncoding getByteEncoding();

    /**
     * Gets the source url of the underlying file if any.  Embedded fonts will
     * not have a source.
     *
     * @return null if the font is embedded, otherwise the font system path.
     */
    public URL getSource();
}
