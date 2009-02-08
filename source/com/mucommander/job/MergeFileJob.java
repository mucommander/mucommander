package com.mucommander.job;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.util.FileSet;
import com.mucommander.io.StreamUtils;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;

public class MergeFileJob extends AbstractCopyJob {
	AbstractFile destFile = null;
	private OutputStream out;
	private AbstractFile crcFile;


	public MergeFileJob(ProgressDialog progressDialog, MainFrame mainFrame,
			FileSet files, AbstractFile destFile,
			int fileExistsAction) {
		super(progressDialog, mainFrame, files, destFile, null, fileExistsAction);
        this.errorDialogTitle = Translator.get("merge_file_dialog.error_title");
	}

	protected boolean hasFolderChanged(AbstractFile folder) {
        return baseDestFolder.isParentOf(folder);
	}

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
			e.printStackTrace();
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
		baseDestFolder = baseDestFolder.getParentSilently();
        destFile = checkForCollision(file, baseDestFolder, destFile, false);
        if (destFile == null) {
            interrupt();
        	return;
        }
        
        try {
    		out = destFile.getOutputStream(false);
        } catch(IOException e) {
        	e.printStackTrace();
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
		AbstractFile f = file.getParentSilently();
		if (f != null) {
			try {
				crcFile = f.getDirectChild(file.getNameWithoutExtension() + ".sfv");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected void jobStopped() {
		super.jobStopped();
		closeOutputStream();
	}
	
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
                    Translator.get("merge_job.no_crc_file"),
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
			crcLine = crcLine.substring(crcLine.indexOf(' ')+1).trim();
			String crcDest = destFile.calculateChecksum("CRC32");
			if (!crcLine.equals(crcDest)) {
	            showErrorDialog(errorDialogTitle,
	                    Translator.get("merge_job.crc_check_failed", crcDest, crcLine),
	                    new String[]{OK_TEXT},
	                    new int[]{OK_ACTION}
	                    );
			} else {
	            showErrorDialog(Translator.get("merge_file_dialog.title"),
	                    Translator.get("merge_job.crc_ok"),
	                    new String[]{OK_TEXT},
	                    new int[]{OK_ACTION}
	                    );
			}
		} catch (Exception e) {
			e.printStackTrace();
            showErrorDialog(errorDialogTitle,
                    Translator.get("merge_job.crc_read_error"),
                    new String[]{CANCEL_TEXT},
                    new int[]{CANCEL_ACTION}
                    );
		} finally {
			if (crcIn!=null) {
				try {
					crcIn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void closeOutputStream() {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
	            showErrorDialog(errorDialogTitle,
	                    Translator.get("error_while_transferring", destFile.getName()),
	                    new String[]{CANCEL_TEXT},
	                    new int[]{CANCEL_ACTION}
	                    );
			}
		}
	}

}
