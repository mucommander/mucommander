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

package com.mucommander.ui.main.quicklist;

import java.util.LinkedList;

import javax.swing.ImageIcon;

import com.mucommander.text.Translator;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.quicklist.QuickListWithIcons;

/**
 * This quick list shows recently accessed locations.
 * 
 * @author Arik Hadas
 */
public class RecentLocationsQL extends QuickListWithIcons implements LocationListener{
	private static int MAX_ELEMENTS = 11;
	private LinkedList linkedList;

	public RecentLocationsQL() {
		super(Translator.get("recent_locations_quick_list.title"), Translator.get("recent_locations_quick_list.empty_message"));
		
		linkedList = new LinkedList();
	}

	protected void acceptListItem(String item) {
		folderPanel.tryChangeCurrentFolder(item);
	}

	public void locationCancelled(LocationEvent locationEvent) {}

	public void locationChanged(LocationEvent locationEvent) {
		String currentLocation = locationEvent.getFolderPanel().getCurrentFolder().getAbsolutePath();
			
		if (!linkedList.remove(currentLocation) && linkedList.size() >= MAX_ELEMENTS)
			linkedList.removeLast();
		linkedList.addFirst(currentLocation);
	}

	public void locationChanging(LocationEvent locationEvent) {}

	public void locationFailed(LocationEvent locationEvent) {}

	public Object[] getData() {
		LinkedList list = (LinkedList) linkedList.clone();

		if (!list.remove(folderPanel.getCurrentFolder().getAbsolutePath()))
			list.removeLast();
		
		return list.toArray();
	}

	protected ImageIcon itemToIcon(String value) {
		return getImageIconOfFile(value);
	}
}
