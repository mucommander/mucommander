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

package com.mucommander.ui.main.menu;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.mucommander.command.Command;
import com.mucommander.command.CommandManager;
import com.mucommander.command.CommandType;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.main.MainFrame;


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
        super(Translator.get("file_menu.open_with") + "...");
        this.mainFrame = frame;
        populate();
    }

    /**
     * Refreshes the content of the menu.
     */
    private synchronized void populate() {
        for (Command command : CommandManager.commands()) {
            if(command.getType() == CommandType.NORMAL_COMMAND)
            	add(ActionManager.getActionInstance(command, mainFrame));
        }
        if(getItemCount() == 0)
            setEnabled(false);
    }

    @Override
    public final JMenuItem add(Action a) {
    	JMenuItem item = super.add(a);
    	MenuToolkit.configureActionMenuItem(item);
    	return item;
    }
}
