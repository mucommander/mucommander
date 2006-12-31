
package com.mucommander.job;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;
import com.mucommander.file.archiver.Archiver;
import com.mucommander.text.Translator;
import com.mucommander.ui.FileCollisionDialog;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * This FileJob is responsible for compressing a set of files into an archive file.
 *
 * @author Maxence Bernard
 */
public class ArchiveJob extends TransferFileJob {

    /** Destination archive file */
    private AbstractFile destFile;

    /** Base destination folder's path */
    private String baseFolderPath;

    /** Archiver instance that does the actual archiving */
    private Archiver archiver;

    /** Archive format */
    private int archiveFormat;
	
    /** Optional archive comment */
    private String archiveComment;
	
    /** Size of the buffer used to write archived data */
    private final static int WRITE_BUFFER_SIZE = 8192;
    
    /** InputStream of the file currently being read */
    private InputStream in;


    public ArchiveJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files, AbstractFile destFile, int archiveFormat, String archiveComment) {
        super(progressDialog, mainFrame, files);
		
        this.destFile = destFile;
        this.archiveFormat = archiveFormat;
        this.archiveComment = archiveComment;

        this.baseFolderPath = baseSourceFolder.getAbsolutePath(false);
    }

	
    /**
     * Overriden method to initialize the archiver and handle the case where the destination file already exists.
     */
    protected void jobStarted() {
        super.jobStarted();

        // Check for file collisions, i.e. if the file already exists in the destination
        int collision = FileCollisionChecker.checkForCollision(null, destFile);
        if(collision!=FileCollisionChecker.NO_COLLOSION) {
            // File already exists in destination, ask the user what to do (cancel, overwrite,...) but
            // do not offer the multiple files mode options such as 'skip' and 'apply to all'.
            int choice = waitForUserResponse(new FileCollisionDialog(progressDialog, mainFrame, collision, null, destFile, false));

            // Overwrite file
            if (choice== FileCollisionDialog.OVERWRITE_ACTION) {
                // Do nothing, simply continue and file will be overwritten
            }
            // Cancel or dialog close (return)
//            else if (choice==-1 || choice== FileCollisionDialog.CANCEL_ACTION) {
            else {
                interrupt();
                return;
            }
        }

        // Loop for retry
        do {
            try {
                // Tries to open destination file and create Archiver
                this.archiver = Archiver.getArchiver(new BufferedOutputStream(destFile.getOutputStream(false), WRITE_BUFFER_SIZE), archiveFormat);
                this.archiver.setComment(archiveComment);
                break;
            }
            catch(Exception e) {
                int choice = showErrorDialog(Translator.get("warning"),
                                             Translator.get("cannot_write_file", destFile.getName()),
                                             new String[] {CANCEL_TEXT, RETRY_TEXT},
                                             new int[]  {CANCEL_ACTION, RETRY_ACTION}
                                             );
			
                // Retry loops
                if(choice == RETRY_ACTION)
                    continue;
                // Cancel or close dialog returns false
                return;
            }
        } while(true);
    }
	
	
    protected boolean processFile(AbstractFile file, Object recurseParams) {
        if(getState()==INTERRUPTED)
            return false;

        String filePath = file.getAbsolutePath(false);
        String entryRelativePath = filePath.substring(baseFolderPath.length()+1, filePath.length());

        // Process current file
        do {		// Loop for retry
            try {
                if (file.isDirectory() && !file.isSymlink()) {
                    // Create new directory entry in archive file
                    archiver.createEntry(entryRelativePath, file);
					
                    // Recurse on files
                    AbstractFile subFiles[] = file.ls();
                    boolean folderComplete = true;
                    for(int i=0; i<subFiles.length && getState()!=INTERRUPTED; i++) {
                        // Notify job that we're starting to process this file (needed for recursive calls to processFile)
                        nextFile(subFiles[i]);
                        if(!processFile(subFiles[i], null))
                            folderComplete = false;
                    }
					
                    return folderComplete;
                }
                else {
//                    this.in = new CounterInputStream(file.getInputStream(), currentFileByteCounter);
                    this.in = setCurrentInputStream(file.getInputStream());

                    // Create a new file entry in archive and copy the current file
                    AbstractFile.copyStream(in, archiver.createEntry(entryRelativePath, file));

                    in.close();
                
                    return true;
                }
            }
            // Catch Exception rather than IOException as ZipOutputStream has been seen throwing NullPointerException
            catch(Exception e) {
                // If job was interrupted by the user at the time when the exception occurred,
                // it most likely means that the exception by user cancellation.
                // In this case, the exception should not be interpreted as an error.
                if(getState()==INTERRUPTED)
                    return false;

                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Exception caught: "+e);
                
                int ret = showErrorDialog(Translator.get("pack_dialog.error_title"), Translator.get("error_while_transferring", file.getAbsolutePath()));
                // Retry loops
                if(ret==RETRY_ACTION) {
                    // Reset processed bytes currentFileByteCounter
//                    currentFileByteCounter.reset();
                    getCurrentFileByteCounter().reset();

                    continue;
                }
                // Cancel, skip or close dialog return false
                return false;
            }
        } while(true);
    }

	
    /**
     * Overriden method to close the archiver.
     */
    public void jobStopped() {
        super.jobStopped();
    
//        // First, close any open InputStream being archived.
//        // Not doing so before closing the archive would cause a deadlock in ZipOutputStream
//        if(in!=null) {
//            try { in.close(); }
//            catch(IOException e) {}
//        }
        
        // Try to close the archiver
        if(archiver!=null) {
            try { archiver.close(); }
            catch(IOException e) {}
        }
    }


    public String getStatusString() {
        return Translator.get("pack_dialog.packing_file", getCurrentFileInfo());
    }

    protected boolean hasFolderChanged(AbstractFile folder) {
        // This job modifies baseFolder
        return destFile.getParent().equals(folder);
    }
}	
