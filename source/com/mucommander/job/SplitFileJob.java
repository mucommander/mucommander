/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

import com.mucommander.AppLogger;
import com.mucommander.file.*;
import com.mucommander.file.util.FileSet;
import com.mucommander.io.BufferPool;
import com.mucommander.io.ChecksumInputStream;
import com.mucommander.io.FileTransferException;
import com.mucommander.io.StreamUtils;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.SplitFileAction;
import com.mucommander.ui.dialog.file.FileCollisionDialog;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This job split the file into parts with given size.
 * @author Mariusz Jakubowski
 */
public class SplitFileJob extends AbstractCopyJob {

    private long partSize;
	private AbstractFile sourceFile;
	private InputStream origFileStream;
	private AbstractFile destFolder;
	private long sizeLeft;
	private boolean recalculateCRC = false;


	/**
	 * A class for holding file name and size of one part.
	 * @author Mariusz Jakubowski
	 *
	 */
	private static class DummyDestFile extends DummyFile {
		private long size;
		
		public DummyDestFile(FileURL url, long size) {
			super(url);
			this.size = size;
		}
		
		@Override
        public long getSize() {
			return size;
		}
	}

	
	public SplitFileJob(ProgressDialog progressDialog, MainFrame mainFrame, 
			AbstractFile file, AbstractFile destFolder, long partSize, int parts) {
        super(progressDialog, mainFrame, new FileSet(), destFolder, null, FileCollisionDialog.ASK_ACTION);
        this.partSize = partSize;
        this.setNbFiles(parts);
        this.sourceFile = file;
        this.destFolder = destFolder;
        this.errorDialogTitle = Translator.get("split_file_dialog.error_title");
        createInputStream();
        sizeLeft = sourceFile.getSize();
        for (int i=1; i<=parts; i++) {
        	addDummyFile(i, Math.min(partSize, sizeLeft));
        	sizeLeft -= partSize;
        }
        sizeLeft = sourceFile.getSize();
    }

	/**
	 * Adds a dummy output file (used in progress monitoring). 
	 * @param i index of a file
	 * @param size size of a file
	 */
	private void addDummyFile(int i, long size) {
    	String num;
    	if (i<10) {
    		num = "00" + Integer.toString(i); 
    	} else if (i<100) {
    		num = "0" + Integer.toString(i); 
    	} else {
    		num = Integer.toString(i); 
    	}
        FileURL childURL = (FileURL)destFolder.getURL().clone();
        childURL.setPath(destFolder.addTrailingSeparator(childURL.getPath()) + sourceFile.getName() + "." + num);
    	DummyDestFile fileHolder = new DummyDestFile(childURL, size);
    	files.add(fileHolder);
	}
	
	
	@Override
    protected void jobStarted() {
		super.jobStarted();
		createInputStream();
	}

	/**
	 * Creates an input stream from the file. 
	 */
	private void createInputStream() {
        try {
        	origFileStream = sourceFile.getInputStream();
		}
        catch (IOException e) {
            AppLogger.fine("Caught exception", e);
            showErrorDialog(errorDialogTitle,
                    Translator.get("error_while_transferring", sourceFile.getName()),
                    new String[]{CANCEL_TEXT},
                    new int[]{CANCEL_ACTION}
                    );
            setState(INTERRUPTED);
            return;
		}
        origFileStream = setCurrentInputStream(origFileStream);
        // init checksum calculation
        if (isIntegrityCheckEnabled()) {
			try {
				origFileStream = new ChecksumInputStream(origFileStream, MessageDigest.getInstance("CRC32"));
			} catch (NoSuchAlgorithmException e) {
				setIntegrityCheckEnabled(false);
                AppLogger.fine("Caught exception", e);
			}
        }
	}
	

