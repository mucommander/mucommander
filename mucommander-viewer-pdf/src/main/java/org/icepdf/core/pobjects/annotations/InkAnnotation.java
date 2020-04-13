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
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * An ink annotation (PDF 1.3) represents a freehand “scribble” composed of one
 * or more disjoint paths. When opened, it shall display a pop-up window
 * containing the text of the associated note. Table 182 shows the annotation
 * dictionary entries specific to this type of annotation.
 *
 * @since 5.0
 */
public class InkAnnotation extends MarkupAnnotation {

    private static final Logger logger =
            Logger.getLogger(InkAnnotation.class.toString());

    /**
     * (Required) An array of n arrays, each representing a stroked path. Each
     * array shall be a series of alternating horizontal and vertical coordinates
     * in default user space, specifying points along the path. When drawn, the
     * points shall be connected by straight lines or curves in an
     * implementation-dependent way
     */
    public static final Name INK_LIST_KEY = new Name("InkList");

    protected Shape inkPath;

    public InkAnnotation(Library l, HashMap h) {
        super(l, h);
    }

    @SuppressWarnings("unchecked")
    public void init() throws InterruptedException{
        super.init();
        // look for an ink list
        List<List<Number>> inkLists = library.getArray(entries, INK_LIST_KEY);
        GeneralPath inkPaths = new GeneralPath();
        if (inkLists != null) {
            inkPath = new GeneralPath();
            for (List<Number> inkList : inkLists) {
                GeneralPath inkPath = null;
                for (int i = 0, max = inkList.size() - 1; i < max; i += 2) {
                    if (inkPath == null) {
                        inkPath = new GeneralPath();
                        inkPath.moveTo(inkList.get(i).floatValue(), inkList.get(i + 1).floatValue());
                    } else {
                        inkPath.lineTo(inkList.get(i).floatValue(), inkList.get(i + 1).floatValue());
                    }
                }
                inkPaths.append(inkPath, false);
            }
        }
        this.inkPath = inkPaths;
        if (!hasAppearanceStream() && inkPath != null) {
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
     * Converts the ink path back to an array of points.
     *
     * @param inkPath path to translate to an array
     * @return an array of an array of points.
     */
    private List<List<Float>> convertPathToArray(Shape inkPath) {
        List<List<Float>> inkLists = new ArrayList<List<Float>>();
        List<Float> segment = null;
        if (inkPath != null) {
            PathIterator pathIterator = inkPath.getPathIterator(null);
            float[] inkSegment = new float[6];
            int segmentType;
            while (!pathIterator.isDone()) {
                segmentType = pathIterator.currentSegment(inkSegment);
                if (segmentType == PathIterator.SEG_MOVETO) {
                    segment = new ArrayList<Float>();
                    segment.add(inkSegment[0]);
                    segment.add(inkSegment[1]);
                    inkLists.add(segment);
                } else if (segmentType == PathIterator.SEG_LINETO) {
                    segment.add(inkSegment[0]);
                    segment.add(inkSegment[1]);
                }
                pathIterator.next();
            }
        }
        return inkLists;
    }


    /**
     * Gets an instance of a InkAnnotation that has valid Object Reference.
     *
     * @param library document library
     * @param rect    bounding rectangle in user space
     * @return new InkAnnotation Instance.
     */
    public static InkAnnotation getInstance(Library library,
                                            Rectangle rect) {
        // state manager
        StateManager stateManager = library.getStateManager();

        // create a new entries to hold the annotation properties
        HashMap<Name, Object> entries = new HashMap<Name, Object>();
        // set default link annotation values.
        entries.put(Dictionary.TYPE_KEY, Annotation.TYPE_VALUE);
        entries.put(Dictionary.SUBTYPE_KEY, Annotation.SUBTYPE_INK);
        // coordinates
        if (rect != null) {
            entries.put(Annotation.RECTANGLE_KEY,
                    PRectangle.getPRectangleVector(rect));
        } else {
            entries.put(Annotation.RECTANGLE_KEY, new Rectangle(10, 10, 50, 100));
        }

        // create the new instance
        InkAnnotation inkAnnotation = null;
        try {
            inkAnnotation = new InkAnnotation(library, entries);
            inkAnnotation.setPObjectReference(stateManager.getNewReferencNumber());
            inkAnnotation.init();
            inkAnnotation.setNew(true);

            // set default flags.
            inkAnnotation.setFlag(Annotation.FLAG_READ_ONLY, false);
            inkAnnotation.setFlag(Annotation.FLAG_NO_ROTATE, false);
            inkAnnotation.setFlag(Annotation.FLAG_NO_ZOOM, false);
            inkAnnotation.setFlag(Annotation.FLAG_PRINT, true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.fine("Ink annotation instance creation was interrupted");
        }


        return inkAnnotation;
    }

    /**
     * Resets the annotations appearance stream.
     */
    public void resetAppearanceStream(double dx, double dy, AffineTransform pageSpace) {

        // setup clean shapes
        Appearance appearance = appearances.get(currentAppearance);
        AppearanceState appearanceState = appearance.getSelectedAppearanceState();
        appearanceState.setMatrix(new AffineTransform());
        appearanceState.setShapes(new Shapes());

        // update the circle for any dx/dy moves.
        AffineTransform af = new AffineTransform();
        af.setToTranslation(dx * pageSpace.getScaleX(), -dy * pageSpace.getScaleY());
        inkPath = af.createTransformedShape(inkPath);
        entries.put(INK_LIST_KEY, convertPathToArray(inkPath));

        Rectangle2D bbox = appearanceState.getBbox();
        bbox.setRect(userSpaceRectangle.x, userSpaceRectangle.y, userSpaceRectangle.width, userSpaceRectangle.height);
        setUserSpaceRectangle(userSpaceRectangle);

        // setup the AP stream.
        setModifiedDate(PDate.formatDateTime(new Date()));

        // save the stroke.
        if (borderStyle.getStrokeWidth() == 0) {
            borderStyle.setStrokeWidth(1);
        }
        Stroke stroke = getBorderStyleStroke();

        Shapes shapes = appearanceState.getShapes();
        // setup the space for the AP content stream.
        shapes.add(new GraphicsStateCmd(EXT_GSTATE_NAME));
        shapes.add(new AlphaDrawCmd(
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity)));
        shapes.add(new StrokeDrawCmd(stroke));
        shapes.add(new ColorDrawCmd(color));
        shapes.add(new ShapeDrawCmd(inkPath));
        shapes.add(new DrawDrawCmd());
        shapes.add(new AlphaDrawCmd(
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)));

        // remove appearance stream if it exists on an existing edit.
        entries.remove(APPEARANCE_STREAM_KEY);

        // we don't write out an appearance stream for ink annotation, we just regenerate it from properties

        // mark the change.
        StateManager stateManager = library.getStateManager();
        stateManager.addChange(new PObject(this, this.getPObjectReference()));

    }

    public Shape getInkPath() {
        return inkPath;
    }

    public void setInkPath(Shape inkPath) {
        this.inkPath = inkPath;
    }
}
