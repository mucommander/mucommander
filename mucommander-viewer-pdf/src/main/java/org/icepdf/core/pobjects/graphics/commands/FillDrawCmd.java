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
 * FillDrawCmd will execute the Graphics2D fill command on the currentShape.
 * The execute method will not fill the currentShape if the shape does not
 * interest the current graphics clip.
 *
 * @since 5.0
 */
public class FillDrawCmd extends AbstractDrawCmd {

    @Override
    public Shape paintOperand(Graphics2D g, Page parentPage, Shape currentShape,
                              Shape clip, AffineTransform base,
                              OptionalContentState optionalContentState,
                              boolean paintAlpha, PaintTimer paintTimer) {
        if (optionalContentState.isVisible() && currentShape != null &&
                currentShape.intersects(g.getClip().getBounds())) {
            g.fill(currentShape);
            // Send a PaintPage Event to listeners
//            if (parentPage != null && paintTimer.shouldTriggerRepaint()) {
//                parentPage.notifyPaintPageListeners();
//            }
        }
        return currentShape;
    }
}
