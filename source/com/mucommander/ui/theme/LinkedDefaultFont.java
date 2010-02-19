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

import java.awt.Font;

/**
 * {@link DefaultFont} implementation that maps to a value in the current theme.
 * <p>
 * This is typically useful to make sure that a font always defaults to the same value as another theme property.
 * Care should be exercised when using this class, however: it doesn't check for infinite recursion, meaning it's
 * entirely possible to freeze muCommander by linking a font to itself as a default.
 * </p>
 * @author Nicolas Rinaudo
 */
public class LinkedDefaultFont extends DefaultFont implements ThemeListener {
    // - Instance fields -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Identifier of the current theme font to default to. */
    private int id;



    // - Initialisation ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new instance of {@link LinkedDefaultFont}.
     * @param id identifier of the current theme font to default to.
     */
    public LinkedDefaultFont(int id) {
        this.id = id;
    }



    // - DefaultFont implementation ------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    @Override
    public Font getFont(ThemeData data) {
        return data.getFont(id);
    }



    // - ThemeListener implementation ----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    public void colorChanged(ColorChangedEvent event) {
    }

    public void fontChanged(FontChangedEvent event) {
        if(event.getFontId() == id)
            notifyChange(event.getFont());
    }
}
