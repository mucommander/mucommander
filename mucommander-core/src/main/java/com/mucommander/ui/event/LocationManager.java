/*
 * This file is part of muCommander, http://www.mucommander.com
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

import java.util.Collection;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.MonitoredFile;
import com.mucommander.commons.file.archive.AbstractArchiveFile;
import com.mucommander.core.FolderChangeMonitor;
import com.mucommander.core.GlobalLocationHistory;
import com.mucommander.ui.dialog.file.ArchivePasswordDialog;
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
    private MonitoredFile currentFolder;

    /** Filters out unwanted files when listing folder contents */
	private ConfigurableFolderFilter configurableFolderFilter = new ConfigurableFolderFilter();

	private FolderChangeMonitor folderChangeMonitor;

	// TODO: replace this with a proper solution
	private boolean firstRun = true;

	private static final AbstractFile[] emptyAbstractFilesArray = new AbstractFile[0];

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
     * @param folder the {@link AbstractFile} that is going to be presented in the {@link FolderPanel}
     */
    public void setCurrentFolder(AbstractFile folder, AbstractFile fileToSelect, boolean changeLockedTab) {
        setCurrentFolder(folder, fileToSelect, changeLockedTab, true);
    }

    public void setCurrentFolder(AbstractFile folder, AbstractFile fileToSelect, boolean changeLockedTab, boolean fire) {
        LOGGER.trace("calling ls() on {}", folder);
        MonitoredFile newCurrentFile = folder.toMonitoredFile();
        newCurrentFile.startWatch();

        AbstractFile[] children = emptyAbstractFilesArray;
        do {
            try {
                children = folder.ls(configurableFolderFilter);
                firstRun = false;
            } catch (Exception e) {
                LOGGER.debug("Couldn't ls children of " + folder.getAbsolutePath() + ", error: " + e.getMessage());
                if (folder.isArchive()) {
                    ArchivePasswordDialog dialog = new ArchivePasswordDialog(folderPanel.getMainFrame());
                    String password = (String) dialog.getUserInput();
                    if (password != null) {
                        ((AbstractArchiveFile) folder).setPassword(password);
                        continue;
                    }
                }
                if (!firstRun) {
                    throw new RuntimeException(e.getMessage());
                }
            }
            break;
        } while (true);

    	folderPanel.setCurrentFolder(folder, children, fileToSelect, changeLockedTab);

    	if (currentFolder != null)
    	    currentFolder.stopWatch();
    	this.currentFolder = newCurrentFile;

    	if (fire)
    	    // Notify listeners that the location has changed
    	    fireLocationChanged(folder.getURL());

    	// After the initial folder is set, initialize the monitoring thread
    	if (folderChangeMonitor == null)
    		folderChangeMonitor = new FolderChangeMonitor(folderPanel);
    }

    /**
     * Return a {@link MonitoredFile} for the folder presented in the {@link FolderPanel}
     * 
     * @return a {@link MonitoredFile} for the {@link AbstractFile} presented in the {@link FolderPanel}
     */
    public MonitoredFile getCurrentFolder() {
    	return currentFolder;
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

    private Collection<LocationListener> listeners() {
        return locationListeners.keySet();
    }

    /**
     * Notifies all registered listeners that the current folder has changed on associated FolderPanel.
     *
     * @param folderURL url of the new current folder in the associated FolderPanel
     */
    public synchronized void fireLocationChanged(FileURL folderURL) {
        LocationEvent event = new LocationEvent(folderURL);
        listeners().forEach(listener -> listener.locationChanged(event));
    }

    /**
     * Notifies all registered listeners that the current folder is being changed on the associated FolderPanel.
     *
     * @param folderURL url of the folder that will become the new location if the folder change is successful
     */
    public synchronized void fireLocationChanging(FileURL folderURL) {
        LocationEvent event = new LocationEvent(folderURL);
        listeners().forEach(listener -> listener.locationChanging(event));
    }

    /**
     * Notifies all registered listeners that the folder change as notified by {@link #fireLocationChanging(FileURL)}
     * has been cancelled by the user.
     *
     * @param folderURL url of the folder for which a failed attempt was made to make it the current folder
     */
    public synchronized void fireLocationCancelled(FileURL folderURL) {
        LocationEvent event = new LocationEvent(folderURL);
        listeners().forEach(listener -> listener.locationCancelled(event));
    }

    /**
     * Notifies all registered listeners that the folder change as notified by {@link #fireLocationChanging(FileURL)}
     * could not be changed, as a result of the folder not existing or failing to list its contents.
     *
     * @param folderURL url of the folder for which a failed attempt was made to make it the current folder
     */
    public synchronized void fireLocationFailed(FileURL folderURL) {
        LocationEvent event = new LocationEvent(folderURL);
        listeners().forEach(listener -> listener.locationFailed(event));
    }
}
