package com.mucommander.io;

import com.mucommander.file.*;
import java.io.*;

/**
 * Opens an input stream on a file that has been saved by {@link com.mucommander.io.BackupOutputStream}.
 * <p>
 * This class' role is to choose which of the original or backup file should be read in order to ensure
 * that the data is not corrupt.
 * </p>
 * @see com.mucommander.io.BackupOutputStream.
 * @author Nicolas Rinaudo
 */
public class BackupInputStream extends FilterInputStream implements BackupConstants {
    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Opens a backup input stream on the specified file.
     * @param     file        file to open for reading.
     * @exception IOException thrown if any IO related error occurs.
     */
    public BackupInputStream(File file) throws IOException {super(getInputStream(FileFactory.getFile(file.getAbsolutePath())));}

    /**
     * Opens a backup input stream on the specified file.
     * @param     path        path to the file to open for reading.
     * @exception IOException thrown if any IO related error occurs.
     */
    public BackupInputStream(String path) throws IOException {super(getInputStream(FileFactory.getFile((new File(path)).getAbsolutePath())));}

    /**
     * Opens a backup input stream on the specified file.
     * @param     file        file to open for reading.
     * @exception IOException thrown if any IO related error occurs.
     */
    public BackupInputStream(AbstractFile file) throws IOException {super(getInputStream(file));}

    /**
     * Opens a stream on the right file.
     * <p>
     * If a backup file is found, and is bigger than the target file, then it will be used.
     * </p>
     * @param     file        file on which to open an input stream.
     * @return                a stream on the right file.
     * @exception IOException thrown if any IO related error occurs.
     */
    private static final InputStream getInputStream(AbstractFile file) throws IOException {
        AbstractFile backup;

        // Checks whether the backup file is a better choice than the target one.
        backup = FileFactory.getFile(file.getAbsolutePath() + BACKUP_SUFFIX);
        if(backup.exists() && (file.getSize() < backup.getSize()) && (file.getDate() <= backup.getDate()))
            return backup.getInputStream();

        // Opens a stream on the target file.
        return file.getInputStream();
    }
}
