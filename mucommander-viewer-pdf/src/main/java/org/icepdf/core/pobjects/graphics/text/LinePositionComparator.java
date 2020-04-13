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

import java.awt.*;
import java.util.Comparator;

/**
 * The LinePositionComparator is optionally called by text extraction algorithms
 * to help insure text lines found on a page are ordered using the y coordinates
 * of the bounding box in the cartesian plane's fourth quadrant.  The sorting
 * tries to order the line blocks via the coordinate system rather then the order
 * that they were plotted in.
 * <p/>
 * It's assumed that all LineText that is a child of PageText will not be
 * sorted on the x access.  The class WordPositionComparator will be used
 * to insure that words are ordered correctly in the parent PageText array.
 *
 * @since 5.0.6
 */
public class LinePositionComparator implements
        Comparator<AbstractText> {

    /**
     * Compares the y coordinates of the AbstractText bounding box's y coordinate.
     *
     * @param lt1 word text object to compare
     * @param lt2 word text object to compare
     * @return the value 0 if lt1.y is numerically equal to lt2.y; a value less
     *         than 0 if lt1.y is numerically less than lt2.y; and a value greater than 0
     *         if lt1.y is numerically greater than lt2.y.
     */
    public int compare(AbstractText lt1, AbstractText lt2) {

//        int comp = Float.compare(lt2.getBounds().y, lt1.getBounds().y);
//        if (comp == 0){
//            comp = Float.compare(lt1.getBounds().x, lt2.getBounds().x);
//        }
//        return comp;

        return Float.compare(lt2.getBounds().y, lt1.getBounds().y);

    }
}
