package org.icepdf.ri.common.views;

import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.PageTree;
import org.icepdf.core.pobjects.annotations.Annotation;
import org.icepdf.core.pobjects.annotations.ChoiceWidgetAnnotation;
import org.icepdf.core.pobjects.annotations.FreeTextAnnotation;
import org.icepdf.core.pobjects.annotations.TextWidgetAnnotation;
import org.icepdf.core.pobjects.graphics.text.PageText;
import org.icepdf.core.search.DocumentSearchController;
import org.icepdf.core.util.GraphicsRenderingHints;
import org.icepdf.core.util.PropertyConstants;
import org.icepdf.ri.common.tools.*;
import org.icepdf.ri.common.views.annotations.AbstractAnnotationComponent;
import org.icepdf.ri.common.views.annotations.AnnotationComponentFactory;
import org.icepdf.ri.common.views.annotations.PopupAnnotationComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @since 6.2 heavily modified behaviour for cpu and memory enhancements.
 */
@SuppressWarnings("serial")
public class PageViewComponentImpl extends AbstractPageViewComponent implements FocusListener {

    private static final Logger logger =
            Logger.getLogger(PageViewComponentImpl.class.toString());

    // currently selected tool
    protected ToolHandler currentToolHandler;

    // we always keep around a page selection tool, it's only called from the parent view
    // component, this allows for multiple page selection.
    protected TextSelectionPageHandler textSelectionPageHandler;

    // annotations component for this pageViewComp.
    protected ArrayList<AbstractAnnotationComponent> annotationComponents;

    public PageViewComponentImpl(DocumentViewModel documentViewModel, PageTree pageTree, final int pageIndex,
                                 JScrollPane parentScrollPane, int width, int height) {
        super(documentViewModel, pageTree, pageIndex, parentScrollPane, width, height);
        setFocusable(true);
        addFocusListener(this);
        // text selection handler
        textSelectionPageHandler = new TextSelectionPageHandler(documentViewController, this, documentViewModel);
        // fully dynamic view, so we need to make sure we don't paint annotations to the buffer.
        paintAnnotations = false;
    }

    public void setDocumentViewCallback(DocumentView parentDocumentView) {
        super.setDocumentViewCallback(parentDocumentView);
        textSelectionPageHandler.setDocumentViewController(documentViewController);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create(0, 0, pageSize.width, pageSize.height);
        GraphicsRenderingHints grh = GraphicsRenderingHints.getDefault();
        g2d.setRenderingHints(grh.getRenderingHints(GraphicsRenderingHints.SCREEN));

        // paint the annotation components.
        paintAnnotationComponents(g2d);
        // paint selected and highlighted text.
        paintTextSelection(g2d);

        // paint annotation handler effect if any.
        if (currentToolHandler != null) {
            currentToolHandler.paintTool(g2d);
        }
        if (documentViewModel.getViewToolMode() == DocumentViewModel.DISPLAY_TOOL_TEXT_SELECTION) {
            textSelectionPageHandler.paintTool(g2d);
        }
        g2d.dispose();
    }

    public void dispose() {
        // remove annotation listeners.
        removeMouseMotionListener(currentToolHandler);
        removeMouseListener(currentToolHandler);
        // remove focus listener
        removeFocusListener(this);
        // dispose annotations components
        if (annotationComponents != null) {
            for (int i = 0, max = annotationComponents.size(); i < max; i++) {
                annotationComponents.get(i).dispose();
            }
        }
    }

