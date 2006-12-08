package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

/**
 * This action opens the mucommander.com bug repository URL in the system's default browser.
 *
 * @author Maxence Bernard
 */
public class ReportBugAction extends OpenURLInBrowserAction {

    public ReportBugAction(MainFrame mainFrame) {
        super(mainFrame, com.mucommander.RuntimeConstants.BUG_REPOSITORY_URL);
    }
}
