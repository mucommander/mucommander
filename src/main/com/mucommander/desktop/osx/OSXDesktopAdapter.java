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

package com.mucommander.desktop.osx;

import java.awt.event.MouseEvent;

import com.mucommander.command.Command;
import com.mucommander.command.CommandException;
import com.mucommander.command.CommandManager;
import com.mucommander.command.CommandType;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.desktop.DefaultDesktopAdapter;
import com.mucommander.desktop.DesktopInitialisationException;
import com.mucommander.desktop.DesktopManager;

/**
 * @author Nicolas Rinaudo
 */
public class OSXDesktopAdapter extends DefaultDesktopAdapter {
    private static final String OPENER_COMMAND = "open $f";
    private static final String FINDER_COMMAND = "open -a Finder $f";
    private static final String FINDER_NAME    = "Finder";

    public String toString() {return "MAC OS X Desktop";}

    @Override
    public boolean isAvailable() {return OsFamily.getCurrent().equals(OsFamily.MAC_OS_X);}

    @Override
    public void init(boolean install) throws DesktopInitialisationException {
        // Initialises trash management.
        DesktopManager.setTrashProvider(new OSXTrashProvider());

        // Registers OS X specific commands.
        try {
            CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_OPENER_ALIAS,  OPENER_COMMAND, CommandType.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.URL_OPENER_ALIAS,   OPENER_COMMAND, CommandType.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_MANAGER_ALIAS, FINDER_COMMAND, CommandType.SYSTEM_COMMAND, FINDER_NAME));
        }
        catch(CommandException e) {throw new DesktopInitialisationException(e);}
    }

    @Override
    public boolean isLeftMouseButton(MouseEvent e) {
        int modifiers;

        modifiers = e.getModifiers();
        return (modifiers & MouseEvent.BUTTON1_MASK) != 0 && !e.isControlDown();
    }

    @Override
    public boolean isRightMouseButton(MouseEvent e) {
        int modifiers;

        modifiers = e.getModifiers();
        return (modifiers & MouseEvent.BUTTON3_MASK) != 0 || ((modifiers & MouseEvent.BUTTON1_MASK) != 0 && e.isControlDown());
    }

    /**
     * Returns <code>true</code> for directories with an <code>app</code> extension (case-insensitive comparison).
     *
     * @param file the file to test
     * @return <code>true</code> for directories with an <code>app</code> extension (case-insensitive comparison).
     */
    @Override
    public boolean isApplication(AbstractFile file) {
        String extension = file.getExtension();

        // the isDirectory() test comes last as it is I/O bound
        return extension!=null && extension.equalsIgnoreCase("app") && file.isDirectory();
    }
}
