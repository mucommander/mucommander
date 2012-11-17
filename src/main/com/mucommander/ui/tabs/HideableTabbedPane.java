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

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Iterator;

import javax.swing.JComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;

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
public class HideableTabbedPane<T extends Tab> extends JComponent implements TabsEventListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(HideableTabbedPane.class);
	
	/* The tabs which are being displayed */
	private TabsCollection<T> tabsCollection;
	/* The tabs display type (with/without tabs headers)
	 * It is initialize as nullable so that it can be destroyed when it's replaced for the first time (see @{link tabAdded()})*/
	private TabsViewer<T> tabsViewer = new NullableTabsViewer<T>();
	/* The factory that will be used to create the display type */	
	private TabsViewerFactory<T> tabsDisplayFactory;
	
	/**
	 * Constructor
	 *  
	 * @param tabsDisplayFactory - factory of tabs-display
	 */
	public HideableTabbedPane(TabsViewerFactory<T> tabsDisplayFactory) {
		setLayout(new BorderLayout());

		// Set the tabs display factory
		this.tabsDisplayFactory = tabsDisplayFactory;
		
		// Initialize the tabs collection
		tabsCollection = new TabsCollection<T>();
		// Register for tabs changes
		tabsCollection.addTabsListener(this);
	}

	/**
	 * This function returns an iterator that points to the current Tabs contained in the TabbedPane
	 * 
	 * @return Iterator that points to current Tabs
	 */
	public Iterator<T> iterator() {
		return tabsCollection.iterator();
	}

	/**
	 * Select the given tab
	 * 
	 * @param tab the tab to be selected
	 */
	public void selectTab(T tab) {
		int index = tabsCollection.indexOf(tab);
		
		if (index != -1)
			selectTab(index);
		else
			LOGGER.error("Was requested to change to non-existing tab, ignoring");
	}
	
	/**
	 * Select the tab at the given index
	 * An exception will be thrown if no tab exists in the given index
	 * 
	 * @param index of the tab to be selected
	 */
	public void selectTab(int index) {
		tabsViewer.setSelectedTabIndex(index);
	}
	
	/**
	 * Return the index of the selected tab
	 * 
	 * @return index of the selected tab
	 */
	public int getSelectedIndex() {
		return tabsViewer.getSelectedTabIndex();
	}
	
	protected TabsCollection<T> getTabs() {
		return tabsCollection;
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
	protected void addTab(T tab) {
		tabsCollection.add(tab);
	}
	
	/**
	 * Add new tab and select it
	 * 
	 * @param tab - new tab's data
	 */
	protected void addAndSelectTab(T tab) {
		addTab(tab);
		tabsViewer.setSelectedTabIndex(tabsCollection.count()-1);
	}
	
	/**
	 * Update the current displayed tab's data with the given {@link TabUpdater}
	 * 
	 * @param updater - object that will be used to update the tab
	 */
	protected void updateCurrentTab(TabUpdater<T> updater) {
		tabsCollection.updateTab(getSelectedIndex(), updater);
	}
	
	/* Actions that depended on the display type (single/multiple tabs) */

	/**
	 * Remove tab with the given header
	 */
	protected void removeTab(Component header) {
		tabsViewer.removeTab(header);
	}
	
	/**
	 * Remove current displayed tab
	 */
	protected T removeTab() {
		return tabsViewer.removeCurrentTab();
	}
	
	/**
	 * Remove duplicate tabs
	 */
	protected void removeDuplicateTabs() {
		tabsViewer.removeDuplicateTabs();
	}
	
	/**
	 * Remove all tabs except the current displayed tab
	 */
	protected void removeOtherTabs() {
		tabsViewer.removeOtherTabs();
	}
	
	/**
	 * Change the current displayed tab to the tab which is located to the right of the
	 * current displayed tab.
	 * If the current displayed tab is the rightmost tab, the leftmost tab will be displayed.
	 */
	public void nextTab() {
		tabsViewer.nextTab();
	}
	
	/**
	 * Change the current displayed tab to the tab which is located to the left of the
	 * current displayed tab.
	 * If the current displayed tab is the leftmost tab, the rightmost tab will be displayed.
	 */
	public void previousTab() {
		tabsViewer.previousTab();
	}
	
	/******************
	 * Private Methods
	 ******************/
	
	private void switchToTabsWithHeaders() {
		setTabsViewer(tabsDisplayFactory.createTabsWithHeadersDisplay(tabsCollection));
	}
	
	private void switchToTabWithoutHeader() {
		setTabsViewer(tabsDisplayFactory.createTabWithoutHeaderDisplay(tabsCollection));
	}
	
	private void setTabsViewer(TabsViewer<T> tabsViewer) {
		this.tabsViewer.destroy();
		this.tabsViewer = tabsViewer;
		
		removeAll();
		add(tabsViewer);
		validate();
		
		tabsViewer.requestFocus();
	}
	
	/********************
	 * Protected Methods
	 ********************/
	
	/**
	 * Returns the tab at the given index
	 * An exception will be thrown if no tab exists in the given index
	 * 
	 * @param index of the requested tab
	 * @return tab in the given index
	 */
	protected T getTab(int index) {
		return tabsCollection.get(index);
	}
	
	
	/************************************
	 * TabsChangeListener Implementation
	 ************************************/

	public void tabAdded(int index) {
		// The number of tabs including the one that was just been added
		int nbTabs = tabsCollection.count();
		
		switch (nbTabs) {
		case 2:
			switchToTabsWithHeaders();
			
			break;
		case 1:
			boolean alwaysShowSingleTabHeader = MuConfigurations.getPreferences().getVariable(MuPreference.SHOW_SINGLE_TAB_HEADER, MuPreferences.DEFAULT_SHOW_SINGLE_TAB_HEADER); 
			
			if (alwaysShowSingleTabHeader)
				switchToTabsWithHeaders();
			else
				switchToTabWithoutHeader();
			
			break;
		default:
			tabsViewer.add(tabsCollection.get(index), index);
		}
	}

	public void tabRemoved(int index) {
		// The number of tabs without the one that was just been removed
		int nbTabs = tabsCollection.count();
		
		// TODO: decide what to do in case nbTabs == 0
		
		if (nbTabs == 1) {
			boolean alwaysShowSingleTabHeader = MuConfigurations.getPreferences().getVariable(MuPreference.SHOW_SINGLE_TAB_HEADER, MuPreferences.DEFAULT_SHOW_SINGLE_TAB_HEADER);
			
			if (!alwaysShowSingleTabHeader)
				switchToTabWithoutHeader();
		}
		else
			tabsViewer.removeTab(index);
	}
	
	public void tabUpdated(int index) {
		tabsViewer.update(tabsCollection.get(index), index);
	}
}
