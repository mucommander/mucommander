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

import org.icepdf.core.io.ByteDoubleArrayInputStream;
import org.icepdf.core.io.SequenceInputStream;
import org.icepdf.core.pobjects.*;
import org.icepdf.core.pobjects.graphics.*;
import org.icepdf.core.pobjects.graphics.commands.GlyphOutlineDrawCmd;
import org.icepdf.core.pobjects.graphics.commands.ImageDrawCmd;
import org.icepdf.core.pobjects.graphics.text.PageText;
import org.icepdf.core.util.Library;
import org.icepdf.core.util.Parser;
import org.icepdf.core.util.PdfOps;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ContentParser is responsible for parsing a page's content streams.  The
 * parsed text, image and other PDF object types are added the pages Shapes
 * object for later drawing and display.
 */
public class OContentParser extends AbstractContentParser {

    private static final Logger logger =
            Logger.getLogger(OContentParser.class.toString());


    /**
     * @param l PDF library master object.
     * @param r resources
     */
    public OContentParser(Library l, Resources r) {
        super(l, r);
    }

    /**
     * Parse a pages content stream.
     *
     * @param streamBytes byte stream containing page content
     * @return a Shapes Object containing all the pages text and images shapes.
     * @throws InterruptedException if current parse thread is interrupted.
     * @throws IOException          unexpected end of content stream.
     */
    public ContentParser parse(byte[][] streamBytes, Page page)
            throws InterruptedException, IOException {
        if (shapes == null) {
            shapes = new Shapes();
            // Normal, clean content parse where graphics state is null
            if (graphicState == null) {
                graphicState = new GraphicsState(shapes);
            }
            // If not null we have an Form XObject that contains a content stream
            // and we must copy the previous graphics states draw settings in order
            // preserve colour and fill data for the XOjbects content stream.
            else {
                // the graphics state gets a new coordinate system.
                graphicState.setCTM(new AffineTransform());
                // reset the clipping area.
                graphicState.setClip(null);
                // copy previous stroke info
                setStroke(shapes, graphicState);
                // assign new shapes to the new graphics state
                graphicState.setShapes(shapes);
            }
        }

        if (oCGs == null && library.getCatalog().getOptionalContent() != null) {
            oCGs = new LinkedList<OptionalContents>();
        }

        if (logger.isLoggable(Level.FINER)) {
            logger.fine("Parsing page content streams: " + streamBytes.length);
            // print all the stream byte chunks.
            for (byte[] streamByte : streamBytes) {
                if (streamByte != null) {
                    String tmp = new String(streamByte, "ISO-8859-1");
                    logger.finer("Content = " + tmp);
                }
            }
        }

        // great a parser to get tokens for stream
        Parser parser;

        // test case for progress bar
        java.util.List<InputStream> in = new ArrayList<InputStream>();
        for (int i = 0; i < streamBytes.length; i++) {
            in.add(new ByteArrayInputStream(streamBytes[i]));
        }
        parser = new Parser(new SequenceInputStream(in, ' '));

        // text block y offset.
        float yBTstart = 0;

//        long startTime = System.currentTimeMillis();
        try {

            // loop through each token returned form the parser
            Object tok;
            while (true) {

                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("ContentParser thread interrupted");
                }

                tok = parser.getStreamObject();

                // add any names and numbers and every thing else on the
                // stack for future reference
                if (!(tok instanceof String)) {
                    stack.push(tok);
                } else {

                    // Append a straight line segment from the current point to the
                    // point (x, y). The new current point is (x, y).
                    if (tok.equals(PdfOps.l_TOKEN)) {
//                        collectTokenFrequency(PdfOps.l_TOKEN);
                        geometricPath = consume_L(stack, geometricPath);
                    }

                    // Begin a new subpath by moving the current point to
                    // coordinates (x, y), omitting any connecting line segment. If
                    // the previous path construction operator in the current path
                    // was also m, the new m overrides it; no vestige of the
                    // previous m operation remains in the path.
                    else if (tok.equals(PdfOps.m_TOKEN)) {
//                        collectTokenFrequency(PdfOps.m_TOKEN);
                        geometricPath = consume_m(stack, geometricPath);
                    }

                    // Append a cubic Bezier curve to the current path. The curve
                    // extends from the current point to the point (x3, y3), using
                    // (x1, y1) and (x2, y2) as the Bezier control points.
                    // The new current point is (x3, y3).
                    else if (tok.equals(PdfOps.c_TOKEN)) {
//                        collectTokenFrequency(PdfOps.c_TOKEN);
                        geometricPath = consume_c(stack, geometricPath);
                    }

                    // Stroke the path
                    else if (tok.equals(PdfOps.S_TOKEN)) {
//                        collectTokenFrequency(PdfOps.S_TOKEN);
                        geometricPath = consume_S(graphicState, shapes, geometricPath);
                    }

                    // Font selection
                    else if (tok.equals(PdfOps.Tf_TOKEN)) {
                        consume_Tf(graphicState, stack, resources);
                    }

                    // Begin a text object, initializing the text matrix, Tm, and
                    // the text line matrix, Tlm, to the identity matrix. Text
                    // objects cannot be nested; a second BT cannot appear before
                    // an ET.
                    else if (tok.equals(PdfOps.BT_TOKEN)) {
//                        collectTokenFrequency(PdfOps.BT_TOKEN);
                        // start parseText, which parses until ET is reached
                        try {
                            yBTstart = parseText(parser, shapes, yBTstart);
                        } catch (Exception e) {
                            logger.log(Level.FINEST, "Error parsing text block", e);
                        }
                    }

                    // Fill the path, using the nonzero winding number rule to
                    // determine the region to fill (see "Nonzero Winding
                    // Number Rule" ). Any subpaths that are open are implicitly
                    // closed before being filled. f or F
                    else if (tok.equals(PdfOps.F_TOKEN) ||
                            tok.equals(PdfOps.f_TOKEN)) {
//                        collectTokenFrequency(PdfOps.F_TOKEN);
//                        collectTokenFrequency(PdfOps.f_TOKEN);
                        geometricPath = consume_F(graphicState, shapes, geometricPath);
                    }

                    // Saves Graphics State, should copy the entire  graphics state onto
                    // the graphicsState object's stack
                    else if (tok.equals(PdfOps.q_TOKEN)) {
                        graphicState = consume_q(graphicState);
                    }
                    // Restore Graphics State, should restore the entire graphics state
                    // to its former value by popping it from the stack
                    else if (tok.equals(PdfOps.Q_TOKEN)) {
                        graphicState = consume_Q(graphicState, shapes);
                    }

                    // Append a rectangle to the current path as a complete subpath,
                    // with lower-left corner (x, y) and dimensions width and height
                    // in user space. The operation x y width height re is equivalent to
                    //        x y m
                    //        (x + width) y l
                    //       (x + width) (y + height) l
                    //        x (y + height) l
                    //        h
                    else if (tok.equals(PdfOps.re_TOKEN)) {
//                        collectTokenFrequency(PdfOps.re_TOKEN);
                        geometricPath = consume_re(stack, geometricPath);
                    }

                    // Modify the current transformation matrix (CTM) by concatenating the
                    // specified matrix
                    else if (tok.equals(PdfOps.cm_TOKEN)) {
                        consume_cm(graphicState, stack, inTextBlock, textBlockBase);
                    }

                    // Close the current sub path by appending a straight line segment
                    // from the current point to the starting point of the sub path.
                    // This operator terminates the current sub path; appending
                    // another segment to the current path will begin a new subpath,
                    // even if the new segment begins at the endpoint reached by the
                    // h operation. If the current subpath is already closed,
                    // h does nothing.
                    else if (tok.equals(PdfOps.h_TOKEN)) {
//                        collectTokenFrequency(PdfOps.h_TOKEN);
                        consume_h(geometricPath);
                    }

                    // Begin a marked-content sequence with an associated property
                    // list, terminated by a balancing EMC operator. tag is a name
                    // object indicating the role or significance of the sequence;
                    // properties is either an inline dictionary containing the
                    // property list or a name object associated with it in the
                    // Properties sub dictionary of the current resource dictionary
                    else if (tok.equals(PdfOps.BDC_TOKEN)) {
//                        collectTokenFrequency(PdfOps.BDC_TOKEN);
                        consume_BDC(stack, shapes,
                                oCGs, resources);
                    }

                    // End a marked-content sequence begun by a BMC or BDC operator.
                    else if (tok.equals(PdfOps.EMC_TOKEN)) {
                        consume_EMC(shapes, oCGs);
                    }

                    /**
                     * External Object (XObject) a graphics object whose contents
                     * are defined by a self-contained content stream, separate
                     * from the content stream in which it is used. There are three
                     * types of external object:
                     *
                     *   - An image XObject (Section 4.8.4, "Image Dictionaries")
                     *     represents a sampled visual image such as a photograph.
                     *   - A form XObject (Section 4.9, "Form XObjects") is a
                     *     self-contained description of an arbitrary sequence of
                     *     graphics objects.
                     *   - A PostScript XObject (Section 4.7.1, "PostScript XObjects")
                     *     contains a fragment of code expressed in the PostScript
                     *     page description language. PostScript XObjects are no
                     *     longer recommended to be used. (NOT SUPPORTED)
                     */
                    // Paint the specified XObject. The operand name must appear as
                    // a key in the XObject subdictionary of the current resource
                    // dictionary (see Section 3.7.2, "Resource Dictionaries"); the
                    // associated value must be a stream whose Type entry, if
                    // present, is XObject. The effect of Do depends on the value of
                    // the XObject's Subtype entry, which may be Image , Form, or PS
                    else if (tok.equals(PdfOps.Do_TOKEN)) {
//                        collectTokenFrequency(PdfOps.Do_TOKEN);
                        graphicState = consume_Do(graphicState, stack, shapes,
                                resources, true, imageIndex, page);
                    }

                    // Fill the path, using the even-odd rule to determine the
                    // region to fill
                    else if (tok.equals(PdfOps.f_STAR_TOKEN)) {
//                        collectTokenFrequency(PdfOps.f_STAR_TOKEN);
                        geometricPath = consume_f_star(graphicState, shapes, geometricPath);
                    }

                    // Sets the specified parameters in the graphics state.  The gs operand
                    // points to a name resource which should be a an ExtGState object.
                    // The graphics state parameters in the ExtGState must be concatenated
                    // with the the current graphics state.
                    else if (tok.equals(PdfOps.gs_TOKEN)) {
                        consume_gs(graphicState, stack, resources, shapes);
                    }

                    // End the path object without filling or stroking it. This
                    // operator is a "path-painting no-op," used primarily for the
                    // side effect of changing the current clipping path
                    else if (tok.equals(PdfOps.n_TOKEN)) {
//                        collectTokenFrequency(PdfOps.n_TOKEN);
                        geometricPath = consume_n(geometricPath);
                    }

                    // Set the line width in the graphics state
                    else if (tok.equals(PdfOps.w_TOKEN) ||
                            tok.equals(PdfOps.LW_TOKEN)) {
                        consume_w(graphicState, stack, shapes, glyph2UserSpaceScale);
                    }

                    // Modify the current clipping path by intersecting it with the
                    // current path, using the nonzero winding number rule to
                    // determine which regions lie inside the clipping path.
                    else if (tok.equals(PdfOps.W_TOKEN)) {
//                        collectTokenFrequency(PdfOps.W_TOKEN);
                        consume_W(graphicState, geometricPath);
                    }

                    // Fill Color with ColorSpace
                    else if (tok.equals(PdfOps.sc_TOKEN)) {
                        consume_sc(graphicState, stack, library, resources, false);
                    } else if (tok.equals(PdfOps.scn_TOKEN)) {
                        consume_sc(graphicState, stack, library, resources, true);
                    }

                    // Close, fill, and then stroke the path, using the nonzero
                    // winding number rule to determine the region to fill. This
                    // operator has the same effect as the sequence h B. See also
                    // "Special Path-Painting Considerations"
                    else if (tok.equals(PdfOps.b_TOKEN)) {
//                        collectTokenFrequency(PdfOps.b_TOKEN);
                        geometricPath = consume_b(graphicState, shapes, geometricPath);
                    }

                    // Same as K, but for non-stroking operations.
                    else if (tok.equals(PdfOps.k_TOKEN)) { // Fill Color CMYK
                        consume_k(graphicState, stack, library);
                    }

                    // Same as g but for none stroking operations
                    else if (tok.equals(PdfOps.g_TOKEN)) {
                        consume_g(graphicState, stack, library);
                    }

                    // Sets the flatness tolerance in the graphics state, NOT SUPPORTED
                    // flatness is a number in the range 0 to 100, a value of 0 specifies
                    // the default tolerance
                    else if (tok.equals(PdfOps.i_TOKEN)) {
                        consume_i(stack);
                    }

                    // Miter Limit
                    else if (tok.equals(PdfOps.M_TOKEN)) {
                        consume_M(graphicState, stack, shapes);
                    }

                    // Set the line cap style of the graphic state, related to Line Join
                    // style
                    else if (tok.equals(PdfOps.J_TOKEN)) {
                        consume_J(graphicState, stack, shapes);
                    }

                    // Same as RG, but for non-stroking operations.
                    else if (tok.equals(PdfOps.rg_TOKEN)) { // Fill Color RGB
                        consume_rg(graphicState, stack, library);
                    }

                    // Sets the line dash pattern in the graphics state. A normal line
                    // is [] 0.  See Graphics State -> Line dash patter for more information
                    // in the PDF Reference.  Java 2d uses the same notation so there
                    // is not much work to be done other then parsing the data.
                    else if (tok.equals(PdfOps.d_TOKEN)) {
                        consume_d(graphicState, stack, shapes);
                    }

                    // Append a cubic Bezier curve to the current path. The curve
                    // extends from the current point to the point (x3, y3), using
                    // the current point and (x2, y2) as the Bezier control points.
                    // The new current point is (x3, y3).
                    else if (tok.equals(PdfOps.v_TOKEN)) {
//                        collectTokenFrequency(PdfOps.v_TOKEN);
                        consume_v(stack, geometricPath);
                    }

                    // Set the line join style in the graphics state
                    else if (tok.equals(PdfOps.j_TOKEN)) {
                        consume_j(graphicState, stack, shapes);
                    }

                    // Append a cubic Bezier curve to the current path. The curve
                    // extends from the current point to the point (x3, y3), using
                    // (x1, y1) and (x3, y3) as the Bezier control points.
                    // The new current point is (x3, y3).
                    else if (tok.equals(PdfOps.y_TOKEN)) {
//                        collectTokenFrequency(PdfOps.y_TOKEN);
                        consume_y(stack, geometricPath);
                    }

                    // Same as CS, but for nonstroking operations.
                    else if (tok.equals(PdfOps.cs_TOKEN)) {
                        consume_cs(graphicState, stack, resources);
                    }

                    // Color rendering intent in the graphics state
                    else if (tok.equals(PdfOps.ri_TOKEN)) {
//                        collectTokenFrequency(PdfOps.ri_TOKEN);
                        stack.pop();
                    }

                    // Set the color to use for stroking operations in a device, CIE-based
                    // (other than ICCBased), or Indexed color space. The number of operands
                    // required and their interpretation depends on the current stroking color space:
                    //   - For DeviceGray, CalGray, and Indexed color spaces, one operand
                    //     is required (n = 1).
                    //   - For DeviceRGB, CalRGB, and Lab color spaces, three operands are
                    //     required (n = 3).
                    //   - For DeviceCMYK, four operands are required (n = 4).
                    else if (tok.equals(PdfOps.SC_TOKEN)) { // Stroke Color with ColorSpace
                        consume_SC(graphicState, stack, library, resources, false);
                    } else if (tok.equals(PdfOps.SCN_TOKEN)) { // Stroke Color with ColorSpace
                        consume_SC(graphicState, stack, library, resources, true);
                    }

                    // Fill and then stroke the path, using the nonzero winding
                    // number rule to determine the region to fill. This produces
                    // the same result as constructing two identical path objects,
                    // painting the first with f and the second with S. Note,
                    // however, that the filling and stroking portions of the
                    // operation consult different values of several graphics state
                    // parameters, such as the current color.
                    else if (tok.equals(PdfOps.B_TOKEN)) {
//                        collectTokenFrequency(PdfOps.B_TOKEN);
                        geometricPath = consume_B(graphicState, shapes,
                                geometricPath);
                    }

                    // Set the stroking color space to DeviceCMYK (or the DefaultCMYK color
                    // space; see "Default Color Spaces" on page 227) and set the color to
                    // use for stroking operations. Each operand must be a number between
                    // 0.0 (zero concentration) and 1.0 (maximum concentration). The
                    // behavior of this operator is affected by the overprint mode
                    // (see Section 4.5.6, "Overprint Control").
                    else if (tok.equals(PdfOps.K_TOKEN)) { // Stroke Color CMYK
                        consume_K(graphicState, stack, library);
                    }

                    /**
                     * Type3 operators, update the text state with data from these operands
                     */
                    else if (tok.equals(PdfOps.d0_TOKEN)) {
//                        collectTokenFrequency(PdfOps.d0_TOKEN);
                        graphicState = consume_d0(graphicState, stack);
                    }

                    // Close and stroke the path. This operator has the same effect
                    // as the sequence h S.
                    else if (tok.equals(PdfOps.s_TOKEN)) {
//                        collectTokenFrequency(PdfOps.s_TOKEN);
                        geometricPath = consume_s(graphicState, shapes, geometricPath);
                    }

                    // Set the stroking color space to DeviceGray (or the DefaultGray color
                    // space; see "Default Color Spaces" ) and set the gray level to use for
                    // stroking operations. gray is a number between 0.0 (black)
                    // and 1.0 (white).
                    else if (tok.equals(PdfOps.G_TOKEN)) {
                        consume_G(graphicState, stack, library);
                    }

                    // Close, fill, and then stroke the path, using the even-odd
                    // rule to determine the region to fill. This operator has the
                    // same effect as the sequence h B*. See also "Special
                    // Path-Painting Considerations"
                    else if (tok.equals(PdfOps.b_STAR_TOKEN)) {
//                        collectTokenFrequency(PdfOps.b_STAR_TOKEN);
                        geometricPath = consume_b_star(graphicState,
                                shapes, geometricPath);
                    }

                    // Set the stroking color space to DeviceRGB (or the DefaultRGB color
                    // space; see "Default Color Spaces" on page 227) and set the color to
                    // use for stroking operations. Each operand must be a number between
                    // 0.0 (minimum intensity) and 1.0 (maximum intensity).
                    else if (tok.equals(PdfOps.RG_TOKEN)) { // Stroke Color RGB
                        consume_RG(graphicState, stack, library);
                    }

                    // Set the current color space to use for stroking operations. The
                    // operand name must be a name object. If the color space is one that
                    // can be specified by a name and no additional parameters (DeviceGray,
                    // DeviceRGB, DeviceCMYK, and certain cases of Pattern), the name may be
                    // specified directly. Otherwise, it must be a name defined in the
                    // ColorSpace sub dictionary of the current resource dictionary; the
                    // associated value is an array describing the color space.
                    // <b>Note:</b>
                    // The names DeviceGray, DeviceRGB, DeviceCMYK, and Pattern always
                    // identify the corresponding color spaces directly; they never refer to
                    // resources in the ColorSpace sub dictionary. The CS operator also sets
                    // the current stroking color to its initial value, which depends on the
                    // color space:
                    // <li>In a DeviceGray, DeviceRGB, CalGray, or CalRGB color space, the
                    //     initial color has all components equal to 0.0.</li>
                    // <li>In a DeviceCMYK color space, the initial color is
                    //     [0.0 0.0 0.0 1.0].   </li>
                    // <li>In a Lab or ICCBased color space, the initial color has all
                    //     components equal to 0.0 unless that falls outside the intervals
                    //     specified by the space's Range entry, in which case the nearest
                    //     valid value is substituted.</li>
                    // <li>In an Indexed color space, the initial color value is 0. </li>
                    // <li>In a Separation or DeviceN color space, the initial tint value is
                    //     1.0 for all colorants. </li>
                    // <li>In a Pattern color space, the initial color is a pattern object
                    //     that causes nothing to be painted. </li>
                    else if (tok.equals(PdfOps.CS_TOKEN)) {
                        consume_CS(graphicState, stack, resources);
                    } else if (tok.equals(PdfOps.d1_TOKEN)) {
//                        collectTokenFrequency(PdfOps.d1_TOKEN);
                        graphicState = consume_d1(graphicState, stack
                        );
                    }

                    // Fill and then stroke the path, using the even-odd rule to
                    // determine the region to fill. This operator produces the same
                    // result as B, except that the path is filled as if with f*
                    // instead of f. See also "Special Path-Painting Considerations"
                    else if (tok.equals(PdfOps.B_STAR_TOKEN)) {
//                        collectTokenFrequency(PdfOps.B_STAR_TOKEN);
                        geometricPath = consume_B_star(graphicState, shapes, geometricPath);
                    }

                    // Begin a marked-content sequence terminated by a balancing EMC
                    // operator.tag is a name object indicating the role or
                    // significance of the sequence.
                    else if (tok.equals(PdfOps.BMC_TOKEN)) {
                        consume_BMC(stack, shapes, oCGs, resources);
                    }

                    // Begin an inline image object
                    else if (tok.equals(PdfOps.BI_TOKEN)) {
//                        collectTokenFrequency(PdfOps.BI_TOKEN);
                        // start parsing image object, which leads to ID and EI
                        // tokends.
                        //    ID - Begin in the image data for an inline image object
                        //    EI - End an inline image object
                        parseInlineImage(parser, shapes);
                    }

                    // Begin a compatibility section. Unrecognized operators
                    // (along with their operands) will be ignored without error
                    // until the balancing EX operator is encountered.
                    else if (tok.equals(PdfOps.BX_TOKEN)) {
//                        collectTokenFrequency(PdfOps.BX_TOKEN);
                    }
                    // End a compatibility section begun by a balancing BX operator.
                    else if (tok.equals(PdfOps.EX_TOKEN)) {
//                        collectTokenFrequency(PdfOps.EX_TOKEN);
                    }

                    // Modify the current clipping path by intersecting it with the
                    // current path, using the even-odd rule to determine which
                    // regions lie inside the clipping path.
                    else if (tok.equals(PdfOps.W_STAR_TOKEN)) {
                        consume_W_star(graphicState, geometricPath);
                    }

                    /**
                     * Single marked-content point
                     */
                    // Designate a marked-content point with an associated property
                    // list. tag is a name object indicating the role or significance
                    // of the point; properties is either an in line dictionary
                    // containing the property list or a name object associated with
                    // it in the Properties sub dictionary of the current resource
                    // dictionary.
                    else if (tok.equals(PdfOps.DP_TOKEN)) {
//                        collectTokenFrequency(PdfOps.DP_TOKEN);
                        consume_DP(stack);
                    }
                    // Designate a marked-content point. tag is a name object
                    // indicating the role or significance of the point.
                    else if (tok.equals(PdfOps.MP_TOKEN)) {
//                        collectTokenFrequency(PdfOps.MP_TOKEN);
                        consume_MP(stack);
                    }

                    // shading operator.
                    else if (tok.equals(PdfOps.sh_TOKEN)) {
//                        collectTokenFrequency(PdfOps.sh_TOKEN);
                        consume_sh(graphicState, stack, shapes,
                                resources);
                    }

                    /**
                     * We've seen a couple cases when the text state parameters are written
                     * outside of text blocks, this should cover these cases.
                     */
                    // Character Spacing
                    else if (tok.equals(PdfOps.Tc_TOKEN)) {
                        consume_Tc(graphicState, stack);
                    }
                    // Word spacing
                    else if (tok.equals(PdfOps.Tw_TOKEN)) {
                        consume_Tw(graphicState, stack);
                    }
                    // Text leading
                    else if (tok.equals(PdfOps.TL_TOKEN)) {
                        consume_TL(graphicState, stack);
                    }
                    // Rendering mode
                    else if (tok.equals(PdfOps.Tr_TOKEN)) {
                        consume_Tr(graphicState, stack);
                    }
                    // Horizontal scaling
                    else if (tok.equals(PdfOps.Tz_TOKEN)) {
                        consume_Tz(graphicState, stack);
                    }
                    // Text rise
                    else if (tok.equals(PdfOps.Ts_TOKEN)) {
                        consume_Ts(graphicState, stack);
                    }
                }
            }
        } catch (IOException e) {
            // eat the result as it a normal occurrence
            logger.finer("End of Content Stream");
        } catch (NoninvertibleTransformException e) {
            logger.log(Level.WARNING, "Error creating inverse transform:", e);
        } catch (InterruptedException e){
            throw new InterruptedException(e.getMessage());
        } finally {
            // End of stream set alpha state back to 1.0f, so that other
            // streams aren't applied an incorrect alpha value.
            setAlpha(shapes, graphicState, AlphaComposite.SRC_OVER, 1.0f);
        }
//        long endTime = System.currentTimeMillis();
//        System.out.println("Paring Duration " + (endTime - startTime));
//        printTokenFrequency();

