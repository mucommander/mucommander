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
import java.awt.event.MouseEvent;

/**
 * <p>Constructs a two column page view as defined in the PDF specification.
 * A two column page view displays pages in two columns with odd numbered pages
 * on the left.</p>
 * <p/>
 * <p>Page views are basic containers which use Swing Layout Containers to
 * place pages </p>
 *
 * @since 2.5
 */
@SuppressWarnings("serial")
public class TwoColumnPageView extends AbstractDocumentView {

    protected int viewAlignment;

    // specialized listeners for different gui operations
    protected CurrentPageChanger currentPageChanger;

    protected KeyListenerPageColumnChanger keyListenerPageChanger;

    public TwoColumnPageView(DocumentViewController documentDocumentViewController,
                             JScrollPane documentScrollpane,
                             DocumentViewModel documentViewModel,
                             final int viewAlignment) {

        super(documentDocumentViewController, documentScrollpane, documentViewModel);

        // used to redirect mouse events
        this.documentScrollpane = documentScrollpane;

        // assign view allignemnt
        this.viewAlignment = viewAlignment;

        // put all the gui elements together
        buildGUI();

        // add the first of many tools need for this views and others like it.
        currentPageChanger =
                new CurrentPageChanger(documentScrollpane, this,
                        documentViewModel.getPageComponents());

        // add page changing key listeners
        if (this.documentViewController.getParentController() instanceof SwingController) {
            keyListenerPageChanger =
                    KeyListenerPageColumnChanger.install(
                            (SwingController) this.documentViewController.getParentController(),
                            this.documentScrollpane, this, currentPageChanger);
        }
    }

    private void buildGUI() {
        // add all page components to gridlayout panel
        pagesPanel = new JPanel();
        pagesPanel.setBackground(BACKGROUND_COLOUR);
        // two column equals facing page view continuous
        GridLayout gridLayout = new GridLayout(0, 2, horizontalSpace, verticalSpace);
        pagesPanel.setLayout(gridLayout);

        // use a gridbag to center the page component panel
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
        java.util.List<AbstractPageViewComponent> pageComponents =
                documentViewModel.getPageComponents();

        if (pageComponents != null) {
            PageViewComponent pageViewComponent;
            for (int i = 0, max = pageComponents.size(), max2 = pageComponents.size();
                 i < max && i < max2; i++) {
                // save for facing page
                if (i == 0 && max2 > 2 && viewAlignment == RIGHT_VIEW) {
                    // should be adding spacer
                    pagesPanel.add(new JLabel());
                }
                pageViewComponent = pageComponents.get(i);
                if (pageViewComponent != null) {
                    pageViewComponent.setDocumentViewCallback(this);
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

    public void mouseReleased(MouseEvent e) {
        // let the current PageListener now about the mouse release
        currentPageChanger.mouseReleased(e);
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

        // trigger a relayout
        pagesPanel.removeAll();
        pagesPanel.invalidate();

        // make sure we call super.
        super.dispose();
    }

    public Dimension getDocumentSize() {
        float pageViewWidth = 0;
        float pageViewHeight = 0;
        if (pagesPanel != null) {
            // The page index and corresponding component index are approximately equal
            // If the first page is on the right, then there's a spacer on the left,
            //  bumping indexes up by one.
            int currPageIndex = documentViewController.getCurrentPageIndex();
            int currCompIndex = currPageIndex;
            int numComponents = pagesPanel.getComponentCount();
            boolean foundCurrent = false;
            while (currCompIndex >= 0 && currCompIndex < numComponents) {
                Component comp = pagesPanel.getComponent(currCompIndex);
                if (comp instanceof PageViewDecorator) {
                    PageViewDecorator pvd = (PageViewDecorator) comp;
                    PageViewComponent pvc = pvd.getPageViewComponent();
                    if (pvc.getPageIndex() == currPageIndex) {
                        Dimension dim = pvd.getPreferredSize();
                        pageViewWidth = dim.width;
                        pageViewHeight = dim.height;
                        foundCurrent = true;
                        break;
                    }
                }
                currCompIndex++;
            }
            if (foundCurrent) {
                // Determine if the page at (currPageIndex,currCompIndex) was
                //  on the left or right, so that if there's a page next to
                //  it, whether it's earlier or later in the component list,
                //  so we can get it's pageViewHeight and use that for our pageViewHeight
                //  calculation.
                // If the other component is past the ends of the component
                //  list, or not a PageViewDecorator, then current was either
                //  the first or last page in the document
                boolean evenPageIndex = ((currPageIndex & 0x1) == 0);
                boolean bumpedIndex = (currCompIndex != currPageIndex);
                boolean onLeft = evenPageIndex ^ bumpedIndex; // XOR
                int otherCompIndex = onLeft ? (currCompIndex + 1) : (currCompIndex - 1);
                if (otherCompIndex >= 0 && otherCompIndex < numComponents) {
                    Component comp = pagesPanel.getComponent(otherCompIndex);
                    if (comp instanceof PageViewDecorator) {
                        PageViewDecorator pvd = (PageViewDecorator) comp;
                        Dimension dim = pvd.getPreferredSize();
                        pageViewWidth = dim.width;
                        pageViewHeight = dim.height;
                    }
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

//    public void adjustmentValueChanged(AdjustmentEvent e){
//
////        System.out.println("Adjusting " + e.getAdjustable().getValue());
//          if (e.getAdjustable().getOrientation() == Adjustable.HORIZONTAL){
////              System.out.println("horizontal");
//          }
//          else if (e.getAdjustable().getOrientation() == Adjustable.VERTICAL){
////              System.out.println("vertical");
////              int newValue = e.getAdjustable().getValue();
////              System.out.println("value " + newValue);
////              e.getAdjustable().setValue(0);
//          }
//    }

//    public void mouseDragged(MouseEvent e) {
//        Point point = e.getPoint();
//        super.mouseDragged(e);
//        Point currentLocation = getLocation();
//        pageViewComponentImpl.setBounds(point.x,
//                                        point.y, 640,480);
//    }
}
