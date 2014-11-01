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

import java.awt.Cursor;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.auth.CredentialsManager;
import com.mucommander.auth.CredentialsMapping;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.AuthenticationType;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.FileProtocols;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.PermissionBits;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.impl.CachedFile;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.dialog.auth.AuthDialog;
import com.mucommander.ui.dialog.file.DownloadDialog;
import com.mucommander.ui.event.LocationManager;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.utils.Callback;

/**
 * 
 * @author Arik Hadas, Maxence Bernard
 */
public class LocationChanger {
	private static final Logger LOGGER = LoggerFactory.getLogger(LocationChanger.class);

    /** Last time folder has changed */
    private long lastFolderChangeTime;

	private ChangeFolderThread changeFolderThread;

	private GlobalLocationHistory globalHistory = GlobalLocationHistory.Instance();

	/** The lock object used to prevent simultaneous folder change operations */
	private final Object FOLDER_CHANGE_LOCK = new Object();

	private final static int CANCEL_ACTION = 0;
	private final static int BROWSE_ACTION = 1;
	private final static int DOWNLOAD_ACTION = 2;

	private final static String CANCEL_TEXT = Translator.get("cancel");
	private final static String BROWSE_TEXT = Translator.get("browse");
	private final static String DOWNLOAD_TEXT = Translator.get("download");
	
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
	 * @param callback the {@link Callback#call()} method will be called when folder has changed
	 */
	public void tryChangeCurrentFolderInternal(final FileURL folderURL, final Callback callback) {
		mainFrame.setNoEventsMode(true);
		// Set cursor to hourglass/wait
		mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
    	Thread setLocationThread = new Thread() {
    		@Override
    		public void run() {
    			AbstractFile folder = getWorkableLocation(folderURL);
    			try {
    				locationManager.setCurrentFolder(folder, null, true);
    			} finally {
    				mainFrame.setNoEventsMode(false);
    				// Restore default cursor
					mainFrame.setCursor(Cursor.getDefaultCursor());
					// Notify callback that the folder has been set 
    				callback.call();
    	    	}
    		}
    	};

    	if (EventQueue.isDispatchThread())
    		setLocationThread.start();
    	else
    		setLocationThread.run();
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
		
		return FileProtocols.FILE.equals(folderURL.getScheme()) ?
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
			ChangeFolderThread thread = new ChangeFolderThread(folder, findWorkableFolder, changeLockedTab);

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
			ChangeFolderThread thread = new ChangeFolderThread(folderURL, credentialsMapping, changeLockedTab);
			thread.start();

			changeFolderThread = thread;
			return thread;
		}
	}

