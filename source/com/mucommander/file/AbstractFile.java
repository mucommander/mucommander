package com.mucommander.file;

import java.io.*;
import java.util.Vector;

public abstract class AbstractFile {


	
	protected abstract void setParent(AbstractFile parent);
	
	
	/**
	 * Returns an instance of AbstractFile for the given absolute path.
	 *
	 * @return <code>null</code> if the given path is not absolute or incorrect (doesn't correspond to any file).
	 */
	public static AbstractFile getAbstractFile(String absPath) {
		return getAbstractFile(absPath, null);
	}



	/**
	 * Returns an instance of AbstractFile for the given absolute path and sets the giving parent. AbstractFile subclasses should
	 * call this method rather than getAbstractFile(String) because it is more efficient.
	 *
	 * @param parent the returned file's parent
	 * @return <code>null</code> if the given path is not absolute or incorrect (doesn't correspond to any file).
	 */
	protected static AbstractFile getAbstractFile(String absPath, AbstractFile parent) {
		AbstractFile file;

		// SMB file
		if (absPath.toLowerCase().startsWith("smb://")) {
			try {
				// Patch for jcifs 0.8.0b (path has to end with /)
//				file = new SMBFile(absPath.endsWith("/")?absPath:absPath+"/");
				file = new SMBFile(absPath);
			}
			catch(IOException e) {
				return null;
			}
		}
/*		// HTML file
		else if (absPath.toLowerCase().startsWith("http://")) {
			try {
				file = new HTMLFile(absPath);
			}
			catch(IOException e) {
				return null;
			}
		}
*/
/*        else if (absPath.toLowerCase().startsWith("ftp://")) {
            try {
                file = new FTPFile(absPath);
            }
            catch(IOException e) {
                if(com.mucommander.Debug.TRACE) {
                    System.out.println(e);
                    e.printStackTrace();
                }
                return null;
            }
        }
*/
        // FS file, tests if the given path is indeed absolute
		else if (new File(absPath).isAbsolute()) {
			file = new FSFile(absPath);
		}
		else {
			return null;
        }
		
        String name = file.getName();
//		if(name!=null && !file.isFolder() && (name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".jar")))
		if(name!=null && !file.isDirectory() && (name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".jar")))
			return new ZipArchiveFile(file);
		
		if(parent!=null)
			file.setParent(parent);
		
		return file;		
	}


    
	/**
	 * <p>Tests a file for equality: returns <code>true</code> if the given file denotes the same
	 * file or directory. Note that two files can be equal and not have the exact same absolute
	 * path.</p>
	 *
	 * <p>This method should be overriden as it only compares the absolute path.</p>
	 */
	public boolean equals(Object f) {
		if(f==null || !(f instanceof AbstractFile))
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

	
	public String getCanonicalPath() {
		return getAbsolutePath();
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
	 * Returns true if this AbstractFile is a 'regular' directory, not only a 'browsable' file (like an archive file).
	 */
	public abstract boolean isDirectory();

	/**
	 * Returns true if this AbstractFile can be browsed (entered): true for directories and supported archive files.
	 */
	public boolean isBrowsable() {
		return isDirectory() || (this instanceof ArchiveFile);
	}

	/**
	 * Returns true if this file *may* be a symbolic link and thus handled with care.
	 */
	public boolean isSymlink() {
		return false;
	}
	
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