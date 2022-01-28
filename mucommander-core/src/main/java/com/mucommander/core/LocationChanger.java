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

package com.mucommander.core;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.io.IOException;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.auth.CredentialsManager;
import com.mucommander.auth.CredentialsMapping;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.MonitoredFile;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.protocol.local.LocalFile;
import com.mucommander.commons.file.protocol.search.SearchFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.event.LocationManager;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.tabs.FileTableTab;

/**
 * 
 * @author Arik Hadas, Maxence Bernard
 */
public class LocationChanger {
	private static final Logger LOGGER = LoggerFactory.getLogger(LocationChanger.class);

    /** Last time folder has changed */
    private long lastFolderChangeTime;

	private ChangeFolderThread changeFolderThread;

	/** The lock object used to prevent simultaneous folder change operations */
	private final Object FOLDER_CHANGE_LOCK = new Object();

	private MainFrame mainFrame;
	private FolderPanel folderPanel;
	private LocationManager locationManager;
	
	public LocationChanger(MainFrame mainFrame, FolderPanel folderPanel, LocationManager locationManager) {
		this.mainFrame = mainFrame;
		this.folderPanel = folderPanel;
		this.locationManager = locationManager;
	}

	/**
	 * This method is triggered internally (i.e not by user request) to change the current
	 * folder to the given folder
	 *
	 * @param folderURL the URL of the folder to switch to
	 * @param runnable an Implementation of {@link Runnable} that would be executed after the location is changed
	 */
	public void tryChangeCurrentFolderInternal(FileTableTab tab, Runnable runnable) {
		mainFrame.setNoEventsMode(true);
		// Set cursor to hourglass/wait
		mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		Runnable locationSetter = () -> {
		    AbstractFile folder = getWorkableLocation(tab.getLocation());
		    AbstractFile selectedFile = tab.getSelectedFile();
		    try {
		        locationManager.setCurrentFolder(folder, selectedFile, true);
		        tab.setSelectedFile(null);
		    } finally {
		        mainFrame.setNoEventsMode(false);
		        // Restore default cursor
		        mainFrame.setCursor(Cursor.getDefaultCursor());
		        // Execute the given runnable
		        runnable.run();
		    }
		};

		if (EventQueue.isDispatchThread())
		    new Thread(locationSetter).start();
		else
		    locationSetter.run();
	}

	/**
	 * Return a workable location according the following logic:
	 * - If the given folder exists, return it
	 * - if the given folder is local file, find workable location
	 *   according to the logic used for inaccessible local files
	 * - Otherwise, return the non-exist remote location
	 */
	private AbstractFile getWorkableLocation(FileURL folderURL) {
		AbstractFile folder = FileFactory.getFile(folderURL);
		if (folder != null && folder.exists())
			return folder;
		
		if (folder == null)
			folder = new NullableFile(folderURL);
		
		return LocalFile.SCHEMA.equals(folderURL.getScheme()) ?
				getWorkableFolder(folder) : folder;
	}

	/**
	 * Tries to change the current folder to the new specified one and notifies the user in case of a problem.
	 *
	 * <p>This method spawns a separate thread that takes care of the actual folder change and returns it.
	 * It does nothing and returns <code>null</code> if another folder change is already underway.</p>
	 *
	 * <p>
	 * This method is <b>not</b> I/O-bound and returns immediately, without any chance of locking the calling thread.
	 * </p>
	 *
	 * @param folder the folder to be made current folder
	 * @param changeLockedTab - flag that indicates whether to change the presented folder in the currently selected tab although it's locked
	 * @return the thread that performs the actual folder change, null if another folder change is already underway
	 */
	public ChangeFolderThread tryChangeCurrentFolder(AbstractFile folder, boolean changeLockedTab) {
		/* TODO branch setBranchView(false); */
		return tryChangeCurrentFolder(folder, null, false, changeLockedTab);
	}

