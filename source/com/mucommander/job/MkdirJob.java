
package com.mucommander.job;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileSet;
import com.mucommander.text.Translator;
import com.mucommander.ui.FileExistsDialog;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;


/**
 * This FileJob creates a new directory in a given folder.
 *
 * @author Maxence Bernard
 */
public class MkdirJob extends FileJob {
	
    private AbstractFile destFolder;
    private String filename;
	private boolean mkfileMode;


    /**
     * Creates a new Mkdir/Mkfile job.
     *
     * @param mkfileMode if true, this job will operate in 'mkfile' mode, if false in 'mkdir' mode
     */
    public MkdirJob(MainFrame mainFrame, FileSet fileSet, String filename, boolean mkfileMode) {
        super(mainFrame, fileSet);

        this.destFolder = fileSet.getBaseFolder();
        this.filename = filename;
        this.mkfileMode = mkfileMode;
		
        setAutoUnmark(false);
    }


    /////////////////////////////////////
    // Abstract methods Implementation //
    /////////////////////////////////////

    /**
     * Creates the new directory in the destination folder.
     */
    protected boolean processFile(AbstractFile file, Object recurseParams) {
        // Stop if interrupted (although there is no way to stop the job at this time)
        if(isInterrupted())
            return false;

        do {
            try {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Creating "+destFolder+" "+ filename);

                AbstractFile newFile = FileFactory.getFile(destFolder.getAbsolutePath(true)+ filename);

                // Create file
                if(mkfileMode) {
                    if(newFile.exists()) {
                        // File already exists in destination (directory case already tested):
                        // ask the user what to do (cancel, overwrite,...) but
                        // do not offer the 'resume' option nor the multiple files mode options such as 'skip'.
                        FileExistsDialog dialog = getFileExistsDialog(null, newFile, false);
                        int choice = waitForUserResponse(dialog);

                        // Cancel or dialog close (return)
                        if (choice==-1 || choice==FileExistsDialog.CANCEL_ACTION) {
                            stop();
                            return false;
                        }
                        // Overwrite file
                        else if (choice==FileExistsDialog.OVERWRITE_ACTION) {
                            // Do nothing, simply continue
                        }
                    }

                    OutputStream out = newFile.getOutputStream(false);
                    out.close();
                }
                // Create directory
                else {
                    // If a regular file already exists (directory case already tested), report the error and stop
                    if(newFile.exists()) {
                        JOptionPane.showMessageDialog(mainFrame, Translator.get("file_already_exists", newFile.getAbsolutePath(false)), Translator.get("error"), JOptionPane.ERROR_MESSAGE);
                        stop();
                        return false;
                    }

                    destFolder.mkdir(filename);
                }

                // Refresh and selects newly created folder in active table
                FileTable table1 = mainFrame.getFolderPanel1().getFileTable();
                FileTable table2 = mainFrame.getFolderPanel2().getFileTable();
                FileTable lastActiveTable = mainFrame.getActiveTable();
                for(FileTable table=table1; table!=null; table=table==table1?table2:null) {
                    if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Determining if folders need to be refreshed, tableFolder="+table.getCurrentFolder().getAbsolutePath()+" destFolder="+destFolder.getAbsolutePath());
                    if(destFolder.equals(table.getCurrentFolder())) {
                        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Refreshing tableFolder="+table.getCurrentFolder().getAbsolutePath());
                        // Refresh folder panel in a separate thread
                        table.getFolderPanel().tryRefreshCurrentFolder(lastActiveTable==table?newFile:null);
                    }
                }
				
                return true;		// Return Success
            }
            catch(IOException e) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("IOException caught: "+e);

                int action = showErrorDialog(
                     Translator.get("error"),
                     Translator.get(mkfileMode?"cannot_write_file":"cannot_create_folder", destFolder.getAbsolutePath(true)+ filename),
                     new String[]{RETRY_TEXT, CANCEL_TEXT},
                     new int[]{RETRY_ACTION, CANCEL_ACTION}
                );
                // Retry (loop)
                if(action==RETRY_ACTION)
                    continue;
				
                // Cancel action
                return false;		// Return Failure
            }    
        }
        while(true);
    }

    /**
     * Not used.
     */
    public String getStatusString() {
        return null;
    }

    protected boolean hasFolderChanged(AbstractFile folder) {
        // Refresh is done manually
        return false;
    }
}
