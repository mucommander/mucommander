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
package org.icepdf.core.pobjects.annotations;

import org.icepdf.core.pobjects.*;
import org.icepdf.core.pobjects.graphics.Shapes;
import org.icepdf.core.pobjects.graphics.commands.*;
import org.icepdf.core.util.Library;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * The purpose of a line annotation (PDF 1.3) is to display a single straight
 * line on the page. When opened, it shall display a pop-up window containing
 * the text of the associated note. Table 175 shows the annotation dictionary
 * entries specific to this type of annotation.
 *
 * @since 5.0
 */
public class LineAnnotation extends MarkupAnnotation {

    private static final Logger logger =
            Logger.getLogger(LineAnnotation.class.toString());

    /**
     * (Required) An array of four numbers, [x1 y1 x2 y2], specifying the starting
     * and ending coordinates of the line in default user space.
     * <p/>
     * If the LL entry is present, this value shall represent the endpoints of
     * the leader lines rather than the endpoints of the line itself; see Figure 60.
     */
    public static final Name L_KEY = new Name("L");
    /**
     * (Optional; PDF 1.4) An array of two names specifying the line ending styles
     * that shall be used in drawing the line. The first and second elements of
     * the array shall specify the line ending styles for the endpoints defined,
     * respectively, by the first and second pairs of coordinates, (x1, y1) and
     * (x2, y2), in the L array. Table 176 shows the possible values.
     * <p/>
     * Default value: [/None /None].
     */
    public static final Name LE_KEY = new Name("LE");
    /**
     * (Required if LLE is present, otherwise optional; PDF 1.6) The length of
     * leader lines in default user space that extend from each endpoint of the
     * line perpendicular to the line itself, as shown in Figure 60. A positive
     * value shall mean that the leader lines appear in the direction that is
     * clockwise when traversing the line from its starting point to its ending
     * point (as specified by L); a negative value shall indicate the opposite
     * direction.
     * <p/>
     * Default value: 0 (no leader lines).
     */
    public static final Name LL_KEY = new Name("LL");
    /**
     * (Optional; PDF 1.6) A non-negative number that shall represents the
     * length of leader line extensions that extend from the line proper 180
     * degrees from the leader lines, as shown in Figure 60.
     * <p/>
     * Default value: 0 (no leader line extensions).
     */
    public static final Name LLE_KEY = new Name("LLE");
    /**
     * (Optional; PDF 1.4) An array of numbers in the range 0.0 to 1.0 specifying
     * the interior color that shall be used to fill the annotation’s line endings
     * (see Table 176). The number of array elements shall determine the colour
     * space in which the colour is defined:
     * 0 - No colour; transparent
     * 1 - DeviceGray
     * 3 - DeviceRGB
     * 4 - DeviceCMYK
     */
    public static final Name IC_KEY = new Name("IC");
    /**
     * (Optional; PDF 1.6) If true, the text specified by the Contents or RC
     * entries shall be replicated as a caption in the appearance of the line,
     * as shown in Figure 61 and Figure 62. The text shall be rendered in a
     * manner appropriate to the content, taking into account factors such as
     * writing direction.
     * <p/>
     * Default value: false.
     */
    public static final Name CAP_KEY = new Name("Cap");
    /**
     * (Optional; PDF 1.7) A non-negative number that shall represent the length
     * of the leader line offset, which is the amount of empty space between the
     * endpoints of the annotation and the beginning of the leader lines.
     */
    public static final Name LLO_KEY = new Name("LLO");

