package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.CommandBar;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.text.Translator;

/**
 * This action shows/hides the current MainFrame's {@link com.mucommander.ui.CommandBar} depending on its
 * current visible state: if it is visible, hides it, if not shows it.
 *
 * <p>This action's label will be updated to reflect the current visible state.
 *
 * <p>Each time this action is executed, the new current visible state is stored in the configuration so that
 * new MainFrame windows will use it to determine whether the CommandBar has to be made visible or not.
 *
 * @author Maxence Bernard
 */
public class ToggleCommandBarAction extends MucoAction {

    public ToggleCommandBarAction(MainFrame mainFrame) {
        super(mainFrame, ConfigurationManager.getVariableBoolean("prefs.command_bar.visible", true)?"view_menu.hide_command_bar":"view_menu.show_command_bar");
    }

    public void performAction(MainFrame mainFrame) {
        CommandBar commandBar = mainFrame.getCommandBar();
        boolean visible = !commandBar.isVisible();
        // Save the last command bar visible state in the configuration, this will become the default for new MainFrame windows.
        ConfigurationManager.setVariableBoolean("prefs.command_bar.visible", visible);
        // Change the label to reflect the new command bar state
        setLabel(Translator.get(visible?"view_menu.hide_command_bar":"view_menu.show_command_bar"));
        // Show/hide the command bar
        commandBar.setVisible(visible);
        mainFrame.validate();
    }
}
