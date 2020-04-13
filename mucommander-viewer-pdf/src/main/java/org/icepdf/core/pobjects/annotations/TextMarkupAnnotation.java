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
import org.icepdf.core.util.ColorUtil;
import org.icepdf.core.util.Defs;
import org.icepdf.core.util.Library;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Text markup annotations shall appear as highlights, underlines, strikeouts
 * (all PDF 1.3), or jagged (“squiggly”) underlines (PDF 1.4) in the text of a
 * document. When opened, they shall display a pop-up window containing the text
 * of the associated note. Table 179 shows the annotation dictionary entries
 * specific to these types of annotations.
 *
 * @since 5.0
 */
public class TextMarkupAnnotation extends MarkupAnnotation {

    private static final Logger logger =
            Logger.getLogger(TextMarkupAnnotation.class.toString());

    public static final Name SUBTYPE_HIGHLIGHT = new Name("Highlight");
    public static final Name SUBTYPE_UNDERLINE = new Name("Underline");
    public static final Name SUBTYPE_SQUIGGLY = new Name("Squiggly");
    public static final Name SUBTYPE_STRIKE_OUT = new Name("StrikeOut");

    private static Color highlightColor;
    private static Color strikeOutColor;
    private static Color underlineColor;

    static {

        // sets annotation selected highlight colour
        try {
            String color = Defs.sysProperty(
                    "org.icepdf.core.views.page.annotation.textmarkup.highlight.color", "#ffff00");
            int colorValue = ColorUtil.convertColor(color);
            highlightColor =
                    new Color(colorValue >= 0 ? colorValue :
                            Integer.parseInt("ffff00", 16));
        } catch (NumberFormatException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Error reading Text Markup Annotation highlight colour");
            }
        }
        // sets annotation selected highlight colour
        try {
            String color = Defs.sysProperty(
                    "org.icepdf.core.views.page.annotation.textmarkup.strikeOut.color", "#ff0000");
            int colorValue = ColorUtil.convertColor(color);
            strikeOutColor =
                    new Color(colorValue >= 0 ? colorValue :
                            Integer.parseInt("ff0000", 16));
        } catch (NumberFormatException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Error reading Text Markup Annotation strike out colour");
            }
        }
        // sets annotation selected highlight colour
        try {
            String color = Defs.sysProperty(
                    "org.icepdf.core.views.page.annotation.textmarkup.underline.color", "#00ff00");
            int colorValue = ColorUtil.convertColor(color);
            underlineColor =
                    new Color(colorValue >= 0 ? colorValue :
                            Integer.parseInt("00ff00", 16));
        } catch (NumberFormatException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Error reading Text Markup Annotation underline colour");
            }
        }
    }

    /**
     * (Required) An array of 8 × n numbers specifying the coordinates of
     * n quadrilaterals in default user space. Each quadrilateral shall encompasses
     * a word or group of contiguous words in the text underlying the annotation.
     * The coordinates for each quadrilateral shall be given in the order
     * x1 y1 x2 y2 x3 y3 x4 y4
     * specifying the quadrilateral’s four vertices in counterclockwise order
     * (see Figure 64). The text shall be oriented with respect to the edge
     * connecting points (x1, y1) and (x2, y2).
     * <p/>
     * The annotation dictionary’s AP entry, if present, shall take precedence
     * over QuadPoints; see Table 168 and 12.5.5, “Appearance Streams.”
     */
    public static final Name KEY_QUAD_POINTS = new Name("QuadPoints");

    /**
     * Highlight transparency default
     */
    public static final int HIGHLIGHT_ALPHA = 80;


    /**
     * Converted Quad points.
     */
    private Shape[] quadrilaterals;

    private Color textMarkupColor;

    private GeneralPath markupPath;
    private ArrayList<Shape> markupBounds;

    /**
     * Creates a new instance of an TextMarkupAnnotation.
     *
     * @param l document library.
     * @param h dictionary entries.
     */
    public TextMarkupAnnotation(Library l, HashMap h) {
        super(l, h);
    }

    @SuppressWarnings("unchecked")
    public void init() throws InterruptedException {
        super.init();
        // collect the quad points.
        List<Number> quadPoints = library.getArray(entries, KEY_QUAD_POINTS);
        if (quadPoints != null) {
            int size = quadPoints.size() / 8;
            quadrilaterals = new Shape[size];
            GeneralPath shape;
            for (int i = 0, count = 0; i < size; i++, count += 8) {
                shape = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 4);
                shape.moveTo(quadPoints.get(count + 6).floatValue(), quadPoints.get(count + 7).floatValue());
                shape.lineTo(quadPoints.get(count + 4).floatValue(), quadPoints.get(count + 5).floatValue());
                shape.lineTo(quadPoints.get(count).floatValue(), quadPoints.get(count + 1).floatValue());
                shape.lineTo(quadPoints.get(count + 2).floatValue(), quadPoints.get(count + 3).floatValue());
                shape.closePath();
                quadrilaterals[i] = shape;
            }
        }
        if (SUBTYPE_HIGHLIGHT.equals(subtype)) {
            textMarkupColor = highlightColor;
        } else if (SUBTYPE_STRIKE_OUT.equals(subtype)) {
            textMarkupColor = strikeOutColor;
        } else if (SUBTYPE_UNDERLINE.equals(subtype)) {
            textMarkupColor = underlineColor;
        } else if (SUBTYPE_SQUIGGLY.equals(subtype)) {
            // not implemented
        }

        // for editing purposes grab anny shapes from the AP Stream and
        // store them as markupBounds and markupPath. This works ok but
        // perhaps a better way would be to reapply the bound box
        Appearance appearance = appearances.get(currentAppearance);
        AppearanceState appearanceState = appearance.getSelectedAppearanceState();
        Shapes shapes = appearanceState.getShapes();
        if (shapes != null) {
            markupBounds = new ArrayList<Shape>();
            markupPath = new GeneralPath();

            ShapeDrawCmd shapeDrawCmd;
            for (DrawCmd cmd : shapes.getShapes()) {
                if (cmd instanceof ShapeDrawCmd) {
                    shapeDrawCmd = (ShapeDrawCmd) cmd;
                    markupBounds.add(shapeDrawCmd.getShape());
                    markupPath.append(shapeDrawCmd.getShape(), false);
                }
            }

        }
        // try and generate an appearance stream.
        resetNullAppearanceStream();
    }

    /**
     * Gets an instance of a TextMarkupAnnotation that has valid Object Reference.
     *
     * @param library document library
     * @param rect    bounding rectangle in user space
     * @return new TextMarkupAnnotation Instance.
     */
    public static TextMarkupAnnotation getInstance(Library library,
                                                   Rectangle rect,
                                                   final Name subType) {
        // state manager
        StateManager stateManager = library.getStateManager();

        // create a new entries to hold the annotation properties
        HashMap<Name, Object> entries = new HashMap<Name, Object>();
        // set default link annotation values.
        entries.put(Dictionary.TYPE_KEY, Annotation.TYPE_VALUE);
        entries.put(Dictionary.SUBTYPE_KEY, subType);
        entries.put(Annotation.FLAG_KEY, 4);
        // coordinates
        if (rect != null) {
            entries.put(Annotation.RECTANGLE_KEY,
                    PRectangle.getPRectangleVector(rect));
        } else {
            entries.put(Annotation.RECTANGLE_KEY, new Rectangle(10, 10, 50, 100));
        }

        TextMarkupAnnotation textMarkupAnnotation =
                null;
        try {
            textMarkupAnnotation = new TextMarkupAnnotation(library, entries);
            textMarkupAnnotation.init();
            entries.put(NM_KEY,
                    new LiteralStringObject(String.valueOf(textMarkupAnnotation.hashCode())));
            textMarkupAnnotation.setPObjectReference(stateManager.getNewReferencNumber());
            textMarkupAnnotation.setNew(true);
            textMarkupAnnotation.setModifiedDate(PDate.formatDateTime(new Date()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.fine("Text markup annotation instance creation was interrupted");
        }
        return textMarkupAnnotation;
    }


    public static boolean isTextMarkupAnnotation(Name subType) {
        return SUBTYPE_HIGHLIGHT.equals(subType) ||
                SUBTYPE_UNDERLINE.equals(subType) ||
                SUBTYPE_SQUIGGLY.equals(subType) ||
                SUBTYPE_STRIKE_OUT.equals(subType);
    }

    /**
     * Resets the annotations appearance stream.
     */
    public void resetAppearanceStream(double dx, double dy, AffineTransform pageTransform) {

        // check if we have anything to reset.
        if (markupBounds == null) {
            return;
        }

        Appearance appearance = appearances.get(currentAppearance);
        AppearanceState appearanceState = appearance.getSelectedAppearanceState();
        appearanceState.setShapes(new Shapes());

        Rectangle2D bbox = appearanceState.getBbox();
        AffineTransform matrix = appearanceState.getMatrix();
        Shapes shapes = appearanceState.getShapes();

        // setup the stroke from the border settings.
        BasicStroke stroke = new BasicStroke(1f);
        shapes.add(new StrokeDrawCmd(stroke));
        shapes.add(new GraphicsStateCmd(EXT_GSTATE_NAME));
        shapes.add(new AlphaDrawCmd(
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity)));
        if (SUBTYPE_HIGHLIGHT.equals(subtype)) {
            shapes.add(new ShapeDrawCmd(markupPath));
            shapes.add(new ColorDrawCmd(textMarkupColor));
            shapes.add(new FillDrawCmd());
            shapes.add(new AlphaDrawCmd(
                    AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)));
        } else if (SUBTYPE_STRIKE_OUT.equals(subtype)) {
            for (Shape shape : markupBounds) {
                // calculate the line that will stroke the bounds
                GeneralPath strikeOutPath = new GeneralPath();
                Rectangle2D bound = shape.getBounds2D();
                float y = (float) (bound.getMinY() + (bound.getHeight() / 2));
                strikeOutPath.moveTo((float) bound.getMinX(), y);
                strikeOutPath.lineTo((float) bound.getMaxX(), y);
                strikeOutPath.closePath();
                shapes.add(new ShapeDrawCmd(strikeOutPath));
                shapes.add(new ColorDrawCmd(textMarkupColor));
                shapes.add(new DrawDrawCmd());
            }
        } else if (SUBTYPE_UNDERLINE.equals(subtype)) {
            for (Shape shape : markupBounds) {
                // calculate the line that will stroke the bounds
                GeneralPath underlinePath = new GeneralPath();
                Rectangle2D bound = shape.getBounds2D();
                underlinePath.moveTo((float) bound.getMinX(), (float) bound.getMinY());
                underlinePath.lineTo((float) bound.getMaxX(), (float) bound.getMinY());
                underlinePath.closePath();
                shapes.add(new ShapeDrawCmd(underlinePath));
                shapes.add(new ColorDrawCmd(textMarkupColor));
                shapes.add(new DrawDrawCmd());
            }
        } else if (SUBTYPE_SQUIGGLY.equals(subtype)) {
            // not implemented,  need to create a custom stroke or
            // build out a custom line move.
        }
        shapes.add(new AlphaDrawCmd(
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)));

        // create the quad points
        List<Float> quadPoints = new ArrayList<Float>();
        if (markupBounds != null) {
            Rectangle2D bounds;
            // build out the square in quadrant 1.
            for (Shape shape : markupBounds) {
                bounds = shape.getBounds2D();

                quadPoints.add((float) bounds.getX());
                quadPoints.add((float) (bounds.getY() + bounds.getHeight()));

                quadPoints.add((float) (bounds.getX() + bounds.getWidth()));
                quadPoints.add((float) (bounds.getY() + bounds.getHeight()));

                quadPoints.add((float) (bounds.getX()));
                quadPoints.add((float) (bounds.getY()));

                quadPoints.add((float) (bounds.getX() + bounds.getWidth()));
                quadPoints.add((float) (bounds.getY()));
            }
        }
        entries.put(KEY_QUAD_POINTS, quadPoints);
        setModifiedDate(PDate.formatDateTime(new Date()));

        // update the appearance stream
        // create/update the appearance stream of the xObject.
        Form form = updateAppearanceStream(shapes, bbox, matrix,
                PostScriptEncoder.generatePostScript(shapes.getShapes()));
        generateExternalGraphicsState(form, opacity);
    }

    @Override
    protected void renderAppearanceStream(Graphics2D g) {
        Appearance appearance = appearances.get(currentAppearance);
        AppearanceState appearanceState = appearance.getSelectedAppearanceState();
        Shapes shapes = appearanceState.getShapes();

        // Appearance stream takes precedence over the quad points.
        if (shapes != null) {
            super.renderAppearanceStream(g);
        }
        // draw the quad points.
        else if (quadrilaterals != null) {

            // check to see if we are painting highlight annotations.
            // if so we add some transparency to the context.
            if (subtype != null && SUBTYPE_HIGHLIGHT.equals(subtype)) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .30f));
                // remove other alpha defs from painting
                if (shapes != null) {
                    shapes.setPaintAlpha(false);
                }
            }

            Object tmp = getObject(RECTANGLE_KEY);
            Rectangle2D.Float rectangle = null;
            if (tmp instanceof List) {
                rectangle = library.getRectangle(entries, RECTANGLE_KEY);
            }

            // get the current position of the userSpaceRectangle
            Rectangle2D.Float origRect = getUserSpaceRectangle();
            // build the transform to go back to users space
            AffineTransform af = g.getTransform();
            double x = rectangle.getX() - origRect.getX();
            double y = rectangle.getY() - origRect.getY();
            af.translate(-origRect.getX(), -origRect.getY());
            g.setTransform(af);
            g.setColor(highlightColor);
            AffineTransform af2 = new AffineTransform();
            af2.translate(-x, -y);
            for (Shape shape : quadrilaterals) {
                g.fill(af2.createTransformedShape(shape));
            }
            // revert the alpha value.
            if (subtype != null && SUBTYPE_HIGHLIGHT.equals(subtype)) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                // remove other alpha defs from painting
                if (shapes != null) {
                    shapes.setPaintAlpha(true);
                }
            }
        }
    }

    public void setMarkupPath(GeneralPath markupPath) {
        this.markupPath = markupPath;
    }

    public GeneralPath getMarkupPath() {
        return markupPath;
    }

    public void setMarkupBounds(ArrayList<Shape> markupBounds) {
        this.markupBounds = markupBounds;
    }

    public Color getTextMarkupColor() {
        return textMarkupColor;
    }

    public void setTextMarkupColor(Color textMarkupColor) {
        this.textMarkupColor = textMarkupColor;
    }

}
