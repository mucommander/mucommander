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
import java.util.Vector;

/**
 * Represents a default value for a theme font.
 * <p>
 * Instances of this class are used to provide default values for theme fonts and notify the current theme when they
 * are modified.
 * </p>
 * <p>
 * If, for example, a font should default to the look and feel defined TextArea font and the look and feel is changed,
 * the corresponding {@link DefaultFont} instance will catch that event and notify the current theme of the change.
 * </p>
 * @author Nicolas Rinaudo
 */
public abstract class DefaultFont {
    // - Instance fields -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** List of fonts linked to this default value. */
    private Vector<Integer> linkedFonts;



    // - Initialisation ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new instance of {@link DefaultFont}.
     */
    protected DefaultFont() {
        linkedFonts = new Vector<Integer>();
    }



    // - Event propagation ---------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Registers a theme font as defaulting to the current instance.
     * <p>
     * If the default font's value were to change, the current theme will automatically be notified of the change and
     * ultimately propagate to all registered theme listeners if necessary.
     * </p>
     * @param fontId identifier of the font that uses this instance as a default value.
     */
    public void link(Integer fontId) {
        linkedFonts.add(fontId);
    }

    /**
     * Notifies the current theme of a default value change to all linked fonts.
     * @param font new default font value.
     */
    protected void notifyChange(Font font) {
        for(int i : linkedFonts)
            ThemeData.triggerFontEvent(i, font);
    }



    // - Abstract methods ----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the font this default value represents.
     * @param  data contains all the current theme values.
     * @return      the font this default value represents.
     */
    public abstract Font getFont(ThemeData data);
}
