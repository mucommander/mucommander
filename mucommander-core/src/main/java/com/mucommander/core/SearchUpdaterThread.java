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
import java.io.IOException;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.protocol.search.SearchFile;
import com.mucommander.job.FileJobState;
import com.mucommander.job.impl.SearchJob;
import com.mucommander.search.SearchBuilder;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.event.LocationManager;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

/**
 * @author Arik Hadas
 */
public class SearchUpdaterThread extends ChangeFolderThread {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeFolderThread.class);

    private SearchFile search;
    private boolean changeLockedTab;
    private MainFrame mainFrame;
    private FolderPanel folderPanel;
    private LocationChanger locationChanger;

    private boolean stoppedDueToMaxResults;

    private final static int CONTINUE_ACTION = 0;
    private final static int STOP_ACTION = 1;

    private static final EnumSet<FileJobState> COMPLETED_SEARCH_STATUSES = EnumSet.of(FileJobState.FINISHED, FileJobState.INTERRUPTED);

    public SearchUpdaterThread(FileURL folderURL, boolean changeLockedTab,
            MainFrame mainFrame, FolderPanel folderPanel, LocationManager locationManager, LocationChanger locationChanger) {
        super(locationManager, folderURL);
        this.mainFrame = mainFrame;
        this.folderPanel = folderPanel;
        this.locationChanger = locationChanger;
        this.changeLockedTab = changeLockedTab;
    }

    @Override
    public void selectThisFileAfter(AbstractFile fileToSelect) {
        // no-op
    }

    @Override
    public void run() {
        LOGGER.debug("starting search updater...");
        boolean searchEndedSuccessfully = false;

        // Show some progress in the progress bar to give hope
        folderPanel.setProgressValue(10);

        try {
            // Set cursor to hourglass/wait
            mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            // Render all actions inactive while changing folder
            mainFrame.setNoEventsMode(true);

            search = (SearchFile) FileFactory.getFile(folderURL, true);

            // Initiate the search thread
            search.start(SearchBuilder.newSearch().mainFrame(mainFrame));

            // Retrieve the timestamp of the latest search results
            long date = search.getDate();

            // Update the file table, most likely with empty result set
            setCurrentFolder(false);

            // Search started, advance progress
            folderPanel.setProgressValue(50);

            while(true) {
                if (killed)
                    throw new InterruptedException("search-updater thread stopped");

                sleep(1000);

                boolean searchCompleted = COMPLETED_SEARCH_STATUSES.contains(search.getSearchPhase());
                // Retrieve the timestamp of the latest search results
                long currentDate = search.getDate();
                boolean searchResultsChanged = currentDate != date;

                if (searchCompleted) {
                    synchronized(KILL_LOCK) {
                        if (killed)
                            throw new InterruptedException("search-updater thread stopped");
                        doNotKill = true;
                    }

                    if (searchResultsChanged) {
                        folderPanel.setProgressValue(75);
                        setCurrentFolder(true);
                    } else {
                        folderPanel.setProgressValue(90);
                        folderPanel.getLocationManager().fireLocationChanged(search.getURL());
                    }

                    // folder set -> 95% complete
                    folderPanel.setProgressValue(95);

                    searchEndedSuccessfully = true;

                    break;
                }

                if (searchResultsChanged) {
                    date = currentDate;

                    // Change the file table's current folder and select the specified file (if any)
                    setCurrentFolder(false);

                    if (search.isPausedToDueMaxResults()) {
                        // Restore default cursor
                        mainFrame.setCursor(Cursor.getDefaultCursor());

                        // Download or browse file ?
                        int ret = showSearchExceededMaxResults();

                        if (ret==-1 || ret==STOP_ACTION) {
                            stoppedDueToMaxResults = true;
                            folderPanel.getLocationManager().fireLocationChanged(search.getURL());
                            break;
                        }

                        // Set cursor to hourglass/wait
                        mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        // continue the paused search
                        search.continueSearch();
                    }
                }
            }
        }
        catch(Exception e) {
            LOGGER.debug("Caught exception", e);

            if (search == null) {
                locationChanger.showFolderDoesNotExistDialog();
            } else {
                search.stop();
                if (killed)
                    setCurrentFolderDismissException();
            }
        }

        synchronized(KILL_LOCK) {
            // Clean things up
            cleanup(searchEndedSuccessfully);
        }
    }

    private void setCurrentFolder(boolean fireLocationChanged) throws UnsupportedFileOperationException, IOException {
        LOGGER.trace("calling setCurrentFolder");
        locationChanger.setCurrentFolder(search, null, changeLockedTab, fireLocationChanged);
    }

    private void setCurrentFolderDismissException() {
        try {
            setCurrentFolder(false);
        } catch (IOException e) {
            LOGGER.debug("failed to set current folder", e);
        }
    }

    private int showSearchExceededMaxResults() {
        return new QuestionDialog(mainFrame,
                Translator.get("warning"),
                Translator.get("search.exceeds_max_results", String.valueOf(SearchFile.MAX_RESULTS)),
                mainFrame,
                new String[] {Translator.get("yes"), Translator.get("no")},
                new int[] {STOP_ACTION, CONTINUE_ACTION},
                0).getActionValue();
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
            if (killed || stoppedDueToMaxResults)
                locationManager.fireLocationCancelled(folderURL);
            else
                locationManager.fireLocationFailed(folderURL);
        }
    }
}
