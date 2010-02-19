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
import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.io.StreamUtils;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;

import java.io.*;

/**
 * This job combines files into one file, optionally checking the CRC of the merged file.
 * @author Mariusz Jakubowski
 */
public class CombineFilesJob extends AbstractCopyJob {
	AbstractFile destFile = null;
	private OutputStream out;
	private AbstractFile crcFile;


	public CombineFilesJob(ProgressDialog progressDialog, MainFrame mainFrame,
			FileSet files, AbstractFile destFile,
			int fileExistsAction) {
		super(progressDialog, mainFrame, files, destFile, null, fileExistsAction);
        this.errorDialogTitle = Translator.get("combine_files_dialog.error_title");
	}

    @Override
    protected boolean hasFolderChanged(AbstractFile folder) {
        return baseDestFolder.isParentOf(folder);
	}

	@Override
    protected boolean processFile(AbstractFile file, Object recurseParams) {
        if (destFile == null) {
        	// executed only on first part
        	createDestFile(file);
            findCRCFile(file);
        }
        
        if(getState()==INTERRUPTED)
            return false;
        
        try {
			InputStream in = file.getInputStream();
			setCurrentInputStream(in);
			StreamUtils.copyStream(in, out);
		} catch (IOException e) {
            AppLogger.fine("Caught exception", e);
            showErrorDialog(errorDialogTitle,
                    Translator.get("error_while_transferring", destFile.getName()),
                    new String[]{CANCEL_TEXT},
                    new int[]{CANCEL_ACTION}
                    );
            interrupt();
			return false;
		} finally {
			closeCurrentInputStream();
		}
        
		return true;
	}
	
	/**
	 * Creates the destination (merged) file.
	 * @param file first part
	 */
	protected void createDestFile(AbstractFile file) {
		destFile = baseDestFolder;
		baseDestFolder = baseDestFolder.getParent();
        destFile = checkForCollision(file, baseDestFolder, destFile, false);
        if (destFile == null) {
            interrupt();
        	return;
        }
        
        try {
    		out = destFile.getOutputStream();
        } catch(IOException e) {
        	AppLogger.fine("Caught exception", e);
            showErrorDialog(errorDialogTitle,
                    Translator.get("error_while_transferring", destFile.getName()),
                    new String[]{CANCEL_TEXT},
                    new int[]{CANCEL_ACTION}
                    );
            interrupt();
        }
	}
	
	/**
	 * Checks if CRC file exists.
	 * @param file firts part
	 */
	private void findCRCFile(AbstractFile file) {
		AbstractFile f = file.getParent();
		if (f != null) {
			try {
				crcFile = f.getDirectChild(file.getNameWithoutExtension() + ".sfv");
			} catch (IOException e) {
				AppLogger.fine("Caught exception", e);
			}
		}
	}

	@Override
    protected void jobStopped() {
		super.jobStopped();
		closeOutputStream();
	}
	
	@Override
    protected void jobCompleted() {
		super.jobCompleted();
		closeOutputStream();
		checkCRC();
	}

	/**
	 * Checks CRC of merged file (if CRC file exists).
	 */
	private void checkCRC() {
		if (crcFile==null  || !crcFile.exists()) {
            showErrorDialog(errorDialogTitle,
                    Translator.get("combine_files_job.no_crc_file"),
                    new String[]{OK_TEXT},
                    new int[]{OK_ACTION}
                    );
			return;
		}
		InputStream crcIn = null;
		try {
			crcIn = crcFile.getInputStream();
			BufferedReader crcReader = new BufferedReader(new InputStreamReader(crcIn));
			String crcLine = crcReader.readLine();
			crcLine = crcLine.substring(crcLine.lastIndexOf(' ')+1).trim();
			String crcDest = destFile.calculateChecksum("CRC32");
			if (!crcLine.equals(crcDest)) {
	            showErrorDialog(errorDialogTitle,
	                    Translator.get("combine_files_job.crc_check_failed", crcDest, crcLine),
	                    new String[]{OK_TEXT},
	                    new int[]{OK_ACTION}
	                    );
			} else {
	            showErrorDialog(Translator.get("combine_files_dialog.error_title"),
	                    Translator.get("combine_files_job.crc_ok"),
	                    new String[]{OK_TEXT},
	                    new int[]{OK_ACTION}
	                    );
			}
		} catch (Exception e) {
            AppLogger.fine("Caught exception", e);
            showErrorDialog(errorDialogTitle,
                    Translator.get("combine_files_job.crc_read_error"),
                    new String[]{CANCEL_TEXT},
                    new int[]{CANCEL_ACTION}
                    );
		} finally {
			if (crcIn!=null) {
				try {
					crcIn.close();
				} catch (IOException e) {
                    AppLogger.fine("Caught exception", e);
				}
			}
		}
	}

	/**
	 * Closes the output stream.
	 */
	private void closeOutputStream() {
		if (out != null) {
			try {
				out.close();
			}
            catch (IOException e) {
                AppLogger.fine("Caught exception", e);
	            showErrorDialog(errorDialogTitle,
	                    Translator.get("error_while_transferring", destFile.getName()),
	                    new String[]{CANCEL_TEXT},
	                    new int[]{CANCEL_ACTION}
	                    );
			}
		}
	}

}
