package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

import java.util.Hashtable;

/**
 * Marks/unmarks files in the active FileTable, from the currently selected row to the first row (inclusive).
 * The first row will also become the currently selected row.
 *
 * <p>The currently selected row's marked state determines whether the rows will be marked or unmarked : if the selected
 * row is marked, the rows will be unmarked and vice-versa.
 *
 * @author Maxence Bernard
 */
public class MarkUpToFirstRowAction extends MucoAction {

    public MarkUpToFirstRowAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        FileTable fileTable = mainFrame.getActiveTable();

        int selectedRow = fileTable.getSelectedRow();

        fileTable.setRangeMarked(selectedRow, 0, !fileTable.getFileTableModel().isRowMarked(selectedRow));
        fileTable.selectRow(0);
    }
}
