/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 * This <code>CompletionService</code> handles system variables completion.
 * 
 * @author Arik Hadas
 */

public class SystemVariablesService implements CompletionService {
	private String[] cachedKeyNames;
	
	public SystemVariablesService() {
		Set<String> keys = System.getenv().keySet();
		int nbKeys = keys.size();
		cachedKeyNames = new String[nbKeys];
		Iterator<String> iter = keys.iterator();
		for (int i=0; i<nbKeys; i++)
			cachedKeyNames[i] = "$" + iter.next();
		Arrays.sort(cachedKeyNames, String.CASE_INSENSITIVE_ORDER);		
	}

	public Vector<String> getPossibleCompletions(String path) {
		return PrefixFilter.createPrefixFilter(path).filter(cachedKeyNames);
	}

	public String complete(String selectedCompletion) {
		String result = null;
		int nbKeyNames = cachedKeyNames.length;
		for (int i=0; i<nbKeyNames; i++)
			if (cachedKeyNames[i].equalsIgnoreCase(selectedCompletion)) {
				result = cachedKeyNames[i];
				break;
			}
		return result;
	}
}
