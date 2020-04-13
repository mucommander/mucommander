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
import org.icepdf.core.util.Defs;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.logging.Logger;

/**
 * AbstractDrawCmd provide common implementation details of any DrawCmd
 * implementation. Draw/paint specific system properties are currently handled
 * by this class.
 * <ul>
 * <li>org.icepdf.core.paint.disableClipping - disable clipping draw commands.</li>
 * <li>org.icepdf.core.paint.disableAlpha - disable alpha draw commands. </li>
 * </ul>
 *
 * @since 5.0
 */
public abstract class AbstractDrawCmd implements DrawCmd {

    protected static final Logger logger =
            Logger.getLogger(AbstractDrawCmd.class.toString());

    protected static boolean disableClipping;

    static {

        // disable clipping, helps with printing issues on windows where the
        // clip can sometimes blank a whole page.  This should only be used as
        // a lost resort.  Buffering to an image is another way to avoid the clip
        // problem.
        disableClipping =
                Defs.sysPropertyBoolean("org.icepdf.core.paint.disableClipping",
                        false);
    }

    public abstract Shape paintOperand(Graphics2D g, Page parentPage,
                                       Shape currentShape, Shape clip,
                                       AffineTransform base,
                                       OptionalContentState optionalContentState,
                                       boolean paintAlpha, PaintTimer paintTimer) throws InterruptedException;
}
