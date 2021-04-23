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

package com.mucommander.main;

import java.io.File;

/**
 * @author Arik Hadas
 */
public class UserPreferencesDir {

    /**
     * Gets the folder in which muCommander will look for its preferences in accordance with
     * the specified path.
     * <p>
     * If <code>folder</code> is a file, its parent folder will be used instead. If it doesn't exist,
     * this method will create it.
     * </p>
     * @param  folder           path to the folder in which muCommander should look for its preferences.
     * @throws RuntimeException if an error occurs in creating a folder in the specified path.
     * @return a folder in which muCommander will look for its preferences.
     */
    public static File getPreferencesFolder(String folder) {
        File file = new File(folder);

        if (file.exists() && !file.isDirectory())
            file = file.getParentFile();

        if (!file.exists() && !file.mkdir())
            throw new RuntimeException("cannot create: " + file.getAbsolutePath());

        return file;
    }

    /**
     * Returns the default muCommander preferences folder.
     * <p>
     * This folder is:
     * <ul>
     *  <li><code>~/Library/Preferences/muCommander/</code> under macOS.</li>
     *  <li><code>/boot/home/config/settings/mucommander</code> under Haiku.</li>
     *  <li><code>~/.mucommander/</code> under all other OSes.</li>
     * </ul>
     * </p>
     * <p>
     * If the default preferences folder doesn't exist, this method will create it.
     * </p>
     * @throws RuntimeException if an error occurs in creating the folder or a non-directory file exists at the same path.
     * @return the default muCommander preferences folder.
     */
    public static File getDefaultPreferencesFolder() {
        File folder = getDefaultPreferencesFolderPerOperatingSystem();

        if (folder.exists() && !folder.isDirectory())
            throw new RuntimeException("not a directory: " + folder.getAbsolutePath());

        if (!folder.exists() && !folder.mkdir())
            throw new RuntimeException("cannot create: " + folder.getAbsolutePath());

        return folder;
    }

    private static File getDefaultPreferencesFolderPerOperatingSystem() {
        // macOS X specific folder (~/Library/Preferences/muCommander)
        if (System.getProperty("os.name").startsWith("Mac OS X"))
            return new File(System.getProperty("user.home")+"/Library/Preferences/muCommander");

        // For all other platforms, use generic folder (~/.mucommander)
        File folder = new File(System.getProperty("user.home"), "/.mucommander");

        // Respect the environment variable XDG_CONFIG_HOME on Linux and try
        // to migrate ~/.mucommander, if exists, to the configured folder, if set.
        String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
        if (xdgConfigHome != null) {
            File xdgConfigHomeFolder = new File(xdgConfigHome, "/mucommander");
            if (folder.exists())
                folder.renameTo(xdgConfigHomeFolder);
            folder = xdgConfigHomeFolder;
        }

        return folder;
    }
}
