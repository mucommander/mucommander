
package com.mucommander.ui;

import com.mucommander.ui.table.FileTable;
import com.mucommander.file.AbstractFile;
import com.mucommander.job.MoveJob;
import com.mucommander.text.Translator;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.Vector;


/**
 * Dialog invoked when the user wants to move or rename (F6) files.
 *
 * @author Maxence Bernard
 */
public class MoveDialog extends DestinationDialog {

	private Vector filesToMove;
	
	public MoveDialog(MainFrame mainFrame, boolean isShiftDown) {
		super(mainFrame);
		
		FileTable activeTable = mainFrame.getLastActiveTable();
		FileTable table1 = mainFrame.getBrowser1().getFileTable();
		FileTable table2 = mainFrame.getBrowser2().getFileTable();
    	this.filesToMove = activeTable.getSelectedFiles();
		int nbFiles = filesToMove.size();
		if(nbFiles==0)
    		return;

		boolean rename = isShiftDown && nbFiles==1;

		init(Translator.get(rename?"move_dialog.rename":"move_dialog.move"),
			Translator.get(rename?"move_dialog.rename_description":"move_dialog.move_description"),
			Translator.get(rename?"move_dialog.rename":"move_dialog.move"));
        
		String fieldText;
		if(isShiftDown && nbFiles==1) {
			fieldText = ((AbstractFile)filesToMove.elementAt(0)).getName();
		}
		else {
			AbstractFile destFolder = (activeTable==table1?table2:table1).getCurrentFolder();
			fieldText = destFolder.getAbsolutePath(true);
			if(nbFiles==1)
				fieldText += ((AbstractFile)filesToMove.elementAt(0)).getName();
		}

		setTextField(fieldText);
 
		showDialog();
	}



	/**
	 * Starts a MoveJob. This method is trigged by the 'OK' button or return key.
	 */
	protected void okPressed() {
		String destPath = pathField.getText();
	    FileTable activeTable = mainFrame.getLastActiveTable();

		// Resolves destination folder
		Object ret[] = mainFrame.resolvePath(destPath);
		// The path entered doesn't correspond to any existing folder
		if (ret==null || (filesToMove.size()>1 && ret[1]!=null)) {
			showErrorDialog(Translator.get("this_folder_does_not_exist", destPath), Translator.get("move_dialog.error_title"));
			return;
		}

		AbstractFile destFolder = (AbstractFile)ret[0];
		String newName = (String)ret[1];

		if (newName==null && activeTable.getCurrentFolder().equals(destFolder)) {
			showErrorDialog(Translator.get("move_dialog.same_source_destination"), Translator.get("move_dialog.error_title"));
			return;
		}

		if (filesToMove.contains(destFolder)) {
			showErrorDialog(Translator.get("move_dialog.cannot_move_to_itself"), Translator.get("move_dialog.error_title"));
			return;
		}
		
		// Starts moving files
		ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("move_dialog.moving"));
		MoveJob moveJob = new MoveJob(progressDialog, mainFrame, filesToMove, destFolder, newName);
	    progressDialog.start(moveJob);
	}
	
}