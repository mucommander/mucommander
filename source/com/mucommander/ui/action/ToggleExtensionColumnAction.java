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

import com.mucommander.text.Translator;
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.Columns;
import com.mucommander.ui.main.table.FileTable;

import java.util.Hashtable;

/**
 * Shows/hides the 'Extension' column of the currently active FileTable. If the column is currently visible, this will
 * hide it and vice-versa.
 *
 * @author Maxence Bernard
 */
public class ToggleExtensionColumnAction extends MuAction {

    public ToggleExtensionColumnAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties, false);
        setLabel(Translator.get("extension"));
    }

    public void performAction() {
        if(mainFrame.getActiveTable() == mainFrame.getLeftTable())
            MuConfiguration.setVariable(MuConfiguration.SHOW_LEFT_EXTENSION, !MuConfiguration.getVariable(MuConfiguration.SHOW_LEFT_EXTENSION,
                                                                                                          MuConfiguration.DEFAULT_SHOW_EXTENSION));
        else
            MuConfiguration.setVariable(MuConfiguration.SHOW_RIGHT_EXTENSION, !MuConfiguration.getVariable(MuConfiguration.SHOW_RIGHT_EXTENSION,
                                                                                                          MuConfiguration.DEFAULT_SHOW_EXTENSION));
    }
}
