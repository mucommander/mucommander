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

import org.icepdf.core.pobjects.Form;
import org.icepdf.core.pobjects.ImageUtility;
import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.graphics.*;
import org.icepdf.core.util.Defs;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;


/**
 * The FormDrawCmd when executed will draw an xForm's shapes to a raster and
 * then paint the raster.  This procedure is only executed if the xForm
 * is part of transparency group that has a alpha value < 1.0f.
 *
 * @since 5.0
 */
public class FormDrawCmd extends AbstractDrawCmd {

    private Form xForm;

    private BufferedImage xFormBuffer;
    private int x, y;

    private static boolean disableXObjectSMask;

    // Used to use Max_value but we have a few corner cases where the dimension is +-5 of Short.MAX_VALUE, but
    // realistically we seldom have enough memory to load anything bigger then 8000px.  4k+ image are big!
    public static int MAX_IMAGE_SIZE = 2000; // Short.MAX_VALUE

    static {
        // decide if large images will be scaled
        disableXObjectSMask =
                Defs.sysPropertyBoolean("org.icepdf.core.disableXObjectSMask",
                        false);

        MAX_IMAGE_SIZE = Defs.sysPropertyInt("org.icepdf.core.maxSmaskImageSize", MAX_IMAGE_SIZE);
    }

    public FormDrawCmd(Form xForm) {
        this.xForm = xForm;
    }

