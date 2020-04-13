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

package org.icepdf.ri.common;

import java.awt.*;

/**
 * The ToolbarLayout class provides a Flow-like Layout Manager specifically
 * suited for use within a JToolBar.
 * <p/>
 * <p>This layout manager will dynamically wrap JToolBar contents to additional
 * toolbar rows if the current toolbar width is not sufficient for it to display
 * all of the toolbar items on the current number of toolbar rows. Alternatively,
 * it will also collapse the number of JToolBar rows if possible to display the
 * toolbar contents on as few rows as possible.
 */

class ToolbarLayout implements LayoutManager {
    public static final int LEFT = 0;
    public static final int CENTER = 1;
    public static final int RIGHT = 2;
    public static final int LEADING = 3;
    public static final int TRAILING = 4;

    int align;
    int hgap;
    int vgap;

    public ToolbarLayout() {
        this(CENTER, 5, 5);
    }

    public ToolbarLayout(int align) {
        this(align, 5, 5);
    }

    public ToolbarLayout(int align, int hgap, int vgap) {
        this.hgap = hgap;
        this.vgap = vgap;
        setAlignment(align);
    }

    public int getAlignment() {
        return align;
    }

    public void setAlignment(int align) {
        switch (align) {
            case LEADING:
                this.align = LEFT;
                break;
            case TRAILING:
                this.align = RIGHT;
                break;
            default:
                this.align = align;
                break;
        }
    }

    public int getHgap() {
        return hgap;
    }

    public void setHgap(int hgap) {
        this.hgap = hgap;
    }

    public int getVgap() {
        return vgap;
    }

    public void setVgap(int vgap) {
        this.vgap = vgap;
    }

    public Dimension minimumLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Dimension dim = new Dimension(0, 0);
            int componentCount = parent.getComponentCount();

            for (int i = 0; i < componentCount; i++) {
                Component c = parent.getComponent(i);
                if (c.isVisible()) {
                    Dimension d = c.getMinimumSize();
                    dim.height = Math.max(dim.height, d.height);
                    if (i > 0) {
                        dim.width += hgap;
                    }
                    dim.width += d.width;
                }
            }
            Insets insets = parent.getInsets();
            dim.width += insets.left + insets.right + 2 * hgap;
            dim.height += insets.top + insets.bottom + 2 * vgap;
            return dim;
        }
    }

    public Dimension preferredLayoutSize(Container parent) {
        if (parent.getWidth() == 0) {
            // Parent has no size yet, ask for enough to fit all toolbar items on one row
            return minimumLayoutSize(parent);

        } else {

            synchronized (parent.getTreeLock()) {
                Dimension dim = new Dimension(0, 0);
                int maxWidth = 0;
                int componentCount = parent.getComponentCount();
                Insets insets = parent.getInsets();
                int padWidths = (hgap * 2) + insets.left + insets.right;

                for (int i = 0; i < componentCount; i++) {
                    Component c = parent.getComponent(i);
                    if (c.isVisible()) {
                        Dimension d = c.getPreferredSize();
                        // Does this comp fit in the current toolbar width?
                        if ((dim.width + d.width + padWidths) <= parent.getWidth()) {
                            // Yes, check to see if the row height needs to grow to fit it.
                            dim.height = Math.max(dim.height, d.height);
                        } else {
                            // No, add height for another toolbar row, reset row width
                            dim.height += vgap + d.height;
                            dim.width = 0;
                        }
                        if (dim.width > 0) {
                            // Add hgap between toolbar items
                            dim.width += hgap;
                        }
                        // Add width of this toolbar item to row width
                        dim.width += d.width;
                        if (dim.width > maxWidth) {
                            maxWidth = dim.width;
                        }
                    }
                }

                dim.width = Math.max(dim.width, maxWidth);
                dim.width += insets.left + insets.right + 2 * hgap;
                dim.height += insets.top + insets.bottom + 2 * vgap;
                return dim;
            }
        }
    }


    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int maxWidth = parent.getWidth() -
                    (insets.left + insets.right + hgap * 2);
            int componentCount = parent.getComponentCount();
            int x = 0, y = insets.top + vgap;
            int rowh = 0, start = 0;
            boolean ltr = parent.getComponentOrientation().isLeftToRight();

            for (int i = 0; i < componentCount; i++) {
                Component c = parent.getComponent(i);
                if (c.isVisible()) {
                    Dimension d = c.getPreferredSize();
                    c.setSize(d.width, d.height);
                    if ((x == 0) || ((x + d.width) <= maxWidth)) {
                        if (x > 0) {
                            x += hgap;
                        }
                        x += d.width;
                        rowh = Math.max(rowh, d.height);
                    } else {
                        rowh = moveComponents(parent, insets.left + hgap, y,
                                maxWidth - x, rowh, start, i, ltr);
                        x = d.width;
                        y += vgap + rowh;
                        rowh = d.height;
                        start = i;
                    }
                }
            }
            moveComponents(parent, insets.left + hgap, y, maxWidth - x,
                    rowh, start, componentCount, ltr);
        }
    }

    private int moveComponents(Container parent, int x, int y, int width,
                               int height, int rowStart, int rowEnd,
                               boolean ltr) {
        switch (align) {
            case LEFT:
                x += ltr ? 0 : width;
                break;
            case CENTER:
                x += width / 2;
                break;
            case RIGHT:
                x += ltr ? width : 0;
                break;
            case LEADING:
                break;
            case TRAILING:
                x += width;
                break;
        }
        for (int i = rowStart; i < rowEnd; i++) {
            Component c = parent.getComponent(i);
            if (c.isVisible()) {
                int cy;
                cy = y + (height - c.getHeight()) / 2;
                if (ltr) {
                    c.setLocation(x, cy);
                } else {
                    c.setLocation(parent.getWidth() - x - c.getWidth(), cy);
                }
                x += c.getWidth() + hgap;
            }
        }
        return height;
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }
}

