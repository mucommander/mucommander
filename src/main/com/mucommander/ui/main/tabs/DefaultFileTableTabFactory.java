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

package com.mucommander.ui.main.tabs;

import com.mucommander.commons.file.FileURL;
import com.mucommander.core.LocalLocationHistory;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.tabs.TabFactory;

/**
 * Factory for creating regular {@link com.mucommander.ui.main.tabs.FileTableTab} presenting the given location
 * 
 * @author Arik Hadas
 */
public class DefaultFileTableTabFactory implements TabFactory<FileTableTab, FileURL> {

	private FolderPanel folderPanel;
	
	public DefaultFileTableTabFactory(FolderPanel folderPanel) {
		this.folderPanel = folderPanel;
	}

	public FileTableTab createTab(FileURL location) {
		if (location == null)
			throw new RuntimeException("Invalid location");

		return new DefaultFileTableTab(location, folderPanel);
	}

	class DefaultFileTableTab implements FileTableTab {
		
		/** The location presented in this tab */
		private FileURL location;

		/** Flag that indicates whether the tab is locked or not */
		private boolean locked;
		
		/** History of accessed location within the tab */
		private LocalLocationHistory locationHistory;

		/**
		 * Private constructor
		 * 
		 * @param location - the location that would be presented in the tab
		 */
		private DefaultFileTableTab(FileURL location, FolderPanel folderPanel) {
			this.location = location;
			this.locked = false;
			locationHistory = new LocalLocationHistory(folderPanel);
		}
		
		public void setLocation(FileURL location) {
			this.location = location;
			
			// add location to the history (See LocalLocationHistory to see how it handles the first location it gets)
			locationHistory.tryToAddToHistory(location);
		}

		public FileURL getLocation() {
			return location;
		}
		
		public void setLocked(boolean locked) {
			this.locked = locked;
		}

		public boolean isLocked() {
			 return locked;
		}

		public LocalLocationHistory getLocationHistory() {
			return locationHistory;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof FileTableTab)
				return location.equals(((FileTableTab) obj).getLocation()) &&
					   locked == ((FileTableTab) obj).isLocked();
			return false;
		}

		@Override
		public int hashCode() {
			return location.hashCode();
		}
	}
}
