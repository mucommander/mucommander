
package com.mucommander.job;

import com.mucommander.file.*;

import java.util.Vector;
import java.io.IOException;

/**
 * This Job calculates the number of files contained in a list of file and folders and
 * computes their size.
 */
public class PropertiesJob extends FileJob implements Runnable {
	private Vector files;

	private int nbFolders;
	private int nbFiles;
	private long totalBytes;
	
	public PropertiesJob(Vector files) {
		super(null);
		
		this.files = files;
	}

    /**
     * Always returns -1.
     */
    public int getFilePercentDone() {
		return -1;
    }

    /**
     * Always returns -1.
     */
    public int getTotalPercentDone() {
		return -1;
    }
	
	/**
	 * Returns "Calculating" or "Complete".
	 */
	public String getCurrentInfo() {
		return hasFinished()?"Complete":"Calculating";
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
 	public int getNbFiles() {
		return nbFiles;
 	}
 
	/**
	 * Adds the given file to the total of files or folders and the total size,
	 * and recurses if it is a folder.
	 */
	private void addRecurse(AbstractFile file) {
		if (file.isFolder() && !(file instanceof ArchiveFile)) {
			nbFolders++;

			try {
			    AbstractFile subFiles[] = file.ls();
			    for(int i=0; i<subFiles.length && !isInterrupted(); i++) {
					addRecurse(subFiles[i]);
			    }
			}
			catch(IOException e) {
				// Should we tell the user?
			}
		}
		else {
			nbFiles++;
			totalBytes += file.getSize();
		}
	}

	public void run() {
		for(int i=0; i<files.size(); i++)
			addRecurse((AbstractFile)files.elementAt(i));
//		System.out.println(totalBytes+" "+getCurrentInfo());

		stop();
	}
}