/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.file.impl.local;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileProtocols;
import com.mucommander.file.FileURL;
import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.io.FileTransferException;
import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.io.RandomAccessOutputStream;
import com.mucommander.process.AbstractProcess;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;


/**
 * LocalFile provides access to files located on a locally-mounted filesystem. 
 * Note that despite the class' name, LocalFile instances may indifferently be residing on a local hard drive,
 * or on a remote server mounted locally by the operating system.
 *
 * <p>The associated {@link FileURL} protocol is {@link FileProtocols#FILE}. The host part should be {@link FileURL#LOCALHOST},
 * except for Windows UNC URLs (see below). Native path separators ('/' or '\\' depending on the OS) can be used
 * in the path part.
 *
 * <p>Here are a few examples of valid local file URLs:
 * <code>
 * file://localhost/C:\winnt\system32\<br>
 * file://localhost/usr/bin/gcc<br>
 * file://localhost/~<br>
 * file://home/maxence/..<br>
 * </code>
 *
 * <p>Windows UNC paths can be represented as FileURL instances, using the host part of the URL. The URL format for
 * those is the following:<br>
 * <code>file:\\server\share</code> .<br>
 *
 * <p>Under Windows, LocalFile will translate those URLs back into a UNC path. For example, a LocalFile created with the
 * <code>file://garfield/stuff</code> FileURL will have the <code>getAbsolutePath()</code> method return
 * <code>\\garfield\stuff</code>. Note that this UNC path translation doesn't happen on OSes other than Windows, which
 * would not be able to handle the path.
 *
 * <p>Access to local files is provided by the <code>java.io</code> API, {@link #getUnderlyingFileObject()} allows
 * to retrieve an <code>java.io.File</code> instance corresponding to this LocalFile.
 *
 * @author Maxence Bernard
 */
public class LocalFile extends AbstractFile {

    private File file;
    private String parentFilePath;
    private String absPath;

    /** Caches the parent folder, initially null until getParent() gets called */
    private AbstractFile parent;
    /** Indicates whether the parent folder instance has been retrieved and cached or not (parent can be null) */
    private boolean parentValueSet;
	
    /** Underlying local filesystem's path separator: "/" under UNIX systems, "\" under Windows and OS/2 */
    public final static String SEPARATOR = File.separator;

    /** true if the underlying local filesystem uses drives assigned to letters (e.g. A:\, C:\, ...) instead
     * of having single a root folder '/' */
    public final static boolean USES_ROOT_DRIVES = PlatformManager.isWindowsFamily() || PlatformManager.getOsFamily()==PlatformManager.OS_2;

    /** Are we running Windows ? */
    private final static boolean IS_WINDOWS;

		
    static {
        IS_WINDOWS = PlatformManager.isWindowsFamily();
    }


    /**
     * Creates a new instance of LocalFile. The given FileURL's protocol should be {@link FileProtocols#FILE}, and the
     * host {@link FileURL#LOCALHOST}.  
     */
    public LocalFile(FileURL fileURL) throws IOException {
        super(fileURL);

        String path = fileURL.getPath();

        // If OS is Windows and hostname is not 'localhost', translate path back
        // into a Windows-style UNC network path ( \\hostname\path )
        String hostname = fileURL.getHost();
        if(IS_WINDOWS && !FileURL.LOCALHOST.equals(hostname))
            path = "\\\\"+hostname+fileURL.getPath().replace('/', '\\');    // Replace leading / char by \

        this.file = new File(path);

        // Throw an exception is the file's path is not absolute.
        if(!file.isAbsolute())
            throw new IOException();

        this.parentFilePath = file.getParent();
        this.absPath = file.getAbsolutePath();

        // removes trailing separator (if any)
        this.absPath = absPath.endsWith(SEPARATOR)?absPath.substring(0,absPath.length()-1):absPath;
    }


