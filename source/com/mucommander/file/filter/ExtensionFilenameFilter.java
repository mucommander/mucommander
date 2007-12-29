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

import com.mucommander.util.StringUtils;

/**
 * This {@link FilenameFilter} matches filenames ending with one of several specified extensions.
 * The filter can be made case-sensitive or case-insensitive, this behavior is specified at creation time.
 *
 * <p>The extension(s) may be any string, but when used in the traditional sense of a file extension (e.g. zip extension)
 * the '.' character must be included in the specified extension (e.g. ".zip" must be used, not just "zip").</p>
 * 
 * @author Maxence Bernard, Nicolas Rinaudo
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
        int i;

        // If case isn't important, a simple String.endsWith is enough.
        if(isCaseSensitive()) {
            for(i = 0; i < extensions.length; i++)
                if(filename.endsWith(extensions[i]))
                    return true;
        }

        // If case is important, we have to be a bit more creative and
        // use String.regionMatches.
        else {
            int nameLength; // Filename's length.

            // Matches the file name to each extension.
            nameLength = filename.length();
            for(i = 0; i < extensions.length; i++)
                if(StringUtils.endsWithIgnoreCase(filename, extensions[i], nameLength))
                    return true;
        }
        return false; 
    }
}
