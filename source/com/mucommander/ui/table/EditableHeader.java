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

import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.Component;
import java.awt.Rectangle;
import java.util.EventObject;

public class EditableHeader extends JTableHeader implements CellEditorListener {

	public final int HEADER_ROW = -10;

	transient protected int editingColumn;

	transient protected TableCellEditor cellEditor;

	transient protected Component editorComp;

	public EditableHeader(TableColumnModel columnModel) {
		super(columnModel);
		setReorderingAllowed(false);
		cellEditor = null;
		recreateTableColumn(columnModel);
	}

	@Override
    public void updateUI() {
		setUI(new EditableHeaderUI());
		resizeAndRepaint();
		invalidate();
	}

	protected void recreateTableColumn(TableColumnModel columnModel) {
		int n = columnModel.getColumnCount();
		EditableHeaderTableColumn[] newCols = new EditableHeaderTableColumn[n];
		TableColumn[] oldCols = new TableColumn[n];
		for (int i = 0; i < n; i++) {
			oldCols[i] = columnModel.getColumn(i);
			newCols[i] = new EditableHeaderTableColumn();
			newCols[i].copyValues(oldCols[i]);
		}
		for (int i = 0; i < n; i++) {
			columnModel.removeColumn(oldCols[i]);
		}
		for (int i = 0; i < n; i++) {
			columnModel.addColumn(newCols[i]);
		}
	}

	public boolean editCellAt(int index, EventObject e) {
		if (cellEditor != null && !cellEditor.stopCellEditing()) {
			return false;
		}
		/*if (!isCellEditable(index)) {
			return false;
		} */
		TableCellEditor editor = getCellEditor(index);

		if (editor != null && editor.isCellEditable(e)) {
			editorComp = prepareEditor(editor, index);
			editorComp.setBounds(getHeaderRect(index));
			add(editorComp);
			editorComp.validate();
			setCellEditor(editor);
			setEditingColumn(index);
			editor.addCellEditorListener(this);

			return true;
		}
		return false;
	}

	/*public boolean isCellEditable(int index) {
		return false;
	}*/

	public TableCellEditor getCellEditor(int index) {
		int columnIndex = columnModel.getColumn(index).getModelIndex();
		EditableHeaderTableColumn col = (EditableHeaderTableColumn) columnModel.getColumn(columnIndex);
		return col.getHeaderEditor();
	}

	public void setCellEditor(TableCellEditor newEditor) {
		TableCellEditor oldEditor = cellEditor;
		cellEditor = newEditor;

		// firePropertyChange

		if (oldEditor != null && oldEditor instanceof TableCellEditor) {
			oldEditor.removeCellEditorListener(this);
		}
		if (newEditor != null && newEditor instanceof TableCellEditor) {
			newEditor.addCellEditorListener(this);
		}
	}

	public Component prepareEditor(TableCellEditor editor, int index) {
		Object value = columnModel.getColumn(index).getHeaderValue();
		boolean isSelected = true;
		int row = HEADER_ROW;
		JTable table = getTable();
		return editor.getTableCellEditorComponent(table, value,
				isSelected, row, index);
	}

	public TableCellEditor getCellEditor() { return cellEditor; }

	public Component getEditorComponent() { return editorComp; }

	public void setEditingColumn(int aColumn) { editingColumn = aColumn; }

	public int getEditingColumn() { return editingColumn; }

	public void removeEditor() {
		TableCellEditor editor = getCellEditor();
		if (editor != null) {
			editor.removeCellEditorListener(this);

			requestFocus();
			remove(editorComp);

			int index = getEditingColumn();
			Rectangle cellRect = getHeaderRect(index);

			setCellEditor(null);
			setEditingColumn(-1);
			editorComp = null;

			repaint(cellRect);
		}
	}

	public boolean isEditing() { return cellEditor != null; }

	//
	// CellEditorListener
	//
	public void editingStopped(ChangeEvent e) {
		TableCellEditor editor = getCellEditor();
		if (editor != null) {
			Object value = editor.getCellEditorValue();
			int index = getEditingColumn();
			columnModel.getColumn(index).setHeaderValue(value);
			removeEditor();
		}
	}

	public void editingCanceled(ChangeEvent e) {
		removeEditor();
	}
}