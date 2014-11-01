/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.desktop.xfce;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.desktop.QueuedTrash;
import com.mucommander.job.DeleteJob;
import com.mucommander.process.ProcessRunner;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;

/**
 * This class provides access to the Xfce trash.
 *
 * <p>
 * <b>Implementation notes:</b><br>
 * <br>
 * This trash is implemented as a {@link com.mucommander.desktop.QueuedTrash} as it spawns a process to move a file to
 * the trash and it is thus more effective to group files to be moved instead of spawning multiple processes.<br>
 * </p>
 * 
 * TODO: combine this trash and gnome trash to "freedesktop" trash
 * 
 * @see XfceTrashProvider
 * @author Arik Hadas
 */
public class XfceTrash extends QueuedTrash {
	private static final Logger LOGGER = LoggerFactory.getLogger(XfceTrash.class);
	
	/** Open trash folder in Thunar */ 
	private final static String REVEAL_TRASH_COMMAND = "thunar trash:///";

	/**
	 * User trash folder, as defined by the freedesktop specification (see http://freedesktop.org/wiki/Specifications/trash-spec)
	 * <code>null</code> if there is no usable trash folder.
	 */
	private final static AbstractFile TRASH_FOLDER;

	/** "info" subfolder of the user trash folder */
	private final static AbstractFile TRASH_INFO_SUBFOLDER;

	/** "files" subfolder of the user trash folder */
	private final static AbstractFile TRASH_FILES_SUBFOLDER;

	/** Volume on which the trash folder resides, used for checking whether a file can be moved to the trash or not */
	private final static AbstractFile TRASH_VOLUME;

	/** Formats dates in trash info files */
	private final static SimpleDateFormat INFO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	/**
	 * Resolves the user Trash folder and its "info" and "files" subfolders once and for all.
	 * The trash folder is created if it doesn't already exist.
	 */
	static {
		TRASH_FOLDER = getTrashFolder();
		if(TRASH_FOLDER!=null) {
			TRASH_INFO_SUBFOLDER = TRASH_FOLDER.getChildSilently("info");
			TRASH_FILES_SUBFOLDER = TRASH_FOLDER.getChildSilently("files");
			TRASH_VOLUME = TRASH_FOLDER.getVolume();
		}
		else {
			TRASH_INFO_SUBFOLDER = null;
			TRASH_FILES_SUBFOLDER = null;
			TRASH_VOLUME = null;
		}
	}

	/**
	 * Tries to find an existing user Trash folder and returns it. If no existing
	 * Trash folder was found, creates the standard Xfce user Trash folder and returns it.
	 *
	 * @return the user Trash folder, <code>null</code> if no user trash folder could be found or created
	 */
	private static AbstractFile getTrashFolder() {
		AbstractFile userHome = LocalFile.getUserHome();

		AbstractFile trashDir = userHome.getChildSilently(".local/share/Trash/");
		if(isTrashFolder(trashDir)) {
			return trashDir;
		}

		// No existing user trash was found: create the folder, only if it doesn't already exist.
		if(!trashDir.exists()) {
			try {
				trashDir.mkdirs();
				trashDir.getChild("info").mkdir();
				trashDir.getChild("files").mkdir();

				return trashDir;
			}
			catch(IOException e) {
				// Will return null
			}
		}

		return null;
	}

	/**
	 * Return <code>true</code> if the specified file is a Xfce Trash folder, i.e. is a directory and has two
	 * subdirectories named "info" and "files".
	 *
	 * @param file the file to test
	 * @return <code>true</code> if the specified file is a Xfce Trash folder
	 */
	private static boolean isTrashFolder(AbstractFile file) {
		try {
			return file.isDirectory() && file.getChild("info").isDirectory() && file.getChild("files").isDirectory();
		}
		catch(IOException e) {
			return false;
		}
	}

	/**
     * Implementation of {@link com.mucommander.desktop.QueuedTrash} moveToTrash method.
     * <p>
     * Try to copy a collection of files to the Xfce's Trash.
     * </p>
     * @param queuedFiles Collection of files to the trash
     * @return <code>true</code> if movement has been successful or <code>false</code> otherwise
     */
    @Override
    protected boolean moveToTrash(List<AbstractFile> queuedFiles) {
        int nbFiles = queuedFiles.size();
        String fileInfoContent;
        String trashFileName;
        boolean retVal = true;     // overall return value (if everything went OK or at least one file wasn't moved properly
        
        for(int i=0; i<nbFiles; i++) {
            AbstractFile fileToDelete = queuedFiles.get(i);
            // generate content of info file and new filename
            try {
                fileInfoContent = getFileInfoContent(fileToDelete);
                trashFileName = getUniqueFilename(fileToDelete);
            } catch (IOException ex) {
                LOGGER.debug("Failed to create filename for new trash item: " + fileToDelete.getName(), ex);
                
                // continue with other file (do not move file, because info file cannot be properly created
                continue;
            }

            AbstractFile infoFile = null;
            OutputStreamWriter infoWriter = null;
            try {
                // create info file
                infoFile = TRASH_INFO_SUBFOLDER.getChild(trashFileName + ".trashinfo");
                infoWriter = new OutputStreamWriter(infoFile.getOutputStream());
                infoWriter.write(fileInfoContent);
            } catch (IOException ex) {
                retVal = false;
                LOGGER.debug("Failed to create trash info file: " + trashFileName, ex);

                // continue with other file (do not move file, because info file wasn't properly created)
                continue;
            }
            finally {
                if(infoWriter!=null) {
                    try {
                        infoWriter.close();
                    }
                    catch(IOException e) {
                        // Not much else to do
                    }
                }
            }
            
            try {
                // rename original file
                fileToDelete.renameTo(TRASH_FILES_SUBFOLDER.getChild(trashFileName));
            } catch (IOException ex) {
                try {
                    // remove info file
                    infoFile.delete();

                } catch (IOException ex1) {
                    // simply ignore
                }
                
                retVal = false;
                LOGGER.debug("Failed to move file to trash: " + trashFileName, ex);
            }
        }

        return retVal;
    }

