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
package org.icepdf.core.pobjects.graphics.commands;

import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.graphics.GlyphOutlineClip;
import org.icepdf.core.pobjects.graphics.OptionalContentState;
import org.icepdf.core.pobjects.graphics.PaintTimer;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * The GlyphOutlineDrawCmd applies the clip defined by a locally stored
 * glyphOutlineClip object.
 *
 * @since 5.0
 */
public class GlyphOutlineDrawCmd extends AbstractDrawCmd {

    private GlyphOutlineClip glyphOutlineClip;

    public GlyphOutlineDrawCmd(GlyphOutlineClip glyphOutlineClip) {
        this.glyphOutlineClip = glyphOutlineClip;
    }

    @Override
    public Shape paintOperand(Graphics2D g, Page parentPage, Shape currentShape, Shape clip,
                              AffineTransform base, OptionalContentState optionalContentState,
                              boolean paintAlpha, PaintTimer paintTimer) {
        if (optionalContentState.isVisible()) {
            // save and revert the af for the page so that we can
            // paint the converted clip glyph outline.
            AffineTransform preTrans = new AffineTransform(g.getTransform());
            g.setTransform(base);
            // set clip directly but it should be the intersection with the current.
            Shape glyphClip = glyphOutlineClip.getGlyphOutlineClip();
            g.setClip(glyphClip);
            g.setTransform(preTrans);
        }
        return currentShape;
    }
}
