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
import java.util.Iterator;
import java.util.LinkedList;
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
    private static HashMap primaryActionKeymap = new HashMap();
    /** Maps action Class instances onto Keystroke instances*/
    private static HashMap alternateActionKeymap = new HashMap();

    /** Maps Keystroke instances onto action Class */
    private static HashMap acceleratorMap = new HashMap();
    
    /** Maps action Class onto default Keystroke instances*/
    private static HashMap defaultPrimaryActionKeymap;
    /** Maps action Class instances onto default Keystroke instances*/
    private static HashMap defaultAlternateActionKeymap;

    ///////////////////
    ///// getters /////
    ///////////////////
    
    public static KeyStroke getAccelerator(Class muActionClass) {
        return (KeyStroke)primaryActionKeymap.get(muActionClass);
    }

    public static KeyStroke getAlternateAccelerator(Class muActionClass) {
        return (KeyStroke)alternateActionKeymap.get(muActionClass);
    }

    public static boolean isKeyStrokeRegistered(KeyStroke ks) {
        return getRegisteredActionClassForKeystroke(ks)!=null;
    }

    public static Class getRegisteredActionClassForKeystroke(KeyStroke ks) {
        return (Class)acceleratorMap.get(ks);
    }

    public static void registerActions(MainFrame mainFrame) {
        JComponent leftTable = mainFrame.getLeftPanel().getFileTable();
        JComponent rightTable = mainFrame.getRightPanel().getFileTable();

        Iterator actionClassesIterator = primaryActionKeymap.keySet().iterator();
        while(actionClassesIterator.hasNext()) {
            MuAction action = ActionManager.getActionInstance((Class)actionClassesIterator.next(), mainFrame);
            ActionKeymap.registerActionAccelerators(action, leftTable, JComponent.WHEN_FOCUSED);
            ActionKeymap.registerActionAccelerators(action, rightTable, JComponent.WHEN_FOCUSED);
        }

        actionClassesIterator = alternateActionKeymap.keySet().iterator();
        while(actionClassesIterator.hasNext()) {
            MuAction action = ActionManager.getActionInstance((Class)actionClassesIterator.next(), mainFrame);
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
    
    private static void registerActionAccelerators(Class muActionClass, KeyStroke accelerator, KeyStroke alternateAccelerator) {
    	primaryActionKeymap.put(muActionClass, accelerator);
    	if (accelerator!=null)
    		acceleratorMap.put(accelerator, muActionClass);

    	alternateActionKeymap.put(muActionClass, alternateAccelerator);
    	if (alternateAccelerator!=null)
    		acceleratorMap.put(alternateAccelerator, muActionClass);
    }
    
    public static void changeActionAccelerators(Class muActionClass, KeyStroke accelerator, KeyStroke alternateAccelerator) {
    	// Remove old accelerators (primary and alternate) from accelerators map
    	KeyStroke oldAccelator = (KeyStroke)primaryActionKeymap.remove(muActionClass);
    	if(oldAccelator!=null)
    		acceleratorMap.remove(oldAccelator);

    	oldAccelator = (KeyStroke)alternateActionKeymap.remove(muActionClass);
    	if(oldAccelator!=null)
    		acceleratorMap.remove(oldAccelator);

    	// Register new accelerators
    	registerActionAccelerators(muActionClass, accelerator, alternateAccelerator);

    	// Update each MainFrame's action instance and input map
    	Vector actionInstances = ActionManager.getActionInstances(muActionClass);
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
    	Iterator classesIterator = primaryActionKeymap.keySet().iterator();
    	LinkedList modifiedClasses = new LinkedList();
    	while(classesIterator.hasNext()) {
    		Class actionClass = (Class) classesIterator.next();
    		if (!equals((KeyStroke) primaryActionKeymap.get(actionClass), (KeyStroke) defaultPrimaryActionKeymap.get(actionClass)) ||
    			!equals((KeyStroke) alternateActionKeymap.get(actionClass), (KeyStroke) defaultAlternateActionKeymap.get(actionClass)))
    			modifiedClasses.add(actionClass);
    	}
    	return modifiedClasses.iterator();
    }
    
    /**
     * @return true if the two KeyStrokes are equal, false otherwise.
     */
    private static boolean equals(KeyStroke first, KeyStroke second) {
    	if (first == null)
    		return second == null;
    	return first.equals(second);
    }
    
    static void setDefaultKeymap(HashMap defaultPrimaryActionKeymap, HashMap defaultAlternateActionKeymap) {
    	ActionKeymap.defaultPrimaryActionKeymap = defaultPrimaryActionKeymap;
    	ActionKeymap.defaultAlternateActionKeymap = defaultAlternateActionKeymap;
    	registerActions(defaultPrimaryActionKeymap, defaultAlternateActionKeymap);
    }
    
    public static void restoreDefault() {
    	registerActions(defaultPrimaryActionKeymap, defaultAlternateActionKeymap);
    }
    
    public static boolean isDefault() { return false; }
    
    static void registerActions(HashMap primary, HashMap alternate) {
    	Iterator classesIterator = primary.keySet().iterator();
    	
    	while(classesIterator.hasNext()) {
    		Class actionClass = (Class) classesIterator.next();
    		
    		// Add the action/keystroke mapping
        	ActionKeymap.changeActionAccelerators(actionClass, 
        			(KeyStroke) primary.get(actionClass), 
        			(KeyStroke) alternate.get(actionClass));
    	}
    }
}
