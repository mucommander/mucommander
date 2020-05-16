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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.CachedFile;
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

    private AbstractFile folder;
    private boolean findWorkableFolder;
    private boolean changeLockedTab;
    private FileURL folderURL;
    private AbstractFile fileToSelect;
    private MainFrame mainFrame;
    private FolderPanel folderPanel;
    private LocationManager locationManager;
    private LocationChanger locationChanger;

    public SearchUpdaterThread(AbstractFile folder, boolean findWorkableFolder, boolean changeLockedTab,
            MainFrame mainFrame, FolderPanel folderPanel, LocationManager locationManager, LocationChanger locationChanger) {
        this(mainFrame, folderPanel, locationManager, locationChanger);
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
     * @param changeLockedTab
     */
    public SearchUpdaterThread(FileURL folderURL, boolean changeLockedTab,
            MainFrame mainFrame, FolderPanel folderPanel, LocationManager locationManager, LocationChanger locationChanger) {
        this(mainFrame, folderPanel, locationManager, locationChanger);
        this.folderURL = folderURL;
        this.changeLockedTab = changeLockedTab;

        setPriority(Thread.MAX_PRIORITY);
    }

    private SearchUpdaterThread(MainFrame mainFrame, FolderPanel folderPanel, LocationManager locationManager, LocationChanger locationChanger) {
        this.mainFrame = mainFrame;
        this.folderPanel = folderPanel;
        this.locationManager = locationManager;
        this.locationChanger = locationChanger;
    }

    @Override
    public void selectThisFileAfter(AbstractFile fileToSelect) {
        // no-op
    }

    @Override
    public boolean tryKill() {
        ((SearchFile) folder).stopSearch();
        return true;
    }

    @Override
    public void run() {
        try {
            folder = FileFactory.getFile(folderURL, true);

            SearchFile searchFile = (SearchFile) folder;
//            if (locationManager.getCurrentFolderDate() == 0)

            if (!searchFile.isSearchStarted()) {
                searchFile.startSearch(mainFrame);
                locationManager.fireLocationChanging(folderURL);
                // started started -> 15% complete
                folderPanel.setProgressValue(15);    
            }

            boolean searchDone = searchFile.isSearchCompleted();

            LOGGER.trace("calling setCurrentFolder");

            // Change the file table's current folder and select the specified file (if any)
            locationChanger.setCurrentFolder(folder, fileToSelect, changeLockedTab, searchDone);

            // folder set -> 95% complete
            folderPanel.setProgressValue(searchDone ? 95 : 50);

            locationChanger.cleanChangeFolderThread();

            if (searchDone)
                cleanup();
        }
        catch(Exception e) {
            e.printStackTrace();
            LOGGER.debug("Caught exception", e);
/*
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

            // Stop looping!
            break; */
        }
    }

    public void cleanup() {
     // Clear the interrupted flag in case this thread has been killed using #interrupt().
        // Not doing this could cause some of the code called by this method to be interrupted (because this thread
        // is interrupted) and throw an exception
        interrupted();

        // Reset location field's progress bar
        folderPanel.setProgressValue(0);
    }
}
