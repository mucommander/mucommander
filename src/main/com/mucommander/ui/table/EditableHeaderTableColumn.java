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

package com.mucommander.ui.table;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

public class EditableHeaderTableColumn extends TableColumn {

	protected TableCellEditor headerEditor;

	protected boolean isHeaderEditable;

	public EditableHeaderTableColumn() {
		isHeaderEditable = false;
	}

	public void setHeaderEditor(TableCellEditor headerEditor) {
		this.headerEditor = headerEditor;
	}

	public TableCellEditor getHeaderEditor() {
		return headerEditor;
	}

	public void setHeaderEditable(boolean isEditable) {
		isHeaderEditable = isEditable;
	}

	public boolean isHeaderEditable() {
		return headerEditor != null && isHeaderEditable;
	}

	public void copyValues(TableColumn base) {
		modelIndex = base.getModelIndex();
		identifier = base.getIdentifier();
		width = base.getWidth();
		minWidth = base.getMinWidth();
		setPreferredWidth(base.getPreferredWidth());
		maxWidth = base.getMaxWidth();
		headerRenderer = base.getHeaderRenderer();
		headerValue = base.getHeaderValue();
		cellRenderer = base.getCellRenderer();
		cellEditor = base.getCellEditor();
		isResizable = base.getResizable();
	}
}
