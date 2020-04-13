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
package org.icepdf.core.pobjects;

import org.icepdf.core.util.Library;

import java.util.HashMap;

/**
 * <p>This class represents a PDF document outline.  A document outline is
 * an optional component of a PDF document and is accessible from the document's
 * Catalog.  The outline consists of a tree-structured hierarchy of outline items
 * (sometimes called bookmarks) which can be used to display a documents
 * structure to the user.</p>
 * <p/>
 * <p>The outlines class does not build a visible structure; it only represents the
 * non-visual structure of the outline.  The OutlineItemTreeNode available in
 * the packageorg.icepdf.core.ri.common provides an example on converting
 * this hierarchy to a Swing JTree.</p>
 *
 * @see org.icepdf.ri.common.utility.outline.OutlineItemTreeNode
 * @see org.icepdf.core.pobjects.OutlineItem
 * @since 1.0
 */
public class Outlines extends Dictionary {

    public static final Name D_KEY = new Name("D");
    public static final Name COUNT_KEY = new Name("Count");

    // number of child outline items
    private Integer count;

    // needed for future dispose implementation.
    //private OutlineItem rootOutlineItem;

    /**
     * Creates a new instance of Outlines.
     *
     * @param l document library.
     * @param h Outlines dictionary entries.
     */
    public Outlines(Library l, HashMap h) {
        super(l, h);
        if (entries != null) {
            count = library.getInt(entries, COUNT_KEY);
        }
    }

    /**
     * Gets the root OutlineItem.  The root outline item can be traversed to build
     * a visible outline of the hierarchy.
     *
     * @return root outline item.
     */
    public OutlineItem getRootOutlineItem() {
        if (count == null)
            return null;
        return new OutlineItem(library, entries);
    }

}
