/**
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


package com.mucommander.commons.file;

import java.net.MalformedURLException;

/**
 * SchemeParser is an interface that provides a single {@link #parse(String, FileURL)} method used by
 * {@link FileURL#getFileURL(String)} to turn a URL string into a corresponding <code>FileURL</code> instance.
 *
 * @see FileURL#getFileURL(String)
 * @see com.mucommander.commons.file.SchemeHandler
 * @author Maxence Bernard
 */
public interface SchemeParser {

    /**
     * Extracts the different parts from the given URL string and sets them in the specified FileURL instance.
     * The FileURL is empty when it is passed, with just the handler set. The scheme, host, port, login, password, path,
     * ... parts must all be set, using the corresponding setter methods.
     *
     * <p>Some parts such as the query and fragment have a meaning only for certain schemes such as HTTP, other schemes
     * may simply ignore the corresponding query/fragment delimiters ('?' and '#' resp.) and include them in the
     * path part.</p>
     *
     * @param url the URL to parse
     * @param fileURL the FileURL instance in which to set the different parsed parts
     * @throws MalformedURLException if the specified string is not a valid URL and cannot be parsed
     */
    public void parse(String url, FileURL fileURL) throws MalformedURLException;
}
