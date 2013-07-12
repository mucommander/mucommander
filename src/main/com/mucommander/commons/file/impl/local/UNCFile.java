package com.mucommander.commons.file.impl.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.GroupedPermissionBits;
import com.mucommander.commons.file.IndividualPermissionBits;
import com.mucommander.commons.file.PermissionBits;
import com.mucommander.commons.file.ProtocolFile;
import com.mucommander.commons.file.UnsupportedFileOperation;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.commons.file.impl.local.LocalFile.LocalInputStream;
import com.mucommander.commons.file.impl.local.LocalFile.LocalOutputStream;
import com.mucommander.commons.file.impl.local.LocalFile.LocalRandomAccessInputStream;
import com.mucommander.commons.file.impl.local.LocalFile.LocalRandomAccessOutputStream;
import com.mucommander.commons.file.util.Kernel32;
import com.mucommander.commons.file.util.Kernel32API;
import com.mucommander.commons.file.util.PathUtils;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;
import com.mucommander.commons.runtime.JavaVersion;
import com.mucommander.commons.runtime.OsVersion;
import com.sun.jna.ptr.LongByReference;

/**
 * TODO: update this documentation and LocalFile documentation
 * 
 * @author Arik Hadas
 */
public class UNCFile extends ProtocolFile {
	private static final Logger LOGGER = LoggerFactory.getLogger(UNCFile.class);

    protected File file;
    private FilePermissions permissions;
    
    /** Absolute file path, free of trailing separator */
    protected String absPath;

    /** Caches the parent folder, initially null until getParent() gets called */
    protected AbstractFile parent;
    /** Indicates whether the parent folder instance has been retrieved and cached or not (parent can be null) */
    protected boolean parentValueSet;
	
    /** Underlying Windows's path separator */
    public final static String SEPARATOR = "\\";
    
    // Permissions can only be changed under Java 1.6 and up and are limited to 'user' access.
    // Note: 'read' and 'execute' permissions have no meaning under Windows (files are either read-only or
    // read-write) and as such can't be changed.

    /** Changeable permissions mask for Java 1.6 and up, on Windows OS (any version) */
    private static PermissionBits CHANGEABLE_PERMISSIONS_JAVA_1_6_WINDOWS = new GroupedPermissionBits(128);   // -w------- (200 octal)

    /** Changeable permissions mask for Java 1.5 or below */
    private static PermissionBits CHANGEABLE_PERMISSIONS_JAVA_1_5 = PermissionBits.EMPTY_PERMISSION_BITS;   // --------- (0)

    /** Bit mask that indicates which permissions can be changed */
    private final static PermissionBits CHANGEABLE_PERMISSIONS = JavaVersion.JAVA_1_6.isCurrentOrHigher()
            ?CHANGEABLE_PERMISSIONS_JAVA_1_6_WINDOWS:CHANGEABLE_PERMISSIONS_JAVA_1_5;
    
	/**
     * Creates a new instance of UNCFile and a corresponding {@link File} instance.
     */
    protected UNCFile(FileURL fileURL) throws IOException {
        this(fileURL, null);
    }

    /**
     * Creates a new instance of UNCFile, using the given {@link File} if not <code>null</code>, creating a new
     * {@link File} instance otherwise.
     */
    protected UNCFile(FileURL fileURL, File file) throws IOException {
        super(fileURL);

        if(file==null) {
            absPath = SEPARATOR+SEPARATOR+fileURL.getHost()+fileURL.getPath().replace('/', '\\');    // Replace leading / char by \			

            // Create the java.io.File instance and throw an exception if the path is not absolute.
            file = new File(absPath);
            if(!file.isAbsolute())
                throw new IOException();
        }
        // the java.io.File instance was created by ls(), no need to re-create it or call the costly File#getAbsolutePath()
        else {
            absPath = SEPARATOR+SEPARATOR+fileURL.getHost()+fileURL.getPath().replace('/', '\\');
        }
		
		// Remove the trailing separator if present
        if(absPath.endsWith(SEPARATOR))
            absPath = absPath.substring(0, absPath.length()-1);

        this.file = file;
        this.permissions = new UNCFilePermissions(file);
    }

