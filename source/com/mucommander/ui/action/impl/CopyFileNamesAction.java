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

package com.mucommander.ui.action.impl;

import com.mucommander.file.util.FileSet;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.ActionFactory;
import com.mucommander.ui.dnd.ClipboardSupport;
import com.mucommander.ui.dnd.TransferableFileSet;
import com.mucommander.ui.main.MainFrame;

import java.util.Hashtable;


/**
 * This action copies the filename(s) of the currently selected / marked files(s) to the system clipboard.
 *
 * @author Maxence Bernard
 */
public class CopyFileNamesAction extends SelectedFilesAction {

    public CopyFileNamesAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        FileSet selectedFiles = mainFrame.getActiveTable().getSelectedFiles();

        if(selectedFiles.size()>0) {
            // Create a TransferableFileSet and make DataFlavour.stringFlavor (text) the only DataFlavour supported
            TransferableFileSet tfs = new TransferableFileSet(selectedFiles);

            // Disable unwanted data flavors
            tfs.setJavaFileListDataFlavorSupported(false);
            tfs.setTextUriFlavorSupported(false);
            // Note: not disabling this flavor would throw an exception because the flavor data is not serializable
            tfs.setFileSetDataFlavorSupported(false);

            // Transfer filenames, not file paths
            tfs.setStringDataFlavourTransfersFilename(true);

            ClipboardSupport.setClipboardContents(tfs);
        }
    }
    
    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable properties) {
			return new CopyFileNamesAction(mainFrame, properties);
		}
    }
}