
package com.mucommander.ui.event;

import com.mucommander.ui.FolderPanel;


/**
 * Event used to indicate that a folder change is or has occurred. This event is passed to to every LocationListener
 * that registered to receive those events on a particular FolderPanel.
 *
 * @author Maxence Bernard
 */
public class LocationEvent {

    /** FolderPanel where location has or is being changed */
    private FolderPanel folderPanel;
	

    /**
     * Creates a new LocationEvent.
     *
     * @param folderPanel FolderPanel where location has or is being changed.
     */
    public LocationEvent(FolderPanel folderPanel) {
        this.folderPanel = folderPanel;
    }


    /**
     * Returns the FolderPanel instance where location has or is being changed.
     */
    public FolderPanel getFolderPanel() {
        return folderPanel;
    }
}
