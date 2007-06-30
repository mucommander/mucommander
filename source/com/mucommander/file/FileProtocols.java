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

    /** Protocol for files served by an NFS server */
    public final static String NFS = "nfs";

    /** Protocol for files served by an SFTP server (not to be confused with FTPS or SCP) */
    public final static String SFTP = "sftp";

    /** Protocol for files served by a SMB/CIFS server */
    public final static String SMB = "smb";

    /** Protocol for files served by a web server using Webdav/HTTP */
    public final static String WEBDAV = "webdav";

    /** Protocol for files served by a web server using Webdav/HTTPS */
    public final static String WEBDAVS = "webdavs";
}