    /**
     * Sets the tool mode for the current page component implementation.  When
     * a tool mode is assigned the respective tool handler is registered and
     * various event listeners are registered.
     *
     * @param viewToolMode view tool modes as defined in
     *                     DocumentViewMode.DISPLAY_TOOL_*
     */
    public void setToolMode(final int viewToolMode) {
        if (currentToolHandler != null) {
            currentToolHandler.uninstallTool();
            removeMouseListener(currentToolHandler);
            removeMouseMotionListener(currentToolHandler);
            currentToolHandler = null;
        }
        // assign the correct tool handler
        switch (viewToolMode) {
            case DocumentViewModel.DISPLAY_TOOL_ZOOM_IN:
                currentToolHandler = new ZoomInPageHandler(
                        documentViewController,
                        this,
                        documentViewModel);
                break;
            case DocumentViewModel.DISPLAY_TOOL_SELECTION:
                // no handler is needed for selection as it is handle by
                // each annotation.
                currentToolHandler = new AnnotationSelectionHandler(
                        documentViewController,
                        this,
                        documentViewModel);
                documentViewController.clearSelectedText();
                break;
            case DocumentViewModel.DISPLAY_TOOL_LINK_ANNOTATION:
                // handler is responsible for the initial creation of the annotation
                currentToolHandler = new LinkAnnotationHandler(
                        documentViewController,
                        this,
                        documentViewModel);
                documentViewController.clearSelectedText();
                break;
            case DocumentViewModel.DISPLAY_TOOL_HIGHLIGHT_ANNOTATION:
                // handler is responsible for the initial creation of the annotation
                currentToolHandler = new HighLightAnnotationHandler(
                        documentViewController,
                        this,
                        documentViewModel);
                ((HighLightAnnotationHandler) currentToolHandler).createTextMarkupAnnotation(null);
                documentViewController.clearSelectedText();
                break;
            case DocumentViewModel.DISPLAY_TOOL_STRIKEOUT_ANNOTATION:
                currentToolHandler = new StrikeOutAnnotationHandler(
                        documentViewController,
                        this,
                        documentViewModel);
                ((StrikeOutAnnotationHandler) currentToolHandler).createTextMarkupAnnotation(null);
                documentViewController.clearSelectedText();
                break;
            case DocumentViewModel.DISPLAY_TOOL_UNDERLINE_ANNOTATION:
                currentToolHandler = new UnderLineAnnotationHandler(
                        documentViewController,
                        this,
                        documentViewModel);
                ((UnderLineAnnotationHandler) currentToolHandler).createTextMarkupAnnotation(null);
                documentViewController.clearSelectedText();
                break;
            case DocumentViewModel.DISPLAY_TOOL_LINE_ANNOTATION:
                currentToolHandler = new LineAnnotationHandler(
                        documentViewController,
                        this,
                        documentViewModel);
                documentViewController.clearSelectedText();
                break;
            case DocumentViewModel.DISPLAY_TOOL_LINE_ARROW_ANNOTATION:
                currentToolHandler = new LineArrowAnnotationHandler(
                        documentViewController,
                        this,
                        documentViewModel);
                documentViewController.clearSelectedText();
                break;
            case DocumentViewModel.DISPLAY_TOOL_SQUARE_ANNOTATION:
                currentToolHandler = new SquareAnnotationHandler(
                        documentViewController,
                        this,
                        documentViewModel);
                documentViewController.clearSelectedText();
                break;
            case DocumentViewModel.DISPLAY_TOOL_CIRCLE_ANNOTATION:
                currentToolHandler = new CircleAnnotationHandler(
                        documentViewController,
                        this,
                        documentViewModel);
                documentViewController.clearSelectedText();
                break;
            case DocumentViewModel.DISPLAY_TOOL_INK_ANNOTATION:
                currentToolHandler = new InkAnnotationHandler(
                        documentViewController,
                        this,
                        documentViewModel);
                documentViewController.clearSelectedText();
                break;
            case DocumentViewModel.DISPLAY_TOOL_FREE_TEXT_ANNOTATION:
                currentToolHandler = new FreeTextAnnotationHandler(
                        documentViewController,
                        this,
                        documentViewModel);
                documentViewController.clearSelectedText();
                break;
            case DocumentViewModel.DISPLAY_TOOL_TEXT_ANNOTATION:
                currentToolHandler = new TextAnnotationHandler(
                        documentViewController,
                        this,
                        documentViewModel);
                documentViewController.clearSelectedText();
                break;
            default:
                currentToolHandler = null;
        }
        if (currentToolHandler != null) {
            currentToolHandler.installTool();
            addMouseListener(currentToolHandler);
            addMouseMotionListener(currentToolHandler);
        }
    }

    /**
     * Gets a list of the annotation components used in this page view.
     *
     * @return list of annotation components, can be null.
     */
    public ArrayList<AbstractAnnotationComponent> getAnnotationComponents() {
        return annotationComponents;
    }

