
package com.mucommander.job;

import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.MainFrame;

import com.mucommander.file.AbstractFile;

import java.io.*;


/**
 * ExtendedFileJob is a container for a 'file task' : basically an operation that involves files and bytes.<br>
 * <p>What makes it different from FileJob is that the class implementing ExtendedFileJob has to be able to give
 * information about the file currently being processed.</p>
 * 
 * @author Maxence Bernard
 */
public abstract class ExtendedFileJob extends FileJob {

    /** Number of bytes of current file that have been processed, see {@link #getCurrentFileBytesProcessed() getCurrentFileBytesProcessed} */
    protected long currentFileProcessed;

	/** Read buffer */
	protected byte buffer[];

	/** Size that should be allocated to read buffer */
	protected final static int READ_BLOCK_SIZE = 8192;

	
    public ExtendedFileJob(ProgressDialog progressDialog, MainFrame mainFrame) {
        super(progressDialog, mainFrame);
    }


	/**
	 * Copies the given InputStream's content to the given OutputStream, skipping the specified number
	 * of bytes (can be 0) from the InputStream.
	 */
	protected void copyStream(InputStream in, OutputStream out, long skipBytes) throws IOException {
		// Init read buffer the first time
		if(buffer==null)
			buffer = new byte[READ_BLOCK_SIZE];
		
		// Skip/do not read a number of bytes from the input stream
		if(skipBytes>0) {
			in.skip(skipBytes);
			nbBytesProcessed += skipBytes;
			currentFileProcessed += skipBytes;
			nbBytesSkipped += skipBytes;
		}
		
		// Copies the InputStream's content to the OutputStream
		int read;
		while ((read=in.read(buffer, 0, buffer.length))!=-1 && !isInterrupted()) {
			out.write(buffer, 0, read);
			nbBytesProcessed += read;
			currentFileProcessed += read;
		}
	}
	
	
	/**
	 * Copies the given source file to the specified destination file, resuming 
	 */
	protected void copyFile(AbstractFile sourceFile, AbstractFile destFile, boolean resume) throws FileJobException {
		OutputStream out = null;
		InputStream in = null;
		long bytesSkipped;

		try {
			// Try to open InputStream
			try  { in = sourceFile.getInputStream(); }
			catch(IOException e1) {
				throw new FileJobException(FileJobException.CANNOT_OPEN_SOURCE);
			}
	
			// Try to open OutputStream
			try  { out = destFile.getOutputStream(resume); }
			catch(IOException e2) {
				throw new FileJobException(FileJobException.CANNOT_OPEN_DESTINATION);
			}
	
			// Try to copy InputStream to OutputStream
			try  { copyStream(in, out, resume?sourceFile.getSize():0); }
			catch(IOException e3) {
				throw new FileJobException(FileJobException.ERROR_WHILE_TRANSFERRING);
			}
		}
		catch(FileJobException e) {
			// Rethrow exception 
			throw e;
		}
		finally {
			// Tries to close the streams no matter what happened before
			// This block is always executed, even if an exception
			// is thrown by the catch block
			if(in!=null)
				try { in.close(); }
				catch(IOException e1) {}
			if(out!=null)
				try { out.close(); }
				catch(IOException e2) {}
		}
		
	}


    /**
     * Computes and returns the percent done of current file. Returns 0 if current file's size is not available
     * (getNbCurrentFileBytesProcessed() returns -1).
     */
    public int getFilePercentDone() {
        long currentFileSize = getCurrentFileSize();
        if(currentFileSize<=0)
            return 0;
        else
            return (int)(100*getCurrentFileBytesProcessed()/(float)currentFileSize);

    }

    
    /**
     * Returns the number of bytes of the current file that have been processed.
     */
    public long getCurrentFileBytesProcessed() {
		return currentFileProcessed;
	}


    /**
     * Returns current file's size, -1 if is not available.
     */
    public abstract long getCurrentFileSize();

	
	/**
	 * Advances file index and resets file bytes counter. This method should be called by subclasses whenever the job
	 * starts processing a new file.
	 */
	protected void nextFile() {
		super.nextFile();
		currentFileProcessed = 0;
	}


    /**
     * Overrides this method to returns a more accurate percent value of the job processed so far, taking
     * into account current file's percent.
     */

/*
    public int getTotalPercentDone() {
        float nbFilesProcessed = getCurrentFileIndex();
        long currentFileSize = getCurrentFileSize();
        if(currentFileSize>0)
            nbFilesProcessed += getCurrentFileBytesProcessed()/(float)currentFileSize;

//System.out.println(nbFilesProcessed+" "+currentFileSize+" "+((int)(100*(nbFilesProcessed/(float)getNbFiles()))));

        return (int)(100*(nbFilesProcessed/(float)getNbFiles()));
    }
*/
    
}