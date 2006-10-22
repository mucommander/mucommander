
package com.mucommander.ui.event;

import com.mucommander.ui.FolderPanel;


/**
 * Interface to be implemented by classes that wish to be notified of active table changes on a particular MainFrame.
 * Those classes need to be registered to receive those events, this can be done by calling
 * {@link com.mucommander.ui.MainFrame#addActivePanelListener(ActivePanelListener)}.
 *
 * @see com.mucommander.ui.MainFrame
 * @author Maxence Bernard
 */
public interface ActivePanelListener {

    /**
     * This method is invoked when the currently active (i.e. that has focus) folder panel has changed on the MainFrame.
     *
     * @param folderPanel the new active FolderPanel.
     */
    public void activePanelChanged(FolderPanel folderPanel);
}
