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
package org.icepdf.core.pobjects.functions;

import org.icepdf.core.pobjects.Dictionary;
import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.Reference;
import org.icepdf.core.util.Library;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * <p>The class <code>Function</code> is factory responsible for creating the correct
 * function type for the given "FunctionType" dictionary entry.</p>
 * <p/>
 * <p>Functions in PDF represent static, self-contained numerical transformations.
 * In general, a function can take any number (m) of input values and produce any
 * number (n) of output values:
 * <ul>
 * f(x<sub>0</sub>,..., x<sub>m-1</sub>) = y<sub>0</sub>, ... , y<sub>n-1</sub>
 * </ul>
 * <p>In PDF functions, all the input values and all the output values are numbers.
 * Each function definition includes a <code>domain</code>, the set of legal
 * values for the input.  Some functions also define a <code>range</code>, the
 * set of legal values for the output. Input and output values are clipped to
 * the respective <code>domain</code> and <code>range</code>.
 * </p>
 * <p/>
 * <p>This function factory currently support the following function types:</p>
 * <ul>
 * <li><b>type 0</b> - sampled function, uses a table of sample values to define the function.
 * various techniques are used to interpolate values between the sampled values.
 * </li>
 * <li><b>type 2</b> - exponential interpolation, defines a set of
 * coefficients for an exponential function.
 * </li>
 * <li><b>type 3</b> - stitching function, a combination of
 * other functions, partitioned across a domain.
 * </li>
 * <li><b>type 4</b> - calculator function, uses operators from
 * the PostScript language do describe an arithmetic expression.
 * </li>
 * </u>
 *
 * @since 1.0
 */
public abstract class Function {

    private static final Logger logger =
            Logger.getLogger(Function.class.toString());

    public static final Name FUNCTIONTYPE_NAME = new Name("FunctionType");
    public static final Name DOMAIN_NAME = new Name("Domain");
    public static final Name RANGE_NAME = new Name("Range");

    /**
     * An array of 2 x m numbers, where m is the number of input values.  Input
     * values outside the declared domain are clipped to the nearest boundary value.
     */
    protected float[] domain;

    /**
     * An array of 2 x n numbers, where n is the number of output values.  Output
     * values outside the declared range are clipped to the nearest boundary value.
     * If this entry is absent, no clipping is done.
     */
    protected float[] range;

    /**
     * Function type associated with this function.
     */
    protected int functionType;

    /**
     * <p>Creates a new instance of a Function object.  Possible function types
     * are:</p>
     * <ul>
     * <li>0 - sampled funciton.</li>
     * <li>2 - exponential interpolation funciton.</li>
     * </ul>
     *
     * @param l document library.
     * @param o dictionary or Hashmap containing Function type entries.
     * @return Function object for the specified function type, null if the
     *         function type is not available or not defined.
     */
    public static Function getFunction(Library l, Object o) {
        Dictionary d = null;

        if (o instanceof Reference) {
            o = l.getObject((Reference) o);
        }

        // create a dictionary out of the object if possible
        if (o instanceof Dictionary) {
            d = (Dictionary) o;
        } else if (o instanceof HashMap) {
            d = new Dictionary(l, (HashMap) o);
        }

        if (d != null) {
            // find out what time of function type and create the appropriate
            // function object.
            int fType = d.getInt(FUNCTIONTYPE_NAME);
            switch (fType) {
                // sampled function
                case 0:
                    return new Function_0(d);
                // exponential interpolation
                case 2:
                    return new Function_2(d);
                // stitching function
                case 3:
                    return new Function_3(d);
                // PostScript calculator
                case 4:
                    return new Function_4(d);
            }
        }
        return null;
    }

    /**
     * Creates a new instance of <code>Function</code> object.
     *
     * @param d dictionary containing a vaild function dictionary.
     */
    protected Function(Dictionary d) {
        List dom = (List) d.getObject(DOMAIN_NAME);
        domain = new float[dom.size()];
        for (int i = 0; i < dom.size(); i++) {
            domain[i] = ((Number) dom.get(i)).floatValue();
        }
        List r = (List) d.getObject(RANGE_NAME);
        if (r != null) {
            range = new float[r.size()];
            for (int i = 0; i < r.size(); i++) {
                range[i] = ((Number) r.get(i)).floatValue();
            }
        }
    }

    /**
     * <p>Gets the function type number.
     * <ul>
     * <li><b>type 0</b> - sampled function, uses a table of sample values to define the function.
     * various techniques are used to interpolate values between the sampled values.
     * </li>
     * <li><b>type 2</b>  - exponential interpolation, defines a set of
     * coeffiecients for an exponential function.
     * </li>
     * <li><b>type 3</b>  - stitching function, a combination of
     * other functions, partitioned across a domain.
     * </li>
     * <li><b>type 4</b> - calculator function, uses operators from
     * the PostScript language do describe an arithmetic expression.
     * </li>
     * </u>
     */
    public int getFunctionType() {
        return functionType;
    }

    /**
     * <p>Interpolation function.  For the given value of x, the interpolate
     * calculates the y value on the line defined by the two points
     * (x<sub>min</sub>, y<sub>min</sub>) and (x<sub>max</sub>, y<sub>max</sub>).
     *
     * @param x    value we want to find a y value for.
     * @param xmin point 1, x value.
     * @param xmax point 2, x value.
     * @param ymin point 1, y value.
     * @param ymax oint 2, y value.
     * @return y value for the given x value on the point define by
     *         (x<sub>min</sub>, y<sub>min</sub>) and (x<sub>max</sub>, y<sub>max</sub>).
     */
    public static float interpolate(float x, float xmin, float xmax, float ymin, float ymax) {
        return ((x - xmin) * (ymax - ymin) / (xmax - xmin)) + ymin;
    }

    /**
     * <p>Evaluates the input values specified by <code>m</code>. In general, a
     * function can take any number (m) of input values and produce any
     * number (n) of output values:
     * <ul>
     * f(x<sub>0</sub>,..., x<sub>m-1</sub>) = y<sub>0</sub>, ... , y<sub>n-1</sub>
     * </ul>
     *
     * @param m input values to put through function.
     * @return n output values.
     */
    public abstract float[] calculate(float[] m);

    public float[] getDomain() {
        return domain;
    }

    public float[] getRange() {
        return range;
    }
}
