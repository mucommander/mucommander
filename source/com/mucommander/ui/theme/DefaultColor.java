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

import java.awt.Color;
import java.util.Vector;

/**
 * Represents a default value for a theme color.
 * <p>
 * Instances of this class are used to provide default values for theme colors and notify the current theme when they
 * are modified.
 * </p>
 * <p>
 * If, for example, a color should default to the look and feel defined TextArea foreground color and the look and feel
 * is changed, the corresponding {@link DefaultColor} instance will catch that event and notify the current theme of the
 * change.
 * </p>
 * @author Nicolas Rinaudo
 */
public abstract class DefaultColor {
    // - Instance fields -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** List of colors linked to this default value. */
    private Vector<Integer> linkedColors;



    // - Initialisation ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new instance of {@link DefaultColor}.
     */
    protected DefaultColor() {
        linkedColors = new Vector<Integer>();
    }



    // - Event propagation ---------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Notifies the current theme of a default value change to all linked colors.
     * @param color new default color value.
     */
    protected void notifyChange(Color color) {
        for(int i : linkedColors)
            ThemeData.triggerColorEvent(i, color);    
    }

    /**
     * Registers a theme color as defaulting to the current instance.
     * <p>
     * If the default color's value were to change, the current theme will automatically be notified of the change and
     * ultimately propagate to all registered theme listeners if necessary.
     * </p>
     * @param colorId identifier of the color that uses this instance as a default value.
     */
    public void link(Integer colorId) {
        linkedColors.add(colorId);
    }



    // - Abstract methods ----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the color this default value represents.
     * @param  data contains all the current theme values.
     * @return      the color this default value represents.
     */
    public abstract Color getColor(ThemeData data);
}
