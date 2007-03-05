package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

import java.util.Hashtable;

/**
 * Marks/unmarks rows in the active FileTable, from the currently selected row to the previous page's row (inclusive).
 * The row immediately after the last marked/unmarked row will become the currently selected row.
 *
 * @author Maxence Bernard
 */
public class MarkPageUpAction extends MucoAction {

    public MarkPageUpAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        FileTable fileTable = mainFrame.getActiveTable();

        int currentRow = fileTable.getSelectedRow();
        int endRow = Math.max(0, currentRow - fileTable.getPageRowIncrement());

        fileTable.setRangeMarked(currentRow, endRow, !fileTable.getFileTableModel().isRowMarked(currentRow));
        fileTable.selectRow(Math.max(0, endRow-1));
    }
}