    /**
     * (Optional; PDF 1.6) A name describing the intent of the line annotation
     * (see also Table 170). Valid values shall be LineArrow, which means that
     * the annotation is intended to function as an arrow, and LineDimension,
     * which means that the annotation is intended to function as a dimension line.
     */
//    public static final Name IT_KEY = new Name("IT");
    /**
     * (Optional; meaningful only if Cap is true; PDF 1.7) A name describing the
     * annotation’s caption positioning. Valid values are Inline, meaning the
     * caption shall be centered inside the line, and Top, meaning the caption
     * shall be on top of the line.
     * <p/>
     * Default value: Inline
     */
    public static final Name CP_KEY = new Name("CP");
    /**
     * (Optional; PDF 1.7) A measure dictionary (see Table 261) that shall
     * specify the scale and units that apply to the line annotation.
     */
    public static final Name MEASURE_KEY = new Name("Measure");
    /**
     * (Optional; meaningful only if Cap is true; PDF 1.7) An array of two numbers
     * that shall specify the offset of the caption text from its normal position.
     * The first value shall be the horizontal offset along the annotation line
     * from its midpoint, with a positive value indicating offset to the right
     * and a negative value indicating offset to the left. The second value shall
     * be the vertical offset perpendicular to the annotation line, with a
     * positive value indicating a shift up and a negative value indicating a
     * shift down.
     * <p/>
     * Default value: [0, 0] (no offset from normal positioning)
     */
    public static final Name CO_KEY = new Name("CO");
    /**
     * A square filled with the annotation’s interior color, if any
     */
    public static final Name LINE_END_NONE = new Name("None");
    /**
     * A circle filled with the annotation’s interior color, if any
     */
    public static final Name LINE_END_SQUARE = new Name("Square");
    /**
     * A diamond shape filled with the annotation’s interior color, if any
     */
    public static final Name LINE_END_CIRCLE = new Name("Circle");
    /**
     * Two short lines meeting in an acute angle to form an open arrowhead
     */
    public static final Name LINE_END_DIAMOND = new Name("Diamond");
    /**
     * Two short lines meeting in an acute angle as in the OpenArrow style and
     * connected by a third line to form a triangular closed arrowhead filled
     * with the annotation’s interior color, if any
     */
    public static final Name LINE_END_OPEN_ARROW = new Name("OpenArrow");
    /**
     * No line ending
     */
    public static final Name LINE_END_CLOSED_ARROW = new Name("ClosedArrow");

    protected Point2D startOfLine;
    protected Point2D endOfLine;
    protected Color interiorColor;

    // default line caps.
    protected Name startArrow = LINE_END_NONE;
    protected Name endArrow = LINE_END_NONE;

    public LineAnnotation(Library l, HashMap h) {
        super(l, h);
    }

