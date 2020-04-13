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
import org.icepdf.core.pobjects.graphics.OptionalContentState;
import org.icepdf.core.pobjects.graphics.PaintTimer;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Applies a clipping command to the Graphics2D context which makes the clip
 * match the currentShape or at least the intersection of the currentShape
 * with the previous clip.
 *
 * @since 5.0
 */
public class ClipDrawCmd extends AbstractDrawCmd {

//    private int rule = AlphaComposite.SRC_OVER;
//    private float alpha = .5f;

    @Override
    public Shape paintOperand(Graphics2D g, Page parentPage, Shape currentShape,
                              Shape clip, AffineTransform base,
                              OptionalContentState optionalContentState,
                              boolean paintAlpha, PaintTimer paintTimer) {

        // Capture the current af for the
        //  page
        AffineTransform af = new AffineTransform(g.getTransform());
        // Set the transform to the base, which is fact where the page
        // lies in the viewport, very dynamic.
        g.setTransform(base);
//        if (!g.getClip().getBounds().equals(clip.getBounds())) {// apply the clip, which is always the initial paper size,
            g.setClip(clip);
//        }
        // apply the af, which places the clip in the correct location
        g.setTransform(af);
        if (currentShape != null && !disableClipping) {
            // clip outline
//            Color tmp = g.getColor();
//            g.setColor(Color.red);
//            g.draw(currentShape);
//            g.setColor(tmp);
//            g.setComposite(AlphaComposite.getInstance(rule, alpha));
            // apply the new clip
//            if (!g.getClip().getBounds().equals(currentShape.getBounds())) {
                g.clip(currentShape);
//            }
        }
        return currentShape;
    }
}
