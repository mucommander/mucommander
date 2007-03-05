package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * This action invokes the garbage collector and is here for debugging purposes only.
 *
 * @author Maxence Bernard
 */
public class GarbageCollectAction extends MucoAction {

    public GarbageCollectAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("calling System.gc()");

        System.gc();

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("freeMemory="+Runtime.getRuntime().freeMemory()+" totalMemory="+Runtime.getRuntime().totalMemory());
    }
}
