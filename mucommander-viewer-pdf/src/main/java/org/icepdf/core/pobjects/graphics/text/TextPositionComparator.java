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
 * Text Position Comparator to sort AbstractText objects that might not be
 * plotted in a top down fasion.  The comparator only looks at vertical position
 * and does not sort text in the horizontal plain,  for example LTR or RTL text
 * layouts.
 *
 * @since 5.0.1
 */
public class TextPositionComparator implements
        Comparator<AbstractText> {

    public int compare(AbstractText lt1, AbstractText lt2) {
        float y1 = lt1.bounds.y;
        float y2 = lt2.bounds.y;
        float x1 = lt1.bounds.x;
        float x2 = lt2.bounds.x;
        return y1 != y2 ? Float.compare(y2, y1) : Float.compare(x1, x2);
    }
}
