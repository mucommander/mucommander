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

import java.util.Iterator;
import java.util.WeakHashMap;

import javax.swing.KeyStroke;

/**
 * This class is responsible to handle the attributes of CommandBars - their actions, alternate actions and modifier.
 * Every CommandBar should get its attributes from this class, and register in it for receiving attributes modifications.  
 * 
 * @author Arik Hadas
 */
public class CommandBarAttributes {

	/** Command bar actions */
    private static Class actions[];
    /** Command bar alternate actions */
    private static Class alternateActions[];
    /** Modifier key that triggers the display of alternate actions when pressed */
    private static KeyStroke modifier;
    
    /** Command bar default actions */
    private static Class defaultActions[];
    /** Command bar default alternate actions */
    private static Class defaultAlternateActions[];
    /** Default modifier key that triggers the display of alternate actions when pressed */
    private static KeyStroke defaultModifier;
    
    /** Contains all registered command-bar attributes listeners, stored as weak references */
    private static WeakHashMap listeners = new WeakHashMap();

    /**
     * This method restore the default command-bar attributes.
     * The attributes are updated only if they are not already equal to the default attributes.
     */
    public static void restoreDefault() {
    	if (!isDefault())
    		setAttributes(defaultActions, defaultAlternateActions, defaultModifier);
    }
    
    /**
     * 
     * @return true if command-bar attributes equal to the default attributes.
     */
    public static boolean isDefault() {
    	if (actions != defaultActions) {
    		int nbActions = actions.length;
    		for (int i=0; i<nbActions; ++i)
    			if (!equals(actions[i], defaultActions[i]))
    				return false;
    	}
    	
    	if (alternateActions != defaultAlternateActions) {
    		int nbAlternateActions = alternateActions.length;
    		for (int i=0; i<nbAlternateActions; ++i)
    			if (!equals(alternateActions[i], defaultAlternateActions[i]))
    				return false;
    	}
    	
    	return defaultModifier == modifier || defaultModifier.equals(modifier);
    }
    
    private static boolean equals(Class action1, Class action2) {
    	if (action1 == null)
    		return action2 == null;
    	return action1.equals(action2);
    }
    
    ///////////////
    /// setters ///
    ///////////////
    
    /**
     * This method is used to set the default attributes of command-bar.
     * This method should be called only once, when parsing the command-bar resource file, as it 
     * updates the command-bar with the given default values.
     */
    static void setDefaultAttributes(Class[] defaultActions, Class[] defaultAlternateActions, KeyStroke defaultModifier) {
    	CommandBarAttributes.defaultActions = defaultActions;
    	CommandBarAttributes.defaultAlternateActions = defaultAlternateActions;
    	CommandBarAttributes.defaultModifier = defaultModifier;
    	setAttributes(defaultActions, defaultAlternateActions, defaultModifier);
    }
    
    /**
     * This method is used to set
     * 
     * @param actions          standard command-bar actions.
     * @param alternateActions alternate command-bar actions.
     * @param modifier         command-bar modifier.
     */
    public static void setAttributes(Class[] actions, Class[] alternateActions, KeyStroke modifier) {
    	CommandBarAttributes.actions = actions;
    	CommandBarAttributes.alternateActions = alternateActions;
    	CommandBarAttributes.modifier = modifier;
    	fireAttributesChanged();
    }
    
    ///////////////
    /// getters ///
    ///////////////
        
    public static Class[] getActions() {return actions;}
    
    public static Class[] getAlternateActions() {return alternateActions;}
    
    public static KeyStroke getModifier() {return modifier;}
    
    
    // - Listeners -------------------------------------------------------------
    // -------------------------------------------------------------------------
    public static void addCommandBarAttributesListener(CommandBarAttributesListener listener) {
    	synchronized(listeners) {listeners.put(listener, null);}
    }
    
    public static void removeCommandBarAttributesListener(CommandBarAttributesListener listener) {
    	synchronized(listeners) {listeners.remove(listener);}
    }
    
    protected static void fireAttributesChanged() {
    	synchronized(listeners) {
            // Iterate on all listeners
            Iterator iterator = listeners.keySet().iterator();
            while(iterator.hasNext())
                ((CommandBarAttributesListener)iterator.next()).commandBarAttributeChanged();
        }
    }
}
