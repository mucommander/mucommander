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

import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.KeyStroke;

import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategories;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.ActionFactory;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;

/**
 * This action moves the current {@link FileTable}'s selection to the next page.
 *
 * @author Maxence Bernard
 */
public class SelectNextPageAction extends SelectForwardAction {

    public SelectNextPageAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    protected int getRowIncrement() {
        // Note: the page row increment varies with the file table's height
        return mainFrame.getActiveTable().getPageRowIncrement()+1;
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Factory implements ActionFactory {
        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new SelectNextPageAction(mainFrame, properties);
        }
    }

    public static class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "SelectNextPage";

        public String getId() {
            return ACTION_ID;
        }

        public ActionCategory getCategory() {
            return ActionCategories.SELECTION;
        }

        public KeyStroke getDefaultKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0);
        }

        public KeyStroke getDefaultAltKeyStroke() {
            return null;
        }
    }
}
