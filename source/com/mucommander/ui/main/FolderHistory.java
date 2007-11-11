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


package com.mucommander.ui.main;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileProtocols;
import com.mucommander.file.FileURL;
import com.mucommander.file.impl.local.LocalFile;

import java.io.IOException;
import java.util.Vector;


/**
 * This class maintains a history of visited folders for a given panel, and provides methods to go back and go forward
 * in the folder history.
 *
 * <p>FolderHistory also keeps track of the last visited folder that can be saved and recalled next time the
 * application is started.
 *
 * <p>There is a limit to the number of folders the history can contain, set by {@link #HISTORY_CAPACITY}. 
 *
 * @author Maxence Bernard
 */
public class FolderHistory {

    /** Maximum number of elements the folder history can contain */
    private final static int HISTORY_CAPACITY = 100;

    /** List of visited folders, ordered by recency */
    private Vector history = new Vector(HISTORY_CAPACITY+1);

    /** Index of current folder in history */
    private int historyIndex = -1;

    /** FolderPanel which is being monitored */
    private FolderPanel folderPanel; 

    /** Last folder which can be recalled on next startup */
    private String lastRecallableFolder;


    /**
     * Creates a new FolderHistory instance which will keep track of visited folders in the given FolderPanel.
     */
    public FolderHistory(FolderPanel folderPanel) {
        this.folderPanel = folderPanel;
    }
    

    /**
     * Adds the specified folder to history. The folder won't be added if the previous folder is the same.
     *
     * <p>This method is called by FolderPanel each time a folder is changed.
     */
    void addToHistory(AbstractFile folder) {
        int historySize = history.size();
        FileURL folderURL = folder.getURL();

        // Do not add folder to history if new current folder is the same as previous folder
        if (historyIndex<0 || !folderURL.equals(history.elementAt(historyIndex))) {
            historyIndex++;

            // Delete 'forward' history items if any
            for(int i=historyIndex; i<historySize; i++) {
                history.removeElementAt(historyIndex);
            }

            // If capacity is reached, remove first folder
            if(history.size()>=HISTORY_CAPACITY) {
                history.removeElementAt(0);
                historyIndex--;
            }

            // Add previous folder to history
            history.add(folderURL);
        }

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("history= "+history+" historyIndex="+historyIndex);

        // Save last recallable folder on startup, only if :
        //  - it is a directory on a local filesytem
        //  - it doesn't look like a removable media drive (cd/dvd/floppy), especially in order to prevent
        // Java from triggering that dreaded 'Drive not ready' popup.
        try {
            if(folderURL.getProtocol().equals(FileProtocols.FILE) && folder.isDirectory() && (folder instanceof LocalFile) && !((LocalFile)folder.getRoot()).guessRemovableDrive()) {
                this.lastRecallableFolder = folder.getAbsolutePath();
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("lastRecallableFolder= "+lastRecallableFolder);
            }
        }
        catch(IOException e) {
            // last folder's value won't be updated that's all 
        }
    }


    /**
     * Changes current folder to be the previous one in folder history.
     * Does nothing if there is no previous folder in history. 
     */
    public synchronized void goBack() {
        if (historyIndex==0)
            return;
		
        folderPanel.tryChangeCurrentFolder((FileURL)history.elementAt(--historyIndex));
    }
	
    /**
     * Changes current folder to be the next one in folder history.
     * Does nothing if there is no next folder in history. 
     */
    public synchronized void goForward() {
        if (historyIndex==history.size()-1)
            return;
		
        folderPanel.tryChangeCurrentFolder((FileURL)history.elementAt(++historyIndex));
    }


    /**
     * Returns <code>true</code> if there is at least one folder 'back' in the history.
     */
    public boolean hasBackFolder() {
        return historyIndex>0;
    }

    /**
     * Returns <code>true</code> if there is at least one folder 'forward' in the history.
     */
    public boolean hasForwardFolder() {
        return historyIndex!=history.size()-1;
    }


    /**
     * Returns a list of 'back' folders, most recently visited folder first. The returned array may be empty if there
     * currently isn't any 'back' folder in history, but may never be null.
     */
    public FileURL[] getBackFolders() {
        if(!hasBackFolder())
            return new FileURL[0];

        int backLen = historyIndex;
        FileURL urls[] = new FileURL[backLen];

        int cur = 0;
        for(int i=historyIndex-1; i>=0; i--)
            urls[cur++] = (FileURL)history.elementAt(i);

        return urls;
    }


    /**
     * Returns a list of 'forward' folders, most recently visited folder first. The returned array may be empty if there
     * currently isn't any 'forward' folder in history, but may never be null.
     */
    public FileURL[] getForwardFolders() {
        if(!hasForwardFolder())
            return new FileURL[0];

        int historySize = history.size();
        FileURL urls[] = new FileURL[historySize-historyIndex-1];

        int cur = 0;
        for(int i=historyIndex+1; i<historySize; i++)
            urls[cur++] = (FileURL)history.elementAt(i);

        return urls;
    }


    /**
     * Returns true if the folder history contains the given FileURL, either as a back or forward folder, or as the
     * current folder.
     */
    public boolean historyContains(FileURL folderURL) {
        return history.contains(folderURL);
    }


    /**
     * Returns the last visited folder that can be saved when the application terminates, and recalled next time
     * the application is started.
     *
     * <p>The returned folder will NOT be a folder on a remote filesystem
     * which would be likely not to be reachable next time the app is started, or a removable media drive
     * (cd/dvd/floppy) under Windows, which would trigger a nasty 'drive not ready' popup dialog if the drive
     * is not available or the media has changed.
     */
    public String getLastRecallableFolder() {
        return this.lastRecallableFolder;
    }
}