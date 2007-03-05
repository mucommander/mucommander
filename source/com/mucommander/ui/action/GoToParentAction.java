package com.mucommander.ui.action;

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.FolderPanel;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.event.ActivePanelListener;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;

import java.util.Hashtable;

/**
 * This action changes the current folder of the currently active FolderPanel to the current folder's parent.
 * This action only gets enabled when the current folder has a parent.
 *
 * @author Maxence Bernard
 */
public class GoToParentAction extends MucoAction implements ActivePanelListener, LocationListener {

    public GoToParentAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        // Listen to active table change events
        mainFrame.addActivePanelListener(this);

        // Listen to location change events
        mainFrame.getFolderPanel1().getLocationManager().addLocationListener(this);
        mainFrame.getFolderPanel2().getLocationManager().addLocationListener(this);

        // Set initial state
        toggleEnabledState();
    }


    /**
     * Enables or disables this action based on the currently active folder's
     * has a parent, this action will be enabled, if not it will be disabled.
     */
    private void toggleEnabledState() {
        setEnabled(mainFrame.getActiveTable().getFolderPanel().getCurrentFolder().getParent()!=null);
    }


    ///////////////////////////////
    // MucoAction implementation //
    ///////////////////////////////

    public void performAction() {
        // Changes the current folder to make it the current folder's parent.
        // Does nothing if the current folder doesn't have a parent.
        AbstractFile parent;
        FolderPanel folderPanel = mainFrame.getActiveTable().getFolderPanel();
        if((parent=folderPanel.getCurrentFolder().getParent())!=null)
            folderPanel.tryChangeCurrentFolder(parent);
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
