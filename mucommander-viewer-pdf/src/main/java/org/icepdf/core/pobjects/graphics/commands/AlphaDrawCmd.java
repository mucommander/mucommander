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
 * Stores an AlphaComposite value for differed painting to a Graphics2D object.
 *
 * @since 5.0
 */
public class AlphaDrawCmd extends AbstractDrawCmd {

    private AlphaComposite alphaComposite;

    public AlphaDrawCmd(AlphaComposite alphaComposite) {
        this.alphaComposite = alphaComposite;
    }

    @Override
    public Shape paintOperand(Graphics2D g, Page parentPage, Shape currentShape,
                              Shape clip, AffineTransform base, OptionalContentState optionalContentState,
                              boolean paintAlpha, PaintTimer paintTimer) {

        if (paintAlpha) {
            g.setComposite(alphaComposite);
        }

        return currentShape;
    }

    /**
     * Gets the alpha value that is applied to the graphics context.
     *
     * @return alpha context which will be applied by this command.
     */
    public AlphaComposite getAlphaComposite() {
        return alphaComposite;
    }

    /**
     * Sets the alpha composite to be executed by this command. Care should be taken as this
     * will have direct impact on the PDF shapes stack on the next paint call.
     *
     * @param alphaComposite alphaComposite object to be executed by this command.
     */
    public void setAlphaComposite(AlphaComposite alphaComposite) {
        this.alphaComposite = alphaComposite;
    }
}
