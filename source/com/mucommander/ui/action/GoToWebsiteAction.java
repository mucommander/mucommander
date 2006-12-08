package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

/**
 * This action opens the mucommander.com URL in the system's default browser.
 *
 * @author Maxence Bernard
 */
public class GoToWebsiteAction extends OpenURLInBrowserAction {

    public GoToWebsiteAction(MainFrame mainFrame) {
        super(mainFrame, com.mucommander.RuntimeConstants.HOMEPAGE_URL);
    }
}
