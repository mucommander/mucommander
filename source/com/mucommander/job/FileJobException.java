/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


package com.mucommander.job;


/**
 * FileJobException are exceptions that can be thrown by certain
 * FileJob methods.
 *
 * @author Maxence Bernard
 */
public class FileJobException extends Exception {

    /** Source cannot be opened */
    public final static int CANNOT_OPEN_SOURCE = 1;

    /** Destination cannot be opened */
    public final static int CANNOT_OPEN_DESTINATION = 2;

    /** An error occurred during the file transfer */
    public final static int ERROR_WHILE_TRANSFERRING = 3;


    protected int reason;

	
    public FileJobException(int reason) {
        this.reason = reason;
    }
	
    public int getReason() {
        return reason;
    }
}