    @Override
    public Shape paintOperand(Graphics2D g, Page parentPage, Shape currentShape,
                              Shape clip, AffineTransform base,
                              OptionalContentState optionalContentState,
                              boolean paintAlpha, PaintTimer paintTimer) {
        if (optionalContentState.isVisible() && xFormBuffer == null) {
            RenderingHints renderingHints = g.getRenderingHints();
            Rectangle2D bBox = xForm.getBBox();
            x = (int) bBox.getX();
            y = (int) bBox.getY();
            boolean hasMask = ((xForm.getGraphicsState().getExtGState() != null &&
                    xForm.getGraphicsState().getExtGState().getSMask() != null) ||
                    (xForm.getExtGState() != null && xForm.getExtGState().getSMask() != null));
            boolean isExtendGraphicState = xForm.getGraphicsState().getExtGState() != null &&
                    xForm.getExtGState() != null;
            boolean normalBM = false;
            if (isExtendGraphicState && xForm.getExtGState().getBlendingMode() != null) {
                normalBM = xForm.getExtGState().getBlendingMode().equals(new Name("Normal")) &&
                        xForm.getGraphicsState().getExtGState().getBlendingMode().equals(new Name("Normal")) &&
                        (xForm.getExtGState() != null &&
                                (!xForm.getExtGState().isAlphaAShape() || xForm.getExtGState().getOverprintMode() == 0));
            }

            SoftMask formSoftMask = null;
            SoftMask softMask = null;

            if (xForm.getGraphicsState().getExtGState().getSMask() != null) {
                softMask = xForm.getGraphicsState().getExtGState().getSMask();
                boolean isShading = softMask.getG().getResources().isShading();
                if (isShading) {
                    isShading = checkForShaddingFill(softMask.getG());
                    softMask.getG().setShading(isShading);
                }
                if (!isShading) {
                    x = (int) softMask.getG().getBBox().getX();
                    y = (int) softMask.getG().getBBox().getY();
                }
            }
            if (xForm.getExtGState().getSMask() != null) {
                formSoftMask = xForm.getExtGState().getSMask();
                boolean isShading = formSoftMask.getG().getResources().isShading();
                if (isShading) {
                    isShading = checkForShaddingFill(formSoftMask.getG());
                    formSoftMask.getG().setShading(isShading);
                }
                if (!isShading) {
                    x = (int) formSoftMask.getG().getBBox().getX();
                    y = (int) formSoftMask.getG().getBBox().getY();
                }
            }
            // check if we have the same xobject.
            if (softMask != null && formSoftMask != null) {
                if (softMask.getPObjectReference() != null && formSoftMask.getPObjectReference() != null &&
                        softMask.getPObjectReference().equals(formSoftMask.getPObjectReference())) {
                    softMask = null;
                } else if (softMask.getG().getPObjectReference() != null &&
                        formSoftMask.getG().getPObjectReference() != null &&
                        softMask.getG().getPObjectReference().equals(formSoftMask.getG().getPObjectReference())) {
                    softMask = null;
                }
            }
            // need to check if we really have a shading pattern, as the resources check can be false positive.
            if (xForm.getResources().isShading()) {
                boolean isFormShading = checkForShaddingFill(xForm);
                xForm.setShading(isFormShading);
            }

            // create the form and we'll paint it at the very least
            xFormBuffer = createBufferXObject(parentPage, xForm, null, renderingHints, normalBM);
            if (!disableXObjectSMask && hasMask) {

                // apply the mask and paint.
                if (!xForm.isShading()) {
                    if (softMask != null && softMask.getS().equals(SoftMask.SOFT_MASK_TYPE_ALPHA)) {
                        logger.warning("Smask alpha example, currently not supported.");
                    } else if (softMask != null && softMask.getS().equals(SoftMask.SOFT_MASK_TYPE_LUMINOSITY)) {
                        xFormBuffer = applyMask(parentPage, xFormBuffer, softMask, formSoftMask, g.getRenderingHints());
                    }
                } else if (softMask != null) {
                    // still not property aligning the form or mask space to correctly apply a shading pattern.
                    // experimental as it fixes some, breaks others, but regardless we don't support it well.
                    logger.warning("Smask pattern paint example, currently not supported.");
                    xFormBuffer.flush();
                    xFormBuffer = createBufferXObject(parentPage, softMask.getG(), null, renderingHints, true);
                    return currentShape;
                }
                // apply the form mask to current form content that has been rasterized to xFormBuffer
                if (formSoftMask != null) {
                    BufferedImage formSMaskBuffer = applyMask(parentPage, xFormBuffer, formSoftMask, softMask,
                            g.getRenderingHints());
                    // compost all the images.
                    if (softMask != null) {
                        BufferedImage formBuffer = ImageUtility.createTranslucentCompatibleImage(
                                xFormBuffer.getWidth(), xFormBuffer.getHeight());
                        Graphics2D g2d = (Graphics2D) formBuffer.getGraphics();
//                        java.util.List<Number> compRaw = formSoftMask.getBC();
//                        if (compRaw != null) {
//                            g2d.setColor(Color.BLACK);
//                            g2d.fillRect(0, 0, xFormBuffer.getWidth(), xFormBuffer.getHeight());
//                        }
                        g2d.drawImage(formSMaskBuffer, 0, 0, null);
//                        g2d.drawImage(xFormBuffer, 0, 0, null);
                        xFormBuffer.flush();
                        xFormBuffer = formBuffer;
                    } else {
                        xFormBuffer = formSMaskBuffer;
                    }
                }
            } else if (isExtendGraphicState) {
                BufferedImage shape = createBufferXObject(parentPage, xForm, null, renderingHints, true);
                xFormBuffer = ImageUtility.applyExplicitOutline(xFormBuffer, shape);
            }
//            ImageUtility.displayImage(xFormBuffer, "final" + xForm.getGroup() + " " + xForm.getPObjectReference() +
//                    xFormBuffer.getHeight() + "x" + xFormBuffer.getHeight());
        }
        g.drawImage(xFormBuffer, null, x, y);
        return currentShape;
    }

    private BufferedImage applyMask(Page parentPage, BufferedImage xFormBuffer, SoftMask softMask, SoftMask gsSoftMask,
                                    RenderingHints renderingHints) {
        if (softMask != null && softMask.getS().equals(SoftMask.SOFT_MASK_TYPE_ALPHA)) {
            logger.warning("Smask alpha example, currently not supported.");
        } else if (softMask != null && softMask.getS().equals(SoftMask.SOFT_MASK_TYPE_LUMINOSITY)) {
            BufferedImage sMaskBuffer = createBufferXObject(parentPage, softMask.getG(), softMask, renderingHints, true);
//            ImageUtility.displayImage(xFormBuffer, "base " + xForm.getPObjectReference() + " " + xFormBuffer.getHeight() + " x " + xFormBuffer.getHeight());
//            ImageUtility.displayImage(sMaskBuffer, "smask " + softMask.getG().getPObjectReference() + " " + useLuminosity);
            if (!(gsSoftMask != null)) {
                xFormBuffer = ImageUtility.applyExplicitSMask(xFormBuffer, sMaskBuffer);
            } else {
                // todo try and figure out how to apply an AIS=false alpha to an xobject.
//                xFormBuffer = ImageUtility.applyExplicitLuminosity(xFormBuffer, sMaskBuffer);
                xFormBuffer = ImageUtility.applyExplicitOutline(xFormBuffer, sMaskBuffer);
            }
            // test for TR function
            if (softMask.getTR() != null) {
                logger.warning("Smask Transfer Function example, currently not supported.");
            }
            // todo need to look at matte too which is on the xobject.
        }
//        ImageUtility.displayImage(xFormBuffer, "final  " + softMask.getG().getPObjectReference());
        return xFormBuffer;
    }

