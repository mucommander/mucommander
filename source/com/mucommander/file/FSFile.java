package com.mucommander.file;

import com.mucommander.PlatformManager;
import com.mucommander.Debug;
import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.io.FileTransferException;
import com.mucommander.io.RandomAccessInputStream;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.util.StringTokenizer;


/**
 * FSFile represents a 'file system file', that is a regular native file.
 *
 * @author Maxence Bernard
 */
public class FSFile extends AbstractFile {

    private File file;
    private String parentFile;
    private String absPath;
    private String canonicalPath;
    private boolean isSymlink;
    private boolean symlinkValueSet; 
	
    private AbstractFile parent;
    // Indicates if the parent folder's value has been retrieved
    private boolean parentValueSet;
	
    /** "/" for UNIX systems, "\" for Win32 */
    protected final static String SEPARATOR = File.separator;

    /** Are we running Windows ? */
    private final static boolean IS_WINDOWS;

    /** Used under Windows to determine if a root folder is a floppy drive */
    private final static FileSystemView fileSystemView;
		
    static {
        IS_WINDOWS = PlatformManager.isWindowsFamily();
        fileSystemView = FileSystemView.getFileSystemView();
    }


    /**
     * FSRandomAccessInputStream extends RandomAccessInputStream to provide random access to an <code>FSFile</code>'s
     * content.
     */
    public class FSRandomAccessInputStream extends RandomAccessInputStream {

        private RandomAccessFile raf;

        public FSRandomAccessInputStream(RandomAccessFile raf) {
            this.raf = raf;
        }

        public int read() throws IOException {
            return raf.read();
        }

        public int read(byte b[]) throws IOException {
            return raf.read(b);
        }

        public int read(byte b[], int off, int len) throws IOException {
            return raf.read(b, off, len);
        }

        public void close() throws IOException {
            raf.close();
        }

        public long getOffset() throws IOException {
            return raf.getFilePointer();
        }

        public long getLength() throws IOException {
            return raf.length();
        }

        public void seek(long pos) throws IOException {
            raf.seek(pos);
        }
    }

    /**
     * Creates a new instance of FSFile.
     */
    public FSFile(FileURL fileURL) throws IOException {
        super(fileURL);

        // Remove leading '/' if path is 'a la windows', i.e. starts with a drive like C:\
        String path = fileURL.getPath();
        if(path.indexOf(":\\")!=-1 && path.charAt(0)=='/')
            path = path.substring(1, path.length());

        // If OS is Windows and hostname is not 'localhost', translate path back
        // into a Windows-style UNC network path ( \\hostname\path )
        String hostname = fileURL.getHost();
        if(IS_WINDOWS && !FileURL.LOCALHOST.equals(hostname))
            path = "\\\\"+hostname+fileURL.getPath().replace('/', '\\');    // Replace leading / char by \

        init(new File(path));
    }


//    /**
//     * Creates a new FSFile using the given java.io.File instance.
//     */
//    private FSFile(FileURL fileURL, File file) throws IOException {
//        super(fileURL);
//        init(file);
//    }

	
    private void init(File file) throws IOException {
        // Throw an exception is the file's path is not absolute.
        if(!file.isAbsolute())
            throw new IOException();

        this.file = file;
        this.parentFile = file.getParent();
        this.absPath = file.getAbsolutePath();

        // removes trailing separator (if any)
        this.absPath = absPath.endsWith(SEPARATOR)?absPath.substring(0,absPath.length()-1):absPath;
    }


    /**
     * Uses platform dependant commands to extract free and total volume (where this file resides) space.
     *
     * <p>This method has been made public as it is more efficient to retrieve both free space and volume space
     * info than calling getFreeSpace() and getTotalSpace() separately, since a single command process retrieves both.
     *
     * @return [totalSpace, freeSpace], or <code>null</code> if information could not be retrieved from the current platform. 
     */
    public long[] getVolumeInfo() {
        BufferedReader br = null;
        String absPath = getAbsolutePath();
        long dfInfo[] = new long[]{-1, -1};

        try {
            // OS is Windows
            if(IS_WINDOWS) {
                // Parses the output of 'dir "filePath"' command to retrieve free space information
                // Note : total space information is not available under Windows

                // 'dir' command returns free space on the last line
                //Process process = PlatformManager.execute("dir \""+absPath+"\"", this);
                //Process process = Runtime.getRuntime().exec(new String[] {"dir", absPath}, null, new File(getAbsolutePath()));
                Process process = Runtime.getRuntime().exec(PlatformManager.getDefaultShellCommand()+" dir \""+absPath+"\"");

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
            if(com.mucommander.Debug.ON) {
                com.mucommander.Debug.trace("An error occured while retrieving volume info: "+e);
                e.printStackTrace();
            }
        }
        finally {
            if(br!=null)
                try { br.close(); } catch(IOException e) {}
        }

        return dfInfo;
    }

	
    /**
     * Guesses if this drive is a floppy drive. This method should only be called on a file corresponding
     * to a drive's root folder.
     *
     * <p>The result of this method should be accurate under Java 1.4 and up, just a guess under Java 1.3
     * running under Windows, will return false for Java 1.3 running under a non-Windows platform.</p>
     */
    public boolean guessFloppyDrive() {
        // Use FileSystemView.isFloppyDrive(File) to determine if this file
        // is a floppy drive. This method being available only in Java 1.4 and up.
        if(PlatformManager.JAVA_VERSION>=PlatformManager.JAVA_1_4)
            return fileSystemView.isFloppyDrive(file);

        // We're running Java 1.3 or below, try to guess if file is floppy drive under Windows
        if(IS_WINDOWS && absPath.equals("A:") || absPath.equals("B:"))
            return true;

        // No clue, return false
        return false;
    }
	
    /**
     * Guesses if this drive is a removable media drive (Floppy/CD/DVD). This method should only be called on a file
     * corresponding to a drive's root folder.
     *
     * <p>The result is just a guess that works well under Windows.</p>
     */
    public boolean guessRemovableDrive() {
        // A weak way to characterize such a drive is to check if the corresponding root folder is a floppy drive or 
        // read-only. A better way would be to create a JNI interface as described here: http://forum.java.sun.com/thread.jspa?forumID=256&threadID=363074
        return guessFloppyDrive() || (IS_WINDOWS && (!canWrite()));
    }
	
	
    /////////////////////////////////////////
    // AbstractFile methods implementation //
    /////////////////////////////////////////

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
        if (!parentValueSet) {
            if(parentFile!=null) {
                FileURL parentURL = getURL().getParent();
                if(parentURL != null) {
                    parent = FileFactory.getFile(parentURL);
                }
            }
            parentValueSet = true;
        }
        return parent;
    }
	
