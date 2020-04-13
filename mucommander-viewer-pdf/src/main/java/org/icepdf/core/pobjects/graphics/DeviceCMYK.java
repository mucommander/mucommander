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
import org.icepdf.core.util.Defs;
import org.icepdf.core.util.Library;

import java.awt.*;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Device CMYK colour space definitions. The primary purpose of this colour
 * space is to convert cymk colours to rgb.  No ICC profile is used in this
 * process and the generated rgb colour is just and approximation.
 */
public class DeviceCMYK extends PColorSpace {

    private static final Logger logger =
            Logger.getLogger(DeviceCMYK.class.toString());

    public static final Name DEVICECMYK_KEY = new Name("DeviceCMYK");
    public static final Name CMYK_KEY = new Name("CMYK");

    private static final DeviceGray DEVICE_GRAY = new DeviceGray(null, null);

    // default cmyk value,  > 255 will lighten the image.
    private static float blackRatio;

    // CMYK ICC color profile.
    private static ICC_ColorSpace iccCmykColorSpace;
    // basic cache to speed up the lookup. always 4 bands, can be static
    private ConcurrentHashMap<Integer, Color> iccCmykColorCache =
            new ConcurrentHashMap<Integer, Color>();

    // disable icc color profile lookups as they can be slow. n
    private static boolean disableICCCmykColorSpace;

    static {
        // black ratio
        blackRatio = (float) Defs.doubleProperty("org.icepdf.core.cmyk.colorant.black", 1.0);

        disableICCCmykColorSpace = Defs.booleanProperty("org.icepdf.core.cmyk.disableICCProfile", false);

        // check for a custom CMYK ICC colour profile specified using system properties.
        iccCmykColorSpace = getIccCmykColorSpace();
    }

    public DeviceCMYK(Library l, HashMap h) {
        super(l, h);
    }


    public int getNumComponents() {
        return 4;
    }

    /**
     * Converts a 4 component cmyk colour to rgb.  With out a valid ICC colour
     * profile this is just an approximation.
     *
     * @param f 4 component values of the cmyk, assumes compoents between
     *          0.0 and 1.0
     * @return valid rgb colour object.
     */
    public Color getColor(float[] f, boolean fillAndStroke) {
        return alternative2(f, iccCmykColorCache);
    }

    /**
     * Ah yes the many possible ways to go from cmyk to rgb.  Everybody has
     * an opinion but no one has the solution that is 100%
     */

    /**
     * Adobe photo shop algorithm or so they say.  K is assumed to be f[0]
     *
     * @param f 4 component values of the cmyk, assumes comopents between
     *          0.0 and 1.0
     * @return valid rgb colour object.
     */
    private static Color alternative1(float[] f) {

        float c = f[3];
        float m = f[2];
        float y = f[1];
        float k = f[0];

        float r = 1.0f - Math.min(1.0f, c + k);
        float g = 1.0f - Math.min(1.0f, m + k);
        float b = 1.0f - Math.min(1.0f, y + k);

        return new Color(r, g, b);
    }

    /**
     * @param f 4 component values of the cmyk, assumes components between
     *          0.0 and 1.0
     * @return valid rgb colour object.
     */
    private static Color alternative3(float[] f) {

        float c = f[3];
        float m = f[2];
        float y = f[1];
        float k = f[0];

        float r = 1.0f - Math.min(1.0f, (c * (1 - k)) + k);
        float g = 1.0f - Math.min(1.0f, (m * (1 - k)) + k);
        float b = 1.0f - Math.min(1.0f, (y * (1 - k)) + k);

        return new Color(r, g, b);
    }

    /**
     * Auto cad color model
     * var R=Math.round((1-C)*(1-K)*255);
     * var B=Math.round((1-Y)*(1-K)*255);
     * var G=Math.round((1-M)*(1-K)*255);
     *
     * @param f 4 component values of the cmyk, assumes compoents between
     *          0.0 and 1.0
     * @return valid rgb colour object.
     */
    private static Color getAutoCadColor(float[] f) {

        float c = f[3];
        float m = f[2];
        float y = f[1];
        float k = f[0];

        int red = Math.round((1.0f - c) * (1.0f - k) * 255);
        int blue = Math.round((1.0f - y) * (1.0f - k) * 255);
        int green = Math.round((1.0f - m) * (1.0f - k) * 255);

        return new Color(red, green, blue);
    }

    /**
     * GNU Ghost Script algorithm or so they say.
     * <p/>
     * rgb[0] = colors * (255 - cyan)/255;
     * rgb[1] = colors * (255 - magenta)/255;
     * rgb[2] = colors * (255 - yellow)/255;
     *
     * @param f 4 component values of the cmyk, assumes compoents between
     *          0.0 and 1.0
     * @return valid rgb colour object.
     */
    private static Color getGhostColor(float[] f) {

        int cyan = (int) (f[3] * 255);
        int magenta = (int) (f[2] * 255);
        int yellow = (int) (f[1] * 255);
        int black = (int) (f[0] * 255);
        float colors = 255 - black;

        float[] rgb = new float[3];
        rgb[0] = colors * (255 - cyan) / 255;
        rgb[1] = colors * (255 - magenta) / 255;
        rgb[2] = colors * (255 - yellow) / 255;

        return new Color((int) rgb[0], (int) rgb[1], (int) rgb[2]);

    }

