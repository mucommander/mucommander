package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

/**
 * This action opens the mucommander.com donation page URL in the system's default browser.
 *
 * @author Maxence Bernard
 */
public class DonateAction extends OpenURLInBrowserAction {

    public DonateAction(MainFrame mainFrame) {
        super(mainFrame, com.mucommander.RuntimeConstants.DONATION_URL);
    }
}
