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

/**
* Factory that creates tabs displays.
* 
* @author Arik Hadas
*/
public interface TabsViewerFactory<T extends Tab> {

	/**
	 * Create tabs display for more than one tab
	 * The first tab will be selected
	 * 
	 * @param tabs - collection of the tabs
	 * @return tabs display for more than one tab
	 */
	TabsWithHeaderViewer<T> createTabsWithHeadersDisplay(TabsCollection<T> tabs);
	
	/**
	 * Create tabs display for a single tab
	 * 
	 * @param tabs - collection of the tabs
	 * @return tabs display for a single tab
	 */
	TabWithoutHeaderViewer<T> createTabWithoutHeaderDisplay(TabsCollection<T> tabs);
}
