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
class CommandOpen extends LocalFileOperation {
    // - Instance fields -------------------------------------------------
    // -------------------------------------------------------------------
    /** Whether or not the 'run as executable' command can be used if no better alternative is found. */
    private boolean allowDefault;



    // - Initialisation --------------------------------------------------
    // -------------------------------------------------------------------
    public CommandOpen(boolean allowDefault) {this.allowDefault = allowDefault;}



    // - Desktop operation implementation --------------------------------
    // -------------------------------------------------------------------
    @Override
    public boolean isAvailable() {
        if(allowDefault)
            return true;
        return CommandManager.getCommandForAlias(CommandManager.FILE_OPENER_ALIAS) != null;
    }

    @Override
    public boolean canExecute(AbstractFile file) {
        if(allowDefault)
            return true;

        return CommandManager.getCommandForFile(file, false) != null;
    }

    @Override
    public CompletionStage<Optional<String>> execute(AbstractFile file) throws IOException {
        Command command = CommandManager.getCommandForFile(file, allowDefault);

        // Attempts to find a command that matches the specified target.
        if (command == null)
            throw new UnsupportedOperationException();

        // If found, executes it.
        return ProcessRunner.executeAsync(command.getTokens(file), file);
    }

    /**
     * Returns the operation's name.
     * @return the operation's name.
     */
    @Override
    public String getName() {return "open bridge";}
}
