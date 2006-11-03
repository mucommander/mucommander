package com.mucommander.io;

import com.mucommander.file.*;
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



    // - Backup -----------------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Finishes the backup operation.
     * @exception IOException thrown if any IO related error occurs.
     */
    public void close() throws IOException {
        InputStream in;     // Input stream on the backup file.
        OutputStream out;   // Output stream on the target file.
        byte[]      buffer; // Stores chunks of the backup file.
        int         count;  // Number of bytes read in the last read operation.

        // Closes the underlying output stream.
        super.close();

        in  = null;
        out = null;

        try {
            // Initialises transfer.
            in     = backup.getInputStream();
            out    = target.getOutputStream(false);
            buffer = new byte[512];

            // Transfers the content of in into out.
            while((count = in.read(buffer)) != -1)
                out.write(buffer, 0, count);
        }
        // Cleanup.
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }

            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }
    }
}
