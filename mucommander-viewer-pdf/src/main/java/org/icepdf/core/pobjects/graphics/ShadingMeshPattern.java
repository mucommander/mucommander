package org.icepdf.core.pobjects.graphics;

import org.icepdf.core.io.BitStream;
import org.icepdf.core.pobjects.ImageStream;
import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.Stream;
import org.icepdf.core.pobjects.functions.Function;
import org.icepdf.core.util.Library;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Base class for Mesh shading types 4-7. Each subtype parses the shading vertex information slighly differently
 * but the decode and base parse for flag, coordinate and colour is the same.
 *
 * @since 6.2
 */
public abstract class ShadingMeshPattern extends ShadingPattern implements Pattern {

    private static final Logger logger =
            Logger.getLogger(ShadingMeshPattern.class.toString());

    public static final Name BITS_PER_FLAG_KEY = new Name("BitsPerFlag");
    public static final Name BITS_PER_COORDINATE_KEY = new Name("BitsPerCoordinate");

    protected static final int DECODE_X_MIN = 0;
    protected static final int DECODE_X_MAX = 1;
    protected static final int DECODE_Y_MIN = 2;
    protected static final int DECODE_Y_MAX = 3;

    // (Required) The number of bits used to represent the edge flag for each vertex (see below). The value of
    // BitsPerFlag shall be 2, 4, or 8, but only the least significant 2 bits in each flag value shall be used.
    // The value for the edge flag shall be 0, 1, or 2.
    protected int bitsPerFlag;
    // (Required) The number of bits used to represent each vertex coordinate.
    // The value shall be 1, 2, 4, 8, 12, 16, 24, or 32.
    protected int bitsPerCoordinate;
    // (Required) The number of bits used to represent each colour component.
    // The value shall be 1, 2, 4, 8, 12, or 16.
    protected int bitsPerComponent;
    // colour space component count.
    protected int colorSpaceCompCount;

    // vertex data
    protected BitStream vertexBitStream;
    protected Stream meshDataStream;

    // converted decode data to simply process later on, taken from our DecodeRasterOp class.
    protected float[] decode;

    public ShadingMeshPattern(Library l, HashMap h, Stream meshDataStream) {
        super(l, h);
        this.meshDataStream = meshDataStream;
        shadingDictionary = meshDataStream.getEntries();
        bitsPerFlag = library.getInt(shadingDictionary, BITS_PER_FLAG_KEY);
        bitsPerCoordinate = library.getInt(shadingDictionary, BITS_PER_COORDINATE_KEY);
        bitsPerComponent = library.getInt(shadingDictionary, ImageStream.BITSPERCOMPONENT_KEY);
        colorSpace = PColorSpace.getColorSpace(library, library.getObject(shadingDictionary, COLORSPACE_KEY));
        colorSpaceCompCount = colorSpace.getNumComponents();

        // Function is optional and cannot be used with indexed colour models.
        Object tmp = library.getObject(shadingDictionary, FUNCTION_KEY);
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
        decode = processDecode();
        vertexBitStream = new BitStream(meshDataStream.getDecodedByteArrayInputStream());
    }

    public abstract Paint getPaint() throws InterruptedException;

    /**
     * An array of numbers specifying how to map vertex coordinates and colour components into the
     * appropriate ranges of values. The decoding method is similar to that used in image dictionaries
     * (see 8.9.5.2, "Decode Arrays"). The ranges shall be specified as follows:
     * [xmin xmax ymin ymax c1,min c1,max … cn,min cn,max]
     * Only one pair of c values shall be specified if a Function entry is present.
     */
    protected float[] processDecode() {
        float[] decode = new float[6];
        if (function == null) {
            decode = new float[4 + 2 * colorSpaceCompCount];
        }

        java.util.List<Number> decodeVec = (java.util.List<Number>) library.getObject(shadingDictionary, ImageStream.DECODE_KEY);

        float maxValue = bitsPerCoordinate < 32 ? (float) ((1 << bitsPerCoordinate) - 1) : (float) 2.3283064365386963e-10; // 2^-32;
        for (int i = 0; i <= DECODE_Y_MAX; ) {
            float Dmin = decodeVec.get(i).floatValue();
            float Dmax = decodeVec.get(i + 1).floatValue();
            decode[i++] = Dmin;
            decode[i++] = (Dmax - Dmin) / maxValue;
        }
        maxValue = ((int) Math.pow(2, bitsPerComponent)) - 1;
        for (int i = 4; i < decode.length; ) {
            float Dmin = decodeVec.get(i).floatValue();
            float Dmax = decodeVec.get(i + 1).floatValue();
            decode[i++] = Dmin;
            decode[i++] = (Dmax - Dmin) / maxValue;
        }
        return decode;
    }

    /**
     * Reads the vertex descriptor flag, length of flag is defined by the bitsPerFlag dictionary entry.
     *
     * @return int value of the vertex flag.
     * @throws IOException bit stream issue.
     */
    protected int readFlag() throws IOException {
        return vertexBitStream.getBits(bitsPerFlag);
    }

    /**
     * Reads the vertex coordinate data, length of flag is defined by the bitsPerCoordinate dictionary entry.
     *
     * @return int value of the vertex coordinate.
     * @throws IOException bit stream issue.
     */
    protected Point2D.Float readCoord() throws IOException {
        float x = vertexBitStream.getBits(bitsPerCoordinate);
        float y = vertexBitStream.getBits(bitsPerCoordinate);
        // normalize components to decode array
        x *= decode[DECODE_X_MAX] - decode[DECODE_X_MIN] + decode[DECODE_X_MIN];
        y *= decode[DECODE_Y_MAX] - decode[DECODE_Y_MIN] + decode[DECODE_Y_MIN];
        return new Point2D.Float(x, y);
    }

    /**
     * Reads the vertex colour data, length of flag is defined by the colorSpaceCompCount dictionary entry.
     * Color data is generate using the function if present as well as the defined colour space.
     *
     * @return int value of the vertex colour.
     * @throws IOException bit stream issue.
     */
    protected Color readColor() throws IOException {
        float[] primitives;
        if (function == null) {
            primitives = new float[colorSpaceCompCount];
            for (int i = 0, j = 4; i < colorSpaceCompCount; i++, j += 2) {
                primitives[i] = vertexBitStream.getBits(bitsPerComponent);
                // normalize
                primitives[i] *= decode[j + 1] - decode[j] + decode[j];
            }
            primitives = PColorSpace.reverse(primitives);
            return colorSpace.getColor(primitives, true);
        } else {
            float value = vertexBitStream.getBits(bitsPerComponent);
            // normalize
            value *= (decode[5] - decode[4]) + decode[4];
            primitives = new float[]{value};
            float[] output = calculateValues(primitives);
            if (output != null) {
                output = PColorSpace.reverse(output);
                return colorSpace.getColor(output, true);
            }
        }
        return null;
    }
}
