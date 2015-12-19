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


package com.mucommander.commons.file.filter;

/**
 * This {@link FilenameFilter} matches filenames that start with a specified string.
 *
 * @author Maxence Bernard
 */
public class StartsWithFilenameFilter extends AbstractStartsWithFilter implements FilenameFilter {

    /**
     * Creates a new case-insensitive <code>StartsFilenameFilter</code> matching filenames starting with the specified
     * string and operating in the specified mode.
     *
     * @param s the string to compare filenames against
     */
    public StartsWithFilenameFilter(String s) {
        this(s, false, false);
    }

    /**
     * Creates a new <code>StartsFilenameFilter</code> matching filenames starting with the specified string and
     * operating in non-inverted mode.
     *
     * @param s the string to compare filenames against
     * @param caseSensitive if true, this FilenameFilter will be case-sensitive
     */
    public StartsWithFilenameFilter(String s, boolean caseSensitive) {
        this(s, caseSensitive, false);
    }

    /**
     * Creates a new <code>StartsFilenameFilter</code> matching filenames starting with the specified string and
     * operating in the specified modes.
     *
     * @param s the string to compare filenames against
     * @param caseSensitive if true, this FilenameFilter will be case-sensitive
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public StartsWithFilenameFilter(String s, boolean caseSensitive, boolean inverted) {
        super(new FilenameGenerator(), s, caseSensitive, inverted);
    }
}