    /**
     * Uses platform dependant commands to extract free and total space on the volume where this file resides.
     *
     * <p>This method has been made public as it is more efficient to retrieve both free space and volume space
     * info than calling getFreeSpace() and getTotalSpace() separately, since a single command process retrieves both.
     *
     * @return [totalSpace, freeSpace], both of which can be null if information could not be retrieved.
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
                Process process = Runtime.getRuntime().exec(PlatformManager.getDefaultShellCommand() + " dir \""+absPath+"\"");

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
            // Parses the output of 'df -k "filePath"' command on UNIX-based systems to retrieve free and total space information
            else {
                // 'df -k' returns totals in block of 1K = 1024 bytes
                Process process = Runtime.getRuntime().exec(new String[]{"df", "-k", absPath}, null, file);
				
                // Check that the process was correctly started
                if(process!=null) {
                    br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    // Discard the first line ("Filesystem   1K-blocks     Used    Avail Capacity  Mounted on");
                    br.readLine();
                    String line = br.readLine();

                    // Sample lines:
                    // /dev/disk0s2            116538416 109846712  6179704    95%    /
                    // automount -fstab [202]          0         0        0   100%    /automount/Servers
                    // /dev/disk2s2                 2520      1548      972    61%    /Volumes/muCommander 0.8

                    // We're interested in the '1K-blocks' and 'Avail' fields (only).
                    // The 'Filesystem' and 'Mounted On' fields can contain spaces (e.g. 'automount -fstab [202]' and
                    // '/Volumes/muCommander 0.8' resp.) and therefore be made of several tokens. A stable way to
                    // determine the position of the fields we're interested in is to look for the last token that
                    // starts with a '/' character which should necessarily correspond to the first token of the 
                    // 'Mounted on' field. The '1K-blocks' and 'Avail' fields are 4 and 2 tokens away from it
                    // respectively.

                    // Start by tokenizing the whole line
                    Vector tokenV = new Vector();
                    if(line!=null) {
                        StringTokenizer st = new StringTokenizer(line);
                        while(st.hasMoreTokens())
                            tokenV.add(st.nextToken());
                    }

                    int nbTokens = tokenV.size();
                    if(nbTokens<6) {
                        // This shouldn't normally happen
                        if(Debug.ON) Debug.trace("Failed to parse output of df -k "+absPath+" line="+line);
                        return dfInfo;
                    }

                    // Find the last token starting with '/'
                    int pos = nbTokens-1;
                    while(!((String)tokenV.elementAt(pos)).startsWith("/")) {
                        if(pos==0) {
                            // This shouldn't normally happen
                            if(Debug.ON) Debug.trace("Failed to parse output of df -k "+absPath+" line="+line);
                            return dfInfo;
                        }

                        --pos;
                    }

                    // '1-blocks' field (total space)
                    dfInfo[0] = Long.parseLong((String)tokenV.elementAt(pos-4)) * 1024;
                    // 'Avail' field (free space)
                    dfInfo[1] = Long.parseLong((String)tokenV.elementAt(pos-2)) * 1024;
                }
            }
        }
        catch(Exception e) {	// Could be IOException, NoSuchElementException or NumberFormatException, but better be safe and catch Exception
            if(com.mucommander.Debug.ON) {
                com.mucommander.Debug.trace("Exception thrown while retrieving volume info: "+e);
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
     * Guesses if this drive is a floppy drive. This method will only return true when tested against the floppy drive's
     * root folder (e.g. A:\ under Windows).
     *
//     * <p>The result of this method should be accurate under Java 1.4 and up, just a guess under Java 1.3
//     * running under Windows, will return false for Java 1.3 running under a non-Windows platform.</p>
     */
    public boolean guessFloppyDrive() {
        if(PlatformManager.isWindowsFamily() && !isRoot())
            return false;

        // Use FileSystemView.isFloppyDrive(File) to determine if this file
        // is a floppy drive. This method being available only in Java 1.4 and up.
//        if(PlatformManager.JAVA_VERSION>=PlatformManager.JAVA_1_4)
        return FileSystemView.getFileSystemView().isFloppyDrive(file);

//        // We're running Java 1.3 or below, try to guess if file is floppy drive under Windows
//        if(IS_WINDOWS && absPath.equals("A:") || absPath.equals("B:"))
//            return true;
//
//        // No clue, return false
//        return false;
    }
	
