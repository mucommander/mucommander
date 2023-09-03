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

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.MonitoredFile;
import com.mucommander.commons.file.filter.AbstractFileFilter;
import com.mucommander.commons.file.filter.FileFilter;
import com.mucommander.commons.file.filter.OrFileFilter;
import com.mucommander.commons.file.protocol.FileProtocols;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.job.JobsManager;
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
public class FolderChangeMonitor implements Runnable, WindowListener, LocationListener, WindowFocusListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(FolderChangeMonitor.class);

    private static final Object INIT_LOCK_OBJ = new Object();

    /** Folder panel we are monitoring */
    private FolderPanel folderPanel;

    /** True when the current folder is currently being changed */
    private boolean folderChanging;

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
    private static volatile Thread monitorThread;

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

    /** This forces refreshing the displayed locations immediately */
    private static boolean forceRefresh;

    static {
        instances = Collections.synchronizedList(new ArrayList<>());

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

        // Folder contents is up-to-date let's wait before checking it for changes
        this.lastCheckTimestamp = System.currentTimeMillis();
        this.waitBeforeCheckTime = waitAfterRefresh;

        // Listen to window changes to know when a folder panel is disposed
        folderPanel.getMainFrame().getJFrame().addWindowListener(this);

        // Listen to window focus changes to know when a MainFrame gains focus and make sure
        // that only one instance of FolderChangeMonitor is registered per MainFrame (no need for both panels)
        WindowFocusListener[] listeners = folderPanel.getMainFrame().getJFrame().getWindowFocusListeners();
        if (!Arrays.stream(listeners).anyMatch(l -> l instanceof FolderChangeMonitor))
            folderPanel.getMainFrame().getJFrame().addWindowFocusListener(this);

        instances.add(this);
        initMonitoringThread();
    }

    private void initMonitoringThread() {
        // Create and start the monitor thread on first FolderChangeMonitor instance
        if (monitorThread == null && checkPeriod >= 0) {
            synchronized(INIT_LOCK_OBJ) {
                if (monitorThread == null && checkPeriod >= 0) {
                    monitorThread = new Thread(this, getClass().getName());
                    monitorThread.setDaemon(true);
                    monitorThread.start();
                }
            }
        }
    }
	
    public void run() {
        // TODO: it would be more efficient to use a wait/notify scheme rather than sleeping. 
        // It would also allow folders to be checked immediately upon certain conditions such as a window becoming activated.
        while (monitorThread!=null) {
            // Sleep for a while
            try { Thread.sleep(TICK);}
            catch(InterruptedException e) {}
			
            // Loop on instances
            int nbInstances = instances.size();
            boolean forceRefresh;
            synchronized(monitorThread) {
                forceRefresh = FolderChangeMonitor.forceRefresh;
                FolderChangeMonitor.forceRefresh = false;
            }

            for (int i=0; i<nbInstances; i++) {
                FolderChangeMonitor monitor;
                try { monitor = instances.get(i); }
                catch(Exception e) { continue; } // Exception may be raised when an instance is removed

                // Check for changes in current folder and refresh it only if :
                // - MainFrame is in the foreground
                // - current folder is not being changed
                if (monitor.folderPanel.getMainFrame().isForegroundActive() && !monitor.folderChanging) {
                    if (disableAutoRefreshFilter.match(monitor.folderPanel.getCurrentFolder())) {
                        monitor.lastCheckTimestamp = System.currentTimeMillis();
                        monitor.waitBeforeCheckTime = checkPeriod;
                        continue;
                    }
                    // By checking FolderPanel.getLastFolderChangeTime(), we ensure that we don't check right after
                    // the folder has been refreshed.
                    if (forceRefresh || System.currentTimeMillis()-Math.max(monitor.lastCheckTimestamp, monitor.folderPanel.getLastFolderChangeTime())>monitor.waitBeforeCheckTime) {
                        // Checks folder contents and refreshes view if necessary
                        monitor.waitBeforeCheckTime = monitor.checkAndRefresh(forceRefresh);
                        monitor.lastCheckTimestamp = System.currentTimeMillis();
                    }
                }
            }
        }
    }
    
    /**
     * Stops monitoring (stops monitoring thread).
     */
    public void stop() {
        synchronized(INIT_LOCK_OBJ) {
            monitorThread = null;
        }
    }


    /**
     * Forces this monitor to update current folder information. This method should be called when a folder has been
     * manually refreshed, so that this monitor doesn't detect changes and try to refresh the table again.
     */
    private void updateFolderInfo() {
        // Reset time average
        totalCheckTime = 0;
        nbSamples = 0;
    }
	
	
    /**
     * Refresh the file table if running file jobs could not change the current file table's folder and if current
     * folder's date has changed.
     *
     * @return the time (msec) to wait before next refresh attempt
     * Note that folder change check took an average of N milliseconds, the returned value will be at least N*WAIT_MULTIPLIER
     */
    private synchronized long checkAndRefresh(boolean forceRefresh) {
        if (!mayFolderChangeByFileJob() && isFolderChanged(forceRefresh)) {
            // Try and refresh current folder in a separate thread as to not lock monitor thread
            folderPanel.tryRefreshCurrentFolder();
            return nbSamples==0 ?
                    waitAfterRefresh
                    : Math.max(waitAfterRefresh, (int)(WAIT_MULTIPLIER*(totalCheckTime/(float)nbSamples)));
        }

        return nbSamples==0 ?
                checkPeriod
                : Math.max(checkPeriod, (int)(WAIT_MULTIPLIER*(totalCheckTime/(float)nbSamples)));
    }

    private boolean mayFolderChangeByFileJob() {
        return JobsManager.getInstance().mayFolderChangeByExistingJob(folderPanel.getCurrentFolder());
    }

    private boolean isFolderChanged(boolean forceRefresh) {
        // Update time average next loop
        long timeStamp = System.currentTimeMillis();

        MonitoredFile currentFolder = folderPanel.getCurrentFolder();
        boolean changed = currentFolder.isChanged(!forceRefresh);

        totalCheckTime += System.currentTimeMillis()-timeStamp;
        nbSamples++;

        if (!changed)
            return false;

        LOGGER.debug(this+" ("+currentFolder.getName()+") Detected changes in current folder, refreshing table!");
        return true;
    }

    /////////////////////////////////////
    // LocationListener implementation //
    /////////////////////////////////////

    public void locationChanging(LocationEvent locationEvent) {
        folderChanging = true;
    }

    public void locationChanged(LocationEvent locationEvent) {
        // Update new current folder info
        updateFolderInfo();

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

    ////////////////////////////////////////
    // WindowFocusListener implementation //
    ////////////////////////////////////////

    @Override
    public void windowGainedFocus(WindowEvent e) {
        LOGGER.debug("{}: setting forceRefresh as MainFrame gained focus", this);
        synchronized (monitorThread) {
            forceRefresh = true;
        }
    }

    @Override
    public void windowLostFocus(WindowEvent e) {}
}
