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

package com.mucommander.ui.dnd;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.InputEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.job.impl.CopyJob;
import com.mucommander.job.impl.MoveJob;
import com.mucommander.job.impl.CopyJob.TransferMode;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.FileCollisionDialog;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

/**
 * Provides file(s) 'drop' support to components that add a <code>DropTarget</code> using this
 * <code>DropTargetListener</code>. A {@link com.mucommander.ui.main.FolderPanel} instance has to be specified at
 * creation time, this instance will be used to change the current folder, or copy/move files to the current folder.
 *
 * <p>
 * There are 2 different modes this class can operate in. The mode to be used has to be specified when this class is
 * instantiated.
 *
 * <p>
 * In 'folder change mode', when a file or string representing a file path is dropped, the associated FolderPanel's
 * current folder is changed:
 * <ul>
 * <li>If the file is a directory, the current folder is changed to that directory
 * <li>For any other file kind (archive, regular file...), current folder is changed to the file's parent folder and the
 * file is selected
 * </ul>
 * If more than one file (or file path) is dropped, only the first one is taken into account.
 *
 * <p>
 * In the normal mode, files (or file paths) that are dropped can also be moved or copied to the associated
 * FolderPanel's current folder, on top of the change current folder action. The actual drop action performed (move,
 * copy or change current folder) depends on the keyboard modifiers typed by the user when dragging the files. When the
 * mouse cursor enters the drop-enabled component's area, it is changed to symbolize the action to be performed. The
 * default drop action (when no modifier is down) is copy.
 *
 * <p>
 * Drop events originating from the same FolderPanel are on purpose not accepted as spring-loaded folders are not (yet)
 * supported which would make the drop operation ambiguous and confusing.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class FileDropTargetListener implements DropTargetListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileDropTargetListener.class);

    /** the FolderPanel instance used to change the current folder when a file is dropped */
    private FolderPanel folderPanel;

    /** Mode that specifies what to do when files are dropped */
    private boolean changeFolderOnlyMode;

    /** Drop action (copy or move) currently specified by the user */
    private int currentDropAction;

    /** Has DropTargetDragEvent event been accepted ? */
    private boolean dragAccepted;

    /**
     * Extended modifiers which must be down while dragging for the drop action to be a MOVE and not a COPY (default):
     * <code>InputEvent.META_DOWN_MASK</code> under Mac OS X, <code>InputEvent.ALT_DOWN_MASK</code> under any other
     * platform.
     */
    private final static int MOVE_ACTION_MODIFIERS_EX = OsFamily.MAC_OS.isCurrent() ? InputEvent.META_DOWN_MASK
            : InputEvent.ALT_DOWN_MASK;

    /**
     * Creates a new FileDropTargetListener using the provided FolderPanel that will be used to either change the
     * current folder or copy/move when files are dropped, depending on the specified operating mode and drop action.
     *
     * @param folderPanel
     *            the FolderPanel instance used to change the current folder or copy/move when files are dropped
     * @param changeFolderOnlyMode
     *            if <code>true</code>, the FolderPanel's current folder can only be changed when file(s) are dropped,
     *            files cannot be copied or moved.
     */
    public FileDropTargetListener(FolderPanel folderPanel, boolean changeFolderOnlyMode) {
        this.folderPanel = folderPanel;
        this.changeFolderOnlyMode = changeFolderOnlyMode;
    }

    /**
     * Returns a mouse <code>Cursor<code> that symbolizes the given drop action and 'accepted' status. The given action
     * must one of the following:
     * <ul>
     * <li>DnDConstants.ACTION_COPY
     * <li>DnDConstants.ACTION_MOVE
     * <li>DnDConstants.ACTION_LINK
     * </ul>
     * If the action has any other value, the default Cursor is returned.
     */
    private Cursor getDragActionCursor(int dropAction, boolean dragAccepted) {
        switch (dropAction) {
        case DnDConstants.ACTION_COPY:
            return dragAccepted ? DragSource.DefaultCopyDrop : DragSource.DefaultCopyNoDrop;

        case DnDConstants.ACTION_MOVE:
            return dragAccepted ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop;

        case DnDConstants.ACTION_LINK:
            return dragAccepted ? DragSource.DefaultLinkDrop : DragSource.DefaultLinkNoDrop;

        default:
            return Cursor.getDefaultCursor();
        }
    }

    /**
     * Accepts or rejects the specified <code>DropTargetDragEvent</code> and changes the mouse cursor to match the
     * current drop action. The drag event will be accepted it supports at least one of the supported DataFlavors and
     * one of the two following conditions are true:
     * <ul>
     * <li>the event originates from one of muCommander's {@link FolderPanel} for which the current folder is not the
     * same as the FolderPanel associated with this <code>FileDropTargetListener</code>
     * <li>the event does not originate from muCommander
     * </ul>
     *
     * <p>
     * This method overrides the default drop action for drag-and-drop operations within muCommander to make it
     * <code>DnDConstants.ACTION_COPY</code> instead of <code>DnDConstants.ACTION_MOVE</code>. For a move action to be
     * performed when the mouse is released, the modifiers defined by {@link #MOVE_ACTION_MODIFIERS_EX} must be down.
     * </p>
     *
     * @return <code>true</code> if the event was accepted, false otherwise
     */
    private boolean acceptOrRejectDragEvent(DropTargetDragEvent event) {
        this.dragAccepted = isDragAccepted(event);

        if (dragAccepted) {
            this.currentDropAction = determineDropAction(event);
            // Accept the drag event with our drop action
            event.acceptDrag(currentDropAction);
            LOGGER.trace("drag accepted, dropAction=" + currentDropAction);
        } else {
            // Reject the drag event
            event.rejectDrag();
            LOGGER.trace("drag rejected");
        }

        // Change the mouse cursor on this FolderPanel and child components
        Cursor newCursor = getDragActionCursor(currentDropAction, dragAccepted);
        LOGGER.trace("cursor=" + newCursor);
        folderPanel.setCursor(newCursor);

        return dragAccepted;
    }

    private int determineDropAction(DropTargetDragEvent event) {
        int dropAction = event.getDropAction();

        boolean changeDefaultDropAction = MuConfigurations.getPreferences().getVariable(MuPreference.SET_DROP_ACTION_TO_COPY, MuPreferences.DEFAULT_SET_DROP_ACTION_TO_COPY);
        if (!changeDefaultDropAction)
            return dropAction;

        if (DnDContext.isDragInitiatedByMucommander()) {
            // Change the default drop action to DnDConstants.ACTION_COPY instead of DnDConstants.ACTION_MOVE,
            // if the move extended modifiers are not currently down.
            int dragModifiers = DnDContext.getDragGestureModifiersEx();

            if (dropAction == DnDConstants.ACTION_MOVE
                    && (dragModifiers & MOVE_ACTION_MODIFIERS_EX) == 0
                    && (event.getSourceActions() & DnDConstants.ACTION_COPY) != 0) {
                LOGGER.debug("changing default action, was: DnDConstants.ACTION_MOVE, now: DnDConstants.ACTION_COPY");
                dropAction = DnDConstants.ACTION_COPY;
            }
        } else {
            switch(dropAction) {
            case DnDConstants.ACTION_MOVE:
                if ((event.getSourceActions() & DnDConstants.ACTION_COPY) != 0) {
                    dropAction = DnDConstants.ACTION_COPY;
                    LOGGER.debug("changing default external action, was: DnDConstants.ACTION_MOVE, now: DnDConstants.ACTION_COPY");
                }
                break;
            case DnDConstants.ACTION_COPY:
                if ((event.getSourceActions() & DnDConstants.ACTION_MOVE) != 0) {
                    dropAction = DnDConstants.ACTION_MOVE;
                    LOGGER.debug("changing default external action, was: DnDConstants.ACTION_COPY, now: DnDConstants.ACTION_MOVE");
                }
                break;
            }
        }
        return dropAction;
    }

    private boolean isDragAccepted(DropTargetDragEvent event) {
        boolean dataFlavorSupported = event.isDataFlavorSupported(TransferableFileSet.getFileSetDataFlavor())
                || event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                || event.isDataFlavorSupported(DataFlavor.getTextPlainUnicodeFlavor());

        if (!dataFlavorSupported)
            return false;

        // Refuse drag if the drag was initiated by the same FolderPanel, or if its current folder is the same
        // as this one
        if (DnDContext.isDragInitiatedByMucommander() && isPointToTargetFolder(DnDContext.getDragInitiator()))
            return false;

        return true;
    }

    private boolean isPointToTargetFolder(FolderPanel dragInitiator) {
        return dragInitiator == folderPanel
                || dragInitiator.getCurrentFolder().equalsCanonical(folderPanel.getCurrentFolder());
    }

    ////////////////////////////////////
    // FileDropTargetListener methods //
    ////////////////////////////////////

    public void dragEnter(DropTargetDragEvent event) {
        acceptOrRejectDragEvent(event);
    }

    public void dragOver(DropTargetDragEvent event) {
        // Although it doesn't look necessary, cursor needs to be set each time this method is called otherwise
        // it returns to the default one (at least under Mac OS X w/ Java 1.5)
        acceptOrRejectDragEvent(event);
    }

    public void dropActionChanged(DropTargetDragEvent event) {
        acceptOrRejectDragEvent(event);
    }

    public void dragExit(DropTargetEvent event) {
        // Restore default cursor
        folderPanel.setCursor(Cursor.getDefaultCursor());
    }

    public void drop(DropTargetDropEvent event) {
        // Restore default cursor, no matter what
        folderPanel.setCursor(Cursor.getDefaultCursor());

        // The drop() method is called even if a DropTargetDropEvent was rejected before,
        // so this test is really necessary
        if (!dragAccepted) {
            event.rejectDrop();
            return;
        }

        // Accept drop event
        event.acceptDrop(currentDropAction);

        // Retrieve the files contained by the transferable as a FileSet (takes care of handling the different
        // DataFlavors)
        FileSet droppedFiles = TransferableFileSet.getTransferFiles(event.getTransferable());

        // Stop and report failure if no file could not be retrieved
        if (droppedFiles == null || droppedFiles.size() == 0) {
            // Report drop failure
            event.dropComplete(false);

            return;
        }

        // If in 'change folder mode' or if the drop action is 'ACTION_LINK' in normal mode:
        // change the FolderPanel's current folder to the dropped file/folder :
        // - If the file is a directory, the current folder is changed to that directory
        // - For any other file kind (archive, regular file...), current folder is changed to the file's parent folder
        // and the file is selected
        // If more than one file is dropped, only the first one is used
        if (changeFolderOnlyMode || currentDropAction == DnDConstants.ACTION_LINK) {
            AbstractFile file = droppedFiles.elementAt(0);

            // If file is a directory, change current folder to that directory
            if (file.isDirectory())
                folderPanel.tryChangeCurrentFolder(file);
            // For any other file kind (archive, regular file...), change directory to the file's parent folder
            // and select the file
            else
                folderPanel.tryChangeCurrentFolder(file.getParent(), file, false);

            // Request focus on the FolderPanel
            folderPanel.requestFocus();
        }
        // Normal mode: copy or move dropped files to the FolderPanel's current folder
        else {
            MainFrame mainFrame = folderPanel.getMainFrame();
            AbstractFile destFolder = folderPanel.getCurrentFolder();
            if (currentDropAction == DnDConstants.ACTION_MOVE) {
                // Start moving files
                ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("move_dialog.moving"));
                MoveJob moveJob = new MoveJob(progressDialog,
                        mainFrame,
                        droppedFiles,
                        destFolder,
                        null,
                        FileCollisionDialog.ASK_ACTION,
                        false);
                progressDialog.start(moveJob);
            } else {
                // Start copying files
                ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
                CopyJob job = new CopyJob(progressDialog,
                        mainFrame,
                        droppedFiles,
                        destFolder,
                        null,
                        TransferMode.COPY,
                        FileCollisionDialog.ASK_ACTION);
                progressDialog.start(job);
            }
        }

        // Report that the drop event has been successfully handled
        event.dropComplete(true);
    }
}
