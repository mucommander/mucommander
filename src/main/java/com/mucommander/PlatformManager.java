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

package com.mucommander;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.runtime.OsFamily;

/**
 * This class takes care of platform-specific issues, such as getting screen dimensions and issuing commands.
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class PlatformManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(PlatformManager.class);
	
    // - Preferences folder -----------------------------------------------------
    // --------------------------------------------------------------------------
    /** Folder in which to store the preferences. */
    private static AbstractFile prefFolder;

    /**
     * Returns the path to the default muCommander preferences folder.
     * <p>
     * This folder is:
     * <ul>
     *  <li><code>~/Library/Preferences/muCommander/</code> under MAC OS X.</li>
     *  <li><code>~/.mucommander/</code> under all other OSes.</li>
     * </ul>
     * </p>
     * <p>
     * If the default preferences folder doesn't exist, this method will create it.
     * </p>
     * @return the path to the default muCommander preferences folder.
     */
    public static AbstractFile getDefaultPreferencesFolder() {
        File folder;

        // Mac OS X specific folder (~/Library/Preferences/muCommander)
        if(OsFamily.MAC_OS_X.isCurrent())
            folder = new File(System.getProperty("user.home")+"/Library/Preferences/muCommander");
        // For all other platforms, use generic folder (~/.mucommander)
        else
            folder = new File(System.getProperty("user.home"), "/.mucommander");

        // Makes sure the folder exists.
        if(!folder.exists())
            if(!folder.mkdir())
                LOGGER.warn("Could not create preference folder: " + folder.getAbsolutePath());

        return FileFactory.getFile(folder.getAbsolutePath());
    }

    /**
     * Returns the path to the folder that contains all of the user's data.
     * <p>
     * All modules that save user data to a file should do so in a file located in
     * the folder returned by this method.
     * </p>
     * <p>
     * The value returned by this method can be set through {@link #setPreferencesFolder(File)}.
     * Otherwise, the {@link #getDefaultPreferencesFolder() default preference folder} will be
     * used.
     * </p>
     * @return the path to the user's preference folder.
     * @see    #setPreferencesFolder(AbstractFile)
     */
    public static AbstractFile getPreferencesFolder() {
        // If the preferences folder has been set, use it.
        if(prefFolder != null)
            return prefFolder;

        return getDefaultPreferencesFolder();
    }

    /**
     * Sets the path to the folder in which muCommander will look for its preferences.
     * <p>
     * If <code>folder</code> is a file, its parent folder will be used instead. If it doesn't exist,
     * this method will create it.
     * </p>
     * @param  folder      path to the folder in which muCommander will look for its preferences.
     * @throws IOException if an IO error occurs.
     * @see                #getPreferencesFolder()
     * @see                #setPreferencesFolder(String)
     * @see                #setPreferencesFolder(AbstractFile)
     */
    public static void setPreferencesFolder(File folder) throws IOException {setPreferencesFolder(FileFactory.getFile(folder.getAbsolutePath()));}

    /**
     * Sets the path to the folder in which muCommande rwill look for its preferences.
     * <p>
     * If <code>folder</code> is a file, its parent folder will be used instead. If it doesn't exist,
     * this method will create it.
     * </p>
     * @param  path        path to the folder in which muCommander will look for its preferences.
     * @throws IOException if an IO error occurs.
     * @see                #getPreferencesFolder()
     * @see                #setPreferencesFolder(File)
     * @see                #setPreferencesFolder(AbstractFile)
     */
    public static void setPreferencesFolder(String path) throws IOException {
        AbstractFile folder;

        if((folder = FileFactory.getFile(path)) == null)
            setPreferencesFolder(new File(path));
        else
            setPreferencesFolder(folder);
    }

    /**
     * Sets the path to the folder in which muCommander will look for its preferences.
     * <p>
     * If <code>folder</code> is a file, its parent folder will be used instead. If it doesn't exist,
     * this method will create it.
     * </p>
     * @param  folder      path to the folder in which muCommander will look for its preferences.
     * @throws IOException if an IO error occurs.
     * @see                #getPreferencesFolder()
     * @see                #setPreferencesFolder(String)
     * @see                #setPreferencesFolder(File)
     */
    public static void setPreferencesFolder(AbstractFile folder) throws IOException {
        if(!folder.exists())
            folder.mkdir();
        else if(!folder.isBrowsable())
            folder = folder.getParent();
        prefFolder = folder;
    }
}
