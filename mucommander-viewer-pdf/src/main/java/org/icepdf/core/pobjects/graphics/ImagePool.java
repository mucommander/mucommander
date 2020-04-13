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

import org.icepdf.core.pobjects.Reference;
import org.icepdf.core.util.Defs;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;

/**
 * The Image pool is a Map of the most recently used images.  The pools size
 * by default is setup to be 1/4 the heap size.  So as the pool grows it
 * will self trim to keep the memory foot print at the specified max.  The
 * max pool size can be specified by using the org.icepdf.core.views.imagePoolSize
 * system property.  The value is specified in MB.
 * <p/>
 * The pool also contains an executor pool for processing Images.  The executor
 * allows the pageInitialization thread to continue while the executor processes
 * the image data on another thread.
 * <p/>
 * Teh pool size can be set with the system property  org.icepdf.core.views.imagePoolSize
 * where the default value is 1/4 the heap size.  The pool set can be specified in
 * using a int value representing the desired size in MB.
 * <p/>
 * The pool can also be disabled using the boolean system property
 * org.icepdf.core.views.imagePoolEnabled=false.  The default state is for the
 * ImagePool to be enabled.
 *
 * @since 5.0
 */
@SuppressWarnings("serial")
public class ImagePool {
    private static final Logger log =
            Logger.getLogger(ImagePool.class.toString());

    // Image pool
    private final Map<Reference, BufferedImage> fCache;


    private static boolean enabled;
    static {
        // enable/disable the image pool all together.
        enabled = Defs.booleanProperty("org.icepdf.core.views.imagePoolEnabled", true);
    }


    public ImagePool() {
        fCache = Collections.synchronizedMap(new WeakHashMap<Reference, BufferedImage>(50));
    }

    public void put(Reference ref, BufferedImage image) {
        // create a new reference so we don't have a hard link to the page
        // which will likely keep a page from being GC'd.
        if (enabled) {
//            synchronized (fCache) {
                fCache.put(new Reference(ref.getObjectNumber(), ref.getGenerationNumber()), image);
//            }
        }
    }

    public BufferedImage get(Reference ref) {
        if (enabled) {
//            synchronized (fCache) {
                return fCache.get(ref);
//            }
        } else {
            return null;
        }
    }

    public boolean containsKey(Reference ref) {
        if (enabled) {
//            synchronized (fCache) {
                return fCache.containsKey(ref);
//            }
        } else {
            return false;
        }
    }
}
