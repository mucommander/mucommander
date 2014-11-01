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
 * <code>AbstractPathFilter</code> implements the bulk of the {@link PathFilter} interface. The only method left
 * for subclasses to implement is {@link #accept(Object)}.
 *
 * @author Maxence Bernard
 */
public abstract class AbstractPathFilter extends AbstractStringCriterionFilter implements PathFilter {

    /**
     * Creates a new case-insensitive <code>AbstractPathFilter</code> operating in non-inverted mode.
     */
    public AbstractPathFilter() {
        this(false, false);
    }

    /**
     * Creates a new <code>AbstractPathFilter</code> operating in non-inverted mode.
     *
     * @param caseSensitive if true, this FilePathFilter will be case-sensitive
     */
    public AbstractPathFilter(boolean caseSensitive) {
        this(caseSensitive, false);
    }

    /**
     * Creates a new <code>AbstractPathFilter</code> operating in the specified mode.
     *
     * @param caseSensitive if true, this FilePathFilter will be case-sensitive
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public AbstractPathFilter(boolean caseSensitive, boolean inverted) {
        super(new PathGenerator(), caseSensitive, inverted);
    }
}