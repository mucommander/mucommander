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

import com.mucommander.desktop.osx.CoreServiceTracker;

/**
 * This class registers the About, Preferences and Quit handlers.
 *
 * @author Arik Hadas, Maxence Bernard
 */
class EAWTHandler {

    public EAWTHandler() {
        Desktop desktop = Desktop.getDesktop();
        desktop.setAboutHandler(e -> CoreServiceTracker.getCoreService().showAbout());
        desktop.setPreferencesHandler(e -> CoreServiceTracker.getCoreService().showPreferences());
        desktop.setQuitHandler(this::handleQuitRequestWith);
        desktop.setOpenFileHandler(this::openFiles);
    }

    public void handleQuitRequestWith(final QuitEvent e, final QuitResponse response) {
        if (CoreServiceTracker.getCoreService().doQuit())
            response.performQuit();
        else
            response.cancelQuit();
    }

    public void openFiles(final OpenFilesEvent e) {
        CoreServiceTracker.getCoreService().openFile(e.getFiles().get(0).getAbsolutePath());
    }
}
