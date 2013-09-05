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

package com.mucommander.ui.event;

import java.io.IOException;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.core.FolderChangeMonitor;
import com.mucommander.core.GlobalLocationHistory;
import com.mucommander.ui.main.ConfigurableFolderFilter;
import com.mucommander.ui.main.FolderPanel;

/**
 * @author Maxence Bernard
 */
public class LocationManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(LocationManager.class);

    /** Contains all registered location listeners, stored as weak references */
    private WeakHashMap<LocationListener, ?> locationListeners = new WeakHashMap<LocationListener, Object>();

    /** The FolderPanel instance this LocationManager manages location events for */
    private FolderPanel folderPanel;

    /** Current location presented in the FolderPanel */
    private AbstractFile currentFolder;

    /** Filters out unwanted files when listing folder contents */
	private ConfigurableFolderFilter configurableFolderFilter = new ConfigurableFolderFilter();

	private FolderChangeMonitor folderChangeMonitor;

    /**
     * Creates a new LocationManager that manages location events listeners and broadcasts for the specified FolderPanel.
     *
     * @param folderPanel the FolderPanel instance this LocationManager manages location events for
     */
    public LocationManager(FolderPanel folderPanel) {
        this.folderPanel = folderPanel;
        
        addLocationListener(GlobalLocationHistory.Instance());
    }

    /**
     * Set the given {@link AbstractFile} as the folder presented in the {@link FolderPanel}.
     * This method saves the given {@link AbstractFile}, and notify the {@link LocationListener}s that
     * the location was changed to it.
     * 
     * @param currentFolder the {@link AbstractFile} that is going to be presented in the {@link FolderPanel}
     * @throws IOException 
     * @throws UnsupportedFileOperationException 
     */
    public void setCurrentFolder(AbstractFile folder, AbstractFile fileToSelect, boolean changeLockedTab) {
    	LOGGER.trace("calling ls()");
    	AbstractFile[] children;
		try {
			children = folder.ls(configurableFolderFilter);
		} catch (Exception e) {
			LOGGER.debug("Couldn't ls children of " + folder.getAbsolutePath() + ", error: " + e.getMessage());
			children = new AbstractFile[0];
		}

    	folderPanel.setCurrentFolder(folder, children, fileToSelect, changeLockedTab);

    	this.currentFolder = folder;

    	// Notify listeners that the location has changed
    	fireLocationChanged(folder.getURL());

    	// After the initial folder is set, initialize the monitoring thread
    	if (folderChangeMonitor == null)
    		folderChangeMonitor = new FolderChangeMonitor(folderPanel);
    }

    /**
     * Return the folder presented in the {@link FolderPanel}
     * 
     * @return the {@link AbstractFile} presented in the {@link FolderPanel}
     */
    public AbstractFile getCurrentFolder() {
    	return currentFolder;
    }

    public FolderChangeMonitor getFolderChangeMonitor() {
        return folderChangeMonitor;
    }

    /**
     * Registers a LocationListener to receive notifications whenever the current folder of the associated FolderPanel
     * has or is being changed.
     *
     * <p>Listeners are stored as weak references so {@link #removeLocationListener(LocationListener)}
     * doesn't need to be called for listeners to be garbage collected when they're not used anymore.</p>
     *
     * @param listener the LocationListener to register
     */
    public synchronized void addLocationListener(LocationListener listener) {
        locationListeners.put(listener, null);
    }

    /**
     * Removes the LocationListener from the list of listeners that receive notifications when the current folder of the
     * associated FolderPanel has or is being changed.
     *
     * @param listener the LocationListener to remove
     */
    public synchronized void removeLocationListener(LocationListener listener) {
        locationListeners.remove(listener);
    }

    /**
     * Notifies all registered listeners that the current folder has changed on associated FolderPanel.
     *
     * @param folderURL url of the new current folder in the associated FolderPanel
     */
    private synchronized void fireLocationChanged(FileURL folderURL) {
        for(LocationListener listener : locationListeners.keySet())
            listener.locationChanged(new LocationEvent(folderPanel, folderURL));
    }

    /**
     * Notifies all registered listeners that the current folder is being changed on the associated FolderPanel.
     *
     * @param folderURL url of the folder that will become the new location if the folder change is successful
     */
    public synchronized void fireLocationChanging(FileURL folderURL) {
        for(LocationListener listener : locationListeners.keySet())
            listener.locationChanging(new LocationEvent(folderPanel, folderURL));
    }

    /**
     * Notifies all registered listeners that the folder change as notified by {@link #fireLocationChanging(FileURL)}
     * has been cancelled by the user.
     *
     * @param folderURL url of the folder for which a failed attempt was made to make it the current folder
     */
    public synchronized void fireLocationCancelled(FileURL folderURL) {
        for(LocationListener listener : locationListeners.keySet())
            listener.locationCancelled(new LocationEvent(folderPanel, folderURL));
    }

    /**
     * Notifies all registered listeners that the folder change as notified by {@link #fireLocationChanging(FileURL)}
     * could not be changed, as a result of the folder not existing or failing to list its contents.
     *
     * @param folderURL url of the folder for which a failed attempt was made to make it the current folder
     */
    public synchronized void fireLocationFailed(FileURL folderURL) {
        for(LocationListener listener : locationListeners.keySet())
            listener.locationFailed(new LocationEvent(folderPanel, folderURL));
    }
}
