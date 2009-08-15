/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

import java.awt.Dimension;

import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.MainFrame;

/**
 * CommandBarButton that used for display purpose only
 * 
 * @author Arik Hadas
 */
public class CommandBarButtonForDisplay extends CommandBarButton {
	
	/** The preferred size of display button */
	public static final Dimension PREFERRED_SIZE = new Dimension(130, 30);
	
	public static CommandBarButtonForDisplay create(String actionId) {
		return actionId == null ? null : new CommandBarButtonForDisplay(actionId);
	}
	
	private CommandBarButtonForDisplay(String actionId) {
		super(actionId, null);
		setEnabled(true);
		setPreferredSize(PREFERRED_SIZE);
	}
	
	public void setButtonAction(String actionId, MainFrame mainFrame) {
        // Append the action's shortcut to the button's label
        String label = ActionProperties.getActionLabel(actionId);
        String acceleratorText = ActionProperties.getActionTooltip(actionId);
        if(acceleratorText != null)
            label += " [" + acceleratorText + ']';
        setText(label);

        setIcon(IconManager.getScaledIcon(ActionProperties.getActionIcon(actionId), scaleFactor));
	}
}
