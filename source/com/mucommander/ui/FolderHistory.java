
package com.mucommander.ui;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FSFile;
import com.mucommander.file.FileProtocols;

import java.util.Vector;


/**
 * This class maintains a history of visited folders for a given panel, and provides methods to go back and go forward
 * in the folder history.
 *
 * <p>FolderHistory also keeps track of the last visited folder that can be saved and recalled next time the
 * application is started.
 *
 * <p>There is a limit to the number of folders the history can contain, defined by {@link #HISTORY_CAPACITY}. 
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
     * Adds the specified folder to history.The folder won't be added if the previous folder is the same.
     *
     * <p>This method is called by FolderPanel each time a folder is changed.
     */
    void addToHistory(AbstractFile folder) {
        int historySize = history.size();
        // Do not add folder to history if new current folder is the same as previous folder
        if (historyIndex<0 || !folder.equals(history.elementAt(historyIndex))) {
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
            history.add(folder);
        }

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("history= "+history+" historyIndex="+historyIndex);

        // Save last recallable folder on startup, only if :
        //  - it is a directory on a local filesytem
        //  - it doesn't look like a removable media drive (cd/dvd/floppy), especially in order to prevent
        // Java from triggering that dreaded 'Drive not ready' popup.
        if(folder.getURL().getProtocol().equals(FileProtocols.FILE) && folder.isDirectory() && !((FSFile)folder.getRoot()).guessRemovableDrive()) {
            this.lastRecallableFolder = folder.getAbsolutePath();
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("lastRecallableFolder= "+lastRecallableFolder);
        }
    }


    /**
     * Changes current folder to be the previous one in folder history.
     * Does nothing if there is no previous folder in history. 
     */
    public synchronized void goBack() {
        if (historyIndex==0)
            return;
		
        folderPanel.tryChangeCurrentFolder((AbstractFile)history.elementAt(--historyIndex));
    }
	
    /**
     * Changes current folder to be the next one in folder history.
     * Does nothing if there is no next folder in history. 
     */
    public synchronized void goForward() {
        if (historyIndex==history.size()-1)
            return;
		
        folderPanel.tryChangeCurrentFolder((AbstractFile)history.elementAt(++historyIndex));
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