package com.mucommander.ui.action;

import com.mucommander.conf.ConfigurationManager;
import com.mucommander.conf.ConfigurationVariables;
import com.mucommander.text.Translator;
import com.mucommander.ui.CommandBar;
import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

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

    public ToggleCommandBarAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties, false);
        setLabel(Translator.get(ConfigurationManager.getVariableBoolean(ConfigurationVariables.COMMAND_BAR_VISIBLE,
                                                                        ConfigurationVariables.DEFAULT_COMMAND_BAR_VISIBLE) ?
                                com.mucommander.ui.action.ToggleCommandBarAction.class.getName()+".hide":com.mucommander.ui.action.ToggleCommandBarAction.class.getName()+".show"));
    }


    public void performAction() {
        CommandBar commandBar = mainFrame.getCommandBar();
        boolean visible = !commandBar.isVisible();
        // Save the last command bar visible state in the configuration, this will become the default for new MainFrame windows.
        ConfigurationManager.setVariableBoolean(ConfigurationVariables.COMMAND_BAR_VISIBLE, visible);
        // Change the label to reflect the new command bar state
        setLabel(Translator.get(visible?com.mucommander.ui.action.ToggleCommandBarAction.class.getName()+".hide":com.mucommander.ui.action.ToggleCommandBarAction.class.getName()+".show"));
        // Show/hide the command bar
        commandBar.setVisible(visible);
        mainFrame.validate();
    }
}
