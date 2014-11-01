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

package com.mucommander.core;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileProtocols;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.ui.main.FolderPanel;

/**
 * This class maintains a history of visited locations for a given tab, and provides methods to go back and go forward
 * in the history.
 *
 * <p>There is a limit to the number of locations the history can contain, defined by {@link #HISTORY_CAPACITY}.</p>
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class LocalLocationHistory {
	private static final Logger LOGGER = LoggerFactory.getLogger(LocalLocationHistory.class);

	/** Maximum number of elements the folder history can contain */
	private final static int HISTORY_CAPACITY = 100;

	/** List of visited locations, ordered by last visit date */
	private List<FileURL> history = new Vector<FileURL>(HISTORY_CAPACITY+1);

	/** Index of current folder in history */
	private int historyIndex = -1;

	/** FolderPanel which is being monitored */
	private FolderPanel folderPanel; 

	/** Last folder which can be recalled on next startup */
	private String lastRecallableFolder;


	/**
	 * Creates a new FolderHistory instance which will keep track of visited folders in the given FolderPanel.
	 */
	public LocalLocationHistory(FolderPanel folderPanel) {
		this.folderPanel = folderPanel;
	}

	/**
	 * Adds the specified folder to history. The folder won't be added if the previous folder is the same.
	 *
	 * <p>This method is called by FolderPanel each time a folder is changed.
	 */
	public void tryToAddToHistory(FileURL folderURL) {
		// Do not add folder to history if new current folder is the same as previous folder
		if (historyIndex<0 || !folderURL.equals(history.get(historyIndex), false, false))
			addToHistory(folderURL);

		// Save last recallable folder on startup, only if :
		//  - it is a directory on a local filesytem
		//  - it doesn't look like a removable media drive (cd/dvd/floppy), especially in order to prevent
		// Java from triggering that dreaded 'Drive not ready' popup.
		LOGGER.trace("folder="+folderURL);
		if(folderURL.getScheme().equals(FileProtocols.FILE)) {
			AbstractFile folder = FileFactory.getFile(folderURL);
			if (folder.isDirectory() && (folder instanceof LocalFile) && !((LocalFile)folder.getRoot()).guessRemovableDrive()) {
				this.lastRecallableFolder = folder.getAbsolutePath();
				LOGGER.trace("lastRecallableFolder= "+lastRecallableFolder);
			}
		}
	}

	private void addToHistory(FileURL folderURL) {
		int historySize = history.size();

		historyIndex++;

		// Delete 'forward' history items if any
		for(int i=historyIndex; i<historySize; i++) {
			history.remove(historyIndex);
		}

		// If capacity is reached, remove first folder
		if(history.size()>=HISTORY_CAPACITY) {
			history.remove(0);
			historyIndex--;
		}

		// Add previous folder to history
		history.add(folderURL);
	}

	/**
	 * Changes current folder to be the previous one in folder history.
	 * Does nothing if there is no previous folder in history. 
	 */
	public synchronized void goBack() {
		if (historyIndex==0)
			return;

		folderPanel.tryChangeCurrentFolder(history.get(--historyIndex));
	}

	/**
	 * Changes current folder to be the next one in folder history.
	 * Does nothing if there is no next folder in history. 
	 */
	public synchronized void goForward() {
		if (historyIndex==history.size()-1)
			return;

		folderPanel.tryChangeCurrentFolder(history.get(++historyIndex));
	}


	/**
	 * Returns <code>true</code> if there is at least one folder 'back' in the history.
	 */
	public boolean hasBackFolder() {
		return historyIndex>0;
	}

	/**
	 * Returns <code>true</code> if there is at least one folder 'forward' in the history.
	 */
	public boolean hasForwardFolder() {
		return historyIndex!=history.size()-1;
	}


	/**
	 * Returns a list of 'back' folders, most recently visited folder first. The returned array may be empty if there
	 * currently isn't any 'back' folder in history, but may never be null.
	 */
	public FileURL[] getBackFolders() {
		if(!hasBackFolder())
			return new FileURL[0];

		int backLen = historyIndex;
		FileURL urls[] = new FileURL[backLen];

		int cur = 0;
		for(int i=historyIndex-1; i>=0; i--)
			urls[cur++] = history.get(i);

		return urls;
	}


	/**
	 * Returns a list of 'forward' folders, most recently visited folder first. The returned array may be empty if there
	 * currently isn't any 'forward' folder in history, but may never be null.
	 */
	public FileURL[] getForwardFolders() {
		if(!hasForwardFolder())
			return new FileURL[0];

		int historySize = history.size();
		FileURL urls[] = new FileURL[historySize-historyIndex-1];

		int cur = 0;
		for(int i=historyIndex+1; i<historySize; i++)
			urls[cur++] = history.get(i);

		return urls;
	}

	/**
	 * Returns true if the folder history contains the given FileURL, either as a back or forward folder, or as the
	 * current folder.
	 */
	public boolean historyContains(FileURL folderURL) {
		return history.contains(folderURL);
	}


	/**
	 * Returns the last visited folder that can be saved when the application terminates, and recalled next time
	 * the application is started.
	 *
	 * <p>The returned folder will NOT be a folder on a remote filesystem
	 * which would be likely not to be reachable next time the app is started, or a removable media drive
	 * (cd/dvd/floppy) under Windows, which would trigger a nasty 'drive not ready' popup dialog if the drive
	 * is not available or the media has changed.
	 */
	public String getLastRecallableFolder() {
		return this.lastRecallableFolder;
	}
}
