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

import org.icepdf.core.pobjects.Dictionary;
import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.PRectangle;
import org.icepdf.core.pobjects.StateManager;
import org.icepdf.core.util.Library;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * A pop-up annotation (PDF 1.3) displays text in a pop-up window for entry and
 * editing. It shall not appear alone but is associated with a markup annotation,
 * its parent annotation, and shall be used for editing the parent’s text. It
 * shall have no appearance stream or associated actions of its own and shall be
 * identified by the Popup entry in the parent’s annotation dictionary
 * (see Table 174). Table 183 shows the annotation dictionary entries specific to
 * this type of annotation.A pop-up annotation (PDF 1.3) displays text in a pop-up
 * window for entry and editing. It shall not appear alone but is associated
 * with a markup annotation, its parent annotation, and shall be used for editing
 * the parent’s text. It shall have no appearance stream or associated actions
 * of its own and shall be identified by the Popup entry in the parent’s annotation
 * dictionary (see Table 174). Table 183 shows the annotation dictionary entries
 * specific to this type of annotation.
 *
 * @since 5.0
 */
public class PopupAnnotation extends Annotation {

    private static final Logger logger =
            Logger.getLogger(PopupAnnotation.class.toString());

    /**
     * (Optional; shall be an indirect reference) The parent annotation with
     * which this pop-up annotation shall be associated.
     * <p/>
     * If this entry is present, the parent annotation’s Contents, M, C, and T
     * entries (see Table 168) shall override those of the pop-up annotation
     * itself.
     */
    public static final Name PARENT_KEY = new Name("Parent");
    /**
     * (Optional) A flag specifying whether the pop-up annotation shall
     * initially be displayed open. Default value: false (closed).
     */
    public static final Name OPEN_KEY = new Name("Open");

    protected boolean open;

    protected MarkupAnnotation parent;

    public PopupAnnotation(Library l, HashMap h) {
        super(l, h);
    }

    public void init() throws InterruptedException{
        super.init();
        open = library.getBoolean(entries, OPEN_KEY);
    }

    /**
     * Gets an instance of a PopupAnnotation that has valid Object Reference.
     *
     * @param library document library
     * @param rect    bounding rectangle in user space
     * @return new PopupAnnotation Instance.
     */
    public static PopupAnnotation getInstance(Library library,
                                              Rectangle rect) {
        // state manager
        StateManager stateManager = library.getStateManager();

        // create a new entries to hold the annotation properties
        HashMap<Name, Object> entries = new HashMap<Name, Object>();
        // set default link annotation values.
        entries.put(Dictionary.TYPE_KEY, Annotation.TYPE_VALUE);
        entries.put(Dictionary.SUBTYPE_KEY, Annotation.SUBTYPE_POPUP);
        // coordinates
        if (rect != null) {
            entries.put(Annotation.RECTANGLE_KEY,
                    PRectangle.getPRectangleVector(rect));
        } else {
            entries.put(Annotation.RECTANGLE_KEY, new Rectangle(10, 10, 50, 100));
        }

        // create the new instance
        PopupAnnotation popupAnnotation = null;
        try {
            popupAnnotation = new PopupAnnotation(library, entries);
            popupAnnotation.init();
            popupAnnotation.setPObjectReference(stateManager.getNewReferencNumber());
            popupAnnotation.setNew(true);

            // set default flags.
            popupAnnotation.setFlag(Annotation.FLAG_READ_ONLY, false);
            popupAnnotation.setFlag(Annotation.FLAG_NO_ROTATE, false);
            popupAnnotation.setFlag(Annotation.FLAG_NO_ZOOM, false);
            popupAnnotation.setFlag(Annotation.FLAG_PRINT, false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.finer("Popup Annotation initialization was interrupted");
        }

        return popupAnnotation;
    }

    @Override
    public void resetAppearanceStream(double dx, double dy, AffineTransform pageTransform) {

    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
        entries.put(OPEN_KEY, open);
    }

    public MarkupAnnotation getParent() {
        Object tmp = library.getObject(entries, PARENT_KEY);
        // should normally be a text annotation type.
        if (tmp != null && tmp instanceof MarkupAnnotation) {
            parent = (MarkupAnnotation) tmp;
        }
        return parent;
    }

    public void setParent(MarkupAnnotation parent) {
        this.parent = parent;
        entries.put(PARENT_KEY, parent.getPObjectReference());
    }
}
