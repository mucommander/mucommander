/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

import com.mucommander.desktop.AbstractTrash;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.ui.action.*;
import com.mucommander.ui.main.MainFrame;

import javax.swing.KeyStroke;
import java.util.Hashtable;

/**
 * Empties the system trash. This action is enabled only if the current platform has an
 * {@link com.mucommander.desktop.AbstractTrash} implementation and if it is capable of emptying the trash,
 * as reported by {@link com.mucommander.desktop.AbstractTrash#canEmpty()}.
 *
 * @author Maxence Bernard
 */
public class EmptyTrashAction extends MuAction {

    public EmptyTrashAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
        super(mainFrame, properties);

        AbstractTrash trash = DesktopManager.getTrash();
        setEnabled(trash!=null && trash.canEmpty());
    }

    @Override
    public void performAction() {
        DesktopManager.getTrash().empty();
    }
    
    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
			return new EmptyTrashAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "EmptyTrash";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.FILES; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return null; }
    }
}
