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

import com.mucommander.text.Translator;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategories;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.Column;

import javax.swing.*;
import java.util.Map;

/**
 * Shows/hides a specified column of the currently active FileTable. If the column is currently visible, this action
 * will hide it and vice-versa.
 *
 * @author Maxence Bernard
 */
public abstract class ToggleColumnAction extends MuAction {

    /** Index of the FileTable column this action operates on */
    protected Column column;

    public ToggleColumnAction(MainFrame mainFrame, Map<String,Object> properties, Column column) {
        super(mainFrame, properties);

        this.column = column;
        updateLabel();
    }

    protected boolean isColumnVisible() {
        return mainFrame.getActiveTable().isColumnVisible(column);
    }

    protected void updateLabel() {
        setLabel(Translator.get(isColumnVisible()?"ToggleColumn.hide":"ToggleColumn.show", column.getLabel()));
    }

    @Override
    public void performAction() {
        mainFrame.getActiveTable().setColumnEnabled(column, !isColumnVisible());
    }


    public static abstract class Descriptor extends AbstractActionDescriptor {

        private Column column;

        public Descriptor(Column column) { this.column = column; }

        public String getId() { return column.getToggleColumnActionId(); }

        public ActionCategory getCategory() { return ActionCategories.VIEW; }

        public KeyStroke getDefaultAltKeyStroke() { return null; }

        public KeyStroke getDefaultKeyStroke() { return null; }

        @Override
        public String getLabel() { return Translator.get("ToggleColumn.show", column.getLabel()); }
    }
}