    /**
     * Current runner for conversion that looks closest to acrobat.
     * The algorithm is a little expensive but it does the best approximation.
     *
     * @param f 4 component values of the cmyk, assumes compoents between
     *          0.0 and 1.0
     * @return valid rgb colour object.
     */
    private static Color alternative2(float[] f,
                                      ConcurrentHashMap<Integer, Color> iccCmykColorCache) {
        float inCyan = f[3];
        float inMagenta = f[2];
        float inYellow = f[1];
        float inBlack = f[0];

        // check if we have a valid ICC profile to work with
        if (!disableICCCmykColorSpace && iccCmykColorSpace != null) {
            // generate a key for the colour
            int key = (((int) (f[0] * 255) & 0xff) << 24) |
                    (((int) (f[1] * 255) & 0xff) << 16) |
                    (((int) (f[2] * 255) & 0xff) << 8) |
                    (((int) (f[3] * 255) & 0xff) & 0xff);
            Color color = iccCmykColorCache.get(key);
            if (color != null) {
                return color;
            } else {
                try {
                    f = iccCmykColorSpace.toRGB(reverse(f));
                    color = new Color(f[0], f[1], f[2]);
                    iccCmykColorCache.put(key, color);
                    return color;
                } catch (Throwable e) {
                    logger.warning("Error using iccCmykColorSpace in DeviceCMYK.");
                }
            }
        }

        // soften the amount of black, but exclude explicit black colorant.
        if (!(inCyan == 0 && inMagenta == 0 && inYellow == 0)) {
            inBlack = inBlack * blackRatio;
        }
        // if only the  black colorant then we can treat the colour as gray,
        // cmyk is subtractive.
        else {
            f[0] = 1.0f - f[0];
            return DEVICE_GRAY.getColor(f);
        }

        double c, m, y, aw, ac, am, ay, ar, ag, ab;
        c = clip(0.0, 1.0, inCyan + inBlack);
        m = clip(0.0, 1.0, inMagenta + inBlack);
        y = clip(0.0, 1.0, inYellow + inBlack);
        aw = (1 - c) * (1 - m) * (1 - y);
        ac = c * (1 - m) * (1 - y);
        am = (1 - c) * m * (1 - y);
        ay = (1 - c) * (1 - m) * y;
        ar = (1 - c) * m * y;
        ag = c * (1 - m) * y;
        ab = c * m * (1 - y);

        float outRed = (float) clip(0.0, 1.0, aw + 0.9137 * am + 0.9961 * ay + 0.9882 * ar);
        float outGreen = (float) clip(0.0, 1.0, aw + 0.6196 * ac + ay + 0.5176 * ag);
        float outBlue = (float) clip(0.0, 1.0, aw + 0.7804 * ac + 0.5412 * am + 0.0667 * ar + 0.2118 * ag + 0.4863 * ab);

        return new Color(outRed, outGreen, outBlue);
    }

    /**
     * Clips the value according to the specified floor and ceiling.
     *
     * @param floor   floor value of clip
     * @param ceiling ceiling value of clip
     * @param value   value to clip.
     * @return clipped value.
     */
    private static double clip(double floor, double ceiling, double value) {
        if (value < floor) {
            value = floor;
        }
        if (value > ceiling) {
            value = ceiling;
        }
        return value;
    }

    /**
     * Gets the ICC Color Profile found in the icepdf-core.jar at the location
     * /org/icepdf/core/pobjects/graphics/res/ or the ICC Color Profiel
     * specified by the system property org.icepdf.core.pobjects.graphics.cmyk.
     *
     * @return associated ICC CMYK Color space.
     */
    public static ICC_ColorSpace getIccCmykColorSpace() {
        // would prefer to only have one instance but becuase of JDK-8033238
        // we can run into decode issue if we share the profile across
        String customCMYKProfilePath = null;
        try {
            Object profileStream;
            customCMYKProfilePath = Defs.sysProperty("org.icepdf.core.pobjects.graphics.cmyk");
            if (customCMYKProfilePath == null) {
                customCMYKProfilePath = "/org/icepdf/core/pobjects/graphics/res/CoatedFOGRA27.icc";
                profileStream = DeviceCMYK.class.getResourceAsStream(customCMYKProfilePath);
            } else {
                profileStream = new FileInputStream(customCMYKProfilePath);
            }

            ICC_Profile icc_profile = ICC_Profile.getInstance((InputStream) profileStream);
            return new ICC_ColorSpace(icc_profile);
        } catch (Exception exception) {
            logger.warning("Error loading ICC color profile: " + customCMYKProfilePath);
        }
        return null;
    }

    /**
     * Determines if the ICC CMYK color space should be used to convert
     * CMYK images to RGB.
     *
     * @return true if the ICC CMYK color space should be used, false otherwise.
     */
    public static boolean isDisableICCCmykColorSpace() {
        return disableICCCmykColorSpace;
    }

    /**
     * Set the value of the disableICCCmykColorSpace property.  This property
     * can be set using the system property org.icepdf.core.cmyk.disableICCProfile
     * or overridden using this mehtod.
     *
     * @param disableICCCmykColorSpace true to disable the ICC CMYK color space
     *                                 conversion, false otherwise.
     */
    public static void setDisableICCCmykColorSpace(boolean disableICCCmykColorSpace) {
        DeviceCMYK.disableICCCmykColorSpace = disableICCCmykColorSpace;
    }
}
