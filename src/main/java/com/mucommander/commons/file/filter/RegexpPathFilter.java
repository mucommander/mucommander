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

import java.util.regex.PatternSyntaxException;

/**
 * This {@link PathFilter} that accepts or rejects files whose path match a specific regular expression.
 *
 * @author Maxence Bernard
 */
public class RegexpPathFilter extends AbstractRegexpFilter implements PathFilter {

    /**
     * Creates a new <code>RegexpPathFilter</code> matching the specified regexp and operating in non-inverted
     * mode.
     *
     * @param regexp regular expression that matches string values.
     * @param caseSensitive whether the regular expression is case sensitive or not.
     * @throws PatternSyntaxException if the syntax of the regular expression is not correct.
     */
    public RegexpPathFilter(String regexp, boolean caseSensitive) throws PatternSyntaxException {
        super(new PathGenerator(), regexp, caseSensitive, false);
    }

    /**
     * Creates a new <code>RegexpPathFilter</code> matching the specified regexp and operating in the specified
     * modes.
     *
     * @param regexp regular expression that matches string values.
     * @param caseSensitive whether the regular expression is case sensitive or not.
     * @param inverted if true, this filter will operate in inverted mode.
     * @throws PatternSyntaxException if the syntax of the regular expression is not correct.
     */
    public RegexpPathFilter(String regexp, boolean caseSensitive, boolean inverted) throws PatternSyntaxException {
        super(new PathGenerator(), regexp, caseSensitive, inverted);
    }
}
