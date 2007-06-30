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

package com.mucommander.conf;

/**
 * Holds the name and value of a configuration tree leaf.
 * <p>
 * Note that this class should never be accessed directly, except on very specific
 * exceptions. It was made public in order to allow configuration tree writers to use an iterator
 * on leafs instead of the slow sequential access(eg the infamous vector exploration loop).
 * </p>
 * @author Nicolas Rinaudo
 */
class ConfigurationLeaf {

    /** Leaf's name. */
    private String name;
    /** Leaf's value. */
    private String value;

    /* ----------------------- */
    /*      Initilisation      */
    /* ----------------------- */
    /**
     * Builds a new configuration leaf with the specified name and value.
     * @param name  leaf's name.
     * @param value leaf's value.
     */
    public ConfigurationLeaf(String name, String value) {
        setName(name);
        setValue(value);
    }

    /* ----------------------- */
    /*       Name access       */
    /* ----------------------- */
    /**
     * Returns the leaf's name.
     * @return the leaf's name.
     */
    public String getName() {return name;}

    /**
     * Sets the leaf's name.
     * @param name leaf's name.
     */
    public void setName(String name) {this.name = name;}

    /* ----------------------- */
    /*       Value access      */
    /* ----------------------- */
    /**
     * Returns the leaf's value.
     * @return the leaf's value.
     */
    public String getValue() {return value;}

    /**
     * Sets the leaf's value.
     * @param value leaf's value.
     */
    public void setValue(String value) {this.value = value;}
}
