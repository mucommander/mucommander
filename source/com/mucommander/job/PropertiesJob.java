
package com.mucommander.job;

import com.mucommander.file.*;

import com.mucommander.ui.MainFrame;

import java.util.Vector;

import java.io.IOException;

/**
 * This FileJob calculates the number of files contained in a list of file and folders and
 * computes their size.
 */
public class PropertiesJob extends FileJob implements Runnable {
	private Vector files;
    private int nbFiles;

    private int currentFileIndex;
    
	private int nbFolders;
	private int nbFilesRecurse;
	private long totalBytes;
	
	public PropertiesJob(Vector files, MainFrame mainFrame) {
		super(null, mainFrame);
		
		this.files = files;
        this.nbFiles = files.size();
	}


    public int getNbFiles() {
        return nbFiles;
    }

    public int getCurrentFileIndex() {
        return currentFileIndex;
    }

    public long getTotalBytesProcessed() {
        return -1;
    }

	/**
	 * Returns "Calculating" or "Complete".
	 */
	public String getStatusString() {
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
 	public int getNbFilesRecurse() {
		return nbFilesRecurse;
 	}
 
	/**
	 * Adds the given file to the total of files or folders and the total size,
	 * and recurses if it is a folder.
	 */
	private void addRecurse(AbstractFile file) {
//		if (file.isFolder() && !(file instanceof ArchiveFile)) {
		if (file.isDirectory() && !file.isSymlink()) {
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
			nbFilesRecurse++;
			totalBytes += file.getSize();
		}
	}

	public void run() {
		for(int i=0; i<nbFiles; i++) {
			addRecurse((AbstractFile)files.elementAt(i));
            currentFileIndex++;
        }
//		System.out.println(totalBytes+" "+getStatusString());

		stop();
	}
}