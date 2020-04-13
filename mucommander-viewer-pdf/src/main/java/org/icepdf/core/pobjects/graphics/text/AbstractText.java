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
package org.icepdf.core.pobjects.graphics.text;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Abstract text is the base class for all Text extraction data.  Its main
 * purpose to is hold common data for GeneralPath and Bounds and common
 * contains and intersect calculations.
 * <p/>
 * Some paintable properties are also defined here, such as selected, has selected
 * highlight and hasHighlight which are used as queues to painting selected
 * or highlighted text.
 *
 * @since 4.0
 */
public abstract class AbstractText implements Text {

    // Bounds of text converted to page space.
    protected Rectangle2D.Float bounds;

    // original bounds as plotted by the PDF,  can be used for space and
    // line break detection.  Once normalized to page space (bounds instance var)
    // it may not be possible to make the formatting detection.  However normalized
    // bounds are used for text selection.
    protected Rectangle2D.Float textExtractionBounds;

    // selected states
    protected boolean selected;
    // highlight state
    protected boolean highlight;

    // highlight hint for quicker painting
    protected boolean hasSelected;
    // highlight hint for quicker painting
    protected boolean hasHighlight;

    /**
     * Gets the bounds of the respective text object normalized to page
     * space.  This is mainly used for text selection calculations.
     *
     * @return bounds of text object.
     */
    public abstract Rectangle2D.Float getBounds();

    public void clearBounds() {
        bounds = null;
    }

    /**
     * Creates a new instance of GeneralPath for this AbstractText object and
     * applies the current pageTransformation to it.  The containment
     * calculation is then applied the newly transformed path for the given
     * rectangle.
     * <p/>
     * This method is usually used for text selection via a selection box.
     *
     * @param rect rectangle to check intersection of in page.
     * @return true if the point is contained with in this Text instance.
     */
    public boolean intersects(Rectangle2D rect) {
        // bounds is lazy loaded so getBounds is need to get the value correctly.
        GeneralPath shapePath = new GeneralPath(getBounds());
        return shapePath.intersects(rect);
    }

    /**
     * Tests if the point intersects the text bounds.
     *
     * @param point point to test for intersection.
     * @return true if the point intersects the text bounds,  otherwise false.
     */
    public boolean intersects(Point2D.Float point) {
        // bounds is lazy loaded so getBounds is need to get the value correctly.
        GeneralPath shapePath = new GeneralPath(getBounds());
        return shapePath.contains(point);
    }

    /**
     * Is the AbstractText selected, all of its children must also be selected.
     *
     * @return true if selected false otherwise.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Sets the AbstractText as selected, if it child AbstractText object they
     * must also be selected.
     *
     * @param selected selected state.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Is the AbstractText highlighted, all of its children must also be
     * highlighted.
     *
     * @return true if highlighted false otherwise.
     */
    public boolean isHighlighted() {
        return highlight;
    }

    /**
     * Sets the AbstractText as highlighted, if it child AbstractText object they
     * must also be highlighted.
     *
     * @param highlight selected state.
     */
    public void setHighlighted(boolean highlight) {
        this.highlight = highlight;
    }

    /**
     * Indicates that at least this or one of the child instances of AbstractText
     * is highlighted.
     *
     * @return true if one or more root or parent elements are in a highlighted
     * state.
     */
    public boolean hasHighligh() {
        return hasHighlight;
    }

    /**
     * Indicates that at least this or one of the child instances of AbstractText
     * is selected.
     *
     * @return true if one or more root or parent elements are in a highlighted
     * state.
     */
    public boolean hasSelected() {
        return hasSelected;
    }

    /**
     * Set the highlighted state, meaning that this instance or one of the child
     * AbstractText objects has a highlighted state.
     *
     * @param hasHighlight true to indicates a highlighted states.
     */
    public void setHasHighlight(boolean hasHighlight) {
        this.hasHighlight = hasHighlight;
    }

    /**
     * Set the selected state, meaning that this instance or one of the child
     * AbstractText objects has a selected state.
     *
     * @param hasSelected true to indicates a selected states.
     */
    public void setHasSelected(boolean hasSelected) {
        this.hasSelected = hasSelected;
    }

    /**
     * Gets the original bounds of the text unit, this value is not normalized
     * to page space and represents the raw layout coordinates of the text as
     * defined in the Post Script notation. This is primarily used for text
     * extraction line and word break calculations.
     *
     * @return text bounds.
     */
    public Rectangle2D.Float getTextExtractionBounds() {
        return textExtractionBounds;
    }
}
