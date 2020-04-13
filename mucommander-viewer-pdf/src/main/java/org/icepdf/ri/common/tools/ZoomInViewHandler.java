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

import org.icepdf.ri.common.views.AbstractDocumentView;
import org.icepdf.ri.common.views.AbstractPageViewComponent;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.common.views.DocumentViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.logging.Logger;

/**
 * ZoomInViewHandler handles selection box zoom features when the mouse is
 * dragged creating  box which will be used to calculate a corresponding zoom
 * level.
 *
 * @since 5.0
 */
public class ZoomInViewHandler extends SelectionBoxHandler implements ToolHandler {

    private static final Logger logger =
            Logger.getLogger(ZoomInPageHandler.class.toString());

    private AbstractDocumentView parentComponent;

    public ZoomInViewHandler(DocumentViewController documentViewController,
                             DocumentViewModel documentViewModel,
                             AbstractDocumentView parentComponent) {
        super(documentViewController, null, documentViewModel);
        this.parentComponent = parentComponent;
    }

    public void mouseDragged(MouseEvent e) {
        // handle text selection drags.
        if (documentViewController != null) {
            // update the currently selected box
            updateSelectionSize(e.getX(),e.getY(), parentComponent);

            // add selection box to child pages
            if (documentViewModel != null) {
                java.util.List<AbstractPageViewComponent> pages =
                        documentViewModel.getPageComponents();
                for (AbstractPageViewComponent page : pages) {
                    Rectangle tmp = SwingUtilities.convertRectangle(
                            parentComponent, getRectToDraw(), page);
                    if (page.getBounds().intersects(tmp)) {

                        // convert the rectangle to the correct space
                        Rectangle selectRec =
                                SwingUtilities.convertRectangle(parentComponent,
                                        rectToDraw,
                                        page);
                        // set the selected region.
                        page.setSelectionRectangle(
                                SwingUtilities.convertPoint(parentComponent,
                                        e.getPoint(), page),
                                selectRec);
                    }
                }
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        if ((e.getModifiers() & MouseEvent.MOUSE_PRESSED) != 0) {
            if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
                // zoom in
                documentViewController.setZoomIn(e.getPoint());
            }
        }
        if (parentComponent != null) {
            parentComponent.requestFocus();
        }
    }

    public void mousePressed(MouseEvent e) {
        // start selection box.
        if (documentViewController != null) {
            resetRectangle(e.getX(), e.getY());
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (documentViewController != null) {
            // update selection rectangle
            updateSelectionSize(e.getX(),e.getY(), parentComponent);

            if (documentViewController.getViewPort() != null &&
                    rectToDraw.getWidth() > 0 &&
                    rectToDraw.getHeight() > 0) {
                // zoom in on rectangle bounds.
                float zoom = ZoomInPageHandler.calculateZoom(
                        documentViewController, rectToDraw, documentViewModel);

                // scale the zoom box center to the new location
                Point center = new Point((int) rectToDraw.getCenterX(),
                        (int) rectToDraw.getCenterY());

                documentViewController.setZoomCentered(zoom, center, true);
            }

            // clear the rectangle
            clearRectangle(parentComponent);

            // clear the child rectangle
            // deselect rectangles on other selected pages.
            // consider only repainting visible pages.
            List<AbstractPageViewComponent> selectedPages =
                    documentViewModel.getPageComponents();
            if (selectedPages != null &&
                    selectedPages.size() > 0) {
                for (AbstractPageViewComponent pageComp : selectedPages) {
                    if (pageComp != null && pageComp.isVisible()) {
                        pageComp.clearSelectionRectangle();
                    }
                }
            }
        }
    }

    public void installTool() {

    }

    public void uninstallTool() {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void setSelectionRectangle(Point cursorLocation, Rectangle selection) {

    }

    public void paintTool(Graphics g) {
        paintSelectionBox(g, rectToDraw);
    }
}
