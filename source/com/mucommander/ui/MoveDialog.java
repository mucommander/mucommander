
package com.mucommander.ui;

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

	
	public MoveDialog(MainFrame mainFrame, Vector files, boolean isShiftDown) {
		super(mainFrame, files);
		
		int nbFiles = files.size();
		boolean rename = isShiftDown && nbFiles==1;

		init(Translator.get(rename?"move_dialog.rename":"move_dialog.move"),
			Translator.get(rename?"move_dialog.rename_description":"move_dialog.move_description"),
			Translator.get(rename?"move_dialog.rename":"move_dialog.move"),
			Translator.get("move_dialog.error_title"));
        
		String fieldText;
		if(isShiftDown && nbFiles==1) {
			fieldText = ((AbstractFile)files.elementAt(0)).getName();
			// If rename mode, select filename, not extension. Extension must be 4 characters max,
			// and filename must not be null (e.g. '.DS_Store')
			int extPos = fieldText.lastIndexOf('.');
			int len = fieldText.length();
			setTextField(fieldText, 0, extPos<1||len-extPos>5?len:extPos);
		}
		else {
			AbstractFile destFolder = mainFrame.getUnactiveTable().getCurrentFolder();
			fieldText = destFolder.getAbsolutePath(true);
			if(nbFiles==1)
				fieldText += ((AbstractFile)files.elementAt(0)).getName();
			setTextField(fieldText);
		}

 
		showDialog();
	}



	/**
	 * Starts a MoveJob. This method is trigged by the 'OK' button or return key.
	 */
	protected void startJob(AbstractFile sourceFolder, AbstractFile destFolder, String newName, int defaultFileExistsAction) {
		if (newName==null && sourceFolder.equals(destFolder)) {
			showErrorDialog(Translator.get("move_dialog.same_source_destination"));
			return;
		}

		if (files.contains(destFolder)) {
			showErrorDialog(Translator.get("move_dialog.cannot_move_to_itself"));
			return;
		}
		
		// Starts moving files
		ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("move_dialog.moving"));
		MoveJob moveJob = new MoveJob(progressDialog, mainFrame, files, destFolder, newName, defaultFileExistsAction);
	    progressDialog.start(moveJob);
	}
	
}