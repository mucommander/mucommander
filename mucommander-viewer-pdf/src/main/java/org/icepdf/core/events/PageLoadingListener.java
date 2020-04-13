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

/**
 * The PageProcessingListener can be used to listen for a variety of Page
 * level vents pertaining to the loading of the PDF page.  The hierarchy
 * and ordering of the events is as follows.  PageImageEvent can happen at any
 * point between the PageLoadingEvent.
 * <p/>
 * <ul>
 * <li>pageLoadingStarted(v event);</li>
 * <ul>
 * <li>pageInitializationStarted(PageProcessingEvent event);</li>
 * <ul>
 * <li>pageImageLoaded(PageImageEvent event)</li>
 * </ul>
 * <li>pageInitializationEnded(PageProcessingEvent event)</li>
 * <li>pagePaintingStarted(PagePaintingEvent event)</li>
 * <ul>
 * <li>pageImageLoaded(PageImageEvent event)</li>
 * </ul>
 * <li>pagePaintingEnded(PagePaintingEvent event)</li>
 * </ul>
 * <li>pageLoadingEnded(PageLoadingEvent event)</li>
 * </ul>
 *
 * @since 5.1.0
 */
public interface PageLoadingListener {

    /**
     * Page loading has started.  PageLoadingEvent can be used to get the total
     * number content streams and image streams that will be parsed.
     *
     * @param event PageLoadingEvent is fired populated with data.
     */
    public void pageLoadingStarted(PageLoadingEvent event);

    /**
     * Page initialization has started which is the parsing of the Page's
     * content stream.
     *
     * @param event PageProcessingEvent with data set to false.
     */
    public void pageInitializationStarted(PageInitializingEvent event);

    /**
     * Page initialization has be ended via successful parse or an interrupt
     * exception.  The PageProcessingEvent can be used to determine if the
     * page was successfully parsed.
     *
     * @param event PageProcessingEvent can be used to see if page was
     *              successfully parsed or was interrupted.
     */
    public void pageInitializationEnded(PageInitializingEvent event);

    /**
     * A pages images has been parsed. The PageImageEvent can be used to find out
     * if the parse was successful, image index and total number of images associated
     * with the page.
     *
     * @param event PageImageEvent
     */
    public void pageImageLoaded(PageImageEvent event);

    /**
     * Page painting has begun. The PagePaintingEvent can be used to find out
     * the number of shapes being painted.
     *
     * @param event PagePaintingEvent
     */
    public void pagePaintingStarted(PagePaintingEvent event);

    /**
     * Page painting has stopped.  Teh PagePaintingEvent can be used to find out
     * if the paint was completed or if there was an interrupt.
     *
     * @param event PagePaintingEvent
     */
    public void pagePaintingEnded(PagePaintingEvent event);

    /**
     * Page loading has completed and all images have been loaded, no further
     * page processing will take place.
     *
     * @param event PageLoadingEvent
     */
    public void pageLoadingEnded(PageLoadingEvent event);
}
