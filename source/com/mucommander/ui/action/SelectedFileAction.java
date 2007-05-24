package com.mucommander.ui.action;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.filter.FileFilter;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

import java.util.Hashtable;

/**
 * SelectedFileAction is an abstract action that operates on the currently active FileTable,
 * and that is enabled only when a file other than the parent folder file '..' is selected.
 *
 * <p>Optionally, a FileFilter can be specified using {@link #setSelectedFileFilter(com.mucommander.file.filter.FileFilter) setSelectedFileFilter}
 * to further restrict the enabled condition to files that match the filter.
 *
 * @author Maxence Bernard
 */
public abstract class SelectedFileAction extends FileAction {

    public SelectedFileAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    /**
     * Restricts the enabled condition to selected files that match the specified filter.
     *
     * @param filter FileFilter instance
     */
    public void setSelectedFileFilter(FileFilter filter) {
        this.filter = filter;
    }


    protected boolean getFileTableCondition(FileTable fileTable) {
        AbstractFile selectedFile = fileTable.getSelectedFile(false, true);
        boolean enable = selectedFile!=null;

        if(enable && filter!=null)
            enable = filter.accept(selectedFile);

        return enable;
    }
}
