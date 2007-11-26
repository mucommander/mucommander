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
 * This {@link FilenameFilter} matches filenames ending with one of several specified extensions.
 * The filter can be made case-sensitive or case-insensitive, this behavior is specified at creation time.
 *
 * <p>The extension(s) may be any string, but when used in the traditional sense of a file extension (e.g. zip extension)
 * the '.' character must be included in the specified extension (e.g. ".zip" must be used, not just "zip").</p>
 * 
 * @author Maxence Bernard
 */
public class ExtensionFilenameFilter extends FilenameFilter {

    /** File extensions to match against filenames */
    private String extensions[];

    /**
     * Creates a new <code>ExtensionFilenameFilter</code> that matches filenames ending with the specified extension.
     * By default, new <code>FilenameFilter</code> are case-insensitive.
     *
     * @param extension the extension to match
     */
    public ExtensionFilenameFilter(String extension) {
        this(new String[]{extension});
    }

    /**
     * Creates a new <code>ExtensionFilenameFilter</code> that matches filenames ending with one of the specified
     * extensions. By default, new <code>FilenameFilter</code> are case-insensitive.
     *
     * @param extensions the extensions to match
     */
    public ExtensionFilenameFilter(String extensions[]) {
        this.extensions = extensions;
    }


    ///////////////////////////////////
    // FilenameFilter implementation //
    ///////////////////////////////////

    public boolean accept(String filename) {
        boolean isCaseInsensitive = !isCaseSensitive();
        if(isCaseInsensitive)
            filename = filename.toLowerCase();

        int nbExtensions = extensions.length;
        String extension;
        for(int i=0; i<nbExtensions; i++) {
            extension = extensions[i];
            if(isCaseInsensitive)
                extension = extension.toLowerCase();

            if(filename.endsWith(extension))
                return true;
        }

        return false;
    }
}
