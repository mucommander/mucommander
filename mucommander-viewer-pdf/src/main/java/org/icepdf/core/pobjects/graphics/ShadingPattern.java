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

import org.icepdf.core.pobjects.Dictionary;
import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.Reference;
import org.icepdf.core.pobjects.Stream;
import org.icepdf.core.pobjects.functions.Function;
import org.icepdf.core.util.Library;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Shading Pattern is a Base class for a all shading Types.  It contains
 * all common dictionary entries and acts a factory examing the patternType
 * entry and returning a know Pattern Type implementation.  Currently the
 * factory only support Shading Type2 and Type3 patterns, as thses are the only
 * types we have concrete examples of. </p>
 *
 * @author ICEsoft Technologies Inc.
 * @since 3.0
 */
public abstract class ShadingPattern extends Dictionary implements Pattern {

    private static final Logger logger =
            Logger.getLogger(ShadingPattern.class.toString());

    public static final Name PATTERN_TYPE_KEY = new Name("PatternType");
    public static final Name EXTGSTATE_KEY = new Name("ExtGState");
    public static final Name MATRIX_KEY = new Name("Matrix");
    public static final Name SHADING_KEY = new Name("Shading");
    public static final Name SHADING_TYPE_KEY = new Name("ShadingType");
    public static final Name BBOX_KEY = new Name("BBox");
    public static final Name COLORSPACE_KEY = new Name("ColorSpace");
    public static final Name BACKGROUND_KEY = new Name("Background");
    public static final Name ANTIALIAS_KEY = new Name("AntiAlias");
    public static final Name DOMAIN_KEY = new Name("Domain");
    public static final Name COORDS_KEY = new Name("Coords");
    public static final Name EXTEND_KEY = new Name("Extend");
    public static final Name FUNCTION_KEY = new Name("Function");
    // pattern types by number.
    public static final int SHADING_PATTERN_TYPE_1 = 1;
    public static final int SHADING_PATTERN_TYPE_2 = 2;
    public static final int SHADING_PATTERN_TYPE_3 = 3;
    public static final int SHADING_PATTERN_TYPE_4 = 4;
    public static final int SHADING_PATTERN_TYPE_5 = 5;
    public static final int SHADING_PATTERN_TYPE_6 = 6;
    public static final int SHADING_PATTERN_TYPE_7 = 7;

    // type of PObject, should always be "Pattern"
    protected Name type;

    // A code identifying the type of pattern that this dictionary describes
    protected int patternType;

    // shadingDictionary dictionary, entries vary depending on shading type.
    protected HashMap shadingDictionary;

    // shadingDictionary type 1-7,  most common, 2,3,6..
    protected int shadingType;

    // start of common shading dictionary entries.

    // A 1-in, n-out function or an array of n 1-in, 1-out functions (where n
    // is the number of colour components in the shadingDictionary dictionary's colour
    // space).  The function(s) are called with values of the parametric variables
    // t in the domain defined by the domain entry.  Each function's domain must
    // be a superset of that of the shadingDictionary dictionary.  If the return value
    // is out of range it is adjusted to the nearest value.
    protected Function[] function;

    // An array of four numbers in the pattern coordinate system giving the
    // coordinates of the left, bottom, right, and top edges, respectively, of
    // the pattern cell's bounding box. These boundaries are used to clip the
    // pattern cell.
    protected Rectangle2D bBox;

    // any device, cie-based or special color except Pattern, required.
    protected PColorSpace colorSpace;

    // background colors (optional), not applicable on 'sh'
    protected List background;

    // turn on/off antiAliasing.  (optional)
    protected boolean antiAlias;

    // end of common shading dictionary entries.

    // An array of six numbers specifying the pattern matrix. The default value
    // is the identity matrix [1 0 0 1 0 0].
    protected AffineTransform matrix;

    // graphics state for shading pattern
    protected ExtGState extGState;

    //  initiated flag
    protected boolean inited;

    public ShadingPattern(Library library, HashMap entries) {
        super(library, entries);

        type = library.getName(entries, TYPE_KEY);

        patternType = library.getInt(entries, PATTERN_TYPE_KEY);

        Object attribute = library.getObject(entries, EXTGSTATE_KEY);
        if (attribute instanceof HashMap) {
            extGState = new ExtGState(library, (HashMap) attribute);
        } else if (attribute instanceof Reference) {
            extGState = new ExtGState(library,
                    (HashMap) library.getObject(
                            (Reference) attribute));
        }

        List v = (List) library.getObject(entries, MATRIX_KEY);
        if (v != null) {
            matrix = getAffineTransform(v);
        } else {
            // default is identity matrix
            matrix = new AffineTransform();
        }
    }

    /**
     * Factory method to resolve the shading dictionaries ShadingType 1 - 3.
     *
     * @param library   library for document
     * @param attribute dictionary for potential shading object.
     * @return returns a Shading object based ont he shadingType criteria.
     * if the proper constructor cannot be found then null is returned.
     */
    public static ShadingPattern getShadingPattern(Library library, HashMap attribute) {
        // factory type approach, find shading entries and get type
        Object shading = library.getObject(attribute, SHADING_KEY);
        // setup 1-3 as they are dictionary based.
        if (shading != null && shading instanceof HashMap) {
            return shadingFactory(library, attribute, (HashMap) shading);
        } else if (shading != null && shading instanceof Stream) {
            return getShadingPattern(library, attribute, (Stream) shading);
        }
        return null;
    }

