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

import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.WeakHashMap;

import javax.swing.KeyStroke;

import com.mucommander.ui.action.CloseWindowAction;
import com.mucommander.ui.action.CopyAction;
import com.mucommander.ui.action.DeleteAction;
import com.mucommander.ui.action.EditAction;
import com.mucommander.ui.action.LocalCopyAction;
import com.mucommander.ui.action.MkdirAction;
import com.mucommander.ui.action.MkfileAction;
import com.mucommander.ui.action.MoveAction;
import com.mucommander.ui.action.PermanentDeleteAction;
import com.mucommander.ui.action.RefreshAction;
import com.mucommander.ui.action.RenameAction;
import com.mucommander.ui.action.ViewAction;

/**
 * 
 * @author Arik Hadas
 */
public class CommandBarAttributes {

	/** Default command bar actions */
	private static Class[] defaultActions;
	
	/** Command bar actions */
    private static Class actions[];
    
    /** Default command bar alternate actions */
    private static Class defaultAlternateActions[];
    
    /** Command bar alternate actions */
    private static Class alternateActions[];
    
    private static boolean useDefaultActions = true;
    
    /** Default modifier key that triggers the display of alternate actions when pressed */
    private static KeyStroke defaultModifier = KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0);
    
    /** Modifier key that triggers the display of alternate actions when pressed */
    private static KeyStroke modifier;
    
    private static boolean useDefaultModifier = true;
    
    /** Contains all registered command-bar attributes listeners, stored as weak references */
    private static WeakHashMap listeners = new WeakHashMap();

    static {
    	defaultActions = new Class[8];
    	defaultActions[0] = ViewAction.class;
    	defaultActions[1] = EditAction.class;
    	defaultActions[2] = CopyAction.class;
    	defaultActions[3] = MoveAction.class;
    	defaultActions[4] = MkdirAction.class;
    	defaultActions[5] = DeleteAction.class;
    	defaultActions[6] = RefreshAction.class;
    	defaultActions[7] = CloseWindowAction.class;

    	defaultAlternateActions = new Class[8];
    	defaultAlternateActions[0] = null;
    	defaultAlternateActions[1] = null;
    	defaultAlternateActions[2] = LocalCopyAction.class;
    	defaultAlternateActions[3] = RenameAction.class;
    	defaultAlternateActions[4] = MkfileAction.class;
    	defaultAlternateActions[5] = PermanentDeleteAction.class;
    	defaultAlternateActions[6] = null;
    	defaultAlternateActions[7] = null;
    }
    
    public static void setActions(Class[] actions, Class[] alternateActions) {
    	CommandBarAttributes.actions = actions;
    	CommandBarAttributes.alternateActions = alternateActions;
    	useDefaultActions = false;
    }
    
    public static void setModifier(KeyStroke modifier) {
    	CommandBarAttributes.modifier = modifier;
    	useDefaultModifier = false;
    }
    
    public static Class[] getActions() {
    	return useDefaultActions ? defaultActions : actions;
    }
    
    public static Class[] getAlternateActions() {
    	return useDefaultActions ? defaultAlternateActions : alternateActions;
    }
    
    public static KeyStroke getModifier() {
    	return useDefaultModifier ? defaultModifier : modifier;
    }
    
    // - Listeners -------------------------------------------------------------
    // -------------------------------------------------------------------------
    public static void addCommandBarAttributesListener(CommandBarAttributesListener listener) {
    	synchronized(listeners) {listeners.put(listener, null);}
    }
    
    public static void removeCommandBarAttributesListener(CommandBarAttributesListener listener) {
    	synchronized(listeners) {listeners.remove(listener);}
    }
    
    public static void fireActionsChanged() {
    	synchronized(listeners) {
            // Iterate on all listeners
            Iterator iterator = listeners.keySet().iterator();
            while(iterator.hasNext())
                ((CommandBarAttributesListener)iterator.next()).CommandBarActionsChanged();
        }
    }
    
    public static void fireModifierChanged() {
    	synchronized(listeners) {
            // Iterate on all listeners
            Iterator iterator = listeners.keySet().iterator();
            while(iterator.hasNext())
                ((CommandBarAttributesListener)iterator.next()).CommandBarModifierChanged();
        }
    }
}
