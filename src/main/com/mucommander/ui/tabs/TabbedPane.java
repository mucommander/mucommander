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

package com.mucommander.ui.tabs;

import javax.swing.JTabbedPane;

/**
* TabbedPane that contains implementations of Tab interface
* 
* @author Arik Hadas
*/
public abstract class TabbedPane<T extends Tab> extends JTabbedPane {

	/**
	 * Add tab to the end of the tabbedpane 
	 * 
	 * @param tab - implementation of Tab interface
	 */
	public abstract void add(T tab);
	
	/**
	 * Add tab in a given index
	 * 
	 * @param tab - implementation of Tab interface
	 * @param index - the index in which the tab would be added
	 */
	public abstract void add(T tab, int index);
	
	/**
	 * Update tab in a given index
	 * The updated tab would be selected in the end of the operation
	 * 
	 * @param tab - implementation of Tab interface
	 * @param index - the index of the tab to be updated
	 */
	public abstract void update(T tab, int index);
}
