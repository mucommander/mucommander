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

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;

/**
 * GlyphOutlineClip is contains the glyph outlines for glyph contained
 * within a TextBlock.  This utility method makes it possible to apply
 * text rendering modes that require glyph outlines for clipping such
 * as modes 4-7.
 *
 * @since 4.3.3
 */
public class GlyphOutlineClip {

    private GeneralPath path;

    public void addTextSprite(TextSprite nextSprite) {
        Area area = nextSprite.getGlyphOutline();
        // When TJ/Tj and Other text operators are called on a font using
        // modes 5-7 we don't actually craw anything but we still need to
        // transform the glyph to the correct coordinate, so each
        // outline is place correctly with in the total outline shape.
        Area tmp = area.createTransformedArea(nextSprite.getGraphicStateTransform());
        if (path == null) {
            path = new GeneralPath(tmp);
        } else {
            path.append(tmp, false);
        }
    }

    /**
     * Check to see if the glyph outline contains any outline data.
     *
     * @return true if the are no glyph outlines, otherwise; false.
     */
    public boolean isEmpty() {
        return path == null;
    }

    /**
     * Gets the glyph outline shape which can be used for painting or clipping.
     *
     * @return glyph outline shape.
     */
    public Shape getGlyphOutlineClip() {
        return path;
    }

}