    /**
     * Factory method to resolve the shadingDictionary dictionaries ShadingType 4- 6 which are Stream based.
     *
     * @param library   library for document
     * @param attribute dictionary for potential shadingDictionary object.
     * @return returns a ShadingPattern object based ont he shadingType criteria.
     * if the proper constructor cannot be found then null is returned.
     */
    public static ShadingPattern getShadingPattern(Library library, HashMap attribute, Stream shadingStream) {
        int shadingType = library.getInt(shadingStream.getEntries(), SHADING_TYPE_KEY);
        switch (shadingType) {
            case SHADING_PATTERN_TYPE_4:
                return new ShadingType4Pattern(library, attribute, shadingStream);
            case SHADING_PATTERN_TYPE_5:
                return new ShadingType5Pattern(library, attribute, shadingStream);
            case SHADING_PATTERN_TYPE_6:
                return new ShadingType6Pattern(library, attribute, shadingStream);
            case SHADING_PATTERN_TYPE_7:
                return new ShadingType7Pattern(library, attribute, shadingStream);
        }
        return null;
    }

    /**
     * Factory call create a support pattern type.  Currently only types 2 and
     * 3 are supported.
     *
     * @param library document library
     * @param entries entries in the the currently dictionary.
     * @param shading shading dictionary.
     * @return shading pattern
     */
    public static ShadingPattern getShadingPattern(Library library,
                                                   HashMap entries,
                                                   HashMap shading) {
        // resolve shading pattern
        if (entries != null) {
            ShadingPattern shadingPattern = shadingFactory(library, shading, shading);
            // assign shading dictionary for sh instances that only define
            // the shading dictionary and not the full pattern dictionary.
            shadingPattern.setShadingDictionary(shading);
            return shadingPattern;
        }

        return null;
    }

    // create a new shading pattern.
    private static ShadingPattern shadingFactory(Library library,
                                                 HashMap attribute,
                                                 HashMap patternDictionary) {
        int shadingType = library.getInt(patternDictionary, SHADING_TYPE_KEY);
        if (shadingType == ShadingPattern.SHADING_PATTERN_TYPE_2) {
            return new ShadingType2Pattern(library, attribute);
        } else if (shadingType == ShadingPattern.SHADING_PATTERN_TYPE_3) {
            return new ShadingType3Pattern(library, attribute);
        } else if (shadingType == ShadingPattern.SHADING_PATTERN_TYPE_1) {
            return new ShadingType1Pattern(library, attribute);
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Shading pattern of Type " + shadingType +
                        " are not currently supported");
            }
        }
        return null;
    }

    /**
     * Utility method for parsing a vector of affinetranform values to an
     * affine transform.
     *
     * @param v vector containing affine transform values.
     * @return affine transform based on v
     */
    private static AffineTransform getAffineTransform(List v) {
        float f[] = new float[6];
        for (int i = 0; i < 6; i++) {
            f[i] = ((Number) v.get(i)).floatValue();
        }
        return new AffineTransform(f);
    }

    /**
     * Applies the function data to the values array.
     * @param values values to feed functions.
     * @return new values after function execution.
     */
    protected float[] calculateValues(float[] values) {
        float[] output;
        int length = function.length;
        if (length == 1) {
            output = function[0].calculate(values);
        } else {
            // vector of function for each colour component, 1 in 1 out.
            output = new float[length];
            for (int i = 0; i < length; i++) {
                output[i] = function[i].calculate(values)[0];
            }
        }
        return output;
    }

    /**
     * Gets the Paint object need to fill a shape etc.  Each individual
     * implementation will return a particular paint type.
     *
     * @return Paint type for fill.
     */
    public abstract Paint getPaint() throws InterruptedException;

    /**
     * Initialized shading dictionary attributes. Discrepancies between sh and
     * scn tokens cause us to handle initialization at a later time.
     */
    public abstract void init(GraphicsState graphicsState);

    public void setParentGraphicState(GraphicsState graphicsState) {
        // nothing to be done for shading.
    }

    public void setMatrix(AffineTransform matrix) {
        this.matrix = matrix;
    }

    public int getPatternType() {
        return patternType;
    }

    public Rectangle2D getBBox() {
        return bBox;
    }

    public AffineTransform getMatrix() {
        return matrix;
    }

    public int getShadingType() {
        return shadingType;
    }

    public void setShadingDictionary(HashMap shadingDictionary) {
        this.shadingDictionary = shadingDictionary;
    }

    public Name getType() {
        return type;
    }

    public PColorSpace getColorSpace() {
        return colorSpace;
    }

    public List getBackground() {
        return background;
    }

    public boolean isAntiAlias() {
        return antiAlias;
    }

    public ExtGState getExtGState() {
        return extGState;
    }

    public boolean isInited() {
        return inited;
    }

    public String toString() {
        return "Shading Pattern: \n" +
                "           type: pattern " +
                "\n    patternType: shadingDictionary" +
                "\n         matrix: " + matrix +
                "\n      extGState: " + extGState +
                "\n        shading dictionary: " + shadingDictionary +
                "\n               shadingType: " + shadingType +
                "\n               colourSpace: " + colorSpace +
                "\n                background: " + background +
                "\n                      bbox: " + bBox +
                "\n                 antiAlias: " + antiAlias;
    }
}
