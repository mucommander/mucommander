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

package com.mucommander.ui.action.impl;

import java.util.Map;

import javax.swing.JFrame;
import javax.swing.KeyStroke;

import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategories;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.ActionFactory;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;

/**
 * Maximizes the {@link MainFrame} this action is associated with.
 *
 * @author Maxence Bernard
 * @see com.mucommander.ui.action.impl.MinimizeWindowAction
 */
public class MaximizeWindowAction extends MuAction {

    public MaximizeWindowAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
			return new MaximizeWindowAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "MaximizeWindow";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.WINDOW; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return null; }

        @Override
        public String getLabel() {
            // Use a special label for Mac OS X, if it exists, use the standard action label otherwise
            String macLabelKey = ActionProperties.getActionLabelKey(ACTION_ID)+".mac_os_x";
            if(OsFamily.MAC_OS_X.isCurrent() && Translator.hasValue(macLabelKey, false))
                return Translator.get(macLabelKey);

            return super.getLabel();
        }
    }
}
