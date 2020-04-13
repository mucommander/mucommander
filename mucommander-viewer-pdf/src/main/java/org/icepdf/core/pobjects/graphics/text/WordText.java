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
package org.icepdf.core.pobjects.graphics.text;

import org.icepdf.core.util.Defs;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Word text represents an individual word in the document.  A word can
 * also represent white space between words th isWhiteSpace method can be used
 * to distguish between words and whiteSpace
 * <p/>
 * If extracted text has extract white space then the space width fraction
 * can be adjusted.  The deault value a 4th of the current character width.  To
 * add more sapces the number can be increase or decrease to limit the number
 * of spaces that are added. The system property is as follows:
 * Default<br/>
 * org.icepdf.core.views.page.text.spaceFraction=4
 *
 * @since 4.0
 */
public class WordText extends AbstractText implements TextSelect {

    private static final Logger logger =
            Logger.getLogger(WordText.class.toString());

    // Space Glyph width fraction, the default is 1/x of the character width,
    // constitutes a potential space between glyphs.
    public static int spaceFraction;

    private static boolean autoSpaceInsertion;

    static {
        // sets the shadow colour of the decorator.
        try {
            spaceFraction = Defs.sysPropertyInt(
                    "org.icepdf.core.views.page.text.spaceFraction", 3);
        } catch (NumberFormatException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Error reading text space fraction");
            }
        }
        // sets the shadow colour of the decorator.
        try {
            autoSpaceInsertion = Defs.sysPropertyBoolean(
                    "org.icepdf.core.views.page.text.autoSpace", true);
        } catch (NumberFormatException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Error reading text text auto space detection");
            }
        }
    }

    private GlyphText currentGlyph;
    private ArrayList<GlyphText> glyphs;

    // cached text values.
    private StringBuilder text;
    // is glyph white space.
    private boolean isWhiteSpace;
    // reference to last added text.
    private int previousGlyphText;

    public WordText() {
        text = new StringBuilder();
        glyphs = new ArrayList<GlyphText>(4);
    }

    public int size(){
        return text.length();
    }

    public boolean isWhiteSpace() {
        return isWhiteSpace;
    }

    public void setWhiteSpace(boolean whiteSpace) {
        isWhiteSpace = whiteSpace;
    }

    protected boolean detectSpace(GlyphText sprite) {
        if (currentGlyph != null && autoSpaceInsertion) {
            // last added glyph
            Rectangle2D.Float bounds1 = currentGlyph.getTextExtractionBounds();
            float currentXCoord = sprite.getTextExtractionBounds().x;
            float currentYCoord = sprite.getTextExtractionBounds().y;
            float previousXCoord = currentGlyph.getTextExtractionBounds().x;
            float previousYCoord = currentGlyph.getTextExtractionBounds().y;
            // spaces can be negative if we have a LTR layout.
            float space = Math.abs(currentXCoord - (previousXCoord + bounds1.width));
            // half previous glyph width will be used to determine a space
            float tolerance = bounds1.width / spaceFraction;
            // checking the y coordinate as well as any shift normall means a new work, this might need to get fuzzy later.
            float ydiff = Math.abs(currentYCoord - previousYCoord);
            return space > tolerance || ydiff > tolerance;
        } else {
            return false;
        }
    }

    protected static boolean detectPunctuation(GlyphText sprite, WordText currentWord) {
        String glyphText = sprite.getUnicode();
        // make sure we don't have a decimal, we want to keep double numbers
        // as one word.
        if (glyphText != null && glyphText.length() > 0) {
            int c = glyphText.charAt(0);
            return isPunctuation(c) && !isDigit(currentWord);
        } else {
            return false;
        }
    }

    protected static boolean detectWhiteSpace(GlyphText sprite) {
        String glyphText = sprite.getUnicode();
        if (glyphText != null && glyphText.length() > 0) {
            int c = glyphText.charAt(0);
            return isWhiteSpace(c);
        } else {
            return false;
        }
    }

    public static boolean isPunctuation(int c) {
        return ((c == '.') || (c == ',') || (c == '?') || (c == '!') ||
                (c == ':') || (c == ';') || (c == '"') || (c == '\'')
                || (c == '/') || (c == '\\') || (c == '`') || (c == '#'));
    }

    public static boolean isWhiteSpace(int c) {
        return ((c == ' ') || (c == '\t') || (c == '\r') ||
                (c == '\n') || (c == '\f'));
    }

    public static boolean isDigit(WordText currentWord) {
        if (currentWord != null) {
            int c = currentWord.getPreviousGlyphText();
            return isDigit((char) c);
        } else {
            return false;
        }
    }

    public static boolean isDigit(char c) {
        return c >= 48 && c <= 57;
    }

    protected WordText buildSpaceWord(GlyphText sprite) {

        // because we are in a normalized user space we can work with ints
        Rectangle2D.Float bounds1 = currentGlyph.getTextExtractionBounds();
        Rectangle.Float bounds2 = sprite.getTextExtractionBounds();
        float space = bounds2.x - (bounds1.x + bounds1.width);

        // max width of previous and next glyph, average can be broken by l or i etc.
        float maxWidth = Math.max(bounds1.width, bounds2.width) / 2f;
        int spaces = (int) (space / maxWidth);
        if (spaces == 0) {
            spaces = 1;
        }
        // add extra spaces
        WordText whiteSpace = new WordText();
        double offset;
        GlyphText spaceText = null;
        Rectangle2D.Float spaceBounds;
        // RTL layout
        float spaceWidth = space / spaces;
        boolean ltr = true;
        if (spaces > 0) {
            offset = bounds1.x + bounds1.width;
            spaceBounds = new Rectangle2D.Float(
                    bounds1.x + bounds1.width,
                    bounds1.y,
                    spaceWidth, bounds1.height);
        }
        // LTR layout
        else {
            ltr = false;
            offset = bounds1.x - bounds1.width;
            spaces = 1;//Math.abs(spaces);
            spaceBounds = new Rectangle2D.Float(
                    bounds.x - spaceWidth,
                    bounds1.y,
                    spaceWidth, bounds1.height);
        }
        // todo: consider just using one space with a wider bound
        // Max out the spaces in the case the spaces value scale factor was
        // not correct.  We can end up with a very large number of spaces being
        // inserted in some cases.
        if (autoSpaceInsertion) {
            for (int i = 0; i < spaces && i < 50; i++) {
                whiteSpace = autoSpaceCalculation(offset, spaceBounds, whiteSpace);
                if (ltr) {
                    spaceBounds.x += spaceBounds.width;
                    offset += spaceWidth;
                } else {
                    spaceBounds.x -= spaceBounds.width;
                    offset -= spaceWidth;
                }
            }
        } else {
            whiteSpace = autoSpaceCalculation(offset, spaceBounds, whiteSpace);
        }
        return whiteSpace;
    }

    private WordText autoSpaceCalculation(double offset,
                                          Rectangle2D.Float spaceBounds,
                                          WordText whiteSpace) {
        GlyphText spaceText = new GlyphText((float) offset,
                currentGlyph.getY(),
                new Rectangle2D.Float(spaceBounds.x,
                        spaceBounds.y,
                        spaceBounds.width,
                        spaceBounds.height),
                String.valueOf((char) 32), String.valueOf((char) 32));
        whiteSpace.addText(spaceText);
        whiteSpace.setWhiteSpace(true);
        return whiteSpace;
    }


    protected void addText(GlyphText sprite) {
        // the sprite
        glyphs.add(sprite);

        currentGlyph = sprite;

        // append the bounds calculation
        if (bounds == null) {
            Rectangle2D.Float rect = sprite.getBounds();
            bounds = new Rectangle2D.Float(rect.x, rect.y, rect.width, rect.height);
        } else {
            bounds.add(sprite.getBounds());
        }
        if (textExtractionBounds == null) {
            Rectangle2D.Float rect = sprite.getTextExtractionBounds();
            textExtractionBounds = new Rectangle2D.Float(rect.x, rect.y, rect.width, rect.height);
        } else {
            textExtractionBounds.add(sprite.getTextExtractionBounds());
        }

        // append the text that maps up the sprite
        String unicode = sprite.getUnicode();
        previousGlyphText = unicode != null && unicode.length() > 0 ?
                unicode.charAt(0) : 0;
        text.append(unicode);
    }

    public Rectangle2D.Float getBounds() {
        if (bounds == null) {
            // increase bounds as glyphs are detected.
            for (GlyphText glyph : glyphs) {
                if (bounds == null) {
                    bounds = new Rectangle2D.Float();
                    bounds.setRect(glyph.getBounds());
                } else {
                    bounds.add(glyph.getBounds());
                }
                if (textExtractionBounds == null) {
                    Rectangle2D.Float rect = glyph.getTextExtractionBounds();
                    textExtractionBounds = new Rectangle2D.Float(rect.x, rect.y, rect.width, rect.height);
                } else {
                    textExtractionBounds.add(glyph.getTextExtractionBounds());
                }
            }
        }
        return bounds;
    }

    public ArrayList<GlyphText> getGlyphs() {
        return glyphs;
    }

    public StringBuilder getSelected() {
        StringBuilder selectedText = new StringBuilder();
        for (GlyphText glyph : glyphs) {
            if (glyph.isSelected()) {
                selectedText.append(glyph.getUnicode());
            }
        }
        return selectedText;
    }

    public void clearHighlighted() {
        setHighlighted(false);
        setHasHighlight(false);
        for (GlyphText glyph : glyphs) {
            glyph.setHighlighted(false);
        }
    }

    public void clearSelected() {
        setSelected(false);
        setHasSelected(false);
        for (GlyphText glyph : glyphs) {
            glyph.setSelected(false);
        }
    }

    public void selectAll() {
        setSelected(true);
        setHasSelected(true);
        for (GlyphText glyph : glyphs) {
            glyph.setSelected(true);
        }
    }

    public String getText() {
        // iterate of sprites and get text.
//        if (isWhiteSpace) {
//            return text.toString().replace(" ", "_|");
//        } else if (text.toString().equals(""))
//            return text.toString().replace("", "*");
//        else {
        return text.toString();
//        }
    }

    public int getPreviousGlyphText() {
        return previousGlyphText;
    }

    public String toString() {
        return getText();
    }
}
