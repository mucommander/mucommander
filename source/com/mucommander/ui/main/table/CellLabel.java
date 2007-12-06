/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.ui.main.table;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;


/**
 * A custom <code>JLabel</code> component used by {@link FileTableCellRenderer FileTableCellRenderer} to render table cells.
 *
 * <p>CellLabel is basically a faster dumbed-down JLabel which overrides some JLabel to be no-ops (see below)
 * and some other (setText, setIcon) to call JLabel's super methods only if value has changed since last call,
 * as very often values don't change from one cell to another. Some methods were borrowed from Sun's 
 * <code>DefaultTableCellRender</code> implementation and are marked as much.</p>
 * 
 * <p>Quote from Sun's Javadoc : The table class defines a single cell renderer and uses it as a 
 * as a rubber-stamp for rendering all cells in the table;  it renders the first cell,
 * changes the contents of that cell renderer, shifts the origin to the new location, re-draws it, and so on.
 * <p>The standard <code>JLabel</code> component was not
 * designed to be used this way and we want to avoid 
 * triggering a <code>revalidate</code> each time the
 * cell is drawn. This would greatly decrease performance because the
 * <code>revalidate</code> message would be
 * passed up the hierarchy of the container to determine whether any other
 * components would be affected.  So this class
 * overrides the <code>validate</code>, <code>revalidate</code>,
 * <code>repaint</code>, and <code>firePropertyChange</code> methods to be 
 * no-ops.
 *
 * @author Maxence Bernard, Sun Microsystems
 */
public class CellLabel extends JLabel {
    // - Constants -----------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Amount of border space on the left and right of the cell */
    public static final int CELL_BORDER_WIDTH = 4;
    /** Amount of border space on the top and bottom of the cell */
    public static final int CELL_BORDER_HEIGHT = 1;
    /** Empty border to give more space around cells */
    private static final Border CELL_BORDER = new EmptyBorder(CELL_BORDER_HEIGHT, CELL_BORDER_WIDTH, CELL_BORDER_HEIGHT, CELL_BORDER_WIDTH);



    // - Instance fields -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Last text set by the setText method */
    private String    lastText;
    /** Last icon set by the setIcon method */
    private ImageIcon lastIcon;
    /** Last tooltip text set by the setToolTipText method */
    private String    lastTooltip;
    /** Last foreground color set by the setForeground method */
    private Color     lastForegroundColor;
    /** Last background color set by the setBackground method */
    private Color     lastBackgroundColor;
    /** Outline color (top and bottom). */
    private Color     outlineColor;
    /** Gradient color for the background. */
    private Color     gradientColor;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new blank CellLabel.
     */
    public CellLabel() {setBorder(CELL_BORDER);}


    // - Color changing ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Overrides <code>JComponent.setForeground</code> to call 
     * the super method only if the value has changed since last call.
     * 
     * @param c the new foreground's color for this label
     */
    public void setForeground(Color c) {
        if((c != null && !c.equals(lastForegroundColor)) || (lastForegroundColor != null && !lastForegroundColor.equals(c))) {
            super.setForeground(c); 
            lastForegroundColor = c;
        }
    }
    
    /**
     * Overrides <code>JComponent.setBackground</code> to call 
     * the super method only if the value has changed since last call.
     * 
     * @param c the new background's color for this label
     */
    public void setBackground(Color c) {
        if((c != null && !c.equals(lastBackgroundColor)) || (lastBackgroundColor != null && !lastBackgroundColor.equals(c))) {
            super.setBackground(c); 
            lastBackgroundColor = c;
            gradientColor       = null;
        }
    }

    /**
     * Sets the background to a gradient between the two specified colors.
     * @param c1 first component of the gradient.
     * @param c2 second component of the gradient.
     */
    public void setBackground(Color c1, Color c2) {
        if(c1.equals(c2))
            setBackground(c1);
        else {
            lastBackgroundColor = c1;
            gradientColor       = c2;
        }
    }


    /**
     * Sets the label outline color.
     * @param c the new background's color for this label
     */
    public void setOutline(Color c) {outlineColor = c;}



    // - Label content -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Overrides <code>JLabel.setText</code> to call 
     * the super method only if the value has changed since last call.
     * 
     * @param text the new text this label will display
     */
    public void setText(String text) {
        if((text!=null && !text.equals(lastText)) || (lastText!=null && !lastText.equals(text))) {
            super.setText(text);
            lastText = text;
        }
    }


    /**
     * Overrides <code>JLabel.setIcon</code> to call 
     * the super method only if the value has changed since last call.
     * 
     * @param icon the new icon this label will display
     */
    public void setIcon(ImageIcon icon) {
        if(icon!=lastIcon) {
            super.setIcon(icon);
            lastIcon = icon;
        }
    }


    /**
     * Overrides <code>JLabel.setToolTipText</code> to call 
     * the super method only if the value has changed since last call.
     * 
     * @param tooltip the new tooltip this label will display
     */
    public void setToolTipText(String tooltip) {
        if((tooltip!=null && !tooltip.equals(lastTooltip)) || (lastTooltip!=null && !lastTooltip.equals(tooltip))) {
            super.setToolTipText(tooltip);
            lastTooltip = tooltip;
        }
    }



    // - Painting ------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Paints the label.
     * @param g where to paint the label.
     */
    public void paint(Graphics g) {
        // Checks whether we need to paint a gradient background.
        if(gradientColor != null) {
            Graphics2D g2; // Allows us to use the setPaint and getPaint methods.

            // Initialisation.
            g2 = (Graphics2D)g.create();

            // Paints the gradient background.
            g2.setPaint(new GradientPaint(0, 0, lastBackgroundColor, 0, getHeight(), gradientColor, false));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Restores the Graphics instance to its previous state.
            g2.dispose();
        }

        // Normal painting.
        super.paint(g);

        // If necessary, paints the outline color.
        if(outlineColor != null && !outlineColor.equals(lastBackgroundColor)) {
            g.setColor(outlineColor);
            g.drawLine(0, 0, getWidth(), 0);
            g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        }
    }	



    // - DefaultTableCellRenderer implementation -----------------------------------------
    // -----------------------------------------------------------------------------------
    /*
     * The following methods are overridden as a performance measure to 
     * to prune code-paths are often called in the case of renders
     * but which we know are unnecessary.  Great care should be taken
     * when writing your own renderer to weigh the benefits and 
     * drawbacks of overriding methods like these.
     */

    /**
     * Overridden for performance reasons.
     */
    public boolean isOpaque() {
        // If we're not using a gradient background, the component's opaque
        // status is context dependant.
        if(gradientColor == null) {
            Color     back;
            Component p;

            back = lastBackgroundColor;
            if((p = getParent()) != null)
                p = p.getParent();

            // The label does not need to be opaque if it has an opaque parent component
            // of the same background color.
            return !((back != null) && (p != null) && back.equals(p.getBackground()) && p.isOpaque());
        }

        // We must consider the label not to be opaque, otherwise the gradient would be overpainted by
        // the component's background color.
        return false;
    }

    /**
     * Overridden for performance reasons.
     */
    public void validate() {}

    /**
     * Overridden for performance reasons.
     */
    public void revalidate() {}

    /**
     * Overridden for performance reasons.
     */
    public void repaint(long tm, int x, int y, int width, int height) {}

    /**
     * Overridden for performance reasons.
     */
    public void repaint(Rectangle r) { }

    /**
     * Overridden for performance reasons.
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {	
        // Strings get interned...
        if(propertyName.equals("text"))
            super.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Overridden for performance reasons.
     */
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { }
}
