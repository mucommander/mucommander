/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.ui.main.commandbar;

import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.runtime.OsFamilies;
import com.mucommander.runtime.OsVersions;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.button.NonFocusableButton;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.MainFrame;

import java.awt.Dimension;
import java.awt.Insets;

/**
 * Button that located in command-bar.
 * Button in command-bar are associated with an MuAction and could have an alternative action,
 * i.e an action that would replace the regular button's action when a modifier key is pressed.
 * 
 * @author Arik Hadas
 */
public class CommandBarButton extends NonFocusableButton implements ConfigurationListener {

	/** ID of the button's action */
	private String actionId;
	
	/** Current icon scale factor */
    // The math.max(1.0f, ...) part is to workaround a bug which cause(d) this value to be set to 0.0 in the configuration file.
    protected static float scaleFactor = Math.max(1.0f, MuConfiguration.getVariable(MuConfiguration.COMMAND_BAR_ICON_SCALE,
                                                                        MuConfiguration.DEFAULT_COMMAND_BAR_ICON_SCALE));
	
    public static CommandBarButton create(String actionId, MainFrame mainFrame) {
		return actionId == null ? null : new CommandBarButton(actionId, mainFrame);
	}
	
	protected CommandBarButton(String actionId, MainFrame mainFrame) {
		
		// Use new JButton decorations introduced in Mac OS X 10.5 (Leopard)
        if(OsFamilies.MAC_OS_X.isCurrent() && OsVersions.MAC_OS_X_10_5.isCurrentOrHigher()) {
            putClientProperty("JComponent.sizeVariant", "small");
            putClientProperty("JButton.buttonType", "textured");
        }
        else {
            setMargin(new Insets(3,4,3,4));
        }
        
        this.actionId = actionId;
        
        setButtonAction(actionId, mainFrame);
        
        // For Mac OS X whose default minimum width for buttons is enormous
        setMinimumSize(new Dimension(40, (int) getPreferredSize().getHeight()));
        
        // Listen to configuration changes to reload command bar buttons when icon size has changed
        MuConfiguration.addConfigurationListener(this);
	}
	
	/**
     * Sets the given button's action, custom label showing the accelerator and icon taking into account the scale factor.
     */
	public void setButtonAction(String actionId, MainFrame mainFrame) {
    	MuAction action = ActionManager.getActionInstance(actionId, mainFrame);
    	
    	setAction(action);
    	
        // Append the action's shortcut to the button's label
        String label;
        label = action.getLabel();
        if(action.getAcceleratorText() != null)
            label += " [" + action.getAcceleratorText() + ']';
        setText(label);

        // Scale icon if scale factor is different from 1.0
        if(scaleFactor!=1.0f)
            setIcon(IconManager.getScaledIcon(action.getIcon(), scaleFactor));
    }
	
    /**
     * Return the id of the button's action
     * 
     * @return id of the button's action
     */
    public String getActionId() {
    	return actionId;
    }
    
    ///////////////////////////////////
    // ConfigurationListener methods //
    ///////////////////////////////////

    /**
     * Listens to certain configuration variables.
     */
    public void configurationChanged(ConfigurationEvent event) {
        String var = event.getVariable();

        // Reload butons icon if the icon scale factor has changed
        if (var.equals(MuConfiguration.COMMAND_BAR_ICON_SCALE)) {
            scaleFactor = event.getFloatValue();

            // Change the button's icon but NOT the action's icon which has to remain in its original non-scaled size
            setIcon(IconManager.getScaledIcon(((MuAction) getAction()).getIcon(), scaleFactor));
        }
    }
}
