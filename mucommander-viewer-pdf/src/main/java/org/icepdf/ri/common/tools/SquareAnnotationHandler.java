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

import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.annotations.Annotation;
import org.icepdf.core.pobjects.annotations.AnnotationFactory;
import org.icepdf.core.pobjects.annotations.BorderStyle;
import org.icepdf.core.pobjects.annotations.SquareAnnotation;
import org.icepdf.core.util.ColorUtil;
import org.icepdf.core.util.Defs;
import org.icepdf.ri.common.views.AbstractPageViewComponent;
import org.icepdf.ri.common.views.AnnotationCallback;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.common.views.DocumentViewModel;
import org.icepdf.ri.common.views.annotations.AbstractAnnotationComponent;
import org.icepdf.ri.common.views.annotations.AnnotationComponentFactory;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SquareAnnotationHandler tool is responsible for painting representation of
 * a square on the screen during a click and drag mouse event.  The box
 * created by this mouse event will be used to draw square within its bounds.
 * <p/>
 * Once the mouseReleased event is fired this handler will create new
 * SquareAnnotation and respective AnnotationComponent.  The addition of the
 * Annotation object to the page is handled by the annotation callback.
 *
 * @since 5.0
 */
public class SquareAnnotationHandler extends SelectionBoxHandler implements ToolHandler {

    private static final Logger logger =
            Logger.getLogger(SquareAnnotationHandler.class.toString());

    protected final static float DEFAULT_STROKE_WIDTH = 3.0f;

    protected static BasicStroke stroke;
    protected static float strokeWidth;
    protected static Color lineColor;
    protected static Color internalColor;
    protected static boolean useInternalColor;

    static {

        // sets annotation squareCircle stroke colour
        try {
            String color = Defs.sysProperty(
                    "org.icepdf.core.views.page.annotation.squareCircle.stroke.color", "#ff0000");
            int colorValue = ColorUtil.convertColor(color);
            lineColor =
                    new Color(colorValue >= 0 ? colorValue :
                            Integer.parseInt("ff0000", 16));
        } catch (NumberFormatException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Error reading squareCircle Annotation stroke colour");
            }
        }

        // sets annotation link squareCircle colour
        useInternalColor = Defs.booleanProperty(
                "org.icepdf.core.views.page.annotation.squareCircle.fill.enabled", false);

