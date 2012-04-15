/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.ui.autocomplete.completers.services;

import java.util.Vector;

/**
 * A <code>PrefixFilter</code> matches strings that start with certain prefix.
 * 
 * @author Arik Hadas
 */

public class PrefixFilter {
	private String prefix;
	
	private PrefixFilter(String prefix) {
		this.prefix = prefix!=null ? prefix.toLowerCase() : null;
	}
	
	/**
	 * 
	 * @param prefix - The prefix that each string should start with in order to pass this filter.
	 * @return A filter of the given prefix.
	 */
	public static PrefixFilter createPrefixFilter(String prefix) {
		return new PrefixFilter(prefix);
	}
	
	/**
	 * 
	 * @param input - Some string.
	 * @return <code>true</code> if the given input was accepted by this filter, <code>false</code> otherwise.
	 */
	public boolean accept(String input) {
		return prefix == null || input.toLowerCase().startsWith(prefix);
	}
		
	/**
	 * Convenient method that filters out strings that do not start with this filter's prefix.
	 * 
	 * @param strings - Array of strings.
	 * @return Vector of strings which start with this filter's prefix.
	 */
	public Vector<String> filter(String[] strings) {
		Vector<String> result = new Vector<String>();
		int nbString = strings.length;
		for (int i=0; i<nbString; i++) {
			String stringI = strings[i];
			if (accept(stringI))
				result.add(stringI);
		}
		return result;
	}
	
	/**
	 * Convenient method that filters out strings that do not start with this filter's prefix.
	 * 
	 * @param strings - Vector of strings.
	 * @return Vector of strings which start with this filter's prefix.
	 */
	public Vector<String> filter(Vector<String> strings) {
		Vector<String> result = new Vector<String>();

        for(String s : strings) {
			if (accept(s))
				result.add(s);
		}		
		return result;
	}
}
