package com.mucommander.file;


/**
 * This interface contains a set of known protocol names, that can be found in {@link FileURL}. 
 *
 * @author Maxence Bernard
 */
public interface FileProtocols {

    /** Protocol for local or locally mounted files */
    public final static String FILE = "file";

    /** Protocol for files served by an FTP server */
    public final static String FTP = "ftp";

    /** Protocol for files served by a web server using HTTP */
    public final static String HTTP = "http";

    /** Protocol for files served by a web server using HTTPS */
    public final static String HTTPS = "https";

    /** Protocol for files served by an SFTP server (not to be confused with FTPS or SCP) */
    public final static String SFTP = "sftp";

    /** Protocol for files served by a SMB/CIFS server */
    public final static String SMB = "smb";

    /** Protocol for files served by a web server using Webdav/HTTP */
    public final static String WEBDAV = "webdav";

    /** Protocol for files served by a web server using Webdav/HTTPS */
    public final static String WEBDAVS = "webdavs";
}
