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
 * <p>Type 2 (axial) shadings define colour blend that varies along a linear
 * axis between two endpoints and extends indefinitely perpendicular to the
 * that axis.</p>
 *
 * @author ICEsoft Technologies Inc.
 * @since 2.7
 */
public class ShadingType2Pattern extends ShadingPattern {

    private static final Logger logger =
            Logger.getLogger(ShadingType2Pattern.class.toString());

    // An array of two numbers [t0, t1] specifying the limiting values of a
    // parametric variable t. The variable is considered to vary linearly between
    // these two values as the colour gradient varies between the starting and
    // ending points of the axis.  The variable t becomes the argument to the
    // colour function(s).  Default [0,1].
    protected List<Number> domain;

    // An array of four numbers [x0, y0, x1, y1] specifying the starting and
    // ending coordinates of the axis, expressed in the shading's target
    // coordinate space.
    protected java.util.List coords;

    // An array of two Boolean values specifying whether to extend the shading
    // beyond the starting and ending points of the axis, Default [false, false].
    protected List<Boolean> extend;

    // linear gradient paint describing the gradient.
    private LinearGradientPaint linearGradientPaint;

    public ShadingType2Pattern(Library library, HashMap entries) {
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
        Object tmp = library.getObject(shadingDictionary, BACKGROUND_KEY);
        if (tmp != null && tmp instanceof List) {
            background = (java.util.List) tmp;
        }
        antiAlias = library.getBoolean(shadingDictionary, ANTIALIAS_KEY);

        // get type 2 specific data.
        tmp = library.getObject(shadingDictionary, DOMAIN_KEY);
        if (tmp instanceof List) {
            domain = (List<Number>) tmp;
        } else {
            domain = new ArrayList<Number>(2);
            domain.add(0.0f);
            domain.add(1.0f);
        }

        tmp = library.getObject(shadingDictionary, COORDS_KEY);
        if (tmp instanceof List) {
            coords = (java.util.List) tmp;
        }
        tmp = library.getObject(shadingDictionary, EXTEND_KEY);
        if (tmp instanceof List) {
            extend = (List<Boolean>) tmp;
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

        // calculate the t's
        float t0 = domain.get(0).floatValue();
        float t1 = domain.get(1).floatValue();

        // first off, create the two needed start and end points of the line
        Point2D.Float startPoint = new Point2D.Float(
                ((Number) coords.get(0)).floatValue(),
                ((Number) coords.get(1)).floatValue());

        Point2D.Float endPoint = new Point2D.Float(
                ((Number) coords.get(2)).floatValue(),
                ((Number) coords.get(3)).floatValue());

        // corner case where a pdf engine give zero zero coords which batik
        // can't handle so we pad it slightly.
        if (startPoint.equals(endPoint)) {
            endPoint.x++;
        }

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
            logger.finer("Failed ot initialize gradient paint type 2.");
        }
    }

    /**
     * Calculates x number of points on long the line defined by the start and
     * end point.
     *
     * @param numberOfPoints number of points to generate.
     * @param startPoint     start of line segment.
     * @param endPoint       end of line segment.
     * @return list of points found on line
     */
    protected Color[] calculateColorPoints(int numberOfPoints,
                                           Point2D.Float startPoint,
                                           Point2D.Float endPoint,
                                           float t0, float t1) {
        // calculate the slope
        float m = (startPoint.y - endPoint.y) / (startPoint.x - endPoint.x);
        // calculate the y intercept
        float b = startPoint.y - (m * startPoint.x);

        // let calculate x points between startPoint.x and startPoint.y that
        // are on the line using y = mx + b.
        Color[] color;
        // if we don't have a y-axis line we can uses y=mx + b to get our points.
        if (!Float.isInfinite(m)) {
            float xDiff = (endPoint.x - startPoint.x) / numberOfPoints;
            float xOffset = startPoint.x;
            color = new Color[numberOfPoints + 1];
            Point2D.Float point;
            for (int i = 0, max = color.length; i < max; i++) {
                point = new Point2D.Float(xOffset, (m * xOffset) + b);
                color[i] = calculateColour(colorSpace, point, startPoint, endPoint, t0, t1);
                xOffset += xDiff;
            }
        }
        // otherwise we have a infinite m and can just pick y values
        else {
            float yDiff = (endPoint.y - startPoint.y) / numberOfPoints;
            float yOffset = startPoint.y;
            color = new Color[numberOfPoints + 1];
            Point2D.Float point;
            for (int i = 0, max = color.length; i < max; i++) {
                point = new Point2D.Float(0, yOffset);
                color[i] = calculateColour(colorSpace, point, startPoint, endPoint, t0, t1);
                yOffset += yDiff;
            }
        }
        return color;
    }

