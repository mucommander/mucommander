package com.mucommander.file;

import java.io.*;
import java.util.Vector;

public abstract class AbstractFile {

	/**
	 * Creates a new instance of AbstractFile.
	 */
	public AbstractFile() {
	}
	
	/**
	 * Returns an instance of an AbstractFile for the given ABSOLUTE path.
	 * This method will return an instance of the correct AbstractFile class.
	 * It will return <code>null</code> if the given path is not absolute or incorrect.
	 *
	 */
	public static AbstractFile getAbstractFile(String absPath) {
		AbstractFile file;

		// SMB file
		if (absPath.toLowerCase().startsWith("smb://")) {
//			try {
			file = new SMBFile(absPath);
//			}
//			catch(IOException e) {
//				return null;
//			}
		}
		// HTML file
		else if (absPath.toLowerCase().startsWith("http://")) {
			try {
				file = new HTMLFile(absPath);
			}
			catch(IOException e) {
				return null;
			}
		}
		// FS file, tests if the given path is indeed absolute
		else if (new File(absPath).isAbsolute()) {
			file = new FSFile(absPath);
		}
		else
			return null;

		String name = file.getName();
//System.out.println("getAbstractFile "+absPath);
		if(name!=null && !file.isFolder() && (name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".jar")))
			return new ZipArchiveFile(file);
		
		return file;		
	}
	
	/**
	 * <p>Tests a file for equality: returns <code>true</code> if the given file denotes the same
	 * file or directory. Note that two files can be equal and not have the exact same absolute
	 * path.</p>
	 *
	 * <p>This method should be overriden as it only compares the absolute path.</p>
	 */
	public boolean equals(AbstractFile f) {
		if(f==null)
			return false;
		
		return getAbsolutePath().equals(((AbstractFile)f).getAbsolutePath());
	}
	
	/**
	 * Returns the name of this AbstractFile.
	 */
	public abstract String getName();

	/**
	 * Returns the absolute path of this AbstractFile.
	 */
	public abstract String getAbsolutePath();
	
	/**
	 * Returns the absolute path of this AbstractFile, appending a separator if <code>true</code>
	 * is passed. 
	 */
	public String getAbsolutePath(boolean appendSeparator) {
		return getAbsolutePath()+getSeparator();
	}

	/**
	 * Returns the separator character for this kind of AbstractFile.
	 */
	public abstract String getSeparator();
	
	/**
	 * Returns a date associated with this AbstractFile.
	 */
	public abstract long getDate();
	
	/**
	 * Returns the size in bytes of this AbstractFile.
	 */
	public abstract long getSize();
	
	/**
	 * Returns this AbstractFile's parent or null if it is root.
	 */
	public abstract AbstractFile getParent();
	
	/**
	 * Returns <code>true</code> if this file exists.
	 */
	public abstract boolean exists();
	
	/**
	 * Returns true if this AbstractFile can be read.
	 */	
	public abstract boolean canRead();
	
	/**
	 * Returns true if this AbstractFile can be modified.
	 */	
	public abstract boolean canWrite();
	
	/**
	 * Returns true if this AbstractFile is hidden.
	 */	
	public abstract boolean isHidden();

	/**
	 * Returns true if this AbstractFile is a folder of some sort (not necessarely a directory, can be an archive file...),.
	 */
	public abstract boolean isFolder();

	/**
	 * Returns the contents of this AbstractFile is a folder.
	 * @throws an IOException if this operation is not possible.
	 */
	public abstract AbstractFile[] ls() throws IOException;

	/**
	 * Creates a new directory if this AbstractFile is a folder.
	 * @throws an IOException if this operation is not possible.
	 */
	public abstract void mkdir(String name) throws IOException;

	/**
	 * Returns an InputStream to read from this AbstractFile.
	 * @throw IOException if this AbstractFile cannot be read or is a folder.
	 */
	public abstract InputStream getInputStream() throws IOException;

	/**
	 * Returns an OuputStream to write to this AbstractFile.
	 * @param append if true, data will be appended to the end of this file.
	 * @throw IOException if this operation is not permitted or if this AbstractFile 
	 * is a folder.
	 */
	public abstract OutputStream getOutputStream(boolean append) throws IOException;

//	/**
//	 * Close any resource associated with this AbstractFile.
//	 * This method should always be called when this AbstractFile is not used anymore.
//	 */
//	public abstract void close() throws IOException;

	/**
	 * Moves this AbstractFile to another one. This method will return true if the operation 
	 * can/has been completed. If not, the operation will need to be performed using the source
	 * InputStream, destination OutputStream and the delete method.
	 * <p>
	 * For now, the operation will be performed only when source and destination are both FSFiles,
	 * i.e. using the underlying File.renameTo() method.
	 * </p>
	 * @throw IOException is this AbstractFile cannot be written.
	 */
	public abstract boolean moveTo(AbstractFile dest) throws IOException;

	/**
	 * Deletes this AbstractFile and this one only (does not recurse), throws an IOException
	 * if it failed.
	 * @throw IOException if this AbstractFile cannot be written.
	 */	
	public abstract void delete() throws IOException;
}