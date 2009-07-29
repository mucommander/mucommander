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

package com.mucommander.ui.action.impl;

import com.mucommander.command.Command;
import com.mucommander.file.AbstractFile;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategories;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.ActionFactory;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.viewer.EditorRegistrar;

import javax.swing.*;

import java.util.Hashtable;

/**
 * Opens the current file in edit mode.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class InternalEditAction extends AbstractViewerAction {
    // - Initialization ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new instance of <code>EditAction</code>.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public InternalEditAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        ImageIcon icon;
        if((icon = getStandardIcon(EditAction.class)) != null)
            setIcon(icon);
    }



    // - AbstractViewerAction implementation ---------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Opens the internal editor on the specified file.
     * @param file file to edit.
     */
    protected void performInternalAction(AbstractFile file) {
        EditorRegistrar.createEditorFrame(mainFrame, file, getIcon().getImage());
    }

    protected Command getCustomCommand() {
        return null;
    }



    // - Factory -------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    public static class Factory implements ActionFactory {
		public MuAction createAction(MainFrame mainFrame, Hashtable properties) {
			return new InternalEditAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "InternalEdit";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.FILES; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return null; }
    }
}
