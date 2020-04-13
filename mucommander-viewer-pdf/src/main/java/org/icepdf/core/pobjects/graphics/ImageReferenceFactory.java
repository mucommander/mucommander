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
import org.icepdf.core.util.Defs;

/**
 * The ImageReferenceFactory determines which implementation of the
 * Image Reference should be created.  The ImageReference type can be specified
 * by the following system properties or alternatively by the enum type.
 * <ul>
 * <li>org.icepdf.core.imageReference = default</li>
 * <li>org.icepdf.core.imageReference = scaled</li>
 * <li>org.icepdf.core.imageReference = mipmap</li>
 * <li>org.icepdf.core.imageReference = smoothScaled</li>
 * </ul>
 * The default value returns an unaltered image,  scaled returns a scaled
 * image instance and there MIP mapped returns/picks a scaled image that
 * best fits the current zoom level for a balance of render speed and quality.
 *
 * @see MipMappedImageReference
 * @see ImageStreamReference
 * @see ScaledImageReference
 * @since 5.0
 */
public class ImageReferenceFactory {

    // allow scaling of large images to improve clarity on screen

    public enum ImageReference {
        DEFAULT, SCALED, MIP_MAP, SMOOTH_SCALED // FLOYD_STEINBERG
    }

    private static ImageReference scaleType;

    static {
        // decide if large images will be scaled
        String imageReferencetype =
                Defs.sysProperty("org.icepdf.core.imageReference",
                        "default");
        if ("scaled".equals(imageReferencetype)) {
            scaleType = ImageReference.SCALED;
        } else if ("mipmap".equals(imageReferencetype)) {
            scaleType = ImageReference.MIP_MAP;
        } else if ("smoothScaled".equals(imageReferencetype)) {
            scaleType = ImageReference.SMOOTH_SCALED;
        } else {
            scaleType = ImageReference.DEFAULT;
        }
    }

    private ImageReferenceFactory() {
    }

    public static ImageReference getScaleType() {
        return scaleType;
    }

    public static void setScaleType(ImageReference scaleType) {
        ImageReferenceFactory.scaleType = scaleType;
    }

    /**
     * Gets an instance of an ImageReference object for the given image data.
     * The ImageReference is specified by the system property org.icepdf.core.imageReference
     * or by the static instance variable scale type.
     *
     * @param imageStream   image data
     * @param resources     parent resource object.
     * @param graphicsState image graphic state.
     * @return newly create ImageReference.
     */
    public static org.icepdf.core.pobjects.graphics.ImageReference
    getImageReference(ImageStream imageStream, Resources resources, GraphicsState graphicsState,
                      Integer imageIndex, Page page) {
        switch (scaleType) {
            case SCALED:
                return new ScaledImageReference(imageStream, graphicsState, resources, imageIndex, page);
            case SMOOTH_SCALED:
                return new SmoothScaledImageReference(imageStream, graphicsState, resources, imageIndex, page);
            case MIP_MAP:
                return new MipMappedImageReference(imageStream, graphicsState, resources, imageIndex, page);
            default:
                return new ImageStreamReference(imageStream, graphicsState, resources, imageIndex, page);
        }
    }

}
