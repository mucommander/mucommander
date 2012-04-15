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

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.WeakHashMap;

/**
 * This class is responsible to handle the attributes of CommandBars - their actions, alternate actions and modifier.
 * Every CommandBar should get its attributes from this class, and register in it for receiving attributes modifications.  
 * 
 * @author Arik Hadas
 */
public class CommandBarAttributes {

	/** Command bar actions */
    private static String actionIds[];
    /** Command bar alternate actions */
    private static String alternateActionIds[];
    /** Modifier key that triggers the display of alternate actions when pressed */
    private static KeyStroke modifier;
    
    /** Command bar default actions */
    private static final String DEFAULT_ACTION_IDS[] = 
    {
    	com.mucommander.ui.action.impl.ViewAction.Descriptor.ACTION_ID,
    	com.mucommander.ui.action.impl.EditAction.Descriptor.ACTION_ID,
    	com.mucommander.ui.action.impl.CopyAction.Descriptor.ACTION_ID,
    	com.mucommander.ui.action.impl.MoveAction.Descriptor.ACTION_ID,
    	com.mucommander.ui.action.impl.MkdirAction.Descriptor.ACTION_ID,
    	com.mucommander.ui.action.impl.DeleteAction.Descriptor.ACTION_ID,
    	com.mucommander.ui.action.impl.RefreshAction.Descriptor.ACTION_ID,
    	com.mucommander.ui.action.impl.CloseWindowAction.Descriptor.ACTION_ID
    };
    /** Command bar default alternate actions */
    private static final String DEFAULT_ALTERNATE_ACTION_IDS[] =
    {
    	null,
    	null,
    	com.mucommander.ui.action.impl.LocalCopyAction.Descriptor.ACTION_ID,
    	com.mucommander.ui.action.impl.RenameAction.Descriptor.ACTION_ID,
    	com.mucommander.ui.action.impl.MkfileAction.Descriptor.ACTION_ID,
    	com.mucommander.ui.action.impl.PermanentDeleteAction.Descriptor.ACTION_ID,
    	null,
    	null
    };
    /** Default modifier key that triggers the display of alternate actions when pressed */
    private static KeyStroke DEFAULT_MODIFIER = KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0);
    
    /** Contains all registered command-bar attributes listeners, stored as weak references */
    private static WeakHashMap<CommandBarAttributesListener, ?> listeners = new WeakHashMap<CommandBarAttributesListener, Object>();

    /**
     * This method restore the default command-bar attributes.
     * The attributes are updated only if they are not already equal to the default attributes.
     */
    public static void restoreDefault() {
    	setAttributes(DEFAULT_ACTION_IDS, DEFAULT_ALTERNATE_ACTION_IDS, DEFAULT_MODIFIER);
    }
    
    /**
     * @return true if command-bar attributes equal to the default attributes.
     */
    public static boolean areDefaultAttributes() {
    	if (actionIds != DEFAULT_ACTION_IDS) {
    		int nbActions = actionIds.length;
    		
    		if (nbActions != DEFAULT_ACTION_IDS.length)
    			return false;
    		
    		for (int i=0; i<nbActions; ++i)
    			if (!equals(actionIds[i], DEFAULT_ACTION_IDS[i]))
    				return false;
    	}
    	
    	if (alternateActionIds != DEFAULT_ALTERNATE_ACTION_IDS) {
    		int nbAlternateActions = alternateActionIds.length;
    		
    		if (nbAlternateActions != DEFAULT_ALTERNATE_ACTION_IDS.length)
    			return false;
    		
    		for (int i=0; i<nbAlternateActions; ++i)
    			if (!equals(alternateActionIds[i], DEFAULT_ALTERNATE_ACTION_IDS[i]))
    				return false;
    	}
    	
    	return DEFAULT_MODIFIER == modifier || DEFAULT_MODIFIER.equals(modifier);
    }
    
    private static boolean equals(Object action1, Object action2) {
    	if (action1 == null)
    		return action2 == null;
    	return action1.equals(action2);
    }
    
    ///////////////
    /// setters ///
    ///////////////
    
    /**
     * This method sets command bar actions and modifiers.
     * 
     * @param actionIds          standard command-bar actions.
     * @param alternateActionIds alternate command-bar actions.
     * @param modifier           command-bar modifier.
     */
    public static void setAttributes(String[] actionIds, String[] alternateActionIds, KeyStroke modifier) {
    	CommandBarAttributes.actionIds = actionIds;
    	CommandBarAttributes.alternateActionIds = alternateActionIds;
    	CommandBarAttributes.modifier = modifier;
    	fireAttributesChanged();
    }
    
    ///////////////
    /// getters ///
    ///////////////
    
    public static String[] getActions() {return actionIds;}
    
    public static String[] getAlternateActions() {return alternateActionIds;}
    
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
            for(CommandBarAttributesListener listener : listeners.keySet())
                listener.commandBarAttributeChanged();
        }
    }
}
