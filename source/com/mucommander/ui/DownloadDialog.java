
package com.mucommander.ui;

import com.mucommander.ui.table.FileTable;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;
import com.mucommander.job.CopyJob;
import com.mucommander.text.Translator;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * Dialog invoked when the user wants to download a file.
 *
 * @author Maxence Bernard
 */
public class DownloadDialog extends DestinationDialog {

	private FileSet files;
	
	private String fileURL;

	private String fileName;
	
	
	public DownloadDialog(MainFrame mainFrame, FileSet files) {
		super(mainFrame, files,
			Translator.get("download_dialog.download"),
			Translator.get("download_dialog.description"),
			Translator.get("download_dialog.download"),
			Translator.get("download_dialog.error_title"));

		this.files = files;
		AbstractFile file = (AbstractFile)files.elementAt(0);
		
//		AbstractFile activeFolder = mainFrame.getLastActiveTable().getCurrentFolder();
		AbstractFile unactiveFolder = mainFrame.getUnactiveTable().getCurrentFolder();
		// Fill text field with current folder's absolute path and file name
		setTextField(unactiveFolder.getAbsolutePath(true)+file.getName());
		showDialog();
	}

	
	protected void startJob(AbstractFile destFolder, String newName, int defaultFileExistsAction) {
		// Starts moving files
		ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("download_dialog.downloading"));
		CopyJob downloadJob = new CopyJob(progressDialog, mainFrame, files, destFolder, newName, CopyJob.DOWNLOAD_MODE, defaultFileExistsAction);
		progressDialog.start(downloadJob);
	}
	
}
