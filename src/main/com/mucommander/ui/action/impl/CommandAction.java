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

package com.mucommander.ui.action.impl;

import java.util.Map;

import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.command.Command;
import com.mucommander.commons.file.FileProtocols;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.job.TempOpenWithJob;
import com.mucommander.process.ProcessRunner;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategories;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.ActionFactory;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;

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
        if(selectedFiles.size() == 0)
            return;

        // If we're working with local files, go ahead and runs the command.
        if(selectedFiles.getBaseFolder().getURL().getScheme().equals(FileProtocols.FILE) && (selectedFiles.getBaseFolder().hasAncestor(LocalFile.class))) {
            try {ProcessRunner.execute(command.getTokens(selectedFiles), selectedFiles.getBaseFolder());}
            catch(Exception e) {
                InformationDialog.showErrorDialog(mainFrame);

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

    public static class Factory implements ActionFactory {
    	private Command command;

    	public Factory(Command command) {
    		this.command = command;
    	}

    	public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
    		return new CommandAction(mainFrame, properties, command);
    	}
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

    	public ActionCategory getCategory() { return ActionCategories.COMMANDS; }

    	public KeyStroke getDefaultAltKeyStroke() { return null; }

    	public KeyStroke getDefaultKeyStroke() { return null; }
    }
}
