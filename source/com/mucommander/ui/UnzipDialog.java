
package com.mucommander.ui;

import com.mucommander.file.AbstractFile;
import com.mucommander.job.CopyJob;
import com.mucommander.text.Translator;

import java.util.Vector;


/**
 * Dialog invoked when the user wants to copy (F5) or unzip (thru file menu) files.
 *
 * @author Maxence Bernard
 */
public class UnzipDialog extends DestinationDialog {

	
	/**
	 * Creates and displays a new UnzipDialog.
	 *
	 * @param mainFrame the main frame this dialog is attached to.
	 * @param isShiftDown true if shift key was pressed when invoking this dialog.
	 */
	public UnzipDialog(MainFrame mainFrame, Vector files, boolean isShiftDown) {
		super(mainFrame, files,
			Translator.get("unzip_dialog.unzip"),
			Translator.get("unzip_dialog.destination"),
			Translator.get("unzip_dialog.unzip"),
			Translator.get("unzip_dialog.error_title"));
	    
		AbstractFile destFolder = mainFrame.getUnactiveTable().getCurrentFolder();
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
	protected void startJob(AbstractFile sourceFolder, AbstractFile destFolder, String newName) {

		// Starts copying files
		ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("unzip_dialog.unzipping"));
		CopyJob job = new CopyJob(progressDialog, mainFrame, files, destFolder, newName, CopyJob.UNZIP_MODE);
		progressDialog.start(job);
	}

}
