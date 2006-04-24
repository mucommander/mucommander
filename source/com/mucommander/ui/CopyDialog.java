
package com.mucommander.ui;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;
import com.mucommander.job.CopyJob;
import com.mucommander.text.Translator;


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
	public CopyDialog(MainFrame mainFrame, FileSet files, boolean isShiftDown) {
		super(mainFrame, files,
			Translator.get("copy_dialog.copy"),
			Translator.get("copy_dialog.destination"),
			Translator.get("copy_dialog.copy"),
			Translator.get("copy_dialog.error_title"));
	    
		int nbFiles = files.size();
        
		AbstractFile destFolder = mainFrame.getUnactiveTable().getCurrentFolder();
        String fieldText;

		// Local copy: fill text field with the sole file's name
		if(isShiftDown && nbFiles==1) {
			fieldText = ((AbstractFile)files.elementAt(0)).getName();

			// Select the filename without extension, only if filename part is not empty (unlike '.DS_Store' for example)
			int extPos = fieldText.indexOf('.');
			int len = fieldText.length();
			
			setTextField(fieldText, 0, extPos>0?extPos:len);
		}
		// Fill text field with absolute path, and if there is only one file, 
		// append file's name
		else {
			fieldText = destFolder.getAbsolutePath(true);
			// Append filename to destination path if there is only one file to copy
			// and if the file is not a directory that already exists in destination
			// (otherwise folder would be copied inside the destination folder)
			if(nbFiles==1) {
				AbstractFile file = ((AbstractFile)files.elementAt(0));
				AbstractFile testFile;
			 	if(!(file.isDirectory() && (testFile=AbstractFile.getAbstractFile(fieldText+file.getName())).exists() && testFile.isDirectory()))
					fieldText += file.getName();
			}

			setTextField(fieldText);
		}
		
		showDialog();
	}


	/**
	 * Starts a CopyJob. This method is trigged by the 'OK' button or return key.
	 */
	protected void startJob(AbstractFile destFolder, String newName, int defaultFileExistsAction) {

		if (newName==null && files.getBaseFolder().equals(destFolder)) {
			showErrorDialog(Translator.get("same_source_destination"));
			return;
		}

		// Starts copying files
		ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
		CopyJob job = new CopyJob(progressDialog, mainFrame, files, destFolder, newName, CopyJob.COPY_MODE, defaultFileExistsAction);
		progressDialog.start(job);
	}

}