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

import javax.swing.KeyStroke;

import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.filter.AndFileFilter;
import com.mucommander.commons.file.filter.AttributeFileFilter;
import com.mucommander.commons.file.filter.AttributeFileFilter.FileAttribute;
import com.mucommander.commons.file.filter.FileOperationFilter;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.InvokesDialog;
import com.mucommander.ui.dialog.file.SplitFileDialog;
import com.mucommander.ui.main.MainFrame;

/**
 * This action invokes the split file dialog which allows to split the selected file into several parts.
 *
 * @author Mariusz Jakubowski
 */
@InvokesDialog
public class SplitFileAction extends SelectedFileAction {

    public SplitFileAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);

        setSelectedFileFilter(new AndFileFilter(
            new AttributeFileFilter(FileAttribute.DIRECTORY, true),
            new FileOperationFilter(FileOperation.READ_FILE)
        ));
    }

    @Override
    public void performAction() {
        new SplitFileDialog(mainFrame,
                mainFrame.getActiveTable().getSelectedFile(),
                mainFrame.getInactivePanel().getCurrentFolder()
        ).showDialog();
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "SplitFile";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return null; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return null; }
    }
}