    /**
     * Gets the page components TextSelectionPageHandler.  Each page has one and it directly accessed by the
     * TextSelectionViewHandler.  All other tools are created/disposed as the tools are selected.
     *
     * @return page's instance of the text selection handler.
     */
    public TextSelectionPageHandler getTextSelectionPageHandler() {
        return textSelectionPageHandler;
    }

    public ToolHandler getCurrentToolHandler() {
        return currentToolHandler;
    }

    private void paintTextSelection(Graphics g) {
        // Lazy paint of highlight and select all text states.
        Page currentPage = getPage();
        // paint any highlighted words
        DocumentSearchController searchController =
                documentViewController.getParentController().getDocumentSearchController();
        if (currentPage != null && currentPage.isInitiated() &&
                // make sure we don't accidentally block the awt ui thread, but we still
                // want to paint search text and text selection if text selection tool is selected.
                (searchController.isSearchHighlightRefreshNeeded(pageIndex, null) ||
                        documentViewModel.isViewToolModeSelected(DocumentViewModel.DISPLAY_TOOL_TEXT_SELECTION) ||
                        documentViewModel.isViewToolModeSelected(DocumentViewModel.DISPLAY_TOOL_HIGHLIGHT_ANNOTATION) ||
                        documentViewModel.isViewToolModeSelected(DocumentViewModel.DISPLAY_TOOL_STRIKEOUT_ANNOTATION) ||
                        documentViewModel.isViewToolModeSelected(DocumentViewModel.DISPLAY_TOOL_UNDERLINE_ANNOTATION))
                ) {
            try {
                PageText pageText = currentPage.getViewText();
                if (pageText != null) {
                    // paint any highlighted words
                    if (searchController.isSearchHighlightRefreshNeeded(pageIndex, pageText)) {
                        searchController.searchHighlightPage(pageIndex);
                    }
                    // if select all we'll want to paint the selected text.
                    if (documentViewModel.isSelectAll()) {
                        documentViewModel.addSelectedPageText(this);
                        pageText.selectAll();
                    }
                    // paint selected text.
                    TextSelection.paintSelectedText(g, this, documentViewModel);
                }
            } catch (InterruptedException e) {
                logger.fine("Interrupt exception during view text fetch.");
            }
        } else if (currentPage != null && !currentPage.isInitiated()) {
            // there is good chance a page has been disposed on a large document, but if we have search hit we need
            // to repaint the page, setting the buffer to dirty will reinitialize the page on the next paint cycle.
            if (searchController.isSearchHighlightRefreshNeeded(pageIndex, null)) {
                pageBufferStore.setDirty(true);
            }
        }
    }

    private void paintAnnotationComponents(Graphics g) {
        Page currentPage = getPage();
        if (currentPage != null && annotationComponents != null) {
            Graphics2D gg2 = (Graphics2D) g;
            // save draw state.
            AffineTransform prePaintTransform = gg2.getTransform();
            Color oldColor = gg2.getColor();
            Stroke oldStroke = gg2.getStroke();
            // apply page transform.
            AffineTransform at = currentPage.getPageTransform(
                    documentViewModel.getPageBoundary(),
                    documentViewModel.getViewRotation(),
                    documentViewModel.getViewZoom());
            gg2.transform(at);
            // get current tool state, we don't want to draw the highlight
            // state if the selection tool is selected.
            boolean notSelectTool =
                    documentViewModel.getViewToolMode() !=
                            DocumentViewModel.DISPLAY_TOOL_SELECTION;
            // paint all annotations on top of the content buffer
            AnnotationComponent annotation;
            for (int i = 0; i < annotationComponents.size(); i++) {
                annotation = annotationComponents.get(i);
                if (annotation != null && ((Component) annotation).isVisible() &&
                        !(annotation.getAnnotation() instanceof FreeTextAnnotation
                                && ((AbstractAnnotationComponent) annotation).isActive()) &&
                        !(annotation.getAnnotation() instanceof TextWidgetAnnotation
                                && ((AbstractAnnotationComponent) annotation).isActive()) &&
                        !(annotation.getAnnotation() instanceof ChoiceWidgetAnnotation
                                && ((AbstractAnnotationComponent) annotation).isActive())) {
                    annotation.getAnnotation().render(gg2,
                            GraphicsRenderingHints.SCREEN,
                            documentViewModel.getViewRotation(),
                            documentViewModel.getViewZoom(),
                            annotation.hasFocus() && notSelectTool);
                }
            }
            // post paint clean up.
            gg2.setColor(oldColor);
            gg2.setStroke(oldStroke);
            gg2.setTransform(prePaintTransform);
        }
    }