    @Override
    protected boolean processFile(AbstractFile file, Object recurseParams) {
        if(getState()==INTERRUPTED)
            return false;
        
        // Create destination AbstractFile instance
        AbstractFile destFile = createDestinationFile(baseDestFolder, file.getName());
        if (destFile == null)
            return false;

        destFile = checkForCollision(sourceFile, baseDestFolder, destFile, false);
        if (destFile == null)
            return false;
        
        OutputStream out = null;
        try {
			out = destFile.getOutputStream();
			
			try {
				long written = StreamUtils.copyStream(origFileStream, out, BufferPool.getDefaultBufferSize(), partSize);
				sizeLeft -= written;
			} catch (FileTransferException e) {
				if (e.getReason() == FileTransferException.WRITING_DESTINATION) {
					// out of disk space - ask a user for a new disk
					recalculateCRC = true;		// recalculate CRC (DigestInputStream doesn't support mark() and reset())
					out.close();
					out = null;
					sizeLeft -= e.getBytesWritten();
					showErrorDialog(ActionProperties.getActionLabel(SplitFileAction.Descriptor.ACTION_ID),
							Translator.get("split_file_dialog.insert_new_media"), 
							new String[]{OK_TEXT, CANCEL_TEXT}, 
							new int[]{OK_ACTION, CANCEL_ACTION});
					if (getState()==INTERRUPTED) {
						return false;
					}
					// create new output file if necessary
					if ((sizeLeft>0) && (getCurrentFileIndex() == getNbFiles()-1)) {
						setNbFiles(getNbFiles() + 1);
						addDummyFile(getNbFiles(), sizeLeft);
					}
				} else {
					throw e;
				}
			}
			
	        // Preserve source file's date
            if(destFile.isFileOperationSupported(FileOperation.CHANGE_DATE)) {
                try {
                    destFile.changeDate(sourceFile.getDate());
                }
                catch (IOException e) {
                    AppLogger.fine("failed to change date of "+destFile, e);
                    // Fail silently
                }
            }

	        // Preserve source file's permissions: preserve only the permissions bits that are supported by the source
            // file and use default permissions for the rest of them.
            if(destFile.isFileOperationSupported(FileOperation.CHANGE_PERMISSION)) {
                try {
                    // use #importPermissions(AbstractFile, int) to avoid isDirectory test
                    destFile.importPermissions(sourceFile, FilePermissions.DEFAULT_FILE_PERMISSIONS);
                }
                catch (IOException e) {
                    AppLogger.fine("failed to import "+sourceFile+" permissions into "+destFile, e);
                    // Fail silently
                }
            }
		}
        catch (IOException e) {
            AppLogger.fine("Caught exception", e);

            showErrorDialog(errorDialogTitle,
                    Translator.get("error_while_transferring", destFile.getName()),
                    new String[]{CANCEL_TEXT},
                    new int[]{CANCEL_ACTION}
                    );
			return false;
			
		} finally {
            try {
            	if (out!=null)
            		out.close();
            }
            catch(IOException e2) {
            }
		}
		
    	return true;
    }


    // This job modifies baseDestFolder and its subfolders
    @Override
    protected boolean hasFolderChanged(AbstractFile folder) {
        return baseDestFolder.isParentOf(folder);
    }
    
    @Override
    protected void jobCompleted() {
    	// create checksum file
    	if (isIntegrityCheckEnabled()) {
            if(origFileStream!=null && (origFileStream instanceof ChecksumInputStream)) {
            	String crcFileName = sourceFile.getName() + ".sfv";
                try {
	            	String sourceChecksum;
	            	if (recalculateCRC ) {
	            		origFileStream = sourceFile.getInputStream();
						sourceChecksum = AbstractFile.calculateChecksum(origFileStream, MessageDigest.getInstance("CRC32"));
						origFileStream.close();
	            	} else {
	                	sourceChecksum = ((ChecksumInputStream)origFileStream).getChecksumString();
	            	}
					AbstractFile crcFile = baseDestFolder.getDirectChild(crcFileName);
					OutputStream crcStream = crcFile.getOutputStream();
					String line = sourceFile.getName() + " " + sourceChecksum;
					crcStream.write(line.getBytes("utf-8"));
					crcStream.close();
				} catch (Exception e) {
                    AppLogger.fine("Caught exception", e);
					
		            showErrorDialog(errorDialogTitle,
		                    Translator.get("error_while_transferring", crcFileName),
		                    new String[]{CANCEL_TEXT},
		                    new int[]{CANCEL_ACTION}
		                    );
				}
            }
    	}
    	super.jobCompleted();
    }


}