    /**
     * Paint the form content to a BufferedImage so that the forms content can be
     * used to apply the sMask data.  Further work is needed to fully support this
     * section of transparency groups.
     *
     * @param parentPage     parent page object
     * @param xForm          form being drawn to buffer.
     * @param renderingHints graphic state rendering hinds of parent.
     * @return buffered image of xObject content.
     */
    private BufferedImage createBufferXObject(Page parentPage, Form xForm, SoftMask softMask,
                                              RenderingHints renderingHints, boolean isMask) {
        Rectangle2D bBox = xForm.getBBox();
        int width = (int) bBox.getWidth();
        int height = (int) bBox.getHeight();
        // corner cases where some bBoxes don't have a dimension.
        if (width == 0) {
            width = 1;
        } else if (width >= MAX_IMAGE_SIZE) {
            width = xFormBuffer.getWidth();
        }
        if (height == 0) {
            height = 1;
        } else if (height >= MAX_IMAGE_SIZE) {
            height = xFormBuffer.getHeight();
        }
        // create the new image to write too.
        BufferedImage bi = ImageUtility.createTranslucentCompatibleImage(width, height);
        Graphics2D canvas = bi.createGraphics();
        if (!isMask && xForm.getExtGState() != null && xForm.getExtGState().getBlendingMode() != null
                && !new Name("Normal").equals(xForm.getExtGState().getBlendingMode())
                ) {
            if (xForm.getGroup() != null) {
                HashMap tmp = xForm.getGroup();
                Object cs = xForm.getLibrary().getObject(tmp, new Name("CS"));
                // looking for additive colour spaces, if so we paint an background.
                if (cs == null || cs instanceof ICCBased || cs instanceof Name &&
                        (((Name) cs).equals(DeviceRGB.DEVICERGB_KEY)
                                || ((Name) cs).equals(DeviceCMYK.DEVICECMYK_KEY))) {
                    canvas.setColor(Color.WHITE);
                    canvas.fillRect(0, 0, width, height);
                }
            }
        }
        // copy over the rendering hints
        canvas.setRenderingHints(renderingHints);
        // get shapes and paint them.
        try {
            Shapes xFormShapes = xForm.getShapes();
            if (xFormShapes != null) {
                xFormShapes.setPageParent(parentPage);
                // translate the coordinate system as we'll paint the g
                // graphic at the correctly location later.
                if (!xForm.isShading()) {
                    canvas.translate(-(int) bBox.getX(), -(int) bBox.getY());
                    canvas.setClip(bBox);
                    xFormShapes.paint(canvas);
                    xFormShapes.setPageParent(null);
                }
                // basic support for gradient fills,  still have a few corners cases to work on.
                else {
                    for (DrawCmd cmd : xFormShapes.getShapes()) {
                        if (cmd instanceof ShapeDrawCmd && ((ShapeDrawCmd) cmd).getShape() == null) {
                            Rectangle2D bounds = bBox.getBounds2D();
                            ((ShapeDrawCmd) cmd).setShape(bounds);
                        }
                    }
                    canvas.translate(-x, -y);
                    canvas.setClip(bBox.getBounds2D());
                    xFormShapes.paint(canvas);
                    xFormShapes.setPageParent(null);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.fine("Form draw thread interrupted.");
        }
        canvas.dispose();
        return bi;
    }

    private boolean checkForShaddingFill(Form xform) {
        boolean found = false;
        for (DrawCmd cmd : xform.getShapes().getShapes()) {
            if (cmd instanceof ShapeDrawCmd && ((ShapeDrawCmd) cmd).getShape() == null) {
                found = true;
            }
        }
        return found;
    }
}
