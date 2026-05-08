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

package dev.barebones.commander.ui.action.impl;

import java.util.Map;

import dev.barebones.commander.conf.MuConfigurations;
import dev.barebones.commander.conf.MuPreference;
import dev.barebones.commander.conf.MuPreferences;
import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.ui.action.AbstractActionDescriptor;
import dev.barebones.commander.ui.action.ActionCategory;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.action.MuAction;
import dev.barebones.commander.ui.action.NoIcon;
import dev.barebones.commander.ui.main.MainFrame;
import dev.barebones.commander.ui.main.WindowManager;

/**
 * A simple action that toggles hidden files visibility on and off.
 * 
 * @author Nicolas Rinaudo
 */
public class ToggleHiddenFilesAction extends MuAction {
    // - Initialization ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>ToggleHiddenFilesAction</code>.
     */
    public ToggleHiddenFilesAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    // - Action code ---------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Toggles hidden files display on and off and requests for all file tables to be repainted.
     */
    @Override
    public void performAction() {
        MuConfigurations.getPreferences()
                .setVariable(MuPreference.SHOW_HIDDEN_FILES,
                        !MuConfigurations.getPreferences()
                                .getVariable(MuPreference.SHOW_HIDDEN_FILES, MuPreferences.DEFAULT_SHOW_HIDDEN_FILES));
        WindowManager.tryRefreshCurrentFolders();
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    @NoIcon
    public static class Descriptor extends AbstractActionDescriptor {
        public String getId() {
            return ActionType.ToggleHiddenFiles.getId();
        }

        public ActionCategory getCategory() {
            return ActionCategory.VIEW;
        }
    }
}
