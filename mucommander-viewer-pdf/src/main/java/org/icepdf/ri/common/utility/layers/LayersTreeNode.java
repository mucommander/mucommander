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
package org.icepdf.ri.common.utility.layers;

import org.icepdf.core.pobjects.OptionalContentGroup;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Enumeration;

/**
 * The LayersTreeNode represent a group of optional content members or just
 * one optional content group.  The user object for this DefaultMutableTreeNode
 * must always be of type OptionalContentGroup.  The OptionalContentGroup object
 * is a reference to the OptionalContentGroup in the document's dictionary
 * and any visibility changes will be reflected in the next Page paint.
 *
 * @since 5.0
 */
@SuppressWarnings("serial")
public class LayersTreeNode extends DefaultMutableTreeNode {

    /**
     * Node selection is independent of other nodes.
     */
    public final static int SINGLE_SELECTION = 1;

    /**
     * Nodes behave like radio check boxes where only one can be selected
     * at a time.
     */
    public final static int RADIO_SELECTION = 2;

    protected int selectionMode = SINGLE_SELECTION;

    public LayersTreeNode(Object object) {
        OptionalContentGroup optionalContentGroup = null;
        if (object instanceof String) {
            optionalContentGroup = new OptionalContentGroup((String) object, false);
        } else if (object instanceof OptionalContentGroup) {
            optionalContentGroup = (OptionalContentGroup) object;
        }
        setUserObject(optionalContentGroup);
    }

    public LayersTreeNode(OptionalContentGroup optionalContentGroup) {
        this(optionalContentGroup, true);
        setUserObject(optionalContentGroup);
    }

    public LayersTreeNode(Object userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
    }

    public void setSelectionMode(int mode) {
        selectionMode = mode;
    }

    public int getSelectionMode() {
        return selectionMode;
    }

    public OptionalContentGroup getOptionalContentGroup() {
        return (OptionalContentGroup) getUserObject();
    }

    public void setSelected(boolean isSelected) {
        ((OptionalContentGroup) getUserObject()).setVisible(isSelected);

        // if the node is a branch (has children), propagate the selection
        // in to the child notes.
        if ((selectionMode == SINGLE_SELECTION)
                && (children != null)) {
            LayersTreeNode layerNode;
            for (Object child : children) {
                layerNode = (LayersTreeNode) child;
                layerNode.setSelected(isSelected);
            }
        }
        // only one node cn be selected at one.
        else if (selectionMode == RADIO_SELECTION) {
            // deselect other nodes.
            Enumeration children = parent.children();
            if (children != null) {
                LayersTreeNode layerNode;
                while (children.hasMoreElements()) {
                    layerNode = (LayersTreeNode) children.nextElement();
                    if (!layerNode.equals(this)) {
                        layerNode.getOptionalContentGroup().setVisible(false);
                    }
                }
            }

        }

    }

    public boolean isSelected() {
        return ((OptionalContentGroup) getUserObject()).isVisible();
    }

    @Override
    public String toString() {
        return ((OptionalContentGroup) getUserObject()).getName();
    }
}
