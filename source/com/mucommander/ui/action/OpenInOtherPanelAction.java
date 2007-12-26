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

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileProtocols;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.job.TempExecJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;

import java.util.Hashtable;

/**
 * Opens files in the inactive panel.
 * <p>
 * This action will behave exactly like {@link OpenAction} for non-browsable files.
 * Browsable files, however, will be opened in the inactive folder panel rather than
 * the active one.
 * </p>
 * @author Nicolas Rinaudo
 */
public class OpenInOtherPanelAction extends OpenAction {
    /**
     * Creates a new <code>OpenInOtherPanelAction</code> with the specified parameters.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public OpenInOtherPanelAction(MainFrame mainFrame, Hashtable properties) {super(mainFrame, properties);}

    /**
     * Opens the currently selected file in the inactive folder panel.
     */
    public void performAction() {
        AbstractFile file;

        // Retrieves the currently selected file, aborts if none.
        if((file = mainFrame.getActiveTable().getSelectedFile(true)) == null)
            return;

        // Opens the currently selected file in the inactive panel.
        open(file, mainFrame.getInactiveTable().getFolderPanel());
    }
}
