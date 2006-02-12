package com.mucommander.file;

import com.mucommander.cache.LRUCache;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;


/**
 *
 *
 * @author Maxence Bernard
 */
public abstract class AbstractFile {

	/** URL representing this file */
	protected FileURL fileURL;

	/** Static LRUCache instance that caches frequently accessed files */
	private static LRUCache fileCache = new LRUCache(1000);
	/** Static LRUCache instance that caches frequently accessed file urls */
	private static LRUCache urlCache = new LRUCache(1000);

	/**
	 * Creates a new file instance with the given URL.
	 */
	protected AbstractFile(FileURL url) {
		this.fileURL = url;
	}
	

	/**
	 * Returns an instance of AbstractFile for the given absolute path.
	 * 
	 * <p>This method does not throw any IOException but returns <code>null</code> if the file could not be created.</p>
	 *
	 * @param absPath the absolute path to the file
	 *
	 * @return <code>null</code> if the given path is not absolute or incorrect (doesn't correspond to any file) or 
	 * if something went wrong during file creation.
	 */
	public static AbstractFile getAbstractFile(String absPath) {
		try {
			return getAbstractFile(absPath, null);
		}
		catch(IOException e) {
if(com.mucommander.Debug.ON) e.printStackTrace();
			return null;
		}
	}


	/**
	 * Returns an instance of AbstractFile for the given absolute path.
	 * 
	 * <p>This method does not throw any IOException but returns <code>null</code> if the file could not be created.</p>
	 *
	 * @param absPath the absolute path to the file
	 * @param throwException if set to <code>true</code>, an IOException will be thrown if something went wrong during file creation
	 *
	 * @return <code>null</code> if the given path is not absolute or incorrect (doesn't correspond to any file) 
	 * @throws java.io.IOException  and throwException param was set to <code>true</code>.
	 */
	public static AbstractFile getAbstractFile(String absPath, boolean throwException) throws AuthException, IOException {
		try {
			return getAbstractFile(absPath, null);
		}
		catch(IOException e) {
			if(com.mucommander.Debug.ON) e.printStackTrace();
			if(throwException)
				throw e;
			return null;
		}
	}

	
	/**
	 * Returns an instance of AbstractFile for the given absolute path and sets the giving parent if not null. AbstractFile subclasses should
	 * call this method rather than getAbstractFile(String) because it is more efficient.
	 *
	 * @param absPath the absolute path to the file
	 * @param parent the returned file's parent
	 *
	 * @throws java.io.IOException if something went wrong during file or file url creation.
	 */
	protected static AbstractFile getAbstractFile(String absPath, AbstractFile parent) throws AuthException, IOException {
//		// Remove trailing slash if path is not '/' or trailing backslash if path does not end with ':\' 
//		// (Reminder: C: is C's current folder, while C:\ is C's root)
//		if((path.endsWith("/") && path.length()>1) || (path.endsWith("\\") && path.charAt(path.length()-2)!=':'))
//			path = path.substring(0, path.length()-1);

		// Create a FileURL instance using the given path
		FileURL fileURL;

		// If path contains no protocol, consider the file as a local file and add the 'file' protocol to the URL.
		// Frequently used local FileURL instances are cached for performance  
		if(absPath.indexOf("://")==-1) {
			// Try to find a cached FileURL instance
			fileURL = (FileURL)urlCache.get(absPath);
//			if(com.mucommander.Debug.ON) com.mucommander.Debug.trace((fileURL==null?"Adding to FileURL cache:":"FileURL cache hit: ")+absPath);
			if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("url cache hits/misses: "+urlCache.getNbHits()+"/"+urlCache.getNbMisses());

			if(fileURL==null) {
				// No cached value found, create the FileURL and add it to the FileURL cache 
				fileURL = new FileURL("file://localhost"+((absPath.equals("")||(absPath.charAt(0)=='/'))?absPath:'/'+absPath));
				urlCache.add(absPath, fileURL);
			}
		}
		else {
			// FileURL cache is not used for now as FileURL are mutable (setLogin, setPassword, setPort) and it
			// may cause some weird side effects
			fileURL = new FileURL(absPath);
		}
		
		return getAbstractFile(fileURL, parent);
	}
	

	/**
	 * Returns an instance of AbstractFile for the given FileURL instance.
	 *
	 * @param fileURL the file URL
	 *
	 * @return the created file or null if something went wrong during file creation 
	 */
	public static AbstractFile getAbstractFile(FileURL fileURL) {
		try {
			return getAbstractFile(fileURL, null);
		}
		catch(IOException e) {
if(com.mucommander.Debug.ON) e.printStackTrace();
			return null;
		}
	}


