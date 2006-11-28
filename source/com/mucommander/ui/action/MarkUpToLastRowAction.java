package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

/**
 * Marks/unmarks files in the active FileTable, from the currently selected row to the last row (inclusive).
 * The last row will also become the currently selected row.
 *
 * <p>The currently selected row's marked state determines whether the rows will be marked or unmarked : if the selected
 * row is marked, the rows will be unmarked and vice-versa.
 *
 * @author Maxence Bernard
 */
public class MarkUpToLastRowAction extends MucoAction {

    public MarkUpToLastRowAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        FileTable fileTable = mainFrame.getActiveTable();

        int selectedRow = fileTable.getSelectedRow();
        int lastRow = fileTable.getRowCount()-1;

        fileTable.setRangeMarked(selectedRow, lastRow, !fileTable.getFileTableModel().isRowMarked(selectedRow));
        fileTable.selectRow(lastRow);
    }
}