	/**
	 * Tries to change current folder to the new specified one, and selects the given file after the folder has been
	 * changed. The user is notified by a dialog if the folder could not be changed.
	 *
	 * <p>If the current folder could not be changed to the requested folder and <code>findWorkableFolder</code> is
	 * <code>true</code>, the current folder will be changed to the first existing parent of the request folder if there
	 * is one, to the first existing local volume otherwise. In the unlikely event that no local volume is workable,
	 * the user will be notified that the folder could not be changed.</p>
	 *
	 * <p>This method spawns a separate thread that takes care of the actual folder change and returns it.
	 * It does nothing and returns <code>null</code> if another folder change is already underway.</p>
	 *
	 * <p>
	 * This method is <b>not</b> I/O-bound and returns immediately, without any chance of locking the calling thread.
	 * </p>
	 *
	 * @param folder the folder to be made current folder
	 * @param selectThisFileAfter the file to be selected after the folder has been changed (if it exists in the folder), can be null in which case FileTable rules will be used to select current file
	 * @param changeLockedTab - flag that indicates whether to change the presented folder in the currently selected tab although it's locked
	 * @return the thread that performs the actual folder change, null if another folder change is already underway  
	 */
	public ChangeFolderThread tryChangeCurrentFolder(AbstractFile folder, AbstractFile selectThisFileAfter, boolean findWorkableFolder, boolean changeLockedTab) {
		LOGGER.debug("folder="+folder+" selectThisFileAfter="+selectThisFileAfter);

		synchronized(FOLDER_CHANGE_LOCK) {
			// Make sure a folder change is not already taking place. This can happen under rare but normal
			// circumstances, if this method is called before the folder change thread has had the time to call
			// MainFrame#setNoEventsMode.
			if(changeFolderThread!=null) {
				LOGGER.debug("A folder change is already taking place ("+changeFolderThread+"), returning null");
				return null;
			}

			// Important: the ChangeFolderThread instance must be kept in a local variable (as opposed to the
			// changeFolderThread field only) before being returned. The reason for this is that ChangeFolderThread
			// changes the changeFolderThread field to null when finished, and it may do so before this method has
			// returned (I've seen this happening). Relying solely on the changeFolderThread field could thus cause
			// a null value to be returned, which is particularly problematic during startup (would cause an NPE).
			FileURL folderURL = folder.getURL();
			ChangeFolderThread thread;
			switch(folderURL.getScheme()) {
			case SearchFile.SCHEMA:
			    if (folder instanceof SearchFile)
			        ((SearchFile) folder).stop();
			    folder = FileFactory.getFile(folderURL);
			    thread = new SearchUpdaterThread(folderURL, changeLockedTab, mainFrame, folderPanel, locationManager, this);
			    break;
			default:
			    thread = new BrowseLocationThread(folder,
			            findWorkableFolder,
			            changeLockedTab,
			            mainFrame,
			            folderPanel,
			            locationManager,
			            this);
			}

			if(selectThisFileAfter!=null)
				thread.selectThisFileAfter(selectThisFileAfter);
			thread.start();

			changeFolderThread = thread;
			return thread;
		}
	}

	/**
	 * Tries to change the current folder to the specified path and notifies the user in case of a problem.
	 *
	 * <p>This method spawns a separate thread that takes care of the actual folder change and returns it.
	 * It does nothing and returns <code>null</code> if another folder change is already underway or if the given
	 * path could not be resolved.</p>
	 *
	 * <p>
	 * This method is <b>not</b> I/O-bound and returns immediately, without any chance of locking the calling thread.
	 * </p>
	 *
	 * @param folderPath path to the new current folder. If this path does not resolve into a file, an error message will be displayed.
	 * @return the thread that performs the actual folder change, null if another folder change is already underway or if the given path could not be resolved
	 */
	public ChangeFolderThread tryChangeCurrentFolder(String folderPath) {
		try {
			return tryChangeCurrentFolder(FileURL.getFileURL(folderPath), null, false);
		}
		catch(MalformedURLException e) {
			// FileURL could not be resolved, notify the user that the folder doesn't exist
			showFolderDoesNotExistDialog();

			return null;
		}
	}

