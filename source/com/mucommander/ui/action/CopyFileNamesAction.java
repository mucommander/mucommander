package com.mucommander.ui.action;

import com.mucommander.file.FileSet;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.dnd.TransferableFileSet;
import com.mucommander.ui.dnd.ClipboardSupport;


/**
 * This action copies the filename(s) of the currently selected / marked files(s) to the system clipboard.
 *
 * @author Maxence Bernard
 */
public class CopyFileNamesAction extends SelectedFilesAction {

    public CopyFileNamesAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        FileSet selectedFiles = mainFrame.getActiveTable().getSelectedFiles();

        if(selectedFiles.size()>0) {
            // Create a TransferableFileSet and make DataFlavour.stringFlavor (text) the only DataFlavour supported
            TransferableFileSet tfs = new TransferableFileSet(selectedFiles);

            // Disable DataFlavor.javaFileListFlavor support
            tfs.setJavaFileListDataFlavorSupported(true);

            // Disable FileSetDataFlavor support because:
            // a/ we don't want it
            // b/ it would otherwise throw an exception because the data is not serializable
            tfs.setFileSetDataFlavorSupported(false);

            // Transfer filenames, not file paths
            tfs.setStringDataFlavourTransfersFilename(true);

            ClipboardSupport.setClipboardContents(tfs);
        }



/*
        // Get selected file OR marked files (if any), returned FileSet should never be empty
        FileSet selectedFiles = mainFrame.getActiveTable().getSelectedFiles();
        int nbFiles = selectedFiles.size();
        if(nbFiles==0)
            return;

        // Iterate on all selected/marked files
        StringBuffer clipboardText = new StringBuffer();
        // If shift down, copy full paths instead of names
        AbstractFile file;
        for(int i=0; i<nbFiles; i++) {
            file = selectedFiles.fileAt(i);
            clipboardText.append(file.getName());
            if(i!=nbFiles-1)
                clipboardText.append('\n');
        }

        // Set clipboard's content
        StringSelection stringSelection = new StringSelection(clipboardText.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, stringSelection);
*/
    }
}