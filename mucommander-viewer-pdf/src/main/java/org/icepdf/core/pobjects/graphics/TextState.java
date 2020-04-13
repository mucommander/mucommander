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

import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.PRectangle;
import org.icepdf.core.pobjects.fonts.Font;
import org.icepdf.core.pobjects.fonts.FontFile;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * The text state comprises those graphics state parameters that only affect text.
 * <p/>
 * Tc - Character spacing
 * Tw - Word spacing
 * Th - Horizontal scaling
 * Tl - Leading Tf Text font
 * Tfs - Text font size
 * Tmode - Text rendering mode
 * Trise - Text rise
 * Tk - Text knockout
 *
 * @since 2.0
 */
public class TextState {

    /**
     * Fill text
     */
    public static final int MODE_FILL = 0;

    /**
     * Stroke text
     */
    public static final int MODE_STROKE = 1;
    /**
     * Fill then stroke text.
     */
    public static final int MODE_FILL_STROKE = 2;
    /**
     * Neither fill nor stroke text
     */
    public static final int MODE_INVISIBLE = 3;
    /**
     * Fill text and add to path for clipping.
     */
    public static final int MODE_FILL_ADD = 4;
    /**
     * Stroke text and add to path for clipping
     */
    public static final int MODE_STROKE_ADD = 5;
    /**
     * Fill then stroke,text and add to path for clipping
     */
    public static final int MODE_FILL_STROKE_ADD = 6;

    /**
     * Add text to path for clipping
     */
    public static final int MODE_ADD = 7;

    // type3 font text states for d1 token.
    protected PRectangle type3BBox;
    // type 3 font text state for d0 token.
    protected Point2D.Float type3HorizontalDisplacement;

    /**
     * Set the character spacing, Tc, to charSpace, which is a number expressed
     * in unscaled text space units. Character spacing is used by the Tj, TJ,
     * and ' operators. Initial value: 0.
     */
    public float cspace;
    /**
     * Set the word spacing, Tw, to wordSpace, which is a number expressed in
     * unscaled text space units. Word spacing is used by the Tj, TJ, and '
     * operators. Initial value: 0.
     */
    public float wspace;
    /**
     * Set the horizontal scaling, Th, to (scale div 100). scale is a number
     * specifying the percentage of the normal width. Initial value: 100
     * (normal width).
     */
    public float hScalling;
    /**
     * Set the text leading, Tl, to leading, which is a number expressed in
     * unscaled text space units. Text leading is used only by the T*, ',
     * and " operators. Initial value: 0.
     */
    public float leading;
    /**
     * Text Font size
     */
    public float tsize;

    /**
     * Font's named resource name.
     */
    public Name fontName;
    /**
     * Set the text rendering mode, Tmode, to render, which is an integer.
     * Initial value: 0.
     */
    public int rmode;
    /**
     * Set the text rise, Trise, to rise, which is a number expressed in
     * unscaled text space units. Initial value: 0.
     */
    public float trise;
    /**
     * Transformation matrix defined by the Tm tag
     */
    public AffineTransform tmatrix;
    public AffineTransform tlmatrix;
    /**
     * Text Font - Associated ICEpdf font object
     */
    public Font font;
    /**
     * Text Font - Associated awt font object for display purposes
     */
    public FontFile currentfont;

    /**
     * Create a new Instance of TextState
     */
    public TextState() {
        tmatrix = new AffineTransform();
        tlmatrix = new AffineTransform();
        hScalling = 1;
    }

    /**
     * Creat a new instance of TextState. All text state properties are copied
     * from <code>ts</code>.
     *
     * @param ts text state to
     */
    public TextState(TextState ts) {
        // map properties
        cspace = ts.cspace;
        wspace = ts.wspace;
        hScalling = ts.hScalling;
        leading = ts.leading;
        font = ts.font;
        // create a new clone based on current font, cheap clone
        currentfont = ts.currentfont != null ?
                ts.currentfont.deriveFont(new AffineTransform()) : null;
        tsize = ts.tsize;
        tmatrix = new AffineTransform(ts.tmatrix);
        tlmatrix = new AffineTransform(ts.tlmatrix);
        rmode = ts.rmode;
        trise = ts.trise;
    }

    public PRectangle getType3BBox() {
        return type3BBox;
    }

    public void setType3BBox(PRectangle type3BBox) {
        this.type3BBox = type3BBox;
    }

    public Point2D.Float getType3HorizontalDisplacement() {
        return type3HorizontalDisplacement;
    }

    public void setType3HorizontalDisplacement(Point2D.Float type3HorizontalDisplacement) {
        this.type3HorizontalDisplacement = type3HorizontalDisplacement;
    }

}



