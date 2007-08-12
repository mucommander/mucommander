/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.ui.main.table;

import com.mucommander.Debug;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.conf.impl.ConfigurationVariables;
import com.mucommander.file.AbstractFile;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.main.FolderPanel;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;


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
 */
public class FolderChangeMonitor implements Runnable, WindowListener, LocationListener {

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
    private static Vector instances;
		
    /** Milliseconds period between checks to current folder's date */
    private static long checkPeriod;
	
    /** Delay in milliseconds before folder date check after a folder has been refreshed */
    private static long waitAfterRefresh;
	
    /** If folder change check took an average of N milliseconds, thread will wait at least N*WAIT_MULTIPLIER before next check */
    private final static int WAIT_MULTIPLIER = 50;

    /** Granularity of the thread check (number of milliseconds to sleep before next loop) */
    private final static int TICK = 300;
	
	
    static {
        instances = new Vector();

        // Retrieve configuration values
        checkPeriod = ConfigurationManager.getVariable(ConfigurationVariables.REFRESH_CHECK_PERIOD,
                                                       ConfigurationVariables.DEFAULT_REFRESH_CHECK_PERIOD);
        waitAfterRefresh = ConfigurationManager.getVariable(ConfigurationVariables.WAIT_AFTER_REFRESH,
                                                            ConfigurationVariables.DEFAULT_WAIT_AFTER_REFRESH);
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
        if(monitorThread==null) {
            monitorThread = new Thread(this, getClass().getName());
            monitorThread.setDaemon(true);
            monitorThread.start();
        }
    }

	
    public void run() {

        int nbInstances;
        FolderChangeMonitor monitor;
        boolean folderRefreshed;
		
        while(monitorThread!=null) {
			
            // Sleep for a while
            try { monitorThread.sleep(TICK);}
            catch(InterruptedException e) {}
			
            // Loop on instances
            nbInstances = instances.size();
            for(int i=0; i<nbInstances; i++) {
                try { monitor = (FolderChangeMonitor)instances.elementAt(i); }
                catch(Exception e) { continue; } // Exception may be raised when an instance is removed
				
                // Check for changes in current folder and refresh it only if :
                // - MainFrame is in the foreground
                // - monitor is not paused
                // - current folder is not being changed
                if(monitor.folderPanel.getMainFrame().isForegroundActive() && !folderChanging && !monitor.paused) {
                    if(System.currentTimeMillis()-monitor.lastCheckTimestamp>monitor.waitBeforeCheckTime) {
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
        this.monitorThread = null;
    }


    /**
     * Temporarily pauses/resumes folder checks/refresh.
     */
    public synchronized void setPaused(boolean paused) {
//        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(this+" paused="+paused);
	
        this.paused = paused;

        // Check folder for changes immediately as
        // setPaused(true) is often called after a FileJob
        if(paused)
            this.waitBeforeCheckTime = 0;
    }
	
	
    /**
     * Forces this monitor to update current folder information. This method
     * should be called when a folder has been manually refreshed, so that this monitor doesn't detect changes and try to refresh the table again.
     */
    private void updateFolderInfo(AbstractFile folder) {
        this.currentFolder = folder;
        this.currentFolderDate = currentFolder.getDate();
    }
	
	
    /**
     * Checks if current folder in file table hasn't changed and if not,
     * checks if current folder's date has changed and if it has, asks
     * file table to refresh.
     *
     * @return <code>true</code> if the folder was refreshed.
     */
    private synchronized boolean checkAndRefresh() {
        if(paused)
            return false;
		
        AbstractFile folder;
        long date;
        long timeStamp;

        // if(Debug.ON) Debug.trace("("+currentFolder.getName()+" "+instances.indexOf(this)+"/"+instances.size()+") checking if current folder changed");
		
        // Has current folder changed ?
        folder = folderPanel.getCurrentFolder();
        if(!folder.equals(currentFolder)) {
            currentFolder = folder;
            currentFolderDate = currentFolder.getDate();
            // Reset time average
            totalCheckTime = 0;
            nbSamples = 0;
            // No need to go further
            return false;
        }

        // Update time average next loop
        timeStamp = System.currentTimeMillis();
		
        // Check folder's date
        date = currentFolder.getDate();

        totalCheckTime += System.currentTimeMillis()-timeStamp;
        nbSamples++;
		
        // if(Debug.ON) com.mucommander.Debug.trace("("+currentFolder.getName()+") checking current folder date "+date+" / "+currentFolderDate);

        // Has date changed ?
        if(date!=currentFolderDate) {
            // Checks if current folder still exists, could have become unavailable (returned date would be 0 i.e. different)
            if(!currentFolder.exists())
                return true;	// Folder could not be refreshed but still return true so we don't keep on trying
			
            if(Debug.ON) Debug.trace(this+" ("+currentFolder.getName()+") Detected changes in current folder, refreshing table!");
			
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

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
        // Remove the MainFrame from the list of monitored instances
        instances.remove(this);
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("nbInstances="+instances.size());
    }	
	
}
