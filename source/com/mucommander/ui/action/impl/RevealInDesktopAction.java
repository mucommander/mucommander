/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

import com.mucommander.desktop.DesktopManager;
import com.mucommander.file.AbstractArchiveEntryFile;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileProtocols;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.*;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.main.MainFrame;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Hashtable;


/**
 * This action reveals the currently selected file or folder in the native Desktop's file manager
 * (e.g. Finder for Mac OS X, Explorer for Windows, etc...).
 *
 * @author Maxence Bernard
 */
public class RevealInDesktopAction extends ParentFolderAction {

    public RevealInDesktopAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
        super(mainFrame, properties);

        setEnabled(DesktopManager.canOpenInFileManager());
    }

    @Override
    protected void toggleEnabledState() {
        AbstractFile currentFolder = mainFrame.getActiveTable().getCurrentFolder();
        setEnabled(currentFolder.getURL().getScheme().equals(FileProtocols.FILE)
               && !currentFolder.isArchive()
               && !currentFolder.hasAncestor(AbstractArchiveEntryFile.class)
        );
    }

    @Override
    public void performAction() {
        try {
            DesktopManager.openInFileManager(mainFrame.getActivePanel().getCurrentFolder());
        }
        catch(Exception e) {
            InformationDialog.showErrorDialog(mainFrame);
        }
    }
    
    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
			return new RevealInDesktopAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "RevealInDesktop";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.NAVIGATION; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK); }

        @Override
        public String getLabel() {
            return Translator.get(ActionProperties.getActionLabelKey(RevealInDesktopAction.Descriptor.ACTION_ID), DesktopManager.canOpenInFileManager()?DesktopManager.getFileManagerName():Translator.get("file_manager"));
        }
    }
}
