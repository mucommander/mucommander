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

package com.mucommander.ui.combobox;


/**
 * Interface to be implemented by classes that wish to be notified of selections occuring on a {@link SaneComboBox}.
 * @author Maxence Bernard
 */
public interface ComboBoxListener {
    /**
     * This method is called when an item has been selected from the specified combo box popup menu.
     * The item may have been selected either with the 'Enter' key, or by clicking on the item.
     *
     * @param source the SaneComboBox on which the event was triggered
     */
    public void comboBoxSelectionChanged(SaneComboBox source);

}
