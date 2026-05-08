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

package dev.barebones.commander.ui.main.tabs;

import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.ui.action.impl.CloneTabToOtherPanelAction;
import dev.barebones.commander.ui.action.impl.CloseDuplicateTabsAction;
import dev.barebones.commander.ui.action.impl.CloseOtherTabsAction;
import dev.barebones.commander.ui.action.impl.CloseTabAction;
import dev.barebones.commander.ui.action.impl.DuplicateTabAction;
import dev.barebones.commander.ui.action.impl.MoveTabToOtherPanelAction;
import dev.barebones.commander.ui.action.impl.SetTabTitleAction;
import dev.barebones.commander.ui.action.impl.ToggleLockTabAction;
import dev.barebones.commander.ui.main.MainFrame;
import dev.barebones.commander.ui.popup.MuActionsPopupMenu;

/**
* Contextual popup menu invoked by {@link FileTableTabbedPane} when right-clicking on a tab's title.
* 
* @author Arik Hadas
*/
class FileTableTabPopupMenu extends MuActionsPopupMenu {

	public FileTableTabPopupMenu(MainFrame mainFrame) {
		super(mainFrame);
		addAction(ActionType.DuplicateTab);
		addAction(ActionType.CloseTab);
		addAction(ActionType.CloseOtherTabs);
		addAction(ActionType.CloseDuplicateTabs);
		add(new Separator());
		addAction(ActionType.ToggleLockTab);
		addAction(ActionType.SetTabTitle);
		add(new Separator());
		addAction(ActionType.MoveTabToOtherPanel);
		addAction(ActionType.CloneTabToOtherPanel);
	}
}
