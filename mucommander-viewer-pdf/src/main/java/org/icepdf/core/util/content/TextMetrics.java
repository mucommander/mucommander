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
package org.icepdf.core.util.content;

import java.awt.geom.Point2D;

/**
 * The TextMetrics class purpose is to help sort out the difference between
 * how text is drawn vs. how it can be retrieved for text extraction such as
 * mouse selection/clip board and output to file.
 */
public class TextMetrics {

    private float shift = 0;
    // keeps track of previous text placement so that Compatibility and
    // implementation note 57 is respected.  That is text drawn after a TJ
    // must not be less then the previous glyphs coords.
    private float previousAdvance = 0;
    private Point2D.Float advance = new Point2D.Float(0, 0);

    // previous Td, TD or Tm y coordinate value for text extraction
    private boolean isYstart = true;
    private float yBTStart = 0;


    public float getShift() {
        return shift;
    }

    public void setShift(float shift) {
        this.shift = shift;
    }

    public float getPreviousAdvance() {
        return previousAdvance;
    }

    public void setPreviousAdvance(float previousAdvance) {
        this.previousAdvance = previousAdvance;
    }

    public Point2D.Float getAdvance() {
        return advance;
    }

    public void setAdvance(Point2D.Float advance) {
        this.advance = advance;
    }

    public boolean isYstart() {
        return isYstart;
    }

    public void setYstart(boolean ystart) {
        isYstart = ystart;
    }

    public float getyBTStart() {
        return yBTStart;
    }

    public void setyBTStart(float yBTStart) {
        this.yBTStart = yBTStart;
    }
}
