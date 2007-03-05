package com.mucommander.ui.action;

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * This action opens a URL in the system's default browser. This action is enabled only if the OS/Window manager
 * is capable of doing do.
 * The URL to open must
 *
 *
 *
 * @author Maxence Bernard
 */
public class OpenURLInBrowserAction extends MucoAction {

    public final static String URL_PROPERTY_KEY = "url";

    
    public OpenURLInBrowserAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        // Enable this action only if the current platform is capable of opening URLs in the default browser.
        setEnabled(PlatformManager.canOpenUrl());
    }

    
    public void performAction() {
        Object url = getValue(URL_PROPERTY_KEY);

        if(url!=null && (url instanceof String)) {
            AbstractFile file = FileFactory.getFile((String)url);

            if(file!=null)
                PlatformManager.openUrl(file);
        }
    }
}
