package com.mucommander.ui;

import com.mucommander.ui.MainFrame;
import com.mucommander.text.Translator;
import com.mucommander.command.Command;
import com.mucommander.command.CommandManager;

import javax.swing.*;
import java.util.*;


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
