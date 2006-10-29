
package com.mucommander.job;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;
import com.mucommander.file.archiver.Archiver;
import com.mucommander.text.Translator;
import com.mucommander.ui.FileExistsDialog;
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
public class ArchiveJob extends ExtendedFileJob {

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

        if (destFile.exists()) {
            // File already exists in destination: ask the user what to do (cancel, overwrite,...) but
            // do not offer the 'resume' option nor the multiple files mode options such as 'skip'.
            FileExistsDialog dialog = getFileExistsDialog(null, destFile, false);
            int choice = waitForUserResponse(dialog);

            // Cancel or dialog close (return)
            if (choice==-1 || choice==FileExistsDialog.CANCEL_ACTION) {
                stop();
                return;
            }
            // Overwrite file
            else if (choice==FileExistsDialog.OVERWRITE_ACTION) {
                // Do nothing, simply continue
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
        if(isInterrupted())
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
                    for(int i=0; i<subFiles.length && !isInterrupted(); i++) {
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
            catch(IOException e) {
                // If job was interrupted by the user at the time when the exception occurred,
                // it most likely means that the exception by user cancellation.
                // In this case, the exception should not be interpreted as an error.
                if(isInterrupted())
                    return false;

                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("IOException caught: "+e);
                
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
