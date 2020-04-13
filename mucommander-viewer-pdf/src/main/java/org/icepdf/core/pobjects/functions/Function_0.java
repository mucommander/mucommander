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
import org.icepdf.core.pobjects.Stream;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>This class <code>Function_0</code> represents a generic Type 0, sampled function
 * type.  Type 0 functions use a sequence of sampled values (contained in a stream)
 * to produce an approximation for function whose domains and ranges are bounded.
 * The samples are organized as an m-dimensional table in which each entry has n
 * components. </p>
 * <p/>
 * <p>Sampled functions are highly general and offer reasonably accurate
 * representations of arbitrary analytic functions at low expense.  The
 * dimensionality of a sampled function is restricted only by the implementation
 * limits.</p>
 *
 * @see Function
 * @since 1.0
 */
public class Function_0 extends Function {

    private static final Logger logger =
            Logger.getLogger(Function_0.class.toString());

    public static final Name SIZE_KEY = new Name("Size");
    public static final Name BITSPERSAMPLE_KEY = new Name("BitsPerSample");
    public static final Name ENCODE_KEY = new Name("Encode");
    public static final Name DECODE_KEY = new Name("Decode");

    // An array of m positive integers specifying the number of samples in each
    // input dimension of the sample table.
    private int size[];

    // The number of bits used to represent each sample. If the function has
    // multiple output values, each one occupies BitsPerSample bits.  Valid
    // values are 1,2,4,8,12,16,24, and 32.
    private int bitsPerSample;

    // The order of interpolation between samples.  Valid values are 1 and 3,
    // specifying linear and cubic spline interpolation, respectively.  Default 1
    private int order;

    // An array of 2 x m numbers specifying the linear mapping of input values
    // into the domain of the function's sample table.  Default value:
    // [0 (size<sub>0</sub>-1) 0 size<sub>1</sub> ...].
    private float encode[];

    // An array of 2 x n numbers specifying the linear mapping of sample values
    // into the range the range appropriate for the function's output values.
    // Default same as Range.
    private float[] decode;

    private int[][] samples;

    /**
     * Creates a new instance of a type 0 function.
     *
     * @param d function's dictionary.
     */
    Function_0(Dictionary d) {
        // initiate, domain and range
        super(d);

        List s = (List) d.getObject(SIZE_KEY);
        // setup size array, each entry represents the number of samples for
        // each input dimension.
        size = new int[s.size()];
        for (int i = 0; i < s.size(); i++) {
            size[i] = (int) (((Number) s.get(i)).floatValue());
        }
        // setup bitsPerSample array, each entry represents the number of bits used
        // for each sample
        bitsPerSample = d.getInt(BITSPERSAMPLE_KEY);

        // setup of encode table, specifies the linear mapping of input values
        // into the domain of the function's sample table.
        List enc = (List) d.getObject(ENCODE_KEY);
        encode = new float[size.length * 2];
        if (enc != null) {
            for (int i = 0; i < size.length * 2; i++) {
                encode[i] = ((Number) enc.get(i)).floatValue();
            }
        } else {
            // encoding is optional, so fill up encode area with uniform
            // mapping of 0,size[0]-1, 0,size[1]-1, 0,size[2]-1 which is
            // the default value which is defined in the spec.
            for (int i = 0; i < size.length; i++) {
                encode[2 * i] = 0;
                encode[2 * i + 1] = size[i] - 1;
            }
        }

        // setup decode, an array of  2 x n numbers specifying the linear mapping
        // of sample values into the range appropriate for the function's output values.
        List dec = (List) d.getObject(DECODE_KEY);
        decode = new float[range.length];
        if (dec != null) {
            for (int i = 0; i < range.length; i++) {
                decode[i] = ((Number) dec.get(i)).floatValue();
            }
        } else {
            // decode is optional, so we should copy range as a default values
            System.arraycopy(range, 0, decode, 0, range.length);
        }

        // lastly get the stream byte data if any.
        Stream stream = (Stream) d;
        convertToSamples(stream.getDecodedStreamBytes(0), bitsPerSample);
    }

