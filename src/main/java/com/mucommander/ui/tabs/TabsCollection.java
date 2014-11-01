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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
* Collection of tabs
* The collection is iterable, and can notify listeners about added/removed/updated tabs
* 
* @author Arik Hadas
*/
public class TabsCollection<T extends Tab> implements java.lang.Iterable<T> {
	
	/** List of tabs */
	private List<T> collection = new ArrayList<T>();
	
	/** Listeners that were registered to be notified when tabs are added/removed/updated */
	private WeakHashMap<TabsEventListener, ?> tabsListeners = new WeakHashMap<TabsEventListener, Object>();
	
	/**
	 * Empty constructor
	 */
	public TabsCollection() {
	}
	
	/**
	 * Constructor that creates the collection with a single given tab
	 * 
	 * @param tab - a tab
	 */
	public TabsCollection(T tab) {
		collection.add(tab);
	}
	
	/**
	 * Constructor that creates the collection with multiple given tabs
	 * 
	 * @param tabs - list of tabs
	 */
	public TabsCollection(List<T> tabs) {
		collection.addAll(tabs);
	}
	
	/**
	 * Add the given tab to the collection
	 * The tab would be inserted in the last index in the collection
	 * 
	 * @param tab - tab
	 */
	public void add(T tab) {
		add(tab, count());
	}
	
	/**
	 * Add the given tab to the collection in a given index
	 * 
	 * @param tab - tab
	 * @param index - the index in which the tab would be inserted in the collection
	 */
	public void add(T tab, int index) {
		collection.add(index, tab);
		fireTabAdded(count() - 1);
	}
	
	/**
	 * Update the tab in the given index with the given {@link TabUpdater}
	 * 
	 * @param index - the index of the tab to be updated
	 * @param updater - the object that will be used to update the tab
	 */
	public void updateTab(int index, TabUpdater<T> updater) {
		updater.update(get(index));
		fireTabUpdated(index);
	}
	
	/**
	 * Remove the tab in the given index
	 * If there are less than two tabs in the collection, the tab
	 * won't be removed in order to prevent a situation in which
	 * we remain with no tabs at all
	 * 
	 * @param index - the index of the tab to be removed
	 */
	public T remove(int index) {
		if (collection.size() > 1) {
			T tab = collection.remove(index);
			fireTabRemoved(index);
			return tab;
		}
		return null;
	}
	
	/**
	 * Return the tab in the given index
	 * 
	 * @param index - the index of the tab to be returned
	 * @return the tab in the given index
	 */
	public T get(int index) {
		return collection.get(index);
	}
	
	/**
	 * Return the number of tabs contained in the collection
	 * 
	 * @return the number of tabs contained in the collection
	 */
	public int count() {
		return collection.size();
	}
	
	/**
	 * Return the index of the given tab in the collection
	 * 
	 * @return the index of the given tab or -1 if the tab is not exist in the collection
	 */
	public int indexOf(T tab) {
		return collection.indexOf(tab);
	}
	
	/********************
	 * Listeners support
	 ********************/
	
	/**
	 * Add a given listener to the listeners to be notified about tabs-changes
	 * 
	 * @param listener - object that implements TabsChangeListener interface
	 */
	public synchronized void addTabsListener(TabsEventListener listener) {
        tabsListeners.put(listener, null);
    }

	/**
	 * Remove a given listener from the listeners to be notifies about tabs-changes
	 * 
	 * @param listener - object that implements TabsChangeListener interface
	 */
    public synchronized void removeTabsListener(TabsEventListener listener) {
    	tabsListeners.remove(listener);
    }
    
    /**
     * Notify the registered listeners about addition of tab in the given index
     * 
     * @param index - the index of the added tab
     */
    public synchronized void fireTabAdded(int index) {
    	Set<TabsEventListener> listeners = new HashSet<TabsEventListener>(tabsListeners.keySet());
    	for(TabsEventListener listener : listeners)
            listener.tabAdded(index);
    }
    
    /**
     * Notify the registered listeners about removal of tab in the given index
     * 
     * @param index - the index in which the removed tab was located
     */
    public synchronized void fireTabRemoved(int index) {
    	Set<TabsEventListener> listeners = tabsListeners.keySet();
        for(TabsEventListener listener : listeners)
            listener.tabRemoved(index);
    }
    
    /**
     * Notify the registered listeners about tab that was updated in the given index
     * 
     * @param index - the index of the updated tab
     */
    public synchronized void fireTabUpdated(int index) {
    	Set<TabsEventListener> listeners = tabsListeners.keySet();
        for(TabsEventListener listener : listeners)
            listener.tabUpdated(index);
    }
    
	/**************************
	 * Iterable implementation
	 **************************/

	public Iterator<T> iterator() {
		return collection.iterator();
	}
}
