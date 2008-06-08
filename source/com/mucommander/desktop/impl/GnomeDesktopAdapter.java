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

package com.mucommander.desktop.impl;

import com.mucommander.command.Command;
import com.mucommander.command.CommandException;
import com.mucommander.command.CommandManager;
import com.mucommander.command.PermissionsFileFilter;
import com.mucommander.desktop.DefaultDesktopAdapter;
import com.mucommander.desktop.DesktopInitialisationException;
import com.mucommander.file.PermissionTypes;
import com.mucommander.file.filter.FileFilter;
import com.mucommander.file.filter.RegexpFilenameFilter;
import com.mucommander.runtime.JavaVersion;

/**
 * @author Nicolas Rinaudo
 */
abstract class GnomeDesktopAdapter extends DefaultDesktopAdapter {
    private static final String FILE_MANAGER_NAME = "Nautilus";
    private static final String FILE_OPENER       = "gnome-open $f";
    private static final String EXE_OPENER        = "$f";

    public abstract boolean isAvailable();

    public void init(boolean install) throws DesktopInitialisationException {
        try {
            CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_OPENER_ALIAS,  FILE_OPENER, Command.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.URL_OPENER_ALIAS,   FILE_OPENER, Command.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.EXE_OPENER_ALIAS,   EXE_OPENER,  Command.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_MANAGER_ALIAS, FILE_OPENER, Command.SYSTEM_COMMAND, FILE_MANAGER_NAME));

            FileFilter filter;
            // Disabled actual permissions checking as this will break normal +x files.
            // With this, a +x PDF file will not be opened.
            /*
            // Identifies which kind of filter should be used to match executable files.
            if(JavaVersion.JAVA_1_6.isCurrentOrHigher())
                filter = new PermissionsFileFilter(PermissionTypes.EXECUTE_PERMISSION, true);
            else
            */
                filter = new RegexpFilenameFilter("[^.]+", true);

            CommandManager.registerDefaultAssociation(CommandManager.EXE_OPENER_ALIAS, filter);
        }
        catch(CommandException e) {throw new DesktopInitialisationException(e);}
    }
}
