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
 * <code>AbstractFilenameFilter</code> implements the bulk of the {@link FilenameFilter} interface. The only method left
 * for subclasses to implement is {@link #accept(Object)}.
 *
 * @author Maxence Bernard
 */
public abstract class AbstractFilenameFilter extends AbstractStringCriterionFilter implements FilenameFilter {

    /**
     * Creates a new case-insensitive <code>AbstractFilenameFilter</code> operating in non-inverted mode.
     */
    public AbstractFilenameFilter() {
        this(false, false);
    }

    /**
     * Creates a new <code>AbstractFilenameFilter</code> operating in non-inverted mode.
     *
     * @param caseSensitive if true, this FilenameFilter will be case-sensitive
     */
    public AbstractFilenameFilter(boolean caseSensitive) {
        this(caseSensitive, false);
    }

    /**
     * Creates a new <code>AbstractFilenameFilter</code> operating in the specified mode.
     *
     * @param caseSensitive if true, this FilenameFilter will be case-sensitive
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public AbstractFilenameFilter(boolean caseSensitive, boolean inverted) {
        super(new FilenameGenerator(), caseSensitive, inverted);
    }
}