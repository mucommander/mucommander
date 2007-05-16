package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.WindowManager;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Brings all MainFrame windows to front, from the last window index to the first, except for the current
 * (or last active) MainFrame which is brought to the front last. .
 * After this action has been performed, minimized windows will return to a normal state and windows will be stacked
 * in the following order:
 * <ul>
 *  <li>Current MainFrame
 *  <li>MainFrame #1
 *  <li>MainFrame #2
 *  <li>...
 *  <li>MainFrame #N
 * </ul>
 *
 * @author Maxence Bernard
 */
public class BringAllToFrontAction extends MucoAction {

    public BringAllToFrontAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        Vector mainFrames = WindowManager.getMainFrames();
        MainFrame currentMainFrame = WindowManager.getCurrentMainFrame();

        int nbMainFrames = mainFrames.size();
        MainFrame mainFrame;
        for(int i=nbMainFrames-1; i>=0; i--) {
            mainFrame = (MainFrame)mainFrames.elementAt(i);
            if(mainFrame!=currentMainFrame) {
                mainFrame.toFront();
            }
        }

        currentMainFrame.toFront();
    }
}
