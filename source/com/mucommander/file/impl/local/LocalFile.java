/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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
import com.mucommander.file.*;
import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.file.util.Kernel32;
import com.mucommander.file.util.Kernel32API;
import com.mucommander.file.util.PathUtils;
import com.mucommander.io.*;
import com.mucommander.process.AbstractProcess;
import com.mucommander.runtime.*;
import com.sun.jna.ptr.LongByReference;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * LocalFile provides access to files located on a locally-mounted filesystem. 
 * Note that despite the class' name, LocalFile instances may indifferently be residing on a local hard drive,
 * or on a remote server mounted locally by the operating system.
 *
 * <p>The associated {@link FileURL} scheme is {@link FileProtocols#FILE}. The host part should be {@link FileURL#LOCALHOST},
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

    protected File file;
    private FilePermissions permissions;

    protected String parentFilePath;
    protected String absPath;

    /** True if this file has a Windows-style UNC path. Can be true only under Windows. */
    private boolean isUNC;

    /** Caches the parent folder, initially null until getParent() gets called */
    protected AbstractFile parent;
    /** Indicates whether the parent folder instance has been retrieved and cached or not (parent can be null) */
    protected boolean parentValueSet;
	
    /** Underlying local filesystem's path separator: "/" under UNIX systems, "\" under Windows and OS/2 */
    public final static String SEPARATOR = File.separator;

    /** Are we running Windows ? */
    private final static boolean IS_WINDOWS =  OsFamilies.WINDOWS.isCurrent();

    /** True if the underlying local filesystem uses drives assigned to letters (e.g. A:\, C:\, ...) instead
     * of having single a root folder '/' */
    public final static boolean USES_ROOT_DRIVES = IS_WINDOWS || OsFamilies.OS_2.isCurrent();

    /** Pattern matching Windows-like drives' root, e.g. C:\ */
    final static Pattern driveRootPattern = Pattern.compile("^[a-zA-Z]{1}[:]{1}[\\\\]{1}");

    // Permissions can only be changed under Java 1.6 and up and are limited to 'user' access.
    // Note: 'read' and 'execute' permissions have no meaning under Windows (files are either read-only or
    // read-write) and as such can't be changed.

    /** Changeable permissions mask for Java 1.6 and up, on OSes other than Windows */
    private static PermissionBits CHANGEABLE_PERMISSIONS_JAVA_1_6_NON_WINDOWS = new GroupedPermissionBits(448);   // rwx------ (700 octal)

    /** Changeable permissions mask for Java 1.6 and up, on Windows OS (any version) */
    private static PermissionBits CHANGEABLE_PERMISSIONS_JAVA_1_6_WINDOWS = new GroupedPermissionBits(128);   // -w------- (200 octal)

    /** Changeable permissions mask for Java 1.5 or below */
    private static PermissionBits CHANGEABLE_PERMISSIONS_JAVA_1_5 = PermissionBits.EMPTY_PERMISSION_BITS;   // --------- (0)

    /** Bit mask that indicates which permissions can be changed */
    private final static PermissionBits CHANGEABLE_PERMISSIONS = JavaVersions.JAVA_1_6.isCurrentOrHigher()
            ?(IS_WINDOWS?CHANGEABLE_PERMISSIONS_JAVA_1_6_WINDOWS:CHANGEABLE_PERMISSIONS_JAVA_1_6_NON_WINDOWS)
            : CHANGEABLE_PERMISSIONS_JAVA_1_5;

    static {
        // Prevents Windows from poping up a message box when it cannot find a file. Those message box are triggered by
        // java.io.File methods when operating on removable drives such as floppy or CD-ROM drives which have no disk
        // inserted.
        // This has been fixed in Java 1.6 b55 but this fixes previous versions of Java.
        // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4089199
        if(IS_WINDOWS && Kernel32.isAvailable())
            Kernel32.getInstance().SetErrorMode(Kernel32API.SEM_NOOPENFILEERRORBOX|Kernel32API.SEM_FAILCRITICALERRORS);
    }


    /**
     * Creates a new instance of LocalFile. The given FileURL's scheme should be {@link FileProtocols#FILE}, and the
     * host {@link FileURL#LOCALHOST}.  
     */
    protected LocalFile(FileURL fileURL) throws IOException {
        super(fileURL);

        String path = fileURL.getPath();

        // If OS is Windows and hostname is not 'localhost', translate the path back into a Windows-style UNC path
        // in the form \\hostname\share\path .
        String hostname = fileURL.getHost();
        if(IS_WINDOWS && !FileURL.LOCALHOST.equals(hostname)) {
            path = "\\\\"+hostname+fileURL.getPath().replace('/', '\\');    // Replace leading / char by \
            isUNC = true;
        }

        this.file = new File(path);
        this.permissions = new LocalFilePermissions(file);

        // Throw an exception is the file's path is not absolute.
        if(!file.isAbsolute())
            throw new IOException();

        this.parentFilePath = file.getParent();
        this.absPath = file.getAbsolutePath();

        // removes trailing separator (if any)
        this.absPath = absPath.endsWith(SEPARATOR)?absPath.substring(0,absPath.length()-1):absPath;
    }


    ////////////////////////////////
    // LocalFile-specific methods //
    ////////////////////////////////

    /**
     * Returns the user home folder. Most if not all OSes have one, but in the unlikely event that the OS doesn't have
     * one or that the folder cannot be resolved, <code>null</code> will be returned.
     *
     * @return the user home folder
     */
    public static AbstractFile getUserHome() {
        String userHomePath = System.getProperty("user.home");
        if(userHomePath==null)
            return null;

        return FileFactory.getFile(userHomePath);
    }

    /**
     * Returns the total and free space on the volume where this file resides.
     *
     * <p>Using this method to retrieve both free space and volume space is more efficient than calling
     * {@link #getFreeSpace()} and {@link #getTotalSpace()} separately -- the underlying method retrieving both
     * attributes at the same time.</p>
     *
     * @return a {totalSpace, freeSpace} long array, both values can be null if the information could not be retrieved
     */
    public long[] getVolumeInfo() {
        // Under Java 1.6 and up, use the (new) java.io.File methods
        if(JavaVersions.JAVA_1_6.isCurrentOrHigher()) {
            return new long[] {
                getTotalSpace(),
                getFreeSpace()
            };
        }

        // Under Java 1.5 or lower, use native methods
        return getNativeVolumeInfo();
    }

    /**
     * Uses platform dependant functions to retrieve the total and free space on the volume where this file resides.
     *
     * @return a {totalSpace, freeSpace} long array, both values can be <code>null</code> if the information could not
     * be retrieved.
     */
    protected long[] getNativeVolumeInfo() {
        BufferedReader br = null;
        String absPath = getAbsolutePath();
        long dfInfo[] = new long[]{-1, -1};

        try {
            // OS is Windows
            if(IS_WINDOWS) {
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
                        if(Debug.ON) Debug.trace("Call to GetDiskFreeSpaceEx failed, absPath="+absPath);
                    }
                }
                // Otherwise, parse the output of 'dir "filePath"' command to retrieve free space information
                else {
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
            else if(OsFamily.getCurrent().isUnixBased()) {
                // Parses the output of 'df -P -k "filePath"' command on UNIX-based systems to retrieve free and total space information

                // 'df -P -k' returns totals in block of 1K = 1024 bytes, -P uses the POSIX output format, ensures that line won't break
                Process process = Runtime.getRuntime().exec(new String[]{"df", "-P", "-k", absPath}, null, file);

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

//                // Retrieves the total and free space information using the POSIX statvfs function
//                POSIX.STATVFSSTRUCT struct = new POSIX.STATVFSSTRUCT();
//                if(POSIX.INSTANCE.statvfs(absPath, struct)==0) {
//                    dfInfo[0] = struct.f_blocks * (long)struct.f_frsize;
//                    dfInfo[1] = struct.f_bfree * (long)struct.f_frsize;
//                }
            }
        }
        catch(Throwable e) {	// JNA throws a java.lang.UnsatisfiedLinkError if the native can't be found
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
     * Attemps to detect if this file is the root of a removable media drive (floppy, CD, DVD, USB drive...).
     * This method produces accurate results only under Windows.
     *
     * @return <code>true</code> if this file is the root of a removable media drive (floppy, CD, DVD, USB drive...). 
     */
    public boolean guessRemovableDrive() {
        if(IS_WINDOWS && Kernel32.isAvailable()) {
            int driveType = Kernel32.getInstance().GetDriveType(getAbsolutePath(true));
            if(driveType!=Kernel32API.DRIVE_UNKNOWN)
                return driveType==Kernel32API.DRIVE_REMOVABLE || driveType==Kernel32API.DRIVE_CDROM;
        }


        // For other OS that have root drives (OS/2), a weak way to characterize removable drives is by checking if the
        // corresponding root folder is read-only.
        return hasRootDrives() && isRoot() && !file.canWrite();
    }


    /**
     * Returns <code>true</code> if the underlying local filesystem uses drives assigned to letters (e.g. A:\, C:\, ...)
     * instead of having a single root folder '/' under which mount points are attached.
     * This is <code>true</code> for the following platforms:
     * <ul>
     *  <li>Windows</li>
     *  <li>OS/2</li>
     *  <li>Any other platform that has '\' for a path separator</li>
     * </ul>
     *
     * @return <code>true</code> if the underlying local filesystem uses drives assigned to letters
     */
    public static boolean hasRootDrives() {
        return IS_WINDOWS
            || OsFamilies.OS_2.isCurrent()
            || "\\".equals(SEPARATOR);
    }


    /**
     * Resolves and returns all local volumes:
     * <ul>
     *   <li>On UNIX-based OSes, these are the mount points declared in <code>/etc/ftab</code>.</li>
     *   <li>On the Windows platform, these are the drives displayed in Explorer. Some of the returned volumes may
     * correspond to removable drives and thus may not always be available -- if they aren't, {@link #exists()} will
     * return <code>false</code>.</li>
     * </ul>
     * <p>
     * The return list of volumes is purposively not cached so that new volumes will be returned as soon as they are
     * mounted.
     * </p>
     *
     * @return all local volumes
     */
    public static AbstractFile[] getVolumes() {
        Vector volumesV = new Vector();

        // Add Mac OS X's /Volumes subfolders and not file roots ('/') since Volumes already contains a named link
        // (like 'Hard drive' or whatever silly name the user gave his primary hard disk) to /
        if(OsFamilies.MAC_OS_X.isCurrent()) {
            addMacOSXVolumes(volumesV);
        }
        else {
            // Add java.io.File's root folders
            addJavaIoFileRoots(volumesV);

            // Add /etc/fstab folders under UNIX-based systems.
            if(OsFamily.getCurrent().isUnixBased())
                addFstabEntries(volumesV);
        }

        // Add home folder, if it is not already present in the list
        AbstractFile homeFolder = getUserHome();
        if(!(homeFolder==null || volumesV.contains(homeFolder)))
            volumesV.add(homeFolder);

        AbstractFile volumes[] = new AbstractFile[volumesV.size()];
        volumesV.toArray(volumes);

        return volumes;
    }


    ////////////////////
    // Helper methods //
    ////////////////////

    /**
     * Resolves the root folders returned by {@link File#listRoots()} and adds them to the given <code>Vector</code>.
     *
     * @param v the <code>Vector</code> to add root folders to
     */
    private static void addJavaIoFileRoots(Vector v) {
        // Warning : No file operation should be performed on the resolved folders as under Win32, this would cause a
        // dialog to appear for removable drives such as A:\ if no disk is present.
        File fileRoots[] = File.listRoots();

        int nbFolders = fileRoots.length;
        for(int i=0; i<nbFolders; i++)
            try {
                v.add(FileFactory.getFile(fileRoots[i].getAbsolutePath(), true));
            }
            catch(IOException e) {}
    }

    /**
     * Parses <code>/etc/fstab</code>, resolves all the mount points it contains and adds them to the given
     * <code>Vector</code>.
     *
     * @param v the <code>Vector</code> to add mount points to
     */
    private static void addFstabEntries(Vector v) {
        BufferedReader br;

        br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream("/etc/fstab")));
            StringTokenizer st;
            String line;
            AbstractFile file;
            String folderPath;
            while((line=br.readLine())!=null) {
                // Skip comments
                // JS, 2008-09-25: Exclude empty lines too
                // JS, 2009-03-21: Exclude mount destination "swap" too
                line = line.trim();
                if(line.length() > 0 && !line.startsWith("#")) {
                    st = new StringTokenizer(line);
                    // path is second token
                    st.nextToken();
                    folderPath = st.nextToken();
                    if(!(folderPath.equals("/proc") || folderPath.equals("none") || folderPath.equals("/dev/shm") || folderPath.equals("swap"))) {
                        file = FileFactory.getFile(folderPath);
                        if(file!=null && !v.contains(file))
                            v.add(file);
                    }
                }
            }
        }
        catch(Exception e) {
            if(Debug.ON) Debug.trace("Error reading /etc/fstab entries: "+ e);
        }
        finally {
            if(br != null) {
                try {
                    br.close();
                }
                catch(IOException e) {}
            }
        }
    }

    /**
     * Adds all <code>/Volumes</code> subfolders to the given <code>Vector</code>.
     *
     * @param v the <code>Vector</code> to add the volumes to
     */
    private static void addMacOSXVolumes(Vector v) {
        // /Volumes not resolved for some reason, giving up
        AbstractFile volumesFolder = FileFactory.getFile("/Volumes");
        if(volumesFolder==null)
            return;

        // Adds subfolders
        try {
            AbstractFile volumesFiles[] = volumesFolder.ls();
            int nbFiles = volumesFiles.length;
            AbstractFile folder;
            for(int i=0; i<nbFiles; i++)
                if((folder=volumesFiles[i]).isDirectory()) {
                    // The primary hard drive (the one corresponding to '/') is listed under Volumes and should be
                    // returned as the first volume
                    if(folder.getCanonicalPath().equals("/"))
                        v.insertElementAt(folder, 0);
                    else
                        v.add(folder);
                }
        }
        catch(IOException e) {
            if(Debug.ON) Debug.trace("Can't get /Volumes subfolders: "+ e);
        }
    }


    /////////////////////////////////
    // AbstractFile implementation //
    /////////////////////////////////

    /**
     * Returns a <code>java.io.File</code> instance corresponding to this file.
     */
    public Object getUnderlyingFileObject() {
        return file;
    }

    public boolean isSymlink() {
        // At the moment symlinks under Windows (aka NTFS junction points) are not supported because java.io.File
        // knows nothing about them and there is no way to discriminate them. So there is no need to waste time
        // comparing canonical paths, just return false.
        // Todo: add support for .lnk files (~hard links)
        if(IS_WINDOWS)
            return false;

        // Note: this value must not be cached as its value can change over time (canonical path can change)
        AbstractFile parent = getParent();
        String canonPath = getCanonicalPath(false);
        if(parent==null || canonPath==null)
            return false;
        else {
            String parentCanonPath = parent.getCanonicalPath(true);
            return !canonPath.equalsIgnoreCase(parentCanonPath+getName());
        }
    }

    public long getDate() {
        return file.lastModified();
    }

    public boolean canChangeDate() {
        return true;
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
	
    public FilePermissions getPermissions() {
        return permissions;
    }

    public PermissionBits getChangeablePermissions() {
        return CHANGEABLE_PERMISSIONS;
    }

    public boolean changePermission(int access, int permission, boolean enabled) {
        // Only the 'user' permissions under Java 1.6 are supported
        if(access!=USER_ACCESS || JavaVersions.JAVA_1_6.isCurrentLower())
            return false;

        if(permission==READ_PERMISSION)
            return file.setReadable(enabled);
        else if(permission==WRITE_PERMISSION)
            return file.setWritable(enabled);
        else if(permission==EXECUTE_PERMISSION)
            return file.setExecutable(enabled);

        return false;
    }

    /**
     * Always returns <code>null</code>, this information is not available unfortunately.
     */
    public String getOwner() {
        return null;
    }

    /**
     * Always returns <code>false</code>, this information is not available unfortunately.
     */
    public boolean canGetOwner() {
        return false;
    }

    /**
     * Always returns <code>null</code>, this information is not available unfortunately.
     */
    public String getGroup() {
        return null;
    }

    /**
     * Always returns <code>false</code>, this information is not available unfortunately.
     */
    public boolean canGetGroup() {
        return false;
    }

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
    public InputStream getInputStream() throws IOException {
        return new LocalInputStream(new FileInputStream(file).getChannel());
    }

    /**
     * Implementation notes: the returned <code>InputStream</code> uses a NIO {@link FileChannel} under the hood to
     * benefit from <code>InterruptibleChannel</code> and allow a thread waiting for an I/O to be gracefully interrupted
     * using <code>Thread#interrupt()</code>.
     */
    public OutputStream getOutputStream(boolean append) throws IOException {
        return new LocalOutputStream(new FileOutputStream(absPath, append).getChannel());
    }

    public boolean hasRandomAccessInputStream() {
        return true;
    }

    /**
     * Implementation notes: the returned <code>InputStream</code> uses a NIO {@link FileChannel} under the hood to
     * benefit from <code>InterruptibleChannel</code> and allow a thread waiting for an I/O to be gracefully interrupted
     * using <code>Thread#interrupt()</code>.
     */
    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        return new LocalRandomAccessInputStream(new RandomAccessFile(file, "r").getChannel());
    }

    public boolean hasRandomAccessOutputStream() {
        return true;
    }

    /**
     * Implementation notes: the returned <code>InputStream</code> uses a NIO {@link FileChannel} under the hood to
     * benefit from <code>InterruptibleChannel</code> and allow a thread waiting for an I/O to be gracefully interrupted
     * using <code>Thread#interrupt()</code>.
     */
    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException {
        return new LocalRandomAccessOutputStream(new RandomAccessFile(file, "rw").getChannel());
    }

    public void delete() throws IOException {
        boolean ret = file.delete();
		
        if(!ret)
            throw new IOException();
    }


    public AbstractFile[] ls() throws IOException {
        return ls(null);
    }

    public void mkdir() throws IOException {
        if(!new File(absPath).mkdir())
            throw new IOException();
    }
	

    public long getFreeSpace() {
        if(JavaVersions.JAVA_1_6.isCurrentOrHigher())
            return file.getUsableSpace();

        return getVolumeInfo()[1];
    }
	
    public long getTotalSpace() {
        if(JavaVersions.JAVA_1_6.isCurrentOrHigher())
            return file.getTotalSpace();

        return getVolumeInfo()[0];
    }	


    /**
     * Always returns <code>true</code>.
     *
     * @return <code>true</code>
     */
    public boolean canRunProcess() {
        return true;
    }

    /**
     * Returns a process executing the specied local command.
     * 
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
        // If this file has no parent, return:
        // - the drive's name under OSes with root drives such as Windows, e.g. "C:"
        // - "/" under Unix-based systems
        if(parentFilePath==null)
            return hasRootDrives()?absPath:"/";

        return file.getName();
    }

    public String getAbsolutePath() {
        // Append separator for root folders (C:\ , /) and for directories
        if(parentFilePath ==null || (isDirectory() && !absPath.endsWith(SEPARATOR)))
            return absPath+SEPARATOR;

        return absPath;
    }


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

            if(isUNC)   // Special case for UNC paths which include the hostname in it
                childURL.setPath(addTrailingSeparator(fileURL.getPath())+names[i]);
            else
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
        // If destination file is not a LocalFile nor has a LocalFile ancestor (for instance an archive entry),
        // renaming won't work so use the default moveTo() implementation instead
        if(!destFile.getURL().getScheme().equals(FileProtocols.FILE)) {
            return super.moveTo(destFile);
        }

        destFile = destFile.getTopAncestor();
        if(!(destFile instanceof LocalFile)) {
            return super.moveTo(destFile);
        }

        // Fail in some situations where java.io.File#renameTo() doesn't.
        // Note that java.io.File#renameTo()'s implementation is system-dependant, so it's always a good idea to
        // perform all those checks even if some are not necessary on this or that platform.
        checkCopyPrerequisites(destFile, true);

        // The behavior of java.io.File#renameTo() when the destination file already exists is not consistent
        // accross platforms:
        // - Under UNIX, it succeeds and return true
        // - Under Windows, it fails and return false
        // This Java bug goes in great details about this issue: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4017593
        //
        // => Since this method is required to succeed when the destination file exists, the Windows platform needs
        // special treatment.

        File destJavaIoFile = ((LocalFile)destFile).file;

        if(IS_WINDOWS) {
            // Windows 9x or Windows Me: Kernel32's MoveFileEx function is NOT available
            if(OsVersions.WINDOWS_ME.isCurrentOrLower()) {
                // The destination file is deleted before calling java.io.File#renameTo().
                // Note that in this case, the atomicity of this method is not guaranteed anymore -- if
                // java.io.File#renameTo() fails (for whatever reason), the destination file is deleted anyway.
                if(destFile.exists())
                    destJavaIoFile.delete();
            }
            // Windows NT: Kernel32's MoveFileEx can be used, if the Kernel32 DLL is available.
            else if(Kernel32.isAvailable()) {
                // Note: MoveFileEx is always used, even if the destination file does not exist, to avoid having to
                // call #exists() on the destination file which has a cost.
                return  Kernel32.getInstance().MoveFileEx(absPath, destFile.getAbsolutePath(),
                        Kernel32API.MOVEFILE_REPLACE_EXISTING|Kernel32API.MOVEFILE_WRITE_THROUGH);
            }
            // else fall back to java.io.File#renameTo
        }

        return file.renameTo(destJavaIoFile);
    }


    public boolean isHidden() {
        return file.isHidden();
    }

    /**
     * Overridden to play nice with platforms that have root drives -- for those, the drive's root (e.g. <code>C:\</code>)
     * is returned instead of <code>/</code>.
     */
    public AbstractFile getRoot() {
        if(USES_ROOT_DRIVES) {
            Matcher matcher = driveRootPattern.matcher(getAbsolutePath(true));

            // Test if this file already is the root folder
            if(matcher.matches())
                return this;

            // Extract the drive from the path
            matcher.reset();
            if(matcher.find())
                return FileFactory.getFile(matcher.group());
        }

        return super.getRoot();
    }

    /**
     * Overridden to play nice with platforms that have root drives -- for those, <code>true</code> is returned if
     * this file's path matches the drive root's (e.g. <code>C:\</code>).
     */
    public boolean isRoot() {
        if(USES_ROOT_DRIVES)
            return driveRootPattern.matcher(getAbsolutePath()).matches();

        return super.isRoot();
    }

    /**
     * Overridden to return the local volum on which this file is located. The returned volume is one of the volumes
     * returned by {@link #getVolumes()}.
     */
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
        if(IS_WINDOWS) {
            if(!getRoot().equals(destFile.getRoot()))
                return SHOULD_NOT_HINT;
        }

        return moveHint;
    }


    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * LocalRandomAccessInputStream extends RandomAccessInputStream to provide random read access to a LocalFile.
     * This implementation uses a NIO <code>FileChannel</code> under the hood to benefit from
     * <code>InterruptibleChannel</code> and allow a thread waiting for an I/O to be gracefully interrupted using
     * <code>Thread#interrupt()</code>.
     */
    public static class LocalRandomAccessInputStream extends RandomAccessInputStream {

        private final FileChannel channel;
        private final ByteBuffer bb;

        private LocalRandomAccessInputStream(FileChannel channel) {
            this.channel = channel;
            this.bb = BufferPool.getByteBuffer();
        }

        public int read() throws IOException {
            synchronized(bb) {
                bb.position(0);
                bb.limit(1);

                int nbRead = channel.read(bb);
                if(nbRead<=0)
                    return nbRead;

                return 0xFF&bb.get(0);
            }
        }

        public int read(byte b[], int off, int len) throws IOException {
            synchronized(bb) {
                bb.position(0);
                bb.limit(Math.min(bb.capacity(), len));

                int nbRead = channel.read(bb);
                if(nbRead<=0)
                    return nbRead;

                bb.position(0);
                bb.get(b, off, nbRead);

                return nbRead;
            }
        }

        public void close() throws IOException {
            BufferPool.releaseByteBuffer(bb);
            channel.close();
        }

        public long getOffset() throws IOException {
            return channel.position();
        }

        public long getLength() throws IOException {
            return channel.size();
        }

        public void seek(long offset) throws IOException {
            channel.position(offset);
        }
    }

    /**
     * A replacement for <code>java.io.FileInputStream</code> that uses a NIO {@link FileChannel} under the hood to
     * benefit from <code>InterruptibleChannel</code> and allow a thread waiting for an I/O to be gracefully interrupted
     * using <code>Thread#interrupt()</code>.
     *
     * <p>This class simply delegates all its methods to a
     * {@link com.mucommander.file.impl.local.LocalFile.LocalRandomAccessInputStream} instance. Therefore, this class
     * does not derive from {@link com.mucommander.io.RandomAccessInputStream}, preventing random-access methods from
     * being used.</p>
     *
     */
    public static class LocalInputStream extends FilterInputStream {

        public LocalInputStream(FileChannel channel) {
            super(new LocalRandomAccessInputStream(channel));
        }
    }

    /**
     * A replacement for <code>java.io.FileOutputStream</code> that uses a NIO {@link FileChannel} under the hood to
     * benefit from <code>InterruptibleChannel</code> and allow a thread waiting for an I/O to be gracefully interrupted
     * using <code>Thread#interrupt()</code>.
     *
     * <p>This class simply delegates all its methods to a
     * {@link com.mucommander.file.impl.local.LocalFile.LocalRandomAccessOutputStream} instance. Therefore, this class
     * does not derive from {@link com.mucommander.io.RandomAccessOutputStream}, preventing random-access methods from
     * being used.</p>
     *
     */
    public static class LocalOutputStream extends FilteredOutputStream {

        public LocalOutputStream(FileChannel channel) {
            super(new LocalRandomAccessOutputStream(channel));
        }
    }

    /**
     * LocalRandomAccessOutputStream extends RandomAccessOutputStream to provide random write access to a LocalFile.
     * This implementation uses a NIO <code>FileChannel</code> under the hood to benefit from
     * <code>InterruptibleChannel</code> and allow a thread waiting for an I/O to be gracefully interrupted using
     * <code>Thread#interrupt()</code>.
     */
    public static class LocalRandomAccessOutputStream extends RandomAccessOutputStream {

        private final FileChannel channel;
        private final ByteBuffer bb;

        public LocalRandomAccessOutputStream(FileChannel channel) {
            this.channel = channel;
            this.bb = BufferPool.getByteBuffer();
        }

        public void write(int i) throws IOException {
            synchronized(bb) {
                bb.position(0);
                bb.limit(1);

                bb.put((byte)i);
                bb.position(0);

                channel.write(bb);
            }
        }

        public void write(byte b[]) throws IOException {
            write(b, 0, b.length);
        }

        public void write(byte b[], int off, int len) throws IOException {
            int nbToWrite;
            synchronized(bb) {
                do {
                    bb.position(0);
                    nbToWrite = Math.min(bb.capacity(), len);
                    bb.limit(nbToWrite);

                    bb.put(b, off, nbToWrite);
                    bb.position(0);

                    nbToWrite = channel.write(bb);

                    len -= nbToWrite;
                    off += nbToWrite;
                }
                while(len>0);
            }
        }

        public void setLength(long newLength) throws IOException {
            long currentLength = getLength();

            if(newLength==currentLength)
                return;

            long currentPos = channel.position();

            if(newLength<currentLength) {
                // Truncate the file and position the offset to the new EOF if it was beyond
                channel.truncate(newLength);
                if(currentPos>newLength)
                    channel.position(newLength);
            }
            else {
                // Expand the file by positionning the offset at the new EOF and writing a byte, and reposition the
                // offset to where it was
                channel.position(newLength-1);      // Note: newLength cannot be 0
                write(0);
                channel.position(currentPos);
            }

        }

        public void close() throws IOException {
            BufferPool.releaseByteBuffer(bb);
            channel.close();
        }

        public long getOffset() throws IOException {
            return channel.position();
        }

        public long getLength() throws IOException {
            return channel.size();
        }

        public void seek(long offset) throws IOException {
            channel.position(offset);
        }
    }


    /**
     * A Permissions implementation for LocalFile.
     */
    private static class LocalFilePermissions extends IndividualPermissionBits implements FilePermissions {
        
        private java.io.File file;

        // Permissions are limited to the user access type. Executable permission flag is only available under Java 1.6
        // and up.
        // Note: 'read' and 'execute' permissions have no meaning under Windows (files are either read-only or
        // read-write), but we return default values.

        /** Mask for supported permissions under Java 1.6 */
        private static PermissionBits JAVA_1_6_PERMISSIONS = new GroupedPermissionBits(448);   // rwx------ (700 octal)

        /** Mask for supported permissions under Java 1.5 */
        private static PermissionBits JAVA_1_5_PERMISSIONS = new GroupedPermissionBits(384);   // rw------- (300 octal)

        private final static PermissionBits MASK = JavaVersions.JAVA_1_6.isCurrentOrHigher()
                ?JAVA_1_6_PERMISSIONS
                :JAVA_1_5_PERMISSIONS;

        private LocalFilePermissions(java.io.File file) {
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
            else if(type==EXECUTE_PERMISSION && JavaVersions.JAVA_1_6.isCurrentOrHigher())
                return file.canExecute();

            return false;
        }

        /**
         * Overridden for peformance reasons.
         */
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
}
