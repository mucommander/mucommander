	package com.mucommander.file;

import java.io.*;
import java.util.Vector;

import com.mucommander.PlatformManager;

/**
 * FSFile represents a 'file system file', that is a regular native file.
 */
public class FSFile extends AbstractFile {
	/** "/" for UNIX systems, "\" for Win32 */
	protected final static String separator = File.separator;

	private File file;
    private String absPath;
	private String canonicalPath;
	private boolean isSymlink;
	private boolean symlinkValueSet; 
	
	// These file attributes are cached first time they are accessed to avoid excessive I/O
    	
//	private long date = -1;
//	private long size = -1;

//	private String name;

	private FSFile parent;
	// Indicates whether or not the value has already been retrieved
	private boolean parentValCached = false;
		
		
	/**
	 * Creates a new instance of FSFile.
	 */
	public FSFile(FileURL fileURL) {
		this(fileURL, new File(absPath));
	}


	/**
	 * Creates a new instance of FSFile.
	 */
	public FSFile(FileURL fileURL, File _file) {
		super(fileURL);

		this.absPath = _file.getAbsolutePath();

        // removes trailing separator (if any)
        this.absPath = absPath.endsWith(separator)?absPath.substring(0,absPath.length()-1):absPath;

		if(!_file.isAbsolute())
			this.file = new File(absPath);
		else
			this.file = _file;
	}

	
	/////////////////////////////////////////
	// AbstractFile methods implementation //
	/////////////////////////////////////////

//	public String getProtocol() {
//		return "FILE";
//	}
	
	public String getName() {
    	// Retrieves name and caches it
//    	if (name==null) {
//	    	this.name = file.getParent()==null?absPath+separator:file.getName();
//	   	}    
//		return name;

		return file.getParent()==null?absPath+separator:file.getName();
	}


	public String getAbsolutePath() {
		// Append separator for root folders (C:\ , /) and for directories
		if(file.getParent()==null || (isDirectory() && !absPath.endsWith(separator)))
			return absPath+separator;
	
		return absPath;
	}


	public String getCanonicalPath() {
		// To avoid drive seek and potential 'floppy drive not available' dialog under Win32
		// triggered by java.io.File.getCanonicalPath() 
		int osFamily = PlatformManager.getOSFamily();
		if(osFamily==PlatformManager.WINDOWS_9X || osFamily==PlatformManager.WINDOWS_NT) {
//			String absPath = getAbsolutePath(true);
			String absPath = getAbsolutePath();
			if(absPath.equals("A:\\") || absPath.equals("B:\\"))
				return absPath;
		}
		
		if(this.canonicalPath==null) {
			try {
				this.canonicalPath = file.getCanonicalPath();
				// Append separator for directories
				if(isDirectory() && !this.canonicalPath.endsWith(separator))
					this.canonicalPath = this.canonicalPath + separator;
			}
			catch(IOException e) {
				this.canonicalPath = this.absPath;
			}
		}
		
		return this.canonicalPath;
	}
	
	public String getSeparator() {
		return separator;
	}
	
	public boolean isSymlink() {
		if(!symlinkValueSet) {
			FSFile parent = (FSFile)getParent();
			String canonPath = getCanonicalPath(false);
			if(parent==null || canonPath==null)
				this.isSymlink = false;
			else {
				String parentCanonPath = parent.getCanonicalPath(true);
				this.isSymlink = !canonPath.equals(parentCanonPath+getName());
			}
			
			this.symlinkValueSet = true;
		}
		
		return this.isSymlink;
	}

	public long getDate() {
        return file.lastModified();
	}
	
	public long getSize() {
        return file.length();
	}
	
	public AbstractFile getParent() {
		// Retrieves parent and caches it
		if (!parentValCached) {
			String parentS = file.getParent();
			if(parentS != null)
				parent = new FSFile(new File(parentS));
			parentValCached = true;
		}
        return parent;
	}
	
	public void setParent(AbstractFile parent) {
		this.parent = (FSFile)parent;	
		this.parentValCached = true;
	}
		
	public boolean exists() {
		return file.exists();
	}
	
	public boolean canRead() {
		return file.canRead();
	}
	
	public boolean canWrite() {
		return file.canWrite();
	}
	
	public boolean isHidden() {
        return file.isHidden();
	}

	public boolean isDirectory() {
        return file.isDirectory();
	}

	public boolean equals(Object f) {
		if(!(f instanceof FSFile))
			return super.equals(f);		// could be equal to a ZipArchiveFile

		// Compares canonical path (which File does not do by default in its equals() method)
//		return this.canonicalPath.equals(((FSFile)f).canonicalPath);
		return getCanonicalPath().equals(((FSFile)f).getCanonicalPath());
	}
	

	public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
	}

//	/** 
//	 * Overrides AbstractFile's getInputStream(long) method to provide a more efficient implementation
//	 * which uses RandomAccessFile to skip bytes and not FileInputStream which still reads bytes.
//	 */
/*
	public InputStream getInputStream(long skipBytes) throws IOException {
        RandomAccessInputStream rain = new RandomAccessInputStream(file);
		rain.skip(skipBytes);
		return rain;
	}
*/
	
	public OutputStream getOutputStream(boolean append) throws IOException {
		return new FileOutputStream(absPath, append);
	}
		
	public boolean moveTo(AbstractFile dest) throws IOException  {
		if (dest instanceof FSFile)
			return file.renameTo(new File(dest.getAbsolutePath()));
		return false;
	}

	public void delete() throws IOException {
		boolean ret = file.delete();
		
		if(ret==false)
			throw new IOException();
	}

	public AbstractFile[] ls() throws IOException {
		// // returns a cached array if ls has already been called
        //if(children!=null)
        //    return children;
        
        String names[] = file.list();
		
        if(names==null)
            throw new IOException();
        
        AbstractFile children[] = new AbstractFile[names.length];
		for(int i=0; i<names.length; i++) {
			children[i] = AbstractFile.getAbstractFile(absPath+separator+names[i], this);
		}
		return children;
	}

	public void mkdir(String name) throws IOException {
if(com.mucommander.Debug.ON) System.out.println("FSFile.mkdir "+absPath+separator+name);
		if(!new File(absPath+separator+name).mkdir())
			throw new IOException();
	}
}