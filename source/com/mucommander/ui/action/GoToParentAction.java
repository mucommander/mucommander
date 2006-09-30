package com.mucommander.ui.action;

import com.mucommander.ui.FolderPanel;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.event.TableChangeListener;

/**
 * This action changes the current folder of the currently active FolderPanel to the current folder's parent.
 *
 * @author Maxence Bernard
 */
public class GoToParentAction extends MucoAction implements TableChangeListener, LocationListener {

    public GoToParentAction(MainFrame mainFrame) {
        super(mainFrame);

        // Listen to active table change events
        mainFrame.addTableChangeListener(this);

        // Listen to location change events
        mainFrame.getFolderPanel1().getLocationManager().addLocationListener(this);
        mainFrame.getFolderPanel2().getLocationManager().addLocationListener(this);

        toggleEnabledState();
    }


    public void performAction() {
        mainFrame.getLastActiveTable().getFolderPanel().goToParent();
    }


    /**
     * Enables or disables this action based on the currently active folder's
     * has a parent, this action will be enabled, if not it will be disabled.
     */
    private void toggleEnabledState() {
        setEnabled(mainFrame.getLastActiveTable().getFolderPanel().getCurrentFolder().getParent()!=null);
    }


    /////////////////////////////////
    // TableChangeListener methods //
    /////////////////////////////////

    public void tableChanged(FolderPanel folderPanel) {
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
