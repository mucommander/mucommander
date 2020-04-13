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

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * The MipMappedImageReference will create several scaled instance of the
 * specified image.  The images are all smaller then the original image and
 * are painted as the page zoom level is lowered. The main idea here is that small
 * images are painted at smaller zoom levels which in theory speeds up the image
 * paint time.
 *
 * @since 5.0
 */
class MipMappedImageReference extends ImageReference {

    private ArrayList<ImageReference> images;

    protected MipMappedImageReference(ImageStream imageStream, GraphicsState graphicsState,
                                      Resources resources, int imageIndex,
                                      Page page) {

        super(imageStream, graphicsState, resources, imageIndex, page);

        images = new ArrayList<ImageReference>();

        ImageReference imageReference =
                new ImageStreamReference(imageStream, graphicsState, resources, imageIndex, page);
        images.add(imageReference);

        int width = imageReference.getWidth();
        int height = imageReference.getHeight();
        // disable proxy as we need to scale each image from the previous
        // and thus need to do the downscale in one shot.
        useProxy = false;
        while (width > 20 && height > 20) {
            width /= 2;
            height /= 2;
            imageReference = new ScaledImageReference(imageReference, graphicsState, resources,
                    width, height, imageIndex, page);
            images.add(imageReference);
        }
    }

    public int getWidth() {
        return images.get(0).getWidth();
    }

    public int getHeight() {
        return images.get(0).getHeight();
    }

    public BufferedImage getImage() {
        return images.get(0).getImage();
    }

    public void drawImage(Graphics2D aG, int aX, int aY, int aW, int aH) {
        ImageReference imageReference = chooseImage(aG, aX, aY, aW, aH);
        imageReference.drawImage(aG, aX, aY, aW, aH);
    }

    private ImageReference chooseImage(Graphics2D aG, int aX, int aY, int aW, int aH) {
        Point2D.Double in = new Point2D.Double(aX, aY);
        Point2D.Double p1 = new Point2D.Double();
        Point2D.Double p2 = new Point2D.Double();
        aG.getTransform().transform(in, p1);
        in.x = aW;
        aG.getTransform().transform(in, p2);
        int distSq1 = (int) Math.round(p1.distanceSq(p2));
        in.x = aX;
        in.y = aH;
        aG.getTransform().transform(in, p2);
        int distSq2 = (int) Math.round(p1.distanceSq(p2));
        int maxDistSq = Math.max(distSq1, distSq2);

        int level = 0;
        ImageReference image = images.get(level);
        int width = image.getWidth();
        int height = image.getHeight();

        while (level < (images.size() - 1) &&
                (width * width / 4) > maxDistSq &&
                (height * height / 4) > maxDistSq) {
            image = images.get(level++);
            width = image.getWidth();
            height = image.getHeight();
        }
        return image;
    }

    // no need to implement as this class class calls ScaledImage and MipMapped
    // as needed.
    public BufferedImage call() {
        return null;
    }
}
