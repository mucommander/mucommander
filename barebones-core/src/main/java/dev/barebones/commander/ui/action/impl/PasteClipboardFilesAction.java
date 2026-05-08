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

package dev.barebones.commander.ui.action.impl;

import java.util.Map;

import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.commons.file.util.FileSet;
import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.job.impl.CopyJob;
import dev.barebones.commander.job.impl.CopyJob.TransferMode;
import dev.barebones.commander.text.Translator;
import dev.barebones.commander.ui.action.AbstractActionDescriptor;
import dev.barebones.commander.ui.action.ActionCategory;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.action.MuAction;
import dev.barebones.commander.ui.action.NoIcon;
import dev.barebones.commander.ui.dialog.file.FileCollisionDialog;
import dev.barebones.commander.ui.dialog.file.ProgressDialog;
import dev.barebones.commander.ui.dnd.ClipboardNotifier;
import dev.barebones.commander.ui.dnd.ClipboardSupport;
import dev.barebones.commander.ui.main.MainFrame;

/**
 * This action pastes the files contained by the system clipboard to the currently active folder. Does nothing if the
 * clipboard doesn't contain any file.
 *
 * <p>
 * Under Java 1.5 and up, this action gets automatically enabled/disabled when files are present/not present in the
 * clipboard.
 *
 * @author Maxence Bernard
 */
public class PasteClipboardFilesAction extends MuAction {

    public PasteClipboardFilesAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
        new ClipboardNotifier(this);
    }

    @Override
    public void performAction() {
        // Retrieve clipboard files
        FileSet clipboardFiles = ClipboardSupport.getClipboardFiles();
        if (clipboardFiles == null || clipboardFiles.isEmpty())
            return;

        // Start copying files
        ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
        AbstractFile destFolder = mainFrame.getActivePanel().getCurrentFolder();
        CopyJob job = new CopyJob(progressDialog,
                mainFrame,
                clipboardFiles,
                destFolder,
                null,
                TransferMode.COPY,
                FileCollisionDialog.FileCollisionAction.ASK);
        progressDialog.start(job);
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    @NoIcon
    public static class Descriptor extends AbstractActionDescriptor {
        public String getId() {
            return ActionType.PasteClipboardFiles.getId();
        }

        public ActionCategory getCategory() {
            return ActionCategory.SELECTION;
        }
    }
}
