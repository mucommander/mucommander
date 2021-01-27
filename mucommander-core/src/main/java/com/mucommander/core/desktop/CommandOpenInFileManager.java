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

package com.mucommander.core.desktop;

import com.mucommander.command.Command;
import com.mucommander.command.CommandManager;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.desktop.LocalFileOperation;
import com.mucommander.process.ProcessRunner;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * @author Nicolas Rinaudo
 */
class CommandOpenInFileManager extends LocalFileOperation {
    @Override
    public boolean isAvailable() {
        return CommandManager.getCommandForAlias(CommandManager.FILE_MANAGER_ALIAS) != null;
    }

    @Override
    public CompletionStage<Optional<String>> execute(AbstractFile file) throws IOException {
        Command command = CommandManager.getCommandForAlias(CommandManager.FILE_MANAGER_ALIAS);
        if (command == null)
            throw new UnsupportedOperationException();

        return ProcessRunner.executeAsync(command.getTokens(file), file);
    }

    @Override
    public String getName() {
        Command command = CommandManager.getCommandForAlias(CommandManager.FILE_MANAGER_ALIAS);
        return command != null ? command.getDisplayName() : null;
    }
}
