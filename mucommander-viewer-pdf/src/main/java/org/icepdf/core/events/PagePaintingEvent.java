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
 * PagePaintingEvent are fired when page painting starts and ends.  The start
 * event will indicate the number of shapes that will be painted.
 *
 * @since 5.1.0
 */
@SuppressWarnings("serial")
public class PagePaintingEvent extends PageInitializingEvent {

    private int shapesCount;

    public PagePaintingEvent(Page pageSource, int shapesCount) {
        super(pageSource, false);
        this.shapesCount = shapesCount;
    }

    public PagePaintingEvent(Page pageSource, boolean interrupted) {
        super(pageSource, interrupted);
    }

    public int getShapesCount() {
        return shapesCount;
    }
}
