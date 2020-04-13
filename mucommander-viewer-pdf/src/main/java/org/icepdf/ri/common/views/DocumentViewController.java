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

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;


/**
 * <p>The DocumentViewControllerImpl is the controler in the MVC for multipage view
 * management.  This controller is used to manipulate the one column, one page,
 * two column and two page views.</p>
 * <p/>
 * <p>The Swing implementation of multiple view usesa the folowing MVC base
 * classes:
 * </P>
 *
 * @see org.icepdf.ri.common.views.AbstractDocumentView
 * @see org.icepdf.ri.common.views.AbstractDocumentViewModel
 * @see org.icepdf.ri.common.views.DocumentViewControllerImpl
 * @since 2.5
 */
public interface DocumentViewController {

    /**
     * Set the view to show the page at the specified zoom level.
     */
    public static final int PAGE_FIT_NONE = 1;

    /**
     * Set the view to show the page at actual size
     */
    public static final int PAGE_FIT_ACTUAL_SIZE = 2;

    /**
     * Set the view to show the page at actual size
     */
    public static final int PAGE_FIT_WINDOW_HEIGHT = 3;

    /**
     * Set the view to show the page at actual size
     */
    public static final int PAGE_FIT_WINDOW_WIDTH = 4;


    public static final int CURSOR_HAND_OPEN = 1;

    public static final int CURSOR_HAND_CLOSE = 2;

    public static final int CURSOR_ZOOM_IN = 3;

    public static final int CURSOR_ZOOM_OUT = 4;

    public static final int CURSOR_WAIT = 6;

    public static final int CURSOR_SELECT = 7;

    public static final int CURSOR_DEFAULT = 8;

    public static final int CURSOR_HAND_ANNOTATION = 9;

    public static final int CURSOR_TEXT_SELECTION = 10;

    public static final int CURSOR_CROSSHAIR = 11;

    public static final int CURSOR_MAGNIFY = 12;

    public void setDocument(Document document);

    public Document getDocument();

    public void closeDocument();

    public void dispose();

    public Container getViewContainer();

    public Controller getParentController();

    public void setViewType(final int documentView);

    public int getViewMode();

    public boolean setFitMode(final int fitMode);

    public int getFitMode();

    public void setDocumentViewType(final int documentView, final int fitMode);

    public boolean setCurrentPageIndex(int pageNumber);

    public int setCurrentPageNext();

    public int setCurrentPagePrevious();

    public void setDestinationTarget(Destination destination);

    public int getCurrentPageIndex();

    public int getCurrentPageDisplayValue();

    public void setZoomLevels(float[] zoomLevels);

    public float[] getZoomLevels();

    public boolean setZoom(float userZoom);

    public boolean setZoomIn();

    public boolean setZoomIn(Point point);

    boolean setZoomCentered(float zoom, Point centeringPoint, boolean becauseOfValidFitMode);

    boolean setZoomToViewPort(float zoom, Point viewPortPosition, int pageIndex, boolean becauseOfValidFitMode);

    public boolean setZoomOut();

    public boolean setZoomOut(Point point);

    public float getZoom();

    public boolean setRotation(float userRotation);

    public float getRotation();

    public float setRotateRight();

    public float setRotateLeft();

    public boolean setToolMode(final int viewToolMode);

    public int getToolMode();

    public boolean isToolModeSelected(final int viewToolMode);

    public void requestViewFocusInWindow();

    public void setViewCursor(final int cursorType);

    public Cursor getViewCursor(final int cursorType);

    public int getViewCursor();

    public void setViewKeyListener(KeyListener l);

    public Adjustable getHorizontalScrollBar();

    public Adjustable getVerticalScrollBar();

    public JViewport getViewPort();

    public void setAnnotationCallback(AnnotationCallback annotationCallback);

    public void setSecurityCallback(SecurityCallback securityCallback);

    public void deleteCurrentAnnotation();

    public void deleteAnnotation(AnnotationComponent annotationComponent);

    public void undo();

    public void redo();

    public AnnotationCallback getAnnotationCallback();

    public SecurityCallback getSecurityCallback();

    public DocumentViewModel getDocumentViewModel();

    public DocumentView getDocumentView();

    public void clearSelectedText();

    public void clearHighlightedText();

    public void clearSelectedAnnotations();

    public void assignSelectedAnnotation(AnnotationComponent annotationComponent);

    public void selectAllText();

    public String getSelectedText();

    public void firePropertyChange(String event, int oldValue, int newValue);

    public void firePropertyChange(String event, Object oldValue, Object newValue);
}
