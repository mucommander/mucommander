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

import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.FileCollisionDialog;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.viewer.ViewerRegistrar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

/**
 * This job calculates a checksum for a list of files and stores the results in a checksum file.
 *
 * <p>The format of this file is a de facto standard ; a line is created for each file and goes like this:
 * <pre>
 * e7e9576b9e55940b4b8522a65902d4cd  readme.txt
 * 119abda7c941135d5bf382c386bca2ca  i386/debian-40r1-i386-DVD-1.iso
 * 3c0d332902b9b8dfec43ba02d1618c6e  ppc/debian-40r1-ppc-DVD-1.iso
 * ...
 * </pre>
 * The path of each file is relative to the checksum file's path. In the above example, <code>readme.txt</code> and
 * the checksum file are located in the same folder. Note that 2 space characters (and not just one as anyone in his
 * right mind would think) separate the hexadecimal checksum from the file path.
 * </p>
 *
 * <p>The above file format is used for all checksum algorithms but one: CRC32, which uses the special SFV format where
 * the checksum for each file is written as follow:
 * <pre>
 * wne-ebai.r00 697115b2
 * wne-ebai.r01 f80a8443
 * ...
 * </pre>
 * </p>
 *
 * @author Maxence Bernard
 */
public class CalculateChecksumJob extends TransferFileJob {

    /** The checksum file where the checksum of each file is written */
    private AbstractFile checksumFile;
    /** The OutputStream of the checksum file */
    private OutputStream checksumFileOut;

    /** The path to the base source folder, i.e. the folder which contains all the files this job operates on */
    private String baseSourcePath;

    /** True if the SFV format is used rather than the default 'SUMS' format */
    private boolean useSfvFormat;

    /** The MessageDigest that serves to calculate the checksum */
    private MessageDigest digest;


    public CalculateChecksumJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files, AbstractFile checksumFile, MessageDigest digest) {
        super(progressDialog, mainFrame, files);

        this.checksumFile = checksumFile;
        this.digest = digest;
        this.useSfvFormat = digest.getAlgorithm().equalsIgnoreCase("CRC32");

        this.baseSourcePath = baseSourceFolder.getAbsolutePath(true);
    }


    ////////////////////////////////////
    // TransferFileJob implementation //
    ////////////////////////////////////

    protected boolean processFile(AbstractFile file, Object recurseParams) {
        // Skip directories
        if(file.isDirectory()) {
            do {		// Loop for retry
                try {
                    // for each file in folder...
                    AbstractFile children[] = file.ls();
                    for(int i=0; i<children.length && getState()!=INTERRUPTED; i++) {
                        // Notify job that we're starting to process this file (needed for recursive calls to processFile)
                        nextFile(children[i]);
                        processFile(children[i], null);
                    }

                    return true;
                }
                catch(IOException e) {
                    // file.ls() failed
                    int ret = showErrorDialog(Translator.get("error"), Translator.get("cannot_read_folder", file.getName()));
                    // Retry loops
                    if(ret==RETRY_ACTION)
                        continue;
                    // Cancel, skip or close dialog returns false
                    return false;
                }
            } while(true);
        }

        // Calculate the file's checksum
        do {		// Loop for retry
            InputStream in = null;
            String line;
            String checksum;
            try {
                // Resets the digest before use
                digest.reset();

                in = null;
                in = setCurrentInputStream(file.getInputStream());

                // Determine the path relative to the base source folder
                String relativePath = file.getAbsolutePath();
                relativePath = relativePath.substring(baseSourcePath.length(), relativePath.length());

                // Write a new line in the checksum file, in the appropriate format
                checksum = AbstractFile.calculateChecksum(in, digest);
                if(useSfvFormat) {
                    // SFV format for CRC32 checksums
                    line = relativePath + " " + checksum;     // 1 space character
                }
                else {
                    // 'SUMS' format for other checksum algorithms
                    line = checksum + "  " + relativePath;    // 2 space characters, that's how the format is
                }

                line += '\n';

                // Close the InputStream, we're done with it
                in.close();

                checksumFileOut.write(line.getBytes("utf-8"));

                return true;
            }
            catch(IOException e) {
                // Close the InputStream, a new one will be created when retrying
                if(in!=null) {
                    try { in.close(); }
                    catch(IOException e2){}
                }

                // If the job was interrupted by the user at the time the exception occurred, it most likely means that
                // the IOException was caused by the stream being closed as a result of the user interruption.
                // If that is the case, the exception should not be interpreted as an error.
                // Same goes if the current file was skipped.
                if(getState()==INTERRUPTED || wasCurrentFileSkipped())
                    return false;

                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Caught IOException: "+e);
                
                int ret = showErrorDialog(Translator.get("error"), Translator.get("error_while_transferring", file.getAbsolutePath()));
                // Retry loops
                if(ret==RETRY_ACTION) {
                    // Reset processed bytes currentFileByteCounter
                    getCurrentFileByteCounter().reset();

                    continue;
                }

                // Cancel, skip or close dialog return false
                return false;
            }
        } while(true);
    }

    protected boolean hasFolderChanged(AbstractFile folder) {
        // This job modifies the folder where the checksum file is
        return folder.equals(checksumFile.getParentSilently());     // Note: parent may be null
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    protected void jobStarted() {
        super.jobStarted();

        // Check for file collisions, i.e. if the file already exists in the destination
        int collision = FileCollisionChecker.checkForCollision(null, checksumFile);
        if(collision!=FileCollisionChecker.NO_COLLOSION) {
            // File already exists in destination, ask the user what to do (cancel, overwrite,...) but
            // do not offer the multiple files mode options such as 'skip' and 'apply to all'.
            int choice = waitForUserResponse(new FileCollisionDialog(progressDialog, mainFrame, collision, null, checksumFile, false, false));

            // Overwrite file
            if (choice== FileCollisionDialog.OVERWRITE_ACTION) {
                // Do nothing, simply continue and file will be overwritten
            }
            // 'Cancel' or close dialog interrupts the job
            else {
                interrupt();
                return;
            }
        }

        // Loop for retry
        do {
            try {
                // Tries to get an OutputStream on the destination file
                this.checksumFileOut = checksumFile.getOutputStream(false);

                break;

            }
            catch(Exception e) {
                int choice = showErrorDialog(Translator.get("error"),
                                             Translator.get("cannot_write_file", checksumFile.getName()),
                                             new String[] {CANCEL_TEXT, RETRY_TEXT},
                                             new int[]  {CANCEL_ACTION, RETRY_ACTION}
                                             );

                // Retry loops
                if(choice == RETRY_ACTION)
                    continue;

                // 'Cancel' or close dialog interrupts the job
                interrupt();
                return;
            }
        } while(true);
    }

    protected void jobCompleted() {
        super.jobCompleted();

        // Open the checksum file in a viewer
        ViewerRegistrar.createViewerFrame(mainFrame, checksumFile, IconManager.getImageIcon(checksumFile.getIcon()).getImage());
    }

    protected void jobStopped() {
        super.jobStopped();
        
        // Close the checksum file's OutputStream
        if(checksumFileOut !=null) {
            try { checksumFileOut.close(); }
            catch(IOException e2){
                // No need to inform the user
            }
        }
    }
}
