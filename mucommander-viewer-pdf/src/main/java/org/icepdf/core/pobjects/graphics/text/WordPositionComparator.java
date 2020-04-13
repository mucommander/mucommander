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
package org.icepdf.core.pobjects.graphics.text;

import java.util.Comparator;

/**
 * The WordPositionComparator is optionally called by text extraction algorithms
 * to help insure words found in a line are ordered using the x coordinates
 * of the bounding box in the cartesian plane's fourth quadrant.  The sorting
 * tries to order the word blocks via the coordinate system rather then the order
 * that they were plotted in and thus shouldn't effect LTR or RTL writing formats.
 * <p/>
 * It's assumed that all WordText that is a child of LineText will not be
 * sorted on the y access.  The class LinePositionComparator will be used
 * to insure that lines are ordered correctly in the parent PageText array.
 *
 * @since 5.0.6
 */
public class WordPositionComparator implements
        Comparator<AbstractText> {

    /**
     * Compares the x coordinates of the AbstractText bounding box's x coordinate.
     *
     * @param lt1 word text object to compare
     * @param lt2 word text object to compare
     * @return the value 0 if lt1.x is numerically equal to lt2.x; a value less
     *         than 0 if lt1.x is numerically less than lt2.x; and a value greater than 0
     *         if lt1.x is numerically greater than lt2.x.
     */
    public int compare(AbstractText lt1, AbstractText lt2) {
        return Float.compare(lt1.getTextExtractionBounds().x,
                lt2.getTextExtractionBounds().x);
    }
}
