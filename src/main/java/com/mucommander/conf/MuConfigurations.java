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
import java.io.IOException;

import com.mucommander.commons.conf.Configuration;
import com.mucommander.commons.conf.ConfigurationException;
import com.mucommander.commons.conf.ConfigurationListener;

/**
 * This class contains the configurations of muCommander and exposes their API methods.
 * It provides global access to the configurations without using singletons.
 * 
 * @author Arik Hadas
 */
public class MuConfigurations {

	/** Static configurations of muConnabder */
	private static final MuPreferences preferences = new MuPreferences();
	/** Dynamic configurations of muCommander */
	private static final MuSnapshot snapshot = new MuSnapshot();
	
	/////////////////////////
	// API for preferences //
	/////////////////////////
	
	public static MuPreferencesAPI getPreferences() {
		return preferences;
	}
	
	public static void loadPreferences() throws IOException, ConfigurationException {
		preferences.read();
	}
	
	public static void savePreferences() throws IOException, ConfigurationException {
		preferences.write();
	}
	
	public static void setPreferencesFile(String path) throws FileNotFoundException {
		preferences.setConfigurationFile(path);
	}
	
	public static boolean isPreferencesFileExists() throws IOException {
		return preferences.isFileExists();
	}
	
    public static void addPreferencesListener(ConfigurationListener listener) {
    	preferences.addConfigurationListener(listener);
    }

    public static void removePreferencesListener(ConfigurationListener listener) {
    	preferences.removeConfigurationListener(listener);
    }

    //////////////////////
    // API for snapshot //
    //////////////////////
    
    public static Configuration getSnapshot() {
    	return snapshot.getConfiguration();
    }
    
    public static void loadSnapshot() throws IOException, ConfigurationException {
    	snapshot.read();
    }
    
    public static void saveSnapshot() throws IOException, ConfigurationException {
    	snapshot.write();
    }
}
