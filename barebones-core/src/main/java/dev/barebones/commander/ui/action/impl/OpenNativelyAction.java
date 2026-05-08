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

import java.io.IOException;
import java.util.Map;

import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.commons.file.archive.AbstractArchiveEntryFile;
import dev.barebones.commander.commons.file.protocol.local.LocalFile;
import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.job.impl.TempExecJob;
import dev.barebones.commander.text.Translator;
import dev.barebones.commander.ui.action.AbstractActionDescriptor;
import dev.barebones.commander.ui.action.ActionCategory;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.action.MuAction;
import dev.barebones.commander.ui.action.NoIcon;
import dev.barebones.commander.ui.dialog.InformationDialog;
import dev.barebones.commander.ui.dialog.file.ProgressDialog;
import dev.barebones.commander.ui.main.MainFrame;
import dev.barebones.commander.ui.main.quicklist.RecentExecutedFilesQL;

/**
 * This action opens the currently selected file or folder with native file associations.
 *
 * @author Maxence Bernard
 */
public class OpenNativelyAction extends MuAction {

    public OpenNativelyAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        AbstractFile selectedFile = mainFrame.getActiveTable().getSelectedFile(true, true);

        if (selectedFile == null)
            return;

        // Copy file to a temporary local file and execute it with native file associations if
        // file is not on a local filesystem or file is an archive entry
        if (!LocalFile.SCHEMA.equals(selectedFile.getURL().getScheme())
                || selectedFile.hasAncestor(AbstractArchiveEntryFile.class)) {
            ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
            TempExecJob job = new TempExecJob(progressDialog, mainFrame, selectedFile);
            progressDialog.start(job);
        } else {
            // Tries to execute file with native file associations
            try {
                OpenAction.openFile(getMainFrame(), selectedFile);
                RecentExecutedFilesQL.addFile(selectedFile);
            } catch (IOException | UnsupportedOperationException e) {
                InformationDialog.showErrorDialog(mainFrame.getJFrame());
            }
        }
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    @NoIcon
    public static class Descriptor extends AbstractActionDescriptor {
        public String getId() {
            return ActionType.OpenNatively.getId();
        }

        public ActionCategory getCategory() {
            return ActionCategory.NAVIGATION;
        }
    }
}
