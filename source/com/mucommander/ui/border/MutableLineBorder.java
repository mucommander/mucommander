/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.border;

import javax.swing.border.LineBorder;
import java.awt.Color;

/**
 * Implementation of <code>LineBorder</code> that allows applications to change the color after it's been instanciated.
 * @author Nicolas Rinaudo
 */
public class MutableLineBorder extends LineBorder {
    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a line border with the specified color and a thickness = 1.
     * @param color the color of the border.
     */
    public MutableLineBorder(Color color) {super(color);}

    /**
     * Creates a line border with the specified color and thickness.
     * @param color     the color of the border
     * @param thickness the thickness of the border
     */
    public MutableLineBorder(Color color, int thickness) {super(color, thickness);}

    /**
     * Creates a line border with the specified color, thickness, and corner shape.
     * @param color          the color of the border
     * @param thickness      the thickness of the border
     * @param roundedCorners whether or not border corners should be round
     */
    public MutableLineBorder(Color color, int thickness, boolean roundedCorners) {super(color, thickness, roundedCorners);}



    // - Setters -------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Sets this border's color.
     * @param color the color of the border.
     */
    public void setLineColor(Color color) {lineColor = color;}

    /**
     * Sets this border's corner shape.
     * @param roundedCorners whether or not border corners should be round
     */
    public void setRoundedCorners(boolean roundedCorners) {this.roundedCorners = roundedCorners;}

    /**
     * Sets this border's thickness.
     * @param thickness the thickness of the border.
     */
    public void setThickness(int thickness) {this.thickness = thickness;}
}
