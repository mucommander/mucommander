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

/**
 * {@link DefaultColor} implementation that maps to a value in the current theme.
 * <p>
 * This is typically useful to make sure that a color always defaults to the same value as another theme property.
 * Care should be exercised when using this class, however: it doesn't check for infinite recursion, meaning it's
 * entirely possible to freeze muCommander by linking a color to itself as a default.
 * </p>
 * @author Nicolas Rinaudo
 */
public class LinkedDefaultColor extends DefaultColor implements ThemeListener {
    // - Instance fields -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Identifier of the current theme color to default to. */
    private int colorId;




    // - Initialisation ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new instance of {@link LinkedDefaultColor}.
     * @param colorId identifier of the current theme color to default to.
     */
    public LinkedDefaultColor(int colorId) {
        this.colorId = colorId;
        ThemeData.addDefaultValuesListener(this);
    }



    // - DefaultColor implementation -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    @Override
    public Color getColor(ThemeData data) {
        return data.getColor(colorId); 
    }



    // - ThemeListener implementation ----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    public void colorChanged(ColorChangedEvent event) {
        if(event.getColorId() == colorId)
            notifyChange(event.getColor());
    }

    public void fontChanged(FontChangedEvent event) {
    }
}
