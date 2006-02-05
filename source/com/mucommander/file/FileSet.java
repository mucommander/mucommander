
package com.mucommander.file;

import java.util.Vector;


/**
 * FileSet is a file vector, with an optional base folder attached which is the parent
 * folder of all contained files.
 *
 * @author Maxence Bernard
 */
public class FileSet extends Vector {

	/** Parent folder of all contained files */
	private AbstractFile baseFolder;
	
	
	/**
	 * Creates a new empty FileSet.
	 */
	public FileSet() {
	}


	/**
	 * Creates a new empty FileSet with the specified base folder.
	 */
	public FileSet(AbstractFile baseFolder) {
		this.baseFolder = baseFolder;
	}

	
	/**
	 * Creates a new empty FileSet with the specified base folder, and adds the given file.
	 */
	public FileSet(AbstractFile baseFolder, AbstractFile file) {
		this.baseFolder = baseFolder;
		add(file);
	}

	
	/**
	 * Returns the base folder associated with this FileSet, null if there isn't any.
	 */
	public AbstractFile getBaseFolder() {
		return baseFolder;
	}
	

	/**
	 * Convenience method to avoid casts to AbstractFile.
	 */
	public AbstractFile fileAt(int i) {
		return (AbstractFile)super.elementAt(i);
	}
}