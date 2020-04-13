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

import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.graphics.BlendComposite;
import org.icepdf.core.pobjects.graphics.OptionalContentState;
import org.icepdf.core.pobjects.graphics.PaintTimer;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Applies BlendingComposite draw operations.
 *
 * @since 6.0.2
 */
public class BlendCompositeDrawCmd extends AbstractDrawCmd {

    private Composite blendComposite;

    public BlendCompositeDrawCmd(Name blendComposite, float alpha) {
        // check for -1, value not set and default should be used.
        if (alpha == -1) {
            alpha = 1;
        }
        this.blendComposite = BlendComposite.getInstance(blendComposite, alpha);
    }

    @Override
    public Shape paintOperand(Graphics2D g, Page parentPage, Shape currentShape,
                              Shape clip, AffineTransform base, OptionalContentState optionalContentState,
                              boolean paintAlpha, PaintTimer paintTimer) {

        if (paintAlpha && blendComposite != null) {
            g.setComposite(blendComposite);
        }

        return currentShape;
    }

    /**
     * Gets the alpha value that is applied to the graphics context.
     *
     * @return alpha context which will be applied by this command.
     */
    public Composite getBlendComposite() {
        return blendComposite;
    }

}