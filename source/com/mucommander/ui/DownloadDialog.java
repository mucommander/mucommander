
package com.mucommander.ui;

import com.mucommander.ui.table.FileTable;
import com.mucommander.file.AbstractFile;
import com.mucommander.job.CopyJob;
import com.mucommander.text.Translator;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;


/**
 * Dialog invoked when the user wants to download a file.
 *
 * @author Maxence Bernard
 */
public class DownloadDialog extends DestinationDialog {

	private Vector fileV;
	
	private String fileURL;

	private String fileName;
	
	
	public DownloadDialog(MainFrame mainFrame, Vector fileV) {
		super(mainFrame, fileV,
			Translator.get("download_dialog.download"),
			Translator.get("download_dialog.description"),
			Translator.get("download_dialog.download"),
			Translator.get("download_dialog.error_title"));

		this.fileV = fileV;
		AbstractFile file = (AbstractFile)fileV.elementAt(0);
		
		AbstractFile activeFolder = mainFrame.getLastActiveTable().getCurrentFolder();
		// Fill text field with current folder's absolute path and file name
		setTextField(activeFolder.getAbsolutePath(true)+file.getName());
		showDialog();
	}

	
	protected void startJob(AbstractFile sourceFolder, AbstractFile destFolder, String newName, int defaultFileExistsAction) {
		
		// Starts moving files
		ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("http_download.downloading"));
//		com.mucommander.job.HttpDownloadJob downloadJob = new com.mucommander.job.HttpDownloadJob(mainFrame, progressDialog, fileURL, destFile);
		CopyJob downloadJob = new CopyJob(progressDialog, mainFrame, fileV, destFolder, newName, CopyJob.DOWNLOAD_MODE, defaultFileExistsAction);
		progressDialog.start(downloadJob);
	}
	
}