    /**
     * Guesses if this drive is a removable media drive (Floppy/CD/DVD). This method will only return true when tested
     * against the drive's root folder (e.g. D:\ under Windows).
     *
     * <p>The result is just a guess that works rather well under Windows.</p>
     */
    public boolean guessRemovableDrive() {
        // A weak way to characterize such a drive is to check if the corresponding root folder is a floppy drive or 
        // read-only. A better way would be to create a JNI interface as described here: http://forum.java.sun.com/thread.jspa?forumID=256&threadID=363074
        return guessFloppyDrive() || (IS_WINDOWS && isRoot() && !file.canWrite());
    }


    /**
     * Returns true if the underlying local filesystem uses drives assigned to letters (e.g. A:\, C:\, ...) instead
     * of having single a root folder '/'. This method will return <code>true</code> for Windows and OS/2 systems,
     * false for all other systems.
     */
    public static boolean usesRootDrives() {
        return PlatformManager.isWindowsFamily() || PlatformManager.getOsFamily()==PlatformManager.OS_2;
    }


    /**
     * Returns a <code>java.io.File</code> instance corresponding to this file.
     */
    public Object getUnderlyingFileObject() {
        return file;
    }
    
	
    /////////////////////////////////////////
    // AbstractFile methods implementation //
    /////////////////////////////////////////

    public boolean isSymlink() {
        // Note: this value must not be cached as its value can change over time (canonical path can change)
        LocalFile parent = (LocalFile)getParent();
        String canonPath = getCanonicalPath(false);
        if(parent==null || canonPath==null)
            return false;
        else {
            String parentCanonPath = parent.getCanonicalPath(true);
            return !canonPath.equals(parentCanonPath+getName());
        }
    }

    public long getDate() {
        return file.lastModified();
    }
	
    public boolean changeDate(long lastModified) {
        // java.io.File#setLastModified(long) throws an IllegalArgumentException if time is negative.
        // If specified time is negative, set it to 0 (01/01/1970).
        if(lastModified < 0)
            lastModified = 0;

        return file.setLastModified(lastModified);
    }
		
    public long getSize() {
        return file.length();
    }
	
