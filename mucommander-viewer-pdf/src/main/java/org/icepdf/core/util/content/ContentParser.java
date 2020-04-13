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
package org.icepdf.core.util.content;

import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.graphics.GraphicsState;
import org.icepdf.core.pobjects.graphics.Shapes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Stack;

/**
 * ContentParser interface for content streams.
 *
 * @since 5.0
 */
public interface ContentParser {

    /**
     * Gets the shapes parsed by the last run of {@see parse}.
     *
     * @return Shapes associated with the content parser.
     */
    Shapes getShapes();

    /**
     * Gets the stack used by the content parser.  Under normal execution the
     * stack should be empty.  If the stack has elements remaining then is
     * generally means that a content parsing error has taken place.
     *
     * @return object stack.
     */
    Stack<Object> getStack();

    /**
     * Gets the graphic state object associated with the parser. Needed by
     * the Type3 font program.
     *
     * @return graphic state of the parsed content stream.
     */
    GraphicsState getGraphicsState();

    /**
     * Sets the external graphics state object associated with Form's and
     * Tiling Patterns.
     *
     * @param graphicState graphic state to pass to parser.
     */
    void setGraphicsState(GraphicsState graphicState);

    /**
     * Parse the given stream bytes.
     *
     * @param streamBytes bytes that make of one or more content streams.
     * @return an instance of this content parser.
     * @throws InterruptedException thread was interrupted.
     * @throws IOException          io exception during the pars.
     */
    ContentParser parse(byte[][] streamBytes, Page page)
            throws InterruptedException, IOException;

    /**
     * Optimized text parsing call which will ignore any instructions that
     * are not related to text extraction.  Images and other operands are
     * ignored speeding up the extraction process.
     *
     * @param source byte source to parse.
     * @return Shapes object which contains the extract PageText object.
     * @throws UnsupportedEncodingException encoding error.
     */
    Shapes parseTextBlocks(byte[][] source) throws UnsupportedEncodingException, InterruptedException;

    /**
     * Sets the scale factor used by some graphic state parameters so that the
     * to users space CTM scale factor can be applied.  In particular some
     * Type3 glyphs need to take into account this scaling factor.
     *
     * @param scale scale factor to apply to various graphic state parameters.
     */
    void setGlyph2UserSpaceScale(float scale);
}
