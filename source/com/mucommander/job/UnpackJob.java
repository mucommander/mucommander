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
import com.mucommander.file.util.PathUtils;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.impl.UnmarkAllAction;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;


/**
 * This job unpacks a set of archive files to a base destination folder. Archive entries are extracted in their natural
 * order using {@link com.mucommander.file.AbstractArchiveFile#getEntryIterator()}, to traverse the archive only once
 * and achieve optimal performance.
 *
 * @author Maxence Bernard
 */
public class UnpackJob extends AbstractCopyJob {

    /** Archive entries to be unpacked */
    protected Vector<ArchiveEntry> selectedEntries;

    /** Depth of the folder in which the top entries are located. 0 is the highest depth (archive's root folder) */
    protected int baseArchiveDepth;


    /**
     * Creates a new UnpackJob without starting it.
     * <p>
     * The base destination folder will be created if it doesn't exist.
     * </p>
     *
     * @param progressDialog dialog which shows this job's progress
     * @param mainFrame mainFrame this job has been triggered by
     * @param files files which are going to be unpacked
     * @param destFolder destination folder where the files will be copied
     * @param fileExistsAction default action to be performed when a file already exists in the destination, see {@link com.mucommander.ui.dialog.file.FileCollisionDialog} for allowed values
     */
    public UnpackJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files, AbstractFile destFolder, int fileExistsAction) {
        super(progressDialog, mainFrame, files, destFolder, null, fileExistsAction);

        this.errorDialogTitle = Translator.get("unpack_dialog.error_title");
        this.baseArchiveDepth = 0;
    }

    /**
     * Creates a new UnpackJob without starting it.
     *
     * @param progressDialog dialog which shows this job's progress
     * @param mainFrame mainFrame this job has been triggered by
     * @param archiveFile the archive file which is going to be unpacked
     * @param destFolder destination folder where the files will be copied
     * @param newName the new filename in the destination folder, if <code>null</code> the original filename will be used
     * @param fileExistsAction default action to be performed when a file already exists in the destination, see {@link com.mucommander.ui.dialog.file.FileCollisionDialog} for allowed values
     * @param selectedEntries entries to be unpacked
     * @param baseArchiveDepth depth of the folder in which the top entries are located. 0 is the highest depth (archive's root folder)
     */
    public UnpackJob(ProgressDialog progressDialog, MainFrame mainFrame, AbstractArchiveFile archiveFile, int baseArchiveDepth, AbstractFile destFolder, String newName, int fileExistsAction, Vector<ArchiveEntry> selectedEntries) {
        super(progressDialog, mainFrame, new FileSet(archiveFile.getParent(), archiveFile), destFolder, newName, fileExistsAction);

        this.errorDialogTitle = Translator.get("unpack_dialog.error_title");
        this.baseArchiveDepth = baseArchiveDepth;
        this.selectedEntries = selectedEntries;
    }


    ////////////////////////////////////
    // TransferFileJob implementation //
    ////////////////////////////////////

    @Override
    protected void jobStarted() {
        super.jobStarted();

        // Create the base destination folder if it doesn't exist yet
        if(!baseDestFolder.exists()) {
            // Loop for retry
            do {
                try {
                    baseDestFolder.mkdir();
                }
                catch(IOException e) {
                    // Unable to create folder
                    int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_create_folder", baseDestFolder.getName()));
                    // Retry loops
                    if(ret==RETRY_ACTION)
                        continue;
                    // Cancel or close dialog interrupts the job
                    interrupt();
                    // Skip continues
                }
                break;
            } while(true);
        }
    }

    /**
     * Unpacks the given archive file. If the file is a directory, its children will be processed recursively.
     * If the file is not an archive file nor a directory, it is not processed and <code>false</code> is returned.
     *
     * @param file the file to unpack
     * @param recurseParams unused
     * @return <code>true</code> if the file has been processed successfully
     */
    @Override
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
                    AbstractFile[] archiveFiles = getCurrentFile().ls();

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
                    int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_read_file", getCurrentFilename()));
                    // Retry loops
                    if(ret==RETRY_ACTION)
                        continue;
                    // cancel, skip or close dialog will simply return false
                    return false;
                }
            } while(true);
        }

        // Abort if the file is neither an archive file nor a directory
        if(!file.isArchive())
            return false;

        // 'Cast' the file as an archive file
        AbstractArchiveFile archiveFile = (AbstractArchiveFile)file.getAncestor(AbstractArchiveFile.class);
        ArchiveEntryIterator iterator = null;

        ArchiveEntry entry;
        String entryPath;
        AbstractFile entryFile;
        AbstractFile destFile;
        String destSeparator = destFolder.getSeparator();
        String relDestPath;

        // Unpack the archive, copying entries one by one, in the iterator's order
        try {
            iterator = archiveFile.getEntryIterator();
            while((entry = iterator.nextEntry())!=null && getState()!=INTERRUPTED) {
                entryPath = entry.getPath();

                boolean processEntry = false;
                if(selectedEntries ==null) {    // Entries are processed
                    processEntry = true;
                }
                else {                          // We need to determine if the entry should be processed or not
                    // Process this entry if the selectedEntries set contains this entry, or a parent of this entry
                    int nbSelectedEntries = selectedEntries.size();
                    for(int i=0; i<nbSelectedEntries; i++) {
                        ArchiveEntry selectedEntry = selectedEntries.elementAt(i);
                        // Note: paths of directory entries must end with '/', so this compares whether
                        // selectedEntry is a parent of the current entry.
                        if(selectedEntry.isDirectory()) {
                            if(entryPath.startsWith(selectedEntry.getPath())) {
                                processEntry = true;
                                break;
                                // Note: we can't remove selectedEntryPath from the set, we still need it
                            }
                        }
                        else if(entryPath.equals(selectedEntry.getPath())) {
                            // If the (regular file) entry is in the set, remove it as we no longer need it (will speed up
                            // subsequent searches)
                            processEntry = true;
                            selectedEntries.removeElementAt(i);
                            break;
                        }
                    }
                }

                if(!processEntry)
                    continue;

                // Resolve the entry file
                entryFile = archiveFile.getArchiveEntryFile(entryPath);

                // Notify the job that we're starting to process this file
                nextFile(entryFile);

                // Figure out the destination file's path, relatively to the base destination folder
                relDestPath = baseArchiveDepth==0
                        ?entry.getPath()
                        :PathUtils.removeLeadingFragments(entry.getPath(), "/", baseArchiveDepth);

                if(newName!=null)
                    relDestPath = newName+(PathUtils.getDepth(relDestPath, "/")<=1?"":"/"+PathUtils.removeLeadingFragments(relDestPath, "/", 1));

                if(!"/".equals(destSeparator))
                    relDestPath = relDestPath.replace("/", destSeparator);

                // Create destination AbstractFile instance
                destFile = destFolder.getChild(relDestPath);

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

            return true;
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
    @Override
    protected boolean hasFolderChanged(AbstractFile folder) {
        return baseDestFolder.isParentOf(folder);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    protected void jobCompleted() {
        super.jobCompleted();

        // If the destination files are located inside an archive, optimize the archive file
        AbstractArchiveFile archiveFile = baseDestFolder.getParentArchive();
        if(archiveFile!=null && archiveFile.isArchive() && archiveFile.isWritable())
            optimizeArchive((AbstractRWArchiveFile)archiveFile);

        // Unselect all files in the active table upon successful completion
        if(selectedEntries!=null) {
            ActionManager.performAction(UnmarkAllAction.Descriptor.ACTION_ID, getMainFrame());
        }
    }

    @Override
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

        @Override
        public InputStream getInputStream() throws IOException {
            return archiveFile.getEntryInputStream(entry, iterator);
        }
    }
}
