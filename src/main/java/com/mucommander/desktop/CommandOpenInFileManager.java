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

package com.mucommander.desktop;

import com.mucommander.command.Command;
import com.mucommander.command.CommandManager;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.process.ProcessRunner;

import java.io.IOException;

/**
 * @author Nicolas Rinaudo
 */
class CommandOpenInFileManager extends LocalFileOperation {
    @Override
    public boolean isAvailable() {return CommandManager.getCommandForAlias(CommandManager.FILE_MANAGER_ALIAS) != null;}

    @Override
    public void execute(AbstractFile file) throws IOException {
        Command command;

        if((command = CommandManager.getCommandForAlias(CommandManager.FILE_MANAGER_ALIAS)) == null)
            throw new UnsupportedOperationException();

        ProcessRunner.execute(command.getTokens(file), file);
    }

    @Override
    public String getName() {
        Command command;

        if((command = CommandManager.getCommandForAlias(CommandManager.FILE_MANAGER_ALIAS)) != null)
            return command.getDisplayName();
        return null;
    }
}
