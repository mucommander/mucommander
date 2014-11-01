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

import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.KeyStroke;

import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategories;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.ActionFactory;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;

/**
 * This action changes the current folder of the currently active {@link com.mucommander.ui.main.FolderPanel} to
 * <code>bookmark://</code> which is the root of bookmark filesystem, allowing to explore all the bookmarks the user has.
 *
 * @author Nicolas Rinaudo
 */
public class ExploreBookmarksAction extends ActiveTabAction {

    public ExploreBookmarksAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        mainFrame.getActivePanel().tryChangeCurrentFolder("bookmark://");
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    /**
     * Enables or disables this action based on the currently active folder's
     * current tab is not locked, this action will be enabled,
     * if not it will be disabled.
     */
    @Override
    protected void toggleEnabledState() {
        setEnabled(!mainFrame.getActivePanel().getTabs().getCurrentTab().isLocked());
    }
    
    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
			return new ExploreBookmarksAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "ExploreBookmarks";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.NAVIGATION; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK); }
    }
}
