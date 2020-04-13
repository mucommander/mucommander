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

import org.icepdf.core.pobjects.functions.Function;
import org.icepdf.core.pobjects.graphics.batik.ext.awt.MultipleGradientPaint;
import org.icepdf.core.pobjects.graphics.batik.ext.awt.RadialGradientPaint;
import org.icepdf.core.util.Library;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * <p>Type 3 (radial) shading define a colour blend that varies between two
 * circles.  Shading of this type are commonly used to depict three-dimensional
 * spheres and cones.</p>
 *
 * @author ICEsoft Technologies Inc.
 * @since 3.0
 */
public class ShadingType3Pattern extends ShadingPattern {

    private static final Logger logger =
            Logger.getLogger(ShadingType3Pattern.class.toString());

    // An array of two numbers [t0, t1] specifying the limiting values of a
    // parametric variable t. The variable is considered to vary linearly between
    // these two values as the colour gradient varies between the starting and
    // ending points of the axis.  The variable t becomes the argument to the
    // colour function(s).  Default [0,1].
    protected List<Number> domain;

    // An array of six numbers [x0, y0, r0, x1, y1, r1] specifying the centers
    // and radii of the starting and ending circles.  Expressed in the shading
    // target coordinate space.  The radii r0 and r1 must both be greater than
    // or equal to 0. If both are zero nothing is painted.
    protected List coords;

    // An array of two Boolean values specifying whether to extend the shading
    // beyond the starting and ending points of the axis, Default [false, false].
    protected List<Boolean> extend;

    // radial gradient paint that is used by java for paint. 
    protected RadialGradientPaint radialGradientPaint;


    public ShadingType3Pattern(Library library, HashMap entries) {
        super(library, entries);
    }

    @SuppressWarnings("unchecked")
    public synchronized void init(GraphicsState graphicsState) {

        if (inited) {
            return;
        }

        // shadingDictionary dictionary
        if (shadingDictionary == null) {
            shadingDictionary = library.getDictionary(entries, SHADING_KEY);
        }

        shadingType = library.getInt(shadingDictionary, SHADING_TYPE_KEY);
        bBox = library.getRectangle(shadingDictionary, BBOX_KEY);
        colorSpace = PColorSpace.getColorSpace(library,
                library.getObject(shadingDictionary, COLORSPACE_KEY));
        if (library.getObject(shadingDictionary, BACKGROUND_KEY) != null &&
                library.getObject(shadingDictionary, BACKGROUND_KEY) instanceof List) {
            background = (List) library.getObject(shadingDictionary, BACKGROUND_KEY);
        }
        antiAlias = library.getBoolean(shadingDictionary, ANTIALIAS_KEY);

        // get type 2 specific data.
        Object tmp = library.getObject(shadingDictionary, DOMAIN_KEY);
        if (tmp instanceof List) {
            domain = (List<Number>) tmp;
        } else {
            domain = new ArrayList<Number>(2);
            domain.add(0.0f);
            domain.add(1.0f);
        }
        tmp = library.getObject(shadingDictionary, COORDS_KEY);
        if (tmp instanceof List) {
            coords = (List) tmp;
        }
        tmp = library.getObject(shadingDictionary, EXTEND_KEY);
        if (tmp instanceof List) {
            extend = (List) tmp;
        } else {
            extend = new ArrayList<Boolean>(2);
            extend.add(false);
            extend.add(false);
        }
        tmp = library.getObject(shadingDictionary, FUNCTION_KEY);
        if (tmp != null) {
            if (!(tmp instanceof List)) {
                function = new Function[]{Function.getFunction(library,
                        tmp)};
            } else {
                List functionTemp = (List) tmp;
                function = new Function[functionTemp.size()];
                for (int i = 0; i < functionTemp.size(); i++) {
                    function[i] = Function.getFunction(library, functionTemp.get(i));
                }
            }
        }

        float t0 = domain.get(0).floatValue();
        float t1 = domain.get(1).floatValue();
        float s[] = new float[]{0.0f, 0.25f, 0.5f, 0.75f, 1.0f};

        Point2D.Float center = new Point2D.Float(
                ((Number) coords.get(0)).floatValue(),
                ((Number) coords.get(1)).floatValue());

        Point2D.Float focus = new Point2D.Float(
                ((Number) coords.get(3)).floatValue(),
                ((Number) coords.get(4)).floatValue());

        float radius = ((Number) coords.get(2)).floatValue();
        float radius2 = ((Number) coords.get(5)).floatValue();

        // approximation, as we don't full support radial point via the paint
        // class. 
        if (radius2 > radius) {
            radius = radius2;
        }

        try {
            // get the number off components in the colour
            Color color1 = calculateColour(colorSpace, s[0], t0, t1);
            Color color2 = calculateColour(colorSpace, s[1], t0, t1);
            Color color3 = calculateColour(colorSpace, s[2], t0, t1);
            Color color4 = calculateColour(colorSpace, s[3], t0, t1);
            Color color5 = calculateColour(colorSpace, s[4], t0, t1);

            if (color1 == null || color2 == null) {
                return;
            }
            // Construct a LinearGradientPaint object to be use by java2D
            Color[] colors = {color1, color2, color3, color4, color5};

            radialGradientPaint = new RadialGradientPaint(
                    center, radius,
                    focus,
                    s,
                    colors,
                    MultipleGradientPaint.NO_CYCLE,
                    MultipleGradientPaint.LINEAR_RGB,
                    matrix);

            // get type 3 specific data.
            inited = true;
        } catch (Exception e) {
            logger.finer("Failed ot initialize gradient paint type 3.");
        }
    }

    private Color calculateColour(PColorSpace colorSpace, float s,
                                  float t0, float t1) {

        // find colour at point 1
        float t = parametrixValue(s, t0, t1, extend);
        // find colour at point 
        float[] input = new float[1];
        input[0] = t;
        if (function != null) {
            float[] output = calculateValues(input);
            if (output != null) {
                if (!(colorSpace instanceof DeviceN)) {
                    output = PColorSpace.reverse(output);
                }
                return colorSpace.getColor(output);
            } else {
                return null;
            }
        } else {
            logger.fine("Error processing Shading Type 3 Pattern.");
            return null;
        }
    }

    /**
     * Parametric variable t calculation as defined in Section 4.6, Type 2
     * (axial) shadings.
     *
     * @param linearMapping linear mapping of some point x'
     * @param t0            domain of axial shading, limit 1
     * @param t1            domain of axial shading, limit 2
     * @param extended      2 element vector, indicating line extension along domain
     * @return parametric value.
     */
    private float parametrixValue(float linearMapping, float t0, float t1,
                                  List extended) {
        return t0 + ((t1 - t0) * linearMapping);
    }

    public Paint getPaint() throws InterruptedException {
        init();
        return radialGradientPaint;
    }

    public String toSting() {
        return super.toString() +
                "\n                    domain: " + domain +
                "\n                    coords: " + coords +
                "\n                    extend: " + extend +
                "\n                 function: " + function;
    }
}