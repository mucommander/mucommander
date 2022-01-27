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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.dialog.file.MkdirDialog;
import com.mucommander.ui.main.MainFrame;

/**
 * This action brings up the 'Make directory' dialog which allows to create a new directory in the currently active folder.
 *
 * @author Maxence Bernard
 */
public class MkdirAction extends ParentFolderAction {

    public MkdirAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    protected void toggleEnabledState() {
        AbstractFile firstFile = mainFrame.getActiveTable().getFileTableModel().getFileAt(0);

        // If there is no file at all, do not rely on the action being supported by the current folder as this
        // would be incorrect for some filesystems which do not support operations consistently across the
        // filesystem (e.g. S3). In that case, err on the safe side and enable the action, even if the operation
        // end up not being supported.
        setEnabled(firstFile==null || firstFile.isFileOperationSupported(FileOperation.CREATE_DIRECTORY));
    }

    @Override
    public void performAction() {
        new MkdirDialog(mainFrame, false).showDialog();
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    ///////////////////
    // Inner classes //
    ///////////////////

    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "Mkdir";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategory.FILES; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0); }
    }
}
