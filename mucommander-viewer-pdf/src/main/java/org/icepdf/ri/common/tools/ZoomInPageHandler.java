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
 * Handles mouse click zoom in and scroll box zoom in.   The zoom is handled at the
 * AbstractDocumentView level as we accept mouse clicks from anywhere in the
 * view.
 *
 * @since 4.0
 */
public class ZoomInPageHandler extends SelectionBoxHandler implements ToolHandler {

    private static final Logger logger =
            Logger.getLogger(ZoomInPageHandler.class.toString());

    private Point initialPoint = new Point();

    public ZoomInPageHandler(DocumentViewController documentViewController,
                             AbstractPageViewComponent pageViewComponent,
                             DocumentViewModel documentViewModel) {
        super(documentViewController, pageViewComponent, documentViewModel);

        selectionBoxColour = Color.DARK_GRAY;
    }

    public void mouseDragged(MouseEvent e) {
        // handle text selection drags.
        if (documentViewController != null) {
            // update the currently selected box
            updateSelectionSize(e.getX(),e.getY(), pageViewComponent);
        }
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
                documentViewController.setZoomIn(mouse);
            }
        }
        if (pageViewComponent != null) {
            pageViewComponent.requestFocus();
        }
    }

    public void mousePressed(MouseEvent e) {
        // start selection box.
        if (documentViewController != null) {
            resetRectangle(e.getX(), e.getY());
            initialPoint.setLocation(e.getPoint());
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (documentViewController != null) {
            // update selection rectangle
            updateSelectionSize(e.getX(),e.getY(), pageViewComponent);

            // adjust the starting position of rectToDraw to match the actual
            // view position of the rectangle as the mouseEven position is
            // is relative to the page and now the view.
            int pageIndex = pageViewComponent.getPageIndex();
            Rectangle pageOffset = documentViewModel.getPageBounds(pageIndex);
            Rectangle absoluteRectToDraw = new Rectangle(
                    pageOffset.x + rectToDraw.x,
                    pageOffset.y + rectToDraw.y,
                    rectToDraw.width, rectToDraw.height);

            if (documentViewController.getViewPort() != null &&
                    absoluteRectToDraw.getWidth() > 0 &&
                    absoluteRectToDraw.getHeight() > 0) {
                // zoom in on rectangle bounds.
                float zoom = ZoomInPageHandler.calculateZoom(documentViewController,
                        absoluteRectToDraw, documentViewModel);

                // calculate the delta relative to current page position
                Point delta = new Point(
                        absoluteRectToDraw.x - pageOffset.x,
                        absoluteRectToDraw.y - pageOffset.y);
                documentViewController.setZoomToViewPort(zoom, delta, pageIndex, true);
            }
            // clear the rectangle
            clearRectangle(pageViewComponent);
        }
    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void setSelectionRectangle(Point cursorLocation, Rectangle selection) {
        // rectangle select tool
        setSelectionSize(selection, pageViewComponent);
    }

    public void paintTool(Graphics g) {
        paintSelectionBox(g, rectToDraw);
    }

    public void installTool() {

    }

    public void uninstallTool() {

    }

    public static float calculateZoom(DocumentViewController documentViewController,
                                      Rectangle rectToDraw,
                                      DocumentViewModel documentViewModel) {

        Dimension viewport = documentViewController.getViewPort().getParent().getSize();
        int selectionMax = rectToDraw.width;
        int screenMax = viewport.width;
        // find the largest dimension of the selection rectangle.
        if (screenMax < viewport.getHeight()) {
            screenMax = viewport.height;
        }
        if (selectionMax < rectToDraw.getHeight()) {
            selectionMax = rectToDraw.height;
        }
        // figure out the zoom ratio
        return (screenMax / (float) selectionMax) * documentViewModel.getViewZoom();
    }
}