    /**
     * Calculate domain entries givent the number of point between t0 and t1
     *
     * @param numberOfPoints number of points to calculate
     * @param t0             lower limit
     * @param t1             upper limit
     * @return array of floats the evenly divide t0 and t1, length is
     * numberOfPoints + 1
     */
    protected float[] calculateDomainEntries(int numberOfPoints, float t0, float t1) {

        float offset = 1.0f / numberOfPoints;
        float[] domainEntries = new float[numberOfPoints + 1];

        domainEntries[0] = t0;
        for (int i = 1, max = domainEntries.length; i < max; i++) {
            domainEntries[i] = domainEntries[i - 1] + offset;
        }
        domainEntries[domainEntries.length - 1] = t1;
        return domainEntries;
    }

    /**
     * Calculate the colours value of the point xy on the line point1 and point2.
     *
     * @param colorSpace colour space to apply to the function output
     * @param xy         point to calcualte the colour of.
     * @param point1     start of gradient line
     * @param point2     end of gradient line.
     * @param t0         domain min
     * @param t1         domain max
     * @return colour derived from the input parameters.
     */
    private Color calculateColour(PColorSpace colorSpace, Point2D.Float xy,
                                  Point2D.Float point1, Point2D.Float point2,
                                  float t0, float t1) {
        // find colour at point 1
        float xPrime = linearMapping(xy, point1, point2);
        float t = parametrixValue(xPrime, t0, t1, extend);
        // find colour at point 2
        float[] input = new float[1];
        input[0] = t;
        // apply the function to the given input
        if (function != null) {
            float[] output = calculateValues(input);
            if (output != null) {
                output = PColorSpace.reverse(output);
                return colorSpace.getColor(output, true);
            } else {
                return null;
            }

        } else {
            logger.fine("Error processing Shading Type 2 Pattern.");
            return null;
        }

    }

    /**
     * Colour blend function to be applied to a point on the line with endpoints
     * point1 and point1 for a given point x,y.
     *
     * @param xy     point to linearize.
     * @param point1 end point of line
     * @param point2 end poitn of line.
     * @return linearized x' value.
     */
    private float linearMapping(Point2D.Float xy, Point2D.Float point1, Point2D.Float point2) {
        float x = xy.x;
        float y = xy.y;
        float x0 = point1.x;
        float y0 = point1.y;
        float x1 = point2.x;
        float y1 = point2.y;
        float top = (((x1 - x0) * (x - x0)) + ((y1 - y0) * (y - y0)));
        float bottom = (((x1 - x0) * (x1 - x0)) + ((y1 - y0) * (y1 - y0)));
        // have a couple corner cases where 1.00000046 isn't actually 1.0
        // so I'm going to tweak the calculation to have 3 decimals.
        int map = (int) ((top / bottom) * 100);
        return map / 100.0f;
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

        if (linearMapping < 0 && ((Boolean) extended.get(0))) {
            return t0;
        } else if (linearMapping > 1 && ((Boolean) extended.get(1))) {
            return t1;
        } else {
            return t0 + ((t1 - t0) * linearMapping);
        }
    }

    public Paint getPaint() {
        try {
            init();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.fine("ShadingType2Pattern initialization interrupted");
        }
        return linearGradientPaint;
    }

    public String toString() {
        return super.toString() +
                "\n                    domain: " + domain +
                "\n                    coords: " + coords +
                "\n                    extend: " + extend +
                "\n                 function: " + function;
    }
}