/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.conf;

import java.io.*;

/**
 * Configuration source that open input and output streams on a local file.
 * @author Nicolas Rinaudo
 */
public class FileConfigurationSource implements ConfigurationSource {
    // - Instance variables ----------------------------------------------------
    // -------------------------------------------------------------------------
    /** Path to the file on which to open input and output streams. */
    private File file;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Creates a source that will open streams on the specified file.
     * @param file path to the file on which streams should be opened.
     */
    public FileConfigurationSource(File file) {setFile(file);}

    /**
     * Creates a source that will open streams on the specified file.
     * @param path path to the file on which streams should be opened.
     */
    public FileConfigurationSource(String path) {setFile(path);}



    // - File access -----------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Sets the file on which input and output streams will be opened.
     * @param file path to the file on which input and output streams will be opened.
     * @see        #setFile(String)
     * @see        #getFile()
     */
    public synchronized void setFile(File file) {this.file = file;}

    /**
     * Sets the file on which input and output streams will be opened.
     * @param path path to the file on which input and output streams will be opened.
     * @see        #setFile(File)
     * @see        #getFile()
     */
    public synchronized void setFile(String path) {file = new File(path);}

    /**
     * Returns the file on which input and output streams are opened.
     * @return the file on which input and output streams are opened.
     * @see    #setFile(File)
     * @see    #setFile(String)
     */
    public synchronized File getFile() {return file;}



    // - Source methods --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns an input stream on the configuration file.
     * @return             an input stream on the configuration file.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized InputStream getInputStream() throws IOException {return new FileInputStream(file);}

    /**
     * Returns an output stream on the configuration file.
     * @return             an output stream on the configuration file.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized OutputStream getOutputStream() throws IOException {return new FileOutputStream(file);}
}
