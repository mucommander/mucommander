package com.mucommander.file;

import java.io.*;
import java.util.*;

import com.mucommander.PlatformManager;

/**
 * FSFile represents a 'file system file', that is a regular native file.
 *
 * @author Maxence Bernard
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
	 * Convenience constructor.
	 */
//	public FSFile(String absPath) throws IOException {
//		this(new FileURL("file://"+absPath), new File(absPath));
//	}


	/**
	 * Convenience constructor.
	 */
//	public FSFile(File file) throws IOException {
//		this(new FileURL("file://"+file.getAbsolutePath()), file);
//	}

		
	/**
	 * Creates a new instance of FSFile.
	 */
	public FSFile(FileURL fileURL) throws IOException {
		super(fileURL);

		// Remove leading '/' if path is 'a la windows', i.e. starts with a drive like C:\
		String path = fileURL.getPath();

		if(path.indexOf(":\\")!=-1 && path.charAt(0)=='/')
			path = path.substring(1, path.length());

		init(new File(path));
	}


	/**
	 * Creates a new FSFile using the given java.io.File instance.
	 */
	private FSFile(FileURL fileURL, File file) throws IOException {
		super(fileURL);
		init(file);
	}

	
	private void init(File file) throws IOException {
		// Throw an exception is the file's path is not absolute.
		if(!file.isAbsolute())
			throw new IOException();

		this.file = file;
		this.absPath = file.getAbsolutePath();

        // removes trailing separator (if any)
        this.absPath = absPath.endsWith(separator)?absPath.substring(0,absPath.length()-1):absPath;		
	}

	
	/////////////////////////////////////////
	// AbstractFile methods implementation //
	/////////////////////////////////////////

	public String getName() {
    	// Retrieves name and caches it
		return file.getParent()==null?absPath+separator:file.getName();
	}


	public String getAbsolutePath() {
		// Append separator for root folders (C:\ , /) and for directories
		if(file.getParent()==null || (isDirectory() && !absPath.endsWith(separator)))
			return absPath+separator;
	
		return absPath;
	}


	public String getCanonicalPath() {
		// To avoid drive seeks and potential 'floppy drive not available' dialog under Win32
		// triggered by java.io.File.getCanonicalPath() 
		int osFamily = PlatformManager.getOSFamily();
		if(osFamily==PlatformManager.WINDOWS_9X || osFamily==PlatformManager.WINDOWS_NT) {
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
	
	public boolean changeDate(long lastModified) {
		return file.setLastModified(lastModified);
	}
		
	public long getSize() {
        return file.length();
	}
	
	public AbstractFile getParent() {
		// Retrieves parent and caches it
		if (!parentValCached) {
			FileURL parentURL = getURL().getParent();
			if(parentURL != null)
				try { parent = new FSFile(parentURL, new File(parentURL.getPath())); }
				catch(IOException e) {}
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
		// To avoid drive seeks and potential 'floppy drive not available' dialog under Win32
		// triggered by java.io.File.getCanonicalPath() 
		int osFamily = PlatformManager.getOSFamily();
		if(osFamily==PlatformManager.WINDOWS_9X || osFamily==PlatformManager.WINDOWS_NT) {
			if(absPath.equals("A:") || absPath.equals("B:"))
				return true;
		}

        return file.isDirectory();
	}

	public boolean equals(Object f) {
		if(!(f instanceof FSFile))
			return super.equals(f);		// could be equal to a ZipArchiveFile

		// Compares canonical path (which File does not do by default in its equals() method)
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
		
	public boolean moveTo(AbstractFile destFile) throws IOException  {
		if(destFile.fileURL.getProtocol().equals("file"))
			return file.renameTo(new File(destFile.getAbsolutePath()));
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
		for(int i=0; i<names.length; i++)
			children[i] = AbstractFile.getAbstractFile(absPath+separator+names[i], this);

		return children;
	}

	public void mkdir(String name) throws IOException {
		if(!new File(absPath+separator+name).mkdir())
			throw new IOException();
	}
	

	public long getFreeSpace() {
		return getVolumeInfo()[1];
	}
	
	public long getTotalSpace() {
		return getVolumeInfo()[0];
	}	
	
	/**
	 * Uses platform dependant commands to extract free and total volume (where this file resides) space.
	 *
	 * <p>This method has been made public as it is more efficient to retrieve both free space and volume space
	 * info than calling getFreeSpace() and getTotalSpace() separately, since a single command retrieves both.
	 *
	 * @return [totalSpace, freeSpace], or <code>null</code> if information could not be retrieved from the current platform. 
	 */
	public long[] getVolumeInfo() {
		BufferedReader br = null;
		String absPath = getAbsolutePath();
		long dfInfo[] = new long[]{-1, -1};

		try {
			int osFamily = PlatformManager.getOSFamily();
			// OS is Windows
			if(osFamily==PlatformManager.WINDOWS_9X || osFamily==PlatformManager.WINDOWS_NT) {
				// Parses the output of 'dir "filePath"' command to retrieve free space information
				// Note : total space information not available under Windows

				// 'dir' command returns free space on the last line
				Process process = PlatformManager.execute("dir \""+absPath+"\"", this);

				// Check that the process was correctly started
				if(process!=null) {
					br = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line;
					String lastLine = null;
					// Retrieves last line of dir
					while((line=br.readLine())!=null) {
						if(!line.trim().equals(""))
							lastLine = line;
					}

					// Last dir line may look like something this (might vary depending on system's language, below in French):
					// 6 Rep(s)  14 767 521 792 octets libres		
					if(lastLine!=null) {
						StringTokenizer st = new StringTokenizer(lastLine, " \t\n\r\f,.");
						// Discard first token
						st.nextToken();

						// Concatenates as many contiguous groups of numbers
						String token;
						String freeSpace = "";
						while(st.hasMoreTokens()) {
							token = st.nextToken();
							char c = token.charAt(0);
							if(c>='0' && c<='9')
								freeSpace += token;
							else if(!freeSpace.equals(""))
								break;
						}

						dfInfo[1] = Long.parseLong(freeSpace);
					}
				}
			}
			// Parses the output of 'df -k "filePath"' command on UNIX/BSD-based systems to retrieve free and total space information
			else {
				// 'df -k' returns totals in block of 1K = 1024 bytes
				Process process = Runtime.getRuntime().exec(new String[]{"df", "-k", absPath}, null, file);
				
				// Check that the process was correctly started
				if(process!=null) {
					br = new BufferedReader(new InputStreamReader(process.getInputStream()));
					// Discard the first line ("Filesystem   1K-blocks     Used    Avail Capacity  Mounted on");
					br.readLine();
					String line = br.readLine();
					if(line!=null) {
						StringTokenizer st = new StringTokenizer(line);
						// Discard 'Filesystem' field
						st.nextToken();
						// Parse 'volume total' field
						dfInfo[0] = Long.parseLong(st.nextToken()) * 1024;
						// Discard 'Used' field
						st.nextToken();
						// Parse 'volume free' field
						dfInfo[1] = Long.parseLong(st.nextToken()) * 1024;
					}
				}
			}
		}
		catch(Exception e) {	// Could be IOException, NoSuchElementException or NumberFormatException, but better be safe and catch Exception
			if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("An error occured while executing 'df -k "+absPath+"' command: "+e);
		}
		finally {
			if(br!=null)
				try { br.close(); } catch(IOException e) {}
		}

		return dfInfo;
	}
}
