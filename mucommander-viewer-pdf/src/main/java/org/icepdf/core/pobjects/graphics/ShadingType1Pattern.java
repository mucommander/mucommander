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
import org.icepdf.core.pobjects.graphics.batik.ext.awt.LinearGradientPaint;
import org.icepdf.core.pobjects.graphics.batik.ext.awt.MultipleGradientPaint;
import org.icepdf.core.util.Library;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * In Type 1 (function-based) shadings, the colour at every point in the domain
 * is defined by a specified mathematical function. The function need not be
 * smooth or continuous. This type is the most general of the available shading
 * types and is useful for shadings that cannot be adequately described with any
 * of the other types. Table 79 shows the shading dictionary entries specific
 * to this type of shading, in addition to those common to all shading
 * dictionaries
 *
 * @author ICEsoft Technologies Inc.
 * @since 5.0
 */
public class ShadingType1Pattern extends ShadingType2Pattern {

    private static final Logger logger =
            Logger.getLogger(ShadingType1Pattern.class.toString());

    /**
     * (Optional) An array of four numbers [xmin xmax ymin ymax] specifying the
     * rectangular domain of coordinates over which the colour function(s) are
     * defined. Default value: [0.0 1.0 0.0 1.0].
     */
//    protected java.util.List<Number> domain;

    /**
     * (Required) A 2-in, n-out function or an array of n 2-in, 1-out functions
     * (where n is the number of colour components in the shading dictionary’s
     * colour space). Each function’s domain shall be a superset of that of the
     * shading dictionary. If the value returned by the function for a given
     * colour component is out of range, it shall be adjusted to the nearest
     * valid value.
     */
//    protected Function[] function;

    // linear gradient paint describing the gradient.
    private LinearGradientPaint linearGradientPaint;

    public ShadingType1Pattern(Library library, HashMap entries) {
        super(library, entries);
    }

    @SuppressWarnings("unchecked")
    public synchronized void init(GraphicsState graphicsState) {
        if (inited) {
            return;
        }

        // shading dictionary
        if (shadingDictionary == null) {
            shadingDictionary = library.getDictionary(entries, SHADING_KEY);
        }

        colorSpace = PColorSpace.getColorSpace(library,
                library.getObject(shadingDictionary, COLORSPACE_KEY));

        // get type 2 specific data.
        Object tmp = library.getObject(shadingDictionary, DOMAIN_KEY);
        if (tmp instanceof java.util.List) {
            domain = (List<Number>) tmp;
        } else {
            domain = new ArrayList<Number>(2);
            domain.add(0.0f);
            domain.add(1.0f);
            domain.add(0.0f);
            domain.add(1.0f);
        }

        // functions
        tmp = library.getObject(shadingDictionary, FUNCTION_KEY);
        if (tmp != null) {
            if (!(tmp instanceof java.util.List)) {
                function = new Function[]{Function.getFunction(library,
                        tmp)};
            } else {
                java.util.List functionTemp = (java.util.List) tmp;
                function = new Function[functionTemp.size()];
                for (int i = 0; i < functionTemp.size(); i++) {
                    function[i] = Function.getFunction(library, functionTemp.get(i));
                }
            }
        }

        // first off, create the two needed start and end points of the line
        Point2D.Float startPoint = new Point2D.Float(
                domain.get(0).floatValue(),
                domain.get(2).floatValue());

        Point2D.Float endPoint = new Point2D.Float(
                domain.get(0).floatValue(),
                domain.get(3).floatValue());

        // calculate the t's
        float t0 = domain.get(0).floatValue();
        float t1 = domain.get(3).floatValue();

        // calculate colour based on points that make up the line, 10 is a good
        // number for speed and gradient quality.
        try {
            int numberOfPoints = 10;
            Color[] colors = calculateColorPoints(numberOfPoints, startPoint, endPoint, t0, t1);
            float[] dist = calculateDomainEntries(numberOfPoints, t0, t1);

            linearGradientPaint = new LinearGradientPaint(
                    startPoint, endPoint, dist, colors,
                    MultipleGradientPaint.NO_CYCLE,
                    MultipleGradientPaint.LINEAR_RGB,
                    matrix);
            inited = true;
        } catch (Exception e) {
            logger.finer("Failed ot initialize gradient paint type 1.");
        }
    }

    /**
     * Not implemented
     *
     * @return will always return null;
     */
    public Paint getPaint() {
        return null;
    }


    public String toSting() {
        return super.toString() +
                "\n                    domain: " + domain +
                "\n                    matrix: " + matrix +
                "\n                 function: " + function;
    }
}
