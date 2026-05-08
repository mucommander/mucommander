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

import dev.barebones.commander.core.desktop.DesktopManager;
import dev.barebones.commander.desktop.AbstractTrash;
import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.ui.action.AbstractActionDescriptor;
import dev.barebones.commander.ui.action.ActionCategory;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.action.MuAction;
import dev.barebones.commander.ui.action.NoIcon;
import dev.barebones.commander.ui.main.MainFrame;

/**
 * Empties the system trash. This action is enabled only if the current platform has an
 * {@link dev.barebones.commander.desktop.AbstractTrash} implementation and if it is capable of emptying the trash, as reported
 * by {@link dev.barebones.commander.desktop.AbstractTrash#canEmpty()}.
 *
 * @author Maxence Bernard
 */
public class EmptyTrashAction extends MuAction {

    public EmptyTrashAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);

        AbstractTrash trash = DesktopManager.getTrash();
        setEnabled(trash != null && trash.canEmpty());
    }

    @Override
    public void performAction() {
        DesktopManager.getTrash().empty();
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    @NoIcon
    public static class Descriptor extends AbstractActionDescriptor {
        public String getId() {
            return ActionType.EmptyTrash.getId();
        }

        public ActionCategory getCategory() {
            return ActionCategory.FILES;
        }
    }
}
