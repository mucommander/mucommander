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

package com.mucommander.ui.action.impl;

import java.util.Map;

import com.mucommander.command.Command;
import com.mucommander.command.CommandManager;
import com.mucommander.desktop.ActionType;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.main.MainFrame;

/**
 * User configurable variant of {@link InternalViewAction}.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class ViewAction extends InternalViewAction {
    // - Initialization ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new instance of <code>ViewAction</code>.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public ViewAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);
    }


    // - AbstractViewerAction implementation ---------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    @Override
    protected Command getCustomCommand() {
        return CommandManager.getCommandForAlias(CommandManager.VIEWER_ALIAS);
    }

    public static class Descriptor extends AbstractActionDescriptor {
		public String getId() { return ActionType.View.getId(); }

		public ActionCategory getCategory() { return ActionCategory.FILES; }
    }
}
