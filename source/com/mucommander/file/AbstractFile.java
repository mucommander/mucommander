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
		try {
			AbstractFile file;
			String absPathLC = absPath.toLowerCase();
			
			// SMB file
			if (absPathLC.startsWith("smb://"))
				file = new SMBFile(absPath);
			// HTTP file
			else if (absPathLC.startsWith("http://"))
				file = new HTTPFile(absPath);
			// FTP file
			else if (absPath.toLowerCase().startsWith("ftp://"))
				file = new FTPFile(absPath);
			// FS file, test if the given path is indeed absolute
			else if (new File(absPath).isAbsolute())
				file = new FSFile(absPath);
			else
				return null;

			if(parent!=null)
				file.setParent(parent);
		
			return wrapArchive(file);
		}
		catch(IOException e) {
			if(com.mucommander.Debug.ON) {
				System.out.println(e);
				e.printStackTrace();
			}
			return null;
		}
	}

	
	/**
	 * Tests if given file is an archive and if it is, create the appropriate archive file
	 * on top of the base file object.
	 */
	static AbstractFile wrapArchive(AbstractFile file) {
        String name = file.getName();
		if(name!=null && !file.isDirectory() && (name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".jar")))
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
	public boolean equals(Object f) {
		if(f==null || !(f instanceof AbstractFile))
			return false;
		
		return getAbsolutePath().equals(((AbstractFile)f).getAbsolutePath());
	}

	
	/**
	 * Returns a String representation of this AbstractFile which is the name as returned by getName().
	 */
	public String toString() {
		return getName();
	}
	
	
	/**
	 * Returns the name of this AbstractFile.
	 */
	public abstract String getName();

	
	/**
	 * Returns the absolute path of this AbstractFile, without a trailing separator, except for root folders ('/', 'c:\' ...).
	 */
	public abstract String getAbsolutePath();
	
	/**
	 * Returns the absolute path of this AbstractFile, appending a separator character if <code>true</code> is passed. 
	 */
	public String getAbsolutePath(boolean appendSeparator) {
		String path = getAbsolutePath();
		return appendSeparator?addTrailingSeparator(path):path;
	}

	
	/**
	 * Returns the canonical path of this AbstractFile, resolving any symbolic links or '..' and '.' occurrences, without a trailing separator.
	 * AbstractFile's implementation simply returns the absolute path, this method should be overridden if canonical path resolution is available.
	 */
	public String getCanonicalPath() {
		return getAbsolutePath();
	}

	/**
	 * Returns the canonical path of this AbstractFile, resolving any symbolic links or '..' and '.' occurrences,
	 * appending a separator character if <code>true</code> is passed.
	 * AbstractFile's implementation simply returns the absolute path, this method should be overridden if canonical path resolution is available.
	 */
	public String getCanonicalPath(boolean appendSeparator) {
		String path = getCanonicalPath();
		return appendSeparator?addTrailingSeparator(path):path;
	}

	
	protected String addTrailingSeparator(String path) {
		// Even though getAbsolutePath() is not supposed to return a trailing separator, root folders ('/', 'c:\' ...)
		// are exceptions that's why we still have to test if path ends with a separator
		String separator = getSeparator();
		if(!path.endsWith(separator))
			return path+separator;
		return path;
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
	 * Returns an InputStream to read from this AbstractFile, skipping the
	 * specified number of bytes. This method should be overridden whenever
	 * possible to provide a more efficient implementation, as this implementation
	 * simply use {@link #InputStream.skip(long) InputStream.skip()}
	 * which *reads* bytes and discards them, which is bad (think of an ISO file on a remote server).
	 *
	 * @throw IOException if this AbstractFile cannot be read or is a folder.
	 */
	public InputStream getInputStream(long skipBytes) throws IOException {
		InputStream in = getInputStream();
		
		// Call InputStream.skip() until the specified number of bytes have been skipped
		long nbSkipped = 0;
		long n;
		while(nbSkipped<skipBytes) {
			n = in.skip(skipBytes-nbSkipped);
			if(n>0)
				nbSkipped += n;
		}

		return in;
	}
	
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