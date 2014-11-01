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
import com.mucommander.commons.file.FileFactory;
import com.mucommander.process.ProcessRunner;

import java.io.IOException;
import java.net.URL;

/**
 * @author Nicolas Rinaudo
 */
class CommandBrowse extends UrlOperation {
    // - Desktop operation implementation --------------------------------
    // -------------------------------------------------------------------
    @Override
    public boolean isAvailable() {return CommandManager.getCommandForAlias(CommandManager.URL_OPENER_ALIAS) != null;}

    @Override
    public void execute(URL url) throws IOException {
        Command      command;
        AbstractFile target;

        if((command = CommandManager.getCommandForAlias(CommandManager.URL_OPENER_ALIAS)) == null)
            throw new UnsupportedOperationException();

        target = FileFactory.getFile(url.toString());
        ProcessRunner.execute(command.getTokens(target), target);
    }

    /**
     * Returns the operation's name.
     * @return the operation's name.
     */
    @Override
    public String getName() {return "openURL bridge";}
}
