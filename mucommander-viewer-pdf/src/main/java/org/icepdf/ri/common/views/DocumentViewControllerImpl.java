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

import org.icepdf.core.SecurityCallback;
import org.icepdf.core.pobjects.Destination;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.NamedDestinations;
import org.icepdf.core.pobjects.PageTree;
import org.icepdf.core.search.DocumentSearchController;
import org.icepdf.core.util.PropertyConstants;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.views.annotations.AbstractAnnotationComponent;
import org.icepdf.ri.common.views.annotations.PopupAnnotationComponent;
import org.icepdf.ri.images.Images;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>The DocumentViewControllerImpl is responsible for controlling the four
 * default view models specified by the PDF specification.  This class is used
 * associated with the SwingController, but all view specific control is passed
 * to this class. </p>
 *
 * @since 2.5
 */
@SuppressWarnings("serial")
public class DocumentViewControllerImpl
        implements DocumentViewController, ComponentListener, PropertyChangeListener {

    private static final Logger logger =
            Logger.getLogger(DocumentViewControllerImpl.class.toString());

    /**
     * Displays a one page at a time view.
     */
    public static final int ONE_PAGE_VIEW = 1;
    /**
     * Displays a the pages in one column.
     */
    public static final int ONE_COLUMN_VIEW = 2;
    /**
     * Displays the pages two at a time, with odd-numbered pages on the left.
     */
    public static final int TWO_PAGE_LEFT_VIEW = 3;
    /**
     * Displays the pages in two columns, with odd-numbered pages on the left.
     */
    public static final int TWO_COLUMN_LEFT_VIEW = 4;
    /**
     * Displays the pages two at a time, with event-numbered pages on the left.
     */
    public static final int TWO_PAGE_RIGHT_VIEW = 5;
    /**
     * Displays the pages in two columns, with even-numbered pages on the left.
     */
    public static final int TWO_COLUMN_RIGHT_VIEW = 6;
    /**
     * Displays the pages in two columns, with even-numbered pages on the left.
     */
    public static final int USE_ATTACHMENTS_VIEW = 7;
    /**
     * Zoom factor used when zooming in or out.
     */
    public static final float ZOOM_FACTOR = 1.2F;
    /**
     * Rotation factor used with rotating document.
     */
    public static final float ROTATION_FACTOR = 90F;

    protected float[] zoomLevels;

    protected Document document;

    protected DocumentViewModel documentViewModel;
    protected DocumentView documentView;

    protected JScrollPane documentViewScrollPane;

    protected int viewportWidth, oldViewportWidth;
    protected int viewportHeight, oldViewportHeight;
    protected int viewType, oldViewType;
    protected int viewportFitMode, oldViewportFitMode;
    protected int cursorType;
    protected SwingController viewerController;
    protected AnnotationCallback annotationCallback;
    protected SecurityCallback securityCallback;
    protected PropertyChangeSupport changes = new PropertyChangeSupport(this);


    public DocumentViewControllerImpl(final SwingController viewerController) {

        this.viewerController = viewerController;

        documentViewScrollPane = new JScrollPane();
        documentViewScrollPane.getViewport().setBackground(AbstractDocumentView.BACKGROUND_COLOUR);

        // set scroll bar speeds
        documentViewScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        documentViewScrollPane.getHorizontalScrollBar().setUnitIncrement(20);

        // add a delete key functionality for annotation edits.
        Action deleteAnnotation = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (documentViewModel != null) {
                    deleteCurrentAnnotation();
                    viewerController.reflectUndoCommands();
                }
            }
        };
        InputMap inputMap = documentViewScrollPane.getInputMap(
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("DELETE"),
                "removeSelectedAnnotation");
        documentViewScrollPane.getActionMap().put("removeSelectedAnnotation",
                deleteAnnotation);
    }

    public Document getDocument() {
        return document;
    }


    public void setDocument(Document newDocument) {
        // clean up any previous documents
        if (document != null) {
            document.dispose();
            document = null;
        }
        document = newDocument;

        // clean up old document model and create a new one
        if (documentViewModel != null) {
            documentViewModel.dispose();
            documentViewModel = null;
        }
        documentViewModel = createDocumentViewMode(document, documentViewScrollPane);

        // setup view type
        setViewType();

        // remove re-size listener.
        documentViewScrollPane.addComponentListener(this);
        documentViewScrollPane.validate();
    }

    /**
     * Initialize a DocumentViewModel implementation.  Can be over ridden to provide custom DocumentViewModel
     * implementation.
     *
     * @param document               document that will be opened
     * @param documentViewScrollPane parent scrollPane of view.
     * @return DocumentViewModel for this view.
     */
    protected DocumentViewModel createDocumentViewMode(Document document, JScrollPane documentViewScrollPane) {
        return new DocumentViewModelImpl(document, documentViewScrollPane);
    }

    // we should be resetting some view settings, mainly zoom, rotation, tool and current page
    // Also, null document but do not dispose, this is the responsibility of Controller, we might
    // want to inject another document to view.
    public void closeDocument() {

        // remove re-size listener.
        documentViewScrollPane.removeComponentListener(this);

        // dispose the view
        if (documentView != null) {
            documentViewScrollPane.remove((JComponent) documentView);
            documentView.dispose();
            documentView = null;
        }

        // close current document
        if (documentViewModel != null) {
            documentViewModel.dispose();
            documentViewModel = null;
        }


//        setFitMode(PAGE_FIT_NONE);
        setCurrentPageIndex(0);
        setZoom(1);
        setRotation(0);
//        setToolMode(DocumentViewModelImpl.DISPLAY_TOOL_NONE);
        setViewCursor(DocumentViewControllerImpl.CURSOR_DEFAULT);

    }

    public Adjustable getHorizontalScrollBar() {
        return documentViewScrollPane.getHorizontalScrollBar();
    }

    public Adjustable getVerticalScrollBar() {
        return documentViewScrollPane.getVerticalScrollBar();
    }

    public JViewport getViewPort() {
        return documentViewScrollPane.getViewport();
    }

    /**
     * Set an annotation callback.
     *
     * @param annotationCallback annotation callback associated with this document
     *                           view.
     */
    public void setAnnotationCallback(AnnotationCallback annotationCallback) {
        this.annotationCallback = annotationCallback;
    }

    public void setSecurityCallback(SecurityCallback securityCallback) {
        this.securityCallback = securityCallback;
    }

    public void clearSelectedAnnotations() {
        if (documentViewModel.getCurrentAnnotation() != null) {
            documentViewModel.getCurrentAnnotation().setSelected(false);
            // fire change event
            firePropertyChange(PropertyConstants.ANNOTATION_DESELECTED,
                    documentViewModel.getCurrentAnnotation(),
                    null);
            documentViewModel.setCurrentAnnotation(null);
        }
    }

    public void assignSelectedAnnotation(AnnotationComponent annotationComponent) {
        firePropertyChange(PropertyConstants.ANNOTATION_SELECTED,
                documentViewModel.getCurrentAnnotation(),
                annotationComponent);
        documentViewModel.setCurrentAnnotation(annotationComponent);
    }

    /**
     * Clear selected text in all pages that make up the current document
     */
    public void clearSelectedText() {
        ArrayList<AbstractPageViewComponent> selectedPages =
                documentViewModel.getSelectedPageText();
        documentViewModel.setSelectAll(false);
        if (selectedPages != null &&
                selectedPages.size() > 0) {
            for (AbstractPageViewComponent pageComp : selectedPages) {
                if (pageComp != null && pageComp instanceof PageViewComponentImpl) {
                    pageComp.clearSelectedText();
                }
            }
            selectedPages.clear();
            documentView.repaint();
        }
        // fire property change
        firePropertyChange(PropertyConstants.TEXT_DESELECTED,
                null,
                null);

    }

    /**
     * Clear highlighted text in all pages that make up the current document
     */
    public void clearHighlightedText() {
        DocumentSearchController searchController =
                viewerController.getDocumentSearchController();
        searchController.clearAllSearchHighlight();
        documentView.repaint();
    }

    /**
     * Sets the selectAll status flag as true.  Text selection requires that
     * a pages content has been parsed and can be quite expensive for long
     * documents. The page component will pick up on this plag and paint the
     * selected state.  If the content is copied to the clipboard we go
     * thought he motion of parsing every page.
     */
    public void selectAllText() {
        documentViewModel.setSelectAll(true);
        documentView.repaint();
        firePropertyChange(PropertyConstants.TEXT_SELECT_ALL, null, null);
    }

    public String getSelectedText() {
        StringBuilder selectedText = new StringBuilder();
        try {
            // regular page selected by user mouse, keyboard or api
            if (!documentViewModel.isSelectAll()) {
                ArrayList<AbstractPageViewComponent> selectedPages =
                        documentViewModel.getSelectedPageText();
                if (selectedPages != null &&
                        selectedPages.size() > 0) {
                    for (AbstractPageViewComponent pageComp : selectedPages) {
                        if (pageComp != null) {
                            int pageIndex = pageComp.getPageIndex();
                            selectedText.append(document.getPageText(pageIndex).getSelected());
                        }
                    }
                }
            }
            // select all text
            else {
                Document document = documentViewModel.getDocument();
                // iterate over each page in the document
                for (int i = 0; i < document.getNumberOfPages(); i++) {
                    selectedText.append(viewerController.getDocument().getPageText(i));
                }
            }

        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Page text extraction thread interrupted.", e);
        }
        return selectedText.toString();
    }

    /**
     * Gets the annotation callback.
     *
     * @return annotation callback associated with this document.
     */
    public AnnotationCallback getAnnotationCallback() {
        return annotationCallback;
    }

    /**
     * Gets the security callback.
     *
     * @return security callback associated with this document.
     */
    public SecurityCallback getSecurityCallback() {
        return securityCallback;
    }

    public DocumentView getDocumentView() {
        return documentView;
    }

    public synchronized void setViewKeyListener(KeyListener l) {
        if (documentView != null)
            ((JComponent) documentView).addKeyListener(l);
    }

    public void setDestinationTarget(Destination destination) {

        if (documentView == null || documentViewModel == null) {
            return;
        }

        // check for a named destination def, and if so do the lookup.
        NamedDestinations namedDestinations = document.getCatalog().getDestinations();
        if (namedDestinations != null) {
            Destination tmp = namedDestinations.getDestination(destination.getNamedDestination());
            if (tmp != null) {
                destination = tmp;
            }
        }

        if (destination == null || destination.getPageReference() == null) {
            return;
        }

        // get the page number associated with the destination
        int pageNumber = getPageTree().getPageNumber(destination.getPageReference());
        if (pageNumber < 0) {
            return;
        }

        // ready our view port for manipulation
        JViewport documentViewport = (documentViewScrollPane != null) ?
                documentViewScrollPane.getViewport() : null;

        if (documentViewport != null) {

            // get location of page in document view
            Rectangle pageBounds = documentViewModel.getPageBounds(pageNumber);

            // Only apply destination if rotation is 0
            // todo: implement rotation calculation for destination offset
            if (documentViewModel.getViewRotation() == 0 && pageBounds != null) {

                setCurrentPageIndex(pageNumber);

                // apply zoom, from destination
                if (destination.getZoom() != null &&
                        destination.getZoom() > 0.0f) {
                    setZoomCentered(destination.getZoom(), null, false);
                }
                Point newViewPosition = new Point(pageBounds.getLocation());
                float zoom = getZoom();

                // Process top destination coordinate
                Rectangle viewportBounds = ((JComponent) documentView).getBounds();
                Rectangle viewportRect = documentViewport.getViewRect();
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("viewPort bounds " + viewportBounds);
                    logger.finer("viewPort rect " + viewportRect);
                    logger.finer("page bounds " + pageBounds);
                    logger.finer("page " + pageNumber);
                    logger.finer("top/left " + destination.getTop() + " " + destination.getLeft());
                }
                if (destination.getTop() != null && destination.getTop() != 0) {
                    // calculate potential new y value
                    newViewPosition.y = pageBounds.y + pageBounds.height - (int) (destination.getTop() * zoom);
                }
                if ((newViewPosition.y + viewportRect.height) > viewportBounds.height) {
                    newViewPosition.y = viewportBounds.height - viewportRect.height;
                }

                // Process left destination coordinate
                if (destination.getLeft() != null && destination.getLeft() != 0) {
                    // calculate potential new y value
                    newViewPosition.x = pageBounds.x + (int) (destination.getLeft() * zoom);
                }
                if ((newViewPosition.x + viewportRect.width) > viewportBounds.width) {
                    newViewPosition.x = viewportBounds.width - viewportRect.width;
                }

                // make sure documentViewport is not negative
                if (newViewPosition.x < 0)
                    newViewPosition.x = 0;
                if (newViewPosition.y < 0)
                    newViewPosition.y = 0;

                // finally apply the documentViewport position
                documentViewport.setViewPosition(newViewPosition);
                int oldPageIndex = documentViewModel.getViewCurrentPageIndex();
                documentViewModel.setViewCurrentPageIndex(pageNumber);
                firePropertyChange(PropertyConstants.DOCUMENT_CURRENT_PAGE,
                        oldPageIndex, pageNumber);
            }
            // Otherwise go to the indented page number with out applying
            // destination coordinates.
            else {
                setCurrentPageIndex(pageNumber);
            }

            viewerController.updateDocumentView();
        }
    }

    public void dispose() {
        if (documentView != null) {
            documentView.dispose();
            documentView = null;
        }
        if (documentViewModel != null) {
            documentViewModel.dispose();
            documentViewModel = null;
        }
    }

    /**
     * The controller will own the scrollpane and will insert different views
     * into it.
     */
    public Container getViewContainer() {
        return documentViewScrollPane;
    }

    public Controller getParentController() {
        return viewerController;
    }


    public int getViewMode() {
        return viewType;
    }

    /**
     * View Builder for known doc view types
     *
     * @param documentViewType view type,
     */
    public void setViewType(final int documentViewType) {
        oldViewType = viewType;
        viewType = documentViewType;
        // build the new view;
        if (documentView != null) {
            documentView.uninstallCurrentTool();
        }
        setViewType();
    }

    /**
     * Revert to the previously set view type.
     */
    public void revertViewType() {
        viewType = oldViewType;
        setViewType(viewType);
    }

    /**
     * Sets the view type, one column, two column, single page etc.
     */
    protected void setViewType() {

        // check if there is current view, if so dispose it
        if (documentView != null) {
            documentViewScrollPane.remove((JComponent) documentView);
            documentViewScrollPane.validate();
            documentView.dispose();
        }

        if (documentViewModel == null) {
            return;
        }

        // create the desired view with the current viewModel.
        createDocumentView(viewType);

        // as it may have been inactive
        // notify the view of the tool change
        documentView.setToolMode(documentViewModel.getViewToolMode());

        // add the new view the scroll pane
        documentViewScrollPane.setViewportView((Component) documentView);
        documentViewScrollPane.validate();

        // re-apply the fit mode
        viewerController.setPageFitMode(viewportFitMode, true);

        // set current page
        setCurrentPageIndex(documentViewModel.getViewCurrentPageIndex());
    }

    /**
     * Creates the specified view type used by the setVieType() call.  Can
     * be over ridden to create new or custom views.
     *
     * @param viewType view type constant
     */
    protected void createDocumentView(int viewType) {
        if (viewType == ONE_COLUMN_VIEW) {
            documentView =
                    new OneColumnPageView(this, documentViewScrollPane, documentViewModel);
        } else if (viewType == ONE_PAGE_VIEW) {
            documentView =
                    new OnePageView(this, documentViewScrollPane, documentViewModel);
        } else if (viewType == TWO_COLUMN_LEFT_VIEW) {
            documentView =
                    new TwoColumnPageView(this, documentViewScrollPane,
                            documentViewModel,
                            DocumentView.LEFT_VIEW);
        } else if (viewType == TWO_PAGE_LEFT_VIEW) {
            documentView =
                    new TwoPageView(this, documentViewScrollPane,
                            documentViewModel,
                            DocumentView.LEFT_VIEW);
        } else if (viewType == TWO_COLUMN_RIGHT_VIEW) {
            documentView =
                    new TwoColumnPageView(this, documentViewScrollPane,
                            documentViewModel,
                            DocumentView.RIGHT_VIEW);
        } else if (viewType == TWO_PAGE_RIGHT_VIEW) {
            documentView =
                    new TwoPageView(this, documentViewScrollPane,
                            documentViewModel,
                            DocumentView.RIGHT_VIEW);
        } else if (viewType == USE_ATTACHMENTS_VIEW) {
            documentView =
                    new CollectionDocumentView(this, documentViewScrollPane,
                            documentViewModel);
        } else {
            documentView =
                    new OneColumnPageView(this, documentViewScrollPane, documentViewModel);
        }

        ((JComponent) documentView).addPropertyChangeListener(this);

    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (documentView != null) {
            String prop = evt.getPropertyName();
            Object newValue = evt.getNewValue();
            Object oldValue = evt.getOldValue();
            // propagate the even to each page.
            if (PropertyConstants.DOCUMENT_VIEW_REFRESH_CHANGE.equals(prop) ||
                    PropertyConstants.DOCUMENT_VIEW_DEMO_MODE_CHANGE.equals(prop) ||
                    PropertyConstants.DOCUMENT_VIEW_ZOOM_CHANGE.equals(prop) ||
                    PropertyConstants.DOCUMENT_VIEW_ROTATION_CHANGE.equals(prop)) {
                List<AbstractPageViewComponent> pageComponents = documentViewModel.getPageComponents();
                for (AbstractPageViewComponent pageViewComponent : pageComponents) {
                    // pass in zoom, rotation etc, or get form model....
                    pageViewComponent.updateView(prop, oldValue, newValue);
                }
            }
        }
    }

    public boolean setFitMode(final int fitMode) {

        if (documentViewModel == null || viewType ==
                DocumentViewControllerImpl.USE_ATTACHMENTS_VIEW) {
            return false;
        }

        boolean changed = fitMode != viewportFitMode;
        viewportFitMode = fitMode;

        if (document != null) {

            // update fit
            float newZoom = documentViewModel.getViewZoom();
            if (viewportFitMode == PAGE_FIT_ACTUAL_SIZE) {
                newZoom = 1.0f;
            } else if (viewportFitMode == PAGE_FIT_WINDOW_HEIGHT) {
                if (documentView != null && documentViewScrollPane != null) {
                    float viewportHeight = documentViewScrollPane.getViewport().getViewRect().height;
                    float pageViewHeight = documentView.getDocumentSize().height;

                    // pageViewHeight insert padding on each side.
                    pageViewHeight += AbstractDocumentView.layoutInserts * 2;

                    if (viewportHeight > 0) {
                        newZoom = (viewportHeight / pageViewHeight);
                    } else {
                        newZoom = 1.0f;
                    }
                }
            } else if (viewportFitMode == PAGE_FIT_WINDOW_WIDTH) {
                if (documentView != null && documentViewScrollPane != null) {
                    float viewportWidth = documentViewScrollPane.getViewport().getViewRect().width;
                    float pageViewWidth = documentView.getDocumentSize().width;
                    // test if the scroll bar is not present, if so then we
                    // should consider that the scroll bar will be visible after the
                    // fit width is applied.
                    if (!documentViewScrollPane.getVerticalScrollBar().isVisible()) {
                        viewportWidth -= documentViewScrollPane.getVerticalScrollBar().getWidth();
                    }
                    // add insert padding on each side.
                    pageViewWidth += AbstractDocumentView.layoutInserts * 2;

                    if (viewportWidth > 0) {
                        newZoom = (viewportWidth / pageViewWidth);
                    } else {
                        newZoom = 1.0f;
                    }
                }
            }

            // If we're scrolled all the way to the top, center to top of document when zoom,
            //  otherwise the view will zoom into the general center of the page
            if (getVerticalScrollBar().getValue() == 0) {
                setZoomCentered(newZoom, new Point(0, 0), true);
            } else {
                setZoomCentered(newZoom, null, true);
            }
        }

        return changed;
    }

    public int getFitMode() {
        return viewportFitMode;
    }

    public void setDocumentViewType(final int documentView, final int fitMode) {
        setViewType(documentView);
        setFitMode(fitMode);
    }

    public boolean setCurrentPageIndex(int pageIndex) {

        if (documentViewModel == null) {
            return false;
        }

        boolean changed;
        // make sure that new index is a valid choice.
        if (pageIndex < 0) {
            pageIndex = 0;
        } else if (pageIndex > document.getNumberOfPages() - 1) {
            pageIndex = document.getNumberOfPages() - 1;
        }
        int oldPageIndex = documentViewModel.getViewCurrentPageIndex();
        changed = documentViewModel.setViewCurrentPageIndex(pageIndex);

        if (documentView != null) {
            documentView.updateDocumentView();
        }

        // get location of page in view port
        Rectangle preferedPageOffset = documentViewModel.getPageBounds(getCurrentPageIndex());
        if (preferedPageOffset != null) {
            // scroll the view port to the correct location
            Rectangle currentViewSize = ((JComponent) documentView).getBounds();

            // check to see of the preferedPageOffset will actually be possible.  If the
            // pages is smaller then the view port we need to correct x,y coordinates.
            if (preferedPageOffset.x + preferedPageOffset.width >
                    currentViewSize.width) {
                preferedPageOffset.x = currentViewSize.width - preferedPageOffset.width;
            }

            if (preferedPageOffset.y + preferedPageOffset.height >
                    currentViewSize.height) {
                preferedPageOffset.y = currentViewSize.height - preferedPageOffset.height;
            }

            documentViewScrollPane.getViewport().setViewPosition(preferedPageOffset.getLocation());
            documentViewScrollPane.revalidate();
        }
        firePropertyChange(PropertyConstants.DOCUMENT_CURRENT_PAGE,
                oldPageIndex, pageIndex);

        return changed;
    }

    public int setCurrentPageNext() {
        int increment = 0;
        if (documentViewModel != null) {
            increment = documentView.getNextPageIncrement();
            int current = documentViewModel.getViewCurrentPageIndex();
            if ((current + increment) < document.getNumberOfPages()) {
                documentViewModel.setViewCurrentPageIndex(current + increment);
            } else {
                documentViewModel.setViewCurrentPageIndex(document.getNumberOfPages() - 1);
            }
        }
        return increment;
    }

    public int setCurrentPagePrevious() {
        int decrement = 0;
        if (documentViewModel != null) {
            decrement = documentView.getPreviousPageIncrement();
            int current = documentViewModel.getViewCurrentPageIndex();
            if ((current - decrement) >= 0) {
                documentViewModel.setViewCurrentPageIndex(current - decrement);
            } else {
                documentViewModel.setViewCurrentPageIndex(0);
            }
        }
        return decrement;
    }

    public int getCurrentPageIndex() {
        if (documentViewModel == null) {
            return -1;
        }
        return documentViewModel.getViewCurrentPageIndex();
    }

    public int getCurrentPageDisplayValue() {
        if (documentViewModel == null) {
            return -1;
        }
        return documentViewModel.getViewCurrentPageIndex() + 1;
    }

    public float[] getZoomLevels() {
        return zoomLevels;
    }

    public void setZoomLevels(float[] zoomLevels) {
        this.zoomLevels = zoomLevels;
    }

    /**
     * Sets the zoom factor of the page visualization. A zoom factor of 1.0f
     * is equal to 100% or actual size.  A zoom factor of 0.5f is equal to 50%
     * of the original size.
     *
     * @param viewZoom zoom factor
     * @return if zoom actually changed
     */
    public boolean setZoom(float viewZoom) {
        return setZoomCentered(viewZoom, null, false);
    }

    public boolean setZoomIn() {
        return setZoomIn(null);
    }

    public boolean setZoomOut() {
        return setZoomOut(null);
    }

    public float getZoom() {
        if (documentViewModel != null) {
            return documentViewModel.getViewZoom();
        } else {
            return 0;
        }
    }

    /**
     * Returns the zoom factor of the page visualization.  A zoom factor of 1.0f
     * is equal to 100% or actual size.  A zoom factor of 0.5f is equal to 50%
     * of the original size.
     *
     * @return zoom factor
     */
    public float getRotation() {
        if (documentViewModel == null) {
            return -1;
        }
        return documentViewModel.getViewRotation();
    }

    public float setRotateRight() {
        if (documentViewModel == null) {
            return -1;
        }
        float viewRotation = documentViewModel.getViewRotation();
        viewRotation -= ROTATION_FACTOR;
        if (viewRotation < 0)
            viewRotation += 360;
        setRotation(viewRotation);
        return viewRotation;
    }

    public float setRotateLeft() {
        if (documentViewModel == null) {
            return -1;
        }
        float viewRotation = documentViewModel.getViewRotation();
        viewRotation += ROTATION_FACTOR;
        viewRotation %= 360;
        setRotation(viewRotation);
        return viewRotation;
    }

    public boolean setRotation(float viewRotation) {
        if (documentViewModel == null) {
            return false;
        }
        float oldRotation = documentViewModel.getViewRotation();
        boolean changed = documentViewModel.setViewRotation(viewRotation);
        if (changed) {
            // send out the property change event.
            ((JComponent) documentView).invalidate();
            ((JComponent) documentView).firePropertyChange(PropertyConstants.DOCUMENT_VIEW_ROTATION_CHANGE, oldRotation, viewRotation);
            ((JComponent) documentView).revalidate();
        }
        return changed;

    }

    public boolean setToolMode(final int viewToolMode) {

        if (documentViewModel != null) {
            boolean changed = documentViewModel.setViewToolMode(viewToolMode);
            // update the view and page components so the correct tool handler
            // can ge assigned.
            if (changed) {
                // notify the view of the tool change
                if (documentView != null)
                    documentView.setToolMode(viewToolMode);

                // notify the page components of the tool change.
                List<AbstractPageViewComponent> pageComponents =
                        documentViewModel.getPageComponents();
                for (AbstractPageViewComponent page : pageComponents) {
                    ((PageViewComponentImpl) page).setToolMode(viewToolMode);
                }
            }
            return changed;
        } else {
            return false;
        }
    }

    public boolean isToolModeSelected(final int viewToolMode) {
        return getToolMode() == viewToolMode;
    }

    public int getToolMode() {
        if (documentViewModel == null) {
            return DocumentViewModelImpl.DISPLAY_TOOL_NONE;
        }
        return documentViewModel.getViewToolMode();
    }

    public void setViewCursor(final int cursorType) {
        this.cursorType = cursorType;
        Cursor cursor = getViewCursor(cursorType);
        if (documentViewScrollPane != null) {
            if (documentViewScrollPane.getViewport() != null)
                documentViewScrollPane.getViewport().setCursor(cursor);
        }
    }

    public int getViewCursor() {
        return cursorType;
    }

    public Cursor getViewCursor(final int currsorType) {
        Cursor c;
        String imageName;

        if (currsorType == CURSOR_DEFAULT) {
            return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        } else if (currsorType == CURSOR_WAIT) {
            return Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        } else if (currsorType == CURSOR_SELECT) {
            return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        } else if (currsorType == CURSOR_HAND_OPEN) {
            imageName = "hand_open.gif";
        } else if (currsorType == CURSOR_HAND_CLOSE) {
            imageName = "hand_closed.gif";
        } else if (currsorType == CURSOR_ZOOM_IN) {
            imageName = "zoom_in.gif";
        } else if (currsorType == CURSOR_ZOOM_OUT) {
            imageName = "zoom_out.gif";
        } else if (currsorType == CURSOR_MAGNIFY) {
            imageName = "zoom.gif";
        } else if (currsorType == CURSOR_HAND_ANNOTATION) {
            return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        } else if (currsorType == CURSOR_TEXT_SELECTION) {
            return Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
        } else if (currsorType == CURSOR_CROSSHAIR) {
            return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        } else {
            return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        }

        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension bestSize = tk.getBestCursorSize(24, 24);
        if (bestSize.width != 0) {

            Point cursorHotSpot = new Point(12, 12);
            try {
                ImageIcon cursorImage = new ImageIcon(Images.get(imageName));
                c = tk.createCustomCursor(cursorImage.getImage(), cursorHotSpot, imageName);
            } catch (RuntimeException ex) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE,
                            "Trying to load image: " + imageName, ex);
                }
                throw ex;
            }
        } else {
            c = Cursor.getDefaultCursor();
            logger.warning("System does not support custom cursors");
        }
        return c;
    }

    public void requestViewFocusInWindow() {
        if (documentViewScrollPane != null)
            documentViewScrollPane.requestFocus();
    }

    /**
     * Increases the current page visualization zoom factor by 20%.
     *
     * @param p Recenter the scrollPane here
     */
    public boolean setZoomIn(Point p) {
        float zoom = getZoom() * ZOOM_FACTOR;
        return setZoomCentered(zoom, p, false);
    }

    /**
     * Decreases the current page visualization zoom factor by 20%.
     *
     * @param p Recenter the scrollPane here
     */
    public boolean setZoomOut(Point p) {
        float zoom = getZoom() / ZOOM_FACTOR;
        return setZoomCentered(zoom, p, false);
    }

    /**
     * Utility function for centering the view Port around the given point.
     *
     * @param centeringPoint which the view is to be centered on.
     */
    private void zoomCenter(Point centeringPoint, float previousZoom, float zoom) {
        // make sure the point is not null
        if (centeringPoint == null) {
            centeringPoint = getCenteringPoint();
        }

        if (centeringPoint == null || documentViewScrollPane == null)
            return;

        // get view port information
        int viewPortWidth = documentViewScrollPane.getViewport().getWidth();
        int viewPortHeight = documentViewScrollPane.getViewport().getHeight();

        int scrollPaneX = documentViewScrollPane.getViewport().getViewPosition().x;
        int scrollPaneY = documentViewScrollPane.getViewport().getViewPosition().y;

        Dimension pageViewSize = ((JComponent) documentView).getPreferredSize();
        int pageViewWidth = pageViewSize.width;
        int pageViewHeight = pageViewSize.height;

        // calculate center coordinates of view port x,y
        centeringPoint.setLocation(centeringPoint.x - (viewPortWidth / 2),
                centeringPoint.y - (viewPortHeight / 2));

        // compensate centering point to make sure that preferred site is
        // respected when moving the view port x,y.

        // Special case when page height or width is smaller then the viewPort
        // size.  Respect the zoom but don't try and center on the click
        if (pageViewWidth < viewPortWidth || pageViewHeight < viewPortHeight) {
            if (centeringPoint.x >= pageViewWidth - viewPortWidth ||
                    centeringPoint.x < 0) {
                centeringPoint.x = scrollPaneX;
            }

            if (centeringPoint.y >= pageViewHeight - viewPortHeight ||
                    centeringPoint.y < 0) {
                centeringPoint.y = scrollPaneY;
            }
        }
        // Special case 2: compensate for click where it is not possible to center
        // the page with out shifting the view port paste the pages width
        else {
            // adjust horizontal
            if (centeringPoint.x + viewPortWidth > pageViewWidth) {
                centeringPoint.x = (pageViewWidth - viewPortWidth);
            } else if (centeringPoint.x < 0) {
                centeringPoint.x = 0;
            }

            // adjust vertical
            if (centeringPoint.y + viewPortHeight > pageViewHeight) {
                centeringPoint.y = (pageViewHeight - viewPortHeight);
            } else if (centeringPoint.y < 0) {
                centeringPoint.y = 0;
            }
        }
        // not sure why, but have to set twice for reliable results
        documentViewScrollPane.getViewport().setViewPosition(centeringPoint);
        documentViewScrollPane.getViewport().setViewPosition(centeringPoint);
    }


    /**
     * Zoom to a new zoom level, centered at a specific point.
     *
     * @param zoom                  zoom level which should be in the range of zoomLevels array
     * @param becauseOfValidFitMode true will update ui elements with zoom state.
     * @param centeringPoint        point to center on.
     * @return true if the zoom level changed, false otherwise.
     */
    public boolean setZoomCentered(float zoom, Point centeringPoint, boolean becauseOfValidFitMode) {
        if (documentViewModel == null) {
            return false;
        }
        // make sure the zoom falls in between the zoom range
        zoom = calculateZoom(zoom);

        // set a default centering point if null
        if (centeringPoint == null) {
            centeringPoint = getCenteringPoint();
        }
        // grab previous zoom so that zoom factor can be calculated
        float previousZoom = getZoom();

        // apply zoom
        float oldZoom = documentViewModel.getViewZoom();
        boolean changed = documentViewModel.setViewZoom(zoom);

        if (changed) {
            ((JComponent) documentView).invalidate();
            // send out the property change event.
            ((JComponent) documentView).firePropertyChange(PropertyConstants.DOCUMENT_VIEW_ZOOM_CHANGE, oldZoom, zoom);
            // get the view port validate the viewport and shift the components
            ((JComponent) documentView).revalidate();
        }
        // center zoom calculation, find current center and pass
        // it along to zoomCenter function.
        if (changed && centeringPoint != null) {
            centeringPoint.setLocation(
                    (centeringPoint.x / previousZoom) * zoom,
                    (centeringPoint.y / previousZoom) * zoom);
        }
        // still center on click
        zoomCenter(centeringPoint, previousZoom, zoom);

        // update the UI controls
        if (viewerController != null) {
            viewerController.doCommonZoomUIUpdates(becauseOfValidFitMode);
        }

        return changed;
    }

    private float calculateZoom(float zoom) {
        if (zoomLevels != null) {
            if (zoom < zoomLevels[0])
                zoom = zoomLevels[0];
            else if (zoom > zoomLevels[zoomLevels.length - 1])
                zoom = zoomLevels[zoomLevels.length - 1];
        }
        return zoom;
    }

    /**
     * Zoom to a new zoom level, the viewPort position is set by the addition
     * of the zoomPointDelta to the page bounds as defined by the view.
     *
     * @param zoom                  zoom level which should be in the range of zoomLevels array
     * @param becauseOfValidFitMode true will update ui elements with zoom state.
     * @param zoomPointDelta        point to center on.
     * @param pageIndex             page to zoom in on.
     * @return true if the zoom level changed, false otherwise.
     */
    public boolean setZoomToViewPort(float zoom, Point zoomPointDelta, int pageIndex,
                                     boolean becauseOfValidFitMode) {
        if (documentViewModel == null) {
            return false;
        }
        // make sure the zoom falls in between the zoom range
        zoom = calculateZoom(zoom);

        // set a default centering point if null
        if (zoomPointDelta == null) {
            zoomPointDelta = new Point();
        }
        // grab previous zoom so that zoom factor can be calculated
        float previousZoom = getZoom();
        // apply zoom
        boolean changed = documentViewModel.setViewZoom(zoom);
        if (changed) {
            ((JComponent) documentView).firePropertyChange(PropertyConstants.DOCUMENT_VIEW_ZOOM_CHANGE, previousZoom, zoom);
            documentViewScrollPane.invalidate();
            documentViewScrollPane.validate();
            documentViewScrollPane.getViewport().getView().invalidate();
            documentViewScrollPane.getViewport().getView().validate();
        }

        // center zoom calculation, find current center and pass
        // it along to zoomCenter function.
        if (changed) {
            Rectangle bounds = documentViewModel.getPageBounds(pageIndex);
            zoomPointDelta.setLocation(
                    (zoomPointDelta.x / previousZoom) * zoom,
                    (zoomPointDelta.y / previousZoom) * zoom);
            zoomPointDelta.setLocation(bounds.x + zoomPointDelta.x,
                    bounds.y + zoomPointDelta.y);
            // view hasn't been update yet so we double set the position to make it take effect.
            getViewPort().setViewPosition(zoomPointDelta);
            getViewPort().setViewPosition(zoomPointDelta);
        }

        // update the UI controls
        if (viewerController != null) {
            viewerController.doCommonZoomUIUpdates(becauseOfValidFitMode);
        }

        return changed;
    }


    /**
     * Utility method for finding the center point of the viewport
     *
     * @return current center of view port.
     */
    private Point getCenteringPoint() {
        Point centeringPoint = null;
        if (documentViewScrollPane != null) {
            int x = documentViewScrollPane.getViewport().getViewPosition().x +
                    (documentViewScrollPane.getViewport().getWidth() / 2);
            int y = documentViewScrollPane.getViewport().getViewPosition().y +
                    (documentViewScrollPane.getViewport().getHeight() / 2);
            centeringPoint = new Point(x, y);
        }
        return centeringPoint;
    }

    /**
     * Gives access to the currently openned Document's Catalog's PageTree
     *
     * @return PageTree
     */
    private PageTree getPageTree() {
        if (document == null)
            return null;
        return document.getPageTree();
    }

    public DocumentViewModel getDocumentViewModel() {
        return documentViewModel;
    }

    //
    // ComponentListener interface
    //

    /**
     * SwingController takes AWT/Swing events, and maps them to its own events
     * related to PDF Document manipulation
     */
    public void componentHidden(ComponentEvent e) {
    }

    /**
     * SwingController takes AWT/Swing events, and maps them to its own events
     * related to PDF Document manipulation
     */
    public void componentMoved(ComponentEvent e) {
    }

    /**
     * SwingController takes AWT/Swing events, and maps them to its own events
     * related to PDF Document manipulation
     */
    public void componentResized(ComponentEvent e) {
        Object src = e.getSource();
        if (src == null)
            return;
        // we need to update the document view, if fit width of fit height is
        // selected we need to adjust the zoom level appropriately.
        if (src == documentViewScrollPane) {
            setFitMode(getFitMode());
        }
    }

    /**
     * SwingController takes AWT/Swing events, and maps them to its own events
     * related to PDF Document manipulation
     */
    public void componentShown(ComponentEvent e) {

    }

    public void firePropertyChange(String event, int oldValue, int newValue) {
        changes.firePropertyChange(event, oldValue, newValue);
    }

    /**
     * Fires property change events for Page view UI changes such as:
     * <li>focus gained/lost</li>
     * <li>annotation state change such as move or resize</li>
     * <li>new annotation crreated, currently only for new link annotations</li>
     * <li></li>
     *
     * @param event    property being changes
     * @param oldValue old value, null if no old value
     * @param newValue new annotation value.
     */
    public void firePropertyChange(String event, Object oldValue,
                                   Object newValue) {
        changes.firePropertyChange(event, oldValue, newValue);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    public void deleteCurrentAnnotation() {
        AbstractAnnotationComponent annotationComponent = (AbstractAnnotationComponent)
                documentViewModel.getCurrentAnnotation();
        if (!(annotationComponent instanceof PopupAnnotationComponent)) {
            deleteAnnotation(annotationComponent);
        }
    }

    public void deleteAnnotation(AnnotationComponent annotationComponent) {
        if (documentViewModel != null && annotationComponent != null) {

            // parent component
            PageViewComponent pageComponent =
                    annotationComponent.getPageViewComponent();

            if (annotationCallback != null) {
                annotationCallback.removeAnnotation(pageComponent, annotationComponent);
            }

            // fire event notification
            firePropertyChange(PropertyConstants.ANNOTATION_DELETED,
                    documentViewModel.getCurrentAnnotation(),
                    null);

            // clear previously selected annotation and fire event.
            assignSelectedAnnotation(null);

            // repaint the view.
            documentView.repaint();
        }
    }

    public void undo() {
        // repaint the view.
        documentView.repaint();
    }

    public void redo() {
        // repaint the view.
        documentView.repaint();
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }
}
