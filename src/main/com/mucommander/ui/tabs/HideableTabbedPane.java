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

package com.mucommander.ui.tabs;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JComponent;

/**
 * This component acts like a tabbedpane in which multiple tabs are presented in a JTabbedPane layout 
 * and single tab is presented without the JTabbedPane layout, only the tab's data.
 * 
 * When a single tab is presented and new tab is added this component makes a switch to JTabbedPane layout,
 * and when two tabs are presented and there is a removal of one of the tabs this component makes a switch
 * to JPanel layout that contains the data of the tab that is left.
 * 
 * This component also provides an interface for the other parts of the application to make operations
 * that influence the tabs layout.
 *  
 * @author Arik Hadas
 */
public class HideableTabbedPane<T extends Tab> extends JComponent implements TabsChangeListener {

	/* The tabs which are being displayed */
	private TabsCollection<T> tabs;
	/* The tabs display type - single-tab or multiple-tab */
	private TabsDisplay<T> display = new NullableTabsDisplay<T>();
	/* The factory that will be used to create the display type */
	private TabsDisplayFactory<T> tabsDisplayFactory;
	
	/**
	 * Constructor
	 *  
	 * @param tabsDisplayFactory - factory of tabs-display
	 */
	public HideableTabbedPane(TabsDisplayFactory<T> tabsDisplayFactory) {
		setLayout(new BorderLayout());

		// Set the tabs display factory
		this.tabsDisplayFactory = tabsDisplayFactory;
		
		// Initialize the tabs collection
		tabs = new TabsCollection<T>();
		// Register for tabs changes
		tabs.addTabsListener(this);
	}
	
	/***********************
	 * Tabs Actions Support
	 ***********************/
	
	/* Actions which are not depended on the display type (single/multiple tabs) */
	
	/**
	 * Add new tab
	 * 
	 * @param tab - new tab's data
	 */
	public void addTab(T tab) {
		tabs.add(tab);
	}
	
	/**
	 * Update the current displayed tab's data
	 * 
	 * @param tab - updated tab's data for the current displayed tab
	 */
	public void updateTab(T tab) {
		tabs.updateTab(display.getSelectedTabIndex(), tab);
	}
	
	/* Actions that depended on the display type (single/multiple tabs) */

	/**
	 * Remove tab with the given header
	 */
	public void removeTab(Component header) {
		display.removeTab(header);
	}
	
	/**
	 * Remove current displayed tab
	 */
	public void removeTab() {
		display.removeCurrentTab();
	}
	
	/**
	 * Remove all tabs except the current displayed tab
	 */
	public void removeOtherTabs() {
		display.removeOtherTabs();
	}
	
	/**
	 * Change the current displayed tab to the tab which is located to the right of the
	 * current displayed tab.
	 * If the current displayed tab is the rightmost tab, the leftmost tab will be displayed.
	 */
	public void nextTab() {
		display.nextTab();
	}
	
	/**
	 * Change the current displayed tab to the tab which is located to the left of the
	 * current displayed tab.
	 * If the current displayed tab is the leftmost tab, the rightmost tab will be displayed.
	 */
	public void previousTab() {
		display.previousTab();
	}
	
	/******************
	 * Private Methods
	 ******************/
	
	private void switchToMultipleTabs() {
		setTabsDisplay(tabsDisplayFactory.createMultipleTabsDisplay(tabs, 1));
	}
	
	private void switchToSingleTab() {
		setTabsDisplay(tabsDisplayFactory.createSingleTabsDisplay(tabs));
	}
	
	private void setTabsDisplay(TabsDisplay<T> display) {
		this.display.destroy();
		this.display = display;
		
		removeAll();
		add(display);
		validate();
		
		display.requestFocus();
	}
	
	/*********************
	 * TabsChangeListener 
	 *********************/

	@Override
	public void tabAdded(int index) {
		switch (tabs.count()) {
		case 2:
			switchToMultipleTabs();
			break;
		case 1:
			switchToSingleTab();
			break;
		default:
		}
	}

	@Override
	public void tabRemoved(int index) {
		if (tabs.count() == 1)
			switchToSingleTab();
	}
	
	@Override
	public void tabUpdated(int index) { }
}
