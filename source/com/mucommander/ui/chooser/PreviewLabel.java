/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.ui.chooser;

import com.mucommander.text.Translator;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * PreviewLabel is a component used to preview a color selection that will eventually be used on a label.
 * This component is used by {@link ColorChooser} to preview the current color selection.
 *
 * @author Nicolas Rinaudo, Maxence Bernard
 */
public class PreviewLabel extends JLabel implements PropertyChangeListener, Cloneable {

    /** Color painted on top of the label. */
    private Color overlayColor;

    /** Label's border, if necessary. */
    private Border border;

    /** Controls whether the overlay should be painted over or under the text. */
    private boolean overlayUnderText;

    public final static String FOREGROUND_COLOR_PROPERTY_NAME = "PreviewLabel.ForegroundColor";
    public final static String BACKGROUND_COLOR_PROPERTY_NAME = "PreviewLabel.BackgroundColor";
    public final static String OVERLAY_COLOR_PROPERTY_NAME = "PreviewLabel.OverlayColor";
    public final static String BORDER_COLOR_PROPERTY_NAME = "PreviewLabel.BorderColor";


    /**
     * Creates a new preview label.
     */
    public PreviewLabel() {
        super(" ");
        addPropertyChangeListener(this);
    }

    /**
     * Sets the label's overlay color.
     */
    public void setOverlay(Color color) {
        putClientProperty(OVERLAY_COLOR_PROPERTY_NAME, color);
    }

    public void setTextPainted(boolean b) {
        if(b)
            setText(Translator.get("sample_text"));
        else
            setText(" ");
    }

    public void setOverlayUnderText(boolean b) {
        overlayUnderText = b;
        repaint();
    }

    public void setBorderColor(Color color) {
        putClientProperty(BORDER_COLOR_PROPERTY_NAME, color);
    }

    private void paintText(Graphics g) {
        FontMetrics metrics;

        g.setColor(getForeground());
        g.setFont(getFont());
        metrics = getFontMetrics(getFont());
        g.drawString(getText(), (getWidth() - metrics.stringWidth(getText())) / 2, (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public void setForeground(Color color) {
        putClientProperty(FOREGROUND_COLOR_PROPERTY_NAME, color);
    }

    public void setBackground(Color color) {
        putClientProperty(BACKGROUND_COLOR_PROPERTY_NAME, color);
    }

    public Object clone() throws CloneNotSupportedException {return super.clone();}

    /**
     * Paints the preview label.
     */
    public void paint(Graphics g) {

        int width = getWidth();
        int height = getHeight();

        g.setColor(getBackground());
        g.fillRect(0, 0, width, height);

        if(!overlayUnderText)
            paintText(g);

        if(overlayColor != null) {
            g.setColor(overlayColor);
            g.fillRect(0, 0, width/2, height);
        }

        if(overlayUnderText)
            paintText(g);

        if(border != null)
            border.paintBorder(this, g, 0, 0, width, height);
    }

    public Dimension getPreferredSize() {
        Dimension dimension = super.getPreferredSize();
        dimension.setSize(dimension.getWidth()+8, dimension.getHeight()+6);

        return dimension;
    }

///////////////////////////////////////////
    // PropertyChangeListener implementation //
    ///////////////////////////////////////////

    public void propertyChange(PropertyChangeEvent event) {
        String name = event.getPropertyName();
        Object value = event.getNewValue();

        if(FOREGROUND_COLOR_PROPERTY_NAME.equals(name)) {
            super.setForeground((Color)value);
        }
        else if(BACKGROUND_COLOR_PROPERTY_NAME.equals(name)) {
            super.setBackground((Color)value);
        }
        else if(OVERLAY_COLOR_PROPERTY_NAME.equals(name)) {
            overlayColor = (Color)value;
            repaint();
        }
        else if(BORDER_COLOR_PROPERTY_NAME.equals(name)) {
            border = new LineBorder((Color)value, 1);
            repaint();
        }
    }
}
