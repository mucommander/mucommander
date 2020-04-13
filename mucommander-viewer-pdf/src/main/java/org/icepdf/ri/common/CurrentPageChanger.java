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
package org.icepdf.ri.common;

import org.icepdf.core.util.PropertyConstants;
import org.icepdf.ri.common.views.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
public class CurrentPageChanger extends MouseAdapter implements AdjustmentListener {

    private static final Logger logger =
            Logger.getLogger(CurrentPageChanger.class.toString());

    private boolean isScrolled = false;

    private List<AbstractPageViewComponent> pageComponents;
    private JScrollPane scrollpane;
    private Object mouseWheelCurrentPageListener;
    private AbstractDocumentView documentView;
    private DocumentViewModel documentViewModel;

    public CurrentPageChanger(JScrollPane scrollpane,
                              AbstractDocumentView documentView,
                              List<AbstractPageViewComponent> pageComponents) {
        this(scrollpane, documentView, pageComponents, true);
    }

    public CurrentPageChanger(JScrollPane scrollpane,
                              AbstractDocumentView documentView,
                              List<AbstractPageViewComponent> pageComponents,
                              boolean addWheelMouseListener) {

        this.pageComponents = pageComponents;
        this.scrollpane = scrollpane;
        this.documentView = documentView;
        documentViewModel = documentView.getViewModel();

        // listen for scroll bar manaipulators
        this.documentView.addMouseListener(this);
        this.scrollpane.getHorizontalScrollBar().addAdjustmentListener(this);
        this.scrollpane.getHorizontalScrollBar().addMouseListener(this);

        this.scrollpane.getVerticalScrollBar().addAdjustmentListener(this);
        this.scrollpane.getVerticalScrollBar().addMouseListener(this);

//        addMouseListenerToAnyButtonsIn(scrollpane.getHorizontalScrollBar());
//        addMouseListenerToAnyButtonsIn(scrollpane.getVerticalScrollBar());

        // load wheel mouse listener
        mouseWheelCurrentPageListener = MouseWheelCurrentPageListener.install(scrollpane, this);
    }

    private void addMouseListenerToAnyButtonsIn(java.awt.Component comp) {
        int children = (comp instanceof Container) ? ((Container) comp).getComponentCount() : -1;
        for (int i = 0; i < children; i++) {
            Component kid = ((Container) comp).getComponent(i);
            if (kid instanceof javax.swing.AbstractButton) {
                kid.addMouseListener(this);
            }
            addMouseListenerToAnyButtonsIn(kid);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (isScrolled) {
            calculateCurrentPage();
            isScrolled = false;
        }
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        isScrolled = true;
    }

    public void calculateCurrentPage() {
        if (pageComponents != null) {
            Rectangle viewport = scrollpane.getViewport().getViewRect();
            // find visible pages
            ArrayList<PageViewComponent> visiblePages =
                    new ArrayList<PageViewComponent>(10);
            Rectangle pageBounds;
            int pageCount = 0;
            for (AbstractPageViewComponent pageComponent : pageComponents) {
                if (pageComponent != null) {
                    pageBounds = documentViewModel.getPageBounds(pageCount);
                    if (pageBounds != null &&
                            pageComponent.isShowing()) {
                        visiblePages.add(pageComponent);
                    }
                }
                pageCount++;
            }

            // find center point of view port
            int x = viewport.x + (viewport.width / 2);
            int y = viewport.y + (viewport.height / 2);
            Point centerView = new Point(x, y);

            // find out which page center is closest to center and thus the new current page
            double minLength = Double.MAX_VALUE;
            int minPage = -1;
            double tmpDistance;

            for (PageViewComponent pageComponent : visiblePages) {
                if (pageComponent != null) {
                    pageBounds = documentViewModel.getPageBounds(
                            pageComponent.getPageIndex());
                    x = pageBounds.x + (pageBounds.width / 2);
                    y = pageBounds.y + (pageBounds.height / 2);
                    // find minimum page.
                    tmpDistance = centerView.distance(x, y);
                    if (tmpDistance < minLength) {
                        minLength = tmpDistance;
                        minPage = pageComponent.getPageIndex();
                    }
                }
            }

            //clean up
            visiblePages.clear();
            visiblePages.trimToSize();

            // finally send out event to update page number
            int oldCurrentPage = documentViewModel.getViewCurrentPageIndex();
            documentViewModel.setViewCurrentPageIndex(minPage);
            DocumentViewControllerImpl documentViewController = (DocumentViewControllerImpl) documentView.getParentViewController();
            documentViewController.firePropertyChange(PropertyConstants.DOCUMENT_CURRENT_PAGE,
                    oldCurrentPage,
                    minPage);
        }
    }

    public void dispose() {
        // remove standard mouse listeners
        documentView.removeMouseListener(this);
        scrollpane.getHorizontalScrollBar().removeAdjustmentListener(this);
        scrollpane.getHorizontalScrollBar().removeMouseListener(this);
        scrollpane.getVerticalScrollBar().removeAdjustmentListener(this);
        scrollpane.getVerticalScrollBar().removeMouseListener(this);

        // Remove wheel mouse listener
        MouseWheelCurrentPageListener.uninstall(scrollpane, mouseWheelCurrentPageListener);
    }
}
