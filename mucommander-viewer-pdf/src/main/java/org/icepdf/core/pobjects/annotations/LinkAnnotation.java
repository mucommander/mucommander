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
import org.icepdf.core.util.Library;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.logging.Logger;


/**
 * <h2>Refer to: 8.4.5 Annotation Types</h2>
 * <p/>
 * <table border=1> <tr> <td>Key</td> <td>Type</td> <td>Value</td> </tr> <tr>
 * <td><b>Subtype</b></td> <td>name</td> <td><i>(Required)</i> The type of
 * annotation that this dictionary describes; must be <b>Link</b> for a link
 * annotation.</td> </tr> <tr> <td><b>Dest</b></td> <td>array, name, or
 * string</td> <td><i>(Optional; not permitted if an <b>A</b> entry is
 * present)</i> A destination to be displayed when the annotation is activated
 * (see Section 8.2.1, "Destinations"; see also implementation note 90 in
 * Appendix H).</td> </tr> <tr> <td><b>H</b></td> <td>name</td>
 * <td><i>(Optional; PDF 1.2)</i> The annotation's <i>highlighting mode</i>, the
 * visual effect to be used when the mouse button is pressed or held down inside
 * its active area: <table border=0> <tr> <td>N</td> <td>(None) No
 * highlighting.</td> </tr> <tr> <td>I</td> <td>(Invert) Invert the contents of
 * the annotation rectangle.</td> </tr> <tr> <td>O</td> <td>(Outline) Invert the
 * annotation's border.</td> </tr> <tr> <td>P</td> <td>(Push) Display the
 * annotation as if it were being pushed below the surface of the page; see
 * implementation note 91 in Appendix H.<br> Acrobat viewer displays the link
 * appearance with bevel border, ignoring any down appearance.</td> </tr>
 * </table>Default value: I.</td> </tr> <tr> <td><b>QuadPoints</b></td>
 * <td>array</td> <td><i>(Optional; PDF 1.6)</i> An array of 8 x n numbers
 * specifying the coordinates of n quadrilaterals in default user space that
 * comprise the region in which the link should be activated. The coordinates
 * for each quadrilateral are given in the order<br> x1 y1 x2 y2 x3 y3 x4 y4<br>
 * specifying the four vertices of the quadrilateral in counterclockwise order.
 * For orientation purposes, such as when applying an underline border style,
 * the bottom of a quadrilateral is the line formed by (x1, y1) and (x2, y2). If
 * this entry is not present or the viewer application does not recognize it,
 * the region specified by the <b>Rect</b> entry should be used.
 * <b>QuadPoints</b> should be ignored if any coordinate in the array lies
 * outside the region specified by <b>Rect</b>.</td> </tr> </table>
 *
 * @author Mark Collette
 * @since 2.5
 */
public class LinkAnnotation extends Annotation {

    private static final Logger logger =
            Logger.getLogger(LinkAnnotation.class.toString());

    /**
     * Key used to indicate highlight mode.
     */
    public static final Name DESTINATION_KEY = new Name("Dest");

    /**
     * Key used to indcate highlight mode.
     */
    public static final Name HIGHLIGHT_MODE_KEY = new Name("H");

    /**
     * Indicates that the annotation has no highlight effect.
     */
    public static final Name HIGHLIGHT_NONE = new Name("N");

    /**
     * Indicates that the annotation rectangle colours should be inverted for
     * its highlight effect.
     */
    public static final Name HIGHLIGHT_INVERT = new Name("I");

    /**
     * Indicates that the annotation rectangle border should be inverted for its
     * highlight effect.
     */
    public static final Name HIGHLIGHT_OUTLINE = new Name("O");

    /**
     * Indicates that the annotation rectangle border should be pushed below the
     * surface of th page.
     */
    public static final Name HIGHLIGHT_PUSH = new Name("P");

    /**
     * Creates a new instance of a LinkAnnotation.
     *
     * @param l document library.
     * @param h dictionary entries.
     */
    public LinkAnnotation(Library l, HashMap h) {
        super(l, h);
    }

    /**
     * Gets an instance of a LinkAnnotation that has valid Object Reference.
     *
     * @param library document library
     * @param rect    bounding rectangle in user space
     * @return new LinkAnnotation Instance.
     */
    public static LinkAnnotation getInstance(Library library,
                                             Rectangle rect) {
        // state manager
        StateManager stateManager = library.getStateManager();

        // create a new entries to hold the annotation properties
        HashMap<Name, Object> entries = new HashMap<Name, Object>();
        // set default link annotation values.
        entries.put(Dictionary.TYPE_KEY, Annotation.TYPE_VALUE);
        entries.put(Dictionary.SUBTYPE_KEY, Annotation.SUBTYPE_LINK);
        // coordinates
        if (rect != null) {
            entries.put(Annotation.RECTANGLE_KEY,
                    PRectangle.getPRectangleVector(rect));
        } else {
            entries.put(Annotation.RECTANGLE_KEY, new Rectangle(10, 10, 50, 100));
        }
        // write out the default highlight state.
        entries.put(HIGHLIGHT_MODE_KEY, HIGHLIGHT_INVERT);

        // create the new instance
        LinkAnnotation linkAnnotation = null;
        try {
            linkAnnotation = new LinkAnnotation(library, entries);
            linkAnnotation.init();
            linkAnnotation.setPObjectReference(stateManager.getNewReferencNumber());
            linkAnnotation.setNew(true);

            // set default flags.
            linkAnnotation.setFlag(Annotation.FLAG_READ_ONLY, false);
            linkAnnotation.setFlag(Annotation.FLAG_NO_ROTATE, false);
            linkAnnotation.setFlag(Annotation.FLAG_NO_ZOOM, false);
            linkAnnotation.setFlag(Annotation.FLAG_PRINT, true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.fine("Link annotation instance creation was interrupted");
        }

        return linkAnnotation;
    }

    public void init() throws InterruptedException {
        super.init();
        // try and generate an appearance stream.
        resetNullAppearanceStream();
    }

    /**
     * <p>Gets the link annotations highlight mode (visual effect)taht should
     * be displayed when the mouse button is pressed or held down inside it's
     * active area.</p>
     *
     * @return one of the predefined highlight effects, HIGHLIGHT_NONE,
     * HIGHLIGHT_OUTLINE or HIGHLIGHT_PUSH.
     */
    public Name getHighlightMode() {
        Object possibleName = getObject(HIGHLIGHT_MODE_KEY);
        if (possibleName instanceof Name) {
            Name name = (Name) possibleName;
            if (HIGHLIGHT_NONE.equals(name)) {
                return HIGHLIGHT_NONE;
            } else if (HIGHLIGHT_OUTLINE.equals(name)) {
                return HIGHLIGHT_OUTLINE;
            } else if (HIGHLIGHT_PUSH.equals(name)) {
                return HIGHLIGHT_PUSH;
            }
        }
        return HIGHLIGHT_INVERT;
    }

    /**
     * A destination to be displayed when the annotation is ativated.  Only
     * permitted if an A entry is not present.
     *
     * @return annotation target destination, null if not present in
     * annotation.
     */
    public Destination getDestination() {
        Object obj = getObject(DESTINATION_KEY);
        if (obj != null) {
            return new Destination(library, obj);
        }
        return null;
    }

    @Override
    public void resetAppearanceStream(double dx, double dy, AffineTransform pageTransform) {

    }
}
