/*
 * This file is part of muCommander, http://www.mucommander.com
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

package com.mucommander.main;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.beust.jcommander.Parameter;

/**
 * @author Arik Hadas
 */
public class Configuration {

    /** Whether or not to display verbose error messages. */
    @Parameter(names={"-S", "--silent"}, description="Do not print verbose error messages")
    public boolean silent;
    @Parameter(names={"-v", "--version"}, description="Print the version and exit", help=true)
    public boolean version;
    @Parameter(names={"-h", "--help"}, description="Print the help text and exit", help=true)
    public boolean help;
    // Allows users to tweak how file associations are loaded / saved.
    @Parameter(names={"-a", "--assoc"}, description="Load associations from FILE.")
    public String assoc;
    // Allows users to tweak how bookmarks are loaded / saved.
    @Parameter(names={"-b", "--bookmarks"}, description="Load bookmarks from FILE.")
    public String bookmark;
    // Allows users to tweak how configuration is loaded / saved.
    @Parameter(names={"-c", "--configuration"}, description="Load configuration from FILE")
    public String configuration;
    // Allows users to tweak how command bar configuration is loaded / saved.
    @Parameter(names={"-C", "--commandbar"}, description="Load command bar from FILE.")
    public String commandbar;
    // Allows users to change the extensions folder.
    @Parameter(names={"-e", "--extensions"}, description="Load extensions from FOLDER.")
    public String extensions;
    // Allows users to tweak how custom commands are loaded / saved.
    @Parameter(names={"-f", "--commands"}, description="Load custom commands from FILE.")
    public String commands;
    @Parameter(names={"-w", "--fail-on-warnings"}, description="Quits when a warning is encountered during")
    public boolean fatalWarnings;
    // Allows users to tweak how keymaps are loaded.
    @Parameter(names={"-k", "--keymap"}, description="Load keymap from FILE")
    public String keymap;
    // Allows users to change the preferences folder.
    @Parameter(names={"-p", "--preferences"}, description="Store configuration files in FOLDER")
    public String preferences;
    // Allows users to tweak how shell history is loaded / saved.
    @Parameter(names={"-s", "--shell-history"}, description="Load shell history from FILE")
    public String shellHistory;
    // Allows users to tweak how toolbar configuration are loaded.
    @Parameter(names={"-t", "--toolbar"}, description="Load toolbar from FILE")
    public String toolbar;
    // Allows users to tweak how credentials are loaded.
    @Parameter(names={"-u", "--credentials"}, description="Load credentials from FILE")
    public String credentials;
    @Parameter(description="[folders]")
    public List<String> folders = new ArrayList<>();

    public Set<Entry<String, String>> entrySet() {
        Set<Entry<String, String>> set = new LinkedHashSet<>();
        set.add(toEntry("mucommander.silent", Boolean.toString(silent)));
        set.add(toEntry("mucommander.assoc", assoc));
        set.add(toEntry("mucommander.bookmark", bookmark));
        set.add(toEntry("mucommander.configuration", configuration));
        set.add(toEntry("mucommander.commandbar", commandbar));
        set.add(toEntry("mucommander.extensions", extensions));
        set.add(toEntry("mucommander.commands", commands));
        set.add(toEntry("mucommander.fatalWarnings", Boolean.toString(fatalWarnings)));
        set.add(toEntry("mucommander.keymap", keymap));
        set.add(toEntry("mucommander.preferences", preferences));
        set.add(toEntry("mucommander.shellHistory", shellHistory));
        set.add(toEntry("mucommander.toolbar", toolbar));
        set.add(toEntry("mucommander.credentials", credentials));
        set.add(toEntry("mucommander.folders", String.join(",", folders)));
        return set;
    }

    /**
     * Can be replaced with java.util.KeyValueHolder once we upgrade to Java 9
     */
    private Entry<String, String> toEntry(String key, String value) {
        return new Entry<String, String>() {
            @Override
            public String getValue() {
                return value;
            }

            @Override
            public String getKey() {
                return key;
            }

            @Override
            public String setValue(String value) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
