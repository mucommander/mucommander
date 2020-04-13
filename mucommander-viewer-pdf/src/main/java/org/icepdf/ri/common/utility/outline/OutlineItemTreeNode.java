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
package org.icepdf.ri.common.utility.outline;

import org.icepdf.core.pobjects.OutlineItem;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A PDF document may optionally display a document outline on the screen,
 * allowing the user to navigate interactively from one part of the document to
 * another. The outline consists of a tree-structured hierarchy of outline
 * items (sometimes called bookmarks), which serve as a "visual table of
 * contents" to display the document's structure to the user. The user can
 * interactively open and close individual items by clicking them with the
 * mouse. When an item is open, its immediate children in the hierarchy become
 * visible on the screen; each child may in turn be open or closed, selectively
 * revealing or hiding further parts of the hierarchy. When an item is closed,
 * all of its descendants in the hierarchy are hidden. Clicking the text of any
 * visible item with the mouse activates the item, causing the viewer
 * application to jump to a destination or trigger an action associated with
 * the item.
 * An OutlineItemTreeNode object represents the bookmarks or leaves which makes up
 * the actual Outline JTree.
 */
@SuppressWarnings("serial")
public class OutlineItemTreeNode extends DefaultMutableTreeNode {
    private OutlineItem item;
    private boolean loadedChildren;

    /**
     * Creates a new instance of an OutlineItemTreeNode
     *
     * @param item Contains PDF Outline item data
     */
    public OutlineItemTreeNode(OutlineItem item) {
        super();
        this.item = item;
        loadedChildren = false;

        // build the tree
        setUserObject(item.getTitle());
    }

    public OutlineItem getOutlineItem() {
        return item;
    }

    public void recursivelyClearOutlineItems() {
        item = null;
        if (loadedChildren) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                OutlineItemTreeNode node = (OutlineItemTreeNode) getChildAt(i);
                node.recursivelyClearOutlineItems();
            }
        }
    }

    public int getChildCount() {
        ensureChildrenLoaded();
        return super.getChildCount();
    }

    /**
     * Only load children as needed, so don't have to load
     * OutlineItems that the user has not even browsed to
     */
    private void ensureChildrenLoaded() {
        if (!loadedChildren) {
            loadedChildren = true;

            int count = item.getSubItemCount();
            for (int i = 0; i < count; i++) {
                OutlineItem child = item.getSubItem(i);
                OutlineItemTreeNode childTreeNode = new OutlineItemTreeNode(child);
                add(childTreeNode);
            }
        }
    }
}
