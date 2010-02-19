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

package com.mucommander.ui.text;

import javax.swing.JComponent;
import java.awt.Font;

/**
 * This class contains a set of helper methods that allow to easily change the font of a <code>JComponent</code>
 *
 * @author Maxence Bernard
 */
public class FontUtils {

    /**
     * Changes the style of the given component's font. Other attributes of the font are left unchanged.
     *
     * @param comp the component for which to change the font
     * @param newStyle the new Font style to use, see <code>java.awt.Font</code> for allowed values
     * @return the component that was passed, for convenience only
     */
    public static JComponent changeStyle(JComponent comp, int newStyle) {
        comp.setFont(comp.getFont().deriveFont(newStyle));
        return comp;
    }

    /**
     * Changes the size of the given component's font. Other attributes of the font are left unchanged.
     *
     * @param comp the component for which to change the font
     * @param newSize the new Font size to use, see <code>java.awt.Font</code> for allowed values
     * @return the component that was passed, for convenience only
     */
    public static JComponent changeSize(JComponent comp, float newSize) {
        comp.setFont(comp.getFont().deriveFont(newSize));
        return comp;
    }

    /**
     * Changes the style and size of the given component's font. Other attributes of the font are left unchanged.
     *
     * @param comp the component for which to change the font
     * @param newStyle the new Font style to use, see <code>java.awt.Font</code> for allowed values
     * @param newSize the new Font size to use, see <code>java.awt.Font</code> for allowed values
     * @return the component that was passed, for convenience only
     */
    public static JComponent changeStyleAndSize(JComponent comp, int newStyle, float newSize) {
        comp.setFont(comp.getFont().deriveFont(newStyle, newSize));
        return comp;
    }

    /**
     * Changes the style of the given component's font to {@link java.awt.Font#BOLD}.
     * Other attributes of the font are left unchanged.
     *
     * @param comp the component for which to change the font
     * @return the component that was passed, for convenience only
     */
    public static JComponent makeBold(JComponent comp) {
        changeStyle(comp, Font.BOLD);
        return comp;
    }

    /**
     * Changes the style of the given component's font to {@link java.awt.Font#ITALIC}.
     * Other attributes of the font are left unchanged.
     *
     * @param comp the component for which to change the font
     * @return the component that was passed, for convenience only
     */
    public static JComponent makeItalic(JComponent comp) {
        changeStyle(comp, Font.BOLD);
        return comp;
    }

    /**
     * Changes the style of the given component's font to {@link java.awt.Font#BOLD}|{@link java.awt.Font#ITALIC}.
     * Other attributes of the font are left unchanged.
     *
     * @param comp the component for which to change the font
     * @return the component that was passed, for convenience only
     */
    public static JComponent makeBoldItalic(JComponent comp) {
        changeStyle(comp, Font.BOLD|Font.ITALIC);
        return comp;
    }

    /**
     * Changes the style of the given component's font to {@link java.awt.Font#PLAIN}.
     * Other attributes of the font are left unchanged.
     *
     * @param comp the component for which to change the font
     * @return the component that was passed, for convenience only
     */
    public static JComponent makePlain(JComponent comp) {
        changeStyle(comp, Font.PLAIN);
        return comp;
    }
    /**
     * Decreases the size of the given component's font by 2 units. Other attributes of the font are left unchanged.
     *
     * @param comp the component for which to change the font
     * @return the component that was passed, for convenience only
     */
    public static JComponent makeMini(JComponent comp) {
        changeSize(comp, comp.getFont().getSize()-2);
        return comp;
    }
}
