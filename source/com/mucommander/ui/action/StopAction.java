package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.FolderPanel;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action is invoked to stop a running location change.
 *
 * @author Maxence Bernard
 */
public class StopAction extends MucoAction implements LocationListener {

    public StopAction(MainFrame mainFrame) {
        super(mainFrame, "toolbar.stop", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));

        // This action is initially disabled and enabled only during a folder change
        setEnabled(false);

        // Listen to location change events
        mainFrame.getFolderPanel1().getLocationManager().addLocationListener(this);
        mainFrame.getFolderPanel2().getLocationManager().addLocationListener(this);
    }

    public void performAction(MainFrame mainFrame) {
        FolderPanel folderPanel = mainFrame.getLastActiveTable().getFolderPanel();
        FolderPanel.ChangeFolderThread changeFolderThread = folderPanel.getChangeFolderThread();

        if(changeFolderThread!=null)
            changeFolderThread.tryKill();
    }

    public boolean ignoreEventsWhileInNoEventsMode() {
        return false;
    }


    //////////////////////////////
    // LocationListener methods //
    //////////////////////////////

    public void locationChanged(LocationEvent e) {
        setEnabled(false);
    }

    public void locationChanging(LocationEvent e) {
        setEnabled(true);
    }

    public void locationCancelled(LocationEvent e) {
        setEnabled(false);
    }
}
