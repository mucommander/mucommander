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
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.conf.ConfigurationSource;
import com.mucommander.conf.ConfigurationReaderFactory;
import com.mucommander.conf.ConfigurationWriterFactory;
import com.mucommander.conf.ConfigurationReader;
import com.mucommander.conf.ConfigurationWriter;
import com.mucommander.conf.XmlConfigurationReader;
import com.mucommander.conf.XmlConfigurationWriter;
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
 * muCommander's implementation of the configuration.
 * <p>
 * This acts as a {@link com.mucommander.conf.ConfigurationSource}, a {@link com.mucommander.conf.ConfigurationReaderFactory} and
 * a {@link com.mucommander.conf.ConfigurationWriterFactory}.
 * </p>
 * <p>
 * The configuration file format used is that defined in {@link com.mucommander.conf.XmlConfigurationReader}.
 * </p>
 * <p>
 * <code>MucoConfigurationSource</code> behaves like a {@link com.mucommander.conf.FileConfigurationSource}, but uses
 * instances of {@link com.mucommander.file.AbstractFile} rather <code>java.io.File</code>. This allows configuration data
 * to be loaded from remote file systems, inside archives, ...
 * </p>
 * @author Nicolas Rinaudo
 */
public class MucoConfigurationSource implements ConfigurationSource, ConfigurationReaderFactory, ConfigurationWriterFactory {
    // - Class fields ---------------------------------------------------------------
    // ------------------------------------------------------------------------------
    /** Path to the configuration file. */
    private static       AbstractFile configurationFile;
    /** Default configuration file name. */
    private static final String       DEFAULT_CONFIGURATION_FILE_NAME = "preferences.xml";




    // - Backward compatibility -----------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Moves old configuration variables to their new names.
     */
    public static void processConfiguration() {
        String configurationVersion;

        configurationVersion = ConfigurationManager.getVariable(ConfigurationVariables.VERSION);
        if(configurationVersion == null || !configurationVersion.equals(RuntimeConstants.VERSION)) {
            ConfigurationManager.renameVariable("show_hidden_files", ConfigurationVariables.SHOW_HIDDEN_FILES);
            ConfigurationManager.renameVariable("auto_size_columns", ConfigurationVariables.AUTO_SIZE_COLUMNS);
            ConfigurationManager.renameVariable("show_toolbar",      ConfigurationVariables.TOOLBAR_VISIBLE);
            ConfigurationManager.renameVariable("show_status_bar",   ConfigurationVariables.STATUS_BAR_VISIBLE);
            ConfigurationManager.renameVariable("show_command_bar",  ConfigurationVariables.COMMAND_BAR_VISIBLE);
            ConfigurationManager.setVariable(ConfigurationVariables.VERSION, RuntimeConstants.VERSION);
        }
    }


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



    // - File format handling -------------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Returns a {@link com.mucommander.conf.ConfigurationReader} used to parse the muCommander configuration file.
     * @return the object that will be used to parse the muCommander configuration file.
     */
    public ConfigurationReader getReaderInstance() {return new XmlConfigurationReader();}

    /**
     * Returns a {@link com.mucommander.conf.ConfigurationWriter} used to write the muCommander configuration file.
     * @return the object that will be used to write the muCommander configuration file.
     */
    public ConfigurationWriter getWriterInstance() {return new XmlConfigurationWriter();}
}
