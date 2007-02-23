
package com.mucommander.ui;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.job.CopyJob;
import com.mucommander.text.Translator;


/**
 * Dialog that allows the user to choose the destination to unpack files to.
 *
 * @author Maxence Bernard
 */
public class UnpackDialog extends DestinationDialog {

	
    /**
     * Creates and displays a new UnpackDialog.
     *
     * @param mainFrame the main frame this dialog is attached to
     * @param files the set of files to unpack
     * @param isShiftDown true if shift key was pressed when invoking this dialog
     */
    public UnpackDialog(MainFrame mainFrame, FileSet files, boolean isShiftDown) {
        super(mainFrame, files,
              Translator.get(com.mucommander.ui.action.UnpackAction.class.getName()+".label"),
              Translator.get("unpack_dialog.destination"),
              Translator.get("unpack_dialog.unpack"),
              Translator.get("unpack_dialog.error_title"));
	    
        AbstractFile destFolder = mainFrame.getInactiveTable().getCurrentFolder();
        String fieldText;
        if(isShiftDown)
            fieldText = ".";
        else
            fieldText = destFolder.getAbsolutePath(true);
		
        setTextField(fieldText);
		
        showDialog();
    }


    /**
     * Starts a CopyJob. This method is trigged by the 'OK' button or return key.
     */
    protected void startJob(AbstractFile destFolder, String newName, int defaultFileExistsAction) {

        // Starts copying files
        ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("unpack_dialog.unpacking"));
        CopyJob job = new CopyJob(progressDialog, mainFrame, files, destFolder, newName, CopyJob.UNPACK_MODE, defaultFileExistsAction);
        progressDialog.start(job);
    }

}
