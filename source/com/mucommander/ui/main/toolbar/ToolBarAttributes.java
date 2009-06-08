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

package com.mucommander.ui.main.toolbar;

import java.util.Iterator;
import java.util.WeakHashMap;

import com.mucommander.ui.action.impl.AddBookmarkAction;
import com.mucommander.ui.action.impl.ConnectToServerAction;
import com.mucommander.ui.action.impl.EditBookmarksAction;
import com.mucommander.ui.action.impl.EditCredentialsAction;
import com.mucommander.ui.action.impl.EmailAction;
import com.mucommander.ui.action.impl.GoBackAction;
import com.mucommander.ui.action.impl.GoForwardAction;
import com.mucommander.ui.action.impl.GoToHomeAction;
import com.mucommander.ui.action.impl.GoToParentAction;
import com.mucommander.ui.action.impl.MarkGroupAction;
import com.mucommander.ui.action.impl.NewWindowAction;
import com.mucommander.ui.action.impl.PackAction;
import com.mucommander.ui.action.impl.RevealInDesktopAction;
import com.mucommander.ui.action.impl.RunCommandAction;
import com.mucommander.ui.action.impl.SetSameFolderAction;
import com.mucommander.ui.action.impl.ShowFilePropertiesAction;
import com.mucommander.ui.action.impl.ShowPreferencesAction;
import com.mucommander.ui.action.impl.ShowServerConnectionsAction;
import com.mucommander.ui.action.impl.StopAction;
import com.mucommander.ui.action.impl.SwapFoldersAction;
import com.mucommander.ui.action.impl.UnmarkGroupAction;
import com.mucommander.ui.action.impl.UnpackAction;

/**
 * This class is responsible to handle the attributes of ToolBars - their actions and separators.
 * Every ToolBar should get its attributes from this class, and register in it for receiving attributes modifications.
 * 
 * @author Arik Hadas
 */
public class ToolBarAttributes {
	
	/** Default command bar actions: Class instances or null to signify a separator */
    private static Class defaultActions[];
	
	/** Command bar actions: Class instances or null to signify a separator */
    private static Class actions[];
    
    private static boolean useDefaultActions = true;
    
    /** Contains all registered toolbar-attributes listeners, stored as weak references */
    private static WeakHashMap listeners = new WeakHashMap();
    
    /**
     * create toolbar's default actions.
     */
    static {
    	defaultActions = new Class[32];
    	defaultActions[0]  = NewWindowAction.class;
    	defaultActions[1]  = GoBackAction.class;
    	defaultActions[2]  = GoForwardAction.class;
    	defaultActions[3]  = GoToParentAction.class;
    	defaultActions[4]  = null;
    	defaultActions[5]  = GoToHomeAction.class;
    	defaultActions[6]  = null;
    	defaultActions[7]  = StopAction.class;
    	defaultActions[8]  = null;
    	defaultActions[9]  = MarkGroupAction.class;
    	defaultActions[10] = UnmarkGroupAction.class;
    	defaultActions[11] = null;
    	defaultActions[12] = SwapFoldersAction.class;
    	defaultActions[13] = SetSameFolderAction.class;
    	defaultActions[14] = null;
    	defaultActions[15] = PackAction.class;
    	defaultActions[16] = UnpackAction.class;
    	defaultActions[17] = null;
    	defaultActions[18] = AddBookmarkAction.class;
    	defaultActions[19] = EditBookmarksAction.class;
    	defaultActions[20] = EditCredentialsAction.class;
    	defaultActions[21] = null;
    	defaultActions[22] = ConnectToServerAction.class;
    	defaultActions[23] = ShowServerConnectionsAction.class;
    	defaultActions[24] = RunCommandAction.class;
    	defaultActions[25] = EmailAction.class;
    	defaultActions[26] = null;
    	defaultActions[27] = RevealInDesktopAction.class;
    	defaultActions[28] = ShowFilePropertiesAction.class;
    	defaultActions[29] = null;
    	defaultActions[30] = ShowPreferencesAction.class;
    	defaultActions[31] = null;
    }
    
    /**
     * Sets the toolbar's actions to the given action classes.
     */
    public static void setActions(Class[] actions) {
    	ToolBarAttributes.actions = actions;
    	useDefaultActions = false;
    	fireActionsChanged();
    }
    
    /**
     * Returns toolbar's actions.
     */
    public static Class[] getActions() {
    	return useDefaultActions ? defaultActions : actions;
    }
    
    // - Listeners -------------------------------------------------------------
    // -------------------------------------------------------------------------
    public static void addToolBarAttributesListener(ToolBarAttributesListener listener) {
    	synchronized(listeners) {listeners.put(listener, null);}
    }
    
    public static void removeToolBarAttributesListener(ToolBarAttributesListener listener) {
    	synchronized(listeners) {listeners.remove(listener);}
    }
    
    public static void fireActionsChanged() {
    	synchronized(listeners) {
            // Iterate on all listeners
            Iterator iterator = listeners.keySet().iterator();
            while(iterator.hasNext())
                ((ToolBarAttributesListener)iterator.next()).ToolBarActionsChanged();
        }
    }
}
