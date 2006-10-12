package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.dnd.TransferableFileSet;
import com.mucommander.ui.dnd.ClipboardSupport;
import com.mucommander.file.FileSet;

/**
 * This action copies the path(s) of the currently selected / marked files(s) to the system clipboard.
 *
 * @author Maxence Bernard
 */
public class CopyFilePathsAction extends SelectedFilesAction {

    public CopyFilePathsAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        FileSet selectedFiles = mainFrame.getLastActiveTable().getSelectedFiles();

        if(selectedFiles.size()>0) {
            // Create a TransferableFileSet and make DataFlavour.stringFlavor (text) the only DataFlavour supported
            TransferableFileSet tfs = new TransferableFileSet(selectedFiles);

            // Disable DataFlavor.javaFileListFlavor support
            tfs.setJavaFileListDataFlavorSupported(true);

            // Disable FileSetDataFlavor support because:
            // a/ we don't want it
            // b/ it would otherwise throw an exception because the data is not serializable
            tfs.setFileSetDataFlavorSupported(false);

            ClipboardSupport.setClipboardContents(tfs);
        }
    }
}