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

package com.mucommander.conf;

import com.mucommander.PlatformManager;
import com.mucommander.commons.conf.ConfigurationSource;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.io.backup.BackupInputStream;
import com.mucommander.io.backup.BackupOutputStream;

import java.io.*;
import java.nio.charset.Charset;

/**
 * This abstract package-protected class represents configuration file of muCommander as configuration source 
 * for the mucommander.commons.conf package.
 * It can point to a file in a given path or to the default file located in the preferences folder if no path was given.
 * 
 * @author Nicolas Rinaudo, Arik Hadas
 */
abstract class MuConfigurationFile implements ConfigurationSource {
    // - Class fields ---------------------------------------------------------------
    // ------------------------------------------------------------------------------
    /** Path to the configuration file. */
    private 		     AbstractFile configurationFile;
    /** Default configuration file name. */
    private final String DEFAULT_CONFIGURATION_FILE_NAME;



    // - Initialization -------------------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Creates a new <code>MuConfigurationSource</code> on the specified file.
     * @param path path to the configuration file.
     * @throws FileNotFoundException if <code>path</code> is not accessible.
     */
    public MuConfigurationFile(String path, String defaultFilename) throws FileNotFoundException {
    	DEFAULT_CONFIGURATION_FILE_NAME = defaultFilename;
    	if (path != null)
    		setConfigurationFile(path);
    }


    // - Configuration file handling ------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Returns the path to the configuration file.
     * @return             the path to the configuration file.
     * @throws IOException if an error occured while locating the default configuration file.
     */
    private synchronized AbstractFile getConfigurationFile() throws IOException {
        if(configurationFile == null)
            return PlatformManager.getPreferencesFolder().getChild(DEFAULT_CONFIGURATION_FILE_NAME);
        return configurationFile;
    }

    /**
     * Sets the path to the configuration file.
     * @param  path                  path to the file that should be used for configuration storage.
     * @throws FileNotFoundException if the specified file is not a valid file.
     */
    private synchronized void setConfigurationFile(String path) throws FileNotFoundException {
        AbstractFile file;

        if((file = FileFactory.getFile(path)) == null)
            setConfigurationFile(new File(path));
        else
            setConfigurationFile(file);
    }

    /**
     * Sets the path to the configuration file.
     * @param  file                  path to the file that should be used for configuration storage.
     * @throws FileNotFoundException if the specified file is not a valid file.
     */
    private synchronized void setConfigurationFile(File file) throws FileNotFoundException {setConfigurationFile(FileFactory.getFile(file.getAbsolutePath()));}

    /**
     * Sets the path to the configuration file.
     * @param  file                  path to the file that should be used for configuration storage.
     * @throws FileNotFoundException if the specified file is not a valid file.
     */
    private synchronized void setConfigurationFile(AbstractFile file) throws FileNotFoundException {
        // Makes sure file can be used as a configuration.
        if(file.isBrowsable())
            throw new FileNotFoundException("Not a valid file: " + file.getAbsolutePath());

        configurationFile = file;
    }



    // - Streams handling -----------------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Returns an input stream on the configuration file.
     * @return an input stream on the configuration file.
     */
    public synchronized Reader getReader() throws IOException {
        return new InputStreamReader(new BackupInputStream(getConfigurationFile()), Charset.forName("utf-8"));
    }

    /**
     * Returns an output stream on the configuration file.
     * @return an output stream on the configuration file.
     */
    public synchronized Writer getWriter() throws IOException {
        return new OutputStreamWriter(new BackupOutputStream(getConfigurationFile()), Charset.forName("utf-8"));
    }
    
	public boolean isExists() throws IOException {
		return getConfigurationFile().exists();
	}
}
