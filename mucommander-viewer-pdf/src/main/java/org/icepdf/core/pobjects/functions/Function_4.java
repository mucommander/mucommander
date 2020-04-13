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
import org.icepdf.core.pobjects.Stream;
import org.icepdf.core.pobjects.functions.postscript.Lexer;
import org.icepdf.core.util.Utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Type 4 Function (PDF 1.3), also called a PostScript calculator function,
 * shall be represented as a stream containing code written n a small subset of
 * the PostScript language. </p>
 * <p>Type 4 functions offer greater flexibility and potentially greater
 * accuracy then exponential functions (type 2 functions).  Type 4 functions
 * also make it possible to include a wide variety of halftone spots functions
 * without the loss of accuracy that comes from sampling, and without adding to
 * the list a predefined spot function (10.5.3 spot functions).  All of the
 * predefined spot functions can be written as type 4 functions. </p>
 *
 * @author ICEsoft Technologies Inc.
 * @since 4.2
 */
public class Function_4 extends Function {

    private static final Logger logger =
            Logger.getLogger(Function_4.class.toString());

    // decoded content that makes up the type 4 functions.
    private byte[] functionContent;

    // cache for calculated colour values
    private ConcurrentHashMap<Integer, float[]> resultCache;

    public Function_4(Dictionary d) {
        super(d);
        // decode the stream for parsing.
        if (d instanceof Stream) {
            Stream functionStream = (Stream) d;
            functionContent = functionStream.getDecodedStreamBytes(0);
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Function 4: " + Utils.convertByteArrayToByteString(functionContent));
            }

        } else {
            logger.warning("Type 4 function operands could not be found.");
        }
        // cache for type 4 function results.
        resultCache = new ConcurrentHashMap<Integer, float[]>();
    }

    /**
     * <p>Puts the value x thought the function type 4 algorithm.
     *
     * @param x input values m
     * @return output values n
     */
    public float[] calculate(float[] x) {

        // check the cache in case we've already made the calculation.
        Integer colourKey = calculateColourKey(x);
        float[] result = resultCache.get(colourKey);
        if (result != null) {
            return result;
        }

        // setup the lexer stream
        InputStream content = new ByteArrayInputStream(functionContent);
        Lexer lex = new Lexer();
        lex.setInputStream(content);

        // parse/evaluate the type 4 functions with the input value(s) x.
        try {
            lex.parse(x);
        } catch (Throwable e) {
            logger.log(Level.WARNING, "Error Processing Type 4 definition", e);
        }

        // get the remaining number on the stack which are the return values.
        Stack stack = lex.getStack();

        // length of output array
        int n = range.length / 2;
        // ready output array
        float y[] = new float[n];

        // pop remaining items off the stack and apply the range bounds.
        for (int i = 0; i < n; i++) {
            y[i] = Math.min(Math.max((Float) stack.elementAt(i),
                    range[2 * i]), range[2 * i + 1]);
        }
        // add the new value to the cache.
        resultCache.put(colourKey, y);
        return y;
    }

    /**
     * Utility for creating a comparable colour key for colour components.
     *
     * @param colours one or more colour values,  usually maxes out at four.
     * @return concatenation of colour values.
     */
    private Integer calculateColourKey(float[] colours) {
        int length = colours.length;
        // only works for colour vlues 0-255
        if (!(colours[0] <= 1.0)) {
            if (length == 1) {
                return (int) colours[0];
            } else if (length == 2) {
                return ((int) colours[1] << 8) | (int) colours[0];
            } else if (length == 3) {
                return ((int) colours[2] << 16) |
                        ((int) colours[1] << 8) | (int) colours[0];
            }
        }
        // otherwise expensive hash generation.
        StringBuilder builder = new StringBuilder();
        for (float colour : colours) {
            builder.append(colour);
        }
        return builder.toString().hashCode();
    }
}
