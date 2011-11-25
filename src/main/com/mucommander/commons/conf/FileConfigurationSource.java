/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.commons.conf;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Configuration source that work on a {@link File} instance.
 * @author Nicolas Rinaudo
 */
public class FileConfigurationSource implements ConfigurationSource {
    // - Instance variables --------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Path to the file on which to open input and output streams. */
    private final File    file;
    /** File's charset. */
    private final Charset charset;



    // - Initialization ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a source that will open streams on the specified file.
     * @param file file in which the configuration data is located.
     * @deprecated Application developers should use {@link #FileConfigurationSource(File, Charset)} instead. This
     *             constructor assumes the specified file to be <code>UTF-8</code> encoded.
     */
    @Deprecated
    public FileConfigurationSource(File file) {
        this(file, "utf-8");
    }

    /**
     * Creates a source on the specified file and charset.
     * @param file file in which the configuration data is located.
     * @param charset charset in which the file is encoded.
     */
    public FileConfigurationSource(File file, Charset charset) {
        this.file    = file;
        this.charset = charset;
    }

    /**
     * Creates a source on the specified file and charset.
     * @param file file in which the configuration data is located.
     * @param charset charset in which the file is encoded.
     */
    public FileConfigurationSource(File file, String charset) {
        this(file, Charset.forName(charset));
    }

    /**
     * Creates a source that will open streams on the specified file.
     * @param path  path to the file in which the configuration data is located.
     * @deprecated Application developers should use {@link #FileConfigurationSource(String, Charset)} instead. This
     *             constructor assumes the specified file to be <code>UTF-8</code> encoded.
     */
    @Deprecated
    public FileConfigurationSource(String path) {
        this(new File(path));
    }

    /**
     * Creates a source on the specified file and charset.
     * @param path  path to the file in which the configuration data is located.
     * @param charset charset in which the file is encoded.
     */
    public FileConfigurationSource(String path, String charset) {
        this(new File(path), charset);
    }

    /**
     * Creates a source on the specified file and charset.
     * @param path  path to the file in which the configuration data is located.
     * @param charset charset in which the file is encoded.
     */
    public FileConfigurationSource(String path, Charset charset) {
        this(new File(path), charset);
    }



    // - File access ---------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the file on which input and output streams are opened.
     * @return the file on which input and output streams are opened.
     */
    public File getFile() {
        return file;
    }

    /**
     * Returns the charset in which the {@link #getFile() configuration file} is encoded.
     * @return the charset in which the {@link #getFile() configuration file} is encoded.
     */
    public Charset getCharset() {
        return charset;
    }



    // - Source methods ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    @Override
    public Reader getReader() throws IOException {
        return new InputStreamReader(new FileInputStream(file), charset);
    }

    @Override
    public Writer getWriter() throws IOException {
        return new OutputStreamWriter(new FileOutputStream(file), charset);
    }

	@Override
	public boolean isExists() {
		return file.exists();
	}
}
