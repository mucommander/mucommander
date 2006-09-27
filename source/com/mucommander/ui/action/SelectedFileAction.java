package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.event.TableSelectionListener;
import com.mucommander.ui.event.TableChangeListener;
import com.mucommander.file.AbstractFile;

import javax.swing.*;

/**
 * SelectedFileAction is an abstract action that operates on the currently active FileTable,
 * when a file other than the parent folder file '..' is selected.
 *
 * <p>Optionally, a FileFilter can be specified using {@link #setFileFilter(com.mucommander.file.filter.FileFilter) setFileFilter}
 * to further restrict the enable condition to files that match the filter.
 *
 * @author Maxence Bernard
 */
public abstract class SelectedFileAction extends FileAction implements TableSelectionListener, TableChangeListener {

    public SelectedFileAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    protected boolean getFileTableCondition(FileTable fileTable, AbstractFile selectedFile) {
        return selectedFile!=null;
    }
}
