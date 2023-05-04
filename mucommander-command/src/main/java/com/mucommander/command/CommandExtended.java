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
package com.mucommander.command;

import javax.swing.Icon;

/**
 * Extended version of Command that also keeps optional icon.
 */
public class CommandExtended extends Command {

    private final Icon icon;

    public CommandExtended(String alias, String command, CommandType type, String displayName, Icon icon ) {
        super(alias, command, type, displayName);
        this.icon = icon;
    }

    /**
     * Returns an icon associated with this command.
     * @return an icon, or null;
     */
    public Icon getIcon() {
        return icon;
    }
}
