/*
 * This file is part of muCommander, http://www.mucommander.com
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

package com.mucommander.conf;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;

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
     * @see    #setPreferencesFolder(String)
     */
    public static AbstractFile getPreferencesFolder() {
        return prefFolder;
    }

    public static AbstractFile getCredentialsFolder() throws IOException {
        AbstractFile credentialsFolder = getPreferencesFolder().getChild("/.credentials");
        if (!credentialsFolder.exists())
            credentialsFolder.mkdir();
        return credentialsFolder;
    }

    /**
     * Sets the path to the folder in which muCommander will look for its preferences.
     * <p>
     * If <code>folder</code> is a file, its parent folder will be used instead. If it doesn't exist,
     * this method will create it.
     * </p>
     * @param  path        path to the folder in which muCommander will look for its preferences.
     * @throws IOException if an IO error occurs.
     * @see                #getPreferencesFolder()
     */
    public static void setPreferencesFolder(String path) throws IOException {
        prefFolder = FileFactory.getFile(path);
    }
}
