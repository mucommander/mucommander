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

package com.mucommander.desktop.linux.xfce;

import com.mucommander.command.Command;
import com.mucommander.command.CommandException;
import com.mucommander.command.CommandManager;
import com.mucommander.command.CommandType;
import com.mucommander.desktop.DefaultDesktopAdapter;
import com.mucommander.desktop.DesktopInitialisationException;
import com.mucommander.desktop.TrashProvider;

/**
 * @author Arik Hadas
 */
abstract class XfceDesktopAdapter extends DefaultDesktopAdapter {
	private static final String FILE_MANAGER_NAME = "Thunar";
    private static final String FILE_OPENER       = "exo-open $f";
    private static final String EXE_OPENER        = "$f";
    private static final String CMD_OPENER_COMMAND = "xfce4-terminal --default-working-directory $f";
    
	@Override
    public void init(boolean install) throws DesktopInitialisationException {
        // Registers KDE specific commands.
        try {
        	CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_OPENER_ALIAS,  FILE_OPENER, CommandType.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.URL_OPENER_ALIAS,   FILE_OPENER, CommandType.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.EXE_OPENER_ALIAS,   EXE_OPENER,  CommandType.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_MANAGER_ALIAS, FILE_OPENER, CommandType.SYSTEM_COMMAND, FILE_MANAGER_NAME));
            CommandManager.registerDefaultCommand(new Command(CommandManager.CMD_OPENER_ALIAS, CMD_OPENER_COMMAND, CommandType.SYSTEM_COMMAND, null));
        }
        catch(CommandException e) {throw new DesktopInitialisationException(e);}
    }

	@Override
	public TrashProvider getTrash() {
	    return new XfceTrashProvider();
	}
}
