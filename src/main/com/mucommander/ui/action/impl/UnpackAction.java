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

import com.mucommander.commons.file.filter.AttributeFileFilter;
import com.mucommander.commons.file.filter.AttributeFileFilter.FileAttribute;
import com.mucommander.commons.file.filter.OrFileFilter;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategories;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.ActionFactory;
import com.mucommander.ui.action.InvokesDialog;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.dialog.file.UnpackDialog;
import com.mucommander.ui.main.MainFrame;

/**
 * This action pops up the 'Unpack files' dialog that allows to unpack the currently marked files.
 *
 * @author Maxence Bernard
 */
@InvokesDialog
public class UnpackAction extends SelectedFilesAction {

    public UnpackAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);

        // Unpack job operates on archives and directories
        setSelectedFileFilter(new OrFileFilter(
            new AttributeFileFilter(FileAttribute.ARCHIVE),
            new AttributeFileFilter(FileAttribute.DIRECTORY)
        ));
    }

    @Override
    public void performAction(FileSet files) {
        new UnpackDialog(mainFrame, files).showDialog();
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
			return new UnpackAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "Unpack";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.FILES; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK); }
    }
}