        // sets annotation link squareCircle colour
        try {
            String color = Defs.sysProperty(
                    "org.icepdf.core.views.page.annotation.squareCircle.fill.color", "#ffffff");
            int colorValue = ColorUtil.convertColor(color);
            internalColor =
                    new Color(colorValue >= 0 ? colorValue :
                            Integer.parseInt("ffffff", 16));
        } catch (NumberFormatException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Error reading squareCircle Annotation fill colour");
            }
        }

        strokeWidth = (float) Defs.doubleProperty("org.icepdf.core.views.page.annotation.squareCircle.stroke.width",
                DEFAULT_STROKE_WIDTH);

        // need to make the stroke cap, thickness configurable. Or potentially
        // static from the AnnotationHandle so it would look like the last
        // settings where remembered.
        stroke = new BasicStroke(strokeWidth,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                1.0f);
    }

    // start and end point
    protected Rectangle rectangle;

    protected BorderStyle borderStyle = new BorderStyle();

    /**
     * New Text selection handler.  Make sure to correctly and and remove
     * this mouse and text listeners.
     *
     * @param pageViewComponent page component that this handler is bound to.
     * @param documentViewModel view model.
     */
    public SquareAnnotationHandler(DocumentViewController documentViewController,
                                   AbstractPageViewComponent pageViewComponent,
                                   DocumentViewModel documentViewModel) {
        super(documentViewController, pageViewComponent, documentViewModel);
        borderStyle.setStrokeWidth(DEFAULT_STROKE_WIDTH);
    }

    public void paintTool(Graphics g) {
        if (rectangle != null) {
            Graphics2D gg = (Graphics2D) g;
            Color oldColor = gg.getColor();
            Stroke oldStroke = gg.getStroke();
            gg.setStroke(stroke);
            if (useInternalColor) {
                gg.setColor(internalColor);
                gg.fill(rectangle);
            }
            gg.setColor(lineColor);
            gg.draw(rectangle);
            g.setColor(oldColor);
            gg.setStroke(oldStroke);
        }
    }

    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (rectangle == null) {
            rectangle = new Rectangle();
        }
        currentRect = new Rectangle(x, y, 0, 0);
        updateDrawableRect(pageViewComponent.getWidth(),
                pageViewComponent.getHeight());
        rectangle.setRect(currentRect);
        pageViewComponent.repaint();
    }

    public void mouseReleased(MouseEvent e) {
        updateSelectionSize(e.getX(), e.getY(), pageViewComponent);

        // convert the rectangle to page space
        rectangle = convertToPageSpace(rectangle);

        // check to make sure the bbox isn't zero height or width
        rectToDraw.setRect(rectToDraw.getX() - DEFAULT_STROKE_WIDTH,
                rectToDraw.getY() - DEFAULT_STROKE_WIDTH,
                rectToDraw.getWidth() + DEFAULT_STROKE_WIDTH * 2,
                rectToDraw.getHeight() + DEFAULT_STROKE_WIDTH * 2);

        // convert tBbox
        Rectangle tBbox = convertToPageSpace(rectToDraw);

        // create annotations types that that are rectangle based;
        // which is actually just link annotations
        SquareAnnotation annotation = (SquareAnnotation)
                AnnotationFactory.buildAnnotation(
                        documentViewModel.getDocument().getPageTree().getLibrary(),
                        Annotation.SUBTYPE_SQUARE,
                        tBbox);
        annotation.setColor(lineColor);
        if (annotation.isFillColor() || useInternalColor) {
            annotation.setFillColor(internalColor);
            if (!annotation.isFillColor()) {
                annotation.setFillColor(true);
            }
        }
        borderStyle.setStrokeWidth(strokeWidth);
        annotation.setRectangle(rectangle);
        annotation.setBorderStyle(borderStyle);

        // pass outline shapes and bounds to create the highlight shapes
        annotation.setBBox(new Rectangle(0, 0, tBbox.width, tBbox.height));
        annotation.resetAppearanceStream(getPageTransform());

        // create the annotation object.
        AbstractAnnotationComponent comp =
                AnnotationComponentFactory.buildAnnotationComponent(
                        annotation,
                        documentViewController,
                        pageViewComponent, documentViewModel);
        // set the bounds and refresh the userSpace rectangle
        Rectangle bbox = new Rectangle(rectToDraw.x, rectToDraw.y,
                rectToDraw.width, rectToDraw.height);
        comp.setBounds(bbox);
        // resets user space rectangle to match bbox converted to page space
        comp.refreshAnnotationRect();

        // add them to the container, using absolute positioning.
        if (documentViewController.getAnnotationCallback() != null) {
            AnnotationCallback annotationCallback =
                    documentViewController.getAnnotationCallback();
            annotationCallback.newAnnotation(pageViewComponent, comp);
        }

        // set the annotation tool to he select tool
        documentViewController.getParentController().setDocumentToolMode(
                DocumentViewModel.DISPLAY_TOOL_SELECTION);

        rectangle = null;
        // clear the rectangle
        clearRectangle(pageViewComponent);
    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mouseClicked(MouseEvent e) {
        if (pageViewComponent != null) {
            pageViewComponent.requestFocus();
        }
    }

    public void mouseMoved(MouseEvent e) {

    }

    public void installTool() {

    }

    public void uninstallTool() {

    }

    public void mouseDragged(MouseEvent e) {
        updateSelectionSize(e.getX(), e.getY(), pageViewComponent);
        rectangle.setRect(rectToDraw);
        pageViewComponent.repaint();
    }

    /**
     * Convert the shapes that make up the annotation to page space so that
     * they will scale correctly at different zooms.
     *
     * @return transformed bBox.
     */
    protected Rectangle convertToPageSpace(Rectangle rect) {
        Page currentPage = pageViewComponent.getPage();
        AffineTransform at = currentPage.getPageTransform(
                documentViewModel.getPageBoundary(),
                documentViewModel.getViewRotation(),
                documentViewModel.getViewZoom());
        try {
            at = at.createInverse();
        } catch (NoninvertibleTransformException e) {
            logger.log(Level.FINE, "Error converting to page space.", e);
        }
        // convert the two points as well as the bbox.
        Rectangle tBbox = new Rectangle(rect.x, rect.y,
                rect.width, rect.height);

        tBbox = at.createTransformedShape(tBbox).getBounds();

        return tBbox;

    }

    @Override
    public void setSelectionRectangle(Point cursorLocation, Rectangle selection) {

    }
}
