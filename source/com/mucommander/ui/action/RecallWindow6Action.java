package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * Recalls window number 6 (brings it to the front).
 *
 * @author Maxence Bernard
 */
public class RecallWindow6Action extends RecallWindowAction {

    public RecallWindow6Action(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        putValue(WINDOW_NUMBER_PROPERTY_KEY, "6");
    }
}
