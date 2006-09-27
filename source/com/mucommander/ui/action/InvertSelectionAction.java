package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableModel;
import com.mucommander.file.AbstractFile;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action .
 *
 * @author Maxence Bernard
 */
public class InvertSelectionAction extends MucoAction {

    public InvertSelectionAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction(MainFrame mainFrame) {
        FileTable fileTable = mainFrame.getLastActiveTable();
        FileTableModel tableModel = (FileTableModel)fileTable.getModel();

        // Starts at 1 if current folder is not root so that '..' is not marked
        AbstractFile file;
        int nbRows = tableModel.getRowCount();
        for(int i=fileTable.getParent()==null?0:1; i<nbRows; i++) {
            file = tableModel.getFileAtRow(i);
            if(!file.isDirectory())
                tableModel.setRowMarked(i, !tableModel.isRowMarked(i));
        }
        fileTable.repaint();

        // Update status bar info
        mainFrame.getStatusBar().updateSelectedFilesInfo();
    }
}
