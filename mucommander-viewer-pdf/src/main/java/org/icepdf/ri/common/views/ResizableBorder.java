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
package org.icepdf.ri.common.views;

import org.icepdf.core.util.ColorUtil;
import org.icepdf.core.util.Defs;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The resizable border is mainly designed to bed used with mutable annotation
 * in the UI but suspect it could be used for after content manipulation. Like
 * other Swing Borders the same instance can be used on multiple components.
 *
 * @since 4.0
 */
@SuppressWarnings("serial")
public class ResizableBorder extends AbstractBorder {

    private static final Logger logger =
            Logger.getLogger(ResizableBorder.class.toString());

    private static Color selectColor;
    private static Color outlineColor;
    private static Color outlineResizeColor;

    public static final int INSETS = 5;
    static {

        // sets annotation selected highlight colour
        try {
            String color = Defs.sysProperty(
                    "org.icepdf.core.views.page.annotation.select.color", "#999999");
            int colorValue = ColorUtil.convertColor(color);
            selectColor =
                    new Color(colorValue >= 0 ? colorValue :
                            Integer.parseInt("999999", 16));

            color = Defs.sysProperty(
                    "org.icepdf.core.views.page.annotation.outline.color", "#cccccc");
            colorValue = ColorUtil.convertColor(color);
            outlineColor =
                    new Color(colorValue >= 0 ? colorValue :
                            Integer.parseInt("cccccc", 16));

            color = Defs.sysProperty(
                    "org.icepdf.core.views.page.annotation.outline.colorResize", "#666666");
            colorValue = ColorUtil.convertColor(color);
            outlineResizeColor =
                    new Color(colorValue >= 0 ? colorValue :
                            Integer.parseInt("666666", 16));

        } catch (NumberFormatException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Error reading page annotation outline colour");
            }
        }
    }

    private static final int locations[] = {
            SwingConstants.NORTH, SwingConstants.SOUTH, SwingConstants.WEST,
            SwingConstants.EAST, SwingConstants.NORTH_WEST,
            SwingConstants.NORTH_EAST, SwingConstants.SOUTH_WEST,
            SwingConstants.SOUTH_EAST};
    private static final int cursors[] = {
            Cursor.N_RESIZE_CURSOR, Cursor.S_RESIZE_CURSOR, Cursor.W_RESIZE_CURSOR,
            Cursor.E_RESIZE_CURSOR, Cursor.NW_RESIZE_CURSOR, Cursor.NE_RESIZE_CURSOR,
            Cursor.SW_RESIZE_CURSOR, Cursor.SE_RESIZE_CURSOR};
    private static final Stroke dashedBorder =
            new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2, 1}, 0);
    private static final Stroke solidBorder =
            new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0);

    protected int resizeWidgetDim;
    protected int originalResizeWidgetDim;
    protected int inset;

    public ResizableBorder(int resizeBoxSize) {
        this.originalResizeWidgetDim = resizeBoxSize;
    }

    public Insets getBorderInsets(Component component) {
        return new Insets(inset, inset, inset, inset);
    }

    public void setZoom(float zoom) {
        this.resizeWidgetDim = (int) (this.originalResizeWidgetDim * zoom);
        this.inset = (int) (INSETS * zoom + 0.5);
    }

    public boolean isBorderOpaque() {
        return false;
    }

    public void paintBorder(Component component, Graphics g, int x, int y,
                            int w, int h) {
        boolean isSelected = false;
        boolean isBorderStyle = false;

        boolean isEditable = false;
        boolean isRollover = false;
        boolean isMovable = false;
        boolean isResizable = false;
        boolean isShowInvisibleBorder = false;

        // get render flags from component.
        if (component instanceof AnnotationComponent) {
            AnnotationComponent annot = (AnnotationComponent) component;
            isSelected = annot.isSelected();
            isBorderStyle = annot.isBorderStyle();

            isEditable = annot.isEditable();
            isRollover = annot.isRollover();
            isMovable = annot.isMovable();
            isResizable = annot.isResizable();
            isShowInvisibleBorder = annot.isShowInvisibleBorder();
        }

        // if we aren't in the edit mode, then we have nothing to paint.
        if (!isEditable) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(dashedBorder);

        // get paint colour
        if (isSelected || component.hasFocus() || isRollover) {
            g2.setColor(selectColor);
        } else {
            g2.setColor(outlineColor);
        }

        // paint border
        if (isSelected || isRollover || (isShowInvisibleBorder && !isBorderStyle)) {
            g2.drawRect(x, y, w - 1, h - 1);
        }

        // paint resize widgets.
        g2.setColor(outlineResizeColor);
        g2.setStroke(solidBorder);
        if ((isSelected || isRollover) && isResizable) {
            for (int location : locations) {
                Rectangle rect = getRectangle(x, y, w, h, location);
//                g.setColor(Color.WHITE);
                g2.fillRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
//                g.setColor(Color.BLACK);
                g2.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
            }
        }
    }


    private Rectangle getRectangle(int x, int y, int w, int h, int location) {
        switch (location) {
            case SwingConstants.NORTH:
                return new Rectangle(x + w / 2 - resizeWidgetDim / 2, y, resizeWidgetDim, resizeWidgetDim);
            case SwingConstants.SOUTH:
                return new Rectangle(x + w / 2 - resizeWidgetDim / 2, y + h - resizeWidgetDim, resizeWidgetDim,
                        resizeWidgetDim);
            case SwingConstants.WEST:
                return new Rectangle(x, y + h / 2 - resizeWidgetDim / 2, resizeWidgetDim, resizeWidgetDim);
            case SwingConstants.EAST:
                return new Rectangle(x + w - resizeWidgetDim, y + h / 2 - resizeWidgetDim / 2, resizeWidgetDim,
                        resizeWidgetDim);
            case SwingConstants.NORTH_WEST:
                return new Rectangle(x, y, resizeWidgetDim, resizeWidgetDim);
            case SwingConstants.NORTH_EAST:
                return new Rectangle(x + w - resizeWidgetDim, y, resizeWidgetDim, resizeWidgetDim);
            case SwingConstants.SOUTH_WEST:
                return new Rectangle(x, y + h - resizeWidgetDim, resizeWidgetDim, resizeWidgetDim);
            case SwingConstants.SOUTH_EAST:
                return new Rectangle(x + w - resizeWidgetDim, y + h - resizeWidgetDim, resizeWidgetDim, resizeWidgetDim);
        }
        return null;
    }


    public int getCursor(MouseEvent me) {
        Component c = me.getComponent();
        boolean isEditable = false;
        boolean isMovable = false;
        boolean isResizable = false;

        // get render flags from component.
        if (c instanceof AnnotationComponent) {
            AnnotationComponent annot = (AnnotationComponent) c;
            isEditable = annot.isEditable();
            isResizable = annot.isResizable();
            isMovable = annot.isMovable();
        }

        int w = c.getWidth();
        int h = c.getHeight();

        // show resize cursors for link annotations
        if (isResizable) {
            for (int i = 0; i < locations.length; i++) {
                Rectangle rect = getRectangle(0, 0, w, h, locations[i]);
                if (rect.contains(me.getPoint()))
                    return cursors[i];
            }
        }
        if (isMovable) {
            return Cursor.MOVE_CURSOR;
        }
        // other wise just show the move. 
        return Cursor.DEFAULT_CURSOR;
    }
}
