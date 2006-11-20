package com.mucommander.ui.action;

import com.mucommander.ui.FolderPanel;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.event.ActivePanelListener;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;

/**
 * This action recalls the previous folder in the current FolderPanel's history.
 *
 * @author Maxence Bernard
 */
public class GoBackAction extends MucoAction implements ActivePanelListener, LocationListener {

    public GoBackAction(MainFrame mainFrame) {
        super(mainFrame);

        // Listen to active table change events
        mainFrame.addActivePanelListener(this);

        // Listen to location change events
        mainFrame.getFolderPanel1().getLocationManager().addLocationListener(this);
        mainFrame.getFolderPanel2().getLocationManager().addLocationListener(this);

        toggleEnabledState();
    }


    public void performAction() {
        mainFrame.getActiveTable().getFolderPanel().getFolderHistory().goBack();
    }


    /**
     * Enables or disables this action based on the history of the currently active FolderPanel: if there is a previous
     * folder in the history, this action will be enabled, if not it will be disabled.
     */
    private void toggleEnabledState() {
        setEnabled(mainFrame.getActiveTable().getFolderPanel().getFolderHistory().hasBackFolder());
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

    public void locationFailed(LocationEvent e) {
    }
}
