/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.ui.macosx;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;
import com.mucommander.Launcher;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.WindowManager;


/**
 * This class registers the About, Preferences and Quit handlers using the com.apple.eawt API available
 * under Java 1.4 and up.
 *
 * @author Maxence Bernard
 */
class EAWTHandler implements ApplicationListener {

    public EAWTHandler() {
        Application app = new Application();
        // Enable the 'About' menu item
        app.setEnabledAboutMenu(true);
        // Enable the 'Preferences' menu item
        app.setEnabledPreferencesMenu(true);
        // Register this ApplicationListener
        app.addApplicationListener(this);
    }

    public void handleAbout(ApplicationEvent event) {
        event.setHandled(true);
        OSXIntegration.showAbout();
    }

    public void handlePreferences(ApplicationEvent event) {
        event.setHandled(true);
        OSXIntegration.showPreferences();
    }

    public void handleQuit(ApplicationEvent event) {
        // Accept or reject the request to quit based on user's response
        event.setHandled(OSXIntegration.doQuit());
    }

    public void handleOpenApplication(ApplicationEvent event) {
        // No-op
    }

    public void handleReOpenApplication(ApplicationEvent event) {
        // No-op
    }

    public void handleOpenFile(ApplicationEvent event) {
        // Wait until the application has been launched. This step is required to properly handle the case where the 
        // application is launched with a file to open, for instance when drag-n-dropping a file to the Dock icon
        // when muCommander is not started yet. In this case, this method is called while Launcher is still busy
        // launching the application (no mainframe exists yet).
        Launcher.waitUntilLaunched();

        AbstractFile file = FileFactory.getFile(event.getFilename());
        FolderPanel activePanel = WindowManager.getCurrentMainFrame().getActivePanel();
        if(file.isBrowsable())
            activePanel.tryChangeCurrentFolder(file);
        else
            activePanel.tryChangeCurrentFolder(file.getParent(), file, false);
    }

    public void handlePrintFile(ApplicationEvent event) {
        // No-op
    }
}