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
import org.icepdf.ri.common.KeyListenerPageChanger;
import org.icepdf.ri.common.MouseWheelListenerPageChanger;
import org.icepdf.ri.common.SwingController;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Constructs a two page view as defined in the PDF specification.
 * A two column page view displays two pages with odd numbered pages
 * on the left.</p>
 * <p/>
 * <p>Page views are basic containers which use Swing Layout Containers to
 * place pages </p>
 *
 * @since 2.5
 */
@SuppressWarnings("serial")
public class TwoPageView extends AbstractDocumentView {

    protected int viewAlignment;

    // specialized listeners for different gui operations
    protected Object pageChangerListener;
    protected KeyListenerPageChanger keyListenerPageChanger;
    protected CurrentPageChanger currentPageChanger;

    public TwoPageView(DocumentViewController documentDocumentViewController,
                       JScrollPane documentScrollpane,
                       DocumentViewModel documentViewModel,
                       final int viewAlignment) {

        super(documentDocumentViewController, documentScrollpane, documentViewModel);

        // used to redirect mouse events
        this.documentScrollpane = documentScrollpane;

        // assign view alignment
        this.viewAlignment = viewAlignment;

        // put all the gui elements together
        buildGUI();

        // add page changing key listeners
        if (this.documentViewController.getParentController() instanceof SwingController) {
            pageChangerListener =
                    MouseWheelListenerPageChanger.install(
                            (SwingController) this.documentViewController.getParentController(),
                            this.documentScrollpane, this);

            keyListenerPageChanger =
                    KeyListenerPageChanger.install((SwingController) this.documentViewController.getParentController(),
                            this.documentScrollpane, this);
        }

        // add the first of many tools need for this views and others like it.
        currentPageChanger =
                new CurrentPageChanger(documentScrollpane, this,
                        documentViewModel.getPageComponents(),
                        false);
    }


    private void buildGUI() {
        // add all page components to gridlayout panel
        pagesPanel = new JPanel();
        pagesPanel.setBackground(BACKGROUND_COLOUR);
        // one column equals single page view continuous
        GridLayout gridLayout = new GridLayout(0, 2, horizontalSpace, verticalSpace);
        pagesPanel.setLayout(gridLayout);

        // use a gridbag to center the page component panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weighty = 1.0;                  // allows vertical resizing
        gbc.weightx = 1.0;                  // allows horizontal resizing
        gbc.insets =  // component spacer [top, left, bottom, right]
                new Insets(layoutInserts, layoutInserts, layoutInserts, layoutInserts);
        gbc.gridwidth = GridBagConstraints.REMAINDER;      // one component per row

        // finally add all the components
        // add components for every page in the document
        updateDocumentView();

        this.setLayout(new GridBagLayout());
        this.add(pagesPanel, gbc);
    }

    public void updateDocumentView() {

        java.util.List<AbstractPageViewComponent> pageComponents =
                documentViewModel.getPageComponents();

        if (pageComponents != null) {
            // remove old component
            pagesPanel.removeAll();
            pagesPanel.validate();
            AbstractPageViewComponent pageViewComponent;
            int count = 0;
            int index = documentViewModel.getViewCurrentPageIndex();
            int docLength = pageComponents.size();


            if (viewAlignment == RIGHT_VIEW &&
                    ((index > 0 && index % 2 == 0) || (index > 0 && docLength == 2))) {
                index--;
            }

            for (int i = index; i < docLength && count < 2; i++) {
                // save for facing page
                if (i == 0 && docLength > 2 && viewAlignment == RIGHT_VIEW) {
                    // should be adding spacer
                    pagesPanel.add(new JLabel());
                    count++;
                }
                pageViewComponent = pageComponents.get(i);
                if (pageViewComponent != null) {
                    pageViewComponent.setDocumentViewCallback(this);
                    // add component to layout
                    pagesPanel.add(new PageViewDecorator(pageViewComponent));
                    pageViewComponent.invalidate();
                    pageViewComponent.validate();
                    count++;
                }
            }
            documentScrollpane.validate();

            // make sure we have setup all pages with callback call.
            for (PageViewComponent pageViewCom : pageComponents) {
                if (pageViewCom != null) {
                    pageViewCom.setDocumentViewCallback(this);
                }
            }
        }
    }

    /**
     * Returns a next page increment of two.
     */
    public int getNextPageIncrement() {
        return 2;
    }

    /**
     * Returns a previous page increment of two.
     */
    public int getPreviousPageIncrement() {
        return 2;
    }

    public void dispose() {
        disposing = true;
        // remove utilities
        if (pageChangerListener != null) {
            MouseWheelListenerPageChanger.uninstall(documentScrollpane,
                    pageChangerListener);
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
            int count = pagesPanel.getComponentCount();
            Component comp;
            // should only have one page view decorator for single page view.
            for (int i = 0; i < count; i++) {
                comp = pagesPanel.getComponent(i);
                if (comp instanceof PageViewDecorator) {
                    PageViewDecorator pvd = (PageViewDecorator) comp;
                    Dimension dim = pvd.getPreferredSize();
                    pageViewWidth = dim.width;
                    pageViewHeight = dim.height;
                    break;
                }
            }
        }
        // normalize the dimensions to a zoom level of zero.
        float currentZoom = documentViewModel.getViewZoom();
        pageViewWidth = Math.abs(pageViewWidth / currentZoom);
        pageViewHeight = Math.abs(pageViewHeight / currentZoom);

        // two pages wide, generalization, pages are usually the same size we
        // don't bother to look at the second pages size for the time being. 
        pageViewWidth *= 2;

        // add any horizontal padding from layout manager
        pageViewWidth += AbstractDocumentView.horizontalSpace * 4;
        pageViewHeight += AbstractDocumentView.verticalSpace * 2;

        return new Dimension((int) pageViewWidth, (int) pageViewHeight);
    }

    public void paintComponent(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        g.setColor(BACKGROUND_COLOUR);
        g.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
        // paint selection box
        super.paintComponent(g);
    }
}
