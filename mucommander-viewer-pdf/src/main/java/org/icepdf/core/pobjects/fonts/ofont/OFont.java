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

import org.icepdf.core.pobjects.fonts.CMap;
import org.icepdf.core.pobjects.fonts.Encoding;
import org.icepdf.core.pobjects.fonts.FontFile;
import org.icepdf.core.pobjects.graphics.TextState;

import java.awt.*;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * OFont is an awt Font wrapper used to aid in the paint of glyphs.
 *
 * @since 3.0
 */
public class OFont implements FontFile {

    private static final Logger log =
            Logger.getLogger(OFont.class.toString());

    private Font awtFont;
    private Rectangle2D maxCharBounds =
            new Rectangle2D.Double(0.0, 0.0, 1.0, 1.0);

    // text layout map, very expensive to create, so we'll cache them.
    private HashMap<String, Point2D.Float> echarAdvanceCache;

    protected float[] widths;
    protected Map<Integer, Float> cidWidths;
    protected float missingWidth;
    protected int firstCh;
    protected float ascent;
    protected float descent;
    protected Encoding encoding;
    protected CMap toUnicode;
    protected char[] cMap;


    public OFont(Font awtFont) {
        this.awtFont = awtFont;
        maxCharBounds = new Rectangle2D.Double();
        this.echarAdvanceCache = new HashMap<String, Point2D.Float>(256);
    }

    private OFont(OFont font) {
        this.echarAdvanceCache = font.echarAdvanceCache;
        this.awtFont = font.awtFont;
        this.encoding = font.encoding;
        this.toUnicode = font.toUnicode;
        this.missingWidth = font.missingWidth;
        this.firstCh = font.firstCh;
        this.ascent = font.ascent;
        this.descent = font.descent;
        this.widths = font.widths;
        this.cidWidths = font.cidWidths;
        this.cMap = font.cMap;
        this.maxCharBounds = font.maxCharBounds;
    }

    public FontFile deriveFont(Encoding encoding, CMap toUnicode) {
        OFont font = new OFont(this);
        this.echarAdvanceCache.clear();
        font.encoding = encoding;
        font.toUnicode = toUnicode;
        return font;
    }

    public FontFile deriveFont(float[] widths, int firstCh, float missingWidth,
                               float ascent, float descent, char[] diff) {
        OFont font = new OFont(this);
        this.echarAdvanceCache.clear();
        font.missingWidth = this.missingWidth;
        font.firstCh = firstCh;
        font.ascent = ascent;
        font.descent = descent;
        font.widths = widths;
        font.cMap = diff;
        return font;
    }

    public FontFile deriveFont(Map<Integer, Float> widths, int firstCh, float missingWidth,
                               float ascent, float descent, char[] diff) {
        OFont font = new OFont(this);
        this.echarAdvanceCache.clear();
        font.missingWidth = this.missingWidth;
        font.firstCh = firstCh;
        font.ascent = ascent;
        font.descent = descent;
        font.cidWidths = widths;
        font.cMap = diff;
        return font;
    }

    public FontFile deriveFont(AffineTransform at) {
        OFont font = new OFont(this);
        // clear font metric cache if we change the font's transform
        if (!font.getTransform().equals(this.awtFont.getTransform())) {
            this.echarAdvanceCache.clear();
        }
        font.awtFont = this.awtFont.deriveFont(at);

        font.maxCharBounds = this.maxCharBounds;
        return font;
    }

    public boolean canDisplayEchar(char ech) {
        return true;
    }

    public FontFile deriveFont(float pointsize) {
        OFont font = new OFont(this);
        font.awtFont = this.awtFont.deriveFont(pointsize);
        font.maxCharBounds = this.maxCharBounds;
        return font;
    }

