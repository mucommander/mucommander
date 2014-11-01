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
 * This filter matches files whose string criterion values contain a specified string.
 *
 * @author Maxence Bernard
 */
public class AbstractContainsFilter extends AbstractStringCriterionFilter {

    /** The string to look for in criterion values */
    private String s;

    /**
     * Creates a new <code>AbstractContainsFilter</code> using the specified generator and string, and operating in the
     * specified mode.
     *
     * @param generator generates criterion values for files as requested
     * @param s the string to compare criterion values against
     * @param caseSensitive if true, this filter will be case-sensitive
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public AbstractContainsFilter(CriterionValueGenerator<String> generator, String s, boolean caseSensitive, boolean inverted) {
        super(generator, caseSensitive, inverted);
        this.s = s;
    }


    //////////////////////////////////////////////////
    // AbstractStringCriterionFilter implementation //
    //////////////////////////////////////////////////

    public boolean accept(String value) {
        if(isCaseSensitive())
            return value.indexOf(s)!=-1;

        return value.toLowerCase().indexOf(s.toLowerCase())!=-1;
    }
}
