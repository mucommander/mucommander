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

import java.awt.Component;

import javax.swing.JComponent;

/**
* Component that presents tab with no header
* 
* @author Arik Hadas
*/
public class TabWithoutHeaderViewer<T extends Tab> extends TabsViewer<T> {

	/** The component to be displayed in the tab */
	private JComponent component;

	public TabWithoutHeaderViewer(TabsCollection<T> tabs, JComponent component) {
		super(component, tabs);

		this.component = component;
	}

	@Override
	public void requestFocus() {
		component.requestFocusInWindow();
	}

	@Override
	public int getSelectedTabIndex() {
		return 0;
	}

	@Override
	public void add(T tab, int index) {
		if (index > 0)
			throw new IllegalArgumentException("Unable to add tab at index > 0 to single tab display");
		add(tab);
	}

	@Override
	public void update(T tab, int index) { }

	@Override
	public void setSelectedTabIndex(int index) { }

	@Override
	public void add(T tab) { }

	@Override
	public T removeCurrentTab() { return null; }

	@Override
	public void removeOtherTabs() { }

	@Override
	public void removeTab(Component header) { }

	@Override
	public void removeDuplicateTabs() { }

	@Override
	public void removeTab(int index) { }
}
