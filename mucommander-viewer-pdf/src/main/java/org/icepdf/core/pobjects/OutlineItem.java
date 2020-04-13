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

import org.icepdf.core.pobjects.actions.Action;
import org.icepdf.core.util.Library;
import org.icepdf.core.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>The <code>OutlineItem</code> represents the individual outline item within
 * the hierarchy defined by an <code>Outlines</code> class.  The outlines items
 * are chained together through their <b>Prev</b> and <b>Next</b> entries. Each
 * outline item has a title and a destination which can be accessed by the
 * visual representation to create a function document outlines (sometimes
 * called bookmarks).</p>
 * <p/>
 * <p>This class is used mainly by the Outlines class to build the outline
 * hierarchy.  The root node of the outline hierarchy can be accessed through
 * the Outlines class. </p>
 *
 * @see org.icepdf.ri.common.utility.outline.OutlineItemTreeNode
 * @see org.icepdf.core.pobjects.Outlines
 * @since 2.0
 */
public class OutlineItem extends Dictionary {

    public static final Name A_KEY = new Name("A");
    public static final Name COUNT_KEY = new Name("Count");
    public static final Name TITLE_KEY = new Name("Title");
    public static final Name DEST_KEY = new Name("Dest");
    public static final Name FIRST_KEY = new Name("First");
    public static final Name LAST_KEY = new Name("Last");
    public static final Name NEXT_KEY = new Name("Next");
    public static final Name PREV_KEY = new Name("Prev");
    public static final Name PARENT_KEY = new Name("Parent");

    // The text to be displayed on the screen for this item.
    private String title;

    // The destination to be displayed when this item is activated
    private Destination dest;

    // The action to be performed when this item is activated
    private Action action;

    // The parent of this item in the outline hierarchy. The parent of a
    // top-level item is the outline dictionary itself.
    private Reference parent;

    // The previous item at this outline level.
    private Reference prev;

    // The next item at this outline level.
    private Reference next;

    // An outline item dictionary representing the first top-level item
    // in the outline.
    private Reference first;

    // An outline item dictionary representing the last top-level item
    // in the outline.
    private Reference last;

    // The total number of open items at all levels of the outline. This
    // entry should be omitted if there are no open outline items.
    private int count = -1;

    private boolean loadedSubItems;

    private List<OutlineItem> subItems;


    /**
     * Creates a new instance of an OutlineItem.
     *
     * @param l document library.
     * @param h OutlineItem dictionary entries.
     */
    public OutlineItem(Library l, HashMap h) {
        super(l, h);
        loadedSubItems = false;
        subItems = new ArrayList<OutlineItem>(Math.max(Math.abs(getCount()), 16));
    }

    /**
     * Indicates if the Outline item is empty.  An outline item is empty if
     * it has no title, destination and action dictionary entries.
     *
     * @return true, if the outline entry is empty; false, otherwise.
     */
    public boolean isEmpty() {
        return getTitle() == null && getDest() == null && getAction() == null;
    }

    /**
     * Gets the number of descendants that would appear under this outline item.
     *
     * @return descendant count.
     */
    public int getSubItemCount() {
        ensureSubItemsLoaded();
        if (subItems != null)
            return subItems.size();
        else
            return 0;
    }

    /**
     * Gets the child outline item specified by the index.  All children of the
     * outline items are ordered and numbered.
     *
     * @param index child index number of desired outline item.
     * @return outline specified by index.
     */
    public OutlineItem getSubItem(int index) {
        ensureSubItemsLoaded();
        return subItems.get(index);
    }

    /**
     * Gets the action associated with this OutlineItem.
     *
     * @return the associated action; null, if there is no action.
     */
    public Action getAction() {
        // grab the action attribute
        if (action == null) {
            Object obj = library.getObject(entries, A_KEY);
            if (obj instanceof HashMap) {
                action = new org.icepdf.core.pobjects.actions.Action(library, (HashMap) obj);
            }
        }
        return action;
    }

