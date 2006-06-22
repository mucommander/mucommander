package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.FolderPanel;
import com.mucommander.event.LocationEvent;
import com.mucommander.event.TableChangeListener;
import com.mucommander.event.LocationListener;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action recalls the next folder in the current FolderPanel's history.
 *
 * @author Maxence Bernard
 */
public class GoForwardAction extends MucoAction implements TableChangeListener, LocationListener {

    public GoForwardAction(MainFrame mainFrame) {
        super(mainFrame, "view_menu.go_forward", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_MASK));

        // Listen to active table change events
        mainFrame.addTableChangeListener(this);

        // Listen to location change events
        mainFrame.getFolderPanel1().addLocationListener(this);
        mainFrame.getFolderPanel2().addLocationListener(this);
    }


    public void performAction(MainFrame mainFrame) {
        mainFrame.getLastActiveTable().getFolderPanel().getFolderHistory().goForward();
    }


    /**
     * Enables or disables this action based on the history of the currently active FolderPanel: if there is a next
     * folder in the history, this action will be enabled, if not it will be disabled.
     */
    private void toggleEnabledState() {
        setEnabled(mainFrame.getLastActiveTable().getFolderPanel().getFolderHistory().hasForwardFolder());
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
