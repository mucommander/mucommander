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

import com.mucommander.text.Translator;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategories;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.Columns;

import javax.swing.KeyStroke;
import java.util.Hashtable;

/**
 * Shows/hides a specified column of the currently active FileTable. If the column is currently visible, this action
 * will hide it and vice-versa.
 *
 * @author Maxence Bernard
 */
public abstract class ToggleColumnAction extends MuAction {

    /** Index of the FileTable column this action operates on */
    protected int columnIndex;

    public ToggleColumnAction(MainFrame mainFrame, Hashtable<String,Object> properties, int columnIndex) {
        super(mainFrame, properties);

        this.columnIndex = columnIndex;
        updateLabel();
    }

    protected boolean isColumnVisible() {
        return mainFrame.getActiveTable().isColumnVisible(columnIndex);
    }

    protected void updateLabel() {
        setLabel(Translator.get(isColumnVisible()?"ToggleColumn.hide":"ToggleColumn.show", Columns.getColumnLabel(Columns.DATE)));
    }

    @Override
    public void performAction() {
        mainFrame.getActiveTable().setColumnEnabled(columnIndex, !isColumnVisible());
    }


    public static abstract class Descriptor extends AbstractActionDescriptor {

        private int columnIndex; 

        public Descriptor(int columnIndex) { this.columnIndex = columnIndex; }

        public String getId() { return Columns.getToggleColumnActionId(columnIndex); }

        public ActionCategory getCategory() { return ActionCategories.VIEW; }

        public KeyStroke getDefaultAltKeyStroke() { return null; }

        public KeyStroke getDefaultKeyStroke() { return null; }

        @Override
        public String getLabel() { return Translator.get("ToggleColumn.show", Columns.getColumnLabel(columnIndex)); }
    }
}
