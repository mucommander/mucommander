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

import org.icepdf.core.pobjects.Page;

import java.awt.*;

/**
 * WatermarkCallback allows a user to set a default watermark at the document
 * level which will be applied at paint time for print and/or screen.  The callback
 * can be added to the Document class or on a page by page bases.
 * <p/>
 * The renderingHintType can be used to detect two different output modes;
 * GraphicsRenderingHints.PRINT and GraphicsRenderingHints.SCREEN.
 * <p/>
 * <b>Note:</b> be careful not to hold a reference to the Page object as an
 * an instance of the class as a memory leak may result.
 *
 * @since 5.1.0
 */
public interface WatermarkCallback {

    /**
     * Paints a watermark on top of the pages content.
     *
     * @param g              graphics content used to paint the page content.
     * @param page           page that is being page.
     * @param renderHintType rendering hints,  SCREEN or PRINT
     * @param boundary       boundary box used to paint the page content.
     * @param userRotation   user rotation specified to paint page content
     * @param userZoom       user zoom specified to paint page content.
     */
    void paintWatermark(Graphics g, Page page, int renderHintType, final int boundary,
                        float userRotation, float userZoom);
}
