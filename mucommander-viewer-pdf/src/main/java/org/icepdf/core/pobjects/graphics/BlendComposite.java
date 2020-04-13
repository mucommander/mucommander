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

/*
 * Copyright (c) 2006 Romain Guy <romain.guy@mac.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.icepdf.core.pobjects.Name;

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public final class BlendComposite implements Composite {
    public enum BlendingMode {
        NORMAL,
        AVERAGE,
        MULTIPLY,
        SCREEN,
        DARKEN,
        LIGHTEN,
        OVERLAY,
        HARD_LIGHT,
        SOFT_LIGHT,
        DIFFERENCE,
        NEGATION,
        EXCLUSION,
        COLOR_DODGE,
        INVERSE_COLOR_DODGE,
        SOFT_DODGE,
        COLOR_BURN,
        INVERSE_COLOR_BURN,
        SOFT_BURN,
        REFLECT,
        GLOW,
        FREEZE,
        HEAT,
        ADD,
        SUBTRACT,
        STAMP,
        RED,
        GREEN,
        BLUE,
        HUE,
        SATURATION,
        COLOR,
        LUMINOSITY
    }

    public static final Name NORMAL_VALUE = new Name("Normal");
    public static final Name COMPATIBLE_VALUE = new Name("Compatible");
    public static final Name MULTIPLY_VALUE = new Name("Multiply");
    public static final Name SCREEN_VALUE = new Name("Screen");
    public static final Name OVERLAY_VALUE = new Name("Overlay");
    public static final Name DARKEN_VALUE = new Name("Darken");
    public static final Name LIGHTEN_VALUE = new Name("Lighten");
    public static final Name COLOR_DODGE_VALUE = new Name("ColorDodge");
    public static final Name COLOR_BURN_VALUE = new Name("ColorBurn");
    public static final Name HARD_LIGHT_VALUE = new Name("HardLight");
    public static final Name SOFT_LIGHT_VALUE = new Name("SoftLight");
    public static final Name DIFFERENCE_VALUE = new Name("Difference");
    public static final Name EXCLUSION_VALUE = new Name("Exclusion");

    private float alpha;
    private BlendingMode mode;

    private BlendComposite(BlendingMode mode) {
        this(mode, 1.0f);
    }

    private BlendComposite(BlendingMode mode, float alpha) {
        this.mode = mode;
        setAlpha(alpha);
    }

    public static BlendComposite getInstance(BlendingMode mode) {
        return new BlendComposite(mode);
    }

    public static BlendComposite getInstance(BlendingMode mode, float alpha) {
        return new BlendComposite(mode, alpha);
    }

    // todo consider composite cache.
    public static Composite getInstance(Name modeName, float alpha) {
        // check for -1, value not set and default should be used.
        if (alpha == -1) {
            alpha = 1;
        }
        if (modeName.equals(NORMAL_VALUE) || modeName.equals(COMPATIBLE_VALUE)) {
            return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
//            return new BlendComposite(BlendingMode.NORMAL, alpha);
        } else if (modeName.equals(MULTIPLY_VALUE)) {
            return new BlendComposite(BlendingMode.MULTIPLY, alpha);
        } else if (modeName.equals(SCREEN_VALUE)) {
            return new BlendComposite(BlendingMode.SCREEN, alpha);
        } else if (modeName.equals(OVERLAY_VALUE)) {
            return new BlendComposite(BlendingMode.OVERLAY, alpha);
        } else if (modeName.equals(DARKEN_VALUE)) {
            return new BlendComposite(BlendingMode.DARKEN, alpha);
        } else if (modeName.equals(LIGHTEN_VALUE)) {
            return new BlendComposite(BlendingMode.LIGHTEN, alpha);
        } else if (modeName.equals(COLOR_DODGE_VALUE)) {
            return new BlendComposite(BlendingMode.SOFT_DODGE, alpha);
        } else if (modeName.equals(COLOR_BURN_VALUE)) {
            return new BlendComposite(BlendingMode.SOFT_BURN, alpha);
        } else if (modeName.equals(HARD_LIGHT_VALUE)) {
            return new BlendComposite(BlendingMode.HARD_LIGHT, alpha);
        } else if (modeName.equals(SOFT_LIGHT_VALUE)) {
            return new BlendComposite(BlendingMode.SOFT_LIGHT, alpha);
        } else if (modeName.equals(DIFFERENCE_VALUE)) {
            return new BlendComposite(BlendingMode.DIFFERENCE, alpha);
        } else if (modeName.equals(EXCLUSION_VALUE)) {
            return new BlendComposite(BlendingMode.EXCLUSION, alpha);
        }
//        return new BlendComposite(BlendingMode.NORMAL, alpha);
        return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
    }


    public BlendComposite derive(BlendingMode mode) {
        return this.mode == mode ? this : new BlendComposite(mode, getAlpha());
    }

    public BlendComposite derive(float alpha) {
        return this.alpha == alpha ? this : new BlendComposite(getMode(), alpha);
    }

    public float getAlpha() {
        return alpha;
    }

    public BlendingMode getMode() {
        return mode;
    }

    private void setAlpha(float alpha) {
        if (alpha < 0.0f || alpha > 1.0f) {
            throw new IllegalArgumentException(
                    "alpha must be comprised between 0.0f and 1.0f");
        }

        this.alpha = alpha;
    }

    @Override
    public int hashCode() {
        return Float.floatToIntBits(alpha) * 31 + mode.ordinal();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BlendComposite)) {
            return false;
        }

        BlendComposite bc = (BlendComposite) obj;

        if (mode != bc.mode) {
            return false;
        }

        return alpha == bc.alpha;
    }

    @Override
    public String toString() {
        return mode + " " + alpha;
    }

    public CompositeContext createContext(ColorModel srcColorModel,
                                          ColorModel dstColorModel,
                                          RenderingHints hints) {
        return new BlendingContext(this);
    }

    private static final class BlendingContext implements CompositeContext {
        private final Blender blender;
        private final BlendComposite composite;

        private BlendingContext(BlendComposite composite) {
            this.composite = composite;
            this.blender = Blender.getBlenderFor(composite);
        }

        public void dispose() {
        }

        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            if (src.getSampleModel().getDataType() != DataBuffer.TYPE_INT ||
                    dstIn.getSampleModel().getDataType() != DataBuffer.TYPE_INT ||
                    dstOut.getSampleModel().getDataType() != DataBuffer.TYPE_INT) {
                throw new IllegalStateException(
                        "Source and destination must store pixels as INT.");
            }

            int width = Math.min(src.getWidth(), dstIn.getWidth());
            int height = Math.min(src.getHeight(), dstIn.getHeight());

            float alpha = composite.getAlpha();

            int[] srcPixel = new int[4];
            int[] dstPixel = new int[4];
            int[] srcPixels = new int[width];
            int[] dstPixels = new int[width];

            for (int y = 0; y < height; y++) {
                src.getDataElements(0, y, width, 1, srcPixels);
                dstIn.getDataElements(0, y, width, 1, dstPixels);
                for (int x = 0; x < width; x++) {
                    // pixels are stored as INT_ARGB
                    // our arrays are [R, G, B, A]
                    int pixel = srcPixels[x];
                    srcPixel[0] = (pixel >> 16) & 0xFF;
                    srcPixel[1] = (pixel >> 8) & 0xFF;
                    srcPixel[2] = (pixel) & 0xFF;
                    srcPixel[3] = (pixel >> 24) & 0xFF;

                    pixel = dstPixels[x];
                    dstPixel[0] = (pixel >> 16) & 0xFF;
                    dstPixel[1] = (pixel >> 8) & 0xFF;
                    dstPixel[2] = (pixel) & 0xFF;
                    dstPixel[3] = (pixel >> 24) & 0xFF;

                    int[] result = blender.blend(srcPixel, dstPixel);

                    // mixes the result with the opacity
                    dstPixels[x] =
                            ((int) (dstPixel[3] + (result[3] - dstPixel[3]) * alpha) & 0xFF) << 24 |
                                    ((int) (dstPixel[0] + (result[0] - dstPixel[0]) * alpha) & 0xFF) << 16 |
                                    ((int) (dstPixel[1] + (result[1] - dstPixel[1]) * alpha) & 0xFF) << 8 |
                                    (int) (dstPixel[2] + (result[2] - dstPixel[2]) * alpha) & 0xFF;
                }
                dstOut.setDataElements(0, y, width, 1, dstPixels);
            }
        }
    }

    private static abstract class Blender {
        public abstract int[] blend(int[] src, int[] dst);

        private static void RGBtoHSL(int r, int g, int b, float[] hsl) {
            float var_R = (r / 255f);
            float var_G = (g / 255f);
            float var_B = (b / 255f);

            float var_Min;
            float var_Max;
            float del_Max;

            if (var_R > var_G) {
                var_Min = var_G;
                var_Max = var_R;
            } else {
                var_Min = var_R;
                var_Max = var_G;
            }
            if (var_B > var_Max) {
                var_Max = var_B;
            }
            if (var_B < var_Min) {
                var_Min = var_B;
            }

            del_Max = var_Max - var_Min;

            float H, S, L;
            L = (var_Max + var_Min) / 2f;

            if (del_Max - 0.01f <= 0.0f) {
                H = 0;
                S = 0;
            } else {
                if (L < 0.5f) {
                    S = del_Max / (var_Max + var_Min);
                } else {
                    S = del_Max / (2 - var_Max - var_Min);
                }

                float del_R = (((var_Max - var_R) / 6f) + (del_Max / 2f)) / del_Max;
                float del_G = (((var_Max - var_G) / 6f) + (del_Max / 2f)) / del_Max;
                float del_B = (((var_Max - var_B) / 6f) + (del_Max / 2f)) / del_Max;

                if (var_R == var_Max) {
                    H = del_B - del_G;
                } else if (var_G == var_Max) {
                    H = (1 / 3f) + del_R - del_B;
                } else {
                    H = (2 / 3f) + del_G - del_R;
                }
                if (H < 0) {
                    H += 1;
                }
                if (H > 1) {
                    H -= 1;
                }
            }

            hsl[0] = H;
            hsl[1] = S;
            hsl[2] = L;
        }

        private static void HSLtoRGB(float h, float s, float l, int[] rgb) {
            int R, G, B;

            if (s - 0.01f <= 0.0f) {
                R = (int) (l * 255.0f);
                G = (int) (l * 255.0f);
                B = (int) (l * 255.0f);
            } else {
                float var_1, var_2;
                if (l < 0.5f) {
                    var_2 = l * (1 + s);
                } else {
                    var_2 = (l + s) - (s * l);
                }
                var_1 = 2 * l - var_2;

                R = (int) (255.0f * hue2RGB(var_1, var_2, h + (1.0f / 3.0f)));
                G = (int) (255.0f * hue2RGB(var_1, var_2, h));
                B = (int) (255.0f * hue2RGB(var_1, var_2, h - (1.0f / 3.0f)));
            }

            rgb[0] = R;
            rgb[1] = G;
            rgb[2] = B;
        }

        private static float hue2RGB(float v1, float v2, float vH) {
            if (vH < 0.0f) {
                vH += 1.0f;
            }
            if (vH > 1.0f) {
                vH -= 1.0f;
            }
            if ((6.0f * vH) < 1.0f) {
                return (v1 + (v2 - v1) * 6.0f * vH);
            }
            if ((2.0f * vH) < 1.0f) {
                return (v2);
            }
            if ((3.0f * vH) < 2.0f) {
                return (v1 + (v2 - v1) * ((2.0f / 3.0f) - vH) * 6.0f);
            }
            return (v1);
        }

        public static Blender getBlenderFor(BlendComposite composite) {
            switch (composite.getMode()) {
                case NORMAL:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            if (src[3] == 0) {
                                return dst;
                            }
                            return src;
                        }
                    };
                case MULTIPLY:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            // white stays white.
                            if (src[3] == 0) {
                                return dst;
                            }
                            return new int[]{
                                    (src[0] * dst[0]) >> 8,
                                    (src[1] * dst[1]) >> 8,
                                    (src[2] * dst[2]) >> 8,
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case ADD:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    Math.min(255, src[0] + dst[0]),
                                    Math.min(255, src[1] + dst[1]),
                                    Math.min(255, src[2] + dst[2]),
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case AVERAGE:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    (src[0] + dst[0]) >> 1,
                                    (src[1] + dst[1]) >> 1,
                                    (src[2] + dst[2]) >> 1,
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case BLUE:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    dst[0],
                                    src[1],
                                    dst[2],
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case COLOR:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            float[] srcHSL = new float[3];
                            RGBtoHSL(src[0], src[1], src[2], srcHSL);
                            float[] dstHSL = new float[3];
                            RGBtoHSL(dst[0], dst[1], dst[2], dstHSL);

                            int[] result = new int[4];
                            HSLtoRGB(srcHSL[0], srcHSL[1], dstHSL[2], result);
                            result[3] = Math.min(255, src[3] + dst[3]);

                            return result;
                        }
                    };
                case COLOR_BURN:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    src[0] == 0 ? 0 :
                                            Math.max(0, 255 - (((255 - dst[0]) << 8) / src[0])),
                                    src[1] == 0 ? 0 :
                                            Math.max(0, 255 - (((255 - dst[1]) << 8) / src[1])),
                                    src[2] == 0 ? 0 :
                                            Math.max(0, 255 - (((255 - dst[2]) << 8) / src[2])),
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case COLOR_DODGE:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    src[0] == 255 ? 255 :
                                            Math.min((dst[0] << 8) / (255 - src[0]), 255),
                                    src[1] == 255 ? 255 :
                                            Math.min((dst[1] << 8) / (255 - src[1]), 255),
                                    src[2] == 255 ? 255 :
                                            Math.min((dst[2] << 8) / (255 - src[2]), 255),
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case DARKEN:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    Math.min(src[0], dst[0]),
                                    Math.min(src[1], dst[1]),
                                    Math.min(src[2], dst[2]),
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case DIFFERENCE:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    Math.abs(dst[0] - src[0]),
                                    Math.abs(dst[1] - src[1]),
                                    Math.abs(dst[2] - src[2]),
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case EXCLUSION:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    dst[0] + src[0] - (dst[0] * src[0] >> 7),
                                    dst[1] + src[1] - (dst[1] * src[1] >> 7),
                                    dst[2] + src[2] - (dst[2] * src[2] >> 7),
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case FREEZE:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    src[0] == 0 ? 0 : Math.max(0, 255 - (255 - dst[0]) * (255 - dst[0]) / src[0]),
                                    src[1] == 0 ? 0 : Math.max(0, 255 - (255 - dst[1]) * (255 - dst[1]) / src[1]),
                                    src[2] == 0 ? 0 : Math.max(0, 255 - (255 - dst[2]) * (255 - dst[2]) / src[2]),
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case GLOW:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    dst[0] == 255 ? 255 : Math.min(255, src[0] * src[0] / (255 - dst[0])),
                                    dst[1] == 255 ? 255 : Math.min(255, src[1] * src[1] / (255 - dst[1])),
                                    dst[2] == 255 ? 255 : Math.min(255, src[2] * src[2] / (255 - dst[2])),
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case GREEN:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    dst[0],
                                    dst[1],
                                    src[2],
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case HARD_LIGHT:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            if (src[3] == 0) {
                                return dst;
                            }
                            return new int[]{
                                    src[0] < 128 ? dst[0] * src[0] >> 7 :
                                            255 - ((255 - src[0]) * (255 - dst[0]) >> 7),
                                    src[1] < 128 ? dst[1] * src[1] >> 7 :
                                            255 - ((255 - src[1]) * (255 - dst[1]) >> 7),
                                    src[2] < 128 ? dst[2] * src[2] >> 7 :
                                            255 - ((255 - src[2]) * (255 - dst[2]) >> 7),
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case HEAT:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    dst[0] == 0 ? 0 : Math.max(0, 255 - (255 - src[0]) * (255 - src[0]) / dst[0]),
                                    dst[1] == 0 ? 0 : Math.max(0, 255 - (255 - src[1]) * (255 - src[1]) / dst[1]),
                                    dst[2] == 0 ? 0 : Math.max(0, 255 - (255 - src[2]) * (255 - src[2]) / dst[2]),
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case HUE:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            float[] srcHSL = new float[3];
                            RGBtoHSL(src[0], src[1], src[2], srcHSL);
                            float[] dstHSL = new float[3];
                            RGBtoHSL(dst[0], dst[1], dst[2], dstHSL);

                            int[] result = new int[4];
                            HSLtoRGB(srcHSL[0], dstHSL[1], dstHSL[2], result);
                            result[3] = Math.min(255, src[3] + dst[3]);

                            return result;
                        }
                    };
                case INVERSE_COLOR_BURN:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    dst[0] == 0 ? 0 :
                                            Math.max(0, 255 - (((255 - src[0]) << 8) / dst[0])),
                                    dst[1] == 0 ? 0 :
                                            Math.max(0, 255 - (((255 - src[1]) << 8) / dst[1])),
                                    dst[2] == 0 ? 0 :
                                            Math.max(0, 255 - (((255 - src[2]) << 8) / dst[2])),
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case INVERSE_COLOR_DODGE:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    dst[0] == 255 ? 255 :
                                            Math.min((src[0] << 8) / (255 - dst[0]), 255),
                                    dst[1] == 255 ? 255 :
                                            Math.min((src[1] << 8) / (255 - dst[1]), 255),
                                    dst[2] == 255 ? 255 :
                                            Math.min((src[2] << 8) / (255 - dst[2]), 255),
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case LIGHTEN:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    Math.max(src[0], dst[0]),
                                    Math.max(src[1], dst[1]),
                                    Math.max(src[2], dst[2]),
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case LUMINOSITY:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {

                            float[] srcHSL = new float[3];
                            RGBtoHSL(src[0], src[1], src[2], srcHSL);
                            float[] dstHSL = new float[3];
                            RGBtoHSL(dst[0], dst[1], dst[2], dstHSL);

                            int[] result = new int[4];
                            HSLtoRGB(dstHSL[0], dstHSL[1], srcHSL[2], result);
                            result[3] = Math.min(255, src[3] + dst[3]);

                            return result;
                        }
                    };
                case NEGATION:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    255 - Math.abs(255 - dst[0] - src[0]),
                                    255 - Math.abs(255 - dst[1] - src[1]),
                                    255 - Math.abs(255 - dst[2] - src[2]),
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case OVERLAY:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            // screening with black leaves the underlying colour unchanged.
                            if (src[3] == 0) {
                                return dst;
                            }
                            return new int[]{
                                    dst[0] < 128 ? (dst[0] * src[0]) >> 7 :
                                            255 - ((255 - dst[0]) * (255 - src[0]) >> 7),
                                    dst[1] < 128 ? dst[1] * src[1] >> 7 :
                                            255 - ((255 - dst[1]) * (255 - src[1]) >> 7),
                                    dst[2] < 128 ? (dst[2] * src[2]) >> 7 :
                                            255 - ((255 - dst[2]) * (255 - src[2]) >> 7),
                                    Math.min(255, dst[3])
                            };
//                            return dst;
                        }
                    };
                case RED:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    src[0],
                                    dst[1],
                                    dst[2],
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case REFLECT:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    src[0] == 255 ? 255 : Math.min(255, dst[0] * dst[0] / (255 - src[0])),
                                    src[1] == 255 ? 255 : Math.min(255, dst[1] * dst[1] / (255 - src[1])),
                                    src[2] == 255 ? 255 : Math.min(255, dst[2] * dst[2] / (255 - src[2])),
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case SATURATION:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            float[] srcHSL = new float[3];
                            RGBtoHSL(src[0], src[1], src[2], srcHSL);
                            float[] dstHSL = new float[3];
                            RGBtoHSL(dst[0], dst[1], dst[2], dstHSL);

                            int[] result = new int[4];
                            HSLtoRGB(dstHSL[0], srcHSL[1], dstHSL[2], result);
                            result[3] = Math.min(255, src[3] + dst[3]);

                            return result;
                        }
                    };
                case SCREEN:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            // screening with black leaves the underlying colour unchanged.
                            if ((src[0] == 0 && src[1] == 0 && src[2] == 0)) {
                                return dst;
                            }
                            // screening any colour with white, produces white.
                            if ((dst[0] != 255 && dst[1] != 255 && dst[2] != 255)) {
                                int[] value = new int[]{
                                        255 - ((255 - src[0]) * (255 - dst[0]) >> 8),
                                        255 - ((255 - src[1]) * (255 - dst[1]) >> 8),
                                        255 - ((255 - src[2]) * (255 - dst[2]) >> 8),
                                        Math.min(255, src[3] + (dst[3]))
                                };
                                return value;
                            }
                            return src;
                        }
                    };
                case SOFT_BURN:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    dst[0] + src[0] < 256 ?
                                            (dst[0] == 255 ? 255 :
                                                    Math.min(255, (src[0] << 7) / (255 - dst[0]))) :
                                            Math.max(0, 255 - (((255 - dst[0]) << 7) / src[0])),
                                    dst[1] + src[1] < 256 ?
                                            (dst[1] == 255 ? 255 :
                                                    Math.min(255, (src[1] << 7) / (255 - dst[1]))) :
                                            Math.max(0, 255 - (((255 - dst[1]) << 7) / src[1])),
                                    dst[2] + src[2] < 256 ?
                                            (dst[2] == 255 ? 255 :
                                                    Math.min(255, (src[2] << 7) / (255 - dst[2]))) :
                                            Math.max(0, 255 - (((255 - dst[2]) << 7) / src[2])),
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case SOFT_DODGE:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            if (src[3] == 0) {
                                return dst;
                            }
                            return new int[]{

                                    dst[0] + src[0] < 256 ?
                                            (src[0] == 255 ? 255 :
                                                    Math.min(255, (dst[0] << 7) / (255 - src[0]))) :
                                            Math.max(0, 255 - (((255 - src[0]) << 7) / dst[0])),
                                    dst[1] + src[1] < 256 ?
                                            (src[1] == 255 ? 255 :
                                                    Math.min(255, (dst[1] << 7) / (255 - src[1]))) :
                                            Math.max(0, 255 - (((255 - src[1]) << 7) / dst[1])),
                                    dst[2] + src[2] < 256 ?
                                            (src[2] == 255 ? 255 :
                                                    Math.min(255, (dst[2] << 7) / (255 - src[2]))) :
                                            Math.max(0, 255 - (((255 - src[2]) << 7) / dst[2])),
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case SOFT_LIGHT:
                    break;
                case STAMP:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    Math.max(0, Math.min(255, dst[0] + 2 * src[0] - 256)),
                                    Math.max(0, Math.min(255, dst[1] + 2 * src[1] - 256)),
                                    Math.max(0, Math.min(255, dst[2] + 2 * src[2] - 256)),
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
                case SUBTRACT:
                    return new Blender() {
                        @Override
                        public int[] blend(int[] src, int[] dst) {
                            return new int[]{
                                    Math.max(0, src[0] + dst[0] - 256),
                                    Math.max(0, src[1] + dst[1] - 256),
                                    Math.max(0, src[2] + dst[2] - 256),
                                    Math.min(255, src[3] + dst[3])
                            };
                        }
                    };
            }
            throw new IllegalArgumentException("Blender not implement for " +
                    composite.getMode().name());
        }
    }
}
