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

package com.mucommander.desktop.windows;

import com.mucommander.command.Command;
import com.mucommander.command.CommandException;
import com.mucommander.command.CommandManager;
import com.mucommander.command.CommandType;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.filter.RegexpFilenameFilter;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.desktop.DefaultDesktopAdapter;
import com.mucommander.desktop.DesktopInitialisationException;
import com.mucommander.desktop.TrashProvider;

/**
 * @author Nicolas Rinaudo
 */
class WindowsDesktopAdapter extends DefaultDesktopAdapter {
    protected static final String EXPLORER_NAME = "Explorer";
    private static final String FILE_OPENER_COMMAND = "cmd /c start \"\" \"$f\"";
    private static final String CMD_OPENER_COMMAND = "cmd /k \"cd /d $f\"";
    private static final String EXE_OPENER_COMMAND  = "cmd /c $f";
    private static final String EXE_REGEXP          = ".*\\.exe";

    public String toString() {return "Windows Desktop";}

    @Override
    public void init(boolean install) throws DesktopInitialisationException {
        try {
            CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_OPENER_ALIAS,  FILE_OPENER_COMMAND, CommandType.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.URL_OPENER_ALIAS,   FILE_OPENER_COMMAND, CommandType.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_MANAGER_ALIAS, FILE_OPENER_COMMAND, CommandType.SYSTEM_COMMAND, EXPLORER_NAME));
            CommandManager.registerDefaultCommand(new Command(CommandManager.EXE_OPENER_ALIAS,   EXE_OPENER_COMMAND,  CommandType.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.CMD_OPENER_ALIAS, CMD_OPENER_COMMAND, CommandType.SYSTEM_COMMAND, null));

            CommandManager.registerDefaultAssociation(CommandManager.EXE_OPENER_ALIAS, new RegexpFilenameFilter(EXE_REGEXP, false));
        } catch(CommandException e) {throw new DesktopInitialisationException(e);}
    }

    @Override
    public boolean isAvailable() {
        return OsFamily.WINDOWS.isCurrent();
    }

    /**
     * Returns <code>true</code> for regular files (not directories) with an <code>exe</code> extension
     * (case-insensitive comparison).
     *
     * @param file the file to test
     * @return <code>true</code> for regular files (not directories) with an <code>exe</code> extension
     * (case-insensitive comparison).
     */
    @Override
    public boolean isApplication(AbstractFile file) {
        String extension = file.getExtension();

        // the isDirectory() test comes last as it is I/O bound
        return extension!=null && extension.equalsIgnoreCase("exe") && !file.isDirectory();
    }

    @Override
    public TrashProvider getTrash() {
        // The Windows trash requires access to the Shell32 DLL, register the provider only if the Shell32 DLL
        // is available on the current runtime environment.
        return WindowsTrashProvider.isAvailable() ? new WindowsTrashProvider() : null;
    }

    @Override
    public String getDefaultShell() {return "cmd /c";}
}
