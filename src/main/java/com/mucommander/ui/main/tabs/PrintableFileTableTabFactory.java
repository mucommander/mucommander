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
import com.mucommander.ui.tabs.TabFactory;

/**
 * Factory for creating {@link com.mucommander.ui.main.tabs.FileTableTab} for presentation in {@link com.mucommander.ui.main.quicklist.TabsQL}
 * 
 * @author Arik Hadas
 */
public class PrintableFileTableTabFactory implements TabFactory<FileTableTab, FileTableTab> {

	public FileTableTab createTab(FileTableTab tab) {
		return new PrintableFileTableTab(tab);
	}

	/**
	 * Implementation of the Decorator design pattern which is used to modify the way FileTableTab
	 * is presented (by overriding its toString method) and the way it's compared to 
	 * FileTableTabFactory.DefaultFileTableTab instances (by overriding its equals method)
	 */
	private class PrintableFileTableTab extends FileTableTab {

		private FileTableTab tab;
		
		private PrintableFileTableTab(FileTableTab tab) {
			this.tab = tab;
		}

		@Override
		public void setLocation(FileURL location) {
			tab.setLocation(location);
		}

		@Override
		public FileURL getLocation() {
			return tab.getLocation();
		}

		@Override
		public void setLocked(boolean locked) {
			tab.setLocked(locked);
		}

		@Override
		public boolean isLocked() {
			return tab.isLocked();
		}

		@Override
		public void setTitle(String title) {
			tab.setTitle(title);
		}

		@Override
		public String getTitle() {
			return tab.getTitle();
		}

		@Override
		public LocalLocationHistory getLocationHistory() {
			return tab.getLocationHistory();
		}
		
		@Override
		public boolean equals(Object obj) {
			return tab == obj;
		}
		
		@Override
		public String toString() {
			return getDisplayableTitle();
		}
	}
}
