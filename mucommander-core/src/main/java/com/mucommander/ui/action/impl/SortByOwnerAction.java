/*
 * This file is part of muCommander, http://www.mucommander.com
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

import java.util.Map;

import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.Column;

/**
 * This action sorts the currently active {@link com.mucommander.ui.main.table.FileTable} by owner.
 * If the table is already sorted by owner, the sort order will be reversed.
 *
 * @author Maxence Bernard
 */
public class SortByOwnerAction extends SortByAction {

    public SortByOwnerAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties, Column.OWNER);
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Descriptor extends SortByAction.Descriptor {

        public Descriptor() {
            super(Column.OWNER);
        }
    }
}
