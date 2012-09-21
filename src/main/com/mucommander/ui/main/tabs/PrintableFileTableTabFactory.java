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

import com.mucommander.commons.file.AbstractFile;
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
	private class PrintableFileTableTab implements FileTableTab {

		private FileTableTab tab;
		
		private PrintableFileTableTab(FileTableTab tab) {
			this.tab = tab;
		}
		
		public void setLocation(AbstractFile location) {
			tab.setLocation(location);
		}

		public AbstractFile getLocation() {
			return tab.getLocation();
		}

		public void setLocked(boolean locked) {
			tab.setLocked(locked);
		}

		public boolean isLocked() {
			return tab.isLocked();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof FileTableTabFactory.DefaultFileTableTab)
				return tab == obj;
			
			return super.equals(obj);
		}
		
		@Override
		public String toString() {
			return getLocation().toString();
		}
	}
}