	/**
     * Implementation notes: returns <code>true</code> only for local files that are not archive entries and that
     * reside on the same volume as the trash folder.
     */
    @Override
    public boolean canMoveToTrash(AbstractFile file) {
        return TRASH_FOLDER!=null
            && file.getTopAncestor() instanceof LocalFile
            && file.getVolume().equals(TRASH_VOLUME);
    }

	/**
     * <b>Implementation notes:</b> always returns <code>true</code>.
     * 
     * @return True if trash can be emptied, otherwise false
     */
    @Override
    public boolean canEmpty() {
        return TRASH_FOLDER!=null;
    }

    /**
     * Empty the trash
     * <p>
     * <b>Implementation notes:</b><br>
     * Simply free the <code>TRASH_PATH</code> directory
     * </p>
     * 
     * @return True if everything went well
     */
    @Override
    public boolean empty() {
        // Abort if there is no usable trash folder
        if(TRASH_FOLDER==null)
            return false;

        FileSet filesToDelete = new FileSet(TRASH_FOLDER);

        try {
            // delete real files
            filesToDelete.addAll(TRASH_FILES_SUBFOLDER.ls());
            // delete spec files
            filesToDelete.addAll(TRASH_INFO_SUBFOLDER.ls());
        } catch (java.io.IOException ex) {
            LOGGER.debug("Failed to list files", ex);
            return false;
        }

        if (filesToDelete.size() > 0) {
            // Starts deleting files
            MainFrame mainFrame = WindowManager.getCurrentMainFrame();
            ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("delete_dialog.deleting"));
            DeleteJob deleteJob = new DeleteJob(progressDialog, mainFrame, filesToDelete, false);
            progressDialog.start(deleteJob);
        }
            
        return true;
    }

    @Override
    public boolean isTrashFile(AbstractFile file) {
        return TRASH_FOLDER!=null
            && (file.getTopAncestor() instanceof LocalFile)
            && TRASH_FOLDER.isParentOf(file);
    }

	/**
     * Return trash files count
     * <p>
     * We assume the count of items in trash equals the count of files in 
     * <code>TRASH_PATH + "/info"</code> folder.
     * 
     * @return Count of files in trash
     */
    @Override
    public int getItemCount() {
        // Abort if there is no usable trash folder
        if(TRASH_FOLDER==null)
            return -1;

        try {
            return TRASH_INFO_SUBFOLDER.ls().length;
        } catch (java.io.IOException ex) {
            // can't access trash folder
            return -1;
        }
    }

    /**
     * Opens the trash in Thunar.
     */
    @Override
    public void open() {
        try {
            ProcessRunner.execute(REVEAL_TRASH_COMMAND).waitFor();
        }
        catch(Exception e) {    // IOException, InterruptedException
            LOGGER.debug("Caught an exception running command \"" + REVEAL_TRASH_COMMAND + "\"", e);
        }
    }

	@Override
    public boolean canOpen() {
        return TRASH_FOLDER!=null;
    }
	
	/**
     * Make a content of .trashinfo file
     * @param file File for which the content is built
     * @return Final content
     */
    private String getFileInfoContent(AbstractFile file) {
        synchronized(INFO_DATE_FORMAT) {        // SimpleDateFormat is not thread safe
            return "[Trash Info]\n" +
                    "Path=" + file.getAbsolutePath() + "\n" +
                    "DeletionDate=" + INFO_DATE_FORMAT.format(new Date());
        }
    }
    
    /**
     * It is possible to add several files with same name to the Trash. These files are distinguished
     * by _N appended to the name, where _N is rising int number. <br/>
     * This method tries to find first empty <code>filename_N.ext</code>.
     *
     * @param file File to be deleted
     * @return Suitable filename in trash (without .trashinfo extension)
     */
    private String getUniqueFilename(AbstractFile file) throws IOException {
        // try if no previous file in trash exists
        if (!TRASH_FILES_SUBFOLDER.getChild(file.getName()).exists())
            return file.getName();

        String rawName = file.getNameWithoutExtension();
        String extension = file.getExtension();

        // find first empty filename in format filename_N.ext
        String filename;
        int count = 1;
        while(true) {
            filename = rawName + "_" + count++;
            if(extension!=null)
                filename += "." + extension;

            if(!TRASH_FILES_SUBFOLDER.getChild(filename).exists())
                return filename;
        }
    }
}
