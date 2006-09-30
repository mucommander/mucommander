package com.mucommander.ui.action;

import com.mucommander.conf.ConfigurationManager;
import com.mucommander.text.Translator;
import com.mucommander.ui.CommandBar;
import com.mucommander.ui.MainFrame;

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
        super(mainFrame);
        setLabel(Translator.get(ConfigurationManager.getVariableBoolean("prefs.command_bar.visible", true)?"com.mucommander.ui.action.ToggleCommandBarAction.hide":"com.mucommander.ui.action.ToggleCommandBarAction.show"));
    }


    public void performAction() {
        CommandBar commandBar = mainFrame.getCommandBar();
        boolean visible = !commandBar.isVisible();
        // Save the last command bar visible state in the configuration, this will become the default for new MainFrame windows.
        ConfigurationManager.setVariableBoolean("prefs.command_bar.visible", visible);
        // Change the label to reflect the new command bar state
        setLabel(Translator.get(visible?"com.mucommander.ui.action.ToggleCommandBarAction.hide":"com.mucommander.ui.action.ToggleCommandBarAction.show"));
        // Show/hide the command bar
        commandBar.setVisible(visible);
        mainFrame.validate();
    }
}
