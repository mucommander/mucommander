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

import com.mucommander.file.AbstractFile;
import com.mucommander.file.filter.DirectoryFileFilter;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.editor.EditorRegistrar;

import java.util.Hashtable;

/**
 * This action opens the currently selected file in an integrated editor.
 * @author Maxence Bernard
 */
public class InternalEditAction extends SelectedFileAction {

    public InternalEditAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        // Only enable this action if currently selected file is not a directory
        setSelectedFileFilter(new DirectoryFileFilter());
    }

    public void performAction() {
        AbstractFile file = mainFrame.getActiveTable().getSelectedFile();
        if(file!=null && !(file.isDirectory() || file.isSymlink()))
            EditorRegistrar.createEditorFrame(mainFrame, file, getIcon().getImage());
    }
}
