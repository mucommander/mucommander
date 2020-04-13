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
package org.icepdf.ri.common.tools;

import org.icepdf.ri.common.views.AbstractPageViewComponent;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.common.views.DocumentViewModel;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

/**
 * Handles mouse click zoom out functionality.   The zoom is handled at the
 * AbstractDocumentView level as we accept mouse clicks from anywhere in the
 * view.
 *
 * @since 4.0
 */
public class ZoomOutPageHandler implements ToolHandler {

    private static final Logger logger =
            Logger.getLogger(ZoomOutPageHandler.class.toString());

    private AbstractPageViewComponent pageViewComponent;
    private DocumentViewController documentViewController;
    private DocumentViewModel documentViewModel;

    public ZoomOutPageHandler(DocumentViewController documentViewController,
                              AbstractPageViewComponent pageViewComponent,
                              DocumentViewModel documentViewModel) {
        this.documentViewController = documentViewController;
        this.pageViewComponent = pageViewComponent;
        this.documentViewModel = documentViewModel;
    }


    public void mouseClicked(MouseEvent e) {
        if ((e.getModifiers() & MouseEvent.MOUSE_PRESSED) != 0) {
            if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
                // zoom in
                Point pageOffset = documentViewModel.getPageBounds(
                        pageViewComponent.getPageIndex()).getLocation();
                Point mouse = e.getPoint();
                mouse.setLocation(pageOffset.x + mouse.x,
                        pageOffset.y + mouse.y);
                documentViewController.setZoomOut(mouse);
            }
        }
        if (pageViewComponent != null) {
            pageViewComponent.requestFocus();
        }
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void paintTool(Graphics g) {

    }

    public void installTool() {

    }

    public void uninstallTool() {

    }
}