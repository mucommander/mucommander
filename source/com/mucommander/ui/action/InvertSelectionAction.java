package com.mucommander.ui.action;

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableModel;

import java.util.Hashtable;

/**
 * This action .
 *
 * @author Maxence Bernard
 */
public class InvertSelectionAction extends MucoAction {

    public InvertSelectionAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        FileTable fileTable = mainFrame.getActiveTable();
        FileTableModel tableModel = fileTable.getFileTableModel();

        // Starts at 1 if current folder is not root so that '..' is not marked
        AbstractFile file;
        int nbRows = tableModel.getRowCount();
        for(int i=fileTable.getParent()==null?0:1; i<nbRows; i++) {
            file = tableModel.getFileAtRow(i);
            if(!file.isDirectory())
                tableModel.setRowMarked(i, !tableModel.isRowMarked(i));
        }
        fileTable.repaint();

        // Notify registered listeners that currently marked files have changed on the FileTable
        fileTable.fireMarkedFilesChangedEvent();
    }
}