	/**
	 * Tries to change current folder to the new specified URL and notifies the user in case of a problem.
	 *
	 * <p>This method spawns a separate thread that takes care of the actual folder change and returns it.
	 * It does nothing and returns <code>null</code> if another folder change is already underway.</p>
	 *
	 * <p>
	 * This method is <b>not</b> I/O-bound and returns immediately, without any chance of locking the calling thread.
	 * </p>
	 *
	 * @param folderURL location to the new current folder. If this URL does not resolve into a file, an error message will be displayed.
	 * @return the thread that performs the actual folder change, null if another folder change is already underway
	 */
	public ChangeFolderThread tryChangeCurrentFolder(FileURL folderURL) {
		return tryChangeCurrentFolder(folderURL, null, false);
	}

	public ChangeFolderThread tryChangeCurrentFolder(FileURL folderURL, boolean changeLockedTab) {
		return tryChangeCurrentFolder(folderURL, null, changeLockedTab);
	}

	/**
	 * Tries to change current folder to the new specified path and notifies the user in case of a problem.
	 * If not <code>null</code>, the specified {@link com.mucommander.auth.CredentialsMapping} is used to authenticate
	 * the folder, and added to {@link CredentialsManager} if the folder has been successfully changed.</p>
	 *
	 * <p>This method spawns a separate thread that takes care of the actual folder change and returns it.
	 * It does nothing and returns <code>null</code> if another folder change is already underway.</p>
	 *
	 * <p>
	 * This method is <b>not</b> I/O-bound and returns immediately, without any chance of locking the calling thread.
	 * </p>
	 *
	 * @param folderURL folder's URL to be made current folder. If this URL does not resolve into an existing file, an error message will be displayed.
	 * @param credentialsMapping the CredentialsMapping to use for authentication, can be null
	 * @return the thread that performs the actual folder change, null if another folder change is already underway
	 */
	public ChangeFolderThread tryChangeCurrentFolder(FileURL folderURL, CredentialsMapping credentialsMapping, boolean changeLockedTab) {
		LOGGER.debug("folderURL="+folderURL);

		synchronized(FOLDER_CHANGE_LOCK) {
			// Make sure a folder change is not already taking place. This can happen under rare but normal
			// circumstances, if this method is called before the folder change thread has had the time to call
			// MainFrame#setNoEventsMode.
			if(changeFolderThread!=null) {
				LOGGER.debug("A folder change is already taking place ("+changeFolderThread+"), returning null");
				return null;
			}

			// Important: the ChangeFolderThread instance must be kept in a local variable (as opposed to the
			// changeFolderThread field only) before being returned. The reason for this is that ChangeFolderThread
			// changes the changeFolderThread field to null when finished, and it may do so before this method has
			// returned (I've seen this happening). Relying solely on the changeFolderThread field could thus cause
			// a null value to be returned, which is particularly problematic during startup (would cause an NPE).
			ChangeFolderThread thread;
			switch (folderURL.getScheme()) {
			case SearchFile.SCHEMA:
			    thread = new SearchUpdaterThread(folderURL, changeLockedTab, mainFrame, folderPanel, locationManager, this);
			    break;
			default:
			    thread = new BrowseLocationThread(folderURL, credentialsMapping, changeLockedTab, mainFrame, folderPanel,
			            locationManager, this);
			}
			thread.start();

			changeFolderThread = thread;
			return thread;
		}
	}

	/**
	 * Shorthand for {@link #tryRefreshCurrentFolder(AbstractFile)} called with no specific file (<code>null</code>)
	 * to select after the folder has been changed.
	 */
	public void tryRefreshCurrentFolder() {
		tryRefreshCurrentFolder(null);
	}

