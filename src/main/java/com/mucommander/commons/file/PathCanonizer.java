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


package com.mucommander.commons.file;

/**
 * PathCanonizer is an interface that defines a single {@link #canonize(String)} method that returns the canonical
 * representation of a given path. This interface is used by {@link SchemeParser} implementations to perform
 * scheme-specific path canonization, independently of the actual URL parsing.
 *
 * @see DefaultSchemeParser
 * @author Maxence Bernard
 */
public interface PathCanonizer {

    /**
     * Returns a canonical representation of the given path.
     *
     * @param path path to canonize
     * @return a canonical representation of the given path.
     */
    public String canonize(String path);
}
