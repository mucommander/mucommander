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
package org.icepdf.core.util;

import java.awt.*;

/**
 * <p>The <code>GraphicsRenderingHints</code> class provides a central place for
 * storing Java2D rendering hints settings.  The
 * <code>GraphicsRenderingHints</code> object is used to apply different rendering
 * hints for printing and screen presentation when rending a Page's content.</p>
 * <p/>
 * <p>The "screen" and "print" configuration are configurable with system properties.
 * See the <i>ICEpdf Developer's Guide</i>  for more information about configuring
 * these properites.</p>
 *
 * @author Mark Collette
 * @since 2.0
 */
public class GraphicsRenderingHints {

    /**
     * Constant used to specify rendering hint specific to screen rendering.
     */
    public static final int SCREEN = 1;

    /**
     * Constant used to specify rendering hint specific to print rendering.
     */
    public static final int PRINT = 2;

    /**
     * Gets the singleton representation of this object.
     *
     * @return a reference to the singleton GraphicsRenderingHints object.
     */
    public static synchronized GraphicsRenderingHints getDefault() {
        if (singleton == null) {
            singleton = new GraphicsRenderingHints();
        }
        return singleton;
    }

    // singleton value of this object.
    private static GraphicsRenderingHints singleton;

    /**
     * Load values from the system properties if any and assign defaults.
     */
    private GraphicsRenderingHints() {
        setFromProperties();
    }

    /**
     * Gets the rendering hints for either the SCREEN or PRINT mode.
     *
     * @param hintType SCREEN or PRINT, if incorrectly specified PRINT settings
     *                 are returned.
     * @return RenderingHints used by Java2D graphics context.
     */
    public RenderingHints getRenderingHints(final int hintType) {
        if (hintType == SCREEN)
            return (RenderingHints) screenHints.clone();
        else
            return (RenderingHints) printHints.clone();
    }

    public Color getPageBackgroundColor(final int hintType) {
        if (hintType == SCREEN)
            return screenBackground;
        else
            return printBackground;
    }

    /**
     * Rereads the system properties responsible for setting the rendering hints
     * for both the PRINT and SCREEN modes.
     */
    public synchronized void reset() {
        setFromProperties();
    }

