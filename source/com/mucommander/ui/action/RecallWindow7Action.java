package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * Recalls window number 7 (brings it to the front).
 *
 * @author Maxence Bernard
 */
public class RecallWindow7Action extends RecallWindowAction {

    public RecallWindow7Action(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        putValue(WINDOW_NUMBER_PROPERTY_KEY, "7");
    }
}
