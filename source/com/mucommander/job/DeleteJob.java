
package com.mucommander.job;

import com.mucommander.file.*;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.comp.dialog.YBoxPanel;

import com.mucommander.text.Translator;

import java.io.IOException;
import java.util.Vector;

import javax.swing.JLabel;

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

	private String baseFolderPath;	
	
	private final static int DELETE_LINK_ACTION = 0;
	private final static int DELETE_FOLDER_ACTION = 1;
	private final static int CANCEL_ACTION = 2;
	private final static int SKIP_ACTION = 3;

	private final static String DELETE_LINK_TEXT = "Delete Link only";
	private final static String DELETE_FOLDER_TEXT = "Delete Folder";
	private final static String CANCEL_TEXT = "Cancel";
	private final static String SKIP_TEXT = "Skip";

	
    /**
	 * Creates a new DeleteJob without starting it.
	 *
	 * @param progressDialog dialog which shows this job's progress
	 * @param mainFrame mainFrame this job has been triggered by
	 * @param files files which are going to be deleted
	 */
    public DeleteJob(ProgressDialog progressDialog, MainFrame mainFrame, Vector files) {
		super(progressDialog, mainFrame, files);

		this.baseFolderPath = ((AbstractFile)files.elementAt(0)).getParent().getAbsolutePath();
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
		filePath = filePath.substring(baseFolderPath.length()+1, filePath.length());

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
						for(int i=0; i<subFiles.length && !isInterrupted(); i++)
							processFile(subFiles[i], null);
					}
					catch(IOException e) {
						if(com.mucommander.Debug.ON) e.printStackTrace();

						ret = showErrorDialog(errorDialogTitle, "Unable to read contents of folder "+filePath);
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
	
				ret = showErrorDialog(errorDialogTitle, "Unable to delete "
					+(file.isDirectory()?"folder ":"file ")
					+file.getName());
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
		panel.add(new JLabel("This file looks like a symbolic link."));
		panel.add(new JLabel(" "));
		panel.add(new JLabel("Delete symlink only (safe) or"));
		panel.add(new JLabel("follow symlink and delete folder (caution) ?"));
		panel.add(new JLabel(" "));
		panel.add(new JLabel("  File: "+relativePath));
		panel.add(new JLabel("  Links to: "+canonicalPath));
		
		QuestionDialog dialog = new QuestionDialog(progressDialog, "Symlink found", panel, mainFrame,
			new String[] {DELETE_LINK_TEXT, DELETE_FOLDER_TEXT, SKIP_TEXT, CANCEL_TEXT},
			new int[]  {DELETE_LINK_ACTION, DELETE_FOLDER_ACTION, SKIP_ACTION, CANCEL_ACTION},
			2);
	
	    return waitForUserResponse(dialog);
	}
	

    public String getStatusString() {
        return Translator.get("delete.deleting_file", getCurrentFileInfo());
    }

}	
