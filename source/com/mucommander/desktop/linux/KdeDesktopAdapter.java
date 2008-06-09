/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.desktop.linux;

import com.mucommander.command.Command;
import com.mucommander.command.CommandException;
import com.mucommander.command.CommandManager;
import com.mucommander.desktop.DefaultDesktopAdapter;
import com.mucommander.desktop.DesktopInitialisationException;

/**
 * @author Nicolas Rinaudo
 */
abstract class KdeDesktopAdapter extends DefaultDesktopAdapter {
    private static final String FILE_MANAGER_NAME = "Konqueror";
    private static final String FILE_OPENER       = "kfmclient exec $f";
    private static final String URL_OPENER        = "kfmclient openURL $f";

    public abstract boolean isAvailable();

    public void init(boolean install) throws DesktopInitialisationException {
        // Initialises trash management.
        com.mucommander.file.FileFactory.setTrashProvider(new KDETrashProvider());

        // Registers KDE specific commands.
        try {
            CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_OPENER_ALIAS,  FILE_OPENER, Command.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.URL_OPENER_ALIAS,   URL_OPENER,  Command.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_MANAGER_ALIAS, FILE_OPENER, Command.SYSTEM_COMMAND, FILE_MANAGER_NAME));
        }
        catch(CommandException e) {throw new DesktopInitialisationException(e);}
    }
}
