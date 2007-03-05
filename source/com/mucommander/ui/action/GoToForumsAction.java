package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * This action opens the mucommander.com forums URL in the system's default browser.
 *
 * @author Maxence Bernard
 */
public class GoToForumsAction extends OpenURLInBrowserAction {

    public GoToForumsAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        putValue(URL_PROPERTY_KEY, com.mucommander.RuntimeConstants.FORUMS_URL);
    }
}