	/**
	 * Refreshes the current folder's contents. If the folder is no longer available, the folder will be changed to a
	 * 'workable' folder (see {@link #tryChangeCurrentFolder(AbstractFile, AbstractFile, boolean)}.
	 *
	 * <p>This method spawns a separate thread that takes care of the actual folder change and returns it.
	 * It does nothing and returns <code>null</code> if another folder change is already underway.</p>
	 *
	 * <p>This method is <b>not</b> I/O-bound and returns immediately, without any chance of locking the calling thread.</p>
	 *
	 * @param selectThisFileAfter file to be selected after the folder has been refreshed (if it exists in the folder),
	 * can be null in which case FileTable rules will be used to select current file
	 * @see #tryChangeCurrentFolder(AbstractFile, AbstractFile, boolean)
	 */
	public void tryRefreshCurrentFolder(AbstractFile selectThisFileAfter) {
	    MonitoredFile currentFolder = locationManager.getCurrentFolder();
	    folderPanel.getFoldersTreePanel().refreshFolder(currentFolder);
	    tryChangeCurrentFolder(currentFolder, selectThisFileAfter, true, true);
	}
	
	 /**
     * Changes current folder using the given folder and children files.
     *
     * <p>
     * This method <b>is</b> I/O-bound and locks the calling thread until the folder has been changed. It may under
     * certain circumstances lock indefinitely, for example when accessing network-based filesystems.
     * </p>
     *
     * @param folder folder to be made current folder
     * @param fileToSelect file to be selected after the folder has been refreshed (if it exists in the folder), can be null in which case FileTable rules will be used to select current file
     * @param changeLockedTab - flag that indicates whether to change the presented folder in the currently selected tab although it's locked
     * @param fireLocationChanged whether or not to fire even upon folder change
	 * @throws IOException 
	 * @throws UnsupportedFileOperationException 
     */
    void setCurrentFolder(AbstractFile folder, AbstractFile fileToSelect, boolean changeLockedTab, boolean fireLocationChanged) throws UnsupportedFileOperationException, IOException {
    	// Update the timestamp right before the folder is set in case FolderChangeMonitor checks the timestamp
        // while FileTable#setCurrentFolder is being called. 
        lastFolderChangeTime = System.currentTimeMillis();
        
        locationManager.setCurrentFolder(folder, fileToSelect, changeLockedTab, fireLocationChanged);
    }

    /**
     * Returns the time at which the last folder change completed successfully.
     *
     * @return the time at which the last folder change completed successfully.
     */
    public long getLastFolderChangeTime() {
        return lastFolderChangeTime;
    }

    /**
     * Try to kill the thread that is currently changing the current folder, if exists
     *
     * @return true if an attempt was made to stop the thread
     */
    public boolean tryKillChangeFolderThread() {
        ChangeFolderThread changeFolderThread = this.changeFolderThread;
        return changeFolderThread != null ? changeFolderThread.tryKill() : false;
    }


    /**
     * Returns <code>true</code> ´if the current folder is currently being changed, <code>false</code> otherwise.
     *
     * @return <code>true</code> ´if the current folder is currently being changed, <code>false</code> otherwise
     */
    public boolean isFolderChanging() {
        return changeFolderThread!=null;
    }
    
	/**
     * Displays a popup dialog informing the user that the requested folder doesn't exist or isn't available.
     */
    void showFolderDoesNotExistDialog() {
        InformationDialog.showErrorDialog(mainFrame, Translator.get("table.folder_access_error_title"), Translator.get("folder_does_not_exist"));
    }


	/**
	 * Returns a 'workable' folder as a substitute for the given non-existing folder. This method will return the
	 * first existing parent if there is one, to the first existing local volume otherwise. In the unlikely event
	 * that no local volume exists, <code>null</code> will be returned.
	 *
	 * @param folder folder for which to find a workable folder
	 * @return a 'workable' folder for the given non-existing folder, <code>null</code> if there is none.
	 */
	AbstractFile getWorkableFolder(AbstractFile folder) {
		// Look for an existing parent
		AbstractFile newFolder = folder;
		do {
			newFolder = newFolder.getParent();
			if(newFolder!=null && newFolder.exists())
				return newFolder;
		}
		while(newFolder!=null);

		// Fall back to the first existing volume
		AbstractFile[] localVolumes = LocalFile.getVolumes();
		for(AbstractFile volume : localVolumes) {
			if(volume.exists())
				return volume;
		}

		// No volume could be found, return null
		return null;
	}

	void cleanChangeFolderThread() {
	    synchronized(FOLDER_CHANGE_LOCK) {
            changeFolderThread = null;
        }
	}
}
