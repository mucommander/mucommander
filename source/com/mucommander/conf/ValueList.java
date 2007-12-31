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
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Provides support for variables whose value is a list of tokens.
 * <p>
 * Such values will simply be split using a <code>StringTokenizer</code> and stored as a <code>java.util.List</code>.
 * </p>
 * <p>
 * In addition to the regular <code>List</code> methods, this class provides the same value casting mechanisms as
 * {@link Configuration} and {@link ConfigurationEvent}. These have been extended to iterators through the
 * {@link #valueIterator()} method.
 * </p>
 * @author Nicolas Rinaudo
 */
public class ValueList extends Vector {
    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>ValueList</code> initialised with the specified data.
     * @param data      data contained by the list.
     * @param separator string used to separate <code>data</code> in tokens.
     */
    ValueList(String data, String separator) {
        StringTokenizer tokenizer;

        tokenizer = new StringTokenizer(data, separator);
        while(tokenizer.hasMoreTokens())
            add(tokenizer.nextToken());
    }


    // - Value casting -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the value found at the specified index of the list as a string.
     * @param  index index of the value to retrieve.
     * @return       the value found at the specified index of the list as a string.
     */
    public String valueAt(int index) {return elementAt(index).toString();}

    /**
     * Returns the value found at the specified index of the list as an integer.
     * @param  index                 index of the value to retrieve.
     * @return                       the value found at the specified index of the list as an integer.
     * @throws NumberFormatException if the value cannot be cast to an integer.
     */
    public int integerValueAt(int index) {return ConfigurationSection.getIntegerValue(valueAt(index));}

    /**
     * Returns the value found at the specified index of the list as a float.
     * @param  index                 index of the value to retrieve.
     * @return                       the value found at the specified index of the list as a float.
     * @throws NumberFormatException if the value cannot be cast to a float.
     */
    public float floatValueAt(int index) {return ConfigurationSection.getFloatValue(valueAt(index));}

    /**
     * Returns the value found at the specified index of the list as a double.
     * @param  index                 index of the value to retrieve.
     * @return                       the value found at the specified index of the list as a double.
     * @throws NumberFormatException if the value cannot be cast to a double.
     */
    public double doubleValueAt(int index) {return ConfigurationSection.getDoubleValue(valueAt(index));}

    /**
     * Returns the value found at the specified index of the list as a long.
     * @param  index                 index of the value to retrieve.
     * @return                       the value found at the specified index of the list as a long.
     * @throws NumberFormatException if the value cannot be cast to a long.
     */
    public long longValueAt(int index) {return ConfigurationSection.getLongValue(valueAt(index));}

    /**
     * Returns the value found at the specified index of the list as a boolean.
     * @param  index index of the value to retrieve.
     * @return       the value found at the specified index of the list as a boolean.
     */
    public boolean booleanValueAt(int index) {return ConfigurationSection.getBooleanValue(valueAt(index));}

    /**
     * Returns the value found at the specified index of the list as a {@link ValueList}.
     * @param  index     index of the value to retrieve.
     * @param  separator string used to split the value into tokens.
     * @return           the value found at the specified index of the list as a {@link ValueList}.
     */
    public ValueList listValueAt(int index, String separator) {return ConfigurationSection.getListValue(valueAt(index), separator);}

    /**
     * Returns a {@link ValueIterator} on the list.
     * @return a {@link ValueIterator} on the list.
     */
    public ValueIterator valueIterator() {return new ValueIterator(iterator());}



    // - Helper methods ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns a string representation of the specified list.
     * @param  data      values to represent as a string.
     * @param  separator string used to separate one element from the other.
     * @return           a string representation of the specified list.
     */
    public static String toString(List data, String separator) {
        StringBuffer buffer;
        Iterator     values;

        buffer = new StringBuffer();
        values = data.iterator();

        // Deals with the first value separately.
        if(values.hasNext())
            buffer.append(values.next().toString());

        // All subsequent values will be concatenated after a separator.
        while(values.hasNext()) {
            buffer.append(separator);
            buffer.append(values.next().toString());
        }

        // Returns the final value.
        return buffer.toString();
    }
}
