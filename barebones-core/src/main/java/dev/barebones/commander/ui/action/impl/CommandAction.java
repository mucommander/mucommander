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

import dev.barebones.commander.command.Command;
import dev.barebones.commander.commons.file.protocol.local.LocalFile;
import dev.barebones.commander.commons.file.util.FileSet;
import dev.barebones.commander.job.impl.TempOpenWithJob;
import dev.barebones.commander.process.ProcessRunner;
import dev.barebones.commander.text.Translator;
import dev.barebones.commander.ui.action.AbstractActionDescriptor;
import dev.barebones.commander.ui.action.ActionCategory;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.action.MuAction;
import dev.barebones.commander.ui.dialog.InformationDialog;
import dev.barebones.commander.ui.dialog.file.ProgressDialog;
import dev.barebones.commander.ui.main.MainFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nicolas Rinaudo
 */
public class CommandAction extends MuAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandAction.class);

    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Command to run. */
    private Command command;



    // - Initialization --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Creates a new <code>CommandAction</code> initialized with the specified parameters.
     * @param mainFrame  frame that will be affected by this action.
     * @param properties ignored.
     * @param command    command to run when this action is called.
     */
    public CommandAction(MainFrame mainFrame, Map<String,Object> properties, Command command) {
        super(mainFrame, properties);
        this.command = command;
        setLabel(command.getDisplayName());
    }



    // - Action code -----------------------------------------------------------
    // -------------------------------------------------------------------------
    @Override
    public void performAction() {
        FileSet selectedFiles;

        // Retrieves the current selection.
        selectedFiles = mainFrame.getActiveTable().getSelectedFiles();

        // If no files are either selected or marked, aborts.
        if(command.hasSelectedFileKeyword() && selectedFiles.size() == 0)
            return;

        // If we're working with local files, go ahead and runs the command.
        if(selectedFiles.getBaseFolder().getURL().getScheme().equals(LocalFile.SCHEMA) && (selectedFiles.getBaseFolder().hasAncestor(LocalFile.class))) {
            try {
                InformationDialog.showErrorDialogIfNeeded(getMainFrame().getJFrame(), ProcessRunner.executeAsync(command.getTokens(selectedFiles), selectedFiles.getBaseFolder()));
            } catch(Exception e) {
                InformationDialog.showErrorDialog(mainFrame.getJFrame());
                LOGGER.debug("Failed to execute command: " + command.getCommand(), e);
            }
        }
        // Otherwise, copies the files locally before running the command.
        else {
            ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
            progressDialog.start(new TempOpenWithJob(new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying")), mainFrame, selectedFiles, command));
        }
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor(command);
    }

    public static class Descriptor extends AbstractActionDescriptor {
        private static final String ACTION_ID_PREFIX = "OpenWith_";
        private String ACTION_ID;
        private String label;

        public Descriptor(Command command) {
            ACTION_ID = ACTION_ID_PREFIX + command.getAlias();
            label = String.format("%s %s",
                    Translator.get("file_menu.open_with"),
                    command.getDisplayName());
        }

        public String getId() { return ACTION_ID; }

        public String getLabel() { return label; }

        public ActionCategory getCategory() { return ActionCategory.COMMANDS; }
    }
}
