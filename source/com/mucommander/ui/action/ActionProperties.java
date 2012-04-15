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

package com.mucommander.ui.action;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class that maintains properties of the registered MuAction-s:
 * 1. General properties of all registered actions, such as all existing action categories.
 * 2. ActionDescriptors and helper methods for fetching specific property from ActionDescriptor.
 * 3. Default actions <-> shortcuts mapping.
 * 
 * @author Arik Hadas
 */
public class ActionProperties {
	
	/* Maps action id -> action descriptor */
	private static Hashtable<String, ActionDescriptor> actionDescriptors = new Hashtable<String, ActionDescriptor>();

	private static ActionDescriptor nullActionDescriptor = new NullActionDescriptor();
	
	/* Contains all used action categories (i.e for each category at least one action is registered) */
	private static TreeSet<ActionCategory> actionCategories = new TreeSet<ActionCategory>();

	/* Maps action id -> primary shortcut */
	private static HashMap<String, KeyStroke> defaultPrimaryActionKeymap = new HashMap<String, KeyStroke>();
	/* Maps action id -> alternative shortcut */
	private static HashMap<String, KeyStroke> defaultAlternateActionKeymap = new HashMap<String, KeyStroke>();
	/* Maps shortcut -> action id */
	private static AcceleratorMap defaultAcceleratorMap = new AcceleratorMap();
	
	/**
	 * Getter for ActionDescriptor.
	 * 
	 * @param actionDescriptor - an ActionDescriptor instance to be registered.
	 */
	public static void addActionDescriptor(ActionDescriptor actionDescriptor) {
		String actionId = actionDescriptor.getId();
		
		// Add the descriptor to the descriptors map.
		actionDescriptors.put(actionId, actionDescriptor);
		
		// Add the category in the descriptor to the categories pool
		ActionCategory category = actionDescriptor.getCategory();
		if (category != null)
			actionCategories.add(category);
		
		// Add the shortcuts in the descriptor to the default keymap
		KeyStroke defaultActionKeyStroke = actionDescriptor.getDefaultKeyStroke();
		if (defaultActionKeyStroke != null) {
			defaultPrimaryActionKeymap.put(actionId, defaultActionKeyStroke);
			defaultAcceleratorMap.putAccelerator(defaultActionKeyStroke, actionId);
		}
		
		KeyStroke defaultActionAlternativeKeyStroke = actionDescriptor.getDefaultAltKeyStroke();
		if (defaultActionAlternativeKeyStroke != null) {
			defaultAlternateActionKeymap.put(actionId, defaultActionAlternativeKeyStroke);
			defaultAcceleratorMap.putAlternativeAccelerator(defaultActionAlternativeKeyStroke, actionId);
		}
	}
	
	/**
	 * Getter for MuAction's descriptor.
	 * 
	 * @param actionId - id of MuAction.
	 * @return ActionDescriptor of the given MuAction. null is returned if ActionDescriptor doesn't exist.
	 */
	public static ActionDescriptor getActionDescriptor(String actionId) {
		return actionDescriptors.get(actionId);
	}
	
	/**
	 * Getter for MuAction's description.
	 * MuAction Description is:
	 * 1. action's tooltip.
	 * 2. if tooltip doesn't exist then action's label.
	 * 3. if tooltip and label don't exist, then action's label key.
	 * 
	 * @param actionId - id of MuAction.
	 * @return Description of MuAction as described above.
	 */
	public static String getActionDescription(String actionId) {
		return getNullSafeActionDescriptor(actionId).getDescription();
	}
	
	/**
	 * Getter for MuAction's category.
	 * 
	 * @param actionId - id of MuAction.
	 * @return ActionCategory of the given MuAction. null is returned if ActionCategory doesn't exist.
	 */
	public static ActionCategory getActionCategory(String actionId) {
		return getNullSafeActionDescriptor(actionId).getCategory();
	}
	
