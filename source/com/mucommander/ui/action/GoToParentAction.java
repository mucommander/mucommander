package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.FolderPanel;
import com.mucommander.event.LocationEvent;
import com.mucommander.event.TableChangeListener;
import com.mucommander.event.LocationListener;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action changes the current folder of the currently active FolderPanel to the current folder's parent.
 *
 * @author Maxence Bernard
 */
public class GoToParentAction extends MucoAction implements TableChangeListener, LocationListener {

    public GoToParentAction(MainFrame mainFrame) {
        super(mainFrame, "view_menu.go_to_parent", KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));

        // Listen to active table change events
        mainFrame.addTableChangeListener(this);

        // Listen to location change events
        mainFrame.getFolderPanel1().addLocationListener(this);
        mainFrame.getFolderPanel2().addLocationListener(this);

        toggleEnabledState();
    }


    public void performAction(MainFrame mainFrame) {
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
