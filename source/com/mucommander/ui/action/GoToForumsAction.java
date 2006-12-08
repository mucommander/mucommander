package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

/**
 * This action opens the mucommander.com forums URL in the system's default browser.
 *
 * @author Maxence Bernard
 */
public class GoToForumsAction extends OpenURLInBrowserAction {

    public GoToForumsAction(MainFrame mainFrame) {
        super(mainFrame, com.mucommander.RuntimeConstants.FORUMS_URL);
    }
}
