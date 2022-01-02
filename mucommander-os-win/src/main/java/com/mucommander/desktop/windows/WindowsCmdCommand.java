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
import com.mucommander.command.CommandType;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.core.desktop.DesktopManager;

/**
 * This is a Windows-specific implementation of the {@link Command} class that
 * escapes parenthesis characters when invoking an executable file
 *
 * @author Arik Hadas
 */
public class WindowsCmdCommand extends Command {
    public WindowsCmdCommand(String alias, String command, CommandType type, String displayName) {
        super(alias, command, type, displayName);
    }

    @Override
    protected String getKeywordReplacement(char keyword, AbstractFile file) {
        String replacement = super.getKeywordReplacement(keyword, file);
        if (OsFamily.WINDOWS.isCurrent() && DesktopManager.isApplication(file))
            switch(keyword) {
            case Command.KEYWORD_PATH:
            case Command.KEYWORD_NAME:
                replacement = replacement.replace("(", "^(").replace(")", "^)");
            default:
            }
        return replacement;
    }
}
