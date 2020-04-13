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
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.Resources;
import org.icepdf.core.util.Library;

import java.awt.image.BufferedImage;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents one inline images as define in a content stream.  Inline images
 * don't have object numbers and thus can't be cached.
 *
 * @since 5.0
 */
public class InlineImageStreamReference extends ImageReference {

    private static final Logger logger =
            Logger.getLogger(InlineImageStreamReference.class.toString());

    public InlineImageStreamReference(ImageStream imageStream, GraphicsState graphicsState,
                                      Resources resources, int iamgeIndex,
                                      Page page) {
        super(imageStream, graphicsState, resources, iamgeIndex, page);

        // kick off a new thread to load the image, if not already in pool.
        ImagePool imagePool = imageStream.getLibrary().getImagePool();
        if (useProxy && imagePool.get(reference) == null) {
            futureTask = new FutureTask<BufferedImage>(this);
            Library.executeImage(futureTask);
        } else if (!useProxy && imagePool.get(reference) == null) {
            image = call();
        }
    }

    @Override
    public int getWidth() {
        return imageStream.getWidth();
    }

    @Override
    public int getHeight() {
        return imageStream.getHeight();
    }

    @Override
    public BufferedImage getImage() {
        if (image == null && useProxy) {
            image = createImage();
        } else {
            image = call();
        }
        return image;
    }

    public BufferedImage call() {
        BufferedImage image = null;
        long start = System.nanoTime();
        try {
            image = imageStream.getImage(graphicsState, resources);
        } catch (Throwable e) {
            logger.log(Level.WARNING, "Error loading image: " + imageStream.getPObjectReference() +
                    " " + imageStream.toString(), e);
        }
        long end = System.nanoTime();
        notifyImagePageEvents((end - start));
        return image;
    }
}
