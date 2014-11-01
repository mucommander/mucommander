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
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.file.util.PathUtils;
import com.mucommander.commons.util.StringUtils;
import com.mucommander.core.LocalLocationHistory;
import com.mucommander.ui.tabs.Tab;

/**
 * Interface of tab in the {@link com.mucommander.ui.main.FolderPanel} that contains a {@link com.mucommander.ui.main.table.FileTable}
 *
 * @author Arik Hadas
 */
public abstract class FileTableTab implements Tab {

	/**
	 * Setter for the location presented in the tab
	 * 
	 * @param location the file that is going to be presented in the tab
	 */
	public abstract void setLocation(FileURL location);

	/**
	 * Getter for the location presented in the tab
	 * 
	 * @return the file that is being presented in the tab
	 */
	public abstract FileURL getLocation();
	
	/**
	 * Set the tab to be locked or unlocked according to the given flag
	 * 
	 * @param locked flag that indicates whether the tab should be locked or not
	 */
	public abstract void setLocked(boolean locked);
	
	/**
	 * Returns whether the tab is locked
	 * 
	 * @return indication whether the tab is locked
	 */
	public abstract boolean isLocked();

	/**
	 * Set the title of the tab to the given string
	 * 
	 * @param title - predefined title to be assigned to the tab, null for no predefined title
	 */
	public abstract void setTitle(String title);

	/**
	 * Returns the title that was assigned for the tab
	 * 
	 * @return the title that was assigned for the tab, null is returned if no title was assigned
	 */
	public abstract String getTitle();

	/**
	 * Returns a string representation for the tab:
	 *  the tab's fixed title will be returned if such title was assigned,
	 *  otherwise, a string representation will be created based on the tab's location:
	 *    for local file, the filename will be returned ("/" in case the root folder is presented)
	 *    for remote file, the returned pattern will be "\<host\>:\<filename\>"
	 * 
	 * @return String representation of the tab
	 */
	public String getDisplayableTitle() {
		String title = getTitle();

		return title != null ? title : createDisplayableTitleFromLocation(getLocation());
	}

	private String createDisplayableTitleFromLocation(FileURL location) {
		boolean local = location.getHost().equals(FileURL.LOCALHOST);

		return getHostRepresentation(location.getHost(), local) + getFilenameRepresentation(location.getFilename(), local);
	}

	private String getHostRepresentation(String host, boolean local) {
		return local ? "" : host + ":";
	}

	private String getFilenameRepresentation(String filename, boolean local) {
		// Under for OSes with 'root drives' (Windows, OS/2), remove the leading '/' character
		if(local && LocalFile.hasRootDrives() && filename != null)
			return PathUtils.removeLeadingSeparator(filename, "/");
		// Under other OSes, if the filename is empty return "/"
		else
			return filename == null ? "/" : filename;
	}

	/**
	 * Returns the tracker of the last accessed locations within the tab
	 * 
	 * @return tracker of the last accessed locations within the tab
	 */
	public abstract LocalLocationHistory getLocationHistory();
}
