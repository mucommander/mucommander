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

import org.icepdf.core.util.PropertyConstants;
import org.icepdf.ri.common.views.AbstractPageViewComponent;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.common.views.DocumentViewModel;
import org.icepdf.ri.common.views.PageViewComponentImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * TextSelectionViewHandler propagates text selection events into the the
 * views child page components.  This handle is required for multi-page text
 * selection.  On mouse click all text selection states are removed.
 *
 * @since 5.0
 */
public class TextSelectionViewHandler extends TextSelection
        implements ToolHandler, MouseWheelListener {

    protected static final Logger logger =
            Logger.getLogger(TextSelectionViewHandler.class.toString());

    protected JComponent parentComponent;

    protected boolean isDragging;

    public TextSelectionViewHandler(DocumentViewController documentViewController,
                                    DocumentViewModel documentViewModel,
                                    JComponent parentComponent) {
        super(documentViewController, null, documentViewModel);
        this.parentComponent = parentComponent;
    }

    public void mouseClicked(MouseEvent e) {

        // clear all selected text.
        documentViewController.clearSelectedText();
        clearSelectionState();

        // check if we are over a page
        PageViewComponentImpl pageComponent = isOverPageComponent(parentComponent, e);

        if (pageComponent != null) {
            pageComponent.requestFocus();
            // click word and line selection
            MouseEvent modeEvent = SwingUtilities.convertMouseEvent(parentComponent, e, pageComponent);
            pageComponent.getTextSelectionPageHandler().wordLineSelection(
                    modeEvent.getClickCount(), modeEvent.getPoint(), pageComponent);
        }
    }

    public void mousePressed(MouseEvent e) {
        // clear all selected text.
        documentViewController.clearSelectedText();
        clearSelectionState();

        lastMousePressedLocation = e.getPoint();

        // start selection box.
        resetRectangle(e.getX(), e.getY());

        // check if we are over a page
        PageViewComponentImpl pageComponent = isOverPageComponent(parentComponent, e);
        if (pageComponent != null) {
            pageComponent.requestFocus();
            MouseEvent modeEvent = SwingUtilities.convertMouseEvent(parentComponent, e, pageComponent);
            pageComponent.getTextSelectionPageHandler().selectionStart(modeEvent.getPoint(), pageComponent, true);
        }
    }

    public void mouseReleased(MouseEvent e) {

        isDragging = false;

        // deselect rectangles on other selected pages.
        ArrayList<AbstractPageViewComponent> selectedPages =
                documentViewModel.getSelectedPageText();

        // check if we are over a page
        AbstractPageViewComponent pageComponent =
                isOverPageComponent(parentComponent, e);

        if (pageComponent != null) {
            MouseEvent modeEvent = SwingUtilities.convertMouseEvent(
                    parentComponent, e, pageComponent);

            if (selectedPages != null &&
                    selectedPages.size() > 0) {
                PageViewComponentImpl pageComp;
                for (AbstractPageViewComponent selectedPage : selectedPages) {
                    pageComp = (PageViewComponentImpl)selectedPage;
                    if (pageComp != null) {
                        pageComp.getTextSelectionPageHandler().selectionEnd(modeEvent.getPoint(), pageComp);
                    }
                }
            }
        }
        // finally if we have selected any text then fire a property change event
        if (selectedPages != null && selectedPages.size() > 0) {
            documentViewController.firePropertyChange(
                    PropertyConstants.TEXT_SELECTED,
                    null, null);
        }

        // clear the rectangle
        clearRectangle(parentComponent);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {

        if (isDragging) {
            Component target = documentViewController.getViewPort().getView();
            Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), target);
            MouseEvent m = new MouseEvent(target,
                    0, e.getWhen(), e.getModifiers(),
                    p.x, p.y,
                    e.getClickCount(), e.isPopupTrigger(), e.getButton());
            mouseDragged(m);
        }
    }

    public void mouseDragged(MouseEvent e) {

        // handle text selection drags.
        if (documentViewController != null) {
            isDragging = true;

            // update the currently parentComponent box
            updateSelectionSize(e.getX(), e.getY(), parentComponent);

            // clear previously selected pages
            documentViewModel.clearSelectedPageText();

            // add selection box to child pages
            if (documentViewModel != null) {
                java.util.List<AbstractPageViewComponent> pages =
                        documentViewModel.getPageComponents();
                for (AbstractPageViewComponent page : pages) {
                    Rectangle tmp = SwingUtilities.convertRectangle(
                            parentComponent, getRectToDraw(), page);
                    if (page.getBounds().intersects(tmp)) {
                        // add the page to the page as it is marked for selection
                        documentViewModel.addSelectedPageText(page);

                        Point modEvent = SwingUtilities.convertPoint(parentComponent,
                                e.getPoint(), page);

                        // set the selected region.
                        page.setSelectionRectangle(modEvent, tmp);

                        // pass the selection movement on to the page.
                        boolean isMovingDown = lastMousePressedLocation.y <= e.getPoint().y;
                        boolean isMovingRight = lastMousePressedLocation.x <= e.getPoint().x;
                        ((PageViewComponentImpl)page).getTextSelectionPageHandler().selection(modEvent, page, isMovingDown, isMovingRight);

                    } else {
                        documentViewModel.removeSelectedPageText(page);
                        page.clearSelectedText();
                        page.repaint();
                    }
                }
            }
        }

    }

    public void mouseMoved(MouseEvent e) {

        PageViewComponentImpl pageComponent = isOverPageComponent(parentComponent, e);
        if (pageComponent != null) {
            // assign the correct icon state for the cursor.
            MouseEvent modeEvent = SwingUtilities.convertMouseEvent(parentComponent, e, pageComponent);
            pageComponent.getTextSelectionPageHandler().selectionIcon(modeEvent.getPoint(), pageComponent);
        }

    }

    public void paintTool(Graphics g) {
//        paintSelectionBox(g, rectToDraw);
    }

    @Override
    public void setSelectionRectangle(Point cursorLocation, Rectangle selection) {

    }

    public void installTool() {

    }

    public void uninstallTool() {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }
}
