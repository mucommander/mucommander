package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import javax.swing.*;

/**
 * This action clicks the button specified in the constructor.
 *
 * <p>This does the same thing as if the user had pressed and released the button, which visually translates into
 * the button being briefly 'down'. The purpose of this action is to visually associate a button to a keyboard shortcut.
 *
 * @see com.mucommander.ui.CommandBar
 * @author Maxence Bernard
 */
public class ClickButtonAction extends MucoAction {

    private JButton button;

    public ClickButtonAction(MainFrame mainFrame, JButton button) {
        super(mainFrame);
        this.button = button;

        Action action = button.getAction();
        if(action!=null && action instanceof MucoAction)
            setAccelerator(((MucoAction)action).getAccelerator());
    }

    public void performAction(MainFrame mainFrame) {
        button.doClick();
    }
}
