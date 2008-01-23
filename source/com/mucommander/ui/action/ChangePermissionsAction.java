/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

import com.mucommander.file.util.FileSet;
import com.mucommander.ui.dialog.file.ChangePermissionsDialog;
import com.mucommander.ui.main.MainFrame;

import java.util.Hashtable;

/**
 * Brings up a dialog that allows the user to change the file permissions the currently selected/marked files.
 * 
 * @author Maxence Bernard
 */
public class ChangePermissionsAction extends SelectedFilesAction implements InvokesDialog {

    public ChangePermissionsAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }


    public void performAction() {
        FileSet files = mainFrame.getActiveTable().getSelectedFiles();

        // Display dialog only if at least one file is selected/marked
        if(files.size()>0)
            new ChangePermissionsDialog(mainFrame, files).showDialog();        
    }
}
