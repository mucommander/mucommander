
package com.mucommander.job;

import com.mucommander.file.*;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.comp.dialog.YBoxPanel;

import com.mucommander.text.Translator;

import java.io.IOException;

import javax.swing.*;

/**
 * This class is responsible for deleting recursively a group of files.
 *
 * @author Maxence Bernard
 */
public class DeleteJob extends FileJob {
    
	/** Default choice when encountering an existing file */
	private int defaultFileExistsChoice = -1;

	/** Title used for error dialogs */
	private String errorDialogTitle;

	private final static int DELETE_LINK_ACTION = 100;
	private final static int DELETE_FOLDER_ACTION = 101;

	private final static String DELETE_LINK_TEXT = Translator.get("delete.delete_link_only");
	private final static String DELETE_FOLDER_TEXT = Translator.get("delete.delete_linked_folder");

	
    /**
	 * Creates a new DeleteJob without starting it.
	 *
	 * @param progressDialog dialog which shows this job's progress
	 * @param mainFrame mainFrame this job has been triggered by
	 * @param files files which are going to be deleted
	 */
    public DeleteJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files) {
		super(progressDialog, mainFrame, files);

		this.errorDialogTitle = Translator.get("delete_dialog.error_title");
	}

	
	/**
	 * Deletes recursively the given file or folder. 
	 *
	 * @param file the file or folder to delete
	 * @param recurseParams not used
	 * 
	 * @return <code>true</code> if the file has been completely deleted.
	 */
    protected boolean processFile(AbstractFile file, Object recurseParams) {
		String filePath = file.getAbsolutePath();
		filePath = filePath.substring(baseSourceFolder.getAbsolutePath().length()+1, filePath.length());

		if(isInterrupted())
            return false;

		int ret;
		boolean followSymlink = false;
        if(file.isDirectory()) {
			// If folder is a symlink, asks the user what to do
			boolean isSymlink = file.isSymlink();
			if(isSymlink) {
				ret = showSymlinkDialog(filePath, file.getCanonicalPath());
				if(ret==-1 || ret==CANCEL_ACTION) {
					stop();
					return false;
				}
				else if(ret==SKIP_ACTION) {
					return false;
				}
				// Delete file only
				else if(ret==DELETE_FOLDER_ACTION) {
					followSymlink = true;
				}
			}
			
			if(!isSymlink || followSymlink) {
				do {		// Loop for retry
					// Delete each file in this folder
					try {
						AbstractFile subFiles[] = file.ls();
						for(int i=0; i<subFiles.length && !isInterrupted(); i++) {
							// Notify job that we're starting to process this file (needed for recursive calls to processFile)
							nextFile(subFiles[i]);
							processFile(subFiles[i], null);
						}
						break;
					}
					catch(IOException e) {
						if(com.mucommander.Debug.ON) e.printStackTrace();

//						ret = showErrorDialog(errorDialogTitle, "Unable to read contents of folder "+filePath);
						ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_read_source", filePath));
						// Retry loops
						if(ret==RETRY_ACTION)
							continue;
						// Cancel, skip or close dialog returns false
						return false;
					}
				} while(true);
			}
        }
        
        if(isInterrupted())
            return false;

		do {		// Loop for retry
			try {
				// If file is a symlink to a folder and the user asked to follow the symlink,
				// delete the empty folder
				if(followSymlink) {
					AbstractFile canonicalFile = AbstractFile.getAbstractFile(file.getCanonicalPath());
					if(canonicalFile!=null)
						canonicalFile.delete();
				}
	
				file.delete();
				return true;
			}
			catch(IOException e) {
				if(com.mucommander.Debug.ON) e.printStackTrace();
	
//				ret = showErrorDialog(errorDialogTitle, "Unable to delete "
//					+(file.isDirectory()?"folder ":"file ")
//					+file.getName());
				ret = showErrorDialog(errorDialogTitle,
					Translator.get(file.isDirectory()?"cannot_delete_folder":"cannot_delete_file", file.getName())
				);
				// Retry loops
				if(ret==RETRY_ACTION)
					continue;
				// Cancel, skip or close dialog returns false
				return false;
			}
		} while(true);
    }

	
	private int showSymlinkDialog(String relativePath, String canonicalPath) {
		YBoxPanel panel = new YBoxPanel();

//		panel.add(new JLabel("This file looks like a symbolic link."));
//		panel.add(new JLabel(" "));
//		panel.add(new JLabel("  File: "+relativePath));
//		panel.add(new JLabel("  Links to: "+canonicalPath));
//		panel.add(new JLabel(" "));
//		panel.add(new JLabel("Delete symlink only (safe	) or "));
//		panel.add(new JLabel("Follow symlink and delete folder (CAUTION) ?"));

		JTextArea symlinkWarningArea = new JTextArea(Translator.get("delete.symlink_warning", relativePath, canonicalPath));
		symlinkWarningArea.setEditable(false);
		panel.add(symlinkWarningArea);
		
		QuestionDialog dialog = new QuestionDialog(progressDialog, Translator.get("delete.symlink_warning_title"), panel, mainFrame,
			new String[] {DELETE_LINK_TEXT, DELETE_FOLDER_TEXT, SKIP_TEXT, CANCEL_TEXT},
			new int[]  {DELETE_LINK_ACTION, DELETE_FOLDER_ACTION, SKIP_ACTION, CANCEL_ACTION},
			2);
	
	    return waitForUserResponse(dialog);
	}
	

    public String getStatusString() {
        return Translator.get("delete.deleting_file", getCurrentFileInfo());
    }

	// This job modifies baseFolder and subfolders
	protected boolean hasFolderChanged(AbstractFile folder) {
		return baseSourceFolder.isParent(folder);
	}
}	
