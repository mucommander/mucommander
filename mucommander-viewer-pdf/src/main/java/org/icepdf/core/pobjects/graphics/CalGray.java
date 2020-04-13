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
import org.icepdf.core.util.Library;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.util.HashMap;

/**
 * A CalGray colour space (PDF 1.1) is a special case of a single-component
 * CIE-based colour space, known as a CIE-based A colour space. This type of
 * space is the one-dimensional (and usually achromatic) analog of CIE-based
 * ABC spaces. Colour values in a CIE-based A space shall have a single component,
 * arbitrarily named A.Figure 23 illustrates the transformations of the A
 * component to X, Y, and Z components of the CIE 1931 XYZ space.
 *
 * @since 5.0.1
 */
public class CalGray extends PColorSpace {

    public static final Name WHITE_POINT_KEY = new Name("WhitePoint");
    public static final Name GAMMA_KEY = new Name("Gamma");
    public static final Name MATRIX_KEY = new Name("Matrix");
    public static final Name CAL_GRAY_KEY = new Name("CalGray");

    private static ColorSpace grayCS = ColorSpace.getInstance(ColorSpace.CS_GRAY);

    protected float[] whitepoint = {
            1, 1, 1
    };
    protected float gamma = 1.0f;

    public CalGray(Library l, HashMap h) {
        super(l, h);

        java.util.List m = (java.util.List) h.get(WHITE_POINT_KEY);
        if (m != null) {
            for (int i = 0; i < 3; i++) {
                whitepoint[i] = ((Number) m.get(i)).floatValue();
            }
        }

        Object o = h.get(GAMMA_KEY);
        if (o instanceof Float) {
            gamma = (Float) o;
        }
    }

    @Override
    public Color getColor(float[] f, boolean fillAndStroke) {

        float A = (float) Math.pow(f[0], gamma);

        float X = whitepoint[0] * A;
        float Y = whitepoint[1] * A;
        float Z = whitepoint[2] * A;
        if (X < 0) {
            X = 0;
        }
        if (Y < 0) {
            Y = 0;
        }
        if (Z < 0) {
            Z = 0;
        }
        if (X > 1) {
            X = 1;
        }
        if (Y > 1) {
            Y = 1;
        }
        if (Z > 1) {
            Z = 1;
        }
        Color tmp = new Color(grayCS, new float[]{Z, Y, Z}, 1.0f);
        return tmp;
    }

    @Override
    public int getNumComponents() {
        return 1;
    }
}
