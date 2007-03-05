package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

import java.util.Hashtable;

/**
 * SelectedFilesAction is an abstract action that operates on the currently active FileTable, and is enabled only
 * when at least one file is marked, or when a file other than the parent folder file '..' is selected.
 * When none of those conditions is satisfied, this action is disabled.
 *
 * @author Maxence Bernard
 */
public abstract class SelectedFilesAction extends SelectedFileAction {

    public SelectedFilesAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    protected boolean getFileTableCondition(FileTable fileTable) {
        return fileTable.getFileTableModel().getNbMarkedFiles()>0 || super.getFileTableCondition(fileTable);
    }
}
