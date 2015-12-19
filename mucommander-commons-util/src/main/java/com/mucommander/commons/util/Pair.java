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

package com.mucommander.commons.util;

/**
 * Simple structure of two elements.
 * 
 * Note that this class does not support concurrent usage, and does not exposes setters and 
 * getters methods, on purpose in order to expose simple API similar to C++ pair structure.
 * 
 * @author Arik Hadas
 */
public class Pair<FIRST, SECOND> {

	/* first element */
	public FIRST first;
	/* second element */
	public SECOND second;
	
	/**
	 * Empty Constructor
	 * The elements should be assigned after the class instantiation
	 */
	public Pair() { }
	
	/**
	 * Constructor that take the two elements as parameters
	 * 
	 * @param first - first element
	 * @param second - second element
	 */
	public Pair(FIRST first, SECOND second) {
		this.first = first;
		this.second = second;
	}
}
