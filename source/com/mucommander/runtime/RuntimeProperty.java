/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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
 * This abstract class represents a property of the runtime environment. A runtime property has a finite number of
 * values -- each value having a corresponding <code>RuntimeProperty</code> instance, and only one. Subclasses must
 * ensure that only instance exists per runtime value. Based on this assumption, RuntimeProperty instances can be 
 * compared using the shallow equals operator '=='.
 *
 * <p>When running on a JVM, a RuntimeProperty has a runtime value (and only one), which can be retrieved using
 * {@link #getCurrentValue()}.</p>
 *
 * @author Maxence Bernard
 */
public abstract class RuntimeProperty {

    /** The String representation of this RuntimeProperty, set at creation time */
    protected final String stringRepresentation;

    /**
     * Creates a new RuntimeProperty using the specified String representation.
     *
     * @param stringRepresentation a String representation of this property
     */
    protected RuntimeProperty(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    /**
     * Returns <code>true</code> if this instance is the same instance as the one returned by {@link #getCurrentValue()}.
     *
     * @return true if this instance is the same as the current runtime's value
     */
    public boolean isCurrent() {
        return this==getCurrentValue();
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Returns the String representation passed to the constructor at creation time.
     *
     * @return the String representation passed to the constructor at creation time
     */
    public String toString() {
        return stringRepresentation;
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Returns the current runtime environment's value of this runtime property.
     *
     * @return the current runtime environment's value of this runtime property
     */
    protected abstract RuntimeProperty getCurrentValue();
}
