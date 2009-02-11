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


package com.mucommander.job;

import com.mucommander.file.*;
import com.mucommander.file.impl.ProxyFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;

import java.io.IOException;
import java.io.InputStream;


/**
 * This job unpacks a set of archive files to a base destination folder. Archive entries are extracted in their natural
 * order using {@link com.mucommander.file.AbstractArchiveFile#getEntryIterator()}, to traverse the archive only once
 * and achieve optimal performance.
 *
 * @author Maxence Bernard
 */
public class UnpackJob extends AbstractCopyJob {

    /**
     * Creates a new UnpackJob without starting it.
     *
     * @param progressDialog dialog which shows this job's progress
     * @param mainFrame mainFrame this job has been triggered by
     * @param files files which are going to be unpacking
     * @param destFolder destination folder where the files will be copied
     * @param fileExistsAction default action to be performed when a file already exists in the destination, see {@link com.mucommander.ui.dialog.file.FileCollisionDialog} for allowed values
     */
    public UnpackJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files, AbstractFile destFolder, int fileExistsAction) {
        super(progressDialog, mainFrame, files, destFolder, null, fileExistsAction);

        this.errorDialogTitle = Translator.get("unpack_dialog.error_title");
    }



    ////////////////////////////////////
    // TransferFileJob implementation //
    ////////////////////////////////////

    /**
     * Unpacks the given archive file. If the file is a directory, its children will be processed recursively.
     * If the file is not an archive file nor a directory, it is not processed and <code>false</code> is returned.
     *
     * @param file the file to unpack
     * @param recurseParams unused
     * @return <code>true</code> if the file has been processed successfully
     */
    protected boolean processFile(AbstractFile file, Object recurseParams) {
        // Stop if interrupted
        if(getState()==INTERRUPTED)
            return false;

        // Destination folder
        AbstractFile destFolder = baseDestFolder;

        // If the file is a directory, process its children recursively
        if(file.isDirectory()) {
            do {    // Loop for retries
                try {
                    // List files inside archive file (can throw an IOException)
                    AbstractFile[] archiveFiles = currentFile.ls();

                    // Recurse on zip's contents
                    for(int j=0; j<archiveFiles.length && getState()!=INTERRUPTED; j++) {
                        // Notify job that we're starting to process this file (needed for recursive calls to processFile)
                        nextFile(archiveFiles[j]);
                        // Recurse
                        processFile(archiveFiles[j], destFolder);
                    }
                    // Return true when complete
                    return true;
                }
                catch(IOException e) {
                    // File could not be uncompressed properly
                    int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_read_file", currentFile.getName()));
                    // Retry loops
                    if(ret==RETRY_ACTION)
                        continue;
                    // cancel, skip or close dialog will simply return false
                    return false;
                }
            } while(true);
        }

        // Abort if the file is neither an archive file nor a directory
        if(!file.hasAncestor(AbstractArchiveFile.class))
            return false;

        // 'Cast' the file as an archive file
        AbstractArchiveFile archiveFile = (AbstractArchiveFile)file.getAncestor(AbstractArchiveFile.class);
        ArchiveEntryIterator iterator = null;

        ArchiveEntry entry;
        AbstractFile entryFile;
        AbstractFile destFile;

        // Unpack the archive, copying entries one by one, in the iterator's order
        try {
            iterator = archiveFile.getEntryIterator();

            while(iterator.hasNextEntry() && getState()!=INTERRUPTED) {
                entry = iterator.nextEntry();
                entryFile = archiveFile.getArchiveEntryFile(entry);

                // Notify the job that we're starting to process this file
                nextFile(entryFile);

                // Create destination AbstractFile instance
                destFile = destFolder.getChild(entry.getPath());

                // Do nothing if the file is a symlink (skip file and return)
                if(entryFile.isSymlink())
                    return true;

                // Check if the file does not already exist in the destination
                destFile = checkForCollision(entryFile, destFolder, destFile, false);
                if (destFile == null) {
                    // A collision occurred and either the file was skipped, or the user cancelled the job
                    continue;
                }

                // It is noteworthy that the iterator returns entries in no particular order (consider it random).
                // For that reason, we cannot assume that the parent directory of an entry will be processed
                // before the entry itself.

                // If the entry is a directory ...
                if(entryFile.isDirectory()) {
                    // Create the directory in the destination, if it doesn't already exist
                    if(!(destFile.exists() && destFile.isDirectory())) {
                        // Loop for retry
                        do {
                            try {
                                // Use mkdirs() instead of mkdir() to create any parent folder that doesn't exist yet
                                destFile.mkdirs();
                            }
                            catch(IOException e) {
                                // Unable to create folder
                                int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_create_folder", entryFile.getName()));
                                // Retry loops
                                if(ret==RETRY_ACTION)
                                    continue;
                                // Cancel or close dialog return false
                                return false;
                                // Skip continues
                            }
                            break;
                        } while(true);
                    }
                }
                // The entry is a regular file, copy it
                else  {
                    // Create the file's parent directory(s) if it doesn't already exist
                    AbstractFile destParentFile = destFile.getParent();
                    if(!destParentFile.exists()) {
                        // Use mkdirs() instead of mkdir() to create any parent folder that doesn't exist yet
                        destParentFile.mkdirs();
                    }

                    // The entry is wrapped in a ProxyFile to override #getInputStream() and delegate it to
                    // ArchiveFile#getEntryInputStream in order to take advantage of the ArchiveEntryIterator, which for
                    // some archive file implementations (such as TAR) can speed things by an order of magnitude.
                    if(!tryCopyFile(new ProxiedEntryFile(entryFile, entry, archiveFile, iterator), destFile, append, errorDialogTitle))
                       return false;
                }
            }
        }
        catch(IOException e) {
            showErrorDialog(errorDialogTitle, Translator.get("cannot_read_file", archiveFile.getName()));
        }
        finally {
            // The ArchiveEntryIterator must be closed when finished
            if(iterator!=null) {
                try { iterator.close(); }
                catch(IOException e) {
                    // Not much we can do about it
                }
            }
        }

        return false;
    }

    // This job modifies the base destination folder and its subfolders
    protected boolean hasFolderChanged(AbstractFile folder) {
        return baseDestFolder.isParentOf(folder);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    protected void jobCompleted() {
        super.jobCompleted();

        // If the destination files are located inside an archive, optimize the archive file
        AbstractArchiveFile archiveFile = baseDestFolder.getParentArchive();
        if(archiveFile!=null && archiveFile.isWritableArchive())
            optimizeArchive((AbstractRWArchiveFile)archiveFile);
    }

    public String getStatusString() {
        if(isCheckingIntegrity())
            return super.getStatusString();

        if(isOptimizingArchive)
            return Translator.get("optimizing_archive", archiveToOptimize.getName());

        return Translator.get("unpack_dialog.unpacking_file", getCurrentFilename());
    }


    ///////////////////
    // Inner classes //
    ///////////////////

    private static class ProxiedEntryFile extends ProxyFile {

        private ArchiveEntry entry;
        private AbstractArchiveFile archiveFile;
        private ArchiveEntryIterator iterator;

        public ProxiedEntryFile(AbstractFile entryFile, ArchiveEntry entry, AbstractArchiveFile archiveFile, ArchiveEntryIterator iterator) {
            super(entryFile);

            this.entry = entry;
            this.archiveFile = archiveFile;
            this.iterator = iterator;
        }

        public InputStream getInputStream() throws IOException {
            return archiveFile.getEntryInputStream(entry, iterator);
        }
    }
}
