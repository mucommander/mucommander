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

package com.mucommander.ui.dialog.file;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.ui.main.MainFrame;

/**
 * This abstract class allows to factorize some code among its subclasses.
 *
 * @author Maxence Bernard
 */
public abstract class AbstractCopyDialog extends TransferDestinationDialog {

    public AbstractCopyDialog(MainFrame mainFrame, FileSet files, String title, String labelText, String okText, String errorDialogTitle) {
        super(mainFrame, files, title, labelText, okText, errorDialogTitle, true);
    }

    /**
     * Returns a {@link PathFieldContent} wrapping the given path and a selection corresponding to the filename, with
     * a few subtleties: the file's extension is <b>not</b> selected for regular files or application containers
     * (see {@link DesktopManager#isApplication(AbstractFile)}, but selected for directories.
     * The rationale behind this is that it happens more often that ones wishes to rename a file's name
     * than its extension. This is not the case for directories where the extension is usually an artifact of a
     * filename that contains a '.'.
     *
     * @param file the file to be copied or renamed
     * @param path the destination path
     * @param filenameStart offset to the start of the filename in the given file
     * @return a {@link PathFieldContent} wrapping the given path and a selection corresponding to the filename
     */
    public static PathFieldContent selectDestinationFilename(AbstractFile file, String path, int filenameStart) {
        int endPosition;   // Index of the last selected character

        // If the current file is a directory and not an application file (e.g. Mac OS X .app directory), select
        // the whole file name.
        if(file.isDirectory() && !DesktopManager.isApplication(file)) {
            endPosition = path.length();
        }
        // Otherwise, select the file name without its extension, except when empty ('.DS_Store', for example).
        else {
            endPosition = path.lastIndexOf('.');

            // Text is selected so that user can directly type and replace path
            endPosition = endPosition>filenameStart?endPosition:path.length();
        }

        return new PathFieldContent(path, filenameStart, endPosition);
    }


    //////////////////////////////////////////////////////
    // TransferDestinationDialog partial implementation //
    //////////////////////////////////////////////////////

    @Override
    protected PathFieldContent computeInitialPath(FileSet files) {
        String fieldText;     // Text to display in the destination field.
        int    startPosition; // Index of the first selected character in the destination field.
        int    endPosition;   // Index of the last selected character in the destination field.
        int    nbFiles = files.size();

        // Fill text field with absolute path, and if there is only one file,
        // append file's name
        fieldText = mainFrame.getInactivePanel().getCurrentFolder().getAbsolutePath(true);
        // Append filename to destination path if there is only one file to copy
        // and if the file is not a directory that already exists in destination
        // (otherwise folder would be copied into the destination folder)
        if(nbFiles==1) {
            AbstractFile file = files.elementAt(0);
            AbstractFile destFile;

            startPosition  = fieldText.length();

            if(!(file.isDirectory() && (destFile= FileFactory.getFile(fieldText+file.getName()))!=null && destFile.exists() && destFile.isDirectory())) {
                return selectDestinationFilename(file, fieldText + file.getName(), startPosition);
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
