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
 * ComparableRuntimeProperty is a runtime property for which the values have a natural order. This class provides
 * methods to compare instances, using an int value specified at creation time as the discriminator.
 *
 * <p>The subclass must ensure that no two instances have the same discriminator value. Based on this assumption,
 * if {@link #compareTo(Object)} returns <code>0</code>, then instances are equal according to the shallow equals
 * operator '=='.</p>
 *
 * @author Maxence Bernard
*/
public abstract class ComparableRuntimeProperty extends RuntimeProperty implements Comparable {

    /** The descriminator set at creation time */
    protected final int discriminator;

    /**
     * Creates a new RuntimeProperty using the specified String representation and order discriminator.
     *
     * @param stringRepresentation a String representation of this property
     * @param discriminator the descriminator that serves to compare an instance with another against
     */
    protected ComparableRuntimeProperty(String stringRepresentation, int discriminator) {
        super(stringRepresentation);

        this.discriminator = discriminator;
    }

    /**
     * Returns <code>true</code> if the current runtime's value of this property is equal or lower to this instance,
     * according to {@link #compareTo(Object)}.
     *
     * @return <code>true</code> if the current runtime's value of this property is equal or lower to this instance
     */
    public boolean isCurrentOrLower() {
        return ((ComparableRuntimeProperty)getCurrentValue()).compareTo(this)<=0;
    }

    /**
     * Returns <code>true</code> if the current runtime's value of this property is lower than this instance,
     * according to {@link #compareTo(Object)}.
     *
     * @return <code>true</code> if the current runtime's value of this property is lower than this instance
     */
    public boolean isCurrentLower() {
        return ((ComparableRuntimeProperty)getCurrentValue()).compareTo(this)<0;
    }

    /**
     * Returns <code>true</code> if the current runtime's value of this property is equal or higher to this instance,
     * according to {@link #compareTo(Object)}.
     *
     * @return <code>true</code> if the current runtime's value of this property is equal or higher to this instance
     */
    public boolean isCurrentOrHigher() {
        return ((ComparableRuntimeProperty)getCurrentValue()).compareTo(this)>=0;
    }

    /**
     * Returns <code>true</code> if the current runtime's value of this property is higher than this instance,
     * according to {@link #compareTo(Object)}.
     *
     * @return <code>true</code> if the current runtime's value of this property is higher than this instance
     */
    public boolean isCurrentHigher() {
        return ((ComparableRuntimeProperty)getCurrentValue()).compareTo(this)>0;
    }


    ///////////////////////////////
    // Comparable implementation //
    ///////////////////////////////

    public int compareTo(Object o) {
        if(!(o instanceof ComparableRuntimeProperty))
            throw new IllegalArgumentException();

        return discriminator - ((ComparableRuntimeProperty)o).discriminator;
    }
}
