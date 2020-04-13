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
package org.icepdf.core.pobjects.graphics;

import org.icepdf.core.pobjects.fonts.FontFile;
import org.icepdf.core.pobjects.graphics.text.GlyphText;
import org.icepdf.core.util.Defs;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * <p>This class represents text which will be rendered to the a graphics context.
 * This class was created to act as a wrapper for painting text using the Phelphs
 * font library as well as painting using java.awt.Font.</p>
 * <p/>
 * <p>Objects of this type are created by the content parser when "TJ" or "Tj"
 * operands are encountered in a page's content stream.  Each TextSprite object
 * is comprised of a list of CharSprites which can be painted by the Shapes
 * class at render time.</p>
 *
 * @since 2.0
 */
public class TextSprite {

    // ability to turn off optimized drawing for text.
    private static boolean optimizedDrawingEnabled =
            Defs.booleanProperty("org.icepdf.core.text.optimized", true);

    private final static boolean OPTIMIZED_DRAWING_TYPE_3_ENABLED =
            Defs.booleanProperty("org.icepdf.core.text.optimized.type3", true);

    // child GlyphText objects
    private ArrayList<GlyphText> glyphTexts;

    // text bounds, including all child Glyph sprites, in glyph space
    // this bound is used during painting to respect painting clip.
    Rectangle2D.Float bounds;

    // space reference for where glyph
    private AffineTransform graphicStateTransform;
    private AffineTransform tmTransform;

    // stroke color
    private Color strokeColor;
    // the write
    private int rmode;
    // Font used to paint text
    private FontFile font;
    // font's resource name and size, used by PS writer.
    private String fontName;
    private int fontSize;

    private static final String TYPE_3 = "Type3";

    /**
     * <p>Creates a new TextSprite object.</p>
     *
     * @param font font used when painting glyphs.
     * @param contentLength length of text content.
     */
    public TextSprite(FontFile font, int contentLength, AffineTransform graphicStateTransform, AffineTransform tmTransform) {
        glyphTexts = new ArrayList<GlyphText>(contentLength);
        // all glyphs in text share this ctm
        this.graphicStateTransform = graphicStateTransform;
        this.tmTransform = tmTransform;
        this.font = font;
        if (optimizedDrawingEnabled && !OPTIMIZED_DRAWING_TYPE_3_ENABLED) {
            optimizedDrawingEnabled = !(font.getFormat() != null && font.getFormat().equals(TYPE_3));
        }
        bounds = new Rectangle2D.Float();
    }

    /**
     * <p>Adds a new text char to the TextSprite which will pe painted at x, y under
     * the current CTM</p>
     *
     * @param cid     cid to paint.
     * @param unicode unicode representation of cid.
     * @param x       x-coordinate to paint.
     * @param y       y-coordinate to paint.
     * @param width   width of cid from font.
     */
    public GlyphText addText(String cid, String unicode, float x, float y, float width) {

        // x,y must not chance as it will affect painting of the glyph,
        // we can change the bounds of glyphBounds as this is what needs to be normalized
        // to page space
        // IMPORTANT: where working in Java Coordinates with any of the Font bounds
        float w = width;//(float)stringBounds.getWidth();
        float h = (float) (font.getAscent() + font.getDescent());

        double descent = font.getDescent();
        double ascent = font.getAscent();

        if (h <= 0.0f) {
            h = (float) (font.getMaxCharBounds().getHeight());
        }
        if (w <= 0.0f) {
            w = (float) font.getMaxCharBounds().getWidth();
        }
        // zero height will not intersect with clip rectangle and maybe have visibility issues.
        // we generally get here if the font.getAscent is zero and as a result must compensate.
        if (h <= 0.0f) {
            Rectangle2D bounds = font.getEstringBounds(cid, 0, 1);
            if (bounds != null && bounds.getHeight() > 0) {
                h = (float) bounds.getHeight();
            } else {
                // match the width, as it will make text selection work a bit better.
                h = font.getSize();
            }
            if (ascent == 0) {
                ascent = h;
            }
        }

        Rectangle2D.Float glyphBounds;
        // irregular negative layout of text,  need to create the bbox appropriately.
        if (w < 0.0f || font.getSize() < 0) {
            glyphBounds = new Rectangle2D.Float(x + width, y - (float) descent, -w, h);
        }else{
            glyphBounds = new Rectangle2D.Float(x, y - (float) ascent, w, h);
        }

        // add bounds to total text bounds.
        bounds.add(glyphBounds);

        // create glyph and normalize bounds.
        GlyphText glyphText =
                new GlyphText(x, y, glyphBounds, cid, unicode);
        glyphText.normalizeToUserSpace(graphicStateTransform, tmTransform);
        glyphTexts.add(glyphText);
        return glyphText;
    }

    /**
     * Gets the character bounds of each glyph found in the TextSprite.
     *
     * @return bounds in PDF coordinates of character bounds
     */
    public ArrayList<GlyphText> getGlyphSprites() {
        return glyphTexts;
    }

    public AffineTransform getGraphicStateTransform() {
        return graphicStateTransform;
    }

