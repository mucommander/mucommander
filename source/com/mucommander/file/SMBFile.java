package com.mucommander.file;

import jcifs.smb.*;

import java.io.*;


/**
 * SMBFile represents a file shared through the SMB protocol.
 *
 * @author Maxence Bernard
 */
//public class SMBFile extends AbstractFile implements RemoteFile {
public class SMBFile extends AbstractFile {

    /** File separator is '/' for urls */
    private final static String SEPARATOR = "/";

    private SmbFile file;
    private String publicURL;
    private String privateURL;

    private AbstractFile parent;
    private boolean parentValSet;

    private boolean isDirectory;
    private boolean isDirectoryValSet;
	
    private String name;
	
	
    public SMBFile(FileURL fileURL) throws IOException {	
        this(fileURL, true);
    }
	
	
    private SMBFile(FileURL fileURL, boolean addAuthInfo) throws IOException {	
        super(fileURL);

        AuthManager.authenticate(fileURL, addAuthInfo);

        this.privateURL = fileURL.getStringRep(true);
        this.publicURL = fileURL.getStringRep(false);

        // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("publicURL= "+publicURL);
        // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("privateURL= "+privateURL);

        //		// Unlike java.io.File, SmbFile throws an SmbException
        //		// when file doesn't exist.
        //		// All SMB workgroups, servers, shares, or directories URLs require a trailing slash '/'. 
        //		// Regular SMB files can have a trailing slash as well, so let's add a trailing slash.
        //		this.file = new SmbFile(privateURL.endsWith("/")?privateURL:privateURL+"/");
        this.file = new SmbFile(privateURL);

        // Cache SmbFile.getName()'s return value which parses name each time it is called
        this.name = file.getName();
        if(name.endsWith("/"))
            name = name.substring(0, name.length()-1);
    }
	
	
    /////////////////////////////////////////
    // AbstractFile methods implementation //
    /////////////////////////////////////////
	
    public String getName() {
        return name;
    }

    public String getAbsolutePath() {
        return publicURL;
    }

    public String getSeparator() {
        return SEPARATOR;
    }
	
    public long getDate() {
        try {
            return file.lastModified();
        }
        catch(SmbException e) {
            return 0;
        }
    }

    public boolean changeDate(long lastModified) {
        try {
            // SmbFile.setLastModified() returns "jcifs.smb.SmbAuthException: Access is denied" exceptions
            // don't know if it's a bug in the library or a server limitation (tested with Samba)
            file.setLastModified(lastModified);
            return true;
        }
        catch(SmbException e) {
            if(com.mucommander.Debug.ON) { com.mucommander.Debug.trace("return false "+e);}
            return false;
        }
    }
	
    public long getSize() {
        try {
            return file.length();
        }
        catch(SmbException e) {
            return 0;
        }
    }
	
	
    public AbstractFile getParent() {
        if(!parentValSet) {
            FileURL parentURL = fileURL.getParent();
            if(parentURL!=null) {
                try { this.parent = new SMBFile(parentURL, false); }
                catch(IOException e) { this.parent = null; }
            }
				
            this.parentValSet = true;
            return this.parent;
        }
		
        return this.parent;
    }
	
    public void setParent(AbstractFile parent) {
        this.parent = parent;	
        this.parentValSet = true;
    }
	 
    public boolean exists() {
        // Unlike java.io.File, SmbFile.exists() can throw an SmbException
        try {
            return file.exists();
        }
        catch(IOException e) {
            if(e instanceof SmbAuthException)
                return true;
            return false;
        }

    }
	
    public boolean canRead() {
        // Unlike java.io.File, SmbFile.canRead() can throw an SmbException
        try {
            return file.canRead();
        }
        catch(SmbException e) {
            return false;
        }
    }
	
    public boolean canWrite() {
        // Unlike java.io.File, SmbFile.canWrite() can throw an SmbException
        try {
            return file.canWrite();
        }
        catch(SmbException e) {
            return false;
        }
    }
	
    public boolean isHidden() {
        try {
            return file.isHidden();
        }
        catch(SmbException e) {
            return false;
        }			
    }

    public boolean isDirectory() {
        // Cache SmbFile.isDirectory()'s return value as this method triggers network calls
        // (calls exists() which checks file existence on the server) and will report
        // false if connection is lost.
        if(!isDirectoryValSet) {
            try {
                this.isDirectory = file.isDirectory();
                this.isDirectoryValSet = true;
            }
            catch(SmbException e) {
                return false;
            }
        }
        return this.isDirectory;
    }
	
    public boolean isSymlink() {
        // Symlinks are not supported with jCIFS (or in CIFS/SMB?)
        return false;
    }

    public boolean equals(Object f) {
        if(!(f instanceof SMBFile))
            return super.equals(f);		// could be equal to a ZipArchiveFile
		
        // SmbFile's equals method is just perfect: compares canonical paths
        // and IP addresses
        return file.equals(((SMBFile)f).file);
    }
	

    public InputStream getInputStream() throws IOException {
        return new SmbFileInputStream(privateURL);
    }
	
    public OutputStream getOutputStream(boolean append) throws IOException {
        return new SmbFileOutputStream(privateURL, append);
    }
		
    public boolean moveTo(AbstractFile destFile) throws IOException  {
        // If destination file is an SMB file located on the same server,
        // have the server rename the file.
        if (destFile.fileURL.getProtocol().equals("smb") && destFile.fileURL.getHost().equals(this.fileURL.getHost())) {
            SmbFile destSmbFile;

            // Destination file is an instance of SMBFile
            if(destFile instanceof SMBFile)
                destSmbFile = ((SMBFile)destFile).file;		// Reuse SmbFile instance
            // Destination file uses SMB protocol but is not an instance of SMBFile (an archive file wrapped over for instance)
            else
                try { destSmbFile = new SmbFile(destFile.fileURL.getStringRep(true)); }	// Create new SmbFile instance
                catch(IOException e) { return false; }
			
            try{
                file.renameTo(destSmbFile);
                return true;
            }
            catch(SmbException e) {
                return false;
            }
        }
		
        return false;
    }

    public void delete() throws IOException {
        file.delete();
    }

	
    public AbstractFile[] ls() throws IOException {
        try {
            SmbFile smbFiles[] = file.listFiles();
			
            if(smbFiles==null)
                throw new IOException();
			
            // Create SMBFile by recycling SmbFile instance and sharing parent instance
            // among children
            AbstractFile children[] = new AbstractFile[smbFiles.length];
            AbstractFile child;
            for(int i=0; i<smbFiles.length; i++) {
                child = AbstractFile.wrapArchive(new SMBFile(new FileURL(smbFiles[i].getCanonicalPath(), fileURL), false));
                child.setParent(this);
                children[i] = child;		
            }
			
            return children;
        }
        catch(SmbAuthException e) {
            throw new AuthException(fileURL, e.getMessage());
        }
    }

	
    public void mkdir(String name) throws IOException {
        // Unlike java.io.File.mkdir(), SmbFile does not return a boolean value
        // to indicate if the folder could be created
        new SmbFile(privateURL+SEPARATOR+name).mkdir();
    }


    public long getFreeSpace() {
        try {
            return file.getDiskFreeSpace();
        }
        catch(SmbException e) {
            // Error occured, return -1 (not available)
            return -1;
        }
    }

    public long getTotalSpace() {
        // No way to retrieve this information with jCIFS/SMB, return -1 (not available)
        return -1;
    }
}
