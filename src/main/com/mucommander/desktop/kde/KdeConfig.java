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

package com.mucommander.desktop.kde;

import com.mucommander.AppLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Provides access to the KDE configuration, using the <code>kreadconfig</code> command.
 *
 * @author Maxence Bernard
 */
public class KdeConfig {

    /** Name of the command to invoke for retrieving configuration values */
    private static String CONFIG_COMMAND = "kreadconfig";

    /**
     * Returns the KDE configuration value corresponding to the given key, <code>null</code> if this key has no value.
     *
     * @param key key to the configuration value to retrieve.
     * @return the configuration value corresponding to the given key, <code>null</code> if this key has no value.
     * @throws IOException if an error occurred while invoking the <code>kreadconfig</code> command, for instance if the
     * command isn't available in the path.
     */
    public static String getValue(String key) throws IOException {
        BufferedReader br = null;
        try {
            Process process = Runtime.getRuntime().exec(CONFIG_COMMAND+" --key "+key);

            br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = br.readLine();

            AppLogger.fine(CONFIG_COMMAND+" returned '"+line+"' for "+key);

            if(line==null || (line=line.trim()).equals(""))
                return null;

            return line;
        }
        catch(IOException e) {
            AppLogger.fine("Error while retrieving value for "+key, e);

            throw e;
        }
        finally {
            if(br!=null)
                try { br.close(); } catch(IOException e) {}
        }
    }
}