	/**
	 * Returns an instance of AbstractFile for the given FileURL instance.
	 *
	 * @param fileURL the file URL
	 * @param throwException if set to <code>true</code>, an IOException will be thrown if something went wrong during file creation
	 *
	 * @return the created file
	 * @throws java.io.IOException if something went wrong during file creation
	 */
	public static AbstractFile getAbstractFile(FileURL fileURL, boolean throwException) throws IOException {
		try {
			return getAbstractFile(fileURL, null);
		}
		catch(IOException e) {
			if(com.mucommander.Debug.ON) e.printStackTrace();
			if(throwException)
				throw e;
			return null;
		}
	}


	
	/**
	 * Returns an instance of AbstractFile for the given FileURL instance and sets the giving parent.
	 *
	 * @param fileURL the file URL
	 * @param parent the returned file's parent
	 *
	 * @throws java.io.IOException if something went wrong during file creation.
	 */
	public static AbstractFile getAbstractFile(FileURL fileURL, AbstractFile parent) throws IOException {
		String protocol = fileURL.getProtocol().toLowerCase();
		
		AbstractFile file;

		// FS file (local filesystem) : an LRU file cache is used to recycle frequently used file instances
		if (protocol.equals("file")) {
			String urlRep = fileURL.getStringRep(true);
			file = (AbstractFile)fileCache.get(urlRep);
//			if(com.mucommander.Debug.ON) com.mucommander.Debug.trace((file==null?"Adding to file cache:":"File cache hit: ")+urlRep);
			if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("file cache hits/misses: "+fileCache.getNbHits()+"/"+fileCache.getNbMisses());

			if(file==null) {
				file = new FSFile(fileURL);
				fileCache.add(urlRep, file);
			}
		}
		// SMB file
		else if (protocol.equals("smb"))
			file = new SMBFile(fileURL);
		// HTTP/HTTPS file
		else if (protocol.equals("http") || protocol.equals("https"))
			file = new HTTPFile(fileURL);
		// FTP file
		else if (protocol.equals("ftp"))
			file = new FTPFile(fileURL);
		// SFTP file
		else if (protocol.equals("sftp"))
			file = new SFTPFile(fileURL);
		else
			throw new IOException("Unkown protocol "+protocol);

		if(parent!=null)
			file.setParent(parent);
	
		return wrapArchive(file);
	}

	
	/**
	 * Tests if given file is an archive and if it is, create the appropriate archive file
	 * on top of the base file object.
	 */
	static AbstractFile wrapArchive(AbstractFile file) {
        String name = file.getName();

		if(name!=null && !file.isDirectory()) {
			String nameLC = name.toLowerCase();
			if(nameLC.endsWith(".zip") || nameLC.endsWith(".jar"))
				return new ZipArchiveFile(file);
			else if(nameLC.endsWith(".tar") || nameLC.endsWith(".tgz") || nameLC.endsWith(".tar.gz"))
				return new TarArchiveFile(file);
			else if(nameLC.endsWith(".gz"))
				return new GzipArchiveFile(file);
		}

		return file;		
	}


	/**
	 * Returns the URL representing this file.
	 */
	public FileURL getURL() {
		return fileURL;
	}


