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

import org.icepdf.core.pobjects.ImageStream;
import org.icepdf.core.pobjects.ImageUtility;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.Resources;
import org.icepdf.core.util.Defs;
import org.icepdf.core.util.Library;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

/**
 * The SmoothScaledImageReference scales large images using the
 * bufferedImage.getScaledInstance() method for colour imges and the
 * a custom trilinear scale for black and white images.  The scaled instance
 * uses a minimum of memory and can improve clarity of some CCITTFax images.
 *
 * @since 5.0
 */
public class SmoothScaledImageReference extends CachedImageReference {

    private static final Logger logger =
            Logger.getLogger(ScaledImageReference.class.toString());

    private static int maxImageWidth = 7000;
    private static int maxImageHeight = 7000;

    static {
        try {
            maxImageWidth =
                    Integer.parseInt(Defs.sysProperty("org.icepdf.core.imageReference.smoothscaled.maxwidth",
                            String.valueOf(maxImageWidth)));

            maxImageHeight =
                    Integer.parseInt(Defs.sysProperty("org.icepdf.core.imageReference.smoothscaled.maxheight",
                            String.valueOf(maxImageHeight)));
        } catch (NumberFormatException e) {
            logger.warning("Error reading buffered scale factor");
        }
    }

    // scaled image size.
    private int width;
    private int height;

