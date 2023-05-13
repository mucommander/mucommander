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


package com.mucommander.ui.main.menu;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.protocol.search.SearchFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.desktop.ActionType;
import com.mucommander.ui.dnd.ClipboardNotifier;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.popup.MuActionsPopupMenu;

import javax.swing.JSeparator;

/**
 * Contextual popup menu invoked by FileTable when right-clicking on a file or a group of files.
 * <p>
 * The following items are displayed (see constructor code for conditions) :
 * <p>
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
     * @param mainFrame           parent MainFrame instance
     * @param currentFolder       current folder in table
     * @param clickedFile         right-clicked file, can be null if user clicked on the folder table background
     * @param parentFolderClicked true if user right-clicked on the parent '..' folder
     * @param markedFiles         list of marked files, can be empty but never null
     */
    public TablePopupMenu(MainFrame mainFrame, AbstractFile currentFolder, AbstractFile clickedFile, boolean parentFolderClicked, FileSet markedFiles) {
        super(mainFrame);

        // 'Open ...' actions displayed if a single file was clicked
        if (clickedFile != null || parentFolderClicked) {
            addAction(ActionType.Open);
            addAction(ActionType.OpenNatively);
            add(new OpenWithMenu(mainFrame, clickedFile));
            if (clickedFile != null && !clickedFile.isDirectory()) {
                add(new OpenAsMenu(mainFrame));
            }

            addAction(ActionType.OpenInNewTab);
            if (SearchFile.SCHEMA.equals(currentFolder.getURL().getScheme()))
                addAction(ActionType.ShowInEnclosingFolder);
            add(new JSeparator());
        }

        boolean copyOrPasteActionAdded = false;
        // 'Copy name(s)' and 'Copy path(s)' are displayed only if a single file was clicked or files are marked
        if (clickedFile != null || markedFiles.size() > 0) {
            addAction(ActionType.CopyFilesToClipboard);
            addAction(ActionType.CopyFileNames);
            addAction(ActionType.CopyFileBaseNames);
            addAction(ActionType.CopyFilePaths);
            copyOrPasteActionAdded = true;
        }

        if (ClipboardNotifier.isPasteClipboardFilesActionEnabled()) {
            addAction(ActionType.PasteClipboardFiles);
            copyOrPasteActionAdded = true;
        }

        if (copyOrPasteActionAdded)
            add(new JSeparator());

        // Those following items are displayed in all cases
        addAction(ActionType.MarkAll);
        addAction(ActionType.UnmarkAll);
        addAction(ActionType.MarkSelectedFile);

        add(new JSeparator());

        addAction(ActionType.Refresh);
        // 'Reveal in desktop' displayed only if clicked file is a local file and the OS is capable of doing this,
        // if clicked file is null, then it was clicked either on background (current folder) or on '..'
        if (DesktopManager.canOpenInFileManager(clickedFile != null ? clickedFile : currentFolder)) {
            addAction(ActionType.RevealInDesktop);
        }

        // 'Rename' displayed if a single file was clicked
        if (clickedFile != null) {
            add(new JSeparator());
            addAction(ActionType.Rename);

            addAction(ActionType.Delete);

            add(new JSeparator());

            addAction(ActionType.ShowFileProperties);
            addAction(ActionType.ChangePermissions);
            addAction(ActionType.ChangeDate);
        }
    }
}
