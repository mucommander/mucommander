/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.ui.action;

import com.mucommander.Debug;
import com.mucommander.file.AbstractFile;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

import java.io.IOException;
import java.util.Hashtable;

/**
 * This action changes the current folder of the currently active FolderPanel to the current folder's root.
 * This action only gets enabled when the current folder has a parent.
 *
 * @author Maxence Bernard
 */
public class GoToRootAction extends GoToParentAction {

    public GoToRootAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        // Changes the current folder to make it the current folder's root folder.
        // Does nothing if the current folder already is the root.
        FolderPanel folderPanel = mainFrame.getActiveTable().getFolderPanel();
        AbstractFile currentFolder = folderPanel.getCurrentFolder();
        try {
            folderPanel.tryChangeCurrentFolder(currentFolder.getRoot());
        }
        catch(IOException e) {
            if(Debug.ON) Debug.trace("Failed to retrieve root folder for : "+currentFolder+" :"+e);
        }
    }
}