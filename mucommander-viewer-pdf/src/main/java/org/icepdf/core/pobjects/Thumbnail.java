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

package org.icepdf.core.pobjects;

import org.icepdf.core.util.Library;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * A PDF document may contain thumbnail images representing the contents of its
 * pages in miniature form. A conforming reader may display these images on the
 * screen, allowing the user to navigate to a page by clicking its thumbnail
 * image:
 * <p/>
 * <b>NOTE</b>Thumbnail images are not required, and may be included for some
 * pages and not for others.
 */
public class Thumbnail extends Dictionary {

    public static final Name THUMB_KEY = new Name("Thumb");
    public static final Name WIDTH_KEY = new Name("Width");
    public static final Name HEIGHT_KEY = new Name("Height");

    private ImageStream thumbStream;
    private boolean initialized;

    // thumb image
    private BufferedImage image;

    // dimensions
    private Dimension dimension;

    public Thumbnail(Library library, HashMap entries) {
        super(library, entries);
        Object thumb = library.getObject(entries, THUMB_KEY);
        if (thumb != null) {
            if (thumb instanceof ImageStream) {
                // get the thumb image.
                thumbStream = (ImageStream) thumb;
            } else {
                thumbStream = new ImageStream(library, ((Stream) thumb).getEntries(),
                        ((Stream) thumb).getRawBytes());
            }
            // grab its bounds.
            int width = library.getInt(thumbStream.entries, WIDTH_KEY);
            int height = library.getInt(thumbStream.entries, HEIGHT_KEY);
            dimension = new Dimension(width, height);
            // the image is lazy loaded on the getImage() call.

        }
    }

    public void init() throws InterruptedException {
        Resources resource = new Resources(library, thumbStream.entries);
        image = thumbStream.getImage(null, resource);
        initialized = true;
    }

    public BufferedImage getImage() throws InterruptedException {
        if (!initialized) {
            init();
        }
        return image;
    }

    public Dimension getDimension() {
        return dimension;
    }
}
