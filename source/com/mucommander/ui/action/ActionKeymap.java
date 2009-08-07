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
import java.util.Vector;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import com.mucommander.ui.main.MainFrame;


/**
 * This class manages keyboard associations with {@link MuAction} classes.
 * Proper documentation and cleaning of this class is pending.  
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class ActionKeymap {

    /** Maps action Class onto Keystroke instances*/
    private static HashMap customPrimaryActionKeymap = new HashMap();
    /** Maps action Class instances onto Keystroke instances*/
    private static HashMap customAlternateActionKeymap = new HashMap();

    /** Maps Keystroke instances onto action Class */
    private static HashMap acceleratorMap = new HashMap();

    ///////////////////
    ///// getters /////
    ///////////////////
    
    public static KeyStroke getAccelerator(String actionId) {
    	if (customPrimaryActionKeymap.containsKey(actionId))
    		return (KeyStroke)customPrimaryActionKeymap.get(actionId);
        return ActionProperties.getDefaultAccelerator(actionId);
    }

    public static KeyStroke getAlternateAccelerator(String actionId) {
    	if (customAlternateActionKeymap.containsKey(actionId))
    		return (KeyStroke)customAlternateActionKeymap.get(actionId);
    	return ActionProperties.getDefaultAlternativeAccelerator(actionId);
    }

    public static boolean isKeyStrokeRegistered(KeyStroke ks) {
    	return getRegisteredActionIdForKeystroke(ks)!=null;
    }

    public static String getRegisteredActionIdForKeystroke(KeyStroke ks) {
    	String actionID = (String) acceleratorMap.get(ks);
        return actionID != null ? actionID : ActionProperties.getActionForKeyStroke(ks);
    }

    public static void registerActions(MainFrame mainFrame) {
    	JComponent leftTable = mainFrame.getLeftPanel().getFileTable();
        JComponent rightTable = mainFrame.getRightPanel().getFileTable();

        Enumeration actionIDs = ActionManager.getActionIds();
        while(actionIDs.hasMoreElements()) {
            MuAction action = ActionManager.getActionInstance((String)actionIDs.nextElement(), mainFrame);
            ActionKeymap.registerActionAccelerators(action, leftTable, JComponent.WHEN_FOCUSED);
            ActionKeymap.registerActionAccelerators(action, rightTable, JComponent.WHEN_FOCUSED);
        }
    }

    private static void registerAction(MainFrame mainFrame, MuAction action) {
        registerActionAccelerators(action, mainFrame.getLeftPanel().getFileTable(), JComponent.WHEN_FOCUSED);
        registerActionAccelerators(action, mainFrame.getRightPanel().getFileTable(), JComponent.WHEN_FOCUSED);
    }

    private static void unregisterAction(MainFrame mainFrame, MuAction action) {
        unregisterActionAccelerators(action, mainFrame.getLeftPanel().getFileTable(), JComponent.WHEN_FOCUSED);
        unregisterActionAccelerators(action, mainFrame.getRightPanel().getFileTable(), JComponent.WHEN_FOCUSED);
    }

    private static void registerActionAccelerator(MuAction action, KeyStroke accelerator, JComponent comp, int condition) {
        if(accelerator==null)
            return;
        InputMap inputMap = comp.getInputMap(condition);
        ActionMap actionMap = comp.getActionMap();
        Class muActionClass = action.getClass();
        inputMap.put(accelerator, muActionClass);
        actionMap.put(muActionClass, action);
    }

    private static void unregisterActionAccelerator(MuAction action, KeyStroke accelerator, JComponent comp, int condition) {
        if(accelerator==null)
            return;
        InputMap inputMap = comp.getInputMap(condition);
        ActionMap actionMap = comp.getActionMap();
        Class muActionClass = action.getClass();
        inputMap.remove(accelerator);
        actionMap.remove(muActionClass);
    }

    public static void registerActionAccelerators(MuAction action, JComponent comp, int condition) {
    	KeyStroke accelerator = action.getAccelerator();
    	if(accelerator!=null)
    		registerActionAccelerator(action, accelerator, comp, condition);

    	accelerator = action.getAlternateAccelerator();
    	if(accelerator!=null)
    		registerActionAccelerator(action, accelerator, comp, condition);
    }

    private static void unregisterActionAccelerators(MuAction action, JComponent comp, int condition) {
    	KeyStroke accelerator = action.getAccelerator();
    	if(accelerator!=null)
    		unregisterActionAccelerator(action, accelerator, comp, condition);

    	accelerator = action.getAlternateAccelerator();
    	if(accelerator!=null)
    		unregisterActionAccelerator(action, accelerator, comp, condition);
    }
    
    private static void registerActionAccelerators(String actionId, KeyStroke accelerator, KeyStroke alternateAccelerator) {
    	if (equals(accelerator, ActionProperties.getDefaultAccelerator(actionId)) &&
    			equals(alternateAccelerator, ActionProperties.getDefaultAlternativeAccelerator(actionId))) {
    		customPrimaryActionKeymap.remove(actionId);
    		customAlternateActionKeymap.remove(actionId);
    		acceleratorMap.remove(accelerator);
    		acceleratorMap.remove(alternateAccelerator);
    	}
    	else {
    		customPrimaryActionKeymap.put(actionId, accelerator);
    		if (accelerator!=null)
    			acceleratorMap.put(accelerator, actionId);

    		customAlternateActionKeymap.put(actionId, alternateAccelerator);
    		if (alternateAccelerator!=null)
    			acceleratorMap.put(alternateAccelerator, actionId);
    	}
    }

    public static void changeActionAccelerators(String actionId, KeyStroke accelerator, KeyStroke alternateAccelerator) {
    	/*String one = getRegisteredActionIdForKeystroke(accelerator);
    	if (one != null)
    		registerActionAccelerators(one, null, null);
    	String two = getRegisteredActionIdForKeystroke(alternateAccelerator);
    	if (two != null)
    		registerActionAccelerators(two, null, null);*/
    	
    	// Remove old accelerators (primary and alternate) from accelerators map
    	KeyStroke oldAccelator = (KeyStroke)customPrimaryActionKeymap.remove(actionId);
    	if(oldAccelator!=null)
    		acceleratorMap.remove(oldAccelator);

    	oldAccelator = (KeyStroke)customAlternateActionKeymap.remove(actionId);
    	if(oldAccelator!=null)
    		acceleratorMap.remove(oldAccelator);

    	// Register new accelerators
    	registerActionAccelerators(actionId, accelerator, alternateAccelerator);

    	// Update each MainFrame's action instance and input map
    	Vector actionInstances = ActionManager.getActionInstances(actionId);
    	int nbActionInstances = actionInstances.size();
    	for(int i=0; i<nbActionInstances; i++) {
    		MuAction action = (MuAction)actionInstances.elementAt(i);
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
    
    public static Iterator getCustomizedActions() {
    	Set modifiedActions = new HashSet();
    	modifiedActions.addAll(customPrimaryActionKeymap.keySet());
    	modifiedActions.addAll(customAlternateActionKeymap.keySet());
    	return modifiedActions.iterator();
    }
    
    /**
     * @return true if the two KeyStrokes are equal, false otherwise.
     */
    private static boolean equals(KeyStroke first, KeyStroke second) {
    	if (first == null)
    		return second == null;
    	return first.equals(second);
    }
    
    public static void restoreDefault() {
    	customPrimaryActionKeymap.clear();
    	customAlternateActionKeymap.clear();
    	acceleratorMap.clear();
    }
    
    static void registerActions(HashMap primary, HashMap alternate) {
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
}
