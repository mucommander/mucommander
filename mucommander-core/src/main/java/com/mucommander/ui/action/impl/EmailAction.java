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

import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.filter.FileOperationFilter;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.desktop.ActionType;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.InvokesDialog;
import com.mucommander.ui.dialog.file.EmailFilesDialog;
import com.mucommander.ui.main.MainFrame;

/**
 * This action pops up the 'Email files' dialog that allows to email the currently marked files as attachment.
 *
 * @author Maxence Bernard
 */
@InvokesDialog
public class EmailAction extends SelectedFilesAction {

    public EmailAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);

        setSelectedFileFilter(new FileOperationFilter(FileOperation.READ_FILE));
    }

    @Override
    public void performAction(FileSet files) {
        new EmailFilesDialog(mainFrame, files).showDialog();
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Descriptor extends AbstractActionDescriptor {
		public String getId() { return ActionType.Email.toString(); }

		public ActionCategory getCategory() { return ActionCategory.FILES; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }
    }
}
