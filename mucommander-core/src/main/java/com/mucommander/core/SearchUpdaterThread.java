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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.search.file.SearchFile;
import com.mucommander.ui.event.LocationManager;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

/**
 * @author Arik Hadas
 */
public class SearchUpdaterThread extends ChangeFolderThread {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeFolderThread.class);

    private SearchFile folder;
    private boolean changeLockedTab;
    private AbstractFile fileToSelect;
    private MainFrame mainFrame;
    private FolderPanel folderPanel;
    private LocationChanger locationChanger;

    public SearchUpdaterThread(FileURL folderURL, boolean changeLockedTab,
            MainFrame mainFrame, FolderPanel folderPanel, LocationManager locationManager, LocationChanger locationChanger) {
        super(locationManager, folderURL);
        this.mainFrame = mainFrame;
        this.folderPanel = folderPanel;
        this.locationChanger = locationChanger;
        this.changeLockedTab = changeLockedTab;
        setPriority(Thread.MAX_PRIORITY);
    }

    @Override
    public void selectThisFileAfter(AbstractFile fileToSelect) {
        // no-op
    }

    @Override
    public void run() {
        LOGGER.debug("starting search updater...");

        // Show some progress in the progress bar to give hope
        folderPanel.setProgressValue(10);

        try {
            folder = (SearchFile) FileFactory.getFile(folderURL, true);

            // Set cursor to hourglass/wait
            mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            // Render all actions inactive while changing folder
            mainFrame.setNoEventsMode(true);

            folder.startSearch(mainFrame);

            // started started -> 15% complete
            folderPanel.setProgressValue(20);

            long date = folder.getDate();

            // Change the file table's current folder and select the specified file (if any)
            locationChanger.setCurrentFolder(folder, fileToSelect, changeLockedTab, false);

            folderPanel.setProgressValue(50);

            do {
                sleep(1000);

                long currentDate = folder.getDate();
                if (currentDate != date) {
                    date = currentDate;
                    LOGGER.trace("calling setCurrentFolder");

                    // Change the file table's current folder and select the specified file (if any)
                    locationChanger.setCurrentFolder(folder, fileToSelect, changeLockedTab, false);
                }
            } while(!folder.isSearchCompleted());

            synchronized(KILL_LOCK) {
                if(killed) {
                    LOGGER.debug("this thread has been killed, stopping");
                    throw new RuntimeException("killed");
                }
                // From now on, thread cannot be killed (would comprise table integrity)
                doNotKill = true;
            }

            // 
            folderPanel.setProgressValue(80);

            locationChanger.setCurrentFolder(folder, fileToSelect, changeLockedTab, true);

            // folder set -> 95% complete
            folderPanel.setProgressValue(95);

            synchronized(KILL_LOCK) {
                // Clean things up
                cleanup(true);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            LOGGER.debug("Caught exception", e);

            if (killed) {
                LOGGER.debug("stopping search");
                folder.stopSearch();
            }

            synchronized(KILL_LOCK) {
                // Clean things up
                cleanup(false);
            }
        }
    }

    protected void cleanup(boolean folderChangedSuccessfully) {
        // Clear the interrupted flag in case this thread has been killed using #interrupt().
        // Not doing this could cause some of the code called by this method to be interrupted (because this thread
        // is interrupted) and throw an exception
        interrupted();

        // Reset location field's progress bar
        folderPanel.setProgressValue(0);

        // Restore normal mouse cursor
        mainFrame.setCursor(Cursor.getDefaultCursor());

        locationChanger.cleanChangeFolderThread();

        // Make all actions active again
        mainFrame.setNoEventsMode(false);

        if (!folderChangedSuccessfully) {
            // Notifies listeners that location change has been cancelled by the user or has failed
            if(killed)
                locationManager.fireLocationCancelled(folderURL);
            else
                locationManager.fireLocationFailed(folderURL);
        }
    }
}