    public void setParent(AbstractFile parent) {
        this.parent = parent;
        this.parentValueSet = true;
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
	
    public boolean isDirectory() {
        // To avoid drive seeks and potential 'floppy drive not available' dialog under Win32
        // triggered by java.io.File.getCanonicalPath() 
        if(IS_WINDOWS && guessFloppyDrive())
            return true;

        return file.isDirectory();
    }

    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        return new FSRandomAccessInputStream(new RandomAccessFile(file, "r"));
    }

    public OutputStream getOutputStream(boolean append) throws IOException {
        return new FileOutputStream(absPath, append);
    }


    public void delete() throws IOException {
        boolean ret = file.delete();
		
        if(!ret)
            throw new IOException();
    }

/*
    public AbstractFile[] ls() throws IOException {
        String names[] = file.list();
		
        if(names==null)
            throw new IOException();
        
        AbstractFile children[] = new AbstractFile[names.length];
        for(int i=0; i<names.length; i++) {
            // Retrieves an AbstractFile (FSFile or archive) instance potentially fetched from the LRUCache
            // and reuse this file as parent
            children[i] = AbstractFile.getFile(absPath+SEPARATOR+names[i], this);
        }

        return children;
    }
*/

    public AbstractFile[] ls() throws IOException {
        return ls((FilenameFilter)null);
    }

    public void mkdir(String name) throws IOException {
        if(!new File(absPath+SEPARATOR+name).mkdir())
            throw new IOException();
    }
	

    public long getFreeSpace() {
        return getVolumeInfo()[1];
    }
	
    public long getTotalSpace() {
        return getVolumeInfo()[0];
    }	


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public String getName() {
        return parentFile==null?absPath+SEPARATOR:file.getName();
    }

    public String getAbsolutePath() {
        // Append separator for root folders (C:\ , /) and for directories
        if(parentFile==null || (isDirectory() && !absPath.endsWith(SEPARATOR)))
            return absPath+SEPARATOR;

        return absPath;
    }


    public String getCanonicalPath() {
        // To avoid drive seeks and potential 'floppy drive not available' dialog under Win32
        // triggered by java.io.File.getCanonicalPath()
        if(IS_WINDOWS && guessFloppyDrive())
            return absPath;

        if(this.canonicalPath==null) {
            try {
                this.canonicalPath = file.getCanonicalPath();
                // Append separator for directories
                if(isDirectory() && !this.canonicalPath.endsWith(SEPARATOR))
                    this.canonicalPath = this.canonicalPath + SEPARATOR;
            }
            catch(IOException e) {
                this.canonicalPath = this.absPath;
            }
        }

        return this.canonicalPath;
    }


    public String getSeparator() {
        return SEPARATOR;
    }


    public AbstractFile[] ls(FilenameFilter filenameFilter) throws IOException {
        String names[] = file.list();

        if(names==null)
            throw new IOException();

        if(filenameFilter!=null)
            names = filenameFilter.filter(names);

        AbstractFile children[] = new AbstractFile[names.length];
        for(int i=0; i<names.length; i++) {
            // Retrieves an AbstractFile (FSFile or archive) instance potentially fetched from the LRUCache
            // and reuse this file as parent
            children[i] = FileFactory.getFile(absPath+SEPARATOR+names[i], this);
        }

        return children;
    }


    /**
     * Overrides {@link AbstractFile#moveTo(AbstractFile)} to move/rename the file directly if the destination file
     * is also a local file.
     */
    public void moveTo(AbstractFile destFile) throws FileTransferException  {
        if(!destFile.fileURL.getProtocol().equals("file")) {
            super.moveTo(destFile);
            return;
        }

        // If file is an archive file, retrieve the enclosed file, which is likely to be an FSFile but not necessarily
        // (may be an ArchiveEntryFile)
        if(destFile instanceof AbstractArchiveFile)
            destFile = ((AbstractArchiveFile)destFile).getProxiedFile();

        // If destination file is not an FSFile (for instance an archive entry), renaming won't work
        // so use the default moveTo() implementation instead
        if(!(destFile instanceof FSFile)) {
            super.moveTo(destFile);
            return;
        }

        // Move file
        if(!file.renameTo(((FSFile)destFile).file))
            throw new FileTransferException(FileTransferException.UNKNOWN_REASON);    // Report that move failed
    }


    public boolean isHidden() {
        return file.isHidden();
    }

    public boolean equals(Object f) {
        if(!(f instanceof FSFile))
            return super.equals(f);		// could be equal to a ZipArchiveFile

        // Compares canonical path (which File does not do by default in its equals() method)
        return getCanonicalPath().equals(((FSFile)f).getCanonicalPath());
    }
}
