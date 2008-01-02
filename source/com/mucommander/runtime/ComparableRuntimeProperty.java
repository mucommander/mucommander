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

package com.mucommander.runtime;

/**
 * @author Maxence Bernard
*/
public abstract class ComparableRuntimeProperty extends RuntimeProperty implements Comparable {

    private int propertyInt;

    protected ComparableRuntimeProperty(String propertyString, int propertyInt) {
        super(propertyString);

        this.propertyInt = propertyInt;
    }

    public boolean isCurrentOrLower() {
        return ((ComparableRuntimeProperty)getCurrentValue()).compareTo(this)<=0;
    }

    public boolean isCurrentLower() {
        return ((ComparableRuntimeProperty)getCurrentValue()).compareTo(this)<0;
    }

    public boolean isCurrentOrHigher() {
        return ((ComparableRuntimeProperty)getCurrentValue()).compareTo(this)>=0;
    }

    public boolean isCurrentHigher() {
        return ((ComparableRuntimeProperty)getCurrentValue()).compareTo(this)>0;
    }

    ///////////////////////////////
    // Comparable implementation //
    ///////////////////////////////

    public int compareTo(Object o) {
        if(!(o instanceof ComparableRuntimeProperty))
            throw new IllegalArgumentException();

        return propertyInt - ((ComparableRuntimeProperty)o).propertyInt;
    }
}
