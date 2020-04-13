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

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

/**
 * LayersTree provides a UI interface for manipulating optional content
 * that maybe specified in a document. The LayersTree stores LayersTreeNodes
 * which when selected directly affect the visibility of the named layer.
 *
 * @since 5.0
 */
@SuppressWarnings("serial")
public class LayersTree extends JTree {

    public LayersTree(TreeNode root) {
        super(root);
        setCellRenderer(new CheckBoxRenderer());
        getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        setRootVisible(true);
        setScrollsOnExpand(true);
        // old font was Arial with is no go for linux.
        setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 13));
        setRowHeight(18);
    }
}

@SuppressWarnings("serial")
class CheckBoxRenderer extends JPanel implements TreeCellRenderer {

    protected JCheckBox checkBox;
    protected TreeLabel treeLabel;

    public CheckBoxRenderer() {
        setLayout(null);
        add(checkBox = new JCheckBox());
        add(treeLabel = new TreeLabel());
        checkBox.setBackground(UIManager.getColor("Tree.textBackground"));
        treeLabel.setForeground(UIManager.getColor("Tree.textForeground"));
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean isSelected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        String stringValue = tree.convertValueToText(value, isSelected,
                expanded, leaf, row, hasFocus);
        setEnabled(tree.isEnabled());
        if (value instanceof LayersTreeNode) {
            checkBox.setSelected(((LayersTreeNode) value).isSelected());
        }
        treeLabel.setFont(tree.getFont());
        treeLabel.setText(stringValue);
        treeLabel.setSelected(isSelected);
        treeLabel.setFocus(hasFocus);
        return this;
    }

    public Dimension getPreferredSize() {
        Dimension d_check = checkBox.getPreferredSize();
        Dimension d_label = treeLabel.getPreferredSize();
        return new Dimension(d_check.width + d_label.width,
                (d_check.height < d_label.height ?
                        d_label.height : d_check.height));
    }

    public void doLayout() {
        Dimension dCheck = checkBox.getPreferredSize();
        Dimension dLabel = treeLabel.getPreferredSize();
        int yCheck = 0;
        int yLabel = 0;
        if (dCheck.height < dLabel.height) {
            yCheck = (dLabel.height - dCheck.height) / 2;
        } else {
            yLabel = (dCheck.height - dLabel.height) / 2;
        }
        checkBox.setLocation(0, yCheck);
        checkBox.setBounds(0, yCheck, dCheck.width, dCheck.height);
        treeLabel.setLocation(dCheck.width, yLabel);
        treeLabel.setBounds(dCheck.width, yLabel, dLabel.width, dLabel.height);
    }


    public void setBackground(Color color) {
        if (color instanceof ColorUIResource)
            color = null;
        super.setBackground(color);
    }


    public class TreeLabel extends JLabel {
        boolean isSelected;
        boolean hasFocus;

        public TreeLabel() {
        }

        public void setBackground(Color color) {
            if (color instanceof ColorUIResource)
                color = null;
            super.setBackground(color);
        }

        public void paint(Graphics g) {
            String str;
            if ((str = getText()) != null) {
                if (0 < str.length()) {
                    if (isSelected) {
                        g.setColor(UIManager.getColor("Tree.selectionBackground"));
                    } else {
                        g.setColor(UIManager.getColor("Tree.textBackground"));
                    }
                    Dimension d = getPreferredSize();
                    int imageOffset = 0;
                    Icon currentI = getIcon();
                    if (currentI != null) {
                        imageOffset = currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
                    }
                    g.fillRect(imageOffset, 0, d.width - 1 - imageOffset, d.height);
                    if (hasFocus) {
                        g.setColor(UIManager.getColor("Tree.selectionBorderColor"));
                        g.drawRect(imageOffset, 0, d.width - 1 - imageOffset, d.height - 1);
                    }
                }
            }
            super.paint(g);
        }

        public Dimension getPreferredSize() {
            Dimension retDimension = super.getPreferredSize();
            if (retDimension != null) {
                retDimension = new Dimension(retDimension.width + 3,
                        retDimension.height);
            }
            return retDimension;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }

        public void setFocus(boolean hasFocus) {
            this.hasFocus = hasFocus;
        }
    }
}
