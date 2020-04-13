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
package org.icepdf.core.pobjects.graphics.commands;

import org.icepdf.core.pobjects.LiteralStringObject;
import org.icepdf.core.pobjects.graphics.TextSprite;
import org.icepdf.core.pobjects.graphics.text.GlyphText;
import org.icepdf.core.util.PdfOps;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The PostScriptEncoder is responsible for converting an ArrayList<DrawCmd>
 * into postscript operands.  Basically the reverse of what the content
 * parser does.
 * <p/>
 * NOTE: this is currently a partial implementation to vac
 *
 * @since 5.0
 */
public class PostScriptEncoder {

    private static final Logger logger =
            Logger.getLogger(PostScriptEncoder.class.toString());

    private static final String SPACE = " ";
    private static final String NEWLINE = "\r\n";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String NAME = "/";
    private static final String BEGIN_ARRAY = "[";
    private static final String END_ARRAY = "]";
    private static final String BEGIN_STRING = "(";
    private static final String END_STRING = ")";

    private PostScriptEncoder() {

    }

    /**
     * Processes the given DrawCmd objects and generates the PostScript to
     * draw simple shapes and text.
     *
     * @param drawCmds commands to convert to postscript.
     * @return byte[] of PostScript notation.
     */
    public static byte[] generatePostScript(ArrayList<DrawCmd> drawCmds) {
        StringBuilder postScript = new StringBuilder();
        Color color = null;
        Shape currentShape = null;
        if (logger.isLoggable(Level.FINEST)) {
            if (drawCmds != null) {
                logger.finest("PostScriptEncoder processing: " +
                        drawCmds.size() + " commands.");
            } else {
                logger.finest("PostScriptEncoder does not have any shapes to process. ");
            }
        }

        try {
            for (DrawCmd drawCmd : drawCmds) {
                // setup an affine transform
                if (drawCmd instanceof TransformDrawCmd) {
                    AffineTransform af = ((TransformDrawCmd) drawCmd).getAffineTransform();
                    postScript.append(af.getScaleX()).append(SPACE)
                            .append(af.getShearX()).append(SPACE)
                            .append(af.getShearY()).append(SPACE)
                            .append(af.getScaleY()).append(SPACE)
                            .append(af.getTranslateX()).append(SPACE)
                            .append(af.getTranslateY()).append(SPACE)
                            .append(PdfOps.cm_TOKEN).append(NEWLINE);
                } else if (drawCmd instanceof TextTransformDrawCmd) {
                    AffineTransform af = ((TransformDrawCmd) drawCmd).getAffineTransform();
                    postScript.append(af.getScaleX()).append(SPACE)
                            .append(af.getShearX()).append(SPACE)
                            .append(af.getShearY()).append(SPACE)
                            .append(af.getScaleY()).append(SPACE)
                            .append(af.getTranslateX()).append(SPACE)
                            .append(af.getTranslateY()).append(SPACE)
                            .append(PdfOps.Tm_TOKEN).append(NEWLINE);
                }
                // reference the colour, we'll decide later if its fill or stroke.
                else if (drawCmd instanceof ColorDrawCmd) {
                    color = ((ColorDrawCmd) drawCmd).getColor();
                }
                // stroke the shape.
                else if (drawCmd instanceof DrawDrawCmd) {
                    if (color != null) {
                        float[] colors = color.getRGBColorComponents(null);
                        // set the stroke color
                        postScript.append(colors[0]).append(SPACE)
                                .append(colors[1]).append(SPACE)
                                .append(colors[2]).append(SPACE)
                                .append(PdfOps.RG_TOKEN).append(NEWLINE);
                        // generate the draw operands for current shape.
                        generateShapePostScript(currentShape, postScript);
                        // add  the fill
                        postScript.append(PdfOps.S_TOKEN).append(NEWLINE);
                    }
                }
                // fill the shape.
                else if (drawCmd instanceof FillDrawCmd) {
                    if (color != null) {
                        float[] colors = color.getRGBColorComponents(null);
                        // set fill color
                        postScript.append(colors[0]).append(SPACE)
                                .append(colors[1]).append(SPACE)
                                .append(colors[2]).append(SPACE)
                                .append(PdfOps.rg_TOKEN).append(NEWLINE);
                        // generate the draw operands for the current shape.
                        generateShapePostScript(currentShape, postScript);
                        // add  the fill
                        postScript.append(PdfOps.f_TOKEN).append(SPACE);
                    }
                }
                // current shape.
                else if (drawCmd instanceof ShapeDrawCmd) {
                    currentShape = ((ShapeDrawCmd) drawCmd).getShape();
                }
                // Sets the stroke.
                else if (drawCmd instanceof StrokeDrawCmd) {
                    BasicStroke stroke = (BasicStroke) ((StrokeDrawCmd) drawCmd).getStroke();
                    postScript.append(
                            // line width
                            stroke.getLineWidth()).append(SPACE)
                            .append(PdfOps.w_TOKEN).append(SPACE);
                    // dash phase
                    float[] dashes = stroke.getDashArray();
                    postScript.append(BEGIN_ARRAY);
                    if (dashes != null) {
                        for (int i = 0, max = dashes.length; i < max; i++) {
                            postScript.append(dashes[i]);
                            if (i < max - 1) {
                                postScript.append(SPACE);
                            }
                        }
                    }
                    postScript.append(END_ARRAY).append(SPACE);
                    postScript.append(stroke.getDashPhase()).append(SPACE)
                            .append(PdfOps.d_TOKEN).append(SPACE);
                    // cap butt
                    if (stroke.getEndCap() == BasicStroke.CAP_BUTT) {
                        postScript.append(0).append(SPACE)
                                .append(PdfOps.J_TOKEN).append(SPACE);
                    } else if (stroke.getEndCap() == BasicStroke.CAP_ROUND) {
                        postScript.append(1).append(SPACE)
                                .append(PdfOps.J_TOKEN).append(SPACE);
                    } else if (stroke.getEndCap() == BasicStroke.CAP_SQUARE) {
                        postScript.append(2).append(SPACE)
                                .append(PdfOps.J_TOKEN).append(SPACE);
                    }
                    // miter join.
                    if (stroke.getMiterLimit() == BasicStroke.JOIN_MITER) {
                        postScript.append(0).append(SPACE)
                                .append(PdfOps.j_TOKEN).append(SPACE);
                    } else if (stroke.getMiterLimit() == BasicStroke.JOIN_ROUND) {
                        postScript.append(1).append(SPACE)
                                .append(PdfOps.j_TOKEN).append(SPACE);
                    } else if (stroke.getMiterLimit() == BasicStroke.JOIN_BEVEL) {
                        postScript.append(2).append(SPACE)
                                .append(PdfOps.j_TOKEN).append(SPACE);
                    }
                    postScript.append(NEWLINE);
                }
                // graphics state setup
                else if (drawCmd instanceof GraphicsStateCmd) {
                    postScript.append('/')
                            .append(((GraphicsStateCmd) drawCmd).getGraphicStateName()).append(SPACE)
                            .append(PdfOps.gs_TOKEN).append(SPACE);
                }
                // break out a text block and child paint operands.
                else if (drawCmd instanceof TextSpriteDrawCmd) {
                    postScript.append(PdfOps.BT_TOKEN).append(NEWLINE);
                    TextSpriteDrawCmd textSpriteDrawCmd = (TextSpriteDrawCmd) drawCmd;
                    TextSprite textSprite = textSpriteDrawCmd.getTextSprite();

                    ArrayList<GlyphText> glyphTexts = textSprite.getGlyphSprites();
                    if (glyphTexts.size() > 0) {
                        // write out stat of text paint
                        postScript.append("1 0 0 -1 ")
                                .append(glyphTexts.get(0).getX()).append(SPACE)
                                .append(glyphTexts.get(0).getY()).append(SPACE).append(PdfOps.Tm_TOKEN).append(NEWLINE);

                        // write out font
                        postScript.append("/").append(textSprite.getFontName()).append(SPACE)
                                .append(textSprite.getFontSize()).append(SPACE).append(PdfOps.Tf_TOKEN).append(NEWLINE);

                        // set the colour
                        float[] colors = textSprite.getStrokeColor().getRGBColorComponents(null);
                        // set fill color
                        postScript.append(colors[0]).append(SPACE)
                                .append(colors[1]).append(SPACE)
                                .append(colors[2]).append(SPACE)
                                .append(PdfOps.rg_TOKEN).append(NEWLINE);
                        float y = glyphTexts.get(0).getY();
                        StringBuilder line = new StringBuilder();
                        GlyphText glyphText;
                        for (int i = 0, max = glyphTexts.size(); i < max; i++) {
                            glyphText = glyphTexts.get(i);

                            // write out the line
                            if (y != glyphText.getY() || i == max - 1) {
                                // make sure we write out the last character.
                                if (i == max - 1) {
                                    line.append(glyphText.getUnicode());
                                }
                                postScript.append(BEGIN_ARRAY).append(BEGIN_STRING)
                                        // use literal string to make sure string is escaped correctly
                                        .append(new LiteralStringObject(line.toString()).toString())
                                        .append(END_STRING)
                                        .append(END_ARRAY).append(SPACE)
                                        .append(PdfOps.TJ_TOKEN).append(NEWLINE);
                                // add shift if newline
                                postScript.append(0).append(SPACE).append(y - glyphText.getY())
                                        .append(SPACE).append(PdfOps.Td_TOKEN).append(NEWLINE);
                                // update the current.
                                y = glyphText.getY();
                                line = new StringBuilder();
                            }
                            line.append(glyphText.getUnicode());
                        }
                        postScript.append(PdfOps.ET_TOKEN).append(NEWLINE);
                    }
                }
            }
        } catch (Throwable e) {
            logger.log(Level.WARNING, "Error encoding PostScript notation ", e);
        }
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("PostEncoding: " + postScript.toString());
        }
        return postScript.toString().getBytes();
    }

    /**
     * Utility to create postscript draw operations from a shapes path
     * iterator.
     *
     * @param currentShape shape to build out draw commands.
     * @param postScript   string to append draw operands to.
     */
    private static void generateShapePostScript(Shape currentShape, StringBuilder postScript) {
        PathIterator pathIterator = currentShape.getPathIterator(null);
        float[] segment = new float[6];
        int segmentType;
        while (!pathIterator.isDone()) {
            segmentType = pathIterator.currentSegment(segment);
            switch (segmentType) {
                case PathIterator.SEG_MOVETO:
                    postScript.append(segment[0]).append(SPACE)
                            .append(segment[1]).append(SPACE)
                            .append(PdfOps.m_TOKEN).append(NEWLINE);
                    break;
                case PathIterator.SEG_LINETO:
                    postScript.append(segment[0]).append(SPACE)
                            .append(segment[1]).append(SPACE)
                            .append(PdfOps.l_TOKEN).append(NEWLINE);
                    break;
                case PathIterator.SEG_QUADTO:
                    postScript.append(segment[0]).append(SPACE)
                            .append(segment[1]).append(SPACE)
                            .append(segment[2]).append(SPACE)
                            .append(segment[3]).append(SPACE)
                            .append(PdfOps.v_TOKEN).append(NEWLINE);
                    break;
                case PathIterator.SEG_CUBICTO:
                    postScript.append(segment[0]).append(SPACE)
                            .append(segment[1]).append(SPACE)
                            .append(segment[2]).append(SPACE)
                            .append(segment[3]).append(SPACE)
                            .append(segment[4]).append(SPACE)
                            .append(segment[5]).append(SPACE)
                            .append(PdfOps.c_TOKEN).append(NEWLINE);
                    break;
                case PathIterator.SEG_CLOSE:
                    postScript.append(PdfOps.h_TOKEN).append(SPACE);
                    break;
            }
            pathIterator.next();
        }
    }
}