    public Point2D echarAdvance(final char ech) {

        // create a glyph vector for the char
        float advance;
        float advanceY;

        // check cache for existing layout
        String text = ech + "_" + awtFont.getSize();
        Point2D.Float echarAdvance = echarAdvanceCache.get(text);

        // generate metrics is needed
        if (echarAdvance == null) {

            // the glyph vector should be created using any toUnicode value if present, as this is what we
            // are drawing, the method also does a check to apply differences if toUnicode is null.
            char echGlyph = getCMapping(ech);

            FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
            GlyphVector glyphVector = awtFont.createGlyphVector(
                    frc,
                    String.valueOf(echGlyph));
            TextLayout textLayout = new TextLayout(String.valueOf(echGlyph), awtFont, frc);

            // get bounds, only need to do this once.
            maxCharBounds = awtFont.getMaxCharBounds(frc);
            ascent = textLayout.getAscent();
            descent = textLayout.getDescent();

            GlyphMetrics glyphMetrics = glyphVector.getGlyphMetrics(0);
            advance = glyphMetrics.getAdvanceX();
            advanceY = glyphMetrics.getAdvanceY();

            echarAdvanceCache.put(text,
                    new Point2D.Float(advance, advanceY));
        }
        // returned cashed value
        else {
            advance = echarAdvance.x;
            advanceY = echarAdvance.y;
        }

        // widths uses original cid's, not the converted to unicode value.
        if (widths != null && ech - firstCh >= 0 && ech - firstCh < widths.length) {
            advance = widths[ech - firstCh] * awtFont.getSize2D();
        } else if (cidWidths != null) {
            Float width = cidWidths.get((int) ech);
            if (width != null) {
                advance = cidWidths.get((int) ech) * awtFont.getSize2D();
            }
        }
        // find any widths in the font descriptor
        else if (missingWidth > 0) {
            advance = missingWidth / 1000f;
        }

        return new Point2D.Float(advance, advanceY);
    }

    /**
     * Gets the ToUnicode character value for the given character.
     *
     * @param currentChar character to find a corresponding CMap for.
     * @return a new Character based on the CMap tranformation.  If the character
     *         can not be found in the CMap the orginal value is returned.
     */
    private char getCMapping(char currentChar) {
        if (toUnicode != null) {
            return toUnicode.toSelector(currentChar);
        }
        return currentChar;
    }

    /**
     * Return the width of the given character
     *
     * @param character character to retreive width of
     * @return width of the given <code>character</code>
     */
    public char getCharDiff(char character) {
        if (cMap != null && character < cMap.length) {
            return cMap[character];
        } else {
            return character;
        }
    }

    private char findAlternateSymbol(char character) {
        // test for known symbol aliases
        for (int i = 0; i < org.icepdf.core.pobjects.fonts.ofont.Encoding.symbolAlaises.length; i++) {
            for (int j = 0; j < org.icepdf.core.pobjects.fonts.ofont.Encoding.symbolAlaises[i].length; j++) {
                if (org.icepdf.core.pobjects.fonts.ofont.Encoding.symbolAlaises[i][j] == character) {
                    //System.out.println("found char " + Encoding.symbolAlaises[i][0]);
                    return (char) org.icepdf.core.pobjects.fonts.ofont.Encoding.symbolAlaises[i][0];
                }
            }
        }
        return character;
    }

    public CMap getToUnicode() {
        return toUnicode;
    }

    public int getStyle() {
        return awtFont.getStyle();
    }

    public String getFamily() {
        return awtFont.getFamily();
    }

    public float getSize() {
        return awtFont.getSize();
    }

    public double getAscent() {
        return ascent;
    }

    public double getDescent() {
        return descent;
    }

    public Rectangle2D getMaxCharBounds() {
        return maxCharBounds;
    }

    public AffineTransform getTransform() {
        return awtFont.getTransform();
    }

    public int getRights() {
        return 0;
    }

    public String getName() {
        return awtFont.getName();
    }

    public boolean isHinted() {
        return false;
    }

    public void setIsCid() {
    }

    public int getNumGlyphs() {
        return awtFont.getNumGlyphs();
    }

    public char getSpaceEchar() {
        return 32;
    }

    public Rectangle2D getEstringBounds(String estr, int beginIndex, int limit) {
        return null;
    }

    public String getFormat() {
        return null;
    }

