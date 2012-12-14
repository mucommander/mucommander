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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.conf.Configuration;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuSnapshot;
import com.mucommander.ui.event.LocationAdapter;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

/**
 * This class tracks location changing events, in every {@link FolderPanel} or {@link MainFrame},
 * and saves those locations, thus creating a global location history tracking.
 * 
 * <p>FolderHistory also keeps track of the last visited location so that it can be saved and recalled the next time the
 * application is started.</p>
 * 
 * @author Arik Hadas
 */
public class GlobalLocationHistory extends LocationAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalLocationHistory.class);
	
	/** Singleton instance */
	private final static GlobalLocationHistory instance = new GlobalLocationHistory();
	
	/** Locations that were accessed */
	private Set<FileURL> history = new LinkedHashSet<FileURL>();
	
	/** Maximum number of location that would be saved */
	private static final int MAX_CAPACITY = 100;
	
	/**
	 * Private Constructor
	 */
	private GlobalLocationHistory() {
		Configuration snapshot = MuConfigurations.getSnapshot();

		// Restore the global history from last run
		int nbLocations = snapshot.getIntegerVariable(MuSnapshot.getRecentLocationsCountVariable());
    	for (int i=0; i<nbLocations; ++i) {
    		String filePath = snapshot.getVariable(MuSnapshot.getRecentLocationVariable(i));
			try {
				history.add(FileURL.getFileURL(filePath));
			} catch (MalformedURLException e) {
				LOGGER.debug("Got invalid URL from the snapshot file: " + filePath, e);
			}
    	}
	}

	/**
	 * Returns Singleton instance of this class
	 * 
	 * @return Singleton instance of this class
	 */
	public static final GlobalLocationHistory Instance() {
		return instance;
	}

	/**
	 * Returns all the tracked locations as a {@link Set} of {@link AbstractFile}
	 * The locations are turned in a reverse insertion-order, that means that the last accessed location would be the first  
	 * 
	 * @return all the tracked locations
	 */
	public List<FileURL> getHistory() {
		return new ArrayList<FileURL>(history);
	}
	
	/**
	 * Returns true if the global history contains the given FileURL
	 */
	public boolean historyContains(FileURL folderURL) {
		return history.contains(folderURL);
	}
	
	///////////////////////
	/// LocationAdapter ///
	///////////////////////
	
	public void locationChanged(LocationEvent locationEvent) {
		FileURL file = locationEvent.getFolderURL();
		
		// remove the new location from the history as it should be put 
		// at the end of the list to preserve the insertion order of the history
		boolean alreadyExists = history.remove(file);
		
		// ensure that we won't cross the maximum number of saved locations
		if (!alreadyExists && MAX_CAPACITY == history.size())
			history.remove(history.iterator().next());
		
		// add the location as last one in the history
		history.add(locationEvent.getFolderURL());
	}
}
