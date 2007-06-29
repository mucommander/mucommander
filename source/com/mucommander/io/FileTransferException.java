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


package com.mucommander.io;

import java.io.IOException;


/**
 * FileTransferException is an IOException which can be thrown to indicate a file transfer error.
 * {@link #getReason() getReason()} returns the reason why the transfer failed.
 *
 * @author Maxence Bernard
 */
public class FileTransferException extends IOException {

    /** Reason not known / not specified */
    public final static int UNKNOWN_REASON = 0;

    /** Source and destination files are identical */
    public final static int SOURCE_AND_DESTINATION_IDENTICAL = 1;

    /** Source file could not be opened for read */
    public final static int OPENING_SOURCE = 2;

    /** Destination file could not be opened for write */
    public final static int OPENING_DESTINATION = 3;

    /** An error occurred while reading the source file */
    public final static int READING_SOURCE = 4;

    /** An error occurred while writing the destination file */
    public final static int WRITING_DESTINATION = 5;

    /** An error occurred while deleting the source file (used when moving a file) */
    public final static int DELETING_SOURCE = 6;

    /** Source file could not be closed */
    public final static int CLOSING_SOURCE = 7;

    /** Destination file could not be closed */
    public final static int CLOSING_DESTINATION = 8;


    protected int reason;


    public FileTransferException(int reason) {
        this.reason = reason;
    }

    public int getReason() {
        return reason;
    }

    public String toString() {
        return super.toString()+" reason="+reason;
    }
}