    /**
     * Gets an instance of a LineAnnotation that has valid Object Reference.
     *
     * @param library document library
     * @param rect    bounding rectangle in user space
     * @return new LineAnnotation Instance.
     */
    public static LineAnnotation getInstance(Library library,
                                             Rectangle rect) {
        // state manager
        StateManager stateManager = library.getStateManager();

        // create a new entries to hold the annotation properties
        HashMap<Name, Object> entries = new HashMap<Name, Object>();
        // set default link annotation values.
        entries.put(Dictionary.TYPE_KEY, Annotation.TYPE_VALUE);
        entries.put(Dictionary.SUBTYPE_KEY, Annotation.SUBTYPE_LINE);
        // coordinates
        if (rect != null) {
            entries.put(Annotation.RECTANGLE_KEY,
                    PRectangle.getPRectangleVector(rect));
        } else {
            entries.put(Annotation.RECTANGLE_KEY, new Rectangle(10, 10, 50, 100));
        }
        // create the new instance
        LineAnnotation lineAnnotation = null;
        try {
            lineAnnotation = new LineAnnotation(library, entries);
            lineAnnotation.init();
            lineAnnotation.setPObjectReference(stateManager.getNewReferencNumber());
            lineAnnotation.setNew(true);
        } catch (InterruptedException e) {
            logger.fine("Line annotation instance creation was interrupted");
        }
        return lineAnnotation;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void drawLineStart(Graphics2D g, Name lineEnding,
                                     Point2D startOfLine, Point2D endOfLine,
                                     Color lineColor, Color interiorColor) {
        if (lineEnding.equals(LineAnnotation.LINE_END_OPEN_ARROW)) {
            drawOpenArrowStart(g, startOfLine, endOfLine, lineColor, interiorColor);
        } else if (lineEnding.equals(LineAnnotation.LINE_END_CLOSED_ARROW)) {
            drawClosedArrowStart(g, startOfLine, endOfLine, lineColor, interiorColor);
        } else if (lineEnding.equals(LineAnnotation.LINE_END_CIRCLE)) {
            drawCircle(g, startOfLine, startOfLine, endOfLine, lineColor, interiorColor);
        } else if (lineEnding.equals(LineAnnotation.LINE_END_DIAMOND)) {
            drawDiamond(g, startOfLine, startOfLine, endOfLine, lineColor, interiorColor);
        } else if (lineEnding.equals(LineAnnotation.LINE_END_SQUARE)) {
            drawSquare(g, startOfLine, startOfLine, endOfLine, lineColor, interiorColor);
        }
    }

    public static void drawLineEnd(Graphics2D g, Name lineEnding,
                                   Point2D startOfLine, Point2D endOfLine,
                                   Color lineColor, Color interiorColor) {
        if (lineEnding.equals(LineAnnotation.LINE_END_OPEN_ARROW)) {
            drawOpenArrowEnd(g, startOfLine, endOfLine, lineColor, interiorColor);
        } else if (lineEnding.equals(LineAnnotation.LINE_END_CLOSED_ARROW)) {
            drawClosedArrowEnd(g, startOfLine, endOfLine, lineColor, interiorColor);
        } else if (lineEnding.equals(LineAnnotation.LINE_END_CIRCLE)) {
            drawCircle(g, endOfLine, startOfLine, endOfLine, lineColor, interiorColor);
        } else if (lineEnding.equals(LineAnnotation.LINE_END_DIAMOND)) {
            drawDiamond(g, endOfLine, startOfLine, endOfLine, lineColor, interiorColor);
        } else if (lineEnding.equals(LineAnnotation.LINE_END_SQUARE)) {
            drawSquare(g, endOfLine, startOfLine, endOfLine, lineColor, interiorColor);
        }
    }

    public static void circleDrawOps(Shapes shapes,
                                     Point2D point, Point2D start,
                                     Point2D end, Color lineColor,
                                     Color internalColor) {
        AffineTransform af = createRotation(point, start, end);
        shapes.add(new TransformDrawCmd(af));
        shapes.add(new ColorDrawCmd(lineColor));
        shapes.add(new ShapeDrawCmd(createCircleEnd()));
        shapes.add(new FillDrawCmd());
    }

    private static Shape createCircleEnd() {
        return new Ellipse2D.Double(-4, -4, 8, 8);
    }

    private static void drawCircle(Graphics2D g, Point2D point,
                                   Point2D startOfLine, Point2D endOfLine,
                                   Color lineColor, Color interiorColor) {
        AffineTransform oldAf = g.getTransform();
        AffineTransform af = createRotation(point, startOfLine, endOfLine);
        AffineTransform gAf = g.getTransform();
        gAf.concatenate(af);
        g.setTransform(gAf);
        g.setColor(lineColor);
        g.fill(createCircleEnd());
        g.setTransform(oldAf);
    }

    public static void diamondDrawOps(Shapes shapes,
                                      Point2D point, Point2D start,
                                      Point2D end, Color lineColor,
                                      Color internalColor) {
        AffineTransform tx = new AffineTransform();
        Line2D.Double line = new Line2D.Double(start, end);
        tx.setToIdentity();
        double angle = Math.atan2(line.y2 - line.y1, line.x2 - line.x1);
        tx.translate(point.getX(), point.getY());
        tx.rotate(angle - (Math.PI / 4));

        AffineTransform af = createRotation(point, start, end);
        shapes.add(new TransformDrawCmd(af));
        shapes.add(new ColorDrawCmd(lineColor));
        shapes.add(new ShapeDrawCmd(createSquareEnd()));
        shapes.add(new FillDrawCmd());
    }

    private static void drawDiamond(Graphics2D g, Point2D point,
                                    Point2D startOfLine, Point2D endOfLine,
                                    Color lineColor, Color interiorColor) {
        AffineTransform oldAf = g.getTransform();
        AffineTransform tx = new AffineTransform();
        Line2D.Double line = new Line2D.Double(startOfLine, endOfLine);
        tx.setToIdentity();
        double angle = Math.atan2(line.y2 - line.y1, line.x2 - line.x1);
        tx.translate(point.getX(), point.getY());
        // quarter rotation
        tx.rotate(angle - (Math.PI / 4));
        AffineTransform gAf = g.getTransform();
        gAf.concatenate(tx);
        g.setTransform(gAf);
        g.setColor(lineColor);
        g.fill(createSquareEnd());
        g.setTransform(oldAf);
    }

    public static void squareDrawOps(Shapes shapes,
                                     Point2D point, Point2D start,
                                     Point2D end, Color lineColor,
                                     Color internalColor) {
        AffineTransform af = createRotation(point, start, end);
        shapes.add(new TransformDrawCmd(af));
        shapes.add(new ColorDrawCmd(lineColor));
        shapes.add(new ShapeDrawCmd(createSquareEnd()));
        shapes.add(new FillDrawCmd());
    }

    private static Shape createSquareEnd() {
        return new Rectangle2D.Double(-4, -4, 8, 8);
    }

    private static void drawSquare(Graphics2D g, Point2D point,
                                   Point2D startOfLine, Point2D endOfLine,
                                   Color lineColor, Color interiorColor) {
        AffineTransform oldAf = g.getTransform();
        AffineTransform af = createRotation(point, startOfLine, endOfLine);
        AffineTransform gAf = g.getTransform();
        gAf.concatenate(af);
        g.setTransform(gAf);
        g.setColor(lineColor);
        g.fill(createSquareEnd());
        g.setTransform(oldAf);
    }

    public static void openArrowEndDrawOps(Shapes shapes,
                                           Point2D start, Point2D end,
                                           Color lineColor, Color internalColor) {
        AffineTransform af = createRotation(end, start, end);
        shapes.add(new TransformDrawCmd(af));
        shapes.add(new ColorDrawCmd(lineColor));
        shapes.add(new ShapeDrawCmd(createOpenArrowEnd()));
        shapes.add(new DrawDrawCmd());
    }

    private static Shape createOpenArrowEnd() {
        GeneralPath arrowHead = new GeneralPath();
        arrowHead.moveTo(0, 0);
        arrowHead.lineTo(5, -10);
        arrowHead.moveTo(0, 0);
        arrowHead.lineTo(-5, -10);
        arrowHead.closePath();
        return arrowHead;
    }

    private static void drawOpenArrowEnd(Graphics2D g,
                                         Point2D startOfLine, Point2D endOfLine,
                                         Color lineColor, Color interiorColor) {
        Shape arrowHead = createOpenArrowEnd();
        AffineTransform oldAf = g.getTransform();
        AffineTransform af = createRotation(endOfLine, startOfLine, endOfLine);
        AffineTransform gAf = g.getTransform();
        gAf.concatenate(af);
        g.setTransform(gAf);
        g.setColor(lineColor);
        g.draw(arrowHead);
        g.setTransform(oldAf);
    }

    public static void openArrowStartDrawOps(Shapes shapes,
                                             Point2D start, Point2D end,
                                             Color lineColor, Color internalColor) {
        AffineTransform af = createRotation(start, start, end);
        shapes.add(new TransformDrawCmd(af));
        shapes.add(new ColorDrawCmd(lineColor));
        shapes.add(new ShapeDrawCmd(createOpenArrowStart()));
        shapes.add(new DrawDrawCmd());
    }

    private static Shape createOpenArrowStart() {
        GeneralPath arrowHead = new GeneralPath();
        arrowHead.moveTo(0, 0);
        arrowHead.lineTo(5, 10);
        arrowHead.moveTo(0, 0);
        arrowHead.lineTo(-5, 10);
        arrowHead.closePath();
        return arrowHead;
    }

    private static void drawOpenArrowStart(Graphics2D g,
                                           Point2D startOfLine, Point2D endOfLine,
                                           Color lineColor, Color interiorColor) {
        Shape arrowHead = createOpenArrowStart();
        AffineTransform oldAf = g.getTransform();
        AffineTransform af = createRotation(startOfLine, startOfLine, endOfLine);
        AffineTransform gAf = g.getTransform();
        gAf.concatenate(af);
        g.setTransform(gAf);
        g.setColor(lineColor);
        g.draw(arrowHead);
        g.setTransform(oldAf);
    }

    public static void closedArrowStartDrawOps(Shapes shapes,
                                               Point2D start, Point2D end,
                                               Color lineColor, Color internalColor) {
        AffineTransform af = createRotation(start, start, end);
        shapes.add(new TransformDrawCmd(af));
        if (internalColor != null) {
            shapes.add(new ColorDrawCmd(internalColor));
            shapes.add(new ShapeDrawCmd(createClosedArrowStart()));
            shapes.add(new FillDrawCmd());
        }
        shapes.add(new ColorDrawCmd(lineColor));
        shapes.add(new ShapeDrawCmd(createClosedArrowStart()));
        shapes.add(new DrawDrawCmd());
    }

    private static Shape createClosedArrowStart() {
        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(0, -5);
        arrowHead.addPoint(-5, 5);
        arrowHead.addPoint(5, 5);
        return arrowHead;
    }

    private static void drawClosedArrowStart(Graphics2D g,
                                             Point2D startOfLine, Point2D endOfLine,
                                             Color lineColor, Color interiorColor) {
        Shape arrowHead = createClosedArrowStart();
        AffineTransform oldAf = g.getTransform();
        AffineTransform af = createRotation(startOfLine, startOfLine, endOfLine);
        AffineTransform gAf = g.getTransform();
        gAf.concatenate(af);
        g.setTransform(gAf);
        if (interiorColor != null) {
            g.setColor(interiorColor);
            g.fill(arrowHead);
        }
        g.setColor(lineColor);
        g.draw(arrowHead);
        g.setTransform(oldAf);
    }

    public static void closedArrowEndDrawOps(Shapes shapes,
                                             Point2D start, Point2D end,
                                             Color lineColor, Color internalColor) {
        AffineTransform af = createRotation(end, start, end);
        shapes.add(new TransformDrawCmd(af));
        if (internalColor != null) {
            shapes.add(new ColorDrawCmd(internalColor));
            shapes.add(new ShapeDrawCmd(createClosedArrowEnd()));
            shapes.add(new FillDrawCmd());
        }
        shapes.add(new ColorDrawCmd(lineColor));
        shapes.add(new ShapeDrawCmd(createClosedArrowEnd()));
        shapes.add(new DrawDrawCmd());
    }

    private static Shape createClosedArrowEnd() {
        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(0, 5);
        arrowHead.addPoint(-5, -5);
        arrowHead.addPoint(5, -5);
        return arrowHead;
    }

    private static void drawClosedArrowEnd(Graphics2D g,
                                           Point2D startOfLine, Point2D endOfLine,
                                           Color lineColor, Color interiorColor) {
        Shape arrowHead = createClosedArrowEnd();
        AffineTransform oldAf = g.getTransform();
        AffineTransform af = createRotation(endOfLine, startOfLine, endOfLine);
        AffineTransform gAf = g.getTransform();
        gAf.concatenate(af);
        g.setTransform(gAf);
        if (interiorColor != null) {
            g.setColor(interiorColor);
            g.fill(arrowHead);
        }
        g.setColor(lineColor);
        g.draw(arrowHead);
        g.setTransform(oldAf);
    }

    private static AffineTransform createRotation(Point2D point,
                                                  Point2D startOfLine,
                                                  Point2D endOfLine) {
        AffineTransform tx = new AffineTransform();
        Line2D.Double line = new Line2D.Double(startOfLine, endOfLine);
        tx.setToIdentity();
        double angle = Math.atan2(line.y2 - line.y1, line.x2 - line.x1);
        tx.translate(point.getX(), point.getY());
        tx.rotate(angle - (Math.PI / 2));
        return tx;
    }

    @SuppressWarnings("unchecked")
    public void init() throws InterruptedException {
        super.init();
        // line points
        List<Number> value = library.getArray(entries, L_KEY);
        if (value != null) {
            startOfLine = new Point2D.Float(value.get(0).floatValue(), value.get(1).floatValue());
            endOfLine = new Point2D.Float(value.get(2).floatValue(), value.get(3).floatValue());
        }

        // line ends.
        List value2 = library.getArray(entries, LE_KEY);
        if (value2 != null) {
            startArrow = (Name) value2.get(0);
            endArrow = (Name) value2.get(1);
        }

        // parse out interior colour, specific to link annotations.
        interiorColor = null; // we default to black but probably should be null
        List C = (List) getObject(IC_KEY);
        // parse thought rgb colour.
        if (C != null && C.size() >= 3) {
            float red = ((Number) C.get(0)).floatValue();
            float green = ((Number) C.get(1)).floatValue();
            float blue = ((Number) C.get(2)).floatValue();
            red = Math.max(0.0f, Math.min(1.0f, red));
            green = Math.max(0.0f, Math.min(1.0f, green));
            blue = Math.max(0.0f, Math.min(1.0f, blue));
            interiorColor = new Color(red, green, blue);
        }

        // check if there is an AP entry, if no generate the shapes data
        // from the other properties.
        if (!hasAppearanceStream() && startOfLine != null && endOfLine != null) {
            Object tmp = getObject(RECTANGLE_KEY);
            Rectangle2D.Float rectangle = null;
            if (tmp instanceof List) {
                rectangle = library.getRectangle(entries, RECTANGLE_KEY);
            }
            if (rectangle != null) {
                setBBox(rectangle.getBounds());
            }
            resetAppearanceStream(new AffineTransform());
        }
        // try and generate an appearance stream.
        resetNullAppearanceStream();
    }

    /**
     * Resets the annotations appearance stream.
     */
    public void resetAppearanceStream(double dx, double dy, AffineTransform pageTransform) {

        // nothing to reset,  creating new annotation.
        if (startOfLine == null || endOfLine == null) {
            return;
        }

        Appearance appearance = appearances.get(currentAppearance);
        AppearanceState appearanceState = appearance.getSelectedAppearanceState();
        AffineTransform matrix = appearanceState.getMatrix();
        // reset transform and shapes.
        appearanceState.setMatrix(new AffineTransform());
        appearanceState.setShapes(new Shapes());

        // adjust the line's start and end points for any potential move
        AffineTransform af = new AffineTransform();
        af.setToTranslation(dx * pageTransform.getScaleX(), -dy * pageTransform.getScaleY());
        af.transform(startOfLine, startOfLine);
        af.transform(endOfLine, endOfLine);
        setStartOfLine(startOfLine);
        setEndOfLine(endOfLine);

        Rectangle2D bbox = appearanceState.getBbox();
        bbox.setRect(userSpaceRectangle.x, userSpaceRectangle.y, userSpaceRectangle.width, userSpaceRectangle.height);
        setUserSpaceRectangle(userSpaceRectangle);

        setModifiedDate(PDate.formatDateTime(new Date()));

        // draw the basic line.
        Stroke stroke = getBorderStyleStroke();
        GeneralPath line = new GeneralPath();
        line.moveTo((float) startOfLine.getX(), (float) startOfLine.getY());
        line.lineTo((float) endOfLine.getX(), (float) endOfLine.getY());
        line.closePath();

        Shapes shapes = appearanceState.getShapes();
//        shapes.add(new GraphicsStateCmd(EXT_GSTATE_NAME));
        shapes.add(new AlphaDrawCmd(
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity)));
        shapes.add(new ShapeDrawCmd(line));
        shapes.add(new StrokeDrawCmd(stroke));
        shapes.add(new ColorDrawCmd(color));
        shapes.add(new DrawDrawCmd());

        // check for a ending end cap.
        if (startArrow.equals(LineAnnotation.LINE_END_OPEN_ARROW)) {
            openArrowStartDrawOps(
                    shapes, startOfLine, endOfLine, color, interiorColor);
        } else if (startArrow.equals(LineAnnotation.LINE_END_CLOSED_ARROW)) {
            closedArrowStartDrawOps(
                    shapes, startOfLine, endOfLine, color, interiorColor);
        } else if (startArrow.equals(LineAnnotation.LINE_END_CIRCLE)) {
            circleDrawOps(
                    shapes, startOfLine, startOfLine, endOfLine, color, interiorColor);
        } else if (startArrow.equals(LineAnnotation.LINE_END_DIAMOND)) {
            diamondDrawOps(
                    shapes, startOfLine, startOfLine, endOfLine, color, interiorColor);
        } else if (startArrow.equals(LineAnnotation.LINE_END_SQUARE)) {
            squareDrawOps(
                    shapes, startOfLine, startOfLine, endOfLine, color, interiorColor);
        }
        // check for a starting end cap.
        if (endArrow.equals(LineAnnotation.LINE_END_OPEN_ARROW)) {
            openArrowEndDrawOps(
                    shapes, startOfLine, endOfLine, color, interiorColor);
        } else if (endArrow.equals(LineAnnotation.LINE_END_CLOSED_ARROW)) {
            closedArrowEndDrawOps(
                    shapes, startOfLine, endOfLine, color, interiorColor);
        } else if (endArrow.equals(LineAnnotation.LINE_END_CIRCLE)) {
            circleDrawOps(
                    shapes, endOfLine, startOfLine, endOfLine, color, interiorColor);
        } else if (endArrow.equals(LineAnnotation.LINE_END_DIAMOND)) {
            diamondDrawOps(
                    shapes, endOfLine, startOfLine, endOfLine, color, interiorColor);
        } else if (endArrow.equals(LineAnnotation.LINE_END_SQUARE)) {
            squareDrawOps(
                    shapes, endOfLine, startOfLine, endOfLine, color, interiorColor);
        }
        shapes.add(new AlphaDrawCmd(
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)));

