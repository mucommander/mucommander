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

package com.mucommander.ui.macos;

import java.awt.Desktop;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitResponse;

import com.mucommander.desktop.macos.CoreServiceTracker;

/**
 * This class registers the About, Preferences and Quit handlers.
 *
 * @author Arik Hadas, Maxence Bernard
 */
class EAWTHandler {

    public EAWTHandler() {
        Desktop desktop = Desktop.getDesktop();
        desktop.setAboutHandler(this::showAbout);
        desktop.setPreferencesHandler(this::showPreferences);
        desktop.setQuitHandler(this::handleQuitRequestWith);
        desktop.setOpenFileHandler(this::openFiles);
    }

    private void showAbout(AboutEvent e) {
        CoreServiceTracker.getCoreService().showAbout();
    }

    private void showPreferences(PreferencesEvent e) {
        CoreServiceTracker.getCoreService().showPreferences();
    }

    private void handleQuitRequestWith(final QuitEvent e, final QuitResponse response) {
        if (CoreServiceTracker.getCoreService().doQuit())
            response.performQuit();
        else
            response.cancelQuit();
    }

    private void openFiles(final OpenFilesEvent e) {
        CoreServiceTracker.getCoreService().openFile(e.getFiles().get(0).getAbsolutePath());
    }
}
