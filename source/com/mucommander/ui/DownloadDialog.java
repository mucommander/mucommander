
package com.mucommander.ui;

import com.mucommander.ui.table.FileTable;
import com.mucommander.file.AbstractFile;
import com.mucommander.job.MoveJob;
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

	private String fileURL;

	private String fileName;
	
	
	public DownloadDialog(MainFrame mainFrame, String URL) {
		super(mainFrame,
			Translator.get("download_dialog.download"),
			Translator.get("download_dialog.description"),
			Translator.get("download_dialog.download"));
		
		this.fileURL = URL;

		// Determines local file name
		int urlLen = fileURL.length();
		while(fileURL.charAt(urlLen-1)=='/')
			fileURL = fileURL.substring(0, --urlLen);
		int lastSlashPos = fileURL.lastIndexOf('/');
		this.fileName = java.net.URLDecoder.decode(fileURL.substring(lastSlashPos==-1||lastSlashPos<7?7:lastSlashPos+1, urlLen));
		
		AbstractFile activeFolder = mainFrame.getLastActiveTable().getCurrentFolder();
		setTextField(activeFolder.getAbsolutePath(true)+fileName);
		showDialog();
	}

	
	/**
	 * Starts an HttpDownloadJob. This method is trigged by the 'OK' button or return key.
	 */
	protected void okPressed() {
		String destPath = pathField.getText();

		// Resolves destination folder
		Object ret[] = mainFrame.resolvePath(destPath);
		// The path entered doesn't correspond to any existing folder
		if (ret==null) {
			showErrorDialog(Translator.get("this_folder_does_not_exist", destPath), Translator.get("download_dialog.error_title"));
			return;
		}

		AbstractFile destFolder = (AbstractFile)ret[0];
		String newName = (String)ret[1];
		
		AbstractFile destFile;
		if(newName!=null)
			destFile = AbstractFile.getAbstractFile(destFolder.getAbsolutePath(true)+newName);
		else {
			// Create destination file
			destFile = AbstractFile.getAbstractFile(destFolder.getAbsolutePath(true)+fileName);
		}
		
		// Starts moving files
		ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("http_download.downloading"));
		com.mucommander.job.HttpDownloadJob downloadJob = new com.mucommander.job.HttpDownloadJob(mainFrame, progressDialog, fileURL, destFile);
		progressDialog.start(downloadJob);
	}
	
}