        // remove appearance stream if it exists on an existing edit.
        entries.remove(APPEARANCE_STREAM_KEY);

        // we don't write out an appearance stream for line annotation, we just regenerate it from properties

        // mark the change.
        StateManager stateManager = library.getStateManager();
        stateManager.addChange(new PObject(this, this.getPObjectReference()));
    }

    public Point2D getStartOfLine() {
        return startOfLine;
    }

    public void setStartOfLine(Point2D startOfLine) {
        this.startOfLine = startOfLine;
    }

    public Point2D getEndOfLine() {
        return endOfLine;
    }

    public void setEndOfLine(Point2D endOfLine) {
        this.endOfLine = endOfLine;
        List<Number> pointArray = new ArrayList<Number>(4);
        pointArray.add((float) startOfLine.getX());
        pointArray.add((float) startOfLine.getY());
        pointArray.add((float) endOfLine.getX());
        pointArray.add((float) endOfLine.getY());
        entries.put(L_KEY, pointArray);
    }

    public Color getInteriorColor() {
        return interiorColor;
    }

    public void setInteriorColor(Color interiorColor) {
        this.interiorColor = interiorColor;
        float[] compArray = new float[3];
        this.interiorColor.getColorComponents(compArray);
        List<Float> colorValues = new ArrayList<Float>(compArray.length);
        for (float comp : compArray) {
            colorValues.add(comp);
        }
        entries.put(IC_KEY, colorValues);
    }

    public Name getStartArrow() {
        return startArrow;
    }

    public void setStartArrow(Name startArrow) {
        this.startArrow = startArrow;
        List<Name> endNameArray = new ArrayList<Name>(2);
        endNameArray.add(startArrow);
        endNameArray.add(endArrow);
        entries.put(LE_KEY, endNameArray);
    }

    public Name getEndArrow() {
        return endArrow;
    }

    public void setEndArrow(Name endArrow) {
        this.endArrow = endArrow;
        List<Name> endNameArray = new ArrayList<Name>(2);
        endNameArray.add(startArrow);
        endNameArray.add(endArrow);
        entries.put(LE_KEY, endNameArray);
    }
}
