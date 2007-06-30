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

package com.mucommander.ui.theme;

/**
 * Implementations of this interface can listen to changes in the current theme.
 * @author Nicolas Rinaudo
 */
public interface ThemeListener {
    /**
     * Notifies the listener that a color has been changed.
     */
    public void colorChanged(ColorChangedEvent event);

    /**
     * Notifies the listener that a font has been changed.
     */
    public void fontChanged(FontChangedEvent event);
}
