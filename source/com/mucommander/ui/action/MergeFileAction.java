/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

package com.mucommander.ui.action;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.ErrorDialog;
import com.mucommander.ui.dialog.file.MergeFileDialog;
import com.mucommander.ui.main.MainFrame;

import java.util.Hashtable;
import java.util.Iterator;

/**
 * This action invokes the merge file dialog which allows to
 * merge parts of a file.
 *
 * @author Mariusz Jakubowski
 */
public class MergeFileAction extends SelectedFilesAction implements InvokesDialog {
	

    public MergeFileAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
    	FileSet files = mainFrame.getActiveTable().getSelectedFiles();
    	if (files.size()==0)
    		return;
    	for (Iterator iterator = files.iterator(); iterator.hasNext();) {
			AbstractFile file = (AbstractFile) iterator.next();
			if (file.isDirectory() || file.isSymlink()) {
				ErrorDialog.showErrorDialog(mainFrame,
						Translator.get("com.mucommander.ui.action.MergeFileAction.wrong_selection"),
						Translator.get("com.mucommander.ui.action.MergeFileAction.select_files_only")); 

				return;
			}
		}
        AbstractFile destFolder = mainFrame.getInactivePanel().getCurrentFolder();
        new MergeFileDialog(mainFrame, files, destFolder).showDialog();
    }


}
