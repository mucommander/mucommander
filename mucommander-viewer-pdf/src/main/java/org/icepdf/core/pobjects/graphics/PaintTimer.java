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

import org.icepdf.core.util.Defs;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PaintTimer encapsulates the time calculation used to fire repaint events
 * to the view.
 *
 * @since 5.0
 */
public class PaintTimer {

    protected static final Logger logger =
            Logger.getLogger(PaintTimer.class.toString());

    protected static int paintDelay;

    static {
        try {
            // Delay between painting calls.
            paintDelay =
                    Defs.intProperty("org.icepdf.core.views.refreshfrequency",
                            250);
        } catch (NumberFormatException e) {
            logger.log(Level.FINE, "Error reading buffered scale factor");
        }
    }

    private long lastPaintTime;

    public boolean shouldTriggerRepaint() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPaintTime > paintDelay) {
            lastPaintTime = currentTime;
            return true;
        } else {
            return false;
        }
    }

}
