
package com.mucommander.job;

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.MainFrame;

import java.util.Vector;
import java.io.IOException;

/**
 * This FileJob calculates the number of files contained in a list of file and folders and
 * computes their size.
 *
 * @author Maxence Bernard
 */
public class PropertiesJob extends FileJob {
    
	/** Number of folders encountered so far */
	private int nbFolders;

	/** Number of regular files (not folders) encountered so far */
	private int nbFilesRecurse;
	
	/** Combined size of all files encountered so far */
	private long totalBytes;
	
	
	public PropertiesJob(Vector files, MainFrame mainFrame) {
		super(mainFrame, files);
	}

	/**
	 * Returns the size in bytes of all the files seen so far.
	 */
	public long getTotalBytes() {
		return totalBytes;
	}

	/**
	 * Returns the number of folders counted so far.
	 */
	public int getNbFolders() {
		return nbFolders;
	}
 
 	/**
 	 * Returns the number of files (folders excluded) counted so far.
 	 */
 	public int getNbFilesRecurse() {
		return nbFilesRecurse;
 	}
 
	/**
	 * Adds the given file to the total of files or folders and the total size,
	 * and recurses if it is a folder.
	 */
	protected boolean processFile(AbstractFile file, Object recurseParams) {
		// Stop if interrupted
		if(isInterrupted())
            return false;

		// If file is a directory, increase folder counter and recurse
		if (file.isDirectory() && !file.isSymlink()) {
			nbFolders++;

			try {
			    AbstractFile subFiles[] = file.ls();
			    for(int i=0; i<subFiles.length && !isInterrupted(); i++)
					processFile(subFiles[i], null);
			}
			catch(IOException e) {
				// Should we tell the user?
			}
		}
		// If not, increase file counter and bytes total
		else {
			nbFilesRecurse++;
			totalBytes += file.getSize();
		}
	
		return true;
	}


	/**
	 * Returns "Calculating" or "Complete".
	 */
	public String getStatusString() {
		return hasFinished()?"Complete":"Calculating";
	}
	
}