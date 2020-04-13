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
import org.icepdf.core.util.Library;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

/**
 * The ScaledImageReference stores the original image data  as well as several
 * smaller images instances which are painted at lower zoom values to improve
 * paint speeds.
 *
 * @since 5.0
 */
public class ScaledImageReference extends CachedImageReference {

    private static final Logger logger =
            Logger.getLogger(ScaledImageReference.class.toString());

    // scaled image size.
    private int width;
    private int height;

    protected ScaledImageReference(ImageStream imageStream, GraphicsState graphicsState,
                                   Resources resources, int imageIndex, Page page) {
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

    public ScaledImageReference(ImageReference imageReference, GraphicsState graphicsState, Resources resources,
                                int width, int height, int imageIndex, Page page) {
        super(imageReference.getImageStream(), graphicsState, resources, imageIndex, page);

        this.width = width;
        this.height = height;

        // check for an repeated scale via a call from MipMap
        if (imageReference.isImage()) {
            image = imageReference.getImage();
        }

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
            image = imageStream.getImage(graphicsState, resources);

            if (image != null) {
                // get eh original image width.
                int width = imageStream.getWidth();
                int height = imageStream.getHeight();

                // apply scaling factor
                double scaleFactor = 1.0;
                if (width > 1000 && width < 1500) {
                    scaleFactor = 0.75;
                } else if (width > 1500) {
                    scaleFactor = 0.5;
                }
                // update image size for any scaling.
                if (scaleFactor < 1.0) {
                    width = (int) Math.ceil(width * scaleFactor);
                    height = (int) Math.ceil(height * scaleFactor);

                    BufferedImage scaled;
                    if (ImageUtility.hasAlpha(image)) {
                        scaled = ImageUtility.createTranslucentCompatibleImage(width, height);
                    } else {
                        scaled = ImageUtility.createCompatibleImage(width, height);
                    }
                    Graphics2D g = scaled.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g.drawImage(image, 0, 0, width, height, null);
                    g.dispose();
                    image.flush();
                    image = scaled;
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
}
