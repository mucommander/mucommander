package com.mucommander.util;

import java.util.Iterator;
import java.util.Enumeration;
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
