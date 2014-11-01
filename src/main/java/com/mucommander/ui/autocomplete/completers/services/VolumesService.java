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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.impl.local.LocalFile;

import java.util.Arrays;
import java.util.Vector;

/**
 * This <code>CompletionService</code> handles root folders completion.
 * 
 * @author Arik Hadas
 */

public class VolumesService implements CompletionService {
	private Vector<String> lastSuggestedCompletions = new Vector<String>();
	
	public VolumesService() {}

    /**
     * Resolves and returns a sorted array of root (top level) folder names. Those folders are purposively not cached
     * so that newly mounted folders will be returned.
     *
     * @return a sorted array of root folder names
     */
	public Vector<String> getPossibleCompletions(String path) {
		lastSuggestedCompletions.clear();
		int index = Math.max(path.lastIndexOf('\\'), path.lastIndexOf('/'));
		if (index == -1) {
			AbstractFile[] fileRoots = LocalFile.getVolumes();
	    	int nbFolders = fileRoots.length;
	    	String[] rootFolderNames = new String[nbFolders];
	    	for (int i=0; i<nbFolders; i++)
	    		rootFolderNames[i] = fileRoots[i].getAbsolutePath();
	    	Arrays.sort(rootFolderNames, String.CASE_INSENSITIVE_ORDER);
	    	lastSuggestedCompletions = PrefixFilter.createPrefixFilter(path).filter(rootFolderNames);
		}
		return lastSuggestedCompletions;
	}

	public String complete(String selectedCompletion) {
		String result = null;
		int nbLastReturnedCompletions = lastSuggestedCompletions.size();
		for (int i=0; i < nbLastReturnedCompletions; i++)
			if (lastSuggestedCompletions.elementAt(i).equalsIgnoreCase(selectedCompletion)) {
				result = lastSuggestedCompletions.elementAt(i);
				break;
			}
		
		return result;
	}
}