    /////////////////////////////////
    // AbstractFile implementation //
    /////////////////////////////////

    /**
     * Returns a <code>java.io.File</code> instance corresponding to this file.
     */
    @Override
    public Object getUnderlyingFileObject() {
    	return file;
    }

    @Override
    public boolean isSymlink() {
    	// At the moment symlinks under Windows (aka NTFS junction points) are not supported because java.io.File
    	// knows nothing about them and there is no way to discriminate them. So there is no need to waste time
    	// comparing canonical paths, just return false.
    	// Todo: add support for .lnk files (~hard links)
    	return false;
    }

    @Override
    public boolean isSystem() {
        return false;
    }

    @Override
    public long getDate() {
    	return file.lastModified();
    }

    @Override
    public void changeDate(long lastModified) throws IOException {
        // java.io.File#setLastModified(long) throws an IllegalArgumentException if time is negative.
        // If specified time is negative, set it to 0 (01/01/1970).
        if(lastModified < 0)
            lastModified = 0;

        if(!file.setLastModified(lastModified))
            throw new IOException();
    }
		
    @Override
    public long getSize() {
        return file.length();
    }
	
    @Override
    public AbstractFile getParent() {
        // Retrieve the parent AbstractFile instance and cache it
        if (!parentValueSet) {
            if(!isRoot()) {
                FileURL parentURL = getURL().getParent();

                if(parentURL != null) {
                    parent = FileFactory.getFile(parentURL);
                }
            }
            parentValueSet = true;
        }
        return parent;
    }
	
    @Override
    public void setParent(AbstractFile parent) {
        this.parent = parent;
        this.parentValueSet = true;
    }
		
    @Override
    public boolean exists() {
        return file.exists();
    }
	
    @Override
    public FilePermissions getPermissions() {
        return permissions;
    }

    @Override
    public PermissionBits getChangeablePermissions() {
        return CHANGEABLE_PERMISSIONS;
    }

    @Override
    public void changePermission(int access, int permission, boolean enabled) throws IOException {
        // Only the 'user' permissions under Java 1.6 are supported
        if(access!=USER_ACCESS || JavaVersion.JAVA_1_6.isCurrentLower())
            throw new IOException();

        boolean success = false;
        if(permission==READ_PERMISSION)
            success = file.setReadable(enabled);
        else if(permission==WRITE_PERMISSION)
            success = file.setWritable(enabled);
        else if(permission==EXECUTE_PERMISSION)
            success = file.setExecutable(enabled);

        if(!success)
            throw new IOException();
    }

    /**
     * Always returns <code>null</code>, this information is not available unfortunately.
     */
    @Override
    public String getOwner() {
        return null;
    }

    /**
     * Always returns <code>false</code>, this information is not available unfortunately.
     */
    @Override
    public boolean canGetOwner() {
        return false;
    }

    /**
     * Always returns <code>null</code>, this information is not available unfortunately.
     */
    @Override
    public String getGroup() {
        return null;
    }

    /**
     * Always returns <code>false</code>, this information is not available unfortunately.
     */
    @Override
    public boolean canGetGroup() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        // This test is not necessary anymore now that 'No disk' error dialogs are disabled entirely (using Kernel32
        // DLL's SetErrorMode function). Leaving this code commented for a while in case the problem comes back.

//        // To avoid drive seeks and potential 'floppy drive not available' dialog under Win32
//        // triggered by java.io.File.isDirectory()
//        if(IS_WINDOWS && guessFloppyDrive())
//            return true;

