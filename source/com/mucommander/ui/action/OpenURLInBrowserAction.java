package com.mucommander.ui.action;

import com.mucommander.file.FileFactory;
import com.mucommander.ui.MainFrame;
import com.mucommander.PlatformManager;
import com.mucommander.text.Translator;

import javax.swing.*;

/**
 * This action opens a given URL in the system's default browser, if the OS/Window manager is capable of doing so.
 *
 * @author Maxence Bernard
 */
public class OpenURLInBrowserAction extends MucoAction {

    private String url;

    public OpenURLInBrowserAction(MainFrame mainFrame, String url) {
        super(mainFrame);
        this.url = url;
    }

    public void performAction() {
        if(PlatformManager.canOpenUrl())
            PlatformManager.openUrl(FileFactory.getFile(url));
    }
}
