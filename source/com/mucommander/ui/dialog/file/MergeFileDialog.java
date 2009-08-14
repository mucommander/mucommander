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

package com.mucommander.ui.dialog.file;

import com.mucommander.AppLogger;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.filter.*;
import com.mucommander.file.util.FileSet;
import com.mucommander.file.util.PathUtils;
import com.mucommander.file.util.PathUtils.ResolvedDestination;
import com.mucommander.job.MergeFileJob;
import com.mucommander.job.TransferFileJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;

import java.io.IOException;

/**
 * Dialog used to merge parts of a file.
 * 
 * @author Mariusz Jakubowski
 */
public class MergeFileDialog extends TransferDestinationDialog {

    private AbstractFile destFolder;

    /**
     * Creates a new combine file dialog.
     * @param mainFrame the main frame
     * @param files a list of files to combine
     * @param destFolder default destination folder
     */
    public MergeFileDialog(MainFrame mainFrame, FileSet files, AbstractFile destFolder) {
        super(mainFrame, files, 
        		Translator.get("merge_file_dialog.title"),
                Translator.get("copy_dialog.destination"),
                Translator.get("merge_file_dialog.merge"),
                Translator.get("copy_dialog.error_title"));

        this.destFolder = destFolder;
    }

    /**
     * Searches for parts of a file.  
     * @param part1 first part of a file
     */
    private void searchParts(AbstractFile part1) {
		AbstractFile parent = part1.getParent();
		if (parent == null) {
			return;
		}
		String ext = part1.getExtension();
		int firstIndex;
		try {
			firstIndex = Integer.parseInt(ext);
		} catch (NumberFormatException e) {
			return;
		}
		String name = part1.getNameWithoutExtension();
		FilenameFilter startsFilter = new StartsFilenameFilter(name, false);
		AttributeFileFilter filesFilter = new AttributeFileFilter(AttributeFileFilter.FILE);
		EqualsFilenameFilter part1Filter = new EqualsFilenameFilter(part1.getName(), false);
		part1Filter.setInverted(true);
		AndFileFilter filter = new AndFileFilter();
		filter.addFileFilter(startsFilter);
		filter.addFileFilter(filesFilter);
		filter.addFileFilter(part1Filter);
		try {
			AbstractFile[] otherParts = parent.ls(filter);
			for (int i = 0; i < otherParts.length; i++) {
				String ext2 = otherParts[i].getExtension();
				try {
					int partIdx = Integer.parseInt(ext2);
					if (partIdx > firstIndex)
						files.add(otherParts[i]);
				} catch (NumberFormatException e) {
					// nothing
				}
			}
		} catch (IOException e) {
            AppLogger.fine("Caught exception", e);
		}
		setFiles(files);
	}

    protected boolean isValidDestination(PathUtils.ResolvedDestination resolvedDest, String destPath) {
        // The path entered doesn't correspond to any existing folder
        if (resolvedDest==null) {
            showErrorDialog(Translator.get("invalid_path", destPath), errorDialogTitle);
            return false;
        }
        return true;
	}


    //////////////////////////////////////////////
    // TransferDestinationDialog implementation //
    //////////////////////////////////////////////

    protected PathFieldContent computeInitialPath(FileSet files) {
        String path = destFolder.getAbsolutePath(true) + files.fileAt(0).getNameWithoutExtension();
        if (files.size() == 1) {
        	searchParts(files.fileAt(0));
        }

        return new PathFieldContent(path);
    }

    protected TransferFileJob createTransferFileJob(ProgressDialog progressDialog, ResolvedDestination resolvedDest, int defaultFileExistsAction) {
		return new MergeFileJob(progressDialog, mainFrame,
		       files, resolvedDest.getDestinationFile(), defaultFileExistsAction);
	}

    protected String getProgressDialogTitle() {
        return Translator.get("progress_dialog.processing_files");
    }

}
