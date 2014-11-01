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

import java.awt.*;

/**
 * {@link DefaultFont} implementation that maps to a fixed value.
 * @author Nicolas Rinaudo
 */
public class FixedDefaultFont extends DefaultFont {
    // - Instance fields -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Font to default to. */
    private Font font;


    // - Initialisation ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new instance of {@link FixedDefaultFont}.
     * @param font font to default to.
     */
    public FixedDefaultFont(Font font) {
        this.font = font;
    }



    // - DefaultFont implementation ------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    @Override
    public Font getFont(ThemeData data) {
        return font;
    }
}