	/**
	 * Returns the file's extension, <code>null</code> if the file doesn't have an extension.
	 */
	public String getExtension() {
		String name = getName();
		int lastDotPos = name.lastIndexOf('.');

		int len;
		if(lastDotPos==-1 || lastDotPos==(len=name.length())-1)
			return null;
	
		return name.substring(lastDotPos+1, len);
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
	 * Returns <code>true</code> if this file is a parent of the given file, or if the 2 files
	 * point to the same path.
	 */
	public boolean isParent(AbstractFile file) {
		return getCanonicalPath(false).startsWith(file.getCanonicalPath(false));
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
	 * Returns the absolute path of this AbstractFile, usually with a trailing separator for directories and root folders ('/', 'c:\' ...) and
	 * no trailing separator for 'regular' files.
	 */
	public abstract String getAbsolutePath();
	
	
	/**
	 * Returns the absolute path of this AbstractFile with a trailing separator character if <code>true</code> is passed,
	 * or without one if <code>false</code> is passed.
	 */
	public String getAbsolutePath(boolean appendSeparator) {
		String path = getAbsolutePath();
		return appendSeparator?addTrailingSeparator(path):removeTrailingSlash(path);
	}

	
	/**
	 * Returns the canonical path of this AbstractFile, resolving any symbolic links or '..' and '.' occurrences.
	 * AbstractFile's implementation simply returns the absolute path, this method should be overridden if canonical path resolution is available.
	 */
	public String getCanonicalPath() {
		return getAbsolutePath();
	}

	/**
	 * Returns the canonical path of this AbstractFile, resolving any symbolic links or '..' and '.' occurrences,
	 * and with a separator character if <code>true</code> is passed or without one if <code>false</code> is passed.
	 * <p>AbstractFile's implementation simply returns the absolute path, this method should be overridden if canonical path resolution is available.</p>
	 */
	public String getCanonicalPath(boolean appendSeparator) {
		String path = getCanonicalPath();
		return appendSeparator?addTrailingSeparator(path):removeTrailingSlash(path);
	}

	
	/**
	 * Tests if the given path contains a trailing separator character, and if not, adds one and returns the path.
	 */
	protected String addTrailingSeparator(String path) {
		// Even though getAbsolutePath() is not supposed to return a trailing separator, root folders ('/', 'c:\' ...)
		// are exceptions that's why we still have to test if path ends with a separator
		String separator = getSeparator();
		if(!path.endsWith(separator))
			return path+separator;
		return path;
	}
	
	
	/**
	 * Tests if the given path contains a trailing separator character, and if it does, removes it and returns the path.
	 */
	private String removeTrailingSlash(String path) {
		// Remove trailing slash if path is not '/' or trailing backslash if path does not end with ':\' 
		// (Reminder: C: is C's current folder, while C:\ is C's root)
		String separator = getSeparator();
		if(path.endsWith(separator)
			 && !((separator.equals("/") && path.length()==1) || (separator.equals("\\") && path.charAt(path.length()-2)==':')))
			path = path.substring(0, path.length()-1);
		return path;
	}
	
	
	/**
	 * Returns the separator character for this kind of AbstractFile.
	 */
	public abstract String getSeparator();
	
	/**
	 * Returns the last modified date, in milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
	 */
	public abstract long getDate();
	
	/**
	 * Changes last modified date and returns <code>true</code> if date was changed successfully.
	 *
	 * @param lastModified last modified date, in milliseconds since the epoch (00:00:00 GMT, January 1, 1970)
	 * @return <code>true</code> if date was changed successfully.
	 */
	public abstract boolean changeDate(long lastModified);
	
	/**
	 * Returns the size in bytes of this AbstractFile, -1 if not known.
	 */
	public abstract long getSize();
	
	/**
	 * Returns this AbstractFile's parent or null if it doesn't have any parent.
	 */
	public abstract AbstractFile getParent();
	

	/**
	 * Returns the root folder that contains this AbstractFile. If this file is already
	 * a root folder (no parent), it will simply be returned.
	 */
	public AbstractFile getRoot() {
		AbstractFile parent;
		AbstractFile child = this; 
		while((parent=child.getParent())!=null && !parent.equals(child)) {
			child = parent;
		}
		
		return child;
	}
	

	/**
	 * Sets this file's parent or null if it doesn't have any parent.
	 */
	public abstract void setParent(AbstractFile parent);

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
	 *
	 * <p>This is a default implementation solely based on file's name. This method should
	 * be overriden if underlying filesystem offers hidden file detection.</p>
	 */	
	public boolean isHidden() {
		return getName().startsWith(".");
	}

	/**
	 * Returns true if this AbstractFile is a 'regular' directory, not only a 'browsable' file (like an archive file).
	 */
	public abstract boolean isDirectory();

	/**
	 * Returns true if this AbstractFile can be browsed (entered): true for directories and supported archive files.
	 */
	public boolean isBrowsable() {
		return isDirectory() || (this instanceof AbstractArchiveFile);
	}

	/**
	 * Returns true if this file *may* be a symbolic link and thus handled with care.
	 */
	public abstract boolean isSymlink();
	
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

	
	/**
	 * Returns free space (in bytes) on the disk/volume where this file is, -1 if this information is not available.
	 */
	public abstract long getFreeSpace();

	/**
	 * Returns the total space (in bytes) of the disk/volume where this file is, -1 if this information is not available. 
	 */
	public abstract long getTotalSpace();


	/**
	 * Simple bench method.
	 */
	public static void main(String args[]) throws IOException {
		AbstractFile folder = AbstractFile.getAbstractFile("/usr/bin/", null);
		folder.ls();

		long totalTime = 0;
		long now;
		int nbIter = 100;
		for(int i=0; i<nbIter; i++) {
			now = System.currentTimeMillis();
			folder.ls();
			totalTime += System.currentTimeMillis()-now;
		}
	
		System.out.println("Average ls() time = "+totalTime/nbIter);
	}	
}