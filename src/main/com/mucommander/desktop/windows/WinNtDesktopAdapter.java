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

package com.mucommander.desktop.windows;

import com.mucommander.command.Command;
import com.mucommander.command.CommandException;
import com.mucommander.command.CommandManager;
import com.mucommander.command.CommandType;
import com.mucommander.commons.file.filter.RegexpFilenameFilter;
import com.mucommander.commons.runtime.OsVersion;
import com.mucommander.desktop.DesktopInitialisationException;

/**
 * @author Nicolas Rinaudo
 */
public class WinNtDesktopAdapter extends WindowsDesktopAdapter {
    private static final String FILE_OPENER_COMMAND = "cmd /c start \"\" \"$f\"";
    private static final String EXE_OPENER_COMMAND  = "cmd /c $f";
    private static final String EXE_REGEXP          = ".*\\.exe";

    public String toString() {return "Windows NT+ Desktop";}

    @Override
    public boolean isAvailable() {return super.isAvailable() && OsVersion.getCurrent().compareTo(OsVersion.WINDOWS_NT) >= 0;}

    @Override
    public void init(boolean install) throws DesktopInitialisationException {
        super.init(install);
        try {
            CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_OPENER_ALIAS,  FILE_OPENER_COMMAND, CommandType.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.URL_OPENER_ALIAS,   FILE_OPENER_COMMAND, CommandType.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_MANAGER_ALIAS, FILE_OPENER_COMMAND, CommandType.SYSTEM_COMMAND, EXPLORER_NAME));
            CommandManager.registerDefaultCommand(new Command(CommandManager.EXE_OPENER_ALIAS,   EXE_OPENER_COMMAND,  CommandType.SYSTEM_COMMAND, null));

            CommandManager.registerDefaultAssociation(CommandManager.EXE_OPENER_ALIAS, new RegexpFilenameFilter(EXE_REGEXP, false));
        }
        catch(CommandException e) {throw new DesktopInitialisationException(e);}
    }

    @Override
    public String getDefaultShell() {return "cmd /c";}
}
