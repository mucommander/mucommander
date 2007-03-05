package com.mucommander.ui.action;

import com.mucommander.conf.ConfigurationManager;
import com.mucommander.conf.ConfigurationVariables;
import com.mucommander.text.Translator;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ToolBar;

import java.util.Hashtable;

/**
 * This action shows/hides the current MainFrame's {@link com.mucommander.ui.ToolBar} depending on its
 * current visible state: if it is visible, hides it, if not shows it.
 *
 * <p>This action's label will be updated to reflect the current visible state.
 *
 * <p>Each time this action is executed, the new current visible state is stored in the configuration so that
 * new MainFrame windows will use it to determine whether the ToolBar has to be made visible or not.
 *
 * @author Maxence Bernard
 */
public class ToggleToolBarAction extends MucoAction {

    public ToggleToolBarAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties, false);
        setLabel(Translator.get(ConfigurationManager.getVariableBoolean(ConfigurationVariables.TOOLBAR_VISIBLE,
                                                                        ConfigurationVariables.DEFAULT_TOOLBAR_VISIBLE)
                                ? com.mucommander.ui.action.ToggleToolBarAction.class.getName()+".hide":com.mucommander.ui.action.ToggleToolBarAction.class.getName()+".show"));
    }

    public void performAction() {
        ToolBar toolBar = mainFrame.getToolBar();
        boolean visible = !toolBar.isVisible();
        // Save the last toolbar visible state in the configuration, this will become the default for new MainFrame windows.
        ConfigurationManager.setVariableBoolean(ConfigurationVariables.TOOLBAR_VISIBLE, visible);
        // Change the label to reflect the new toolbar state
        setLabel(Translator.get(visible?com.mucommander.ui.action.ToggleToolBarAction.class.getName()+".hide":com.mucommander.ui.action.ToggleToolBarAction.class.getName()+".show"));
        // Show/hide the toolbar
        toolBar.setVisible(visible);
        mainFrame.validate();
    }
}