        return file.isDirectory();
    }

    /**
     * Implementation notes: the returned <code>InputStream</code> uses a NIO {@link FileChannel} under the hood to
     * benefit from <code>InterruptibleChannel</code> and allow a thread waiting for an I/O to be gracefully interrupted
     * using <code>Thread#interrupt()</code>.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return new LocalInputStream(new FileInputStream(file).getChannel());
    }

    /**
     * Implementation notes: the returned <code>InputStream</code> uses a NIO {@link FileChannel} under the hood to
     * benefit from <code>InterruptibleChannel</code> and allow a thread waiting for an I/O to be gracefully interrupted
     * using <code>Thread#interrupt()</code>.
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return new LocalOutputStream(new FileOutputStream(absPath, false).getChannel());
    }

    /**
     * Implementation notes: the returned <code>InputStream</code> uses a NIO {@link FileChannel} under the hood to
     * benefit from <code>InterruptibleChannel</code> and allow a thread waiting for an I/O to be gracefully interrupted
     * using <code>Thread#interrupt()</code>.
     */
    @Override
    public OutputStream getAppendOutputStream() throws IOException {
        return new LocalOutputStream(new FileOutputStream(absPath, true).getChannel());
    }

    /**
     * Implementation notes: the returned <code>InputStream</code> uses a NIO {@link FileChannel} under the hood to
     * benefit from <code>InterruptibleChannel</code> and allow a thread waiting for an I/O to be gracefully interrupted
     * using <code>Thread#interrupt()</code>.
     */
    @Override
    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        return new LocalRandomAccessInputStream(new RandomAccessFile(file, "r").getChannel());
    }

    /**
     * Implementation notes: the returned <code>InputStream</code> uses a NIO {@link FileChannel} under the hood to
     * benefit from <code>InterruptibleChannel</code> and allow a thread waiting for an I/O to be gracefully interrupted
     * using <code>Thread#interrupt()</code>.
     */
    @Override
    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException {
        return new LocalRandomAccessOutputStream(new RandomAccessFile(file, "rw").getChannel());
    }

    @Override
    public void delete() throws IOException {
        boolean ret = file.delete();
		
        if(!ret)
            throw new IOException();
    }


    @Override
    public AbstractFile[] ls() throws IOException {
        return ls((FilenameFilter)null);
    }

    @Override
    public void mkdir() throws IOException {
        if(!file.mkdir())
            throw new IOException();
    }
	
    @Override
    public void renameTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
        // Throw an exception if the file cannot be renamed to the specified destination.
        // Fail in some situations where java.io.File#renameTo() doesn't.
        // Note that java.io.File#renameTo()'s implementation is system-dependant, so it's always a good idea to
        // perform all those checks even if some are not necessary on this or that platform.
        checkRenamePrerequisites(destFile, true, false);

        // The behavior of java.io.File#renameTo() when the destination file already exists is not consistent
        // across platforms:
        // - Under UNIX, it succeeds and return true
        // - Under Windows, it fails and return false
        // This ticket goes in great details about the issue: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4017593
        //
        // => Since this method is required to succeed when the destination file exists, the Windows platform needs
        // special treatment.

        destFile = destFile.getTopAncestor();
        File destJavaIoFile = ((UNCFile)destFile).file;

        // This check is necessary under Windows because java.io.File#renameTo(java.io.File) does not return false
        // if the destination file is located on a different drive, contrary for example to Mac OS X where renameTo
        // returns false in this case.
        // Not doing this under Windows would mean files would get moved between drives with renameTo, which doesn't
        // allow the transfer to be monitored.
        // Note that Windows UNC paths are handled by checkRenamePrerequisites() when comparing hosts for equality.
        if(!getRoot().equals(destFile.getRoot()))
        	throw new IOException();

        // Windows 9x or Windows Me: Kernel32's MoveFileEx function is NOT available
        if(OsVersion.WINDOWS_ME.isCurrentOrLower()) {
        	// The destination file is deleted before calling java.io.File#renameTo().
        	// Note that in this case, the atomicity of this method is not guaranteed anymore -- if
        	// java.io.File#renameTo() fails (for whatever reason), the destination file is deleted anyway.
        	if(destFile.exists())
        		if(!destJavaIoFile.delete())
        			throw new IOException();
        }
        // Windows NT: Kernel32's MoveFileEx can be used, if the Kernel32 DLL is available.
        else if(Kernel32.isAvailable()) {
        	// Note: MoveFileEx is always used, even if the destination file does not exist, to avoid having to
        	// call #exists() on the destination file which has a cost.
        	if(!Kernel32.getInstance().MoveFileEx(absPath, destFile.getAbsolutePath(),
        			Kernel32API.MOVEFILE_REPLACE_EXISTING|Kernel32API.MOVEFILE_WRITE_THROUGH)) {
        		String errorMessage = Integer.toString(Kernel32.getInstance().GetLastError());
        		// TODO: use Kernel32.FormatMessage
        		throw new IOException("Rename using Kernel32 API failed: " + errorMessage);
        	} else {
        		// move successful
        		return;
        	}
        }
        // else fall back to java.io.File#renameTo

        if(!file.renameTo(destJavaIoFile))
        	throw new IOException();
    }

    @Override
    public long getFreeSpace() throws IOException {
        if(JavaVersion.JAVA_1_6.isCurrentOrHigher())
            return file.getUsableSpace();

        return getVolumeInfo()[1];
    }
	
    @Override
    public long getTotalSpace() throws IOException {
        if(JavaVersion.JAVA_1_6.isCurrentOrHigher())
            return file.getTotalSpace();

        return getVolumeInfo()[0];
    }	

    // Unsupported file operations

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     *
     * @throws UnsupportedFileOperationException, always
     */
    @Override
    @UnsupportedFileOperation
    public void copyRemotelyTo(AbstractFile destFile) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.COPY_REMOTELY);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public String getName() {
        // If this file has no parent, return:
        // - the drive's name under OSes with root drives such as Windows, e.g. "C:"
        // - "/" under Unix-based systems
        if(isRoot())
            return absPath;

        return file.getName();
    }

    @Override
    public String getAbsolutePath() {
        // Append separator for directories
        if(isDirectory() && !absPath.endsWith(SEPARATOR))
            return absPath+SEPARATOR;

        return absPath;
    }


    @Override
    public String getCanonicalPath() {
        // This test is not necessary anymore now that 'No disk' error dialogs are disabled entirely (using Kernel32
        // DLL's SetErrorMode function). Leaving this code commented for a while in case the problem comes back.
         
//        // To avoid drive seeks and potential 'floppy drive not available' dialog under Win32
//        // triggered by java.io.File.getCanonicalPath()
//        if(IS_WINDOWS && guessFloppyDrive())
//            return absPath;

        // Note: canonical path must not be cached as its resolution can change over time, for instance
        // if a file 'Test' is renamed to 'test' in the same folder, its canonical path would still be 'Test'
        // if it was resolved prior to the renaming and thus be recognized as a symbolic link
        try {
            String canonicalPath = file.getCanonicalPath();
            // Append separator for directories
            if(isDirectory() && !canonicalPath.endsWith(SEPARATOR))
                canonicalPath = canonicalPath + SEPARATOR;

            return canonicalPath;
        }
        catch(IOException e) {
            return absPath;
        }
    }

    @Override
    public String getSeparator() {
        return SEPARATOR;
    }

    @Override
    public AbstractFile[] ls(FilenameFilter filenameFilter) throws IOException {
        File files[] = file.listFiles(filenameFilter==null?null:new UNCFilenameFilter(filenameFilter));

        if(files==null)
            throw new IOException();

        int nbFiles = files.length;
        AbstractFile children[] = new AbstractFile[nbFiles];
        FileURL childURL;
        File file;

        for(int i=0; i<nbFiles; i++) {
            file = files[i];

            // Clone the FileURL of this file and set the child's path, this is more efficient than creating a new
            // FileURL instance from scratch.
            childURL = (FileURL)fileURL.clone();

            childURL.setPath(addTrailingSeparator(fileURL.getPath())+file.getName());

            // Retrieves an AbstractFile (LocalFile or AbstractArchiveFile) instance that's potentially already in
            // the cache, reuse this file as the file's parent, and the already-created java.io.File instance.
            children[i] = FileFactory.getFile(childURL, this, file);
        }

        return children;
    }

    @Override
    public boolean isHidden() {
        return file.isHidden();
    }

    /**
     * TODO
     */
    @Override
    public AbstractFile getRoot() {
    	String[] splittedBySeparator = absPath.split("\\\\");
    	return FileFactory.getFile(SEPARATOR + SEPARATOR + splittedBySeparator[2] + SEPARATOR + splittedBySeparator[3]);
    }

    /**
     * TODO
     */
    @Override
    public boolean isRoot() {
    	return countIndexOf(absPath, "\\\\") <= 3;
    }

	private int countIndexOf(String text, String search) {
		return text.split(search).length - 1;
	}
	
    /**
     * Overridden to return the local volume on which this file is located. The returned volume is one of the volumes
     * returned by {@link #getVolumes()}.
     */
    @Override
    public AbstractFile getVolume() {
        AbstractFile[] volumes = LocalFile.getVolumes();

        // Looks for the volume that best matches this file, i.e. the volume that is the deepest parent of this file.
        // If this file is itself a volume, return it.
        int bestDepth = -1;
        int bestMatch = -1;
        int depth;
        AbstractFile volume;
        String volumePath;
        String thisPath = getAbsolutePath(true);

        for(int i=0; i<volumes.length; i++) {
            volume = volumes[i];
            volumePath = volume.getAbsolutePath(true);

            if(thisPath.equals(volumePath)) {
                return this;
            }
            else if(thisPath.startsWith(volumePath)) {
                depth = PathUtils.getDepth(volumePath, volume.getSeparator());
                if(depth>bestDepth) {
                    bestDepth = depth;
                    bestMatch = i;
                }
            }
        }

        if(bestMatch!=-1)
            return volumes[bestMatch];

        // If no volume matched this file (shouldn't normally happen), return the root folder
        return getRoot();
    }

    
    /**
     * Returns the total and free space on the volume where this file resides.
     *
     * <p>Using this method to retrieve both free space and volume space is more efficient than calling
     * {@link #getFreeSpace()} and {@link #getTotalSpace()} separately -- the underlying method retrieving both
     * attributes at the same time.</p>
     *
     * @return a {totalSpace, freeSpace} long array, both values can be null if the information could not be retrieved
     * @throws IOException if an I/O error occurred
     */
    public long[] getVolumeInfo() throws IOException {
        // Under Java 1.6 and up, use the (new) java.io.File methods
        if(JavaVersion.JAVA_1_6.isCurrentOrHigher()) {
            return new long[] {
                getTotalSpace(),
                getFreeSpace()
            };
        }

        // Under Java 1.5 or lower, use native methods
        return getNativeVolumeInfo();
    }

    /**
     * Uses platform dependent functions to retrieve the total and free space on the volume where this file resides.
     *
     * @return a {totalSpace, freeSpace} long array, both values can be <code>null</code> if the information could not
     * be retrieved.
     * @throws IOException if an I/O error occurred
     */
    protected long[] getNativeVolumeInfo() throws IOException {
        BufferedReader br = null;
        String absPath = getAbsolutePath();
        long dfInfo[] = new long[]{-1, -1};

        try {
                // Use the Kernel32 DLL if it is available
                if(Kernel32.isAvailable()) {
                    // Retrieves the total and free space information using the GetDiskFreeSpaceEx function of the
                    // Kernel32 API.
                    LongByReference totalSpaceLBR = new LongByReference();
                    LongByReference freeSpaceLBR = new LongByReference();

                    if(Kernel32.getInstance().GetDiskFreeSpaceEx(absPath, null, totalSpaceLBR, freeSpaceLBR)) {
                        dfInfo[0] = totalSpaceLBR.getValue();
                        dfInfo[1] = freeSpaceLBR.getValue();
                    }
                    else {
                        LOGGER.warn("Call to GetDiskFreeSpaceEx failed, absPath={}", absPath);
                    }
                }
                // Otherwise, parse the output of 'dir "filePath"' command to retrieve free space information, if
                // running Window NT or higher.
                // Note: no command invocation under Windows 95/98/Me, because it causes a shell window to
                // appear briefly every time this method is called (See ticket #63).
                else if(OsVersion.WINDOWS_NT.isCurrentOrHigher()) {
                    // 'dir' command returns free space on the last line
                    Process process = Runtime.getRuntime().exec(
                            (OsVersion.getCurrent().compareTo(OsVersion.WINDOWS_NT)>=0 ? "cmd /c" : "command.com /c")
                            + " dir \""+absPath+"\"");

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
        }
        finally {
            if(br!=null)
                try { br.close(); } catch(IOException e) {}
        }

        return dfInfo;
    }

    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * A Permissions implementation for LocalFile.
     */
    private static class UNCFilePermissions extends IndividualPermissionBits implements FilePermissions {
        
        private java.io.File file;

        // Permissions are limited to the user access type. Executable permission flag is only available under Java 1.6
        // and up.
        // Note: 'read' and 'execute' permissions have no meaning under Windows (files are either read-only or
        // read-write), but we return default values.

        /** Mask for supported permissions under Java 1.6 */
        private static PermissionBits JAVA_1_6_PERMISSIONS = new GroupedPermissionBits(448);   // rwx------ (700 octal)

        /** Mask for supported permissions under Java 1.5 */
        private static PermissionBits JAVA_1_5_PERMISSIONS = new GroupedPermissionBits(384);   // rw------- (300 octal)

        private final static PermissionBits MASK = JavaVersion.JAVA_1_6.isCurrentOrHigher()
                ?JAVA_1_6_PERMISSIONS
                :JAVA_1_5_PERMISSIONS;

        private UNCFilePermissions(java.io.File file) {
            this.file = file;
        }

        public boolean getBitValue(int access, int type) {
            // Only the 'user' permissions are supported
            if(access!=USER_ACCESS)
                return false;

            if(type==READ_PERMISSION)
                return file.canRead();
            else if(type==WRITE_PERMISSION)
                return file.canWrite();
            // Execute permission can only be retrieved under Java 1.6 and up
            else if(type==EXECUTE_PERMISSION && JavaVersion.JAVA_1_6.isCurrentOrHigher())
                return file.canExecute();

            return false;
        }

        /**
         * Overridden for performance reasons.
         */
        @Override
        public int getIntValue() {
            int userPerms = 0;

            if(getBitValue(USER_ACCESS, READ_PERMISSION))
                userPerms |= READ_PERMISSION;

            if(getBitValue(USER_ACCESS, WRITE_PERMISSION))
                userPerms |= WRITE_PERMISSION;

            if(getBitValue(USER_ACCESS, EXECUTE_PERMISSION))
                userPerms |= EXECUTE_PERMISSION;

            return userPerms<<6;
        }

        public PermissionBits getMask() {
            return MASK;
        }
    }
    
    /**
     * Turns a {@link FilenameFilter} into a {@link java.io.FilenameFilter}.
     */
    private static class UNCFilenameFilter implements java.io.FilenameFilter {

        private FilenameFilter filter;

        private UNCFilenameFilter(FilenameFilter filter) {
            this.filter = filter;
        }


        ///////////////////////////////////////////
        // java.io.FilenameFilter implementation //
        ///////////////////////////////////////////

        public boolean accept(File dir, String name) {
            return filter.accept(name);
        }
    }
}