    public AbstractFile getParent() {
        // Retrieve parent AbstractFile and cache it
        if (!parentValueSet) {
            if(parentFilePath !=null) {
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
	

    public boolean getPermission(int access, int permission) {
        if(access!= USER_ACCESS)
            return false;

        if(permission==READ_PERMISSION)
            return file.canRead();
        else if(permission==WRITE_PERMISSION)
            return file.canWrite();
        else if(permission==EXECUTE_PERMISSION && PlatformManager.getJavaVersion() >= PlatformManager.JAVA_1_6)
            return file.canExecute();

        return false;
    }

    public boolean setPermission(int access, int permission, boolean enabled) {
        if(access!= USER_ACCESS || PlatformManager.getJavaVersion() < PlatformManager.JAVA_1_6)
            return false;

        if(permission==READ_PERMISSION)
            return file.setReadable(enabled);
        else if(permission==WRITE_PERMISSION)
            return file.setWritable(enabled);
        else if(permission==EXECUTE_PERMISSION && PlatformManager.getJavaVersion() >= PlatformManager.JAVA_1_6)
            return file.setExecutable(enabled);

        return false;
    }

    public boolean canGetPermission(int access, int permission) {
        // Get permission support is limited to the user access type. Executable permission flag is only available under
        // Java 1.6 and up.
        if(access!= USER_ACCESS)
            return false;

        return permission!=EXECUTE_PERMISSION || PlatformManager.getJavaVersion() >= PlatformManager.JAVA_1_6;
    }

    public boolean canSetPermission(int access, int permission) {
        // Set permission support is only available under Java 1.6 and up and is limited to the user access type
        if(access!= USER_ACCESS)
            return false;

        return PlatformManager.getJavaVersion() >= PlatformManager.JAVA_1_6;
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

    public OutputStream getOutputStream(boolean append) throws IOException {
        return new FileOutputStream(absPath, append);
    }

    public boolean hasRandomAccessInputStream() {
        return true;
    }

    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        return new LocalRandomAccessInputStream(new RandomAccessFile(file, "r"));
    }

    public boolean hasRandomAccessOutputStream() {
        return true;
    }

    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException {
        return new LocalRandomAccessOutputStream(new RandomAccessFile(file, "rw"));
    }

    public void delete() throws IOException {
        boolean ret = file.delete();
		
        if(!ret)
            throw new IOException();
    }


    public AbstractFile[] ls() throws IOException {
        return ls((FilenameFilter)null);
    }

    public void mkdir() throws IOException {
        if(!new File(absPath).mkdir())
            throw new IOException();
    }
	

    public long getFreeSpace() {
        if(PlatformManager.getJavaVersion() >= PlatformManager.JAVA_1_6)
            return file.getFreeSpace();

        return getVolumeInfo()[1];
    }
	
    public long getTotalSpace() {
        if(PlatformManager.getJavaVersion() >= PlatformManager.JAVA_1_6)
            return file.getTotalSpace();

        return getVolumeInfo()[0];
    }	


    /**
     * Always returns <code>true</code>.
     * @return <code>true</code>
     */
    public boolean canRunProcess() {
        return true;
    }

    /**
     * Returns a process executing the specied local command.
     * @param  tokens      describes the command and its arguments.
     * @throws IOException if an error occured while creating the process.
     */
    public AbstractProcess runProcess(String[] tokens) throws IOException {
        if(!isDirectory()) {
            if(Debug.ON) Debug.trace("Tried to create a process using a file as a working directory.");
            throw new IOException(file + " is not a directory");
        }
        return new LocalProcess(tokens, file);
    }

    
    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public String getName() {
        return parentFilePath ==null?absPath+SEPARATOR:file.getName();
    }

    public String getAbsolutePath() {
        // Append separator for root folders (C:\ , /) and for directories
        if(parentFilePath ==null || (isDirectory() && !absPath.endsWith(SEPARATOR)))
            return absPath+SEPARATOR;

        return absPath;
    }


    public String getCanonicalPath() {
        // To avoid drive seeks and potential 'floppy drive not available' dialog under Win32
        // triggered by java.io.File.getCanonicalPath()
        if(IS_WINDOWS && guessFloppyDrive())
            return absPath;

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
        FileURL childURL;
        for(int i=0; i<names.length; i++) {
            // Clone the FileURL of this file and set the child's path, this is more efficient than creating a new
            // FileURL instance from scratch.
            childURL = (FileURL)fileURL.clone();
            childURL.setPath(absPath+SEPARATOR+names[i]);

            // Retrieves an AbstractFile (LocalFile or AbstractArchiveFile) instance potentially fetched from the
            // LRU cache and reuse this file as parent
            children[i] = FileFactory.getFile(childURL, this);
        }

        return children;
    }


    /**
     * Overrides {@link AbstractFile#moveTo(AbstractFile)} to move/rename the file directly if the destination file
     * is also a local file.
     */
    public boolean moveTo(AbstractFile destFile) throws FileTransferException  {
        if(!destFile.getURL().getProtocol().equals(FileProtocols.FILE)) {
            return super.moveTo(destFile);
        }

        // If destination file is not a LocalFile nor has a LocalFile ancestor (for instance an archive entry),
        // renaming won't work so use the default moveTo() implementation instead
        destFile = destFile.getTopAncestor();
        if(!(destFile instanceof LocalFile)) {
            return super.moveTo(destFile);
        }

        // Move file
        return file.renameTo(((LocalFile)destFile).file);
    }


    public boolean isHidden() {
        return file.isHidden();
    }


    /**
     * Overridden for performance reasons.
     */
    public int getPermissionGetMask() {
        // Get permission support is limited to the user access type. Executable permission flag is only available under
        // Java 1.6 and up.
        return PlatformManager.getJavaVersion() >= PlatformManager.JAVA_1_6?
                448         // rwx------ (700 octal)
                :384;       // rw------- (300 octal)
    }

    /**
     * Overridden for performance reasons.
     */
    public int getPermissionSetMask() {
        // Set permission support is only available under Java 1.6 and up and is limited to the user access type
        return PlatformManager.getJavaVersion() >= PlatformManager.JAVA_1_6?
                448         // rwx------ (700 octal)
                :0;         // --------- (0 octal)
    }

    /**
     * Overridden for performance reasons. This method doesn't iterate like {@link AbstractFile#getRoot()} to resolve
     * the root file.
     */
    public AbstractFile getRoot() {
        if(PlatformManager.isWindowsFamily()) {
            // Extract drive letter from the path
            Matcher matcher = windowsDriveRootPattern.matcher(absPath);
            if(matcher.matches())
                return FileFactory.getFile(absPath.substring(matcher.start(), matcher.end()));
        }
        else if(SEPARATOR.equals("/")) {
            return FileFactory.getFile("/");
        }

        return super.getRoot();
    }

    /**
     * Overridden to return {@link #SHOULD_NOT_HINT} under Windows when the destination file is located on a different
     * drive from this file (e.g. C:\ and E:\).
     */
    public int getMoveToHint(AbstractFile destFile) {
        int moveHint = super.getMoveToHint(destFile);
        if(moveHint!=SHOULD_HINT && moveHint!=MUST_HINT)
            return moveHint;

        // This check is necessary under Windows because java.io.File#renameTo(java.io.File) does not return false
        // if the destination file is located on a different drive, contrary for example to Mac OS X where renameTo
        // returns false in this case.
        // Not doing this under Windows would mean files would get moved between drives with renameTo, which doesn't
        // allow the transfer to be monitored.
        // Note that Windows UNC paths are handled by the super method when comparing hosts for equality.  
        if(PlatformManager.isWindowsFamily() && !getRoot().equals(destFile.getRoot()))
            return SHOULD_NOT_HINT;

        return moveHint;
    }

    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * LocalRandomAccessInputStream extends RandomAccessInputStream to provide random read access to a LocalFile.
     */
    public class LocalRandomAccessInputStream extends RandomAccessInputStream {

        private RandomAccessFile raf;

        public LocalRandomAccessInputStream(RandomAccessFile raf) {
            this.raf = raf;
        }

        public int read() throws IOException {
            return raf.read();
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

        public void seek(long offset) throws IOException {
            raf.seek(offset);
        }
    }

    /**
     * LocalRandomAccessOutputStream extends RandomAccessOutputStream to provide random write access to a LocalFile.
     */
    public class LocalRandomAccessOutputStream extends RandomAccessOutputStream {

        private RandomAccessFile raf;

        public LocalRandomAccessOutputStream(RandomAccessFile raf) {
            this.raf = raf;
        }

        public void write(int i) throws IOException {
            raf.write(i);
        }

        public void write(byte b[]) throws IOException {
            raf.write(b);
        }

        public void write(byte b[], int off, int len) throws IOException {
            raf.write(b, off, len);
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

        public void seek(long offset) throws IOException {
            raf.seek(offset);
        }

        public void setLength(long newLength) throws IOException {
            raf.setLength(newLength);
        }
    }
}