    /**
     * Calculates the y values for the given x values using a sampled function.
     *
     * @param x array of input values m.
     * @return array of output value n.
     */
    public float[] calculate(float[] x) {
        // length of output array
        int n = range.length / 2;
        // ready output array
        float y[] = new float[n];
        // work throw all input data and store in y[]
        try {
            // sampled each input value xi for 0 & i < m
            for (int i = 0; i < size.length; i++) {
                // clip input value appropriately for the given domain
                // xi' = min (max(xi, Domain2i), Domain2i+1)
                x[i] = Math.min(Math.max(x[i], domain[2 * i]), domain[2 * i + 1]);
                // find the encoded value
                // ei = interpolate (xi', Domain2i, Domain2i+1, Encode2i, Encode2i+1)
                float e = interpolate(x[i], domain[2 * i], domain[2 * i + 1],
                        encode[2 * i], encode[2 * i + 1]);
                // clip to the size of the sampled table in that dimension:
                // ei' = min (max(ei, 0), Sizei-1)
                e = Math.min(Math.max(e, 0), size[i] - 1);
                // pretty sure that e1 and e2 are used to for a bilinear interpolation?
                // Output values are are calculated from the nearest surrounding values
                // in the sample table in the sample table.
                int e1 = (int) Math.floor(e);
                int e2 = (int) Math.ceil(e);
                int index;
                // Calculate the final output values
                for (int j = 0; j < n; j++) {
                    //  find nearest surrounding values in the sample table
                    int b1 = samples[e1][j];
                    int b2 = samples[e2][j];
                    // get the average
                    float r = ((float) b1 + (float) b2) / 2;
                    // interpolate to get output values
                    r = interpolate(r, 0f, (float) Math.pow(2, bitsPerSample) -
                            1, decode[2 * j], decode[2 * j + 1]);
                    // finally, decoded values are clipped ot the range
                    // yj = min(max(rj', Range2j), Range2j+1)
                    r = Math.min(Math.max(r, range[2 * j]), range[2 * j + 1]);
                    index = i * n + j;
                    // make sure we y can contain the calculated r value
                    if (index < y.length) {
                        y[index] = r;
                    }

                }
            }
        } catch (Exception e) {
            logger.log(Level.FINER, "Error calculating function 0 values", e);
        }
        return y;
    }

    /**
     * Utility for converting sample bytes to integers of the correct bits per sample.
     *
     * @param bytes         byte array to convert.
     * @param bitsPerSample bits per sample value
     */
    private void convertToSamples(byte[] bytes, int bitsPerSample) {
        int size = 1;
        int inputMax = domain.length / 2;
        int outputMax = range.length / 2;
        for (int i = 0; i < inputMax; i++) {
            size *= this.size[i];
        }
        samples = new int[size][outputMax];

        int sampleIndex = 0;
        int byteLocation = 0;
        int bitLocation = 0;
        for (int i = 0; i < inputMax; i++) {
            for (int j = 0; j < this.size[i]; j++) {
                for (int k = 0; k < outputMax; k++) {
                    int value = 0;
                    int bitsToRead = bitsPerSample;
                    byte byteCount = bytes[byteLocation];
                    while (bitsToRead > 0) {
                        int nextBit = ((byteCount >> (7 - bitLocation)) & 0x1);
                        value |= nextBit << (bitsToRead - 1);
                        bitLocation++;
                        // skip to the next bit.
                        if (bitLocation == 8) {
                            bitLocation = 0;
                            byteLocation++;
                            if (bitsToRead > 1) {
                                byteCount = bytes[byteLocation];
                            }
                        }
                        bitsToRead--;
                    }
                    samples[sampleIndex][k] = value;
                }
                sampleIndex++;
            }
        }
    }
}
