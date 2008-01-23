/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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


package com.mucommander.auth;

import com.mucommander.file.FileURL;

import java.io.IOException;


/**
 * AuthException should be thrown whenever access to a remote file system was denied due to false or missing credentials.
 *
 * <p>AuthException is caught in different places of the application 
 * to provide a way for the user to authenticate (a dialog pops up).
 *
 * @author Maxence Bernard
 */
public class AuthException extends IOException {

    private FileURL fileURL;
    private String msg;

	
    /**
     * Creates a new AuthException instance, without any associated exception.
     *
     * @param fileURL file URL for which authentication failed.
     */
    public AuthException(FileURL fileURL) {
        this(fileURL, null);
    }
	
    /**
     * Creates a new AuthException instance that was caused by the given exception.
     *
     * @param fileURL file URL for which authentication failed.
     * @param msg a reason why the IOException was thrown if not <code>null</code>, in understandable terms.
     */
    public AuthException(FileURL fileURL, String msg) {
        this.fileURL = fileURL;
        if(msg!=null)
            this.msg = msg.trim();
    }
	

    /**
     * Returns the URL of the file for which authentication failed.
     * @return the URL of the file for which authentication failed.
     */
    public FileURL getFileURL() {
        return fileURL;
    }

	
    /**
     * Returns a message describing the exception.
     * @return a message describing the exception.
     */
    public String getMessage() {
        return msg;
    }

}
