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

package com.mucommander.conf;

import java.io.FileNotFoundException;

/**
 * 
 * @author Arik Hadas
 */
class MuPreferencesFile extends MuConfigurationFile {
	
	private static final String DEFAULT_PREFERENCES_FILE_NAME = "preferences.xml";
	
	public static MuPreferencesFile getPreferencesFile(String path) throws FileNotFoundException {
		return new MuPreferencesFile(path);
	}
	
	public static MuPreferencesFile getPreferencesFile() {
		try {
			return new MuPreferencesFile(null);
		} catch (FileNotFoundException e) {
			// Not possible exception
			return null;
		}
	}
	
	private MuPreferencesFile(String path) throws FileNotFoundException {
		super(path, DEFAULT_PREFERENCES_FILE_NAME);
	}
}
