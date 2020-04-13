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
import org.icepdf.core.pobjects.graphics.TextSprite;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * The TextSpriteDrawCmd stores an TextSprite instance and when executed the
 * the TextSprite paint method is called the the respective glyphs are painted
 * the graphics context.
 *
 * @since 5.0
 */
public class TextSpriteDrawCmd extends AbstractDrawCmd {

    private TextSprite textSprite;

    public TextSpriteDrawCmd(TextSprite textSprite) {
        this.textSprite = textSprite;
    }

    @Override
    public Shape paintOperand(Graphics2D g, Page parentPage, Shape currentShape,
                              Shape clip, AffineTransform base,
                              OptionalContentState optionalContentState,
                              boolean paintAlpha, PaintTimer lastPaintTime) {

        if (optionalContentState.isVisible() &&
                textSprite.intersects(g.getClip())) {
            textSprite.paint(g);
        }

        return currentShape;
    }

    public TextSprite getTextSprite() {
        return textSprite;
    }
}
