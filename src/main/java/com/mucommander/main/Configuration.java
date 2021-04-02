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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public Map<String, String> serialize() {
        Map<String, String> map = new HashMap<>();
        map.put("mucommander.silent", Boolean.toString(silent));
        map.put("mucommander.assoc", assoc);
        map.put("mucommander.bookmark", bookmark);
        map.put("mucommander.configuration", configuration);
        map.put("mucommander.commandbar", commandbar);
        map.put("mucommander.extensions", extensions);
        map.put("mucommander.commands", commands);
        map.put("mucommander.fatalWarnings", Boolean.toString(fatalWarnings));
        map.put("mucommander.keymap", keymap);
        map.put("mucommander.preferences", preferences);
        map.put("mucommander.shellHistory", shellHistory);
        map.put("mucommander.toolbar", toolbar);
        map.put("mucommander.credentials", credentials);
        map.put("mucommander.folders", folders.stream().collect(Collectors.joining(",")));
        return map;
    }
}
