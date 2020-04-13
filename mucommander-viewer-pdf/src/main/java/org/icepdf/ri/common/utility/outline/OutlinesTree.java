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

import org.icepdf.ri.images.Images;

import javax.swing.*;
import javax.swing.text.Position;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * OutlinesTree is a JTree derivative whose nodes are OutlineItemTreeNode objects,
 * each of which refers to an OutlineItem
 *
 * @author Mark Collette
 * @see OutlineItemTreeNode
 * @see org.icepdf.core.pobjects.OutlineItem
 * @since 2.0
 */
@SuppressWarnings("serial")
public class OutlinesTree extends JTree {
    public OutlinesTree() {
        getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        // change the look & feel of the jtree
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setOpenIcon(new ImageIcon(Images.get("page.gif")));
        renderer.setClosedIcon(new ImageIcon(Images.get("page.gif")));
        renderer.setLeafIcon(new ImageIcon(Images.get("page.gif")));
        setCellRenderer(renderer);

        setModel(null);
        setRootVisible(true);
        setScrollsOnExpand(true);
        // old font was Arial with is no go for linux.
        setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 13));
        setRowHeight(18);
    }

    public TreePath getNextMatch(String prefix, int startingRow, Position.Bias bias) {
        // this method body is the same as JTree's implementation but has a check
        // for null text object, which causes a null pointer error in JDK 1.4
        int max = getRowCount();
        if (prefix == null) {
            throw new IllegalArgumentException();
        }
        if (startingRow < 0 || startingRow >= max) {
            throw new IllegalArgumentException();
        }
        prefix = prefix.toUpperCase();

        // start search from the next/previous element from the
        // selected element
        int increment = (bias == Position.Bias.Forward) ? 1 : -1;
        int row = startingRow;
        do {
            TreePath path = getPathForRow(row);
            String text = convertValueToText(
                    path.getLastPathComponent(), isRowSelected(row),
                    isExpanded(row), true, row, false);

            // Added check for null text to avoid nasty output
            if (text != null && text.toUpperCase().startsWith(prefix)) {
                return path;
            }
            row = (row + increment + max) % max;
        } while (row != startingRow);
        return null;
    }
}
