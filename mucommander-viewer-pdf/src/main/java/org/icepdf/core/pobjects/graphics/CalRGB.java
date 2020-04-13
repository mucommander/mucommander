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
import java.util.HashMap;
import java.util.List;

/**
 * A CalRGB colour space is a CIE-based ABC colour space with only one
 * transformation stage instead of two. In this type of space, A, B, and C
 * represent calibrated red, green, and blue colour values. These three colour
 * components shall be in the range 0.0 to 1.0; component values falling outside
 * that range shall be adjusted to the nearest valid value without error indication.
 * The decoding functions (denoted by “Decode ABC” in Figure 22) are gamma
 * functions whose coefficients shall be specified by the Gamma entry in the
 * colour space dictionary (see Table 64). The transformation matrix denoted by
 * “Matrix ABC” in Figure 22 shall be defined by the dictionary’s Matrix entry.
 * Since there is no second transformation stage, “Decode LMN” and “Matrix LMN”
 * shall be implicitly taken to be identity transformations.
 *
 * @since 1.0
 */
public class CalRGB extends PColorSpace {

    public static final Name WHITE_POINT_KEY = new Name("WhitePoint");
    public static final Name GAMMA_KEY = new Name("Gamma");
    public static final Name MATRIX_KEY = new Name("Matrix");
    public static final Name CALRGB_KEY = new Name("CalRGB");

    protected float[] whitepoint = {
            1, 1, 1
    };
    protected float[] gamma = {
            1, 1, 1
    };
    protected float[] matrix = {
            1, 0, 0, 0, 1, 0, 0, 0, 1
    };


    CalRGB(Library l, HashMap h) {
        super(l, h);
        List m = (List) h.get(WHITE_POINT_KEY);
        if (m != null) {
            for (int i = 0; i < 3; i++) {
                whitepoint[i] = ((Number) m.get(i)).floatValue();
            }
        }
        m = (List) h.get(GAMMA_KEY);
        if (m != null) {
            for (int i = 0; i < 3; i++) {
                gamma[i] = ((Number) m.get(i)).floatValue();
            }
        }
        m = (List) h.get(MATRIX_KEY);
        if (m != null) {
            for (int i = 0; i < 9; i++) {
                matrix[i] = ((Number) m.get(i)).floatValue();
            }
        }
    }


    public int getNumComponents() {
        return 3;
    }


    public Color getColor(float[] f, boolean fillAndStroke) {
        if (true) {
            return new java.awt.Color(f[2], f[1], f[0]);
        }
        /*        float A = (float)Math.exp(gamma[0]*Math.log(f[2]));
         float B = (float)Math.exp(gamma[1]*Math.log(f[1]));
         float C = (float)Math.exp(gamma[2]*Math.log(f[0]));*/
        float A = (float) Math.pow(f[2], gamma[0]);
        float B = (float) Math.pow(f[1], gamma[1]);
        float C = (float) Math.pow(f[0], gamma[2]);
        float X = matrix[0] * A + matrix[3] * B + matrix[6] * C;
        float Y = matrix[1] * A + matrix[4] * B + matrix[7] * C;
        float Z = matrix[2] * A + matrix[5] * B + matrix[8] * C;
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
        return new Color(X, Y, Z);
        //        return  new java.awt.Color(f[2]*255/max_val, f[1]*255/max_val, f[0]*255/max_val);
    }
}



