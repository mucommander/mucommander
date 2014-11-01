/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * {@link DefaultFont} implementation that maps to a system value.
 * <p>
 * The purpose of this class is to create default fonts that map, for example, to the default text area font for the
 * current look and feel.
 * </p>
 * <p>
 * The mechanism used to identify the default font goes through three different stages:
 * <ul>
 * <li>Look for a specific property in {@link UIManager}.</li>
 * <li>
 * If this isn't found, rely on a {@link ComponentMapper} to get an instance of the target and retrieve its font.
 * </li>
 * <li>
 * If this is <code>null</code>, return a default SansSerif font.
 * </li>
 * </ul>
 * </p>
 * @author Nicolas Rinaudo
 */
public class SystemDefaultFont extends DefaultFont implements PropertyChangeListener {
    // - Instance fields -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Name of the {@link UIManager#getFont(Object)} font property} to query. */
    private String          property;
    /** Current value of the default font. */
    private Font            font;
    /** Used to create instance of the component whose font will be retrieved (in case {@link #property} isn't set). */
    private ComponentMapper mapper;



    // - Initialisation ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new instance of {@link SystemDefaultFont}.
     * @param property {@link UIManager} property to query for the default font.
     * @param mapper   component mapper to use when the {@link UIManager} property isn't set.
     */
    public SystemDefaultFont(String property, ComponentMapper mapper) {
        UIManager.addPropertyChangeListener(this);
        this.property = property;
        this.mapper   = mapper;
    }



    // - DefaultFont implementation ------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    @Override
    public Font getFont(ThemeData data) {
        // If the font hasn't been identified yet...
        if(font == null)
            // ... try to retrieve it from the UIManager.
            if((font = UIManager.getFont(property)) == null)
                // If the current l&f didn't set the right propery, attempt to retrieve it from a component of the
                // desired type.
                if((font = mapper.getComponent().getFont()) == null)
                    // If that failed, defaults to SansSerif (guaranteed to be supported by the VM).
                    font = Font.decode("SansSerif");

        return font;
    }



    // - PropertyChangeListener implementation -------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    public void propertyChange(PropertyChangeEvent evt) {
        String name;

        // Monitors changes to both the global look & feel and the target property and react to them if necessary. 
        name = evt.getPropertyName().toLowerCase();
        if(name.equals("lookandfeel") || name.equalsIgnoreCase(property)) {
            Font oldFont;

            oldFont = font;

            // We first set font to null to ensure that the value is refreshed.
            font    = null;
            font    = getFont(null);

            if(!font.equals(oldFont))
                notifyChange(font);
        }
    }
}
