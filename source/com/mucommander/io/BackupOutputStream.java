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

package com.mucommander.io;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;

import java.io.*;

/**
 * Saves file in as crash-safe a manner as possible.
 * <p>
 * In order to prevent system or muCommander failures to corrupt configuration files,
 * the BackupOutputStream implements the following algorithm:
 * <ul>
 *   <li>Write its content to a backup file instead of the requested file</li>
 *   <li>When close is called, copy the content of the backup file over the original file</li>
 * </ul>
 * This way, if a crash was to happen while configuration files are being saved, either of the
 * following will happen:
 * <ul>
 *   <li>
 *     The backup file is not properly saved, but the original configuration is left untouched.
 *     We have lost <i>some</i> information (modifications since last save) but not <i>all</i>.
 *   </li>
 *   <li>
 *     The original file is not properly saved, but the backup file is correct. This is easy to check,
 *     as the backup and original file should always have the same size. If they don't, then the backup
 *     file should be used rather than the original one.
 *   </li>
 * </p>
 * <p>
 * Files that have been saved by this class should be read with {@link com.mucommander.io.BackupInputStream}
 * in order to make sure that an uncorrupt version of them is loaded.
 * </p>
 * <p>
 * The <code>BackupOutputStream</code> monitors all of its own I/O operations. If an error occurs, then the backup
 * operation will not be performed when {@link #close()} is called. It's possible to force the backup operation by
 * using the {@link #close(boolean)} method.
 * </p>
 * @see    com.mucommander.io.BackupInputStream
 * @author Nicolas Rinaudo
 */
public class BackupOutputStream extends FilterOutputStream implements BackupConstants {
    // - Instance fields --------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Path of the original file. */
    private AbstractFile     target;
    /** Path to the backup file. */
    private AbstractFile     backup;
    /** Whether or not an error occured while writing to the backup file. */
    private boolean          error;



    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Opens a backup output stream on the specified file.
     * @param     file        file on which to open a backup output stream.
     * @exception IOException thrown if any IO error occurs.
     */
    public BackupOutputStream(File file) throws IOException {this(FileFactory.getFile(file.getAbsolutePath()));}

    /**
     * Opens a backup output stream on the specified file.
     * @param     file        file on which to open a backup output stream.
     * @exception IOException thrown if any IO error occurs.
     */
    public BackupOutputStream(String file) throws IOException {this(FileFactory.getFile((new File(file)).getAbsolutePath()));}

    /**
     * Opens a backup output stream on the specified file.
     * @param     file        file on which to open a backup output stream.
     * @exception IOException thrown if any IO error occurs.
     */
    public BackupOutputStream(AbstractFile file) throws IOException {this(file, FileFactory.getFile(file.getAbsolutePath() + BACKUP_SUFFIX));}

    /**
     * Opens an output stream on the specified file using the specified backup file.
     * @param     file        file on which to open the backup output stream.
     * @param     save        file that will be used for backup.
     * @exception IOException thrown if any IO error occurs.
     */
    private BackupOutputStream(AbstractFile file, AbstractFile save) throws IOException {
        super(save.getOutputStream(false));
        target = file;
        backup = save;
    }



    // - Error catching ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Flushes this output stream and forces any buffered output bytes to be written out to the stream.
     * <p>
     * This method calls the <code>flush()</code> method of its underlying output stream.
     * </p>
     * <p>
     * If an error occurs at this point, the {@link #close()} method will not overwrite the target file. This can be
     * forced through the {@link #close(boolean)} method.
     * </p>
     * @throws IOException if an I/O error occurs.
     */
    public void flush() throws IOException {
        if(error)
            super.flush();
        else {
            try {super.flush();}
            catch(IOException e) {
                error = true;
                throw e;
            }
        }
    }

    /**
     * Writes b.length bytes to this output stream.
     * <p>
     * This method calls the <code>write(byte[] b)</code> method of its underlying output stream.
     * </p>
     * <p>
     * If an error occurs at this point, the {@link #close()} method will not overwrite the target file. This can be
     * forced through the {@link #close(boolean)} method.
     * </p>
     * @param  b           the data to be written.
     * @throws IOException if an I/O error occurs.
     */
    public void write(byte[] b) throws IOException {
        if(error)
            super.write(b);
        else {
            try {super.write(b);}
            catch(IOException e) {
                error = true;
                throw e;
            }
        }
    }

    /**
     * Writes len bytes from the specified byte array starting at offset off to this output stream.
     * <p>
     * This method calls the <code>write(byte[] b, int off, int len)</code> method of its underlying output stream.
     * </p>
     * <p>
     * If an error occurs at this point, the {@link #close()} method will not overwrite the target file. This can be
     * forced through the {@link #close(boolean)} method.
     * </p>
     * @param  b           the data to be written.
     * @param  off         the start offset in the data.
     * @param  len         the number of bytes to write.
     * @throws IOException if an I/O error occurs.
     */
    public void write(byte[] b, int off, int len) throws IOException {
        if(error)
            super.write(b, off, len);
        else {
            try {super.write(b, off, len);}
            catch(IOException e) {
                error = true;
                throw e;
            }
        }
    }

    /**
     * Writes the specified byte to this output stream.
     * <p>
     * This method calls the <code>write(byte b)</code> method of its underlying output stream.
     * </p>
     * <p>
     * If an error occurs at this point, the {@link #close()} method will not overwrite the target file. This can be
     * forced through the {@link #close(boolean)} method.
     * </p>
     * @param  b           the data to be written.
     * @throws IOException if an I/O error occurs.
     */
    public void write(int b) throws IOException {
        if(error)
            super.write(b);
        else {
            try {super.write(b);}
            catch(IOException e) {
                error = true;
                throw e;
            }
        }
    }



    // - Backup -----------------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Overwrites the target file with the backup one.
     * @exception IOException thrown if any IO related error occurs.
     */
    private void backup() throws IOException {
        // We're not using backup.moveTo(target) because we want to make absolutely sure
        // that if an error occurs in the middle of the operation, at least one of the two files
        // is complete.
        backup.copyTo(target);
        backup.delete();
    }

    /**
     * Finishes the backup operation.
     * @exception IOException thrown if any IO related error occurs.
     */
    public void close() throws IOException {close(!error);}

    /**
     * Closes the output stream.
     * <p>
     * The <code>backup</code> parameter is meant for those cases when an error happened
     * while writing to the stream: if it did, we don't want to propagate to the target
     * file, and thus should prevent the backup operation from being performed.
     * </p>
     * @param     backup      whether or not to overwrite the target file by the backup one.
     * @exception IOException thrown if any IO related error occurs.
     */
    public void close(boolean backup) throws IOException {
        // Closes the underlying output stream.
        super.flush();
        super.close();

        if(backup)
            backup();
    }
}
