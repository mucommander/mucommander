/**
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.desktop.ActionType;
import com.mucommander.os.api.CoreService;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.dialog.about.AboutDialog;
import com.mucommander.ui.dialog.shutdown.QuitDialog;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;

/**
 * Implementation of CoreService for JPMS service loading.
 *
 * @author Arik Hadas
 */
public class CoreServiceImpl implements CoreService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoreServiceImpl.class);

    @Override
    public void showAbout() {
        MainFrame mainFrame = WindowManager.getCurrentMainFrame();

        // Do nothing (return) when in 'no events mode'
        if(mainFrame.getNoEventsMode())
            return;

        new AboutDialog(mainFrame).showDialog();
    }

    @Override
    public void showPreferences() {
        MainFrame mainFrame = WindowManager.getCurrentMainFrame();

        // Do nothing (return) when in 'no events mode'
        if(mainFrame.getNoEventsMode())
            return;

        ActionManager.performAction(ActionType.ShowPreferences, mainFrame);
    }

    @Override
    public boolean doQuit() {
        // Ask the user for confirmation and abort if user refused to quit.
        if(!QuitDialog.confirmQuit())
            return false;

        // We got a green -> quit!
        Application.initiateShutdown();

        return true;
    }

    @Override
    public void openFile(String path) {
        // Wait until the application has been launched. This step is required to properly handle the case where the
        // application is launched with a file to open, for instance when drag-n-dropping a file to the Dock icon
        // when muCommander is not started yet. In this case, this method is called while Launcher is still busy
        // launching the application (no mainframe exists yet).
        Application.waitUntilLaunched();

        AbstractFile file = FileFactory.getFile(path);
        FolderPanel activePanel = WindowManager.getCurrentMainFrame().getActivePanel();
        if (file == null) {
            LOGGER.error("Ignoring open file, as File is null for path: {}.", path);
            return;
        }
        if (file.isBrowsable()) {
            activePanel.tryChangeCurrentFolder(file);
        } else {
            activePanel.tryChangeCurrentFolder(file.getParent(), file, false);
        }
    }
}
