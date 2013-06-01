/*
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

package com.mucommander.commons.runtime;

/**
 * Utility methods for comparing the current runtime's value of this property to this instance
 *
 * @author Arik Hadas, Maxence Bernard
*/
public interface ComparableRuntimeProperty {

    /**
     * Returns <code>true</code> if the current runtime's value of this property is equal or lower to this instance,
     * according to {@link #compareTo(Object)}.
     *
     * @return <code>true</code> if the current runtime's value of this property is equal or lower to this instance
     */
    boolean isCurrentOrLower();

    /**
     * Returns <code>true</code> if the current runtime's value of this property is lower than this instance,
     * according to {@link #compareTo(Object)}.
     *
     * @return <code>true</code> if the current runtime's value of this property is lower than this instance
     */
    boolean isCurrentLower();

    /**
     * Returns <code>true</code> if the current runtime's value of this property is equal or higher to this instance,
     * according to {@link #compareTo(Object)}.
     *
     * @return <code>true</code> if the current runtime's value of this property is equal or higher to this instance
     */
    boolean isCurrentOrHigher();

    /**
     * Returns <code>true</code> if the current runtime's value of this property is higher than this instance,
     * according to {@link #compareTo(Object)}.
     *
     * @return <code>true</code> if the current runtime's value of this property is higher than this instance
     */
    boolean isCurrentHigher();
}
