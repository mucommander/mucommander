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

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.util.FileSet;
import com.mucommander.ui.main.MainFrame;

/**
 * @author Maxence Bernard
 */
public abstract class AbstractCopyDialog extends TransferDestinationDialog {

    public AbstractCopyDialog(MainFrame mainFrame, FileSet files, String title, String labelText, String okText, String errorDialogTitle) {
        super(mainFrame, files, title, labelText, okText, errorDialogTitle);
    }


    //////////////////////////////////////////////////////
    // TransferDestinationDialog partial implementation //
    //////////////////////////////////////////////////////

    protected PathFieldContent computeInitialPath(FileSet files) {
        String fieldText;     // Text to display in the destination field.
        int    startPosition; // Index of the first selected character in the destination field.
        int    endPosition;   // Index of the last selected character in the destination field.
        int    nbFiles = files.size();

        // Fill text field with absolute path, and if there is only one file,
        // append file's name
        fieldText = mainFrame.getInactiveTable().getCurrentFolder().getAbsolutePath(true);
        // Append filename to destination path if there is only one file to copy
        // and if the file is not a directory that already exists in destination
        // (otherwise folder would be copied into the destination folder)
        if(nbFiles==1) {
            AbstractFile file = ((AbstractFile)files.elementAt(0));
            AbstractFile destFile;

            startPosition  = fieldText.length();

            if(!(file.isDirectory() && (destFile= FileFactory.getFile(fieldText+file.getName()))!=null && destFile.exists() && destFile.isDirectory())) {
                endPosition = file.getName().lastIndexOf('.');
                if(endPosition > 0)
                    endPosition += startPosition;
                else
                    endPosition = startPosition + file.getName().length();
                fieldText += file.getName();
            }
            else
                endPosition = fieldText.length();
        }
        else {
            endPosition   = fieldText.length();
            startPosition = 0;
        }

        return new PathFieldContent(fieldText, startPosition, endPosition);
    }
}
