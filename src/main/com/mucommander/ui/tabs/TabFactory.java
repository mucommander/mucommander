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
 * interface for factory of {@link com.mucommander.ui.tabs.Tab} 
 * 
 * @author Arik Hadas
 *
 * @param <T> kind-of Tab
 * @param <K> parameter for initiating the new tab
 */
public interface TabFactory<T extends Tab, K> {

	/**
	 * This method returns new {@link com.mucommander.ui.tabs.Tab} based on the given parameter
	 * 
	 * @param k a parameter that the new tab is based on
	 * @return instance of subclass of {@link com.mucommander.ui.tabs.Tab}
	 */
	public T createTab(K k);

}
