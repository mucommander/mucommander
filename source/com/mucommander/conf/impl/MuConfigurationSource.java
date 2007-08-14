/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.conf.impl;

import com.mucommander.RuntimeConstants;
import com.mucommander.PlatformManager;
import com.mucommander.conf.ConfigurationSource;
import com.mucommander.io.BackupInputStream;
import com.mucommander.io.BackupOutputStream;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * @author Nicolas Rinaudo
 */
class MuConfigurationSource implements ConfigurationSource {
    // - Class fields ---------------------------------------------------------------
    // ------------------------------------------------------------------------------
    /** Path to the configuration file. */
    private static       AbstractFile configurationFile;
    /** Default configuration file name. */
    private static final String       DEFAULT_CONFIGURATION_FILE_NAME = "preferences.xml";



    // - Initialisation -------------------------------------------------------------
    // ------------------------------------------------------------------------------
    public MuConfigurationSource(String path) {setConfigurationFile(path);}
    public MuConfigurationSource() {}



    // - Configuration file handling ------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Returns the path to the configuration file.
     * @return the path to the configuration file.
     */
    public static synchronized AbstractFile getConfigurationFile() {
        if(configurationFile == null)
            return FileFactory.getFile(new File(PlatformManager.getPreferencesFolder(), DEFAULT_CONFIGURATION_FILE_NAME).getAbsolutePath());
        return configurationFile;
    }

    /**
     * Sets the path to the configuration file.
     * @param  file path to the file that should be used for configuration storage.
     */
    public static synchronized void setConfigurationFile(String file) {configurationFile = FileFactory.getFile(file);}



    // - Streams handling -----------------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Returns an input stream on the configuration file.
     * @return an input stream on the configuration file.
     */
    public synchronized InputStream getInputStream() throws IOException {return new BackupInputStream(getConfigurationFile());}

    /**
     * Returns an output stream on the configuration file.
     * @return an output stream on the configuration file.
     */
    public synchronized OutputStream getOutputStream() throws IOException {return new BackupOutputStream(getConfigurationFile());}
}
