package com.mucommander.ui.autocomplete;

import java.util.Vector;

/**
 * A <code>PrefixFilter</code> matches strings that start with certain prefix.
 * 
 * @author Arik Hadas
 */

public class PrefixFilter {
	private String prefix;
	
	private PrefixFilter(String prefix) {
		this.prefix = prefix;
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
		return prefix!=null ? input.toLowerCase().startsWith(prefix) : true;
	}
		
	/**
	 * Convenient method that filters out strings that do not start with this filter's prefix.
	 * 
	 * @param strings - Array of strings.
	 * @return Vector of strings which start with this filter's prefix.
	 */
	public Vector filter(String[] strings) {
		Vector result = new Vector();
		int nbString = strings.length;
		for (int i=0; i<nbString; i++) {
			String stringI = strings[i];
			if (accept(stringI))
				result.add(stringI);
		}
		return result;
	}
}
