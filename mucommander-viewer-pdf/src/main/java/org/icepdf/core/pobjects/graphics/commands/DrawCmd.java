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
 * Common command pattern for painting PDF draw commands to a Java2D graphics
 * context. Any object that is Shapes shapes array list must implement this
 * interface.
 * <p/>
 * Implementing methods should execute as quickly as possible to avoid slowing
 * down render times.
 *
 * @since 5.0
 */
public interface DrawCmd {

    /**
     * Called by the Shapes class to paint all DrawCmd implementations.
     *
     * @param g                    graphics context to paint this paint command to.
     * @param parentPage           parentPage reference used to notify page painters.
     * @param currentShape         current shape to draw.
     * @param clip                 clip of parent which is the generally the page size.
     * @param base                 base transform of the page.
     * @param optionalContentState state of optional content visibility.
     * @param paintAlpha           enable/disable the alpha painting.
     * @param paintTimer           painTimer keeps track when a repaint should occur.
     * @return resulting shape if currentShape has been altered, otherwise
     *         returns the currentShape.  Current Shape is generally altered
     *         clip shape.
     */
    public Shape paintOperand(Graphics2D g,
                              Page parentPage,
                              Shape currentShape,
                              Shape clip,
                              AffineTransform base,
                              OptionalContentState optionalContentState,
                              boolean paintAlpha,
                              PaintTimer paintTimer) throws InterruptedException;
}
