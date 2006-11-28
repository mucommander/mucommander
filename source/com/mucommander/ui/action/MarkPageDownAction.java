package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

/**
 * Marks/unmarks rows in the active FileTable, from the currently selected row to the next page's row (inclusive).
 * The row immediately after the last marked/unmarked row will become the currently selected row.
 *
 * @author Maxence Bernard
 */
public class MarkPageDownAction extends MucoAction {

    public MarkPageDownAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        FileTable fileTable = mainFrame.getActiveTable();

        int currentRow = fileTable.getSelectedRow();
        int lastRow = fileTable.getRowCount()-1;
        int endRow = Math.min(lastRow, currentRow + fileTable.getPageRowIncrement());

        fileTable.setRangeMarked(currentRow, endRow, !fileTable.getFileTableModel().isRowMarked(currentRow));
        fileTable.selectRow(Math.min(lastRow, endRow+1));
    }
}