	/**
	 * Shorthand for {@link #tryRefreshCurrentFolder(AbstractFile)} called with no specific file (<code>null</code>)
	 * to select after the folder has been changed.
	 *
	 * @return the thread that performs the actual folder change, null if another folder change is already underway
	 */
	public ChangeFolderThread tryRefreshCurrentFolder() {
		return tryRefreshCurrentFolder(null);
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
	 * @return the thread that performs the actual folder change, null if another folder change is already underway
	 * @see #tryChangeCurrentFolder(AbstractFile, AbstractFile, boolean)
	 */
	public ChangeFolderThread tryRefreshCurrentFolder(AbstractFile selectThisFileAfter) {
		folderPanel.getFoldersTreePanel().refreshFolder(locationManager.getCurrentFolder());
		return tryChangeCurrentFolder(locationManager.getCurrentFolder(), selectThisFileAfter, true, true);
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
     * @param children current folder's files (value of folder.ls())
     * @param fileToSelect file to be selected after the folder has been refreshed (if it exists in the folder), can be null in which case FileTable rules will be used to select current file
     * @param changeLockedTab - flag that indicates whether to change the presented folder in the currently selected tab although it's locked
	 * @throws IOException 
	 * @throws UnsupportedFileOperationException 
     */
    private void setCurrentFolder(AbstractFile folder, AbstractFile fileToSelect, boolean changeLockedTab) throws UnsupportedFileOperationException, IOException {
    	// Update the timestamp right before the folder is set in case FolderChangeMonitor checks the timestamp
        // while FileTable#setCurrentFolder is being called. 
        lastFolderChangeTime = System.currentTimeMillis();
        
    	locationManager.setCurrentFolder(folder, fileToSelect, changeLockedTab);
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
     * Returns the thread that is currently changing the current folder, <code>null</code> is the folder is not being
     * changed.
     *
     * @return the thread that is currently changing the current folder, <code>null</code> is the folder is not being
     * changed
     */
    public ChangeFolderThread getChangeFolderThread() {
        return changeFolderThread;
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
    private void showFolderDoesNotExistDialog() {
        InformationDialog.showErrorDialog(mainFrame, Translator.get("table.folder_access_error_title"), Translator.get("folder_does_not_exist"));
    }


    /**
     * Displays a popup dialog informing the user that the requested folder couldn't be opened.
     *
     * @param e the Exception that was caught while changing the folder
     */
    private void showAccessErrorDialog(Exception e) {
        InformationDialog.showErrorDialog(mainFrame, Translator.get("table.folder_access_error_title"), Translator.get("table.folder_access_error"), e==null?null:e.getMessage(), e);
    }


    /**
     * Pops up an {@link AuthDialog authentication dialog} prompting the user to select or enter credentials in order to
     * be granted the access to the file or folder represented by the given {@link FileURL}.
     * The <code>AuthDialog</code> instance is returned, allowing to retrieve the credentials that were selected
     * by the user (if any).
     *
     * @param fileURL the file or folder to ask credentials for
     * @param errorMessage optional (can be null), an error message describing a prior authentication failure
     * @return the AuthDialog that contains the credentials selected by the user (if any)
     */
    private AuthDialog popAuthDialog(FileURL fileURL, boolean authFailed, String errorMessage) {
        AuthDialog authDialog = new AuthDialog(mainFrame, fileURL, authFailed, errorMessage);
        authDialog.showDialog();
        return authDialog;
    }


    /**
     * Displays a download dialog box where the user can choose where to download the given file or cancel
     * the operation.
     *
     * @param file the file to download
     */
    private void showDownloadDialog(AbstractFile file) {
        FileSet fileSet = new FileSet(locationManager.getCurrentFolder());
        fileSet.add(file);
		
        // Show confirmation/path modification dialog
        new DownloadDialog(mainFrame, fileSet).showDialog();
    }

	/**
	 * Returns a 'workable' folder as a substitute for the given non-existing folder. This method will return the
	 * first existing parent if there is one, to the first existing local volume otherwise. In the unlikely event
	 * that no local volume exists, <code>null</code> will be returned.
	 *
	 * @param folder folder for which to find a workable folder
	 * @return a 'workable' folder for the given non-existing folder, <code>null</code> if there is none.
	 */
	private AbstractFile getWorkableFolder(AbstractFile folder) {
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

	////////////////////////////////////
	// ChangeFolderThread inner class //
	////////////////////////////////////

	/**
	 * This thread takes care of changing current folder without locking the main
	 * thread. The folder change can be cancelled.
	 *
	 * <p>A little note out of nowhere: never ever call JComponent.paintImmediately() from a thread
	 * other than the Event Dispatcher Thread, as will create nasty repaint glitches that
	 * then become very hard to track. Sun's Javadoc doesn't make it clear enough... just don't!</p>
	 *
	 * @author Maxence Bernard
	 */
	public class ChangeFolderThread extends Thread {

		private AbstractFile folder;
		private boolean findWorkableFolder;
		private boolean changeLockedTab;
		private FileURL folderURL;
		private AbstractFile fileToSelect;
		private CredentialsMapping credentialsMapping;

		/** True if this thread has been interrupted by the user using #tryKill */
		private boolean killed;
		/** True if an attempt to kill this thread using Thread#interrupt() has already been made */
		private boolean killedByInterrupt;
		/** True if an attempt to kill this thread using Thread#stop() has already been made */
		private boolean killedByStop;
		/** True if it is unsafe to kill this thread */
		private boolean doNotKill;

		private boolean disposed;

		/** Lock object used to ensure consistency and thread safeness when killing the thread */
		private final Object KILL_LOCK = new Object();

		/* TODO branch private ArrayList childrenList; */


		public ChangeFolderThread(AbstractFile folder, boolean findWorkableFolder, boolean changeLockedTab) {
			// Ensure that we work on a raw file instance and not a cached one
			this.folder = (folder instanceof CachedFile)?((CachedFile)folder).getProxiedFile():folder;
			this.folderURL = folder.getURL();
			this.findWorkableFolder = findWorkableFolder;
			this.changeLockedTab = changeLockedTab;

			setPriority(Thread.MAX_PRIORITY);
		}

		/**
		 * 
		 * @param folderURL
		 * @param credentialsMapping the CredentialsMapping to use for accessing the folder, <code>null</code> for none
		 * @param changeLockedTab
		 */
		public ChangeFolderThread(FileURL folderURL, CredentialsMapping credentialsMapping, boolean changeLockedTab) {
			this.folderURL = folderURL;
			this.changeLockedTab = changeLockedTab;
			this.credentialsMapping = credentialsMapping;

			setPriority(Thread.MAX_PRIORITY);
		}

		/**
		 * Sets the file to be selected after the folder has been changed, <code>null</code> for none.
		 *
		 * @param fileToSelect the file to be selected after the folder has been changed
		 */
		public void selectThisFileAfter(AbstractFile fileToSelect) {
			this.fileToSelect = fileToSelect;
		}

		/**
		 * Returns <code>true</code> if the given file should have its canonical path followed. In that case, the
		 * AbstractFile instance must be resolved again.
		 *
		 * <p>HTTP files MUST have their canonical path followed. For all other file protocols, this is an option in
		 * the preferences.</p>
		 *
		 * @param file the file to test
		 * @return <code>true</code> if the given file should have its canonical path followed
		 */
		private boolean followCanonicalPath(AbstractFile file) {
			return (MuConfigurations.getPreferences().getVariable(MuPreference.CD_FOLLOWS_SYMLINKS, MuPreferences.DEFAULT_CD_FOLLOWS_SYMLINKS)
					|| file.getURL().getScheme().equals(FileProtocols.HTTP))
					&& !file.getAbsolutePath(false).equals(file.getCanonicalPath(false));
		}

		/**
		 * Attempts to stop this thread and returns <code>true</code> if an attempt was made.
		 * An attempt to stop this thread will be made using one of the methods detailed hereunder, only if
		 * it is still safe to do so: if the thread is too far into the process of changing the current folder,
		 * this method will have no effect and return <code>false</code>.
		 *
		 * <p>The first time this method is called, {@link #interrupt()} is called, giving the thread a chance to stop
		 * gracefully should it be waiting for a thread or blocked in an interruptible operation such as an
		 * InterruptibleChannel. This may have no immediate effect if the thread is blocked in a non-interruptible
		 * operation. This thread will however be marked as 'killed' which will sooner or later cause {@link #run()}
		 * to stop the thread by simply returning.</p> 
		 *
		 * <p>The second time this method is called, the deprecated (and unsafe) {@link #stop()} method is called,
		 * forcing the thread to abort.</p>
		 *
		 * <p>Any subsequent calls to this method will have no effect and return <code>false</code>.</p>
		 *
		 * @return true if an attempt was made to stop this thread.
		 */
		public boolean tryKill() {
			synchronized(KILL_LOCK) {
				if(killedByStop) {
					LOGGER.debug("Thread already killed by #interrupt() and #stop(), there's nothing we can do, returning");
					return false;
				}

				if(doNotKill) {
					LOGGER.debug("Can't kill thread now, it's too late, returning");
					return false;
				}

				// This field needs to be set before actually killing the thread, #run() relies on it
				killed = true;

				// Call Thread#interrupt() the first time this method is called to give the thread a chance to stop
				// gracefully if it is waiting in Thread#sleep() or Thread#wait() or Thread#join() or in an
				// interruptible operation such as java.nio.channel.InterruptibleChannel. If this is the case,
				// InterruptedException or ClosedByInterruptException will be thrown and thus need to be catched by
				// #run().
				if(!killedByInterrupt) {
					LOGGER.debug("Killing thread using #interrupt()");

					// This field needs to be set before actually interrupting the thread, #run() relies on it
					killedByInterrupt = true;
					interrupt();
				}
				// Call Thread#stop() the first time this method is called
				else {
					LOGGER.debug("Killing thread using #stop()");

					killedByStop = true;
					super.stop();
					// Execute #cleanup() as it would have been done by #run() had the thread not been stopped.
					// Note that #run() may end pseudo-gracefully and catch the underlying Exception. In this case
					// it will also call #cleanup() but the (2nd) call to #cleanup() will be ignored.
					cleanup(false);
				}

				return true;
			}
		}


		@Override
		public void start() {
			// Notify listeners that location is changing
			locationManager.fireLocationChanging(folder==null?folderURL:folder.getURL());

			super.start();
		}


		@Override
		public void run() {
			LOGGER.debug("starting folder change...");
			boolean folderChangedSuccessfully = false;

			// Show some progress in the progress bar to give hope
			folderPanel.setProgressValue(10);

			boolean userCancelled = false;
			CredentialsMapping newCredentialsMapping = null;
			// True if Guest authentication was selected in the authentication dialog (guest credentials must not be
			// added to CredentialsManager)
			boolean guestCredentialsSelected = false;

			AuthenticationType authenticationType = folderURL.getAuthenticationType();
			if(credentialsMapping!=null) {
				newCredentialsMapping = credentialsMapping;
				CredentialsManager.authenticate(folderURL, newCredentialsMapping);
			}
			// If the URL doesn't contain any credentials and authentication for this file protocol is required, or
			// optional and CredentialsManager has credentials for this location, popup the authentication dialog to
			// avoid waiting for an AuthException to be thrown.
			else if(!folderURL.containsCredentials() &&
					(  (authenticationType==AuthenticationType.AUTHENTICATION_REQUIRED)
							|| (authenticationType==AuthenticationType.AUTHENTICATION_OPTIONAL && CredentialsManager.getMatchingCredentials(folderURL).length>0))) {
				AuthDialog authDialog = popAuthDialog(folderURL, false, null);
				newCredentialsMapping = authDialog.getCredentialsMapping();
				guestCredentialsSelected = authDialog.guestCredentialsSelected();

				// User cancelled the authentication dialog, stop
				if(newCredentialsMapping ==null)
					userCancelled = true;
				// Use the provided credentials and invalidate the folder AbstractFile instance (if any) so that
				// it gets recreated with the new credentials
				else {
					CredentialsManager.authenticate(folderURL, newCredentialsMapping);
					folder = null;
				}
			}

			if(!userCancelled) {
				boolean canonicalPathFollowed = false;

				do {
					// Set cursor to hourglass/wait
					mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

					// Render all actions inactive while changing folder
					mainFrame.setNoEventsMode(true);

					try {
						// 2 cases here :
						// - Thread was created using an AbstractFile instance
						// - Thread was created using a FileURL, corresponding AbstractFile needs to be resolved

						// Thread was created using a FileURL
						if(folder==null) {
							AbstractFile file = FileFactory.getFile(folderURL, true);

							synchronized(KILL_LOCK) {
								if(killed) {
									LOGGER.debug("this thread has been killed, returning");
									break;
								}
							}

							// File resolved -> 25% complete
							folderPanel.setProgressValue(25);

							// Popup an error dialog and abort folder change if the file could not be resolved
							// or doesn't exist
							if(file==null || !file.exists()) {
								// Restore default cursor
								mainFrame.setCursor(Cursor.getDefaultCursor());

								showFolderDoesNotExistDialog();
								break;
							}

							// File is a regular directory, all good
							if(file.isDirectory()) {
								// Just continue
							}
							// File is a browsable file (Zip archive for instance) but not a directory : Browse or Download ? => ask the user
							else if(file.isBrowsable()) {
								// If history already contains this file, do not ask the question again and assume
								// the user wants to 'browse' the file. In particular, this prevent the 'Download or browse'
								// dialog from popping up when going back or forward in history.
								// The dialog is also not displayed if the file corresponds to the currently selected file,
								// which is a weak (and not so accurate) way to know if the folder change is the result
								// of the OpenAction (enter pressed on the file). This works well enough in practice.
								if(!globalHistory.historyContains(folderURL) && !file.equals(folderPanel.getFileTable().getSelectedFile())) {
									// Restore default cursor
									mainFrame.setCursor(Cursor.getDefaultCursor());

									// Download or browse file ?
									QuestionDialog dialog = new QuestionDialog(mainFrame,
											null,
											Translator.get("table.download_or_browse"),
											mainFrame,
											new String[] {BROWSE_TEXT, DOWNLOAD_TEXT, CANCEL_TEXT},
											new int[] {BROWSE_ACTION, DOWNLOAD_ACTION, CANCEL_ACTION},
											0);

									int ret = dialog.getActionValue();

									if(ret==-1 || ret==CANCEL_ACTION)
										break;

									// Download file
									if(ret==DOWNLOAD_ACTION) {
										showDownloadDialog(file);
										break;
									}
									// Continue if BROWSE_ACTION
									// Set cursor to hourglass/wait
									mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
								}
								// else just continue and browse file's contents
							}
							// File is a regular file: show download dialog which allows to download (copy) the file
							// to a directory specified by the user
							else {
								showDownloadDialog(file);
								break;
							}

							this.folder = file;
						}
						// Thread was created using an AbstractFile instance, check file existence
						else if(!folder.exists()) {
							// Find a 'workable' folder if the requested folder doesn't exist anymore
							if(findWorkableFolder) {
								AbstractFile newFolder = getWorkableFolder(folder);
								if(newFolder.equals(folder)) {
									// If we've already tried the returned folder, give up (avoids a potentially endless loop)
									showFolderDoesNotExistDialog();
									break;
								}

								// Try again with the new folder
								folder = newFolder;
								folderURL = folder.getURL();
								// Discard the file to select, if any
								fileToSelect = null;

								continue;
							}
							else {
								showFolderDoesNotExistDialog();
								break;
							}
						}

						// Checks if canonical should be followed. If that is the case, the file is invalidated
						// and resolved again. This happens only once at most, to avoid a potential infinite loop
						// in the event that the absolute path still didn't match canonical one after the file is
						// resolved again.
						if(!canonicalPathFollowed && followCanonicalPath(folder)) {
							try {
								// Recreate the FileURL using the file's canonical path
								FileURL newURL = FileURL.getFileURL(folder.getCanonicalPath());
								// Keep the credentials and properties (if any)
								newURL.setCredentials(folderURL.getCredentials());
								newURL.importProperties(folderURL);
								this.folderURL = newURL;
								// Invalidate the AbstractFile instance
								this.folder = null;
								// There won't be any further attempts after this one
								canonicalPathFollowed = true;

								// Loop the resolve the file
								continue;
							}
							catch(MalformedURLException e) {
								// In the unlikely event of the canonical path being malformed, the AbstractFile
								// and FileURL instances are left untouched
							}
						}

						synchronized(KILL_LOCK) {
							if(killed) {
								LOGGER.debug("this thread has been killed, returning");
								break;
							}
						}

						// File tested -> 50% complete
						folderPanel.setProgressValue(50);

						/* TODO branch 
						AbstractFile children[] = new AbstractFile[0];
						if (branchView) {
							childrenList = new ArrayList();
							readBranch(folder);
							children = (AbstractFile[]) childrenList.toArray(children);
						} else {
							children = folder.ls(chainedFileFilter);                            
						}*/ 

						synchronized(KILL_LOCK) {
							if(killed) {
								LOGGER.debug("this thread has been killed, returning");
								break;
							}
							// From now on, thread cannot be killed (would comprise table integrity)
							doNotKill = true;
						}

						// files listed -> 75% complete
						folderPanel.setProgressValue(75);

						LOGGER.trace("calling setCurrentFolder");

						// Change the file table's current folder and select the specified file (if any)
						setCurrentFolder(folder, fileToSelect, changeLockedTab);

						// folder set -> 95% complete
						folderPanel.setProgressValue(95);

						// If new credentials were entered by the user, these can now be considered valid
						// (folder was changed successfully), so we add them to the CredentialsManager.
						// Do not add the credentials if guest credentials were selected by the user.
						if(newCredentialsMapping!=null && !guestCredentialsSelected)
							CredentialsManager.addCredentials(newCredentialsMapping);

						// All good !
						folderChangedSuccessfully = true;

						break;
					}
					catch(Exception e) {
						LOGGER.debug("Caught exception", e);

						if(killed) {
							// If #tryKill() called #interrupt(), the exception we just caught was most likely
							// thrown as a result of the thread being interrupted.
							//
							// The exception can be a java.lang.InterruptedException (Thread throws those),
							// a java.nio.channels.ClosedByInterruptException (InterruptibleChannel throws those)
							// or any other exception thrown by some code that swallowed the original exception
							// and threw a new one.

							LOGGER.debug("Thread was interrupted, ignoring exception");
							break;
						}

						// Restore default cursor
						mainFrame.setCursor(Cursor.getDefaultCursor());

						if(e instanceof AuthException) {
							AuthException authException = (AuthException)e;
							// Retry (loop) if user provided new credentials, if not stop
							AuthDialog authDialog = popAuthDialog(authException.getURL(), true, authException.getMessage());
							newCredentialsMapping = authDialog.getCredentialsMapping();
							guestCredentialsSelected = authDialog.guestCredentialsSelected();

							if(newCredentialsMapping!=null) {
								// Invalidate the existing AbstractFile instance
								folder = null;
								// Use the provided credentials
								CredentialsManager.authenticate(folderURL, newCredentialsMapping);
								continue;
							}
						}
						else {
							// Find a 'workable' folder if the requested folder doesn't exist anymore
							if(findWorkableFolder) {
								AbstractFile newFolder = getWorkableFolder(folder);
								if(newFolder.equals(folder)) {
									// If we've already tried the returned folder, give up (avoids a potentially endless loop)
									showFolderDoesNotExistDialog();
									break;
								}

								// Try again with the new folder
								folder = newFolder;
								folderURL = folder.getURL();
								// Discard the file to select, if any
								fileToSelect = null;

								continue;
							}

							showAccessErrorDialog(e);
						}

						// Stop looping!
						break;
					}
				}
				while(true);
			}

			synchronized(KILL_LOCK) {
				// Clean things up
				cleanup(folderChangedSuccessfully);
			}
		}

		public void cleanup(boolean folderChangedSuccessfully) {
			// Ensures that this method is called only once
			synchronized(KILL_LOCK) {
				if(disposed) {
					LOGGER.debug("already called, returning");
					return;
				}

				disposed = true;
			}

			LOGGER.trace("cleaning up, folderChangedSuccessfully="+folderChangedSuccessfully);

			// Clear the interrupted flag in case this thread has been killed using #interrupt().
			// Not doing this could cause some of the code called by this method to be interrupted (because this thread
			// is interrupted) and throw an exception
			interrupted();

			// Reset location field's progress bar
			folderPanel.setProgressValue(0);

			// Restore normal mouse cursor
			mainFrame.setCursor(Cursor.getDefaultCursor());

			synchronized(FOLDER_CHANGE_LOCK) {
				changeFolderThread = null;
			}

			// Make all actions active again
			mainFrame.setNoEventsMode(false);

			if(!folderChangedSuccessfully) {
				FileURL failedURL = folder==null?folderURL:folder.getURL();
				// Notifies listeners that location change has been cancelled by the user or has failed
				if(killed)
					locationManager.fireLocationCancelled(failedURL);
				else
					locationManager.fireLocationFailed(failedURL);
			}
		}

		// For debugging purposes
		public String toString() {
			return super.toString()+" folderURL="+folderURL+" folder="+folder;
		}
	}
	
	/* TODO branch         
	*//**
	 * Reads all files in the current directory and all its subdirectories.
	 * @param parent
	 *//*
	private void readBranch(AbstractFile parent) {
		AbstractFile[] children;
		try {
			children = parent.ls(chainedFileFilter);
			for (int i=0; i<children.length; i++) {
				if (children[i].isDirectory()) {
					readBranch(children[i]);
				} else {
					childrenList.add(children[i]);
				}
			}
		} catch (IOException e) {
			AppLogger.fine("Caught exception", e);
		}
	}*/
}
