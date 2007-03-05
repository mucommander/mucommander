package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * This action opens the mucommander.com bug repository URL in the system's default browser.
 *
 * @author Maxence Bernard
 */
public class ReportBugAction extends OpenURLInBrowserAction {

    public ReportBugAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        putValue(URL_PROPERTY_KEY, com.mucommander.RuntimeConstants.BUG_REPOSITORY_URL);
    }
}
