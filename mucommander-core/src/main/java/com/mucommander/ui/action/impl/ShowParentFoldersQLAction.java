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

import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.KeyStroke;

import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.QuickLists;

/**
 * This action shows ParentFoldersQL on the current active FileTable.
 * 
 * @author Arik Hadas
 */

public class ShowParentFoldersQLAction extends ShowQuickListAction {
	
	public ShowParentFoldersQLAction(MainFrame mainFrame, Map<String,Object> properties) {
		super(mainFrame, properties);
	}
	
	@Override
    public void performAction() {
		openQuickList(QuickLists.PARENT_FOLDERS);
	}

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

	public static class Descriptor extends AbstractActionDescriptor {
		public static final String ACTION_ID = "ShowParentFoldersQL";
		
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategory.NAVIGATION; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK); }
    }
}