    /**
     * Set the graphic state transform on all child sprites, This is used for
     * xForm object parsing and text selection.  There is no need to do this
     * outside of the context parser.
     *
     * @param graphicStateTransform
     */
    public void setGraphicStateTransform(AffineTransform graphicStateTransform) {
        this.graphicStateTransform = graphicStateTransform;
        for (GlyphText sprite : glyphTexts) {
            sprite.normalizeToUserSpace(this.graphicStateTransform, tmTransform);
        }
    }

    /**
     * <p>Set the rmode for all the characters being in this object. Rmode can
     * have the following values:</p>
     * <ul>
     * <li>0 - Fill text.</li>
     * <li>1 - Stroke text. </li>
     * <li>2 - fill, then stroke text.  </li>
     * <li>3 - Neither fill nor stroke text (invisible).  </li>
     * <li>4 - Fill text and add to path for clipping.  </li>
     * <li>5 - Stroke text and add to path for clipping.   </li>
     * <li>6 - Fill, then stroke text and add to path for clipping. </li>
     * <li>7 - Add text to path for clipping.</li>
     * </ul>
     *
     * @param rmode valid rmode from 0-7
     */
    public void setRMode(int rmode) {
        if (rmode >= 0) {
            this.rmode = rmode;
        }
    }

    public String toString() {
        StringBuilder text = new StringBuilder(glyphTexts.size());
        for (GlyphText glyphText : glyphTexts) {
            text.append(glyphText.getUnicode());
        }
        return text.toString();
    }

    public void setStrokeColor(Color color) {
        strokeColor = color;
    }

    /**
     * Getst the bounds of the text that makes up this sprite.  The bounds
     * are defined PDF space and are relative to the current CTM.
     *
     * @return text sprites bounds.
     */
    public Rectangle2D.Float getBounds() {
        return bounds;
    }

    /**
     * <p>Paints all the character elements in this TextSprite to the graphics
     * context</p>
     *
     * @param g graphics context to which the characters will be painted to.
     */
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // draw bounding box.
//        drawBoundBox(g2d);

        for (GlyphText glyphText : glyphTexts) {

            // paint glyph
            font.drawEstring(g2d,
                    glyphText.getCid(),
                    glyphText.getX(), glyphText.getY(),
                    FontFile.LAYOUT_NONE, rmode, strokeColor);

            // debug glyph box
            // draw glyph box
//            drawGyphBox(g2d, glyphText);
        }
    }

    /**
     * Gets the glyph outline as an Area.  This method is primarily used
     * for processing text rendering modes 4 - 7.
     *
     * @return area representing the glyph outline.
     */
    public Area getGlyphOutline() {
        Area glyphOutline = null;
        for (GlyphText glyphText : glyphTexts) {
            if (glyphOutline != null) {
                glyphOutline.add(new Area(font.getEstringOutline(
                        glyphText.getCid(),
                        glyphText.getX(), glyphText.getY())));
            } else {
                glyphOutline = new Area(font.getEstringOutline(
                        glyphText.getCid(),
                        glyphText.getX(), glyphText.getY()));
            }
        }
        return glyphOutline;
    }

    public FontFile getFont() {
        return font;
    }

    public Color getStrokeColor() {
        return strokeColor;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    /*
    private void drawBoundBox(Graphics2D gg) {

        // draw the characters
        GeneralPath charOutline;

        Color oldColor = gg.getColor();
        Stroke oldStroke = gg.getStroke();
        double scale = gg.getTransform().getScaleX();

        scale = 1.0f / scale;
        if (scale <= 0) {
            scale = 1;
        }
        gg.setStroke(new BasicStroke((float) (scale)));
        gg.setColor(Color.blue);

        charOutline = new GeneralPath(bounds);
        gg.draw(charOutline);

        gg.setColor(oldColor);
        gg.setStroke(oldStroke);
    }
    */

    public void setFont(FontFile font) {
        this.font = font;
    }
    /*
    private void drawGyphBox(Graphics2D gg, GlyphText glyphSprite) {

        // draw the characters
        GeneralPath charOutline;

        Color oldColor = gg.getColor();
        Stroke oldStroke = gg.getStroke();
        double scale = gg.getTransform().getScaleX();

        scale = 1.0f / scale;
        if (scale <= 0) {
            scale = 1;
        }
        gg.setStroke(new BasicStroke((float) (scale)));
        gg.setColor(Color.red);

        charOutline = new GeneralPath(glyphSprite.getBounds());
        gg.draw(charOutline);

        gg.setColor(oldColor);
        gg.setStroke(oldStroke);

    }


    */

    /**
     * Tests if the interior of the <code>TextSprite</code> bounds intersects the
     * interior of a specified <code>shape</code>.
     *
     * @param shape shape to calculate intersection against
     * @return true, if <code>TextSprite</code> bounds intersects <code>shape</code>;
     *         otherwise; false.
     */
    public boolean intersects(Shape shape) {
//        return shape.intersects(bounds.toJava2dCoordinates());
        return !(optimizedDrawingEnabled)||
                (shape != null && shape.intersects(bounds));
    }
}