    protected SmoothScaledImageReference(ImageStream imageStream, GraphicsState graphicsState,
                                         Resources resources, int imageIndex,
                                         Page page) {
        super(imageStream, graphicsState, resources, imageIndex, page);

        // get eh original image width.
        width = imageStream.getWidth();
        height = imageStream.getHeight();

        // kick off a new thread to load the image, if not already in pool.
        ImagePool imagePool = imageStream.getLibrary().getImagePool();
        if (useProxy && imagePool.get(reference) == null) {
            futureTask = new FutureTask<BufferedImage>(this);
            Library.executeImage(futureTask);
        } else if (!useProxy && imagePool.get(reference) == null) {
            image = call();
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BufferedImage call() {
        BufferedImage image = null;
        long start = System.nanoTime();
        try {
            // get the stream image if need, otherwise scale what you have.
            if (image == null) {
                image = imageStream.getImage(graphicsState, resources);
                if (width > maxImageWidth || height > maxImageHeight) {
                    return image;
                }
            }
            if (image != null) {
                // update the width height encase it as scaled during masking.
                int width = image.getWidth();
                int height = image.getHeight();

                // do image scaling on larger images.  This improves the softness
                // of some images that contains black and white text.
                double imageScale = 1.0;

                // for device gray colour spaces use the trilinear scaling method
                // to basically blur the image so it more easily read and less jagged.
                if (imageStream.getColourSpace() != null &&
                        imageStream.getColourSpace() instanceof DeviceGray) {
                    // catch type 3 fonts.
                    if ((width < 50 || height < 50)) {
                        imageScale = 0.90;
                    }
                    // smooth out everything else.
                    else {
                        imageScale = 0.99;
                    }
                    if (imageScale != 1.0) {
                        image = (BufferedImage) getTrilinearScaledInstance(image,
                                (int) Math.ceil(width * imageScale),
                                (int) Math.ceil(height * imageScale));
                    }
                }
                // normal rgb scale as before, as the trilinear scale causes excessive blurring.
                else {
                    if ((width >= 250 || height >= 250) && (width < 500 || height < 500)) {
                        imageScale = 0.90;
                    } else if ((width >= 500 || height >= 500) && (width < 1000 || height < 1000)) {
                        imageScale = 0.80;
                    } else if ((width >= 1000 || height >= 1000) && (width < 1500 || height < 1500)) {
                        imageScale = 0.70;
                    } else if ((width >= 1500 || height >= 1500) && (width < 2000 || height < 2000)) {
                        imageScale = 0.60;
                    } else if ((width >= 2000 || height >= 2000) && (width < 2500 || height < 2500)) {
                        imageScale = 0.50;
                    } else if ((width >= 2500 || height >= 2500) && (width < 3000 || height < 3000)) {
                        imageScale = 0.40;
                    } else if ((width >= 3000 || height >= 3000)) {
                        imageScale = 0.30;
                    }
                    if (imageScale != 1.0) {
                        AffineTransform tx = new AffineTransform();
                        tx.scale(imageScale, imageScale);
                        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BICUBIC);
                        BufferedImage sbim = op.filter(image, null);
                        image.flush();
                        image = sbim;
                    }
                }
            }
        } catch (Throwable e) {
            logger.warning("Error loading image: " + imageStream.getPObjectReference() +
                    " " + imageStream.toString());
        }
        long end = System.nanoTime();
        notifyImagePageEvents((end - start));
        return image;
    }

    /**
     * Applies an iterative scaling method to provide a smooth end result, once complete
     * apply a trilinear blend based on the desired width and height.   Technique
     * derived from Jim Graham example code.
     *
     * @param img          image to scale
     * @param targetWidth  target width
     * @param targetHeight target height
     * @return scaled instance.
     */
    private static Image getTrilinearScaledInstance(BufferedImage img,
                                                    int targetWidth,
                                                    int targetHeight) {
        // Use multi-step technique: start with original size, then
        // scale down in multiple passes with drawImage()
        // until the target size is reached
        int iw = img.getWidth();
        int ih = img.getHeight();

        Object hint = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
                BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

        // First get down to no more than 2x in W & H
        while (iw > targetWidth * 2 || ih > targetHeight * 2) {
            iw = (iw > targetWidth * 2) ? iw / 2 : iw;
            ih = (ih > targetHeight * 2) ? ih / 2 : ih;
            img = scaleImage(img, type, hint, iw, ih);
        }

        // If still too wide - do a horizontal trilinear blend
        // of img and a half-width img
        if (iw > targetWidth) {
            int iw2 = iw / 2;
            BufferedImage img2 = scaleImage(img, type, hint, iw2, ih);
            if (iw2 < targetWidth) {
                img = scaleImage(img, type, hint, targetWidth, ih);
                img2 = scaleImage(img2, type, hint, targetWidth, ih);
                interpolate(img2, img, iw - targetWidth, targetWidth - iw2);
            }
            img = img2;
            iw = targetWidth;
        }
        // iw should now be targetWidth or smaller

        // If still too tall - do a vertical trilinear blend
        // of img and a half-height img
        if (ih > targetHeight) {
            int ih2 = ih / 2;
            BufferedImage img2 = scaleImage(img, type, hint, iw, ih2);
            if (ih2 < targetHeight) {
                img = scaleImage(img, type, hint, iw, targetHeight);
                img2 = scaleImage(img2, type, hint, iw, targetHeight);
                interpolate(img2, img, ih - targetHeight, targetHeight - ih2);
            }
            img = img2;
            ih = targetHeight;
        }
        // ih should now be targetHeight or smaller

        // If we are too small, then it was probably because one of
        // the dimensions was too small from the start.
        if (iw < targetWidth && ih < targetHeight) {
            img = scaleImage(img, type, hint, targetWidth, targetHeight);
        }

        return img;
    }

    /**
     * Utility to interpolate the two imges.
     */
    private static void interpolate(BufferedImage img1,
                                    BufferedImage img2,
                                    int weight1,
                                    int weight2) {
        float alpha = weight1;
        alpha /= (weight1 + weight2);
        Graphics2D g2 = img1.createGraphics();
        g2.setComposite(
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.drawImage(img2, 0, 0, null);
        g2.dispose();
    }

    /**
     * Utility to apply image scaling using the g2.drawImage() method.
     */
    private static BufferedImage scaleImage(BufferedImage orig,
                                            int type,
                                            Object hint,
                                            int w, int h) {
        BufferedImage tmp = ImageUtility.createTranslucentCompatibleImage(w, h);
        Graphics2D g2 = tmp.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
        g2.drawImage(orig, 0, 0, w, h, null);
        g2.dispose();
        return tmp;
    }

}
