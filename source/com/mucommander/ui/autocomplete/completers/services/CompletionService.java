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
 * CompletionService is used to handle completions according to certain criteria.
 * It defines 2 methods:
 * * <ul>
 *   <li>getPossibleCompletions - return possible completions for a given path</li>
 *   <li>complete - return a path corresponding to a given completion</li>
 * </ul>
 * 
 * @author Arik Hadas
 */

public interface CompletionService {
	
	/**
	 * Return a group of suggested completions corresponding to the given path, 
	 * according to this service's criteria.
	 * 
	 * @param path - a path.
	 * @return a Vector of possible completions.
	 */
	public Vector<String> getPossibleCompletions(String path);
	
	/**
	 *  If the given completion match one of my suggested completions, return 
	 *  a corresponding path, null otherwise.
	 *   
	 * @param selectedCompletion - string that represent a completion.
	 * @return a path if the given completion was suggested by this service, null otherwise. 
	 */
	public String complete(String selectedCompletion);
}
