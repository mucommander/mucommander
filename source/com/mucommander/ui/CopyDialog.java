
package com.mucommander.ui;

import com.mucommander.ui.table.FileTable;
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

	private boolean unzipDialog;

	private Vector filesToCopy;

	
	/**
	 * Creates and displays a new CopyDialog.
	 *
	 * @param mainFrame the main frame this dialog is attached to.
	 * @param unzipDialog true if this dialog has been invoked by the 'unzip' action.
	 * @param isShiftDown true if shift key was pressed when invoking this dialog.
	 */
	public CopyDialog(MainFrame mainFrame, boolean unzipDialog, boolean isShiftDown) {
		super(mainFrame, 
			Translator.get(unzipDialog?"unzip_dialog.unzip":"copy_dialog.copy"),
			Translator.get(unzipDialog?"unzip_dialog.destination":"copy_dialog.destination"),
			Translator.get(unzipDialog?"unzip_dialog.unzip":"copy_dialog.copy"));
	    
		this.unzipDialog = unzipDialog;
		
		FileTable activeTable = mainFrame.getLastActiveTable();
		FileTable table1 = mainFrame.getBrowser1().getFileTable();
		FileTable table2 = mainFrame.getBrowser2().getFileTable();
    	this.filesToCopy = activeTable.getSelectedFiles();
		int nbFiles = filesToCopy.size();
		if(nbFiles==0)
    		return;
        
		AbstractFile destFolder = (activeTable==table1?table2:table1).getCurrentFolder();
        String fieldText;
		if(unzipDialog) {
			if(isShiftDown)
				fieldText = ".";
			else
				fieldText = destFolder.getAbsolutePath(true);
		}
		else {
			// Fills text field with sole element's name
			if(isShiftDown && nbFiles==1) {
				fieldText = ((AbstractFile)filesToCopy.elementAt(0)).getName();
			}
			// Fills text field with absolute path, and if there is only one file, append
			// file's name
			else {
				fieldText = destFolder.getAbsolutePath(true);
				AbstractFile file = ((AbstractFile)filesToCopy.elementAt(0));
				AbstractFile testFile;
				if(nbFiles==1 && 
					!(file.isDirectory() && 
					(testFile=AbstractFile.getAbstractFile(fieldText+file.getName())).exists() && testFile.isDirectory())) {
					
					fieldText += file.getName();
				}
			}
		}
		
		setFieldText(fieldText);
		
		showDialog();
	}


	/**
	 * Starts a CopyJob. This method is trigged by the 'OK' button or return key.
	 */
	protected void okPressed() {
		String destPath = pathField.getText();
		FileTable activeTable = mainFrame.getLastActiveTable();

		// Resolves destination folder
		Object ret[] = mainFrame.resolvePath(destPath);
		// The path entered doesn't correspond to any existing folder
		if (ret==null || ((filesToCopy.size()>1 || unzipDialog) && ret[1]!=null)) {
			if(unzipDialog)				
				showErrorDialog(Translator.get("unzip_dialog.folder_does_not_exist", destPath), Translator.get("unzip_dialog.error_title"));
			else
				showErrorDialog(Translator.get("copy_dialog.folder_does_not_exist", destPath), Translator.get("copy_dialog.error_title"));
			return;
		}

		AbstractFile destFolder = (AbstractFile)ret[0];
		String newName = (String)ret[1];

		if (!unzipDialog && newName==null && activeTable.getCurrentFolder().equals(destFolder)) {
			showErrorDialog(Translator.get("copy_dialog.same_source_destination"), Translator.get("copy_dialog.error_title"));
			return;
		}

		// Starts moving files
		ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get(unzipDialog?"unzip_dialog.unzipping":"copy_dialog.copying"));
		CopyJob copyJob = new CopyJob(mainFrame, progressDialog, filesToCopy, newName, destFolder, unzipDialog);
		progressDialog.start(copyJob);
	}

}