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
package org.icepdf.ri.common.views;

import org.icepdf.ri.common.CurrentPageChanger;
import org.icepdf.ri.common.KeyListenerPageColumnChanger;
import org.icepdf.ri.common.SwingController;

import javax.swing.*;
import java.awt.*;
import java.util.List;


/**
 * <p>Constructs a one column page view as defined in the PDF specification. A one
 * column page view displays pages continuously in one column.</p>
 * <p/>
 * <p>Page views are basic containers which use Swing Layout Containers to
 * place pages </p>
 *
 * @since 2.5
 */
@SuppressWarnings("serial")
public class OneColumnPageView extends AbstractDocumentView {

    // specialized listeners for different gui operations
    protected CurrentPageChanger currentPageChanger;

    protected KeyListenerPageColumnChanger keyListenerPageChanger;

    public OneColumnPageView(DocumentViewController documentDocumentViewController,
                             JScrollPane documentScrollpane,
                             DocumentViewModel documentViewModel) {

        super(documentDocumentViewController, documentScrollpane, documentViewModel);

        // used to redirect mouse events
        this.documentScrollpane = documentScrollpane;

        // put all the gui elements together
        buildGUI();

        // add the first of many tools need for this views and others like it.
        currentPageChanger =
                new CurrentPageChanger(documentScrollpane, this,
                        documentViewModel.getPageComponents());

        // add page changing key listeners
        if (this.documentViewController.getParentController() instanceof SwingController) {
            keyListenerPageChanger =
                    KeyListenerPageColumnChanger.install((SwingController) this.documentViewController.getParentController(),
                            this.documentScrollpane, this, currentPageChanger);
        }
    }

    private void buildGUI() {
        // add all page components to grid layout panel
        pagesPanel = new JPanel();
        pagesPanel.setBackground(BACKGROUND_COLOUR);
        // one column equals single page view continuous
        GridLayout gridLayout = new GridLayout(0, 1, horizontalSpace, verticalSpace);
        pagesPanel.setLayout(gridLayout);

        // use a grid bag to center the page component panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weighty = 1.0;                  // allows vertical resizing
        gbc.weightx = 1.0;                  // allows horizontal resizing
        gbc.insets =  // component spacer [top, left, bottom, right]
                new Insets(layoutInserts, layoutInserts, layoutInserts, layoutInserts);
        gbc.gridwidth = GridBagConstraints.REMAINDER;      // one component per row

        this.setLayout(new GridBagLayout());
        this.add(pagesPanel, gbc);

        // finally add all the components
        // add components for every page in the document
        List<AbstractPageViewComponent> pageComponents =
                documentViewModel.getPageComponents();

        if (pageComponents != null) {
            for (PageViewComponent pageViewComponent : pageComponents) {
                if (pageViewComponent != null) {
                    pageViewComponent.setDocumentViewCallback(this);
                    // add component to layout
                    pagesPanel.add(new PageViewDecorator(
                            (AbstractPageViewComponent) pageViewComponent));
                }
            }
        }
    }

    // nothing needs to be done for a column view as all components are already
    // available
    public void updateDocumentView() {
    }

    /**
     * Returns a next page increment of one.
     */
    public int getNextPageIncrement() {
        return 1;
    }

    /**
     * Returns a previous page increment of one.
     */
    public int getPreviousPageIncrement() {
        return 1;
    }

    public void dispose() {
        disposing = true;
        // remove utilities
        if (currentPageChanger != null) {
            currentPageChanger.dispose();
        }
        if (keyListenerPageChanger != null) {
            keyListenerPageChanger.uninstall();
        }

        // trigger a re-layout
        pagesPanel.removeAll();
        pagesPanel.invalidate();

        // make sure we call super.
        super.dispose();
    }

    public Dimension getDocumentSize() {
        float pageViewWidth = 0;
        float pageViewHeight = 0;
        if (pagesPanel != null) {
            int currCompIndex = documentViewController.getCurrentPageIndex();
            int numComponents = pagesPanel.getComponentCount();
            if (currCompIndex >= 0 && currCompIndex < numComponents) {
                Component comp = pagesPanel.getComponent(currCompIndex);
                if (comp instanceof PageViewDecorator) {
                    PageViewDecorator pvd = (PageViewDecorator) comp;
                    Dimension dim = pvd.getPreferredSize();
                    pageViewWidth = dim.width;
                    pageViewHeight = dim.height;
                }
            }
        }
        // normalize the dimensions to a zoom level of zero.
        float currentZoom = documentViewModel.getViewZoom();
        pageViewWidth = Math.abs(pageViewWidth / currentZoom);
        pageViewHeight = Math.abs(pageViewHeight / currentZoom);

        // add any horizontal padding from layout manager
        pageViewWidth += AbstractDocumentView.horizontalSpace * 2;
        pageViewHeight += AbstractDocumentView.verticalSpace * 2;
        return new Dimension((int) pageViewWidth, (int) pageViewHeight);
    }

    public void paintComponent(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        // paint background gray
        g.setColor(BACKGROUND_COLOUR);
        g.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
        // paint selection box
        super.paintComponent(g);
    }
}
