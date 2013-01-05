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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.event.ChangeListener;

/**
* Component that presents tabs with headers
* 
* @author Arik Hadas
*/
public class TabsWithHeaderViewer<T extends Tab> extends TabsViewer<T> {

	private TabsCollection<T> tabsCollection;
	private TabbedPane<T> tabbedpane;

	public TabsWithHeaderViewer(TabsCollection<T> tabs, TabbedPane<T> tabbedpane) {
		super(tabbedpane, tabs);

		this.tabsCollection = tabs;
		this.tabbedpane = tabbedpane;

		int index = 0;
		for (T tab : tabs)
			tabbedpane.add(tab, index++);
	}

	/**************
	 * TabsViewer
	 **************/

	@Override
	public int getSelectedTabIndex() {
		return tabbedpane.getSelectedIndex();
	}

	@Override
	public void removeTab(int index) {
		tabbedpane.remove(index);
	}

	@Override
	public void addChangeListener(ChangeListener listener) { 
		tabbedpane.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) { 
		tabbedpane.removeChangeListener(listener);
	}

	@Override
	public void add(T tab) {
		add(tab, tabsCollection.count());
	}

	@Override
	public void add(T tab, int index) {
		tabbedpane.add(tab, index);
	}

	@Override
	public void update(T tab, int index) {
		tabbedpane.update(tab, index);
	}

	@Override
	public void setSelectedTabIndex(int index) {
		tabbedpane.setSelectedIndex(index);
	}

	@Override
	public void requestFocus() {
		tabbedpane.requestFocusInWindow();
	}

	@Override
	public T removeCurrentTab() {
		T tab = tabsCollection.get(getSelectedTabIndex());
		tabsCollection.remove(getSelectedTabIndex());
		return tab;
	}

	@Override
	public void removeDuplicateTabs() {
		// a Set that will contain the tabs we've seen
		Set<T> visitedTabs = new HashSet<T>();
		// a Set that will contain the tabs which are duplicated
		Set<T> duplicatedTabs = new HashSet<T>(); 
		// The index of the selected tab
		int selectedTabIndex = getSelectedTabIndex();

		// add all duplicated tabs to the duplicatedTab Set
		Iterator<T> existingTabsIterator = tabsCollection.iterator();
		while (existingTabsIterator.hasNext()) {
			T tab = existingTabsIterator.next();
			if (!visitedTabs.add(tab))
				duplicatedTabs.add(tab);
		}

		// remove all duplicated tabs which are identical to the selected tab without
		// changing the tab selection
		T selectedTab = tabsCollection.get(selectedTabIndex);
		if (duplicatedTabs.remove(selectedTab)) {
			int removedTabsCount = 0;
			int tabsCount = tabsCollection.count();
			for (int i=0; i<tabsCount; ++i) {
				if (i == selectedTabIndex) // do not remove the selected tab
					continue;
				if (selectedTab.equals(tabsCollection.get(i-removedTabsCount)))
					tabsCollection.remove(i-removedTabsCount++);
			}
		}

		// remove all other duplicated tabs
		for (int i = 0; i < tabsCollection.count(); ++i) {
			T currentTab = tabsCollection.get(i);
			if (duplicatedTabs.remove(currentTab)) {
				for (int j = i + 1; j < tabsCollection.count(); ++j)
					if (currentTab.equals(tabsCollection.get(j)))
						tabsCollection.remove(j--);
			}
		}
	}

	@Override
	public void removeOtherTabs() {
		int selectedTabIndex = getSelectedTabIndex();

		for (int i=0; i<selectedTabIndex; ++i)
			tabsCollection.remove(0);

		for(int i=tabsCollection.count()-1; i>0; --i)
			tabsCollection.remove(1);		
	}

	@Override
	public void removeTab(Component header) {
		tabsCollection.remove(tabbedpane.indexOfTabComponent(header));
	}
}
