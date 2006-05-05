
package com.mucommander.ui;

import com.mucommander.ui.FolderPanel;
import com.mucommander.event.LocationListener;
import com.mucommander.event.LocationEvent;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FSFile;

import java.util.Vector;


public class FolderHistory implements LocationListener {

    private Vector history;
    private int historyIndex;

    private FolderPanel folderPanel; 

    private String lastSavableFolder;

    
    public FolderHistory(FolderPanel folderPanel) {
        this.folderPanel = folderPanel;
        
        // Listen to folder changes
        folderPanel.addLocationListener(this);

        // Initialize history vector
        history = new Vector();
    	historyIndex = -1;
    }


    /**
     * Changes current folder to be the previous one in folder history.
     * Does nothing if there is no previous folder in history. 
     */
    public synchronized void goBack() {
        if (historyIndex==0)
            return;
		
        AbstractFile folder = (AbstractFile)history.elementAt(--historyIndex);
        folderPanel.trySetCurrentFolder(folder, false);
    }
	
    /**
     * Changes current folder to be the next one in folder history.
     * Does nothing if there is no next folder in history. 
     */
    public synchronized void goForward() {
        if (historyIndex==history.size()-1)
            return;
		
        AbstractFile folder = (AbstractFile)history.elementAt(++historyIndex);
        folderPanel.trySetCurrentFolder(folder, false);
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
     * Returns the last folder the user went to before quitting the application.
     * This folder will be loaded on next mucommander startup, so the returned folder should NOT be
     * a folder on a remote filesystem (likely not to be reachable next time the app is started)
     * or a removable media drive (cd/dvd/floppy) if under Windows, as it would trigger a nasty 
     * 'drive not ready' popup dialog.
     */
    public String getLastSavableFolder() {
        return this.lastSavableFolder;
    }
	

    //////////////////////////////
    // LocationListener methods //
    //////////////////////////////
	
    public void locationChanged(LocationEvent e) {
        AbstractFile folder = folderPanel.getCurrentFolder();
        
        int historySize = history.size();
        // Do not add folder to history if new current folder is the same as previous folder
        // (note: currentFolder is null first time)
        if (historyIndex<0 || !folder.equals(history.elementAt(historyIndex))) {
            historyIndex++;

            // Delete 'forward' history items if any
            for(int i=historyIndex; i<historySize; i++) {
                history.removeElementAt(historyIndex);
            }
            // Add previous folder to history
            history.add(folder);
        }

        // Save last folder recallable on startup only if :
        //  - it is a directory on a local filesytem
        //  - it doesn't look like a removable media drive (cd/dvd/floppy), especially in order to prevent
        // Java from triggering that dreaded 'Drive not ready' popup.
        if(folder.getURL().getProtocol().equals("file") && folder.isDirectory() && !((FSFile)folder.getRoot()).guessRemovableDrive()) {
            this.lastSavableFolder = folder.getAbsolutePath();
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("lastSavableFolder= "+lastSavableFolder);
        }
    }

    public void locationChanging(LocationEvent e) {
    }
	
    public void locationCancelled(LocationEvent e) {
    }    
}