	/**
	 * Getter for MuAction's default primary shortcut.
	 * 
	 * @param actionId - id of MuAction.
	 * @return default shortcut of the given MuAction. null is returned if default shortcut doesn't exist.
	 */
	public static KeyStroke getDefaultAccelerator(String actionId) {
		return defaultPrimaryActionKeymap.get(actionId);
	}
	
	/**
	 * Getter for MuAction's alternative shortcut.
	 * 
	 * @param actionId - id of MuAction.
	 * @return alternative shortcut for the given MuAction. null is returned if alternative shortcut doesn't exist.
	 */
	public static KeyStroke getDefaultAlternativeAccelerator(String actionId) {
		return defaultAlternateActionKeymap.get(actionId);
	}
	
	/**
	 * Getter for shortcut's default MuAction.
	 * 
	 * @param keyStroke - shortcut.
	 * @return default MuAction which the given shortcut is assigned for. null is returned if the shortcut doesn't 
	 * assign to any MuAction by default.
	 */
	static String getDefaultActionForKeyStroke(KeyStroke keyStroke) {
		return defaultAcceleratorMap.getActionId(keyStroke);
	}
	
	/**
	 * Getter for shortcut's default type.
	 * The shortcut's type can be either PRIMARY_ACCELERATOR or ALTERNATIVE_ACCELERATOR or 0 if the shortcut doesn't exist by default.
	 * 
	 * @param keyStroke - shortcut.
	 * @return default shortcut's type (PRIMARY_ACCELERATOR/ALTERNATIVE_ACCELERATOR).
	 */
	static int getDefaultAcceleratorType(KeyStroke keyStroke) {
		return defaultAcceleratorMap.getAcceleratorType(keyStroke);
	}
	
	/**
	 * Getter for MuAction's label.
	 * 
	 * @param actionId - id of MuAction.
	 * @return Label of MuAction. if the label doesn't exist in the dictionary, its key is returned.
	 * null is returned if label's key doesn't exist.
	 */
	public static String getActionLabel(String actionId) {
		return getNullSafeActionDescriptor(actionId).getLabel();
	}
	
	/**
	 * Getter for MuAction's label key.
	 * 
	 * @param actionId - id of MuAction.
	 * @return 
	 */
	public static String getActionLabelKey(String actionId) {
		return getNullSafeActionDescriptor(actionId).getLabelKey();
	}
	
	/**
	 * Getter for MuAction's icon.
	 *  
	 * @param actionId - id of MuAction.
	 * @return Icon of MuAction. null is returned if there is no icon for the action.
	 */
	public static ImageIcon getActionIcon(String actionId) {
		return getNullSafeActionDescriptor(actionId).getIcon();
	}
	
	/**
	 * Getter for MuAction's tooltip.
	 *  
	 * @param actionId - id of MuAction.
	 * @return Tooltip of MuAction. null is returned if there is no tooltip for the action.
	 */
	public static String getActionTooltip(String actionId) {
		return getNullSafeActionDescriptor(actionId).getTooltip();
	}
	
	/**
	 * Getter for all existed categories.
	 * Existed category means an actions category which at least one of its actions is registered.
	 * 
	 * The categories are ordered based on the alphabet order of their descriptions (labels).
	 * 
	 * @return Set of existed action categories.
	 */
	public static Set<ActionCategory> getActionCategories() {
		return actionCategories;
	}
	
	private static ActionDescriptor getNullSafeActionDescriptor(String actionId) {
		ActionDescriptor actionDescriptor = actionDescriptors.get(actionId);
		return actionDescriptor != null ? actionDescriptor : nullActionDescriptor;
	}
	
	/**
	 * Helper class that represent ActionDescriptor with null values
	 */
	private static class NullActionDescriptor implements ActionDescriptor {

		public ActionCategory getCategory() { return null; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return null; }

		public String getDescription() { return null; }

		public ImageIcon getIcon() { return null; }

		public String getId() { return null; }

		public String getLabel() { return null; }

		public String getLabelKey() { return null; }

		public String getTooltip() { return null; }

		public boolean isParameterized() { return false; }
	}
}
