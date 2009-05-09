/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

package com.mucommander.ui.dialog.pref.component;

import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.mucommander.ui.dialog.pref.PreferencesDialog;

/**
 * @author Arik Hadas
 */
public abstract class PrefTable extends JTable implements PrefComponent {
	
	private TableModelListener dialogListener;

	public PrefTable() { super(); }
	
	public PrefTable(TableModel model) { super(model); }
	
	/**
	 * This function sets the widths of the table's columns according to the given array.
	 *
	 * @param percentages - array that contains the width of each column in percentage
	 * 	from the width of the whole table.
	 */
	public void setPreferredColumnWidths(double[] percentages) {
		Dimension tableDim = this.getPreferredSize();
		
		double total = 0;
		for (int i = 0; i < getColumnModel().getColumnCount(); i++)
			total += percentages[i];
		
		for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
			TableColumn column = getColumnModel().getColumn(i);
			column.setPreferredWidth((int) (tableDim.width * (percentages[i] / total)));
		}
	}

	public void addDialogListener(final PreferencesDialog dialog) {
		getModel().addTableModelListener(dialogListener = new TableModelListener() {			
			public void tableChanged(TableModelEvent e) {
				dialog.componentChanged(PrefTable.this);
			}
		});
	}
	
	public void setModel(TableModel model) {
		if (dialogListener != null)
			model.addTableModelListener(dialogListener);
		super.setModel(model);
	}
}
