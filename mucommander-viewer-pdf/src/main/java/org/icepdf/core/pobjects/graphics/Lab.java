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
 * put your documentation comment here
 */
public class Lab extends PColorSpace {

    public static final Name LAB_KEY = new Name("Lab");
    public static final Name WHITE_POINT_KEY = new Name("WhitePoint");
    public static final Name RANGE_KEY = new Name("Range");

    private float[] whitePoint = {
            0.95047f, 1.0f, 1.08883f
    };
    private float[] blackPoint = {
            0f, 0f, 0f
    };
    private float[] range = {
            -100, 100, -100, 100
    };
    private float lBase;
    private float lSpread;
    private float aBase;
    private float aSpread;
    private float bBase;
    private float bSpread;

    private float xBase;
    private float xSpread;
    private float yBase;
    private float ySpread;
    private float zBase;
    private float zSpread;

    /**
     * @param l
     * @param h
     */
    Lab(Library l, HashMap h) {
        super(l, h);
        List v = (java.util.List) l.getObject(h, WHITE_POINT_KEY);
        if (v != null) {
            whitePoint[0] = ((Number) v.get(0)).floatValue();
            whitePoint[1] = ((Number) v.get(1)).floatValue();
            whitePoint[2] = ((Number) v.get(2)).floatValue();
        }
        v = (List) l.getObject(h, RANGE_KEY);
        if (v != null) {
            range[0] = ((Number) v.get(0)).floatValue();
            range[1] = ((Number) v.get(1)).floatValue();
            range[2] = ((Number) v.get(2)).floatValue();
            range[3] = ((Number) v.get(3)).floatValue();
        }

        lBase = 0.0f;
        lSpread = 100.0f;
        aBase = range[0];
        aSpread = range[1] - aBase;
        bBase = range[2];
        bSpread = range[3] - bBase;

        xBase = blackPoint[0];
        xSpread = whitePoint[0] - xBase;
        yBase = blackPoint[1];
        ySpread = whitePoint[1] - yBase;
        zBase = blackPoint[2];
        zSpread = whitePoint[2] - zBase;
    }

    /**
     * @return
     */
    public int getNumComponents() {
        return 3;
    }

    /**
     * @param x
     * @return
     */
    private double g(double x) {
        if (x < 0.2069F)
            x = 0.12842 * (x - 0.13793);
        else
            x = x * x * x;
        return x;
    }

    private double gg(double r) {
        if (r > 0.0031308)
            r = 1.055 * Math.pow(r, (1.0 / 2.4)) - 0.055;
        else
            r *= 12.92;
        return r;
    }

    public void normaliseComponentsToFloats(int[] in, float[] out, float maxval) {
        super.normaliseComponentsToFloats(in, out, maxval);
        out[2] = lBase + (lSpread * out[2]); // L
        out[1] = aBase + (aSpread * out[1]); // a
        out[0] = bBase + (bSpread * out[0]); // b
    }

    /**
     * @param f
     * @return
     */
    public Color getColor(float[] f, boolean fillAndStroke) {
        double cie_b = f[0];
        double cie_a = f[1];
        double cie_L = f[2];

        double var_Y = (cie_L + 16.0) / (116.0);
        double var_X = var_Y + (cie_a * 0.002);
        double var_Z = var_Y - (cie_b * 0.005);
        double X = g(var_X);
        double Y = g(var_Y);
        double Z = g(var_Z);
        X = xBase + X * xSpread;
        Y = yBase + Y * ySpread;
        Z = zBase + Z * zSpread;
        X = Math.max(0, Math.min(1, X));
        Y = Math.max(0, Math.min(1, Y));
        Z = Math.max(0, Math.min(1, Z));

        /*
         * Algorithm from online
        double r = X *  3.2406 + Y * -1.5372 + Z * -0.4986;
        double g = X * -0.9689 + Y *  1.8758 + Z *  0.0415;
        double b = X *  0.0557 + Y * -0.2040 + Z *  1.0570;
        */
        double r = X * 3.241 + Y * -1.5374 + Z * -0.4986;
        double g = X * -0.9692 + Y * 1.876 + Z * 0.0416;
        double b = X * 0.0556 + Y * -0.204 + Z * 1.057;
        r = gg(r);
        g = gg(g);
        b = gg(b);
        int ir = (int) (r * 255.0);
        int ig = (int) (g * 255.0);
        int ib = (int) (b * 255.0);
        ir = Math.max(0, Math.min(255, ir));
        ig = Math.max(0, Math.min(255, ig));
        ib = Math.max(0, Math.min(255, ib));
        return new Color(ir, ig, ib);
    }
}
