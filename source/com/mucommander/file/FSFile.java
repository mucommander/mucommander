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
		

//	/**
//	 * RandomAccessFile does unfortunately not derive from InputStream
//	 * as it also provides OutputStream methods, this class extends 
//	 * InputStream and maps the methods on RandomAccessFile's.
//	 *
//	 * <p>The purpose of this class is to provide an effecient implementation of skip()
//	 * by using RandomAccessFile's seek() method.</p>
//	 */
/*	 
	private class RandomAccessInputStream extends InputStream {
		RandomAccessFile raf;
		
		public RandomAccessInputStream(File f) throws IOException {
			this.raf = new RandomAccessFile(f, "r");
		}
		
		public int read() throws IOException {
			return raf.read();
		}

		public int read(byte[] b, int off, int len) throws IOException {
			return raf.read(b, off, len);
		}
		
		public int read(byte[] b) throws IOException {
			return raf.read(b);
		}
		
		public long skip(long n) throws IOException {
			raf.seek(n);
			return n;
		}
		
		public void close() throws IOException {
			raf.close();
		}
	}
*/		
		
	/**
	 * Creates a new instance of FSFile. Although the existence
	 * of the file is not checked, the given file path should exist.
	 * @param absPath the absolute path of this AbstractFile.
	 */
	public FSFile(String absPath) {
		this(new File(absPath));
	}


	/**
	 * Creates a new instance of FSFile. Although the existence
	 * of the file is not checked, the given file path should exist.
	 * @param file the file of this AbstractFile.
	 */
	public FSFile(File _file) {
//System.out.println("F0");
		this.absPath = _file.getAbsolutePath();

        // removes trailing separator (if any)
        this.absPath = absPath.endsWith(separator)?absPath.substring(0,absPath.length()-1):absPath;

//System.out.println("F1");
		if(!_file.isAbsolute())
			this.file = new File(absPath);
		else
			this.file = _file;
	}

	
	protected void setParent(AbstractFile parent) {
		this.parent = (FSFile)parent;	
		this.parentValCached = true;
	}
	
	
	public String getName() {
    	// Retrieves name and caches it
//    	if (name==null) {
//	    	this.name = file.getParent()==null?absPath+separator:file.getName();
//	   	}    
//		return name;

		return file.getParent()==null?absPath+separator:file.getName();
	}

	public String getAbsolutePath() {
		return file.getParent()==null?absPath+separator:absPath;
	}

	public String getCanonicalPath() {
		// To avoid drive seek and potential 'floppy drive not available' dialog under Win32
		// triggered by java.io.File.getCanonicalPath() 
		int osFamily = PlatformManager.getOSFamily();
		if(osFamily==PlatformManager.WINDOWS_9X || osFamily==PlatformManager.WINDOWS_NT) {
			String absPath = getAbsolutePath(true);
			if(absPath.equals("A:\\") || absPath.equals("B:\\"))
				return absPath;
		}
		
		if(this.canonicalPath==null) {
			try {
				this.canonicalPath = file.getCanonicalPath();
			}
			catch(IOException e) {
				if(com.mucommander.Debug.ON)
					e.printStackTrace();
					
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