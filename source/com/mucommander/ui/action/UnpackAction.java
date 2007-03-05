package com.mucommander.ui.action;

import com.mucommander.file.filter.ArchiveFileKeeper;
import com.mucommander.file.util.FileSet;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.UnpackDialog;

import java.util.Hashtable;

/**
 * This action pops up the 'Unpack files' dialog that allows to unpack the currently marked files.
 *
 * @author Maxence Bernard
 */
public class UnpackAction extends SelectedFilesAction implements InvokesDialog {

    public UnpackAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        setSelectedFileFilter(new ArchiveFileKeeper());
    }

    public void performAction() {
        FileSet files = mainFrame.getActiveTable().getSelectedFiles();
        if(files.size()>0)
            new UnpackDialog(mainFrame, files, false);
    }
}
