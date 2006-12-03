
package com.mucommander.ui.event;

import com.mucommander.ui.FolderPanel;
import com.mucommander.file.FileURL;


/**
 * Event used to indicate that a folder change is or has occurred. This event is passed to to every LocationListener
 * that registered to receive those events on a particular FolderPanel.
 *
 * @author Maxence Bernard
 */
public class LocationEvent {

    /** FolderPanel where location has or is being changed */
    private FolderPanel folderPanel;

    /** URL of the folder that has or is being changed */
    private FileURL folderURL;


    /**
     * Creates a new LocationEvent.
     *
     * @param folderPanel FolderPanel where location has or is being changed.
     * @param folderURL url of the folder that has or is being changed
     */
    public LocationEvent(FolderPanel folderPanel, FileURL folderURL) {
        this.folderPanel = folderPanel;
        this.folderURL = folderURL;
    }


    /**
     * Returns the FolderPanel instance where location has or is being changed.
     */
    public FolderPanel getFolderPanel() {
        return folderPanel;
    }


    /**
     * Returns the URL to the folder that has or is being changed.
     */
    public FileURL getFolderURL() {
        return folderURL;
    }
}
