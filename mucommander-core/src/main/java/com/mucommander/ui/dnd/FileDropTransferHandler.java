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

import java.awt.datatransfer.DataFlavor;
import java.awt.event.InputEvent;

import javax.swing.TransferHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.job.impl.CopyJob;
import com.mucommander.job.impl.CopyJob.TransferMode;
import com.mucommander.job.impl.MoveJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.FileCollisionDialog;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

/**
 * A {@link TransferHandler} that accepts file drops onto a component associated with a {@link FolderPanel}.
 *
 * <p>Using {@code TransferHandler} instead of a raw {@link java.awt.dnd.DropTarget} /
 * {@link java.awt.dnd.DropTargetListener}, see:
 * https://github.com/mucommander/mucommander/pull/1459#issuecomment-4194206647
 * https://github.com/violetlib/vaqua/issues/53#issuecomment-4202707295
 * <p>
 * {@code TransferHandler} infrastructure installs its own {@link java.awt.dnd.DropTarget} on the component and
 * supports multiple concurrent listeners, so it survives L&amp;F switches without throwing
 * {@code IllegalArgumentException: listener mismatch}.</p>
 *
 * <p>There are two operating modes, selected at construction time:</p>
 * <ul>
 *   <li><b>Folder-change mode</b> ({@code changeFolderOnlyMode=true}): dropping a file/path navigates the
 *       associated {@link FolderPanel} to that location.</li>
 *   <li><b>Normal mode</b> ({@code changeFolderOnlyMode=false}): dropped files can also be copied or moved
 *       into the panel's current folder, depending on the modifier keys held during the drag.</li>
 * </ul>
 */
public class FileDropTransferHandler extends TransferHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileDropTransferHandler.class);

    /**
     * Extended modifiers that trigger a MOVE instead of a COPY (the default):
     * Meta on macOS, Alt everywhere else.
     */
    private static final int MOVE_ACTION_MODIFIERS_EX = OsFamily.MAC_OS.isCurrent()
            ? InputEvent.META_DOWN_MASK
            : InputEvent.ALT_DOWN_MASK;

    private final FolderPanel folderPanel;
    private final boolean changeFolderOnlyMode;

    /**
     * @param folderPanel          the panel to navigate / copy-move into
     * @param changeFolderOnlyMode if {@code true} only folder navigation is performed on drop;
     *                             if {@code false} copy/move is also possible
     */
    public FileDropTransferHandler(FolderPanel folderPanel, boolean changeFolderOnlyMode) {
        this.folderPanel = folderPanel;
        this.changeFolderOnlyMode = changeFolderOnlyMode;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if (!support.isDrop()) {
            return false;
        }

        // Accept any of the three flavors we know how to handle
        if (!support.isDataFlavorSupported(TransferableFileSet.getFileSetDataFlavor())
                && !support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                && !support.isDataFlavorSupported(DataFlavor.getTextPlainUnicodeFlavor())) {
            return false;
        }

        // Refuse drops that originate from the same FolderPanel or from a panel whose current
        // folder is identical to ours (spring-loaded folders are not yet supported)
        if (DnDContext.isDragInitiatedByMucommander()
                && isPointToTargetFolder(DnDContext.getDragInitiator())) {
            return false;
        }

        // Override the drop action when needed and signal the chosen action back to the DnD subsystem
        support.setDropAction(resolveDropAction(support));
        return true;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        FileSet droppedFiles = TransferableFileSet.getTransferFiles(support.getTransferable());
        if (droppedFiles == null || droppedFiles.isEmpty()) {
            return false;
        }

        int dropAction = support.getDropAction();

        if (changeFolderOnlyMode || dropAction == LINK) {
            // Navigate to the dropped file/folder
            AbstractFile file = droppedFiles.elementAt(0);
            if (file.isDirectory()) {
                folderPanel.tryChangeCurrentFolder(file);
            } else {
                folderPanel.tryChangeCurrentFolder(file.getParent(), file, false);
            }
            folderPanel.getPanel().requestFocus();
        } else {
            MainFrame mainFrame = folderPanel.getMainFrame();
            AbstractFile destFolder = folderPanel.getCurrentFolder();
            if (dropAction == MOVE) {
                ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("move_dialog.moving"));
                MoveJob moveJob = new MoveJob(progressDialog, mainFrame, droppedFiles, destFolder,
                        null, FileCollisionDialog.FileCollisionAction.ASK, false);
                progressDialog.start(moveJob);
            } else {
                ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
                CopyJob copyJob = new CopyJob(progressDialog, mainFrame, droppedFiles, destFolder,
                        null, TransferMode.COPY, FileCollisionDialog.FileCollisionAction.ASK);
                progressDialog.start(copyJob);
            }
        }
        return true;
    }

    /**
     * Resolves the effective drop action, overriding the default MOVE→COPY when appropriate.
     * Mirrors the logic that was previously in {@code FileDropTargetListener.determineDropAction()}.
     */
    private int resolveDropAction(TransferSupport support) {
        int dropAction = support.getDropAction();
        int sourceActions = support.getSourceDropActions();

        boolean overrideToDefault = MuConfigurations.getPreferences().getVariable(
                MuPreference.SET_DROP_ACTION_TO_COPY, MuPreferences.DEFAULT_SET_DROP_ACTION_TO_COPY);
        if (!overrideToDefault) {
            return dropAction;
        }

        if (DnDContext.isDragInitiatedByMucommander()) {
            int dragModifiers = DnDContext.getDragGestureModifiersEx();
            if (dropAction == MOVE
                    && (dragModifiers & MOVE_ACTION_MODIFIERS_EX) == 0
                    && (sourceActions & COPY) != 0) {
                LOGGER.debug("internal drag: overriding MOVE → COPY");
                return COPY;
            }
        } else {
            // External drag: flip the Swing default so COPY becomes primary
            if (dropAction == MOVE && (sourceActions & COPY) != 0) {
                LOGGER.debug("external drag: overriding MOVE → COPY");
                return COPY;
            }
            if (dropAction == COPY && (sourceActions & MOVE) != 0) {
                LOGGER.debug("external drag: overriding COPY → MOVE");
                return MOVE;
            }
        }
        return dropAction;
    }

    private boolean isPointToTargetFolder(FolderPanel dragInitiator) {
        return dragInitiator == folderPanel
                || dragInitiator.getCurrentFolder().equalsCanonical(folderPanel.getCurrentFolder());
    }
}
