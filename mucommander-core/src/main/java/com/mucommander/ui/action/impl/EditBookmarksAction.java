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

import com.mucommander.desktop.ActionType;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.InvokesDialog;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.dialog.bookmark.EditBookmarksDialog;
import com.mucommander.ui.main.MainFrame;


/**
 * This action brings up the 'Edit bookmarks' dialog that allows to edit bookmarks.
 *
 * @author Maxence Bernard
 */
@InvokesDialog
public class EditBookmarksAction extends MuAction {

    public EditBookmarksAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        new EditBookmarksDialog(mainFrame);
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Descriptor extends AbstractActionDescriptor {
		public String getId() { return ActionType.EditBookmarks.getId(); }

		public ActionCategory getCategory() { return ActionCategory.NAVIGATION; }
    }
}
