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

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileProtocols;
import com.mucommander.commons.file.filter.AbstractFileFilter;
import com.mucommander.commons.file.filter.FileFilter;
import com.mucommander.commons.file.filter.OrFileFilter;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.main.FolderPanel;


/**
 * This file monitors changes in the current folder of a FolderPanel, checking periodically if the current folder's
 * date has changed. If a change has been detected, the FolderPanel will be asked to refresh its current folder.
 * 
 * <p>If the MainFrame which contains the monitored FolderPanel becomes inactive (lies in the background), monitoring
 * on will be not happen until the MainFrame becomes active again.
 *
 * <p>Implementation note: the monitoring is done in one single thread for all folders, each folder being monitored
 * one after another. Current folder refreshes are performed in a separate thread.
 *
 * @author Maxence Bernard
 * @see <a href="http://trac.mucommander.com/wiki/FolderAutoRefresh">FolderAutoRefresh wiki entry</a>
 */
public class FolderChangeMonitor implements Runnable, WindowListener, LocationListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(FolderChangeMonitor.class);
	
    /** Folder panel we are monitoring */
    private FolderPanel folderPanel;

    /** Current file table's folder */
    private AbstractFile currentFolder;

    /** True when the current folder is currently being changed */
    private boolean folderChanging;

    /** Current folder's date */
    private long currentFolderDate;

    /** Folder check/refresh while be skipped while this field is set to <code>true</code> */ 
    private boolean paused;

    /** Number of milliseconds to wait before next folder check */
    private long waitBeforeCheckTime;
	
    /** Timestamp of the last folder change check */
    private long lastCheckTimestamp;

    /** Total time spent checking for folder changes in current folder */
    private long totalCheckTime = 0;
	
    /** Number of checks in current folder */
    private int nbSamples = 0;

	
    //////////////////////
    // Static variables //
    //////////////////////
	
    /** Thread in which the actual monitoring is performed */
    private static Thread monitorThread;

    /** FolderChangeMonitor instances */
    private static List<FolderChangeMonitor> instances;

    private static OrFileFilter disableAutoRefreshFilter = new OrFileFilter();
		
    /** Milliseconds period between checks to current folder's date */
    private static long checkPeriod;
	
    /** Delay in milliseconds before folder date check after a folder has been refreshed */
    private static long waitAfterRefresh;
	
    /** If folder change check took an average of N milliseconds, thread will wait at least N*WAIT_MULTIPLIER before next check */
    private final static int WAIT_MULTIPLIER = 50;

    /** Granularity of the thread check (number of milliseconds to sleep before next loop) */
    private final static int TICK = 300;

    static {
        instances = new Vector<FolderChangeMonitor>();

        // Retrieve configuration values
        checkPeriod = MuConfigurations.getPreferences().getVariable(MuPreference.REFRESH_CHECK_PERIOD,
                                                       MuPreferences.DEFAULT_REFRESH_CHECK_PERIOD);
        waitAfterRefresh = MuConfigurations.getPreferences().getVariable(MuPreference.WAIT_AFTER_REFRESH,
                                                            MuPreferences.DEFAULT_WAIT_AFTER_REFRESH);

        disableAutoRefreshFilter.addFileFilter(new AbstractFileFilter() {
            public boolean accept(AbstractFile file) {
                return file.getURL().getScheme().equals(FileProtocols.S3);
            }
        });
    }


    /**
     * Adds the given {@link FileFilter} to the list of filters that match folders for which auto-refresh is disabled.
     * One use case for disabling auto-refresh is for protocols that involve a cost ($$$) when looking for changes
     * or refreshing the folder. This is the case for Amazon S3 for which auto-refresh is disabled by default.
     *
     * @param filter matches folders for which auto-refresh will be disabled
     */
    public static void addDisableAutoRefreshFilter(FileFilter filter) {
        disableAutoRefreshFilter.addFileFilter(filter);
    }

    public FolderChangeMonitor(FolderPanel folderPanel) {

        this.folderPanel = folderPanel;

        // Listen to folder changes to know when a folder is being / has been changed
        folderPanel.getLocationManager().addLocationListener(this);

        this.currentFolder = folderPanel.getCurrentFolder();
        this.currentFolderDate = currentFolder.getDate();

        // Folder contents is up-to-date let's wait before checking it for changes
        this.lastCheckTimestamp = System.currentTimeMillis();
        this.waitBeforeCheckTime = waitAfterRefresh;
		
        folderPanel.getMainFrame().addWindowListener(this);

        instances.add(this);
		
        // Create and start the monitor thread on first FolderChangeMonitor instance
        if(monitorThread==null && checkPeriod>=0) {
            monitorThread = new Thread(this, getClass().getName());
            monitorThread.setDaemon(true);
            monitorThread.start();
        }
    }

	
    public void run() {
        // TODO: it would be more efficient to use a wait/notify scheme rather than sleeping. 
        // It would also allow folders to be checked immediately upon certain conditions such as a window becoming activated.

        int nbInstances;
        FolderChangeMonitor monitor;
        boolean folderRefreshed;
		
        while(monitorThread!=null) {
			
            // Sleep for a while
            try { Thread.sleep(TICK);}
            catch(InterruptedException e) {}
			
            // Loop on instances
            nbInstances = instances.size();
            for(int i=0; i<nbInstances; i++) {
                try { monitor = instances.get(i); }
                catch(Exception e) { continue; } // Exception may be raised when an instance is removed
				
                // Check for changes in current folder and refresh it only if :
                // - MainFrame is in the foreground
                // - monitor is not paused
                // - current folder is not being changed
                if(monitor.folderPanel.getMainFrame().isForegroundActive() && !folderChanging && !monitor.paused) {
                    // By checking FolderPanel.getLastFolderChangeTime(), we ensure that we don't check right after
                    // the folder has been refreshed.
                    if(System.currentTimeMillis()-Math.max(monitor.lastCheckTimestamp, monitor.folderPanel.getLastFolderChangeTime())>monitor.waitBeforeCheckTime) {
                        // Checks folder contents and refreshes view if necessary
                        folderRefreshed = monitor.checkAndRefresh();
                        monitor.lastCheckTimestamp = System.currentTimeMillis();

                        // If folder change check took an average of N milliseconds, we will wait at least N*WAIT_MULTIPLIER before next check
                        monitor.waitBeforeCheckTime = monitor.nbSamples==0?
                            checkPeriod
                            :Math.max(folderRefreshed?waitAfterRefresh:checkPeriod, (int)(WAIT_MULTIPLIER*(monitor.totalCheckTime/(float)monitor.nbSamples)));
                    }
                }					
            }		
        }
    }

	
    /**
     * Stops monitoring (stops monitoring thread).
     */
    public void stop() {
        monitorThread = null;
    }


    /**
     * Suspends or resumes this monitor.
     *
     * @param paused true to supsend, false to resume
     */
    public void setPaused(boolean paused) {
        // Note: this method should *not* be synchronized as it would potentially lock while the folder is being
        // checked/refreshed
        this.paused = paused;

        // Check folder for changes immediately as setPaused(false) is often called after a FileJob
        if(!paused)
            this.waitBeforeCheckTime = 0;
    }
	
	
    /**
     * Forces this monitor to update current folder information. This method should be called when a folder has been
     * manually refreshed, so that this monitor doesn't detect changes and try to refresh the table again.
     *
     * @param folder the new current folder
     */
    private void updateFolderInfo(AbstractFile folder) {
        this.currentFolder = folder;
        this.currentFolderDate = currentFolder.getDate();

        // Reset time average
        totalCheckTime = 0;
        nbSamples = 0;
    }
	
	
    /**
     * Checks if current file table's folder has changed and if it hasn't, checks if current folder's date has changed
     * and if it has, refresh the file table.
     *
     * @return <code>true</code> if the folder was refreshed.
     */
    private synchronized boolean checkAndRefresh() {
        if(paused || disableAutoRefreshFilter.match(currentFolder))
            return false;

        // Update time average next loop
        long timeStamp = System.currentTimeMillis();
		
        // Check folder's date
        long date = currentFolder.getDate();

        totalCheckTime += System.currentTimeMillis()-timeStamp;
        nbSamples++;
		
        // Has date changed ?
        // Note that date will be 0 if the folder is no longer available, and thus yield a refresh: this is exactly
        // what we want (the folder will be changed to a 'workable' folder).
        if(date!=currentFolderDate) {
            LOGGER.debug(this+" ("+currentFolder.getName()+") Detected changes in current folder, refreshing table!");
			
            // Try and refresh current folder in a separate thread as to not lock monitor thread
            folderPanel.tryRefreshCurrentFolder();
        }
		
        return false;
    }


    /////////////////////////////////////
    // LocationListener implementation //
    /////////////////////////////////////

    public void locationChanging(LocationEvent locationEvent) {
        folderChanging = true;
    }

    public void locationChanged(LocationEvent locationEvent) {
        // Update new current folder info
        updateFolderInfo(locationEvent.getFolderPanel().getCurrentFolder());

        folderChanging = false;
    }

    public void locationCancelled(LocationEvent locationEvent) {
        folderChanging = false;
    }

    public void locationFailed(LocationEvent locationEvent) {
        folderChanging = false;
    }


    ///////////////////////////////////
    // WindowListener implementation //
    ///////////////////////////////////

    public void windowActivated(WindowEvent e) {}

    public void windowDeactivated(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowOpened(WindowEvent e) {}

    public void windowClosing(WindowEvent e) {}

    public void windowClosed(WindowEvent e) {
        // Remove the MainFrame from the list of monitored instances
        instances.remove(this);
        LOGGER.debug("nbInstances="+instances.size());
    }	
	
}