    /**
     * Utility method for reading the system properties.
     */
    private void setFromProperties() {
        // grab System properties for screen rendering attributes
        String property = Defs.sysProperty("org.icepdf.core.screen.alphaInterpolation");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_ALPHA_INTERPOLATION_QUALITY")) {
                screenAlphaInterpolocation = RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY;
            } else if (property.equalsIgnoreCase("VALUE_ALPHA_INTERPOLATION_DEFAULT")) {
                screenAlphaInterpolocation = RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT;
            } else if (property.equalsIgnoreCase("VALUE_ALPHA_INTERPOLATION_SPEED")) {
                screenAlphaInterpolocation = RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED;
            }
        }
        property = Defs.sysProperty("org.icepdf.core.screen.antiAliasing");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_ANTIALIAS_DEFAULT")) {
                screenAntiAliasing = RenderingHints.VALUE_ANTIALIAS_DEFAULT;
            } else if (property.equalsIgnoreCase("VALUE_ANTIALIAS_ON")) {
                screenAntiAliasing = RenderingHints.VALUE_ANTIALIAS_ON;
            } else if (property.equalsIgnoreCase("VALUE_ANTIALIAS_OFF")) {
                screenAntiAliasing = RenderingHints.VALUE_ANTIALIAS_OFF;
            }
        }
        property = Defs.sysProperty("org.icepdf.core.screen.textAntiAliasing");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_TEXT_ANTIALIAS_DEFAULT")) {
                screenTextAntiAliasing = RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
            } else if (property.equalsIgnoreCase("VALUE_TEXT_ANTIALIAS_ON")) {
                screenTextAntiAliasing = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
            } else if (property.equalsIgnoreCase("VALUE_TEXT_ANTIALIAS_OFF")) {
                screenTextAntiAliasing = RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
            }
        }
        property = Defs.sysProperty("org.icepdf.core.screen.colorRender");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_COLOR_RENDER_DEFAULT")) {
                screenColorRendering = RenderingHints.VALUE_COLOR_RENDER_DEFAULT;
            } else if (property.equalsIgnoreCase("VALUE_COLOR_RENDER_QUALITY")) {
                screenColorRendering = RenderingHints.VALUE_COLOR_RENDER_QUALITY;
            } else if (property.equalsIgnoreCase("VALUE_COLOR_RENDER_SPEED")) {
                screenColorRendering = RenderingHints.VALUE_COLOR_RENDER_SPEED;
            }
        }
        property = Defs.sysProperty("org.icepdf.core.screen.dither");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_DITHER_DEFAULT")) {
                screenDithering = RenderingHints.VALUE_DITHER_DEFAULT;
            } else if (property.equalsIgnoreCase("VALUE_DITHER_DISABLE")) {
                screenDithering = RenderingHints.VALUE_DITHER_DISABLE;
            } else if (property.equalsIgnoreCase("VALUE_DITHER_ENABLE")) {
                screenDithering = RenderingHints.VALUE_DITHER_ENABLE;
            }
        }
        property = Defs.sysProperty("org.icepdf.core.screen.fractionalmetrics");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_FRACTIONALMETRICS_DEFAULT")) {
                screenFractionalMetrics = RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT;
            } else if (property.equalsIgnoreCase("VALUE_FRACTIONALMETRICS_ON")) {
                screenFractionalMetrics = RenderingHints.VALUE_FRACTIONALMETRICS_ON;
            } else if (property.equalsIgnoreCase("VALUE_FRACTIONALMETRICS_OFF")) {
                screenFractionalMetrics = RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
            }
        }
        property = Defs.sysProperty("org.icepdf.core.screen.interpolation");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_INTERPOLATION_BICUBIC")) {
                screenInterPolation = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
            } else if (property.equalsIgnoreCase("VALUE_INTERPOLATION_BILINEAR")) {
                screenInterPolation = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
            } else if (property.equalsIgnoreCase("VALUE_INTERPOLATION_NEAREST_NEIGHBOR")) {
                screenInterPolation = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
            }
        }
        property = Defs.sysProperty("org.icepdf.core.screen.render");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_RENDER_DEFAULT")) {
                screenRendering = RenderingHints.VALUE_RENDER_DEFAULT;
            } else if (property.equalsIgnoreCase("VALUE_RENDER_QUALITY")) {
                screenRendering = RenderingHints.VALUE_RENDER_QUALITY;
            } else if (property.equalsIgnoreCase("VALUE_RENDER_SPEED")) {
                screenRendering = RenderingHints.VALUE_RENDER_SPEED;
            }
        }
        property = Defs.sysProperty("org.icepdf.core.screen.stroke");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_STROKE_DEFAULT")) {
                screenStrokeControl = RenderingHints.VALUE_STROKE_DEFAULT;
            } else if (property.equalsIgnoreCase("VALUE_STROKE_NORMALIZE")) {
                screenStrokeControl = RenderingHints.VALUE_STROKE_NORMALIZE;
            } else if (property.equalsIgnoreCase("VALUE_STROKE_PURE")) {
                screenStrokeControl = RenderingHints.VALUE_STROKE_PURE;
            }
        }
        property = Defs.sysProperty("org.icepdf.core.screen.background");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_DRAW_WHITE_BACKGROUND")) {
                screenBackground = Color.white;
            } else if (property.equalsIgnoreCase("VALUE_DRAW_NO_BACKGROUND")) {
                screenBackground = null;
            }
        }

        screenHints = new RenderingHints(
                RenderingHints.KEY_ALPHA_INTERPOLATION, screenAlphaInterpolocation);
        screenHints.put(RenderingHints.KEY_ANTIALIASING, screenAntiAliasing);
        screenHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, screenTextAntiAliasing);
        screenHints.put(RenderingHints.KEY_COLOR_RENDERING, screenColorRendering);
        screenHints.put(RenderingHints.KEY_DITHERING, screenDithering);
        screenHints.put(RenderingHints.KEY_FRACTIONALMETRICS, screenFractionalMetrics);
        screenHints.put(RenderingHints.KEY_INTERPOLATION, screenInterPolation);
        screenHints.put(RenderingHints.KEY_RENDERING, screenRendering);
        screenHints.put(RenderingHints.KEY_STROKE_CONTROL, screenStrokeControl);


        // grab System properties for print rendering attributes
        property = Defs.sysProperty("org.icepdf.core.print.alphaInterpolation");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_ALPHA_INTERPOLATION_QUALITY")) {
                printAlphaInterpolocation = RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY;
            } else if (property.equalsIgnoreCase("VALUE_ALPHA_INTERPOLATION_DEFAULT")) {
                printAlphaInterpolocation = RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT;
            } else if (property.equalsIgnoreCase("VALUE_ALPHA_INTERPOLATION_SPEED")) {
                printAlphaInterpolocation = RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED;
            }
        }
        property = Defs.sysProperty("org.icepdf.core.print.antiAliasing");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_ANTIALIAS_DEFAULT")) {
                printAntiAliasing = RenderingHints.VALUE_ANTIALIAS_DEFAULT;
            } else if (property.equalsIgnoreCase("VALUE_ANTIALIAS_ON")) {
                printAntiAliasing = RenderingHints.VALUE_ANTIALIAS_ON;
            } else if (property.equalsIgnoreCase("VALUE_ANTIALIAS_OFF")) {
                printAntiAliasing = RenderingHints.VALUE_ANTIALIAS_OFF;
            }
        }
        property = Defs.sysProperty("org.icepdf.core.print.textAntiAliasing");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_TEXT_ANTIALIAS_DEFAULT")) {
                printTextAntiAliasing = RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
            } else if (property.equalsIgnoreCase("VALUE_TEXT_ANTIALIAS_ON")) {
                printTextAntiAliasing = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
            } else if (property.equalsIgnoreCase("VALUE_TEXT_ANTIALIAS_OFF")) {
                printTextAntiAliasing = RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
            }
        }
        property = Defs.sysProperty("org.icepdf.core.print.colorRender");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_COLOR_RENDER_DEFAULT")) {
                printColorRendering = RenderingHints.VALUE_COLOR_RENDER_DEFAULT;
            } else if (property.equalsIgnoreCase("VALUE_COLOR_RENDER_QUALITY")) {
                printColorRendering = RenderingHints.VALUE_COLOR_RENDER_QUALITY;
            } else if (property.equalsIgnoreCase("VALUE_COLOR_RENDER_SPEED")) {
                printColorRendering = RenderingHints.VALUE_COLOR_RENDER_SPEED;
            }
        }
        property = Defs.sysProperty("org.icepdf.core.print.dither");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_DITHER_DEFAULT")) {
                printDithering = RenderingHints.VALUE_DITHER_DEFAULT;
            } else if (property.equalsIgnoreCase("VALUE_DITHER_DISABLE")) {
                printDithering = RenderingHints.VALUE_DITHER_DISABLE;
            } else if (property.equalsIgnoreCase("VALUE_DITHER_ENABLE")) {
                printDithering = RenderingHints.VALUE_DITHER_ENABLE;
            }
        }
        property = Defs.sysProperty("org.icepdf.core.print.fractionalmetrics");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_FRACTIONALMETRICS_DEFAULT")) {
                printFractionalMetrics = RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT;
            } else if (property.equalsIgnoreCase("VALUE_FRACTIONALMETRICS_ON")) {
                printFractionalMetrics = RenderingHints.VALUE_FRACTIONALMETRICS_ON;
            } else if (property.equalsIgnoreCase("VALUE_FRACTIONALMETRICS_OFF")) {
                printFractionalMetrics = RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
            }
        }
        property = Defs.sysProperty("org.icepdf.core.print.interpolation");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_INTERPOLATION_BICUBIC")) {
                printInterPolation = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
            } else if (property.equalsIgnoreCase("VALUE_INTERPOLATION_BILINEAR")) {
                printInterPolation = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
            } else if (property.equalsIgnoreCase("VALUE_INTERPOLATION_NEAREST_NEIGHBOR")) {
                printInterPolation = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
            }
        }
        property = Defs.sysProperty("org.icepdf.core.print.render");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_RENDER_DEFAULT")) {
                printRendering = RenderingHints.VALUE_RENDER_DEFAULT;
            } else if (property.equalsIgnoreCase("VALUE_RENDER_QUALITY")) {
                printRendering = RenderingHints.VALUE_RENDER_QUALITY;
            } else if (property.equalsIgnoreCase("VALUE_RENDER_SPEED")) {
                printRendering = RenderingHints.VALUE_RENDER_SPEED;
            }
        }
        property = Defs.sysProperty("org.icepdf.core.print.stroke");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_STROKE_DEFAULT")) {
                printStrokeControl = RenderingHints.VALUE_STROKE_DEFAULT;
            } else if (property.equalsIgnoreCase("VALUE_STROKE_NORMALIZE")) {
                printStrokeControl = RenderingHints.VALUE_STROKE_NORMALIZE;
            } else if (property.equalsIgnoreCase("VALUE_STROKE_PURE")) {
                printStrokeControl = RenderingHints.VALUE_STROKE_PURE;
            }
        }
        property = Defs.sysProperty("org.icepdf.core.print.background");
        if (property != null) {
            if (property.equalsIgnoreCase("VALUE_DRAW_WHITE_BACKGROUND")) {
                printBackground = Color.white;
            } else if (property.equalsIgnoreCase("VALUE_DRAW_NO_BACKGROUND")) {
                printBackground = null;
            }
        }

        printHints = new RenderingHints(
                RenderingHints.KEY_ALPHA_INTERPOLATION, printAlphaInterpolocation);
        printHints.put(RenderingHints.KEY_ANTIALIASING, printAntiAliasing);
        printHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, printTextAntiAliasing);
        printHints.put(RenderingHints.KEY_COLOR_RENDERING, printColorRendering);
        printHints.put(RenderingHints.KEY_DITHERING, printDithering);
        printHints.put(RenderingHints.KEY_FRACTIONALMETRICS, printFractionalMetrics);
        printHints.put(RenderingHints.KEY_INTERPOLATION, printInterPolation);
        printHints.put(RenderingHints.KEY_RENDERING, printRendering);
        printHints.put(RenderingHints.KEY_STROKE_CONTROL, printStrokeControl);
    }


    /**
     * This hint controls how partially-transparent
     * drawing operations are composited.  The default value is
     * VALUE_ALPHA_INTERPOLATION_QUALITY
     */
    Object printAlphaInterpolocation = RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY;
    /**
     * This hint controls if text and images will be drawn using anitialiasing.
     * The default value is VALUE_ANTIALIAS_ON
     */
    Object printAntiAliasing = RenderingHints.VALUE_ANTIALIAS_ON;
    /**
     * This hint controls if text will be drawn using anitialiasing.  This property
     * can not set to ON unless printAntiAliasing is set to ON
     * The default value is VALUE_ANTIALIAS_ON
     */
    Object printTextAntiAliasing = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
    /**
     * This hint controls colour rendering quality.
     * The default value is VALUE_COLOR_RENDER_QUALITY
     */
    Object printColorRendering = RenderingHints.VALUE_COLOR_RENDER_QUALITY;
    /**
     * This hint controls dithering of an image.
     * The default value is VALUE_DITHER_ENABLE
     */
    Object printDithering = RenderingHints.VALUE_DITHER_ENABLE;
    /**
     * This hint controls fractional Metrics calculations for drawing text.
     * The default value is VALUE_FRACTIONALMETRICS_ON
     */
    Object printFractionalMetrics = RenderingHints.VALUE_FRACTIONALMETRICS_ON;
    /**
     * This hint controls image interpolation.
     * The default value is VALUE_INTERPOLATION_BILINEAR
     */
    Object printInterPolation = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
    /**
     * This hint controls rendering quality.
     * The default value is VALUE_RENDER_QUALITY
     */
    Object printRendering = RenderingHints.VALUE_RENDER_QUALITY;
    /**
     * This hint controls stroke quality.
     * The default value is VALUE_STROKE_NORMALIZE
     */
    Object printStrokeControl = RenderingHints.VALUE_STROKE_NORMALIZE;
    /**
     * This hints controls if the Page will paint a white background before drawing itself.
     * The default value is Color.white
     */
    Color printBackground = Color.white;

    // take care of Screen mode default values. The general ideal is to lower
    // qality where possible to encresase drawing speed.

    /**
     * This hint controls how partially-transparent drawing operations are
     * composited.  The default value is
     * VALUE_ALPHA_INTERPOLATION_SPEED
     */
    Object screenAlphaInterpolocation = RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED;
    /**
     * This hint controls if text and images will be drawn using anitialiasing.
     * The default value is VALUE_ANTIALIAS_ON
     */
    Object screenAntiAliasing = RenderingHints.VALUE_ANTIALIAS_ON;
    /**
     * This hint controls if text will be drawn using anitialiasing.  This property
     * can not set to ON unless printAntiAliasing is set to ON
     * The default value is VALUE_ANTIALIAS_ON
     */
    Object screenTextAntiAliasing = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
    /**
     * This hint controls colour rendering quality.
     * The default value is VALUE_COLOR_RENDER_SPEED
     */
    Object screenColorRendering = RenderingHints.VALUE_COLOR_RENDER_SPEED;
    /**
     * This hint controls dithering of an image.
     * The default value is VALUE_DITHER_ENABLE
     */
    Object screenDithering = RenderingHints.VALUE_DITHER_DEFAULT;
    /**
     * This hint controls fractional Metrics calculations for drawing text.
     * The default value is VALUE_FRACTIONALMETRICS_ON
     */
    Object screenFractionalMetrics = RenderingHints.VALUE_FRACTIONALMETRICS_ON;
    /**
     * This hint controls image interpolation.
     * The default value is VALUE_INTERPOLATION_BICUBIC
     */
    Object screenInterPolation = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
    /**
     * This hint controls rendering quality.
     * The default value is VALUE_RENDER_QUALITY
     */
    Object screenRendering = RenderingHints.VALUE_RENDER_SPEED;
    /**
     * This hint controls stroke quality.
     * The default value is VALUE_STROKE_NORMALIZE
     */
    Object screenStrokeControl = RenderingHints.VALUE_STROKE_PURE;
    /**
     * This hints controls if the Page will paint a white background before drawing itself.
     * The default value is Color.white
     */
    Color screenBackground = Color.white;

    private RenderingHints screenHints;
    private RenderingHints printHints;
}
