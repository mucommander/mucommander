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

package com.mucommander.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Converts an <code>Enumeration</code> into an <code>Iterator</code>.
 * @author Nicolas Rinaudo
 */
public class Enumerator implements Iterator {
    // - Instance variables ----------------------------------------------------
    // -------------------------------------------------------------------------
    /** Enumeration wrapper by this <code>Enumerator</code>. */
    private Enumeration enumeration;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Creates a new enumerator from the specified enumeration.
     * @param e enumeration that needs to be treated as an iterator.
     */
    public Enumerator(Enumeration e) {enumeration = e;}



    // - Iterator methods ------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns <code>true</code> if the iterator has more elements.
     * (In other words, returns <code>true</code> if {@link #next() next} would return an element rather than throwing an exception.)
     * @return <code>true</code> if the iterator has more elements, <code>false</code> otherwise.
     */
    public boolean hasNext() {return enumeration.hasMoreElements();}

    /**
     * Returns the next element in the iteration.
     * @return                        the next element in the iteration.
     * @throws NoSuchElementException if there is no next element in the iteration.
     */
    public Object next() throws NoSuchElementException {return enumeration.nextElement();}

    /**
     * Operation not supported.
     * @throws UnsupportedOperationException whenever this method is called.
     */
    public void remove() {throw new UnsupportedOperationException();}
}
