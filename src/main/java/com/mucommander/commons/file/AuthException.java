/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */



package com.mucommander.commons.file;

import java.io.IOException;


/**
 * AuthException is an <code>IOException</code> that is thrown whenever an operation failed due to the lack of,
 * invalid or insufficient credentials. An URL associated with the exception gives the location where the error
 * occurred, and the set of credentials that were used (if any).
 *
 * @author Maxence Bernard
 */
public class AuthException extends IOException {

    protected FileURL fileURL;
    protected String msg;

    /**
     * Creates a new AuthException instance, without any associated exception.
     *
     * @param fileURL the location where the error occurred, with the set of credentials that were used (if any).
     */
    public AuthException(FileURL fileURL) {
        this(fileURL, null);
    }
	
    /**
     * Creates a new AuthException instance that was caused by the given exception.
     *
     * @param fileURL the location where the error occurred, with the set of credentials that were used (if any)
     * @param msg a message describing the error, <code>null</code> if there is none
     */
    public AuthException(FileURL fileURL, String msg) {
        super(msg);

        this.fileURL = fileURL;
        if(msg!=null)
            this.msg = msg.trim();
    }
	

    /**
     * Returns the location where the error occurred, with the set of credentials that were used (if any).
     *
     * @return the location where the error occurred, with the set of credentials that were used (if any)
     */
    public FileURL getURL() {
        return fileURL;
    }
}