        // Print off anything left on the stack, any "Stack" traces should
        // indicate a parsing problem or a not supported operand
        while (!stack.isEmpty()) {
            String tmp = stack.pop().toString();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("STACK=" + tmp);
            }
        }

//        shapes.contract();
        return this;
    }


    /**
     * Specialized method for extracting text from documents.
     *
     * @param source content stream source.
     * @return vector where each entry is the text extracted from a text block.
     */
    public Shapes parseTextBlocks(byte[][] source) throws UnsupportedEncodingException, InterruptedException {

        // great a parser to get tokens for stream
        Parser parser = new Parser(new ByteDoubleArrayInputStream(source));
        Shapes shapes = new Shapes();

        if (graphicState == null) {
            graphicState = new GraphicsState(shapes);
        }

//        long startTime = System.currentTimeMillis();
        try {

            // loop through each token returned form the parser
            Object tok = parser.getStreamObject();
            Stack<Object> stack = new Stack<Object>();
            double yBTstart = 0;
            while (tok != null) {

                // add any names and numbers and every thing else on the
                // stack for future reference
                if (tok instanceof String) {

                    if (tok.equals(PdfOps.BT_TOKEN)) {
                        // start parseText, which parses until ET is reached
                        yBTstart = parseText(parser, shapes, yBTstart);
                        // free up some memory along the way. we don't need
                        // a full stack consume Tf tokens.
                        stack.clear();
                    }
                    // for malformed core docs we need to consume any font
                    // to ensure we can result toUnicode values.
                    else if (tok.equals(PdfOps.Tf_TOKEN)) {
                        consume_Tf(graphicState, stack, resources);
                        stack.clear();
                    }
                    // pick up on xObject content streams.
                    else if (tok.equals(PdfOps.Do_TOKEN)) {
                        consume_Do(graphicState, stack, shapes, resources, false, new AtomicInteger(0), null);
                        stack.clear();
                    }
                } else {
                    stack.push(tok);
                }
                tok = parser.getStreamObject();
            }
            // clear our temporary stack.
            stack.clear();
        } catch (IOException e) {
            // eat the result as it a normal occurrence
            logger.finer("End of Content Stream");
        }
//        long endTime = System.currentTimeMillis();
//        System.out.println("Extraction Duration " + (endTime - startTime));
        shapes.contract();
        return shapes;
    }

    /**
     * Parses Text found with in a BT block.
     *
     * @param parser          parser containging BT tokens
     * @param shapes          container of all shapes for the page content being parsed
     * @param previousBTStart y offset of previous BT definition.
     * @return y offset of the this BT definition.
     * @throws java.io.IOException end of content stream is found
     */
    float parseText(Parser parser, Shapes shapes, double previousBTStart)
            throws IOException, InterruptedException {
        Object nextToken;
        inTextBlock = true;
        // keeps track of previous text placement so that Compatibility and
        // implementation note 57 is respected.  That is text drawn after a TJ
        // must not be less then the previous glyphs coords.
        TextMetrics textMetrics = new TextMetrics();
        textBlockBase = new AffineTransform(graphicState.getCTM());

        // transformation matrix used to cMap core space to drawing space
        graphicState.getTextState().tmatrix = new AffineTransform();
        graphicState.getTextState().tlmatrix = new AffineTransform();
        graphicState.scale(1, -1);

        // get reference to PageText.
        PageText pageText = shapes.getPageText();

        // glyphOutline to support text clipping modes, life span is BT->ET.
        GlyphOutlineClip glyphOutlineClip = new GlyphOutlineClip();

        // start parsing of the BT block
        nextToken = parser.getStreamObject();
        while (!nextToken.equals(PdfOps.ET_TOKEN)) { // ET - end text object
            // add names to the stack, save for later parsing, colour state
            // and graphics state (includes font).

            if (nextToken instanceof String) {

                // Normal text token, string, hex
                if (nextToken.equals(PdfOps.Tj_TOKEN)) {
//                    collectTokenFrequency(PdfOps.Tj_TOKEN);
                    consume_Tj(graphicState, stack, shapes,
                            textMetrics, glyphOutlineClip, oCGs);
                }

                // Character Spacing
                else if (nextToken.equals(PdfOps.Tc_TOKEN)) {
//                    collectTokenFrequency(PdfOps.Tc_TOKEN);
                    consume_Tc(graphicState, stack);
                }

                // Word spacing
                else if (nextToken.equals(PdfOps.Tw_TOKEN)) {
//                    collectTokenFrequency(PdfOps.Tw_TOKEN);
                    consume_Tw(graphicState, stack);
                }

                // move to the start of he next line, offset from the start of the
                // current line by (tx,ty)*tx
                else if (nextToken.equals(PdfOps.Td_TOKEN)) {
//                    collectTokenFrequency(PdfOps.Td_TOKEN);
                    consume_Td(graphicState, stack, textMetrics, pageText,
                            previousBTStart, oCGs);
                }

                /**
                 * Tranformation matrix
                 * tm =   |f1 f2 0|
                 *        |f3 f4 0|
                 *        |f5 f6 0|
                 */
                else if (nextToken.equals(PdfOps.Tm_TOKEN)) {
//                    collectTokenFrequency(PdfOps.Tm_TOKEN);
                    consume_tm(graphicState, stack, textMetrics, pageText,
                            previousBTStart, textBlockBase, oCGs);
                }

                // Font selection
                else if (nextToken.equals(PdfOps.Tf_TOKEN)) {
                    consume_Tf(graphicState, stack, resources);
                }

                // TJ marks a vector, where.......
                else if (nextToken.equals(PdfOps.TJ_TOKEN)) {
//                    collectTokenFrequency(PdfOps.TJ_TOKEN);
                    consume_TJ(graphicState, stack, shapes,
                            textMetrics, glyphOutlineClip, oCGs);
                }

                // Move to the start of the next line, offset from the start of the
                // current line by (tx,ty)
                else if (nextToken.equals(PdfOps.TD_TOKEN)) {
//                    collectTokenFrequency(PdfOps.TD_TOKEN);
                    consume_TD(graphicState, stack, textMetrics, pageText, oCGs);
                }

                // Text leading
                else if (nextToken.equals(PdfOps.TL_TOKEN)) {
//                    collectTokenFrequency(PdfOps.TL_TOKEN);
                    consume_TL(graphicState, stack);
                }

                // Saves Graphics State, should copy the entire  graphics state onto
                // the graphicsState object's stack
                else if (nextToken.equals(PdfOps.q_TOKEN)) {
                    graphicState = consume_q(graphicState);
                }
                // Restore Graphics State, should restore the entire graphics state
                // to its former value by popping it from the stack
                else if (nextToken.equals(PdfOps.Q_TOKEN)) {
                    graphicState = consume_Q(graphicState, shapes);
                }

                // Modify the current transformation matrix (CTM) by concatenating the
                // specified matrix
                else if (nextToken.equals(PdfOps.cm_TOKEN)) {
                    consume_cm(graphicState, stack, inTextBlock, textBlockBase);
                }

                // Move to the start of the next line
                else if (nextToken.equals(PdfOps.T_STAR_TOKEN)) {
//                    collectTokenFrequency(PdfOps.T_STAR_TOKEN);
                    consume_T_star(graphicState, textMetrics, pageText, oCGs);
                } else if (nextToken.equals(PdfOps.BDC_TOKEN)) {
//                    collectTokenFrequency(PdfOps.BDC_TOKEN);
                    consume_BDC(stack, shapes,
                            oCGs, resources);
                } else if (nextToken.equals(PdfOps.EMC_TOKEN)) {
//                    collectTokenFrequency(PdfOps.EMC_TOKEN);
                    consume_EMC(shapes, oCGs);
                }

                // Sets the specified parameters in the graphics state.  The gs operand
                // points to a name resource which should be a an ExtGState object.
                // The graphics state parameters in the ExtGState must be concatenated
                // with the the current graphics state.
                else if (nextToken.equals(PdfOps.gs_TOKEN)) {
                    consume_gs(graphicState, stack, resources, shapes);
                }

                // Set the line width in the graphics state
                else if (nextToken.equals(PdfOps.w_TOKEN) ||
                        nextToken.equals(PdfOps.LW_TOKEN)) {
                    consume_w(graphicState, stack, shapes, glyph2UserSpaceScale);
                }

                // Fill Color with ColorSpace
                else if (nextToken.equals(PdfOps.sc_TOKEN)) {
                    consume_sc(graphicState, stack, library, resources, false);
                } else if (nextToken.equals(PdfOps.scn_TOKEN)) {
                    consume_sc(graphicState, stack, library, resources, true);
                }

                // Same as K, but for nonstroking operations.
                else if (nextToken.equals(PdfOps.k_TOKEN)) { // Fill Color CMYK
                    consume_k(graphicState, stack, library);
                }

                // Same as g but for none stroking operations
                else if (nextToken.equals(PdfOps.g_TOKEN)) {
                    consume_g(graphicState, stack, library);
                }

                // Sets the flatness tolerance in the graphics state, NOT SUPPORTED
                // flatness is a number in the range 0 to 100, a value of 0 specifies
                // the default tolerance
                else if (nextToken.equals(PdfOps.i_TOKEN)) {
                    consume_i(stack);
                }

                // Miter Limit
                else if (nextToken.equals(PdfOps.M_TOKEN)) {
                    consume_M(graphicState, stack, shapes);
                }

                // Set the line cap style of the graphic state, related to Line Join
                // style
                else if (nextToken.equals(PdfOps.J_TOKEN)) {
                    consume_J(graphicState, stack, shapes);
                }

                // Same as RG, but for nonstroking operations.
                else if (nextToken.equals(PdfOps.rg_TOKEN)) { // Fill Color RGB
                    consume_rg(graphicState, stack, library);
                }

                // Sets the line dash pattern in the graphics state. A normal line
                // is [] 0.  See Graphics State -> Line dash patter for more information
                // in the PDF Reference.  Java 2d uses the same notation so there
                // is not much work to be done other then parsing the data.
                else if (nextToken.equals(PdfOps.d_TOKEN)) {
                    consume_d(graphicState, stack, shapes);
                }

                // Set the line join style in the graphics state
                else if (nextToken.equals(PdfOps.j_TOKEN)) {
                    consume_j(graphicState, stack, shapes);
                }

                // Same as CS, but for non-stroking operations.
                else if (nextToken.equals(PdfOps.cs_TOKEN)) {
                    consume_cs(graphicState, stack, resources);
                }

                // Set the color rendering intent in the graphics state
                else if (nextToken.equals("ri")) {
//                    collectTokenFrequency(PdfOps.ri_TOKEN);
                    consume_ri(stack);
                }

                // Set the color to use for stroking operations in a device, CIE-based
                // (other than ICCBased), or Indexed color space. The number of operands
                // required and their interpretation depends on the current stroking color space:
                //   - For DeviceGray, CalGray, and Indexed color spaces, one operand
                //     is required (n = 1).
                //   - For DeviceRGB, CalRGB, and Lab color spaces, three operands are
                //     required (n = 3).
                //   - For DeviceCMYK, four operands are required (n = 4).
                else if (nextToken.equals(PdfOps.SC_TOKEN)) { // Stroke Color with ColorSpace
                    consume_SC(graphicState, stack, library, resources, false);
                } else if (nextToken.equals(PdfOps.SCN_TOKEN)) { // Stroke Color with ColorSpace
                    consume_SC(graphicState, stack, library, resources, true);
                }

                // Set the stroking color space to DeviceCMYK (or the DefaultCMYK color
                // space; see "Default Color Spaces" on page 227) and set the color to
                // use for stroking operations. Each operand must be a number between
                // 0.0 (zero concentration) and 1.0 (maximum concentration). The
                // behavior of this operator is affected by the overprint mode
                // (see Section 4.5.6, "Overprint Control").
                else if (nextToken.equals(PdfOps.K_TOKEN)) { // Stroke Color CMYK
                    consume_K(graphicState, stack, library);
                }

                // Set the stroking color space to DeviceGray (or the DefaultGray color
                // space; see "Default Color Spaces" ) and set the gray level to use for
                // stroking operations. gray is a number between 0.0 (black)
                // and 1.0 (white).
                else if (nextToken.equals(PdfOps.G_TOKEN)) {
                    consume_G(graphicState, stack, library);
                }

                // Set the stroking color space to DeviceRGB (or the DefaultRGB color
                // space; see "Default Color Spaces" on page 227) and set the color to
                // use for stroking operations. Each operand must be a number between
                // 0.0 (minimum intensity) and 1.0 (maximum intensity).
                else if (nextToken.equals(PdfOps.RG_TOKEN)) { // Stroke Color RGB
                    consume_RG(graphicState, stack, library);
                } else if (nextToken.equals(PdfOps.CS_TOKEN)) {
                    consume_CS(graphicState, stack, resources);
                }

                // Rendering mode
                else if (nextToken.equals(PdfOps.Tr_TOKEN)) {
//                    collectTokenFrequency(PdfOps.Tr_TOKEN);
                    consume_Tr(graphicState, stack);
                }

                // Horizontal scaling
                else if (nextToken.equals(PdfOps.Tz_TOKEN)) {
//                    collectTokenFrequency(PdfOps.Tz_TOKEN);
                    consume_Tz(graphicState, stack);
                }

                // Text rise
                else if (nextToken.equals(PdfOps.Ts_TOKEN)) {
//                    collectTokenFrequency(PdfOps.Ts_TOKEN);
                    consume_Ts(graphicState, stack);
                }

                /**
                 * Begin a compatibility section. Unrecognized operators (along with
                 * their operands) will be ignored without error until the balancing
                 * EX operator is encountered.
                 */
                else if (nextToken.equals(PdfOps.BX_TOKEN)) {
//                    collectTokenFrequency(PdfOps.BX_TOKEN);
                }
//                 End a compatibility section begun by a balancing BX operator.
                else if (nextToken.equals(PdfOps.EX_TOKEN)) {
//                    collectTokenFrequency(PdfOps.EX_TOKEN);
                }
                // Move to the next line and show a text string.
                else if (nextToken.equals(PdfOps.SINGLE_QUOTE_TOKEN)) {
//                    collectTokenFrequency(PdfOps.SINGLE_QUOTE_TOKEN);
                    consume_single_quote(graphicState, stack, shapes, textMetrics,
                            glyphOutlineClip, oCGs);
                }
                /**
                 * Move to the next line and show a text string, using aw as the
                 * word spacing and ac as the character spacing (setting the
                 * corresponding parameters in the text state). aw and ac are
                 * numbers expressed in unscaled text space units.
                 */
                else if (nextToken.equals(PdfOps.DOUBLE_QUOTE__TOKEN)) {
//                    collectTokenFrequency(PdfOps.DOUBLE_QUOTE__TOKEN);
                    consume_double_quote(graphicState, stack, shapes, textMetrics,
                            glyphOutlineClip, oCGs);
                }
            }
            // push everything else on the stack for consumptions
            else {
                stack.push(nextToken);
            }

            nextToken = parser.getStreamObject();
        }
        // during a BT -> ET text parse there is a change that we might be
        // in MODE_ADD or MODE_Fill_Add which require that the we push the
        // shapes that make up the clipping path to the shapes stack.  When
        // encountered the path will be used as the current clip.
        if (!glyphOutlineClip.isEmpty()) {
            // set the clips so further clips can use the clip outline
            graphicState.setClip(glyphOutlineClip.getGlyphOutlineClip());
            // add the glyphOutline so the clip can be calculated.
            shapes.add(new GlyphOutlineDrawCmd(glyphOutlineClip));
        }

        // get rid of the rest
        while (!stack.isEmpty()) {
            String tmp = stack.pop().toString();
            if (logger.isLoggable(Level.FINE)) {
                logger.warning("Text=" + tmp);
            }
        }
        graphicState.set(textBlockBase);
        inTextBlock = false;

        return textMetrics.getyBTStart();
    }


    private void parseInlineImage(Parser p, Shapes shapes) throws IOException {
        try {
            //int width = 0, height = 0, bitspercomponent = 0;
            // boolean imageMask = false; // from old pdfgo never used
            // PColorSpace cs = null; // from old pdfgo never used

            Object tok;
            HashMap<Object, Object> iih = new HashMap<Object, Object>();
            tok = p.getStreamObject();
            while (!tok.equals("ID")) {
                if (tok.equals("BPC")) {
                    tok = new Name("BitsPerComponent");
                } else if (tok.equals("CS")) {
                    tok = new Name("ColorSpace");
                } else if (tok.equals("D")) {
                    tok = new Name("Decode");
                } else if (tok.equals("DP")) {
                    tok = new Name("DecodeParms");
                } else if (tok.equals("F")) {
                    tok = new Name("Filter");
                } else if (tok.equals("H")) {
                    tok = new Name("Height");
                } else if (tok.equals("IM")) {
                    tok = new Name("ImageMask");
                } else if (tok.equals("I")) {
                    tok = new Name("Indexed");
                } else if (tok.equals("W")) {
                    tok = new Name("Width");
                }
                Object tok1 = p.getStreamObject();
                //System.err.println(tok+" - "+tok1);
                iih.put(tok, tok1);
                tok = p.getStreamObject();
            }
            // For inline images in content streams, we have to use
            //   a byte[], instead of going back to the original file,
            //   to reget the image data, because the inline image is
            //   only a small part of a content stream, which is also
            //   filtered, and potentially concatenated with other
            //   content streams.
            // Long story short: it's too hard to re-get from PDF file
            // Now, since non-inline-image streams can go back to the
            //   file, we have to fake it as coming from the file ...
            ByteArrayOutputStream buf = new ByteArrayOutputStream(4096);
            tok = p.peek2();
            boolean ateEI = false;
            while (tok != null && !tok.equals(" EI")) {
                ateEI = p.readLineForInlineImage(buf);
                if (ateEI)
                    break;
                tok = p.peek2();
            }
            if (!ateEI) {
                // get rid of trash...
                p.getToken();
            }
            buf.flush();
            buf.close();

            byte[] data = buf.toByteArray();
            // create the image stream
            ImageStream st = new ImageStream(library, iih, data);
            ImageReference imageStreamReference =
                    new InlineImageStreamReference(st, graphicState, resources, 0, null);
//            ImageUtility.displayImage(imageStreamReference.getImage(), "BI");
            AffineTransform af = new AffineTransform(graphicState.getCTM());
            graphicState.scale(1, -1);
            graphicState.translate(0, -1);
            shapes.add(new ImageDrawCmd(imageStreamReference));
            graphicState.set(af);

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.FINE, "Error parsing inline image.", e);
        }
    }


}