    public void focusGained(FocusEvent e) {
        int oldCurrentPage = documentViewModel.getViewCurrentPageIndex();
        documentViewModel.setViewCurrentPageIndex(pageIndex);
        documentViewController.firePropertyChange(PropertyConstants.DOCUMENT_CURRENT_PAGE,
                oldCurrentPage,
                pageIndex);
    }

    public void focusLost(FocusEvent e) {
    }

    public void updateView(String propertyConstant, Object oldValue, Object newValue) {
        super.updateView(propertyConstant, oldValue, newValue);
        // revalidate the annotation components.
        if (PropertyConstants.DOCUMENT_VIEW_ROTATION_CHANGE.equals(propertyConstant) ||
                PropertyConstants.DOCUMENT_VIEW_ZOOM_CHANGE.equals(propertyConstant)) {
            if (annotationComponents != null) {
                for (AbstractAnnotationComponent comp : annotationComponents) {
                    comp.validate();
                }
            }
        }
    }

    /**
     * Add a new annotation object to this page view component.
     *
     * @param annotation annotation to add.
     */
    public void addAnnotation(AnnotationComponent annotation) {
        // delegate to handler.
        if (annotationComponents == null) {
            annotationComponents = new ArrayList<AbstractAnnotationComponent>();
        }
        annotationComponents.add((AbstractAnnotationComponent) annotation);
        if (annotation instanceof PopupAnnotationComponent) {
            this.add((AbstractAnnotationComponent) annotation, JLayeredPane.POPUP_LAYER);
        } else {
            this.add((AbstractAnnotationComponent) annotation, JLayeredPane.DEFAULT_LAYER);
        }
    }

    /**
     * Remove the specified annotation from this page view.
     *
     * @param annotationComp annotation to be removed.
     */
    public void removeAnnotation(AnnotationComponent annotationComp) {
        annotationComponents.remove(annotationComp);
        this.remove((AbstractAnnotationComponent) annotationComp);
    }

    public void pageInitializedCallback(Page page) {
        refreshAnnotationComponents(page);
    }

    public void pageTeardownCallback() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // we're cleaning up the page which may involve awt component manipulations o we queue
                // callback on the awt thread so we don't try and paint something we just removed
                annotationComponents = null;
            }
        });
    }

    public void refreshAnnotationComponents(Page page) {
        if (page != null) {
            final List<Annotation> annotations = page.getAnnotations();
            final AbstractPageViewComponent parent = this;
            if (annotations != null && annotations.size() > 0) {
                // we don't want to re-initialize the component as we'll
                // get duplicates if the page has be gc'd
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (annotationComponents == null) {
                            annotationComponents = new ArrayList<AbstractAnnotationComponent>(annotations.size());

                            for (Annotation annotation : annotations) {
                                // parser can sometimes return an empty array depending on the PDF syntax being used.
                                if (annotation != null) {
                                    final AbstractAnnotationComponent comp =
                                            AnnotationComponentFactory.buildAnnotationComponent(
                                                    annotation, documentViewController,
                                                    parent, documentViewModel);
                                    if (comp != null ) {
                                        // add for painting
                                        annotationComponents.add(comp);
                                        // add to layout
                                        if (comp instanceof PopupAnnotationComponent) {
                                            parent.add(comp, JLayeredPane.POPUP_LAYER);
                                        } else {
                                            parent.add(comp, JLayeredPane.DEFAULT_LAYER);
                                        }
                                        comp.revalidate();
                                        comp.repaint();
                                    }

                                }
                            }
                        }
                    }
                });
            }
        }
    }

}
