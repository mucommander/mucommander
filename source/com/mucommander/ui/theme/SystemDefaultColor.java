/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.ui.theme;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * {@link DefaultColor} implementation that maps to a system value.
 * <p>
 * The purpose of this class is to create default colors that map, for example, to the default text area foreground
 * color for the current look and feel.
 * </p>
 * <p>
 * The mechanism used to identify the default color goes through three different stages:
 * <ul>
 * <li>Look for a specific property in {@link UIManager}.</li>
 * <li>
 * If this isn't found, rely on a {@link ComponentMapper} to get an instance of the target and retrieve the relevant
 * color.
 * </li>
 * <li>
 * If this is <code>null</code>, return a hard-coded default value.
 * </li>
 * </ul>
 * </p>
 * @author Nicolas Rinaudo
 */
public class SystemDefaultColor extends DefaultColor implements PropertyChangeListener {
    // - Fallbacks -----------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Foreground color used in case no system default could be identified. */
    public static Color DEFAULT_FOREGROUND           = Color.BLACK;
    /** Background color used in case no system default could be identified. */
    public static Color DEFAULT_BACKGROUND           = Color.WHITE;
    /** Selection foreground color used in case no system default could be identified. */
    public static Color DEFAULT_SELECTION_FOREGROUND = Color.WHITE;
    /** Selection background color used in case no system default could be identified. */
    public static Color DEFAULT_SELECTION_BACKGROUND = Color.BLUE;



    // - Color types ---------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Identifies a foreground color (linked to {@link JComponent#getForeground()}). */
    public static final int FOREGROUND = 1;
    /** Identifies a background color (linked to {@link JComponent#getBackground()}). */
    public static final int BACKGROUND = 2;
    /** Identifies a selection foreground color (linked to {@link JTextComponent#getSelectedTextColor()}). */
    public static final int SELECTION_FOREGROUND = 3;
    /** Identifies a selection background color (linked to {@link JTextComponent#getSelectionColor()}). */
    public static final int SELECTION_BACKGROUND = 4;



    // - Instance fields -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** {@link UIManager} property to look for. */
    private String          property;
    /**
     * Type of the default color (can be one of {@link #FOREGROUND}, {@link #BACKGROUND}, {@link #SELECTION_FOREGROUND}
     * or {@link #SELECTION_BACKGROUND}). 
     */
    private int             type;
    /** Current default color value. */
    private Color           color;
     /** Used to create instance of the component whose color will be retrieved (in case {@link #property} isn't set). */
    private ComponentMapper mapper;



    // - Initialisation ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new instance of {@link SystemDefaultColor}.
     * @param type     type of the color being described (can be one of {@link #FOREGROUND}, {@link #BACKGROUND},
     *                 {@link #SELECTION_FOREGROUND} or {@link #SELECTION_BACKGROUND}).
     * @param property name of the {@link UIManager} property to look for.
     * @param mapper   component mapper to use when the {@link UIManager} property isn't set.
     */
    public SystemDefaultColor(int type, String property, ComponentMapper mapper) {
        UIManager.addPropertyChangeListener(this);
        this.property = property;
        this.mapper   = mapper;
        this.type     = type;
    }



    // - DefaultColor implementation -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the color of the right {@link #type type} used by the specified component.
     * @param  component component to analyse.
     * @return           the color of the right {@link #type type} used by the specified component.
     */
    private Color getColor(JComponent component) {
        // Foreground color.
        if(type == FOREGROUND)
            return component.getForeground();

        // Background color.
        else if(type == BACKGROUND)
            return component.getBackground();

        // Text component specific colors.
        else if(component instanceof JTextComponent) {
            JTextComponent comp;

            comp = (JTextComponent)component;

            // Selection foreground color.
            if(type == SELECTION_FOREGROUND)
                return comp.getSelectedTextColor();

            // Selection background color.
            else if(type == SELECTION_BACKGROUND)
                return comp.getSelectionColor();
        }
        return null;
    }

    /**
     * Returns the fallback color of the right {@link #type type}.
     * @return the fallback color of the right {@link #type type}.
     */
    private Color getColor() {
        switch(type) {
            case FOREGROUND:
                return DEFAULT_FOREGROUND;
            case SELECTION_FOREGROUND:
                return DEFAULT_SELECTION_FOREGROUND;
            case SELECTION_BACKGROUND:
                return DEFAULT_SELECTION_BACKGROUND;
            case BACKGROUND:
            default:
                return DEFAULT_BACKGROUND;
        }
    }

    @Override
    public Color getColor(ThemeData data) {
        if(color == null) {
            if((color = UIManager.getColor(property)) == null)
                if((color = getColor(mapper.getComponent())) == null)
                    color = getColor();
                
            color = new Color(color.getRGB(), (color.getRGB() & 0xFF000000) != 0xFF000000);
        }

        return color;
    }




    // - PropertyChangeListener implementation -------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    public void propertyChange(PropertyChangeEvent evt) {
        String name;

        name = evt.getPropertyName().toLowerCase();

        if(name.equals("lookandfeel") || name.equalsIgnoreCase(property)) {
            Color oldColor;

            color    = null;
            oldColor = color;
            color    = getColor((ThemeData)null);
            if(!color.equals(oldColor))
                notifyChange(color);
        }
    }
}
