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

import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.graphics.ImageReference;
import org.icepdf.core.pobjects.graphics.OptionalContentState;
import org.icepdf.core.pobjects.graphics.PaintTimer;
import org.icepdf.core.util.Defs;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * The ImageDrawCmd class when executed will draw the image associated
 * with this DrawCmd.
 *
 * @since 5.0
 */
public class ImageDrawCmd extends AbstractDrawCmd {

    // enable disable scaled paint.
    private static boolean isScaledPaint;

    // narrow image scaling max dimension size to kick of the use of the lookup
    // table
    public static int MIN_DIMENSION;

    static {
        isScaledPaint = Defs.booleanProperty("org.icepdf.core.imageDrawCmd.scale.enabled", false);
        MIN_DIMENSION = Defs.intProperty("org.icepdf.core.imageDrawCmd.maxDimension", 5);
    }

    private ImageReference image;
    // paint scale factor of original image.
    private int xScale = 1;
    private int yScale = 1;
    private boolean xIsScale = false;
    private boolean yIsScale = false;

    // narrow image scaling lookup table for 1xh or wx1 images.  Soft values
    // but keeps the images from not painting a low zoom levels.
    // first column is the zoom level and the second is the total number of
    // pixels that must be present for the image to be painted.
    private static final double[][] SCALE_LOOKUP = {
            {1.50, 2},
            {0.70, 3},
            {0.40, 4},
            {0.30, 6},
            {0.20, 8},
            {0.10, 10},
            {0.05, 12}
    };

    public ImageDrawCmd(ImageReference image) {
        this.image = image;
        // check image dimensions to see if we should do some work for
        // Xxh or wxX images sizes, as they tend not to be painted by Java2d
        // at zoom levels < 144%.
        if (isScaledPaint) {
            if (image.getHeight() <= MIN_DIMENSION) {
                yIsScale = true;
            }
            if (image.getWidth() <= MIN_DIMENSION) {
                xIsScale = true;
            }
        }
    }

    public Image getImage() {
        return image.getImage();
    }

    @Override
    public Shape paintOperand(Graphics2D g, Page parentPage, Shape currentShape,
                              Shape clip, AffineTransform base,
                              OptionalContentState optionalContentState,
                              boolean paintAlpha, PaintTimer paintTimer) {
        if (optionalContentState.isVisible()) {
            if (isScaledPaint && (xIsScale || yIsScale)) {
                calculateThinScale(base.getScaleX());
            }
            image.drawImage(g, 0, 0, xScale, yScale);
            if (parentPage != null && paintTimer.shouldTriggerRepaint()) {
                parentPage.notifyPaintPageListeners();
            }
        }
        return currentShape;
    }

    /**
     * Alter the width or height value of 1px or less then MIN_DIMENSION.
     *
     * @param scale scale factor of current view.
     */
    private void calculateThinScale(double scale) {
        if (xIsScale) {
            xScale = commonScaling(scale, image.getWidth());
        }
        // horizon scale needs to be applied for an Wx1px image.
        if (yIsScale) {
            yScale = commonScaling(scale, image.getHeight());
        }
    }

    /**
     * Fetches a scale value from lookup table and returns the appropriate
     * scale so the image will be visible.
     *
     * @param scale page level scale being applied to page.
     * @param size  original size, width or height of the image to sale.
     * @return scale value applied to g.drawImage().
     */
    private int commonScaling(double scale, int size) {
        // find the appropriate range and final minimal
        for (int i = SCALE_LOOKUP.length - 1; i >= 0; i--) {
            if (scale < SCALE_LOOKUP[i][0]) {
                double neededSize = SCALE_LOOKUP[i][1];
                double scaleFactor = neededSize / size;
                return (int) Math.ceil(scaleFactor);
            }
        }
        return 1;
    }

}
