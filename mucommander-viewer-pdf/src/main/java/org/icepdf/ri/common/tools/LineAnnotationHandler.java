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

import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.PDate;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.annotations.Annotation;
import org.icepdf.core.pobjects.annotations.AnnotationFactory;
import org.icepdf.core.pobjects.annotations.BorderStyle;
import org.icepdf.core.pobjects.annotations.LineAnnotation;
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
import java.awt.geom.Point2D;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LineAnnotationHandler tool is responsible for painting representation of
 * a line on the screen during a click and drag mouse event.  The first point
 * is recorded on mousePressed and the line is drawn from first point the current
 * location of the mouse.
 * <p/>
 * Once the mouseReleased event is fired this handler will create new
 * LineAnnotation and respective AnnotationComponent.  The addition of the
 * Annotation object to the page is handled by the annotation callback.
 *
 * @since 5.0
 */
public class LineAnnotationHandler extends SelectionBoxHandler implements ToolHandler {


    private static final Logger logger =
            Logger.getLogger(LineAnnotationHandler.class.toString());

    // need to make the stroke cap, thickness configurable. Or potentially
    // static from the lineAnnotationHandle so it would look like the last
    // settings where remembered.
    protected static BasicStroke stroke = new BasicStroke(1.0f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            1.0f);

    protected static Color lineColor;
    protected static Color internalColor;

    static {

        // sets annotation link stroke colour
        try {
            String color = Defs.sysProperty(
                    "org.icepdf.core.views.page.annotation.line.stroke.color", "#ff0000");
            int colorValue = ColorUtil.convertColor(color);
            lineColor =
                    new Color(colorValue >= 0 ? colorValue :
                            Integer.parseInt("ff0000", 16));
        } catch (NumberFormatException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Error reading line Annotation stroke colour");
            }
        }

        // sets annotation link fill colour
        try {
            String color = Defs.sysProperty(
                    "org.icepdf.core.views.page.annotation.line.fill.color", "#ff0000");
            int colorValue = ColorUtil.convertColor(color);
            internalColor =
                    new Color(colorValue >= 0 ? colorValue :
                            Integer.parseInt("ff0000", 16));
        } catch (NumberFormatException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Error reading line Annotation fill colour");
            }
        }
    }

    protected static Name startLineEnding = LineAnnotation.LINE_END_NONE;
    protected static Name endLineEnding = LineAnnotation.LINE_END_NONE;

    // start and end point
    protected Point2D startOfLine;
    protected Point2D endOfLine;

    protected BorderStyle borderStyle = new BorderStyle();

    /**
     * New Text selection handler.  Make sure to correctly and and remove
     * this mouse and text listeners.
     *
     * @param pageViewComponent page component that this handler is bound to.
     * @param documentViewModel view model.
     */
    public LineAnnotationHandler(DocumentViewController documentViewController,
                                 AbstractPageViewComponent pageViewComponent,
                                 DocumentViewModel documentViewModel) {
        super(documentViewController, pageViewComponent, documentViewModel);
        startLineEnding = LineAnnotation.LINE_END_NONE;
        endLineEnding = LineAnnotation.LINE_END_NONE;
    }

    public void paintTool(Graphics g) {
        if (startOfLine != null && endOfLine != null) {
            Graphics2D gg = (Graphics2D) g;
            Color oldColor = gg.getColor();
            Stroke oldStroke = gg.getStroke();
            g.setColor(lineColor);
            gg.setStroke(stroke);
            g.drawLine((int) startOfLine.getX(), (int) startOfLine.getY(),
                    (int) endOfLine.getX(), (int) endOfLine.getY());
            g.setColor(oldColor);
            gg.setStroke(oldStroke);
        }
    }

    public void mousePressed(MouseEvent e) {
        startOfLine = e.getPoint();
        // annotation selection box.
        int x = e.getX();
        int y = e.getY();
        currentRect = new Rectangle(x, y, 0, 0);
        updateDrawableRect(pageViewComponent.getWidth(),
                pageViewComponent.getHeight());
        pageViewComponent.repaint();
    }

    public void mouseReleased(MouseEvent e) {
        endOfLine = e.getPoint();
        updateSelectionSize(e.getX(),e.getY(), pageViewComponent);

        // add a little padding or the end point icon types
        rectToDraw.setRect(rectToDraw.getX() - 8, rectToDraw.getY() - 8,
                rectToDraw.getWidth() + 16, rectToDraw.getHeight() + 16);

        // convert bbox and start and end line points.
        Rectangle tBbox = convertToPageSpace(rectToDraw).getBounds();
        // convert start of line and end of line to page space
        Point2D[] points = convertToPageSpace(startOfLine, endOfLine);

        // create annotations types that  are rectangle based;
        LineAnnotation annotation = (LineAnnotation)
                AnnotationFactory.buildAnnotation(
                        documentViewModel.getDocument().getPageTree().getLibrary(),
                        Annotation.SUBTYPE_LINE,
                        tBbox);
        annotation.setStartArrow(startLineEnding);
        annotation.setEndArrow(endLineEnding);
        annotation.setStartOfLine(points[0]);
        annotation.setEndOfLine(points[1]);
        annotation.setBorderStyle(borderStyle);
        annotation.setColor(lineColor);
        annotation.setInteriorColor(internalColor);

        // setup the markup properties.
        annotation.setContents(annotation.getSubType().toString());
        annotation.setCreationDate(PDate.formatDateTime(new Date()));
        annotation.setTitleText(System.getProperty("user.name"));

        // pass outline shapes and bounds to create the highlight shapes
        annotation.setBBox(tBbox);
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
        // add them to the container, using absolute positioning.
        if (documentViewController.getAnnotationCallback() != null) {
            AnnotationCallback annotationCallback =
                    documentViewController.getAnnotationCallback();
            annotationCallback.newAnnotation(pageViewComponent, comp);
        }

        // set the annotation tool to he select tool
        documentViewController.getParentController().setDocumentToolMode(
                DocumentViewModel.DISPLAY_TOOL_SELECTION);

        // clear the rectangle
        clearRectangle(pageViewComponent);
        startOfLine = endOfLine = null;
    }

    public void mouseDragged(MouseEvent e) {
        updateSelectionSize(e.getX(),e.getY(), pageViewComponent);
        endOfLine = e.getPoint();
        pageViewComponent.repaint();
    }

    /**
     * Convert the shapes that make up the annotation to page space so that
     * they will scale correctly at different zooms.
     *
     * @return transformed bBox.
     */
    protected Rectangle convertToPageSpace() {
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
        Rectangle tBbox = new Rectangle(rectToDraw.x, rectToDraw.y,
                rectToDraw.width, rectToDraw.height);

        tBbox = at.createTransformedShape(tBbox).getBounds();

        // convert the points
        startOfLine = at.transform(startOfLine, null);
        endOfLine = at.transform(endOfLine, null);

        return tBbox;

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseClicked(MouseEvent e) {
        if (pageViewComponent != null) {
            pageViewComponent.requestFocus();
        }
    }

    public void mouseExited(MouseEvent e) {

    }

    public void mouseMoved(MouseEvent e) {

    }

    public void installTool() {

    }

    public void uninstallTool() {

    }

    @Override
    public void setSelectionRectangle(Point cursorLocation, Rectangle selection) {

    }
}
