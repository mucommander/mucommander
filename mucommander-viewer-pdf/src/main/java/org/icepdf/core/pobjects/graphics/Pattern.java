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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;


/**
 * <p>Patterns come in two varieties:</p>
 * <ul>
 * <li><p><i>Tiling patterns</i> consist of a small graphical figure (called a
 * pattern cell) that is replicated at fixed horizontal and vertical
 * intervals to fill the area to be painted. The graphics objects to
 * use for tiling are described by a content stream. (PDF 1.2)</li>
 * <p/>
 * <li><p><i>Shading patterns</i> define a gradient fill that produces a smooth
 * transition between colors across the area. The color to use is
 * specified as a function of position using any of a variety of
 * methods. (PDF 1.3)</li>
 * </ul>
 * <p>Note Tiling pattern and shading patterns are not currently supported</p>
 *
 * @since 1.0
 */
public interface Pattern {

    /**
     * The pattern type is a tiling pattern
     */
    public static final int PATTERN_TYPE_TILING = 1;

    /**
     * The pattern type is a shading pattern
     */
    public static final int PATTERN_TYPE_SHADING = 2;

    public static final Name TYPE_VALUE = new Name("pattern");

    public Name getType();

    public int getPatternType();

    public AffineTransform getMatrix();

    public void setMatrix(AffineTransform matrix);

    public Rectangle2D getBBox();

    void init(GraphicsState graphicsState);

    public Paint getPaint() throws InterruptedException;

    public void setParentGraphicState(GraphicsState graphicsState);

}
