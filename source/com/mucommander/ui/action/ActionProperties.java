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

package com.mucommander.ui.action;

import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.KeyStroke;

/**
 * TODO: document
 * 
 * @author Arik Hadas
 */
public class ActionProperties {
	/* map action id -> action descriptor */
	private static Hashtable actionDescriptors = new Hashtable();
	
	private static HashMap defaultPrimaryActionKeymap = new HashMap();
	private static HashMap defaultAlternateActionKeymap = new HashMap();
	private static HashMap defaultAcceleratorMap = new HashMap();
	
	public static void addActionDescriptor(ActionDescriptor actionDescriptor) {
		String actionId = actionDescriptor.getId();
		actionDescriptors.put(actionId, actionDescriptor);
		
		// keymaps:
		KeyStroke defaultActionKeyStroke = actionDescriptor.getDefaultKeyStroke();
		if (defaultActionKeyStroke != null) {
			defaultPrimaryActionKeymap.put(actionId, defaultActionKeyStroke);
			defaultAcceleratorMap.put(defaultActionKeyStroke, actionId);
		}
		
		KeyStroke defaultActionAlternativeKeyStroke = actionDescriptor.getDefaultAltKeyStroke();
		if (defaultActionAlternativeKeyStroke != null) {
			defaultAlternateActionKeymap.put(actionId, defaultActionAlternativeKeyStroke);
			defaultAcceleratorMap.put(defaultActionAlternativeKeyStroke, actionId);
		}
	}
	
	public static ActionDescriptor getActionDescriptor(String actionId) {
		return (ActionDescriptor) actionDescriptors.get(actionId);
	}
	
	static KeyStroke getDefaultAccelerator(String actionID) {
		return (KeyStroke) defaultPrimaryActionKeymap.get(actionID);
	}
	
	static KeyStroke getDefaultAlternativeAccelerator(String actionID) {
		return (KeyStroke) defaultAlternateActionKeymap.get(actionID);
	}
	
	static String getActionForKeyStroke(KeyStroke keyStroke) {
		return (String) defaultAcceleratorMap.get(keyStroke);
	}
}
