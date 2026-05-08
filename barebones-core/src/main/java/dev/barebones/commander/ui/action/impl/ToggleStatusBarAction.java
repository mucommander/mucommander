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
import dev.barebones.commander.text.Translator;
import dev.barebones.commander.ui.action.AbstractActionDescriptor;
import dev.barebones.commander.ui.action.ActionCategory;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.action.MuAction;
import dev.barebones.commander.ui.action.NoIcon;
import dev.barebones.commander.ui.main.MainFrame;
import dev.barebones.commander.ui.main.StatusBar;

/**
 * This action shows/hides the current MainFrame's {@link dev.barebones.commander.ui.main.StatusBar} depending on its current
 * visible state: if it is visible, hides it, if not shows it.
 *
 * <p>
 * This action's label will be updated to reflect the current visible state.
 *
 * <p>
 * Each time this action is executed, the new current visible state is stored in the configuration so that new MainFrame
 * windows will use it to determine whether the StatusBar has to be made visible or not.
 *
 * @author Maxence Bernard
 */
public class ToggleStatusBarAction extends MuAction {

    public ToggleStatusBarAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
        updateLabel(MuConfigurations.getPreferences()
                .getVariable(MuPreference.STATUS_BAR_VISIBLE, MuPreferences.DEFAULT_STATUS_BAR_VISIBLE));
    }

    private void updateLabel(boolean visible) {
        setLabel(Translator.get(visible ? ActionType.ToggleStatusBar + ".hide" : ActionType.ToggleStatusBar + ".show"));
    }

    @Override
    public void performAction() {
        StatusBar statusBar = mainFrame.getStatusBar();
        boolean visible = !statusBar.isVisible();
        // Save the last status bar visible state in the configuration, this will become the default for new MainFrame
        // windows.
        MuConfigurations.getPreferences().setVariable(MuPreference.STATUS_BAR_VISIBLE, visible);
        // Change the label to reflect the new status bar state
        updateLabel(visible);
        // Show/hide the status bar
        statusBar.setVisible(visible);
        mainFrame.getJFrame().validate();
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    @NoIcon
    public static class Descriptor extends AbstractActionDescriptor {
        public String getId() {
            return ActionType.ToggleStatusBar.getId();
        }

        public ActionCategory getCategory() {
            return ActionCategory.VIEW;
        }

        @Override
        public String getLabelKey() {
            return ActionType.ToggleStatusBar + ".show";
        }
    }
}
