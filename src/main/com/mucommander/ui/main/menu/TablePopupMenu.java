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


package com.mucommander.ui.main.menu;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.popup.MuActionsPopupMenu;

import javax.swing.JSeparator;

/**
 * Contextual popup menu invoked by FileTable when right-clicking on a file or a group of files.
 *
 * The following items are displayed (see constructor code for conditions) :
 *
 * Open
 * Open in new tab
 * Open natively
 * Open with...
 * Rename
 * Reveal in Finder
 * ----
 * Copy name(s)
 * Copy path(s)
 * ----
 * Mark all
 * Unmark all
 * Mark / Unmark
 * ----
 * Delete
 * ----
 * Properties
 * 
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class TablePopupMenu extends MuActionsPopupMenu {

    /**
     * Creates a new TablePopupMenu.
     *
     * @param mainFrame parent MainFrame instance
     * @param currentFolder current folder in table
     * @param clickedFile right-clicked file, can be null if user clicked on the folder table background
     * @param parentFolderClicked true if user right-clicked on the parent '..' folder
     * @param markedFiles list of marked files, can be empty but never null
     */
    public TablePopupMenu(MainFrame mainFrame, AbstractFile currentFolder, AbstractFile clickedFile, boolean parentFolderClicked, FileSet markedFiles) {
        super(mainFrame);
        
        // 'Open ...' actions displayed if a single file was clicked
        if(clickedFile!=null || parentFolderClicked) {
            addAction(com.mucommander.ui.action.impl.OpenAction.Descriptor.ACTION_ID);
            addAction(com.mucommander.ui.action.impl.OpenNativelyAction.Descriptor.ACTION_ID);
            add(new OpenWithMenu(mainFrame));

            addAction(com.mucommander.ui.action.impl.OpenInNewTabAction.Descriptor.ACTION_ID);
        }

        // 'Reveal in desktop' displayed only if clicked file is a local file and the OS is capable of doing this
        if(DesktopManager.canOpenInFileManager(currentFolder))
            addAction(com.mucommander.ui.action.impl.RevealInDesktopAction.Descriptor.ACTION_ID);

        add(new JSeparator());

        // 'Copy name(s)' and 'Copy path(s)' are displayed only if a single file was clicked or files are marked
        if(clickedFile!=null || markedFiles.size()>0) {
            addAction(com.mucommander.ui.action.impl.CopyFilesToClipboardAction.Descriptor.ACTION_ID);
            addAction(com.mucommander.ui.action.impl.CopyFileNamesAction.Descriptor.ACTION_ID);
            addAction(com.mucommander.ui.action.impl.CopyFileBaseNamesAction.Descriptor.ACTION_ID);
            addAction(com.mucommander.ui.action.impl.CopyFilePathsAction.Descriptor.ACTION_ID);
            
            add(new JSeparator());
        }

        // Those following items are displayed in all cases
        addAction(com.mucommander.ui.action.impl.MarkAllAction.Descriptor.ACTION_ID);
        addAction(com.mucommander.ui.action.impl.UnmarkAllAction.Descriptor.ACTION_ID);
        addAction(com.mucommander.ui.action.impl.MarkSelectedFileAction.Descriptor.ACTION_ID);

        add(new JSeparator());

        // 'Rename' displayed if a single file was clicked
        if(clickedFile!=null)
            addAction(com.mucommander.ui.action.impl.RenameAction.Descriptor.ACTION_ID);

        addAction(com.mucommander.ui.action.impl.DeleteAction.Descriptor.ACTION_ID);

        add(new JSeparator());

        addAction(com.mucommander.ui.action.impl.ShowFilePropertiesAction.Descriptor.ACTION_ID);
        addAction(com.mucommander.ui.action.impl.ChangePermissionsAction.Descriptor.ACTION_ID);
        addAction(com.mucommander.ui.action.impl.ChangeDateAction.Descriptor.ACTION_ID);
    }
}
