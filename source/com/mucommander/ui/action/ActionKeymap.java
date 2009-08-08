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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import com.mucommander.ui.main.MainFrame;

/**
 * This class manages keyboard associations with {@link MuAction} ids.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class ActionKeymap {

    /** Maps action id onto Keystroke instances*/
    private static HashMap customPrimaryActionKeymap = new HashMap();
    /** Maps action id instances onto Keystroke instances*/
    private static HashMap customAlternateActionKeymap = new HashMap();
    /** Maps Keystroke instances onto action id */
    private static AcceleratorMap acceleratorMap = new AcceleratorMap();

    /******************
     * Public Methods *
     ******************/
    
    /**
     * Register all action shortcuts to the given MainFrame's file tables.
     * 
     * @param mainFrame - MainFrame instance to which all action shortcuts would be registered.
     */
    public static void registerActions(MainFrame mainFrame) {
        Enumeration actionIds = ActionManager.getActionIds();
        while(actionIds.hasMoreElements()) {
            MuAction action = ActionManager.getActionInstance((String)actionIds.nextElement(), mainFrame);
            registerAction(mainFrame, action);
        }
    }
    
    /**
     * Register MuAction to JComponent with the given condition. 
     * 
     * @param action - MuAction instance.
     * @param comp - JComponent to which the action would be registered
     * @param condition - condition in which the action could be invoked. 
     */
    public static void registerActionAccelerators(MuAction action, JComponent comp, int condition) {
    	registerActionAccelerator(action, action.getAccelerator(), comp, condition);
    	registerActionAccelerator(action, action.getAlternateAccelerator(), comp, condition);
    }

    /**
     * Register new accelerators to the given action.
     * 
     * @param actionId - id of the MuAction.
     * @param accelerator - KeyStroke that would be primary accelerator of the given action.
     * @param alternateAccelerator - KeyStroke that would be alternative accelerator of the given action.
     */
    public static void changeActionAccelerators(String actionId, KeyStroke accelerator, KeyStroke alternateAccelerator) {
    	// If primary accelerator is already registered to MuAction, unregister it.
    	if (isKeyStrokeRegistered(accelerator)) {
    		String prevAcceleratorActionId = getRegisteredActionIdForKeystroke(accelerator);
    		
    		switch (getAcceleratorType(accelerator)) {
    		case AcceleratorMap.PRIMARY_ACCELERATOR:
    			registerActionAccelerators(prevAcceleratorActionId, null, getAlternateAccelerator(prevAcceleratorActionId));
    			break;
    		case AcceleratorMap.ALTERNATIVE_ACCELERATOR:
    			registerActionAccelerators(prevAcceleratorActionId, getAccelerator(prevAcceleratorActionId), null);
    			break;
    		}
    	}
    	
    	// If alternative accelerator is already registered to MuAction, unregister it.
    	if (isKeyStrokeRegistered(alternateAccelerator)) {
    		String prevAltAcceleratorActionId = getRegisteredActionIdForKeystroke(alternateAccelerator);
    		
    		switch (getAcceleratorType(alternateAccelerator)) {
    		case AcceleratorMap.PRIMARY_ACCELERATOR:
    			registerActionAccelerators(prevAltAcceleratorActionId, null, getAlternateAccelerator(prevAltAcceleratorActionId));
    			break;
    		case AcceleratorMap.ALTERNATIVE_ACCELERATOR:
    			registerActionAccelerators(prevAltAcceleratorActionId, getAccelerator(prevAltAcceleratorActionId), null);
    			break;
    		}
    	}
    	
    	// Remove action's previous accelerators (primary and alternate)
    	acceleratorMap.remove((KeyStroke)customPrimaryActionKeymap.remove(actionId));
    	acceleratorMap.remove((KeyStroke)customAlternateActionKeymap.remove(actionId));

    	// Register new accelerators
    	registerActionAccelerators(actionId, accelerator, alternateAccelerator);
    }
    
    /**
     * Register all primary and alternative accelerator given.
     * 
     * @param primary - HashMap that maps action id to primary accelerator.
     * @param alternate - HashMap that maps action id to alternative accelerator.
     */
    public static void registerActions(HashMap primary, HashMap alternate) {
    	Iterator actionIdsIterator = primary.keySet().iterator();
    	
    	while(actionIdsIterator.hasNext()) {
    		String actionId = (String) actionIdsIterator.next();
    		
    		// Add the action/keystroke mapping
        	ActionKeymap.changeActionAccelerators(
        			actionId, 
        			(KeyStroke) primary.get(actionId), 
        			(KeyStroke) alternate.get(actionId));
    	}
    }

    /**
     * Check whether an accelerator is registered to MuAction.
     * 
     * @param ks - accelerator.
     * @return true if the given accelerator is registered to whatever MuAction, false otherwise.
     */
    public static boolean isKeyStrokeRegistered(KeyStroke ks) {
    	return getRegisteredActionIdForKeystroke(ks)!=null;
    }
    
    /**
     * Restore the default accelerators assignments (remove custom assignments).
     */
    public static void restoreDefault() {
    	customPrimaryActionKeymap.clear();
    	customAlternateActionKeymap.clear();
    	acceleratorMap.clear();
    }
    
    ///////////////////
    ///// getters /////
    ///////////////////
    
    /**
     * Return primary accelerator of MuAction.
     * 
     * @param actionId - id of MuAction.
     * @return primary accelerator of the given MuAction.
     */
    public static KeyStroke getAccelerator(String actionId) {
    	if (customPrimaryActionKeymap.containsKey(actionId))
    		return (KeyStroke)customPrimaryActionKeymap.get(actionId);
        return ActionProperties.getDefaultAccelerator(actionId);
    }

    /**
     * Return alternative accelerator of MuAction.
     * 
     * @param actionId - id of MuAction.
     * @return alternative accelerator of the given MuAction.
     */
    public static KeyStroke getAlternateAccelerator(String actionId) {
    	if (customAlternateActionKeymap.containsKey(actionId))
    		return (KeyStroke)customAlternateActionKeymap.get(actionId);
    	return ActionProperties.getDefaultAlternativeAccelerator(actionId);
    }

    /**
     * Return the id of MuAction to which accelerator is registered.
     * 
     * @param ks - accelerator.
     * @return id of MuAction to which the given accelerator is registered, null if the accelerator is not registered.
     */
    public static String getRegisteredActionIdForKeystroke(KeyStroke ks) {
    	String actionID = acceleratorMap.getActionId(ks);
        return actionID != null ? actionID : ActionProperties.getDefaultActionForKeyStroke(ks);
    }
    
    /**
     * Return accelerator type: primary\alternative\not registered.
     * 
     * @param ks - accelerator.
     * @return accelerator type.
     */
    public static int getAcceleratorType(KeyStroke ks) {
    	int type = acceleratorMap.getAcceleratorType(ks);
    	return type != 0 ? type : ActionProperties.getDefaultAcceleratorType(ks);
    }
    
    /**
     * Return ids of actions that their registered accelerators are different from their default accelerators.
     * 
     * @return Iterator of actions that their accelerators were customized.
     */
    public static Iterator getCustomizedActions() {
    	Set modifiedActions = new HashSet();
    	modifiedActions.addAll(customPrimaryActionKeymap.keySet());
    	modifiedActions.addAll(customAlternateActionKeymap.keySet());
    	return modifiedActions.iterator();
    }
    
    
    /*******************
     * Private Methods *
     *******************/

    /**
     * Register MuAction instance to MainFrame instance.
     */
    private static void registerAction(MainFrame mainFrame, MuAction action) {
        registerActionAccelerators(action, mainFrame.getLeftPanel().getFileTable(), JComponent.WHEN_FOCUSED);
        registerActionAccelerators(action, mainFrame.getRightPanel().getFileTable(), JComponent.WHEN_FOCUSED);
    }

    /**
     * Register accelerator of MuAction to JComponent with a condition that states when the action can be invoked.
     */
    private static void registerActionAccelerator(MuAction action, KeyStroke accelerator, JComponent comp, int condition) {
    	if(accelerator != null) {
    		InputMap inputMap = comp.getInputMap(condition);
    		ActionMap actionMap = comp.getActionMap();
    		Class muActionClass = action.getClass();
    		inputMap.put(accelerator, muActionClass);
    		actionMap.put(muActionClass, action);
    	}
    }

    /**
     * Unregister MuAction instance from MainFrame instance.
     */
    private static void unregisterAction(MainFrame mainFrame, MuAction action) {
        unregisterActionAccelerators(action, mainFrame.getLeftPanel().getFileTable(), JComponent.WHEN_FOCUSED);
        unregisterActionAccelerators(action, mainFrame.getRightPanel().getFileTable(), JComponent.WHEN_FOCUSED);
    }
    
    /**
     * Unregister MuAction from JComponent.
     */
    private static void unregisterActionAccelerators(MuAction action, JComponent comp, int condition) {
    	unregisterActionAccelerator(action, action.getAccelerator(), comp, condition);
    	unregisterActionAccelerator(action, action.getAlternateAccelerator(), comp, condition);
    }
    
    /**
     * Unregister accelerator of MuAction from JComponent.
     */
    private static void unregisterActionAccelerator(MuAction action, KeyStroke accelerator, JComponent comp, int condition) {
    	if(accelerator != null) {
    		InputMap inputMap = comp.getInputMap(condition);
    		ActionMap actionMap = comp.getActionMap();
    		Class muActionClass = action.getClass();
    		inputMap.remove(accelerator);
    		actionMap.remove(muActionClass);
    	}
    }
    
    /**
     * Register primary and alternative accelerators to MuAction.
     */
    private static void registerActionAccelerators(String actionId, KeyStroke accelerator, KeyStroke alternateAccelerator) {
    	// New accelerators are identical to the default action's accelerators
    	if (equals(accelerator, ActionProperties.getDefaultAccelerator(actionId)) &&
    			equals(alternateAccelerator, ActionProperties.getDefaultAlternativeAccelerator(actionId))) {
    		// Remove all action's accelerators customization
    		customPrimaryActionKeymap.remove(actionId);
    		customAlternateActionKeymap.remove(actionId);
    		acceleratorMap.remove(accelerator);
    		acceleratorMap.remove(alternateAccelerator);
    	}
    	// New accelerators are different from the default accelerators
    	else {
    		customPrimaryActionKeymap.put(actionId, accelerator);
    		acceleratorMap.putAccelerator(accelerator, actionId);

    		customAlternateActionKeymap.put(actionId, alternateAccelerator);
    		acceleratorMap.putAlternativeAccelerator(alternateAccelerator, actionId);
    	}
    	
    	// Update each MainFrame's action instance and input map
    	Iterator actionInstancesIterator = ActionManager.getActionInstances(actionId).iterator();
    	while (actionInstancesIterator.hasNext()) {
    		MuAction action = (MuAction) actionInstancesIterator.next();
    		MainFrame mainFrame = action.getMainFrame();

    		// Remove action from MainFrame's action and input maps
    		unregisterAction(mainFrame, action);

    		// Change action's accelerators
    		action.setAccelerator(accelerator);
    		action.setAlternateAccelerator(alternateAccelerator);

    		// Add updated action to MainFrame's action and input maps
    		registerAction(mainFrame, action);
    	}
    }
    
    /**
     * Return true if the two KeyStrokes are equal, false otherwise.
     */
    private static boolean equals(KeyStroke first, KeyStroke second) {
    	if (first == null)
    		return second == null;
    	return first.equals(second);
    }
}
