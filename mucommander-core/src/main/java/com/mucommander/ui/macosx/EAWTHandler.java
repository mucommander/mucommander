/*
 * This file is part of muCommander, http://www.mucommander.com
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

import java.awt.Desktop;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitResponse;

import com.mucommander.muCommander;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.WindowManager;

/**
 * This class registers the About, Preferences and Quit handlers.
 *
 * @author Arik Hadas, Maxence Bernard
 */
class EAWTHandler {

    public EAWTHandler() {
        Desktop desktop = Desktop.getDesktop();
        desktop.setAboutHandler(e -> OSXIntegration.showAbout());
        desktop.setPreferencesHandler(e -> OSXIntegration.showPreferences());
        desktop.setQuitHandler(this::handleQuitRequestWith);
        desktop.setOpenFileHandler(this::openFiles);
    }

    public void handleQuitRequestWith(final QuitEvent e, final QuitResponse response) {
        if (OSXIntegration.doQuit())
            response.performQuit();
        else
            response.cancelQuit();
    }

    public void openFiles(final OpenFilesEvent e) {
     // Wait until the application has been launched. This step is required to properly handle the case where the
        // application is launched with a file to open, for instance when drag-n-dropping a file to the Dock icon
        // when muCommander is not started yet. In this case, this method is called while Launcher is still busy
        // launching the application (no mainframe exists yet).
        muCommander.waitUntilLaunched();

        AbstractFile file = FileFactory.getFile(e.getFiles().get(0).getAbsolutePath());
        FolderPanel activePanel = WindowManager.getCurrentMainFrame().getActivePanel();
        if (file.isBrowsable())
            activePanel.tryChangeCurrentFolder(file);
        else
            activePanel.tryChangeCurrentFolder(file.getParent(), file, false);
    }
}
