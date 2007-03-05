package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * This action opens the mucommander.com URL in the system's default browser.
 *
 * @author Maxence Bernard
 */
public class GoToWebsiteAction extends OpenURLInBrowserAction {

    public GoToWebsiteAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        putValue(URL_PROPERTY_KEY, com.mucommander.RuntimeConstants.HOMEPAGE_URL);
    }
}
