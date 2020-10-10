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

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.desktop.macos.CoreServiceTracker;

/**
 * This class registers the About, Preferences and Quit handlers.
 *
 * @author Arik Hadas, Maxence Bernard
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

    @Override
    public void handleAbout(ApplicationEvent event) {
        CoreServiceTracker.getCoreService().showAbout();
        event.setHandled(true);
    }

    @Override
    public void handleOpenApplication(ApplicationEvent event) {
    }

    @Override
    public void handleOpenFile(ApplicationEvent event) {
        AbstractFile file = FileFactory.getFile(event.getFilename());
        CoreServiceTracker.getCoreService().openFile(file.getAbsolutePath());
        event.setHandled(true);
    }

    @Override
    public void handlePreferences(ApplicationEvent event) {
        CoreServiceTracker.getCoreService().showPreferences();
        event.setHandled(true);
    }

    @Override
    public void handlePrintFile(ApplicationEvent event) {
    }

    @Override
    public void handleQuit(ApplicationEvent event) {
        event.setHandled(CoreServiceTracker.getCoreService().doQuit());
    }

    @Override
    public void handleReOpenApplication(ApplicationEvent event) {
    }
}