    public void drawEstring(Graphics2D g, String displayText, float x, float y,
                            long layout, int mode, Color strokecolor) {

        AffineTransform af = g.getTransform();
        Shape outline = getEstringOutline(displayText, x, y);

        if (TextState.MODE_FILL == mode || TextState.MODE_FILL_STROKE == mode ||
                TextState.MODE_FILL_ADD == mode || TextState.MODE_FILL_STROKE_ADD == mode) {
            g.fill(outline);
        }
        if (TextState.MODE_STROKE == mode || TextState.MODE_FILL_STROKE == mode ||
                TextState.MODE_STROKE_ADD == mode || TextState.MODE_FILL_STROKE_ADD == mode) {
            g.draw(outline);
        }
        g.setTransform(af);

    }

    public String toUnicode(String displayText) {
        // Check string for displayable Glyphs,  try and substitute any failed ones
        StringBuilder sb = new StringBuilder(displayText.length());
        for (int i = 0; i < displayText.length(); i++) {
            // Updated with displayable glyph when possible
            sb.append(toUnicode(displayText.charAt(i)));
        }
        return sb.toString();
    }

    public String toUnicode(char c1) {
        // the toUnicode map is used for font substitution and especially for CID fonts.  If toUnicode is available
        // we use it as is, if not then we can use the charDiff mapping, which takes care of font encoding
        // differences.
        char c = toUnicode == null ? getCharDiff(c1) : c1;

        // The problem here is that some CMapping only work properly if the
        // embedded font is working properly, so that's how this logic works.

        //System.out.print((int)c + " (" + (char)c + ")");
        // check for CMap ToUnicode properties, if so we return it, no point
        // jumping though the other hoops.
        if (toUnicode != null) {
            return toUnicode.toUnicode(c);
        }
        // otherwise work with a single char
        c = getCMapping(c);
        //System.out.print(" -> " + (int)c + " (" + (char)c + ")");
        //System.out.println();

        // try alternate representation of character
        if (!awtFont.canDisplay(c)) {
            c |= 0xF000;
        }
        // correct the character c if possible
//            if (!textState.font.font.canDisplay(c) && textState.font.font.canDisplay(c1)) {
//                c = c1;
//            }

        // due to different character encoding for invalid embedded fonts
        // the proper font can not always be found
        if (!awtFont.canDisplay(c)) {

            // try and find a similar symbol that can be displayed.
            c = findAlternateSymbol(c);
//                System.out.println(c + " + " + (int) c + " " +
//                                   textState.currentfont.getName() + " " +
//                                   textState.font.font );
        }

        // Debug code, show any undisplayable glyphs
        if (log.isLoggable(Level.FINER)) {
            if (!awtFont.canDisplay(c)) {
                log.finer(
                        ((int) c1) + " " + Character.toString(c1) + " " +
                                (int) c + " " + c + " " + awtFont);
                //+ " " + textState.font.font + " " + textState.font.font.getNumGlyphs());
            }
        }
        return String.valueOf(c);
    }

    public ByteEncoding getByteEncoding() {
        return ByteEncoding.ONE_BYTE;
    }

    public Shape getEstringOutline(String displayText, float x, float y) {

        displayText = toUnicode(displayText);
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
        GlyphVector glyphVector = awtFont.createGlyphVector(frc, displayText);
        glyphVector.setGlyphPosition(0, new Point2D.Float(x, y));

        // Iterate through displayText to calculate the the new advance value if
        // the displayLength is greater then one character. This in sures that
        // cid -> String will get displayed correctly.
        int displayLength = displayText.length();
        float lastx;
        if (displayLength > 1) {
            Point2D p;
            float advance = 0;
            for (int i = 0; i < displayText.length(); i++) {
                // Position of the specified glyph relative to the origin of glyphVector
                p = glyphVector.getGlyphPosition(i);
                lastx = (float) p.getX();
                // add fonts rise to the to glyph position (sup,sub scripts)
                glyphVector.setGlyphPosition(
                        i,
                        new Point2D.Double(lastx + advance, p.getY()));

                // subtract the advance because we will be getting it from the fonts width
                float adv1 = glyphVector.getGlyphMetrics(i).getAdvance();
                double adv2 = echarAdvance(displayText.charAt(i)).getX();
                advance += -adv1 + adv2 + lastx;
            }
        }

        return glyphVector.getOutline();
    }

    public URL getSource() {
        return null;
    }
}
