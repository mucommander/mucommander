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
package org.icepdf.core.events;

import org.icepdf.core.pobjects.Page;

/**
 * PageImageEvent is fired when an image is done loading.  The event stores
 * the image number that was loaded and the total number of images. The total
 * image is as specified by the parent page's resource dictionary and may not
 * 100% reliable. It should also be noted that the total count also excludes
 * inline images as the total inline images is only know after parsing is
 * complete.
 *
 * @since 5.1.0
 */
@SuppressWarnings("serial")
public class PageImageEvent extends PageInitializingEvent {

    private int index;
    private int total;
    private long duration;

    /**
     * Construct a new PageImageEvent
     *
     * @param pageSource  parent page.
     * @param index       index of image in the resource dictionary
     * @param total       total number of images to loaded.
     * @param interrupted true if the image loading was interrupted in anyway.
     */
    public PageImageEvent(Page pageSource, int index, int total, long duration, boolean interrupted) {
        super(pageSource, interrupted);
        this.index = index;
        this.total = total;
        this.duration = duration;
    }

    /**
     * The image index with respect to the total number of images.
     *
     * @return image index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * The total number of images being loaded for the parent page.
     *
     * @return total number of images to load.
     */
    public int getTotal() {
        return total;
    }

    public long getDuration() {
        return duration;
    }
}
