
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
public class CopyDialog extends DestinationDialog {

	
	/**
	 * Creates and displays a new CopyDialog.
	 *
	 * @param mainFrame the main frame this dialog is attached to.
	 * @param isShiftDown true if shift key was pressed when invoking this dialog.
	 */
	public CopyDialog(MainFrame mainFrame, Vector files, boolean isShiftDown) {
		super(mainFrame, files,
			Translator.get("copy_dialog.copy"),
			Translator.get("copy_dialog.destination"),
			Translator.get("copy_dialog.copy"),
			Translator.get("copy_dialog.error_title"));
	    
		int nbFiles = files.size();
        
		AbstractFile destFolder = mainFrame.getUnactiveTable().getCurrentFolder();
        String fieldText;

		// Fills text field with sole element's name
		if(isShiftDown && nbFiles==1) {
			fieldText = ((AbstractFile)files.elementAt(0)).getName();
		}
		// Fills text field with absolute path, and if there is only one file, append
		// file's name
		else {
			fieldText = destFolder.getAbsolutePath(true);
			AbstractFile file = ((AbstractFile)files.elementAt(0));
			AbstractFile testFile;
			if(nbFiles==1 && 
				!(file.isDirectory() && 
				(testFile=AbstractFile.getAbstractFile(fieldText+file.getName())).exists() && testFile.isDirectory())) {
				
				fieldText += file.getName();
			}
		}
		
		setTextField(fieldText);
		
		showDialog();
	}


	/**
	 * Starts a CopyJob. This method is trigged by the 'OK' button or return key.
	 */
	protected void startJob(AbstractFile sourceFolder, AbstractFile destFolder, String newName, int defaultFileExistsAction) {

		if (newName==null && sourceFolder.equals(destFolder)) {
			showErrorDialog(Translator.get("same_source_destination"));
			return;
		}

		// Starts copying files
		ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
		CopyJob job = new CopyJob(progressDialog, mainFrame, files, destFolder, newName, CopyJob.COPY_MODE, defaultFileExistsAction);
		progressDialog.start(job);
	}

}