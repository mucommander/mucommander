package com.mucommander.ui.event;

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.table.FileTable;

/**
 * Interface to be implemented by classes that wish to be notified of selection changes on a particular
 * FileTable. Those classes need to be registered to receive those events, this can be done by calling
 * {@link com.mucommander.ui.table.FileTable#addTableSelectionListener(TableSelectionListener) FileTable.addTableSelectionListener()}.
 *
 * @see com.mucommander.ui.table.FileTable
 * @author Maxence Bernard
 */
public interface TableSelectionListener {

    /**
     * This method is invoked when the selected file has changed on the specified FileTable .
     *
     * @param source the {@link com.mucommander.ui.table.FileTable} instance on which the file selection has changed
     */
    public void selectedFileChanged(FileTable source);


    /**
     * This method is invoked when the files marked have changed on the specified FileTable.
     *
     * @param source the {@link com.mucommander.ui.table.FileTable} instance on which the files marked have changed
     */
    public void markedFilesChanged(FileTable source);
}
