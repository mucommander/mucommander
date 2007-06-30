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

package com.mucommander.file.filter;

/**
 * This filename filter only accepts filenames ending with an extension specified at creation time of this filter.
 * The case is ignored when testing filenames: all case variations of an extension will be accepted by {@link #accept(String)}.
 *
 * <p>The extension(s) may be any string, but when used in the traditional sense of a file extension (e.g. zip extension)
 * the '.' character must be included in the specified extension (e.g. ".zip" must be used, not "zip").
 * 
 * @author Maxence Bernard
 */
public class ExtensionFilenameFilter extends FilenameFilter {

    private String extensions[];

    public ExtensionFilenameFilter(String extensions[]) {
        this.extensions = extensions;

        // Convert extensions to lower-case
        int nbExtensions = extensions.length;
        for(int i=0; i<nbExtensions; i++)
            extensions[i] = extensions[i].toLowerCase();
    }

    public ExtensionFilenameFilter(String extension) {
        this(new String[]{extension});
    }


    public boolean accept(String filename) {
        // Convert filename to lower-case, as extensions already are lower-cased.
        String filenameLC = filename.toLowerCase();
        int nbExtensions = extensions.length;

        for(int i=0; i<nbExtensions; i++)
            if(filenameLC.endsWith(extensions[i]))
                return true;

        return false;
    }
}
