package com.mucommander.ui.action;

import com.mucommander.conf.*;
import com.mucommander.text.Translator;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.StatusBar;

/**
 * This action shows/hides the current MainFrame's {@link com.mucommander.ui.StatusBar} depending on its
 * current visible state: if it is visible, hides it, if not shows it.
 *
 * <p>This action's label will be updated to reflect the current visible state.
 *
 * <p>Each time this action is executed, the new current visible state is stored in the configuration so that
 * new MainFrame windows will use it to determine whether the StatusBar has to be made visible or not.
 *
 * @author Maxence Bernard
 */
public class ToggleStatusBarAction extends MucoAction {

    public ToggleStatusBarAction(MainFrame mainFrame) {
        super(mainFrame, false);
        setLabel(Translator.get(ConfigurationManager.getVariableBoolean(ConfigurationVariables.STATUS_BAR_VISIBLE,
                                                                        ConfigurationVariables.DEFAULT_STATUS_BAR_VISIBLE) ? 
                                "com.mucommander.ui.action.ToggleStatusBarAction.hide":"com.mucommander.ui.action.ToggleStatusBarAction.show"));
    }

    public void performAction() {
        StatusBar statusBar = mainFrame.getStatusBar();
        boolean visible = !statusBar.isVisible();
        // Save the last status bar visible state in the configuration, this will become the default for new MainFrame windows.
        ConfigurationManager.setVariableBoolean(ConfigurationVariables.STATUS_BAR_VISIBLE, visible);
        // Change the label to reflect the new status bar state
        setLabel(Translator.get(visible?"com.mucommander.ui.action.ToggleStatusBarAction.hide":"com.mucommander.ui.action.ToggleStatusBarAction.show"));
        // Show/hide the status bar
        statusBar.setVisible(visible);
        mainFrame.validate();
    }
}
