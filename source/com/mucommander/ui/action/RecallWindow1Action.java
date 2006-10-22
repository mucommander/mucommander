package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

/**
 * Recalls window number 1 (brings it to the front). 
 *
 * @author Maxence Bernard
 */
public class RecallWindow1Action extends RecallWindowAction {

    public RecallWindow1Action(MainFrame mainFrame) {
        super(mainFrame, 1);
    }
}
