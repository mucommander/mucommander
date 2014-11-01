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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This {@link AbstractStringCriterionFilter} accept or reject files whose string criterion values match a specific
 * regular expression.
 *
 * @author Nicolas Rinaudo, Maxence Bernard
 */
public abstract class AbstractRegexpFilter extends AbstractStringCriterionFilter {

    /** Pattern against which criteria values will be compared. */
    private Pattern pattern;

    /**
     * Creates a new <code>AbstractRegexpFilter</code> matching the specified regexp and operating in the specified
     * modes.
     *
     * @param generator generates criterion values for files as requested
     * @param regexp regular expression that matches string values.
     * @param caseSensitive whether the regular expression is case sensitive or not.
     * @param inverted if true, this filter will operate in inverted mode.
     * @throws PatternSyntaxException if the syntax of the regular expression is not correct.
     */
    public AbstractRegexpFilter(CriterionValueGenerator<String> generator, String regexp, boolean caseSensitive, boolean inverted) throws PatternSyntaxException {
        super(generator, caseSensitive, inverted);

        pattern = Pattern.compile(regexp, caseSensitive?0:Pattern.CASE_INSENSITIVE);
    }

    /**
     * Returns the regular expression used by this filter.
     *
     * @return the regular expression used by this filter.
     */
    public String getRegularExpression() {
        return pattern.pattern();
    }


    ////////////////////////////////////
    // CriterionFilter implementation //
    ////////////////////////////////////

    /**
     * Returns <code>true</code> if the specified value matches the filter's regular expression.
     *
     * @param value value to match against the filter's regular expression.
     * @return <code>true</code> if the specified value matches the filter's regular expression,
     * <code>false</code> otherwise.
     */
    public boolean accept(String value) {
        return pattern.matcher(value).matches();
    }
}
