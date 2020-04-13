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
 * Convenience adaptor for working with the PageProcessingListener interface.
 *
 * @since 5.1.0
 */
public abstract class PageLoadingAdapter implements PageLoadingListener {

    public void pageLoadingStarted(PageLoadingEvent event) {
    }

    public void pageInitializationStarted(PageInitializingEvent event) {
    }

    public void pageInitializationEnded(PageInitializingEvent event) {
    }

    public void pageImageLoaded(PageImageEvent event) {
    }

    public void pagePaintingStarted(PagePaintingEvent event) {
    }

    public void pagePaintingEnded(PagePaintingEvent event) {
    }

    public void pageLoadingEnded(PageLoadingEvent event) {
    }
}
