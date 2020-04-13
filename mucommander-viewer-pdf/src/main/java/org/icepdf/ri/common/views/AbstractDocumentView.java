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

import org.icepdf.core.pobjects.Document;
import org.icepdf.core.util.ColorUtil;
import org.icepdf.core.util.Defs;
import org.icepdf.core.util.PropertyConstants;
import org.icepdf.ri.common.tools.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>The AbstractDocumentView class is implemented by the four predefined page views;
 * OneColumnPageView, OnePageView, TwoColumnPageView and TwoPageView. Most of
 * common work is implemented in this class which aid developers in defining their
 * own custom page views.<p>
 *
 * @since 2.5
 */
public abstract class AbstractDocumentView
        extends JComponent
        implements DocumentView, PropertyChangeListener, MouseListener {

    private static final Logger logger =
            Logger.getLogger(AbstractDocumentView.class.toString());

    // background colour
    public static Color BACKGROUND_COLOUR;

    static {
        // sets the shadow colour of the decorator.
        try {
            String color = Defs.sysProperty(
                    "org.icepdf.core.views.background.color", "#808080");
            int colorValue = ColorUtil.convertColor(color);
            BACKGROUND_COLOUR =
                    new Color(colorValue >= 0 ? colorValue :
                            Integer.parseInt("808080", 16));
        } catch (NumberFormatException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Error reading page shadow colour");
            }
        }
    }

    // general layout of page component spacing.
    public static int verticalSpace = 2;
    public static int horizontalSpace = 1;
    public static int layoutInserts = 0;

    protected DocumentViewController documentViewController;

    protected JScrollPane documentScrollpane;
    protected JPanel pagesPanel;
    protected boolean disposing;

    protected Document currentDocument;

    protected DocumentViewModel documentViewModel;

    // current page view tool.
    protected ToolHandler currentTool;

    // mouse wheel zoom, always on regardless of tool ctr-wheel mouse rotation
    // for zoom in and out.
    protected MouseWheelZoom mouseWheelZoom;

    /**
     * Creates a new instance of AbstractDocumentView.
     *
     * @param documentViewController controller for MVC
     * @param documentScrollpane     scrollpane used to view pages
     * @param documentViewModel      model to represent view
     */
    public AbstractDocumentView(DocumentViewController documentViewController,
                                JScrollPane documentScrollpane,
                                DocumentViewModel documentViewModel) {
        this.documentViewController = documentViewController;
        this.documentScrollpane = documentScrollpane;
        this.documentViewModel = documentViewModel;

        currentDocument = this.documentViewModel.getDocument();

        setFocusable(true);
        // add focus listener
        addFocusListener(this);

        // add mouse listener
        addMouseListener(this);

        // wheel listener
        mouseWheelZoom = new MouseWheelZoom(documentViewController, documentScrollpane);
        documentScrollpane.addMouseWheelListener(mouseWheelZoom);

        // listen for scroll bar manipulators
        documentViewController.getHorizontalScrollBar().addAdjustmentListener(this);
        documentViewController.getVerticalScrollBar().addAdjustmentListener(this);

        // add a focus management listener.
        KeyboardFocusManager focusManager =
                KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.addPropertyChangeListener(this);

    }

    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        Object newValue = evt.getNewValue();
        Object oldValue = evt.getOldValue();
        if ("focusOwner".equals(prop) &&
                newValue instanceof AnnotationComponent) {
            // the correct annotations for the properties pane
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Selected Annotation " + newValue);
            }
            DocumentViewController documentViewController =
                    getParentViewController();
            documentViewController.firePropertyChange(
                    PropertyConstants.ANNOTATION_FOCUS_GAINED,
                    evt.getOldValue(),
                    evt.getNewValue());

        } else if ("focusOwner".equals(prop) &&
                oldValue instanceof AnnotationComponent) {
            // the correct annotations for the properties pane
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Deselected Annotation " + oldValue);
            }
            DocumentViewController documentViewController =
                    getParentViewController();
            documentViewController.firePropertyChange(
                    PropertyConstants.ANNOTATION_FOCUS_LOST,
                    evt.getOldValue(),
                    evt.getNewValue());
        }
    }

    public DocumentViewController getParentViewController() {
        return documentViewController;
    }

    public DocumentViewModel getViewModel() {
        return documentViewModel;
    }

    public void invalidate() {
        super.invalidate();
        pagesPanel.invalidate();
    }

    public void dispose() {

        currentDocument = null;

        // clean up scroll listeners
        documentViewController.getHorizontalScrollBar().removeAdjustmentListener(this);
        documentViewController.getVerticalScrollBar().removeAdjustmentListener(this);

        // remove custom handlers
        if (currentTool != null) {
            removeMouseListener(currentTool);
            removeMouseMotionListener(currentTool);
        }

        // mouse/wheel listener
        documentScrollpane.removeMouseWheelListener(mouseWheelZoom);
        removeMouseListener(this);

        // focus management
        removeFocusListener(this);
        // add a focus management listener.
        KeyboardFocusManager focusManager =
                KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.removePropertyChangeListener(this);
    }

    /**
     * invalidates page components
     */
    public abstract void updateDocumentView();

    public ToolHandler uninstallCurrentTool() {
        if (currentTool != null) {
            currentTool.uninstallTool();
            removeMouseListener(currentTool);
            removeMouseMotionListener(currentTool);
            if (currentTool instanceof TextSelectionViewHandler) {
                documentScrollpane.removeMouseWheelListener((TextSelectionViewHandler) currentTool);
            }
        }
        return currentTool;
    }

    public void installCurrentTool(ToolHandler currentTool) {
        if (currentTool != null) {
            currentTool.installTool();
            addMouseListener(currentTool);
            addMouseMotionListener(currentTool);
            this.currentTool = currentTool;
        }
    }

    public ToolHandler getCurrentToolHandler() {
        return currentTool;
    }

    public void setToolMode(final int viewToolMode) {
        uninstallCurrentTool();
        // assign the correct tool handler
        switch (viewToolMode) {
            case DocumentViewModel.DISPLAY_TOOL_PAN:
                currentTool = new PanningHandler(documentViewController,
                        documentViewModel, this);
                break;
            case DocumentViewModel.DISPLAY_TOOL_ZOOM_IN:
                currentTool = new ZoomInViewHandler(documentViewController,
                        documentViewModel, this);
                break;
            case DocumentViewModel.DISPLAY_TOOL_ZOOM_DYNAMIC:
                currentTool = new DynamicZoomHandler(documentViewController,
                        documentScrollpane);
                break;
            case DocumentViewModel.DISPLAY_TOOL_TEXT_SELECTION:
                currentTool = new TextSelectionViewHandler(documentViewController,
                        documentViewModel, this);
                documentScrollpane.addMouseWheelListener((TextSelectionViewHandler) currentTool);
                break;
            case DocumentViewModel.DISPLAY_TOOL_SELECTION:
                currentTool = new AnnotationSelectionHandler(
                        documentViewController,
                        null,
                        documentViewModel);
                break;
            default:
                currentTool = null;
                break;
        }
        if (currentTool != null) {
            currentTool.installTool();
            addMouseListener(currentTool);
            addMouseMotionListener(currentTool);
        }
    }

    /**
     * Paints the selection box for this page view.
     *
     * @param g Java graphics context to paint to.
     */
    public void paintComponent(Graphics g) {
        if (currentTool != null) {
            currentTool.paintTool(g);
        }
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {

    }

    public void focusGained(FocusEvent e) {

    }

    public void focusLost(FocusEvent e) {

    }

    public void mouseClicked(MouseEvent e) {
        requestFocus();
    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    /**
     * Utility method for determining if the mouse event occurred over a
     * page in the page view.
     *
     * @param e mouse event in this coordinates space
     * @return component that mouse event is over or null if not over a page.
     */
    private AbstractPageViewComponent isOverPageComponent(MouseEvent e) {
        // mouse -> page  broadcast .
        Component comp = findComponentAt(e.getPoint());
        if (comp instanceof AbstractPageViewComponent) {
            return (AbstractPageViewComponent) comp;
        } else {
            return null;
        }
    }

    public JScrollPane getDocumentScrollpane() {
        return documentScrollpane;
    }
}
