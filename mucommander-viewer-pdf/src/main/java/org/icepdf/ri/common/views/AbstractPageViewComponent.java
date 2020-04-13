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

import org.icepdf.core.events.PaintPageEvent;
import org.icepdf.core.events.PaintPageListener;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.PageTree;
import org.icepdf.core.util.*;
import org.icepdf.ri.common.views.listeners.DefaultPageViewLoadingListener;
import org.icepdf.ri.common.views.listeners.PageViewLoadingListener;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains all the functionality for showing a pages content.   This view works closely with the clip
 * provided by a parent JScrollPane component to optimize memory usage.  Page content is painted to a back buffer
 * which is painted by the component when ready.  The back buffer is scaled on subsequent paints to show content and
 * is later replaced with a new buffer that is painted with the current page properties.
 */
public abstract class AbstractPageViewComponent
        extends JLayeredPane
        implements PageViewComponent {

    private static final Logger logger =
            Logger.getLogger(AbstractPageViewComponent.class.toString());

    protected static final int PAGE_BOUNDARY_BOX = Page.BOUNDARY_CROPBOX;

    private static Color pageColor;
    protected static int pageBufferPadding = 250;
    protected static boolean progressivePaint = true;

    static {
        try {
            String color = Defs.sysProperty(
                    "org.icepdf.core.views.page.paper.color", "#FFFFFF");
            int colorValue = ColorUtil.convertColor(color);
            pageColor =
                    new Color(colorValue >= 0 ? colorValue :
                            Integer.parseInt("FFFFFF", 16));
        } catch (NumberFormatException e) {
            logger.warning("Error reading page paper color.");
        }
        // buffer size padding in pixels
        pageBufferPadding = Defs.intProperty("org.icepdf.core.views.bufferpadding", 250);
        // progressive paint of first page loat.
        progressivePaint = Defs.booleanProperty("org.icepdf.core.views.page.progressivePaint", true);
    }

    // flags for painting annotations and text highlights.
    protected boolean paintAnnotations = true;
    protected boolean paintSearchHighlight = false;

    // view mvc parents
    protected DocumentView parentDocumentView;
    protected DocumentViewModel documentViewModel;
    protected DocumentViewController documentViewController;

    // scrollPane is very important for optimization of multiple page views.
    protected JScrollPane parentScrollPane;
    protected PageTree pageTree;
    protected int pageIndex;

    // page properties for a given view state.
    protected Rectangle pageSize;
    protected float pageZoom, pageRotation;
    protected int pageBoundaryBox;
    protected PageBufferStore pageBufferStore;
    // systems graphics configuration for creating a pages back buffer.
    protected GraphicsConfiguration graphicsConfiguration;

    // Main worker task.
    protected FutureTask<Object> pageImageCaptureTask;

    public AbstractPageViewComponent(DocumentViewModel documentViewModel, PageTree pageTree,
                                     final int pageIndex, JScrollPane parentScrollPane, int width, int height) {
        // needed to propagate mouse events.
        this.documentViewModel = documentViewModel;
        this.parentScrollPane = parentScrollPane;
        this.pageTree = pageTree;
        this.pageIndex = pageIndex;

        // current state.
        if (documentViewModel != null) {
            pageZoom = documentViewModel.getViewZoom();
            pageRotation = documentViewModel.getViewRotation();
            pageBoundaryBox = documentViewModel.getPageBoundary();
        } else {
            pageZoom = 1.0f;
            pageRotation = 0;
            pageBoundaryBox = PAGE_BOUNDARY_BOX;
        }

        // setup the store for the pageBufferPadding and current clip
        pageBufferStore = new PageBufferStore();

        // initialize page size
        pageSize = new Rectangle();
        if (documentViewModel != null && width == 0 && height == 0) {
            calculatePageSize(pageSize, documentViewModel.getViewRotation(), documentViewModel.getViewZoom());
        } else {
            pageSize.setSize(width, height);
        }
    }

    public Dimension getPreferredSize() {
        return pageSize.getSize();
    }

    public Dimension getSize() {
        return pageSize.getSize();
    }

    public void clearSelectedText() {
        // on mouse click clear the currently selected sprints
        Page currentPage = getPage();
        // clear selected text.
        if (currentPage.isInitiated()) {
            try {
                if (currentPage.getViewText() != null) {
                    currentPage.getViewText().clearSelected();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets the text that is contained in the specified rectangle and the
     * given mouse pointer.  The cursor and selection rectangle must be in
     * in page space.
     *
     * @param cursorLocation location of cursor or mouse.
     * @param selection      rectangle of text to include in selection.
     */
    public void setSelectionRectangle(Point cursorLocation, Rectangle selection) {

    }

    /**
     * Clear any internal data structures that represent selected text and
     * repaint the component.
     */
    public void clearSelectionRectangle() {

    }

    public int getPageIndex() {
        return pageIndex;
    }

    public Page getPage() {
        return pageTree.getPage(pageIndex);
    }

    public void setDocumentViewCallback(DocumentView parentDocumentView) {
        this.parentDocumentView = parentDocumentView;
        documentViewController = this.parentDocumentView.getParentViewController();
    }

    public static boolean isAnnotationTool(final int displayTool) {
        return displayTool == DocumentViewModel.DISPLAY_TOOL_SELECTION ||
                displayTool == DocumentViewModel.DISPLAY_TOOL_LINK_ANNOTATION ||
                displayTool == DocumentViewModel.DISPLAY_TOOL_HIGHLIGHT_ANNOTATION ||
                displayTool == DocumentViewModel.DISPLAY_TOOL_SQUIGGLY_ANNOTATION ||
                displayTool == DocumentViewModel.DISPLAY_TOOL_STRIKEOUT_ANNOTATION ||
                displayTool == DocumentViewModel.DISPLAY_TOOL_UNDERLINE_ANNOTATION;
    }

    /**
     * Called from parent controls when a UI control has manipulated the view, property
     * change is picked up and the view is updated accordingly. Responds to
     * PropertyConstants.DOCUMENT_VIEW_ROTATION_CHANGE and
     * PropertyConstants.DOCUMENT_VIEW_ZOOM_CHANGE.  If the worker is currently working
     * is is cancel with interrupts.
     *
     * @param propertyConstant document view change property.
     * @param oldValue         old value
     * @param newValue         new value
     */
    public void updateView(String propertyConstant, Object oldValue, Object newValue) {
        if (pageImageCaptureTask != null && !pageImageCaptureTask.isDone()) {
            pageImageCaptureTask.cancel(true);
        }
        if (PropertyConstants.DOCUMENT_VIEW_ROTATION_CHANGE.equals(propertyConstant)) {
            pageRotation = (Float) newValue;
        } else if (PropertyConstants.DOCUMENT_VIEW_ZOOM_CHANGE.equals(propertyConstant)) {
            pageZoom = (Float) newValue;
        } else if (PropertyConstants.DOCUMENT_VIEW_REFRESH_CHANGE.equals(propertyConstant)) {
            // nothing to do but repaint
        } else if (PropertyConstants.DOCUMENT_VIEW_DEMO_MODE_CHANGE.equals(propertyConstant)) {
            // re-initialized the page.
            pageBufferStore.setDirty(true);
            Page page = getPage();
            page.getLibrary().disposeFontResources();
            page.resetInitializedState();
        }
        calculatePageSize(pageSize, pageRotation, pageZoom);
        pageBufferStore.setDirty(true);
    }

    /**
     * Checks if this page intersects the viewport
     *
     * @return true if page is visible in viewport,  false otherwise.
     * @throws NullPointerException if the parent scrollPane is null.
     */
    private boolean isPageIntersectViewport() {
        Rectangle pageBounds = documentViewModel != null ? documentViewModel.getPageBounds(pageIndex) : getBounds();
        return pageBounds != null && this.isShowing() &&
                pageBounds.intersects(parentScrollPane.getViewport().getViewRect());
    }

    /**
     * Calculates the page size for the rotation and zoom.  The new values are assigned to the pageSize.
     *
     * @param pageSize rectangle to update,  new rectangle will not be created.
     * @param rotation rotation of page.
     * @param zoom     zoom of page
     */
    protected void calculatePageSize(Rectangle pageSize, float rotation, float zoom) {
        if (pageTree != null) {
            Page currentPage = pageTree.getPage(pageIndex);
            if (currentPage != null) {
                pageSize.setSize(currentPage.getSize(pageBoundaryBox,
                        rotation, zoom).toDimension());
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // create a copy so we can set our own state with out affecting the parent graphics conttent.
        Graphics2D g2d = (Graphics2D) g.create(0, 0, pageSize.width, pageSize.height);
        GraphicsRenderingHints grh = GraphicsRenderingHints.getDefault();
        g2d.setRenderingHints(grh.getRenderingHints(GraphicsRenderingHints.SCREEN));
        // page location in the the entire view.
        calculateBufferLocation();

        // paint the paper
        g2d.setColor(pageColor);
        g2d.fillRect(0, 0, pageSize.width, pageSize.height);

        // paint the pageBufferPadding, but get the latest copy encase it was returned extra quick
        BufferedImage pageImage = pageBufferStore.getImageReference();
        if (pageImage != null) {
            Rectangle paintingClip = pageBufferStore.getImageLocation();
            // check if we should scale and rotate the current capture
            if (pageZoom != pageBufferStore.getPageZoom() ||
                    pageRotation != pageBufferStore.getPageRotation()) {
                g2d.transform(calculateBufferAffineTransform());
                pageBufferStore.setDirty(true);
            }
            g2d.drawImage(pageImage, paintingClip.x, paintingClip.y, null);
        }
        g2d.dispose();
    }

    /**
     * Calculates where we should be painting the new buffer and kicks off the the worker if the buffer
     * is deemed dirty. The Parent scrollpane viewport is taken into account to setup the clipping.
     */
    protected void calculateBufferLocation() {

        // grab a reference to the graphics configuration via the AWT thread,  if we get it on the worker thread
        // it sometimes return null.
        graphicsConfiguration = parentScrollPane.getGraphicsConfiguration();

        // update page size as we may have a page that's larger then the average document size.
        calculatePageSize(pageSize, pageRotation, pageZoom);

        // page location in the the entire view.
        Rectangle pageLocation = documentViewModel != null ?
                documentViewModel.getPageBounds(pageIndex) : new Rectangle(pageSize);
        Rectangle viewPort = parentScrollPane.getViewport().getViewRect();
        Rectangle imageLocation;
        Rectangle imageClipLocation;
        if (pageLocation.width < viewPort.width || pageLocation.height < viewPort.height) {
            // if page is smaller then viewport then we use the full page size.
            imageLocation = new Rectangle(0, 0, pageLocation.width, pageLocation.height);
            imageClipLocation = new Rectangle(imageLocation);
        } else {
            // otherwise we create a pageBufferPadding based on the viewport size plus some padding
            imageClipLocation = viewPort.intersection(pageLocation);
            // move the clip relative to page coordinates
            imageClipLocation.setLocation(
                    imageClipLocation.x - pageLocation.x, imageClipLocation.y - pageLocation.y);
            // we want the image to be a bit bigger to make scrolling look a little smoother.
            imageLocation = new Rectangle(imageClipLocation.x - pageBufferPadding,
                    imageClipLocation.y - pageBufferPadding,
                    imageClipLocation.width + pageBufferPadding * 2,
                    imageClipLocation.height + pageBufferPadding * 2);
            // we're using the AWT thread to check for scroll repaints,
            if (pageImageCaptureTask != null && pageBufferStore.getImageLocation() != null) {
                Rectangle imageAbsoluteLocation = new Rectangle(pageBufferStore.getImageLocation());
                imageAbsoluteLocation.setLocation(imageAbsoluteLocation.x + pageLocation.x,
                        imageAbsoluteLocation.y + pageLocation.y);
                if (!imageAbsoluteLocation.contains(viewPort.intersection(pageLocation))) {
                    pageBufferStore.setDirty(true);
                }
            }
        }

        // check if we need create or refresh the back pageBufferPadding.
        if (pageBufferStore.isDirty() || pageBufferStore.getImageReference() == null) {
            // start future task to paint back pageBufferPadding
            if (pageImageCaptureTask == null || pageImageCaptureTask.isDone() || pageImageCaptureTask.isCancelled()) {
                pageImageCaptureTask = new FutureTask<Object>(
                        new PageImageCaptureTask(this, imageLocation, imageClipLocation,
                                pageZoom,
                                pageRotation));
                Library.execute(pageImageCaptureTask);
            }
        }
    }

    /**
     * Calculates the affine transform that paints the old buffered image using the current scale and rotation.  This
     * avoid the back buffer flicker.  Once the worker captures the new buffer we swap in the new buffer.
     * todo, still needs some work with regards to rotation of the buffer.
     *
     * @return transform needed to paint the previous out of sync buffer in the correct place.
     */
    private AffineTransform calculateBufferAffineTransform() {
        AffineTransform at = new AffineTransform();
        if (pageZoom != pageBufferStore.getPageZoom()) {
            double pageScale = pageZoom / (double) pageBufferStore.getPageZoom();
            at.scale(pageScale, pageScale);
        }
        // get the page size of the currently painted image we are trying to scale or rotate.
        if (pageRotation != pageBufferStore.getPageRotation()) {
            double rotation = 0;
            rotation = pageBufferStore.getPageRotation() - pageRotation;
            if (rotation < 0) {
                rotation += 360;
            }
            Rectangle imageLocation = pageBufferStore.getPageSize();
            if (rotation == 90) {
                at.translate(imageLocation.height, 0);
            } else if (rotation == 180) {
                at.translate(imageLocation.width, 0);
            } else if (rotation == 270) {
                at.translate(imageLocation.height, -imageLocation.width);
            }
            double theta = rotation * Math.PI / 180.0;
            at.rotate(theta);
        }
        return at;
    }

    /**
     * The worker of any successful page paint.  The worker takes a snapshot of the given page state
     * and paint the desired image to buffer.  One completed the the new buffer is stuffed into
     * the pageBufferStore instance with properties so that it can be painted in the correct thread
     * when the component is repainted.
     */
    public class PageImageCaptureTask implements Callable<Object>, PaintPageListener {

        private float zoom;
        private float rotation;
        private Rectangle imageLocation;
        private Rectangle imageClipLocation;
        private JComponent parent;

        public PageImageCaptureTask(JComponent parent, Rectangle imageLocation, Rectangle imageClipLocation,
                                    float zoom, float rotation) {
            this.zoom = zoom;
            this.rotation = rotation;
            this.parent = parent;
            this.imageLocation = imageLocation;
            this.imageClipLocation = imageClipLocation;
        }

        public Object call() throws Exception {
            if (!isPageIntersectViewport()) {
                pageTeardownCallback();
                return null;
            }
            // paint page.
            Page page = pageTree.getPage(pageIndex);
            // page loading progress
            PageViewLoadingListener pageLoadingListener = new DefaultPageViewLoadingListener(parent, documentViewController);
            boolean isFirstProgressivePaint = false;
            try {
                if (documentViewController != null) page.addPageProcessingListener(pageLoadingListener);
                // page init, interruptible
                page.init();
                pageInitializedCallback(page);

                BufferedImage pageBufferImage = graphicsConfiguration.createCompatibleImage(
                        imageLocation.width, imageLocation.height,
                        BufferedImage.TYPE_INT_ARGB);
                Graphics g2d = pageBufferImage.createGraphics();

                // if we don't have a soft reference then we are likely on a first clean paint at which
                // point we can kick off the animated paint.
                if (progressivePaint && pageBufferStore.getImageReference() == null) {
                    page.addPaintPageListener(this);
                    isFirstProgressivePaint = true;
                    pageBufferStore.setState(pageBufferImage, imageLocation, imageClipLocation, pageSize,
                            zoom, rotation, true);
                }
                g2d.setClip(0, 0, imageLocation.width, imageLocation.height);
                g2d.translate(-imageLocation.x, -imageLocation.y);
                // paint page interruptible
                page.paint(g2d, GraphicsRenderingHints.SCREEN, pageBoundaryBox, rotation, zoom,
                        paintAnnotations, paintSearchHighlight);
                g2d.dispose();
                // init and paint thread went under interrupted, we can move the back pageBufferPadding to the front.
                pageBufferStore.setState(pageBufferImage, imageLocation, imageClipLocation, pageSize,
                        zoom, rotation, false);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.finer("Interrupted page capture task: " + e.getMessage() + " " + pageIndex);
                // flush the buffer if this is our first paint.
                if (isFirstProgressivePaint) pageBufferStore.setImageReference(null);
            } catch (Throwable e) {
                logger.log(Level.WARNING, "Error during page capture task: " + e.getMessage() + " " + pageIndex, e);
                // avoid a repaint as we'll likely get caught in an infinite loop.
            } finally {
                page.removePaintPageListener(this);
                page.removePageProcessingListener(pageLoadingListener);
            }
            // queue a repaint, regardless of outcome
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    repaint();
                }
            });

            notifyAll();
            return null;
        }

        public void paintPage(PaintPageEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    repaint();
                }
            });
        }
    }

    /**
     * Synchronized page buffer property store, insures that a page capture occurs using the correct properties.
     */
    protected class PageBufferStore {

        // last page buffer store,
        private SoftReference<BufferedImage> imageReference;
        // paint location if buffer is clipped to be smaller then the page size.
        private Rectangle imageLocation;
        // location of the current clip,  generally the viewport intersection with the page bounds.
        private Rectangle imageClipLocation;
        private float pageZoom, pageRotation;
        // page size at the given zoom and location.
        private Rectangle pageSize;
        // dirty flag.
        private boolean isDirty;

        private final Object objectLock = new Object();

        PageBufferStore() {
            imageReference = new SoftReference<BufferedImage>(null);
        }

        void setState(BufferedImage pageBufferImage, Rectangle imageLocation, Rectangle imageClipLocation,
                      Rectangle pageSize, float pageZoom, float pageRotation, boolean isDirty) {
            synchronized (objectLock) {
                this.imageReference = new SoftReference<BufferedImage>(pageBufferImage);
                this.imageLocation = imageLocation;
                this.imageClipLocation = imageClipLocation;
                this.pageSize = pageSize;
                this.pageZoom = pageZoom;
                this.pageRotation = pageRotation;
                this.isDirty = isDirty;
            }
        }

        void setImageReference(BufferedImage bufferedImage) {
            synchronized (objectLock) {
                this.imageReference = new SoftReference<BufferedImage>(bufferedImage);
            }
        }

        public BufferedImage getImageReference() {
            synchronized (objectLock) {
                return imageReference.get();
            }
        }

        Rectangle getImageLocation() {
            synchronized (objectLock) {
                return imageLocation;
            }
        }

        Rectangle getImageClipLocation() {
            synchronized (objectLock) {
                return imageClipLocation;
            }
        }

        Rectangle getPageSize() {
            synchronized (objectLock) {
                return pageSize;
            }
        }

        float getPageZoom() {
            synchronized (objectLock) {
                return pageZoom;
            }
        }

        float getPageRotation() {
            synchronized (objectLock) {
                return pageRotation;
            }
        }

        public boolean isDirty() {
            synchronized (objectLock) {
                return isDirty;
            }
        }

        public void setDirty(boolean dirty) {
            synchronized (objectLock) {
                this.isDirty = dirty;
            }
        }
    }

}
