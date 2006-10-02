package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

/**
 * This action invokes the garbage collector and is here for debugging purposes only.
 *
 * @author Maxence Bernard
 */
public class GarbageCollectAction extends MucoAction {

    public GarbageCollectAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("calling System.gc()");

        System.gc();
    }
}
