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

package com.mucommander.ui.main.menu;

import com.mucommander.command.Command;
import com.mucommander.command.CommandManager;
import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.util.Hashtable;
import java.util.Iterator;


/**
 * Open with menu.
 * <p>
 * Note that this class doesn't yet monitor modifications to the command list.
 * </p>
 * @author Nicolas Rinaudo
 */
public class OpenWithMenu extends JMenu {
    private MainFrame mainFrame;

    /**
     * Creates a new Open With menu.
     */
    public OpenWithMenu(MainFrame frame) {
        super(Translator.get("file_menu.open_with"));
        this.mainFrame = frame;
        populate();
    }

    /**
     * Refreshes the content of the menu.
     */
    private synchronized void populate() {
        Iterator iterator = CommandManager.commands();
        Command  command;

        while(iterator.hasNext()) {
            command = (Command)iterator.next();
            if(command.getType() == Command.NORMAL_COMMAND)
                add(new com.mucommander.ui.action.CommandAction(mainFrame, new Hashtable(), command));
        }
        if(getItemCount() == 0)
            setEnabled(false);
    }
}
