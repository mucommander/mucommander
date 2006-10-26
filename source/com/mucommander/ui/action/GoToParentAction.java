package com.mucommander.ui.action;

import com.mucommander.ui.FolderPanel;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.event.ActivePanelListener;

/**
 * This action changes the current folder of the currently active FolderPanel to the current folder's parent.
 *
 * @author Maxence Bernard
 */
public class GoToParentAction extends MucoAction implements ActivePanelListener, LocationListener {

    public GoToParentAction(MainFrame mainFrame) {
        super(mainFrame);

        // Listen to active table change events
        mainFrame.addActivePanelListener(this);

        // Listen to location change events
        mainFrame.getFolderPanel1().getLocationManager().addLocationListener(this);
        mainFrame.getFolderPanel2().getLocationManager().addLocationListener(this);

        toggleEnabledState();
    }


    public void performAction() {
        mainFrame.getActiveTable().getFolderPanel().goToParent();
    }


    /**
     * Enables or disables this action based on the currently active folder's
     * has a parent, this action will be enabled, if not it will be disabled.
     */
    private void toggleEnabledState() {
        setEnabled(mainFrame.getActiveTable().getFolderPanel().getCurrentFolder().getParent()!=null);
    }


    /////////////////////////////////
    // ActivePanelListener methods //
    /////////////////////////////////

    public void activePanelChanged(FolderPanel folderPanel) {
        toggleEnabledState();
    }


    //////////////////////////////
    // LocationListener methods //
    //////////////////////////////

    public void locationChanged(LocationEvent e) {
        toggleEnabledState();
    }

    public void locationChanging(LocationEvent e) {
    }

    public void locationCancelled(LocationEvent e) {
    }
}
