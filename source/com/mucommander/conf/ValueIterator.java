/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.conf;

import java.util.Iterator;

/**
 * Iterator with support for value casting.
 * <p>
 * Instances of this class can only be retrieved through {@link ValueList#valueIterator()}.
 * </p>
 * @author Nicolas Rinaudo
 */
public class ValueIterator implements Iterator {
    // - Instance fields -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Wrapped iterator. */
    private Iterator iterator;



    // - Initialisastion -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>ValueIterator</code> wrapping the specified <code>iterator</code>.
     * @param iterator iterator to wrap.
     */
    ValueIterator(Iterator iterator) {this.iterator = iterator;}



    // - Iterator implementation ---------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns <code>true</code> if the iteration has more elements.
     * (In other words, returns <code>true</code> if next would return an element rather than throwing an exception.)
     * @return <code>true</code> if the iteration has more elements.
     */
    public boolean hasNext() {return iterator.hasNext();}

    /**
     * Returns the next element in the iteration.
     * @return                        the next element in the iteration.
     * @throws NoSuchElementException if the iteration has no more elements.
     */
    public Object next() {return iterator.next();}

    /**
     * Throws an <code>UnsupportedOperationException</code>.
     */
    public void remove() {throw new UnsupportedOperationException();}



    // - Value casting -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the next value in the iterator as a string.
     * @return                        the next value in the iterator as a string.
     * @throws NoSuchElementException if the iteration has no more elements.
     */
    public String nextValue() {return iterator.next().toString();}

    /**
     * Returns the next value in the iterator as a integer.
     * @return                        the next value in the iterator as a integer.
     * @throws NoSuchElementException if the iteration has no more elements.
     * @throws NumberFormatException  if the value cannot be cast to an integer.
     */
    public int nextIntegerValue() {return ConfigurationSection.getIntegerValue(nextValue());}

    /**
     * Returns the next value in the iterator as a float.
     * @return                        the next value in the iterator as a float.
     * @throws NoSuchElementException if the iteration has no more elements.
     * @throws NumberFormatException  if the value cannot be cast to a float.
     */
    public float nextFloatValue() {return ConfigurationSection.getFloatValue(nextValue());}

    /**
     * Returns the next value in the iterator as a long.
     * @return                        the next value in the iterator as a long.
     * @throws NoSuchElementException if the iteration has no more elements.
     * @throws NumberFormatException  if the value cannot be cast to a long.
     */
    public long nextLongValue() {return ConfigurationSection.getLongValue(nextValue());}

    /**
     * Returns the next value in the iterator as a double.
     * @return                        the next value in the iterator as a double.
     * @throws NoSuchElementException if the iteration has no more elements.
     * @throws NumberFormatException  if the value cannot be cast to a double.
     */
    public double nextDoubleValue() {return ConfigurationSection.getDoubleValue(nextValue());}

    /**
     * Returns the next value in the iterator as a boolean.
     * @return                        the next value in the iterator as a boolean.
     * @throws NoSuchElementException if the iteration has no more elements.
     */
    public boolean nextBooleanValue() {return ConfigurationSection.getBooleanValue(nextValue());}

    /**
     * Returns the next value in the iterator as a {@link ValueList}.
     * @param  separator              stirng used to tokenise the next value.
     * @return                        the next value in the iterator as a {@link ValueList}.
     * @throws NoSuchElementException if the iteration has no more elements.
     */
    public ValueList nextListValue(String separator) {return ConfigurationSection.getListValue(nextValue(), separator);}
}
