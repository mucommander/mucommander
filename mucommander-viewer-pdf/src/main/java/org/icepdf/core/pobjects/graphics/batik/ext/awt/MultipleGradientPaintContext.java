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

package org.icepdf.core.pobjects.graphics.batik.ext.awt;

import org.icepdf.core.pobjects.graphics.batik.ext.awt.image.GraphicsUtil;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.lang.ref.WeakReference;


/**
 * This is the superclass for all PaintContexts which use a multiple color
 * gradient to fill in their raster. It provides the actual color interpolation
 * functionality.  Subclasses only have to deal with using the gradient to fill
 * pixels in a raster.
 *
 * @author Nicholas Talian, Vincent Hardy, Jim Graham, Jerry Evans
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id: MultipleGradientPaintContext.java,v 1.1 2008/09/30 20:44:16 patrickc Exp $
 */
abstract class MultipleGradientPaintContext implements PaintContext {

    protected static final boolean DEBUG = false;

    /**
     * The color model data is generated in (always un premult).
     */
    protected ColorModel dataModel;
    /**
     * PaintContext's output ColorModel ARGB if colors are not all
     * opaque, RGB otherwise.  Linear and premult are matched to
     * output ColorModel.
     */
    protected ColorModel model;

    /**
     * Color model used if gradient colors are all opaque
     */
    private static ColorModel lrgbmodel_NA = new DirectColorModel
            (ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB),
                    24, 0xff0000, 0xFF00, 0xFF, 0x0,
                    false, DataBuffer.TYPE_INT);

    private static ColorModel srgbmodel_NA = new DirectColorModel
            (ColorSpace.getInstance(ColorSpace.CS_sRGB),
                    24, 0xff0000, 0xFF00, 0xFF, 0x0,
                    false, DataBuffer.TYPE_INT);

    private static ColorModel graybmodel_NA =
            new ComponentColorModel(ColorSpace.getInstance(
                    ColorSpace.CS_GRAY), new int[]{1}, false, false,
                    ColorModel.OPAQUE, DataBuffer.TYPE_INT);

    /**
     * Color model used if some gradient colors are transparent
     */
    private static ColorModel lrgbmodel_A = new DirectColorModel
            (ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB),
                    32, 0xff0000, 0xFF00, 0xFF, 0xFF000000,
                    false, DataBuffer.TYPE_INT);

    private static ColorModel srgbmodel_A = new DirectColorModel
            (ColorSpace.getInstance(ColorSpace.CS_sRGB),
                    32, 0xff0000, 0xFF00, 0xFF, 0xFF000000,
                    false, DataBuffer.TYPE_INT);

    private static ColorModel graybmodel_A =
            new ComponentColorModel(ColorSpace.getInstance(
                    ColorSpace.CS_GRAY), new int[]{1, 1}, true, false,
                    ColorModel.TRANSLUCENT, DataBuffer.TYPE_INT);

    /**
     * The cached colorModel
     */
    protected static ColorModel cachedModel;

    /**
     * The cached raster, which is reusable among instances
     */
    protected static WeakReference<WritableRaster> cached;

    /**
     * Raster is reused whenever possible
     */
    protected WritableRaster saved;

    /**
     * The method to use when painting out of the gradient bounds.
     */
    protected MultipleGradientPaint.CycleMethodEnum cycleMethod;

    /**
     * The colorSpace in which to perform the interpolation
     */
    protected MultipleGradientPaint.ColorSpaceEnum colorSpace;

    /**
     * Elements of the inverse transform matrix.
     */
    protected float a00, a01, a10, a11, a02, a12;

    /**
     * This boolean specifies wether we are in simple lookup mode, where an
     * input value between 0 and 1 may be used to directly index into a single
     * array of gradient colors.  If this boolean value is false, then we have
     * to use a 2-step process where we have to determine which gradient array
     * we fall into, then determine the index into that array.
     */
    protected boolean isSimpleLookup = true;

    /**
     * This boolean indicates if the gradient appears to have sudden
     * discontinuities in it, this may be because of multiple stops
     * at the same location or use of the REPEATE mode.
     */
    protected boolean hasDiscontinuity = false;

    /**
     * Size of gradients array for scaling the 0-1 index when looking up
     * colors the fast way.
     */
    protected int fastGradientArraySize;

    /**
     * Array which contains the interpolated color values for each interval,
     * used by calculateSingleArrayGradient().  It is protected for possible
     * direct access by subclasses.
     */
    protected int[] gradient;

    /**
     * Array of gradient arrays, one array for each interval.  Used by
     * calculateMultipleArrayGradient().
     */
    protected int[][] gradients;

    /**
     * This holds the blend of all colors in the gradient.
     * we use this at extreamly low resolutions to ensure we
     * get a decent blend of the colors.
     */
    protected int gradientAverage;

    /**
     * This holds the color to use when we are off the bottom of the
     * gradient
     */
    protected int gradientUnderflow;

    /**
     * This holds the color to use when we are off the top of the
     * gradient
     */
    protected int gradientOverflow;

    /**
     * Length of the 2D slow lookup gradients array.
     */
    protected int gradientsLength;

    /**
     * Normalized intervals array
     */
    protected float[] normalizedIntervals;

    /**
     * fractions array
     */
    protected float[] fractions;

    /**
     * Used to determine if gradient colors are all opaque
     */
    private int transparencyTest;

    /**
     * Colorspace conversion lookup tables
     */
    private static final int[] SRGBtoLinearRGB = new int[256];
    private static final int[] LinearRGBtoSRGB = new int[256];

    //build the tables
    static {
        for (int k = 0; k < 256; k++) {
            SRGBtoLinearRGB[k] = convertSRGBtoLinearRGB(k);
            LinearRGBtoSRGB[k] = convertLinearRGBtoSRGB(k);
        }
    }

    /**
     * Constant number of max colors between any 2 arbitrary colors.
     * Used for creating and indexing gradients arrays.
     */
    protected static final int GRADIENT_SIZE = 256;
    protected static final int GRADIENT_SIZE_INDEX = GRADIENT_SIZE - 1;

    /**
     * Maximum length of the fast single-array.  If the estimated array size
     * is greater than this, switch over to the slow lookup method.
     * No particular reason for choosing this number, but it seems to provide
     * satisfactory performance for the common case (fast lookup).
     */
    private static final int MAX_GRADIENT_ARRAY_SIZE = 5000;

    /**
     * Constructor for superclass. Does some initialization, but leaves most
     * of the heavy-duty math for calculateGradient(), so the subclass may do
     * some other manipulation beforehand if necessary.  This is not possible
     * if this computation is done in the superclass constructor which always
     * gets called first.
     */
    protected MultipleGradientPaintContext(ColorModel cm,
                                           Rectangle deviceBounds,
                                           Rectangle2D userBounds,
                                           AffineTransform t,
                                           RenderingHints hints,
                                           float[] fractions,
                                           Color[] colors,
                                           MultipleGradientPaint.CycleMethodEnum
                                                   cycleMethod,
                                           MultipleGradientPaint.ColorSpaceEnum
                                                   colorSpace)
            throws NoninvertibleTransformException {
        //We have to deal with the cases where the 1st gradient stop is not
        //equal to 0 and/or the last gradient stop is not equal to 1.
        //In both cases, create a new point and replicate the previous
        //extreme point's color.

        boolean fixFirst = false;
        boolean fixLast = false;
        int len = fractions.length;

        //if the first gradient stop is not equal to zero, fix this condition
        if (fractions[0] != 0f) {
            fixFirst = true;
            len++;
        }

        //if the last gradient stop is not equal to one, fix this condition
        if (fractions[fractions.length - 1] != 1.0f) {
            fixLast = true;
            len++;
        }

        for (int i = 0; i < fractions.length - 1; i++)
            if (fractions[i] == fractions[i + 1])
                len--;

        this.fractions = new float[len];
        Color[] loColors = new Color[len - 1];
        Color[] hiColors = new Color[len - 1];
        normalizedIntervals = new float[len - 1];

        gradientUnderflow = colors[0].getRGB();
        gradientOverflow = colors[colors.length - 1].getRGB();

        int idx = 0;
        if (fixFirst) {
            this.fractions[0] = 0;
            loColors[0] = colors[0];
            hiColors[0] = colors[0];
            normalizedIntervals[0] = fractions[0];
            idx++;
        }

        for (int i = 0; i < fractions.length - 1; i++) {
            if (fractions[i] == fractions[i + 1]) {
                // System.out.println("EQ Fracts");
                if (!colors[i].equals(colors[i + 1])) {
                    hasDiscontinuity = true;
                }
                continue;
            }
            this.fractions[idx] = fractions[i];
            loColors[idx] = colors[i];
            hiColors[idx] = colors[i + 1];
            normalizedIntervals[idx] = fractions[i + 1] - fractions[i];
            idx++;
        }

        this.fractions[idx] = fractions[fractions.length - 1];

        if (fixLast) {
            loColors[idx] = hiColors[idx] = colors[colors.length - 1];
            normalizedIntervals[idx] = 1 - fractions[fractions.length - 1];
            idx++;
            this.fractions[idx] = 1;
        }

        // The inverse transform is needed to from device to user space.
        // Get all the components of the inverse transform matrix.
        AffineTransform tInv = t.createInverse();

        double[] m = new double[6];
        tInv.getMatrix(m);
        a00 = (float) m[0];
        a10 = (float) m[1];
        a01 = (float) m[2];
        a11 = (float) m[3];
        a02 = (float) m[4];
        a12 = (float) m[5];

        //copy some flags
        this.cycleMethod = cycleMethod;
        this.colorSpace = colorSpace;

        // Setup an example Model, we may refine it later.
        if (cm.getColorSpace() == lrgbmodel_A.getColorSpace())
            dataModel = lrgbmodel_A;
        else if (cm.getColorSpace() == srgbmodel_A.getColorSpace())
            dataModel = srgbmodel_A;
        else if (cm.getColorSpace() == graybmodel_A.getColorSpace())
            dataModel = srgbmodel_A;
        else
            throw new IllegalArgumentException
                    ("Unsupported ColorSpace for interpolation");

        calculateGradientFractions(loColors, hiColors);

        model = GraphicsUtil.coerceColorModel(dataModel,
                cm.isAlphaPremultiplied());
    }


    /**
     * This function is the meat of this class.  It calculates an array of
     * gradient colors based on an array of fractions and color values at those
     * fractions.
     */
    protected final void calculateGradientFractions
    (Color[] loColors, Color[] hiColors) {

        //if interpolation should occur in Linear RGB space, convert the
        //colors using the lookup table
        if (colorSpace == LinearGradientPaint.LINEAR_RGB) {
            int[] workTbl = SRGBtoLinearRGB; // local is cheaper

            for (int i = 0; i < loColors.length; i++) {

                loColors[i] = interpolateColor(workTbl, loColors[i]);

                hiColors[i] = interpolateColor(workTbl, hiColors[i]);

            }
        }

        //initialize to be fully opaque for ANDing with colors
        transparencyTest = 0xff000000;
        if (cycleMethod == MultipleGradientPaint.NO_CYCLE) {
            // Include overflow and underflow colors in transparency
            // test.
            transparencyTest &= gradientUnderflow;
            transparencyTest &= gradientOverflow;
        }

        //array of interpolation arrays
        gradients = new int[fractions.length - 1][];
        gradientsLength = gradients.length;

        // TODO ??? whats going on here
        // ??? the following comments and the name Imin suggest, that we search for something small
        // ??? but the for-loop actually looks for the LARGEST value

        // Find smallest interval
        int n = normalizedIntervals.length;

        float Imin = 1;
        float[] workTbl = normalizedIntervals;   // local is cheaper
        for (int i = 0; i < n; i++) {
            // ??? find the LARGEST value in normalizedIntervals
            Imin = (Imin > workTbl[i]) ? workTbl[i] : Imin;
        }

        //estimate the size of the entire gradients array.
        //This is to prevent a tiny interval from causing the size of array to
        //explode.  If the estimated size is too large, break to using
        //seperate arrays for each interval, and using an indexing scheme at
        //look-up time.
        int estimatedSize = 0;

        if (Imin == 0) {
            estimatedSize = Integer.MAX_VALUE;
            hasDiscontinuity = true;
        } else {
            for (int i = 0; i < workTbl.length; i++) {
                estimatedSize += (workTbl[i] / Imin) * GRADIENT_SIZE;
            }
        }


        if (estimatedSize > MAX_GRADIENT_ARRAY_SIZE) {
            //slow method
            calculateMultipleArrayGradient(loColors, hiColors);
            if ((cycleMethod == MultipleGradientPaint.REPEAT) &&
                    (gradients[0][0] !=
                            gradients[gradients.length - 1][GRADIENT_SIZE_INDEX]))
                hasDiscontinuity = true;
        } else {
            //fast method
            calculateSingleArrayGradient(loColors, hiColors, Imin);
            if ((cycleMethod == MultipleGradientPaint.REPEAT) &&
                    (gradient[0] != gradient[fastGradientArraySize]))
                hasDiscontinuity = true;
        }

        // Use the most 'economical' model (no alpha).
        if ((transparencyTest >>> 24) == 0xff) {
            if (dataModel.getColorSpace() == lrgbmodel_NA.getColorSpace())
                dataModel = lrgbmodel_NA;
            else if (dataModel.getColorSpace() == srgbmodel_NA.getColorSpace())
                dataModel = srgbmodel_NA;
            else if (dataModel.getColorSpace() == graybmodel_NA.getColorSpace())
                dataModel = graybmodel_NA;
            model = dataModel;
        }
    }

    /**
     * We assume, that we always generate valid colors. When this is valid, we can compose the
     * color-value by ourselves and use the faster Color-ctor, which does not check the incoming values.
     *
     * @param workTbl typically SRGBtoLinearRGB
     * @param inColor the color to interpolate
     * @return the interpolated color
     */
    private static Color interpolateColor(int[] workTbl, Color inColor) {

        int oldColor = inColor.getRGB();

        int newColorValue =
                ((workTbl[(oldColor >> 24) & 0xff] & 0xff) << 24) |
                        ((workTbl[(oldColor >> 16) & 0xff] & 0xff) << 16) |
                        ((workTbl[(oldColor >> 8) & 0xff] & 0xff) << 8) |
                        ((workTbl[(oldColor) & 0xff] & 0xff));

        return new Color(newColorValue, true);
    }

    /**
     * FAST LOOKUP METHOD
     * <p/>
     * This method calculates the gradient color values and places them in a
     * single int array, gradient[].  It does this by allocating space for
     * each interval based on its size relative to the smallest interval in
     * the array.  The smallest interval is allocated 255 interpolated values
     * (the maximum number of unique in-between colors in a 24 bit color
     * system), and all other intervals are allocated
     * size = (255 * the ratio of their size to the smallest interval).
     * <p/>
     * This scheme expedites a speedy retrieval because the colors are
     * distributed along the array according to their user-specified
     * distribution.  All that is needed is a relative index from 0 to 1.
     * <p/>
     * The only problem with this method is that the possibility exists for
     * the array size to balloon in the case where there is a
     * disproportionately small gradient interval.  In this case the other
     * intervals will be allocated huge space, but much of that data is
     * redundant.  We thus need to use the space conserving scheme below.
     *
     * @param Imin the size of the smallest interval
     */
    private void calculateSingleArrayGradient
    (Color[] loColors, Color[] hiColors, float Imin) {

        //set the flag so we know later it is a non-simple lookup
        isSimpleLookup = true;

        int gradientsTot = 1; //the eventual size of the single array

        // These are fixed point 8.16 (start with 0.5)
        int aveA = 0x008000;
        int aveR = 0x008000;
        int aveG = 0x008000;
        int aveB = 0x008000;

        //for every interval (transition between 2 colors)
        for (int i = 0; i < gradients.length; i++) {

            //create an array whose size is based on the ratio to the
            //smallest interval.
            int nGradients = (int) ((normalizedIntervals[i] / Imin) * 255f);
            gradientsTot += nGradients;
            gradients[i] = new int[nGradients];

            //the the 2 colors (keyframes) to interpolate between
            int rgb1 = loColors[i].getRGB();
            int rgb2 = hiColors[i].getRGB();

            //fill this array with the colors in between rgb1 and rgb2
            interpolate(rgb1, rgb2, gradients[i]);

            // Calculate Average of two colors...
            int argb = gradients[i][GRADIENT_SIZE / 2];
            float norm = normalizedIntervals[i];
            aveA += (int) (((argb >> 8) & 0xFF0000) * norm);
            aveR += (int) (((argb) & 0xFF0000) * norm);
            aveG += (int) (((argb << 8) & 0xFF0000) * norm);
            aveB += (int) (((argb << 16) & 0xFF0000) * norm);

            //if the colors are opaque, transparency should still be 0xff000000
            transparencyTest &= rgb1 & rgb2;
        }

        gradientAverage = (((aveA & 0xFF0000) << 8) |
                ((aveR & 0xFF0000)) |
                ((aveG & 0xFF0000) >> 8) |
                ((aveB & 0xFF0000) >> 16));

        // Put all gradients in a single array
        gradient = new int[gradientsTot];
        int curOffset = 0;
        for (int i = 0; i < gradients.length; i++) {
            System.arraycopy(gradients[i], 0, gradient,
                    curOffset, gradients[i].length);
            curOffset += gradients[i].length;
        }
        gradient[gradient.length - 1] = hiColors[hiColors.length - 1].getRGB();

        //if interpolation occurred in Linear RGB space, convert the
        //gradients back to SRGB using the lookup table
        if (colorSpace == LinearGradientPaint.LINEAR_RGB) {
            if (dataModel.getColorSpace() ==
                    ColorSpace.getInstance(ColorSpace.CS_sRGB)) {
                for (int i = 0; i < gradient.length; i++) {
                    gradient[i] =
                            convertEntireColorLinearRGBtoSRGB(gradient[i]);
                }
                gradientAverage =
                        convertEntireColorLinearRGBtoSRGB(gradientAverage);
            }
        } else {
            if (dataModel.getColorSpace() ==
                    ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB)) {
                for (int i = 0; i < gradient.length; i++) {
                    gradient[i] =
                            convertEntireColorSRGBtoLinearRGB(gradient[i]);
                }
                gradientAverage =
                        convertEntireColorSRGBtoLinearRGB(gradientAverage);
            }
        }

        fastGradientArraySize = gradient.length - 1;
    }


    /**
     * SLOW LOOKUP METHOD
     * <p/>
     * This method calculates the gradient color values for each interval and
     * places each into its own 255 size array.  The arrays are stored in
     * gradients[][].  (255 is used because this is the maximum number of
     * unique colors between 2 arbitrary colors in a 24 bit color system)
     * <p/>
     * This method uses the minimum amount of space (only 255 * number of
     * intervals), but it aggravates the lookup procedure, because now we
     * have to find out which interval to select, then calculate the index
     * within that interval.  This causes a significant performance hit,
     * because it requires this calculation be done for every point in
     * the rendering loop.
     * <p/>
     * For those of you who are interested, this is a classic example of the
     * time-space tradeoff.
     */
    private void calculateMultipleArrayGradient
    (Color[] loColors, Color[] hiColors) {

        //set the flag so we know later it is a non-simple lookup
        isSimpleLookup = false;

        int rgb1; //2 colors to interpolate
        int rgb2;

        // These are fixed point 8.16 (start with 0.5)
        int aveA = 0x008000;
        int aveR = 0x008000;
        int aveG = 0x008000;
        int aveB = 0x008000;

        //for every interval (transition between 2 colors)
        for (int i = 0; i < gradients.length; i++) {

            // This interval will never actually be used (zero size)
            if (normalizedIntervals[i] == 0)
                continue;

            //create an array of the maximum theoretical size for each interval
            gradients[i] = new int[GRADIENT_SIZE];

            //get the the 2 colors
            rgb1 = loColors[i].getRGB();
            rgb2 = hiColors[i].getRGB();

            //fill this array with the colors in between rgb1 and rgb2
            interpolate(rgb1, rgb2, gradients[i]);

            // Calculate Average of two colors...
            int argb = gradients[i][GRADIENT_SIZE / 2];
            float norm = normalizedIntervals[i];
            aveA += (int) (((argb >> 8) & 0xFF0000) * norm);
            aveR += (int) (((argb) & 0xFF0000) * norm);
            aveG += (int) (((argb << 8) & 0xFF0000) * norm);
            aveB += (int) (((argb << 16) & 0xFF0000) * norm);

            //if the colors are opaque, transparency should still be 0xff000000
            transparencyTest &= rgb1;
            transparencyTest &= rgb2;
        }

        gradientAverage = (((aveA & 0xFF0000) << 8) |
                ((aveR & 0xFF0000)) |
                ((aveG & 0xFF0000) >> 8) |
                ((aveB & 0xFF0000) >> 16));

        //if interpolation occurred in Linear RGB space, convert the
        //gradients back to SRGB using the lookup table
        if (colorSpace == LinearGradientPaint.LINEAR_RGB) {
            if (dataModel.getColorSpace() ==
                    ColorSpace.getInstance(ColorSpace.CS_sRGB)) {
                for (int j = 0; j < gradients.length; j++) {
                    for (int i = 0; i < gradients[j].length; i++) {
                        gradients[j][i] =
                                convertEntireColorLinearRGBtoSRGB(gradients[j][i]);
                    }
                }
                gradientAverage =
                        convertEntireColorLinearRGBtoSRGB(gradientAverage);
            }
        } else {
            if (dataModel.getColorSpace() ==
                    ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB)) {
                for (int j = 0; j < gradients.length; j++) {
                    for (int i = 0; i < gradients[j].length; i++) {
                        gradients[j][i] =
                                convertEntireColorSRGBtoLinearRGB(gradients[j][i]);
                    }
                }
                gradientAverage =
                        convertEntireColorSRGBtoLinearRGB(gradientAverage);
            }
        }
    }

    /**
     * Yet another helper function.  This one linearly interpolates between
     * 2 colors, filling up the output array.
     *
     * @param rgb1   the start color
     * @param rgb2   the end color
     * @param output the output array of colors... assuming this is not null or length 0.
     */
    private void interpolate(int rgb1, int rgb2, int[] output) {

        int nSteps = output.length;

        //step between interpolated values.
        float stepSize = 1 / (float) nSteps;

        //extract color components from packed integer
        int a1 = (rgb1 >> 24) & 0xff;
        int r1 = (rgb1 >> 16) & 0xff;
        int g1 = (rgb1 >> 8) & 0xff;
        int b1 = (rgb1) & 0xff;
        // calculate the total change in alpha, red, green, blue
        // the deltas can be negative !
        int da = ((rgb2 >> 24) & 0xff) - a1;
        int dr = ((rgb2 >> 16) & 0xff) - r1;
        int dg = ((rgb2 >> 8) & 0xff) - g1;
        int db = ((rgb2) & 0xff) - b1;

        // this method is a hotspot so we try to save some cycles
        // pre-compute some intermediate values.
        // the multiplication by 2 is used to help with rounding.
        float tempA = 2.0f * da * stepSize;
        float tempR = 2.0f * dr * stepSize;
        float tempG = 2.0f * dg * stepSize;
        float tempB = 2.0f * db * stepSize;

        //for each step in the interval calculate the in-between color by
        //multiplying the normalized current position by the total color change
        //(.5 is added to prevent truncation round-off error)

        // the previous implementation used a simple +0.5d to do some rounding.
        // but that is just rounding towards +inifitity. This results in
        // slightly different values (thus gradients) when you interpolate from
        //    color1 -> color2
        // versus
        //    color1 <- color2
        //
        // this implementation uses an implied multiplication by 2 ( in tempX )
        // and then a signed right-shift to do signed rounding.
        // this also spares a float-add per color-band.
        // we could also save the shift when we use a different and-mask and a different left-shift,
        // but that would obfuscate too much...
        //
        output[0] = rgb1;             // the start-color is fixed
        nSteps--;                       // upto, but not including the last slot
        output[nSteps] = rgb2;        // the last color is also fixed
        for (int i = 1; i < nSteps; i++) {
            output[i] =
                    ((a1 + ((((int) (i * tempA)) + 1) >> 1) & 0xff) << 24) |
                            ((r1 + ((((int) (i * tempR)) + 1) >> 1) & 0xff) << 16) |
                            ((g1 + ((((int) (i * tempG)) + 1) >> 1) & 0xff) << 8) |
                            ((b1 + ((((int) (i * tempB)) + 1) >> 1) & 0xff));
        }

    }


    /**
     * Yet another helper function.  This one extracts the color components
     * of an integer RGB triple, converts them from LinearRGB to SRGB, then
     * recompacts them into an int.
     */
    private static int convertEntireColorLinearRGBtoSRGB(int rgb) {

        //extract red, green, blue components
        int a1 = (rgb >> 24) & 0xff;
        int r1 = (rgb >> 16) & 0xff;
        int g1 = (rgb >> 8) & 0xff;
        int b1 = rgb & 0xff;

        //use the lookup table
        int[] workTbl = LinearRGBtoSRGB; // local is cheaper
        r1 = workTbl[r1];
        g1 = workTbl[g1];
        b1 = workTbl[b1];

        //re-compact the components
        return ((a1 << 24) |
                (r1 << 16) |
                (g1 << 8) |
                b1);
    }

    /**
     * Yet another helper function.  This one extracts the color components
     * of an integer RGB triple, converts them from LinearRGB to SRGB, then
     * recompacts them into an int.
     */
    private static int convertEntireColorSRGBtoLinearRGB(int rgb) {

        //extract red, green, blue components
        int a1 = (rgb >> 24) & 0xff;
        int r1 = (rgb >> 16) & 0xff;
        int g1 = (rgb >> 8) & 0xff;
        int b1 = rgb & 0xff;

        //use the lookup table
        int[] workTbl = SRGBtoLinearRGB; // local is cheaper
        r1 = workTbl[r1];
        g1 = workTbl[g1];
        b1 = workTbl[b1];

        //re-compact the components
        return ((a1 << 24) |
                (r1 << 16) |
                (g1 << 8) |
                b1);
    }


    /**
     * Helper function to index into the gradients array.  This is necessary
     * because each interval has an array of colors with uniform size 255.
     * However, the color intervals are not necessarily of uniform length, so
     * a conversion is required.
     *
     * @param position the unmanipulated position.  want to map this into the
     *                 range 0 to 1
     * @return integer color to display
     */
    protected final int indexIntoGradientsArrays(float position) {

        //first, manipulate position value depending on the cycle method.

        if (cycleMethod == MultipleGradientPaint.NO_CYCLE) {

            if (position >= 1) { //upper bound is 1
                return gradientOverflow;
            } else if (position <= 0) { //lower bound is 0
                return gradientUnderflow;
            }
        } else if (cycleMethod == MultipleGradientPaint.REPEAT) {
            //get the fractional part
            //(modulo behavior discards integer component)
            position = position - (int) position;

            //position now be between -1 and 1

            if (position < 0) {
                position = position + 1; //force it to be in the range 0-1
            }

            int w = 0, c1 = 0, c2 = 0;
            if (isSimpleLookup) {
                position *= gradient.length;
                int idx1 = (int) (position);
                if (idx1 + 1 < gradient.length)
                    return gradient[idx1];

                w = (int) ((position - idx1) * (1 << 16));
                c1 = gradient[idx1];
                c2 = gradient[0];
            } else {
                //for all the gradient interval arrays
                for (int i = 0; i < gradientsLength; i++) {

                    if (position < fractions[i + 1]) { //this is the array we want

                        float delta = position - fractions[i];

                        delta = ((delta / normalizedIntervals[i]) * GRADIENT_SIZE);
                        //this is the interval we want.
                        int index = (int) delta;
                        if ((index + 1 < gradients[i].length) ||
                                (i + 1 < gradientsLength))
                            return gradients[i][index];

                        w = (int) ((delta - index) * (1 << 16));
                        c1 = gradients[i][index];
                        c2 = gradients[0][0];
                        break;
                    }
                }
            }

            return
                    ((((((c1 >> 8) & 0xFF0000) +
                            ((((c2 >>> 24)) - ((c1 >>> 24))) * w)) & 0xFF0000) << 8) |

                            (((((c1) & 0xFF0000) +
                                    ((((c2 >> 16) & 0xFF) - ((c1 >> 16) & 0xFF)) * w)) & 0xFF0000)) |

                            (((((c1 << 8) & 0xFF0000) +
                                    ((((c2 >> 8) & 0xFF) - ((c1 >> 8) & 0xFF)) * w)) & 0xFF0000) >> 8) |

                            (((((c1 << 16) & 0xFF0000) +
                                    ((((c2) & 0xFF) - ((c1) & 0xFF)) * w)) & 0xFF0000) >> 16));

            // return c1 +
            //   ((( ((((c2>>>24)     )-((c1>>>24)     ))*w)&0xFF0000)<< 8) |
            //    (( ((((c2>> 16)&0xFF)-((c1>> 16)&0xFF))*w)&0xFF0000)    ) |
            //    (( ((((c2>>  8)&0xFF)-((c1>>  8)&0xFF))*w)&0xFF0000)>> 8) |
            //    (( ((((c2     )&0xFF)-((c1     )&0xFF))*w)&0xFF0000)>>16));
        } else {  //cycleMethod == MultipleGradientPaint.REFLECT

            if (position < 0) {
                position = -position; //take absolute value
            }

            int part = (int) position; //take the integer part

            position = position - part; //get the fractional part

            if ((part & 0x00000001) == 1) { //if integer part is odd
                position = 1 - position; //want the reflected color instead
            }
        }

        //now, get the color based on this 0-1 position:

        if (isSimpleLookup) { //easy to compute: just scale index by array size
            return gradient[(int) (position * fastGradientArraySize)];
        } else { //more complicated computation, to save space

            //for all the gradient interval arrays
            for (int i = 0; i < gradientsLength; i++) {

                if (position < fractions[i + 1]) { //this is the array we want

                    float delta = position - fractions[i];

                    //this is the interval we want.
                    int index = (int) ((delta / normalizedIntervals[i])
                            * (GRADIENT_SIZE_INDEX));

                    return gradients[i][index];
                }
            }

        }

        return gradientOverflow;
    }


    /**
     * Helper function to index into the gradients array.  This is necessary
     * because each interval has an array of colors with uniform size 255.
     * However, the color intervals are not necessarily of uniform length, so
     * a conversion is required.  This version also does anti-aliasing by
     * averaging the gradient over position+/-(sz/2).
     *
     * @param position the unmanipulated position.  want to map this into the
     *                 range 0 to 1
     * @param sz       the size in gradient space to average.
     * @return ARGB integer color to display
     */
    protected final int indexGradientAntiAlias(float position, float sz) {
        //first, manipulate position value depending on the cycle method.
        if (cycleMethod == MultipleGradientPaint.NO_CYCLE) {
            if (DEBUG) System.out.println("NO_CYCLE");
            float p1 = position - (sz / 2);
            float p2 = position + (sz / 2);

            if (p1 >= 1)
                return gradientOverflow;

            if (p2 <= 0)
                return gradientUnderflow;

            int interior;
            float top_weight = 0, bottom_weight = 0, frac;
            if (p2 >= 1) {
                top_weight = (p2 - 1) / sz;
                if (p1 <= 0) {
                    bottom_weight = -p1 / sz;
                    frac = 1;
                    interior = gradientAverage;
                } else {
                    frac = 1 - p1;
                    interior = getAntiAlias(p1, true, 1, false, 1 - p1, 1);
                }
            } else if (p1 <= 0) {
                bottom_weight = -p1 / sz;
                frac = p2;
                interior = getAntiAlias(0, true, p2, false, p2, 1);
            } else
                return getAntiAlias(p1, true, p2, false, sz, 1);

            int norm = (int) ((1 << 16) * frac / sz);
            int pA = (((interior >>> 20) & 0xFF0) * norm) >> 16;
            int pR = (((interior >> 12) & 0xFF0) * norm) >> 16;
            int pG = (((interior >> 4) & 0xFF0) * norm) >> 16;
            int pB = (((interior << 4) & 0xFF0) * norm) >> 16;

            if (bottom_weight != 0) {
                int bPix = gradientUnderflow;
                // System.out.println("ave: " + gradientAverage);
                norm = (int) ((1 << 16) * bottom_weight);
                pA += (((bPix >>> 20) & 0xFF0) * norm) >> 16;
                pR += (((bPix >> 12) & 0xFF0) * norm) >> 16;
                pG += (((bPix >> 4) & 0xFF0) * norm) >> 16;
                pB += (((bPix << 4) & 0xFF0) * norm) >> 16;
            }

            if (top_weight != 0) {
                int tPix = gradientOverflow;

                norm = (int) ((1 << 16) * top_weight);
                pA += (((tPix >>> 20) & 0xFF0) * norm) >> 16;
                pR += (((tPix >> 12) & 0xFF0) * norm) >> 16;
                pG += (((tPix >> 4) & 0xFF0) * norm) >> 16;
                pB += (((tPix << 4) & 0xFF0) * norm) >> 16;
            }

            return (((pA & 0xFF0) << 20) |
                    ((pR & 0xFF0) << 12) |
                    ((pG & 0xFF0) << 4) |
                    ((pB & 0xFF0) >> 4));
        }

        // See how many times we are going to "wrap around" the gradient,
        // array.
        int intSz = (int) sz;

        float weight = 1.0f;
        if (intSz != 0) {
            // We need to make sure that sz is < 1.0 otherwise
            // p1 and p2 my pass each other which will cause no end of
            // trouble.
            sz -= intSz;
            weight = sz / (intSz + sz);
            if (weight < 0.1)
                // The part of the color from the location will be swamped
                // by the averaged part of the gradient so just use the
                // average color for the gradient.
                return gradientAverage;
        }

        // So close to full gradient just use the average value...
        if (sz > 0.99)
            return gradientAverage;

        // Go up and down from position by 1/2 sz.
        float p1 = position - (sz / 2);
        float p2 = position + (sz / 2);
        if (DEBUG) System.out.println("P1: " + p1 + " P2: " + p2);

        // These indicate the direction to go from p1 and p2 when
        // averaging...
        boolean p1_up = true;
        boolean p2_up = false;

        if (cycleMethod == MultipleGradientPaint.REPEAT) {
            if (DEBUG) System.out.println("REPEAT");

            // Get positions between -1 and 1
            p1 = p1 - (int) p1;
            p2 = p2 - (int) p2;

            // force to be in rage 0-1.
            if (p1 < 0) p1 += 1;
            if (p2 < 0) p2 += 1;
        } else {  //cycleMethod == MultipleGradientPaint.REFLECT
            if (DEBUG) System.out.println("REFLECT");

            //take absolute values
            // Note when we reflect we change sense of p1/2_up.
            if (p2 < 0) {
                p1 = -p1;
                p1_up = !p1_up;
                p2 = -p2;
                p2_up = !p2_up;
            } else if (p1 < 0) {
                p1 = -p1;
                p1_up = !p1_up;
            }

            int part1, part2;
            part1 = (int) p1;   // take the integer part
            p1 = p1 - part1; // get the fractional part

            part2 = (int) p2;   // take the integer part
            p2 = p2 - part2; // get the fractional part

            // if integer part is odd we want the reflected color instead.
            // Note when we reflect we change sense of p1/2_up.
            if ((part1 & 0x01) == 1) {
                p1 = 1 - p1;
                p1_up = !p1_up;
            }

            if ((part2 & 0x01) == 1) {
                p2 = 1 - p2;
                p2_up = !p2_up;
            }

            // Check if in the end they just got switched around.
            // this commonly happens if they both end up negative.
            if ((p1 > p2) && !p1_up && p2_up) {
                float t = p1;
                p1 = p2;
                p2 = t;
                p1_up = true;
                p2_up = false;
            }
        }

        return getAntiAlias(p1, p1_up, p2, p2_up, sz, weight);
    }


    private int getAntiAlias(float p1, boolean p1_up,
                             float p2, boolean p2_up,
                             float sz, float weight) {

        // Until the last set of ops these are 28.4 fixed point values.
        int ach = 0, rch = 0, gch = 0, bch = 0;
        if (isSimpleLookup) {
            p1 *= fastGradientArraySize;
            p2 *= fastGradientArraySize;

            int idx1 = (int) p1;
            int idx2 = (int) p2;

            int i, pix;

            if (p1_up && !p2_up && (idx1 <= idx2)) {

                if (idx1 == idx2)
                    return gradient[idx1];

                // Sum between idx1 and idx2.
                for (i = idx1 + 1; i < idx2; i++) {
                    pix = gradient[i];
                    ach += ((pix >>> 20) & 0xFF0);
                    rch += ((pix >>> 12) & 0xFF0);
                    gch += ((pix >>> 4) & 0xFF0);
                    bch += ((pix << 4) & 0xFF0);
                }
            } else {
                // Do the bulk of the work, all the whole gradient entries
                // for idx1 and idx2.
                int iStart;
                int iEnd;
                if (p1_up) {
                    iStart = idx1 + 1;
                    iEnd = fastGradientArraySize;
                } else {
                    iStart = 0;
                    iEnd = idx1;
                }
                for (i = iStart; i < iEnd; i++) {
                    pix = gradient[i];
                    ach += ((pix >>> 20) & 0xFF0);
                    rch += ((pix >>> 12) & 0xFF0);
                    gch += ((pix >>> 4) & 0xFF0);
                    bch += ((pix << 4) & 0xFF0);
                }

                if (p2_up) {
                    iStart = idx2 + 1;
                    iEnd = fastGradientArraySize;
                } else {
                    iStart = 0;
                    iEnd = idx2;
                }
                for (i = iStart; i < iEnd; i++) {
                    pix = gradient[i];
                    ach += ((pix >>> 20) & 0xFF0);
                    rch += ((pix >>> 12) & 0xFF0);
                    gch += ((pix >>> 4) & 0xFF0);
                    bch += ((pix << 4) & 0xFF0);
                }
            }

            int norm, isz;

            // Normalize the summation so far...
            isz = (int) ((1 << 16) / (sz * fastGradientArraySize));
            ach = (ach * isz) >> 16;
            rch = (rch * isz) >> 16;
            gch = (gch * isz) >> 16;
            bch = (bch * isz) >> 16;

            // Clean up with the partial buckets at each end.
            if (p1_up) norm = (int) ((1 - (p1 - idx1)) * isz);
            else norm = (int) ((p1 - idx1) * isz);
            pix = gradient[idx1];
            ach += (((pix >>> 20) & 0xFF0) * norm) >> 16;
            rch += (((pix >>> 12) & 0xFF0) * norm) >> 16;
            gch += (((pix >>> 4) & 0xFF0) * norm) >> 16;
            bch += (((pix << 4) & 0xFF0) * norm) >> 16;

            if (p2_up) norm = (int) ((1 - (p2 - idx2)) * isz);
            else norm = (int) ((p2 - idx2) * isz);
            pix = gradient[idx2];
            ach += (((pix >>> 20) & 0xFF0) * norm) >> 16;
            rch += (((pix >>> 12) & 0xFF0) * norm) >> 16;
            gch += (((pix >>> 4) & 0xFF0) * norm) >> 16;
            bch += (((pix << 4) & 0xFF0) * norm) >> 16;

            // Round and drop the 4bits frac.
            ach = (ach + 0x08) >> 4;
            rch = (rch + 0x08) >> 4;
            gch = (gch + 0x08) >> 4;
            bch = (bch + 0x08) >> 4;

        } else {
            int idx1 = 0, idx2 = 0;
            int i1 = -1, i2 = -1;
            float f1 = 0, f2 = 0;
            // Find which gradient interval our points fall into.
            for (int i = 0; i < gradientsLength; i++) {
                if ((p1 < fractions[i + 1]) && (i1 == -1)) {
                    //this is the array we want
                    i1 = i;
                    f1 = p1 - fractions[i];

                    f1 = ((f1 / normalizedIntervals[i])
                            * GRADIENT_SIZE_INDEX);
                    //this is the  interval we want.
                    idx1 = (int) f1;
                    if (i2 != -1) break;
                }
                if ((p2 < fractions[i + 1]) && (i2 == -1)) {
                    //this is the array we want
                    i2 = i;
                    f2 = p2 - fractions[i];

                    f2 = ((f2 / normalizedIntervals[i])
                            * GRADIENT_SIZE_INDEX);
                    //this is the interval we want.
                    idx2 = (int) f2;
                    if (i1 != -1) break;
                }
            }

            if (i1 == -1) {
                i1 = gradients.length - 1;
                f1 = idx1 = GRADIENT_SIZE_INDEX;
            }

            if (i2 == -1) {
                i2 = gradients.length - 1;
                f2 = idx2 = GRADIENT_SIZE_INDEX;
            }

            if (DEBUG) System.out.println("I1: " + i1 + " Idx1: " + idx1 +
                    " I2: " + i2 + " Idx2: " + idx2);

            // Simple case within one gradient array (so the average
            // of the two idx gives us the true average of colors).
            if ((i1 == i2) && (idx1 <= idx2) && p1_up && !p2_up)
                return gradients[i1][(idx1 + idx2 + 1) >> 1];

            // i1 != i2

            int pix, norm;
            int base = (int) ((1 << 16) / sz);
            if ((i1 < i2) && p1_up && !p2_up) {
                norm = (int) ((base
                        * normalizedIntervals[i1]
                        * (GRADIENT_SIZE_INDEX - f1))
                        / GRADIENT_SIZE_INDEX);
                pix = gradients[i1][(idx1 + GRADIENT_SIZE) >> 1];
                ach += (((pix >>> 20) & 0xFF0) * norm) >> 16;
                rch += (((pix >>> 12) & 0xFF0) * norm) >> 16;
                gch += (((pix >>> 4) & 0xFF0) * norm) >> 16;
                bch += (((pix << 4) & 0xFF0) * norm) >> 16;

                for (int i = i1 + 1; i < i2; i++) {
                    norm = (int) (base * normalizedIntervals[i]);
                    pix = gradients[i][GRADIENT_SIZE >> 1];

                    ach += (((pix >>> 20) & 0xFF0) * norm) >> 16;
                    rch += (((pix >>> 12) & 0xFF0) * norm) >> 16;
                    gch += (((pix >>> 4) & 0xFF0) * norm) >> 16;
                    bch += (((pix << 4) & 0xFF0) * norm) >> 16;
                }

                norm = (int) ((base * normalizedIntervals[i2] * f2)
                        / GRADIENT_SIZE_INDEX);
                pix = gradients[i2][(idx2 + 1) >> 1];
                ach += (((pix >>> 20) & 0xFF0) * norm) >> 16;
                rch += (((pix >>> 12) & 0xFF0) * norm) >> 16;
                gch += (((pix >>> 4) & 0xFF0) * norm) >> 16;
                bch += (((pix << 4) & 0xFF0) * norm) >> 16;
            } else {
                if (p1_up) {
                    norm = (int) ((base
                            * normalizedIntervals[i1]
                            * (GRADIENT_SIZE_INDEX - f1))
                            / GRADIENT_SIZE_INDEX);
                    pix = gradients[i1][(idx1 + GRADIENT_SIZE) >> 1];
                } else {
                    norm = (int) ((base * normalizedIntervals[i1] * f1)
                            / GRADIENT_SIZE_INDEX);
                    pix = gradients[i1][(idx1 + 1) >> 1];
                }
                ach += (((pix >>> 20) & 0xFF0) * norm) >> 16;
                rch += (((pix >>> 12) & 0xFF0) * norm) >> 16;
                gch += (((pix >>> 4) & 0xFF0) * norm) >> 16;
                bch += (((pix << 4) & 0xFF0) * norm) >> 16;

                if (p2_up) {
                    norm = (int) ((base
                            * normalizedIntervals[i2]
                            * (GRADIENT_SIZE_INDEX - f2))
                            / GRADIENT_SIZE_INDEX);
                    pix = gradients[i2][(idx2 + GRADIENT_SIZE) >> 1];
                } else {
                    norm = (int) ((base * normalizedIntervals[i2] * f2)
                            / GRADIENT_SIZE_INDEX);
                    pix = gradients[i2][(idx2 + 1) >> 1];
                }
                ach += (((pix >>> 20) & 0xFF0) * norm) >> 16;
                rch += (((pix >>> 12) & 0xFF0) * norm) >> 16;
                gch += (((pix >>> 4) & 0xFF0) * norm) >> 16;
                bch += (((pix << 4) & 0xFF0) * norm) >> 16;

                // p1_up and p2_up are just used to set the loop-boundarys,
                // then we loop from iStart to iEnd
                int iStart;
                int iEnd;

                if (p1_up) {
                    iStart = i1 + 1;
                    iEnd = gradientsLength;
                } else {
                    iStart = 0;
                    iEnd = i1;
                }
                for (int i = iStart; i < iEnd; i++) {
                    norm = (int) (base * normalizedIntervals[i]);
                    pix = gradients[i][GRADIENT_SIZE >> 1];

                    ach += (((pix >>> 20) & 0xFF0) * norm) >> 16;
                    rch += (((pix >>> 12) & 0xFF0) * norm) >> 16;
                    gch += (((pix >>> 4) & 0xFF0) * norm) >> 16;
                    bch += (((pix << 4) & 0xFF0) * norm) >> 16;
                }

                if (p2_up) {
                    iStart = i2 + 1;
                    iEnd = gradientsLength;
                } else {
                    iStart = 0;
                    iEnd = i2;
                }
                for (int i = iStart; i < iEnd; i++) {
                    norm = (int) (base * normalizedIntervals[i]);
                    pix = gradients[i][GRADIENT_SIZE >> 1];

                    ach += (((pix >>> 20) & 0xFF0) * norm) >> 16;
                    rch += (((pix >>> 12) & 0xFF0) * norm) >> 16;
                    gch += (((pix >>> 4) & 0xFF0) * norm) >> 16;
                    bch += (((pix << 4) & 0xFF0) * norm) >> 16;
                }


            }
            ach = (ach + 0x08) >> 4;
            rch = (rch + 0x08) >> 4;
            gch = (gch + 0x08) >> 4;
            bch = (bch + 0x08) >> 4;
            if (DEBUG) System.out.println("Pix: [" + ach + ", " + rch +
                    ", " + gch + ", " + bch + ']');
        }

        if (weight != 1) {
            // System.out.println("ave: " + gradientAverage);
            int aveW = (int) ((1 << 16) * (1 - weight));
            int aveA = ((gradientAverage >>> 24) & 0xFF) * aveW;
            int aveR = ((gradientAverage >> 16) & 0xFF) * aveW;
            int aveG = ((gradientAverage >> 8) & 0xFF) * aveW;
            int aveB = ((gradientAverage) & 0xFF) * aveW;

            int iw = (int) (weight * (1 << 16));
            ach = ((ach * iw) + aveA) >> 16;
            rch = ((rch * iw) + aveR) >> 16;
            gch = ((gch * iw) + aveG) >> 16;
            bch = ((bch * iw) + aveB) >> 16;
        }

        return ((ach << 24) | (rch << 16) | (gch << 8) | bch);
    }


    /**
     * Helper function to convert a color component in sRGB space to linear
     * RGB space.  Used to build a static lookup table.
     */
    private static int convertSRGBtoLinearRGB(int color) {

        // use of float and double arithmetic gives exactly same results
        float output;

        float input = color / 255.0f;
        if (input <= 0.04045f) {
            output = input / 12.92f;
        } else {
            output = (float) Math.pow((input + 0.055) / 1.055, 2.4);
        }
        return Math.round(output * 255.0f);
    }

    /**
     * Helper function to convert a color component in linear RGB space to
     * SRGB space. Used to build a static lookup table.
     */
    private static int convertLinearRGBtoSRGB(int color) {

        // use of float and double arithmetic gives exactly same results
        float output;

        float input = color / 255.0f;

        if (input <= 0.0031308f) {
            output = input * 12.92f;
        } else {
            output = (1.055f * ((float) Math.pow(input, (1.0 / 2.4)))) - 0.055f;
        }
        return Math.round(output * 255.0f);
    }


    /**
     * Superclass getRaster...
     */
    public final Raster getRaster(int x, int y, int w, int h) {
        if (w == 0 || h == 0) {
            return null;
        }

        //
        // If working raster is big enough, reuse it. Otherwise,
        // build a large enough new one.
        //
        WritableRaster raster = saved;
        if (raster == null || raster.getWidth() < w || raster.getHeight() < h) {
            raster = getCachedRaster(dataModel, w, h);
            saved = raster;
            // NOTE:We would like to use 'x' & 'y' here instead of
            // '0', '0' but this will fail on MacOSX.  Since it
            // doesn't have an effect on other JVMs.
            raster = raster.createWritableChild
                    (raster.getMinX(), raster.getMinY(), w, h, 0, 0, null);
        }

        // Access raster internal int array. Because we use a DirectColorModel,
        // we know the DataBuffer is of type DataBufferInt and the SampleModel
        // is SinglePixelPackedSampleModel.
        // Adjust for initial offset in DataBuffer and also for the scanline
        // stride.
        //
        DataBufferInt rasterDB = (DataBufferInt) raster.getDataBuffer();
        int[] pixels = rasterDB.getBankData()[0];
        int off = rasterDB.getOffset();
        int scanlineStride = ((SinglePixelPackedSampleModel)
                raster.getSampleModel()).getScanlineStride();
        int adjust = scanlineStride - w;

        fillRaster(pixels, off, adjust, x, y, w, h); //delegate to subclass.

        GraphicsUtil.coerceData(raster, dataModel,
                model.isAlphaPremultiplied());


        return raster;
    }

    /**
     * Subclasses should implement this.
     */
    protected abstract void fillRaster(int[] pixels, int off, int adjust,
                                       int x, int y, int w, int h);


    /**
     * Took this cacheRaster code from GradientPaint. It appears to recycle
     * rasters for use by any other instance, as long as they are sufficiently
     * large.
     */
    protected static synchronized WritableRaster getCachedRaster
    (ColorModel cm, int w, int h) {
        if (cm == cachedModel) {
            if (cached != null) {
                WritableRaster ras = cached.get();
                if (ras != null &&
                        ras.getWidth() >= w &&
                        ras.getHeight() >= h) {
                    cached = null;
                    return ras;
                }
            }
        }
        // Don't create rediculously small rasters...
        if (w < 32) w = 32;
        if (h < 32) h = 32;
        return cm.createCompatibleWritableRaster(w, h);
    }

    /**
     * Took this cacheRaster code from GradientPaint. It appears to recycle
     * rasters for use by any other instance, as long as they are sufficiently
     * large.
     */
    protected static synchronized void putCachedRaster(ColorModel cm,
                                                       WritableRaster ras) {
        if (cached != null) {
            WritableRaster cras = cached.get();
            if (cras != null) {
                int cw = cras.getWidth();
                int ch = cras.getHeight();
                int iw = ras.getWidth();
                int ih = ras.getHeight();
                if (cw >= iw && ch >= ih) {
                    return;
                }
                if (cw * ch >= iw * ih) {
                    return;
                }
            }
        }
        cachedModel = cm;
        cached = new WeakReference<WritableRaster>(ras);
    }

    /**
     * Release the resources allocated for the operation.
     */
    public final void dispose() {
        if (saved != null) {
            putCachedRaster(model, saved);
            saved = null;
        }
    }

    /**
     * Return the ColorModel of the output.
     */
    public final ColorModel getColorModel() {
        return model;
    }
}

