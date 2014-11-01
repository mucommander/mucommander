/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import com.mucommander.command.Command;
import com.mucommander.command.CommandManager;
import com.mucommander.ui.action.*;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * User configurable variant of {@link InternalEditAction}.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class EditAction extends InternalEditAction {
    // - Initialization ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new instance of <code>EditAction</code>.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public EditAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);
    }



    // - AbstractViewerAction implementation ---------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    @Override
    protected Command getCustomCommand() {
        return CommandManager.getCommandForAlias(CommandManager.EDITOR_ALIAS);
    }



    // - Factory -------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    public static class Factory implements ActionFactory {
		public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
			return new EditAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "Edit";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.FILES; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0); }
    }
}
