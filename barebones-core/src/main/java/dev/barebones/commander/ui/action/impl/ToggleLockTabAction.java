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

import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.text.Translator;
import dev.barebones.commander.ui.action.AbstractActionDescriptor;
import dev.barebones.commander.ui.action.ActionCategory;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.action.NoIcon;
import dev.barebones.commander.ui.main.MainFrame;

/**
 * This action locks/unlocks the currently selected {@link dev.barebones.commander.ui.main.tabs.FileTableTab} depending on its
 * current locking state: if it is locked, unlock it, if not lock it.
 *
 * <p>
 * This action's label will be updated to reflect the locking state of the currently selected tab.
 *
 * @author Arik Hadas
 */
public class ToggleLockTabAction extends ActiveTabAction {

    public ToggleLockTabAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    private void updateLabel(boolean locked) {
        setLabel(Translator.get(locked ? ActionType.ToggleLockTab + ".unlock" : ActionType.ToggleLockTab + ".lock"));
    }

    @Override
    public void performAction() {
        boolean lock = !mainFrame.getActivePanel().getTabs().getCurrentTab().isLocked();

        if (lock)
            mainFrame.getActivePanel().getTabs().lock();
        else
            mainFrame.getActivePanel().getTabs().unlock();

        // Change the label to reflect the new tab's locking state
        updateLabel(lock);
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    @Override
    protected void toggleEnabledState() {
        updateLabel(mainFrame.getActivePanel().getTabs().getCurrentTab().isLocked());
    }

    @NoIcon
    public static class Descriptor extends AbstractActionDescriptor {
        public String getId() {
            return ActionType.ToggleLockTab.getId();
        }

        public ActionCategory getCategory() {
            return ActionCategory.TAB;
        }
    }
}