    /**
     * Gets a reference to an outline item dictionary representing the first
     * top-level item in the outline.
     *
     * @return reference to first top-level item
     */
    public Reference getFirst() {
        if (first == null) {
            Object attribute = entries.get(FIRST_KEY);
            if (attribute instanceof Reference) {
                first = (Reference) attribute;
            }
        }
        return first;
    }

    /**
     * Gets a reference to an outline item dictionary representing the last
     * top-level item in the outline.
     *
     * @return reference to last top-level item
     */
    public Reference getLast() {
        if (last == null) {
            Object attribute = entries.get(LAST_KEY);
            if (attribute instanceof Reference) {
                last = (Reference) attribute;
            }
        }
        return last;
    }

    /**
     * Gets the next outline item at this outline level.
     *
     * @return next item at this outline level.
     */
    public Reference getNext() {
        if (next == null) {
            Object attribute = entries.get(NEXT_KEY);
            if (attribute instanceof Reference) {
                next = (Reference) attribute;
            }
        }
        return next;
    }

    /**
     * Gets the previous outline item at this outline level.
     *
     * @return previous item at this outline level.
     */
    public Reference getPrev() {
        if (prev == null) {
            Object attribute = entries.get(PREV_KEY);
            if (attribute instanceof Reference) {
                prev = (Reference) attribute;
            }
        }
        return prev;
    }

    /**
     * Gets the parent of this outline item in the outline hierarchy.  The
     * parent of a top-level item is the outline dictionary itself.
     *
     * @return parent of this item.
     */
    public Reference getParent() {
        if (parent == null) {
            Object attribute = entries.get(PARENT_KEY);
            if (attribute instanceof Reference) {
                parent = (Reference) attribute;
            }
        }
        return parent;
    }

    /**
     * Gets the number of descendants that would appear under this outline item.
     *
     * @return descendant count.
     */
    private int getCount() {
        if (count < 0) {
            // grab the count attribute
            count = library.getInt(entries, COUNT_KEY);
        }
        return count;
    }

    /**
     * Gets the text to be displayed on the screen for this item.
     *
     * @return text to be displayed
     */
    public String getTitle() {
        if (title == null) {
            // get title String for outline entry
            Object obj = library.getObject(entries, TITLE_KEY);
            if (obj instanceof StringObject) {
                title = Utils.convertStringObject(library, (StringObject) obj);
            }
        }
        return title;
    }

    /**
     * Gets the destination to be displayed when this item is activated.
     *
     * @return destination to be displayed.
     */
    public Destination getDest() {
        if (dest == null) {
            // grab the Destination attribute
            Object obj = library.getObject(entries, DEST_KEY);
            if (obj != null) {
                dest = new Destination(library, obj);
            }
        }
        return dest;
    }

    /**
     * Utility method for loading all children of this outline.  The main purpose
     * of this is to make sure the count is accurate.
     */
    private void ensureSubItemsLoaded() {
        if (loadedSubItems)
            return;

        loadedSubItems = true;

        if (getFirst() != null) {
            // get first child
            Reference nextReference = getFirst();
            Reference oldNextReference;
            OutlineItem outLineItem;
            HashMap dictionary;
            Object tmp;
            // iterate through children and see if then have children. 
            while (nextReference != null) {
                // result the outline dictionary
                tmp = library.getObject(nextReference);
                if (tmp == null || !(tmp instanceof HashMap)) {
                    break;
                } else {
                    dictionary = (HashMap) tmp;
                }
                // create the new outline
                outLineItem = new OutlineItem(library, dictionary);

                // add the new item to the list of children
                subItems.add(outLineItem);

                // old reference is kept to make sure we don't iterate out
                // of control on a circular reference.
                oldNextReference = nextReference;
                nextReference = outLineItem.getNext();

                // make sure we haven't already added this node, some
                // Outlines terminate with next being a pointer to itself.
                if (oldNextReference.equals(nextReference)) {
                    break;
                }
            }
        }
    }
}
