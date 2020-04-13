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
import org.icepdf.core.pobjects.graphics.OptionalContentState;
import org.icepdf.core.pobjects.graphics.PaintTimer;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * GraphicsStateCmd currently doesn't paint anything but rather acts as a
 * queue for the PostScriptEncoder to write out a named graphics context.
 * This command would not normally be used in
 *
 * @since 5.0
 */
public class GraphicsStateCmd extends AbstractDrawCmd {

    private Name graphicStateName;

    public GraphicsStateCmd(Name graphicStateName) {
        this.graphicStateName = graphicStateName;
    }

    @Override
    public Shape paintOperand(Graphics2D g, Page parentPage, Shape currentShape,
                              Shape clip, AffineTransform base,
                              OptionalContentState optionalContentState, boolean paintAlpha, PaintTimer paintTimer) {
        return currentShape;
    }

    public Name getGraphicStateName() {
        return graphicStateName;
    }
}