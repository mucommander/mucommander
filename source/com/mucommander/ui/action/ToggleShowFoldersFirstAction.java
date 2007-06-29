/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.ui.action;

import com.mucommander.conf.ConfigurationManager;
import com.mucommander.conf.ConfigurationVariables;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

import java.util.Hashtable;

/**
 * This action toggles the 'Show folders first' option, which controls whether folders are displayed first in the
 * FileTable or mixed with regular files.
 *
 * @author Maxence Bernard
 */
public class ToggleShowFoldersFirstAction extends MucoAction {

    public ToggleShowFoldersFirstAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        FileTable activeTable = mainFrame.getActiveTable();
        boolean showFoldersFirst = !activeTable.isShowFoldersFirstEnabled();
        activeTable.setShowFoldersFirstEnabled(showFoldersFirst);
        ConfigurationManager.setVariableBoolean(ConfigurationVariables.SHOW_FOLDERS_FIRST, showFoldersFirst);
    }
}
