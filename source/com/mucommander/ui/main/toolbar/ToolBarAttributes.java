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

import com.mucommander.bookmark.BookmarkListener;
import com.mucommander.ui.action.AddBookmarkAction;
import com.mucommander.ui.action.ConnectToServerAction;
import com.mucommander.ui.action.EditBookmarksAction;
import com.mucommander.ui.action.EditCredentialsAction;
import com.mucommander.ui.action.EmailAction;
import com.mucommander.ui.action.GoBackAction;
import com.mucommander.ui.action.GoForwardAction;
import com.mucommander.ui.action.GoToHomeAction;
import com.mucommander.ui.action.GoToParentAction;
import com.mucommander.ui.action.MarkGroupAction;
import com.mucommander.ui.action.NewWindowAction;
import com.mucommander.ui.action.PackAction;
import com.mucommander.ui.action.RevealInDesktopAction;
import com.mucommander.ui.action.RunCommandAction;
import com.mucommander.ui.action.SetSameFolderAction;
import com.mucommander.ui.action.ShowFilePropertiesAction;
import com.mucommander.ui.action.ShowPreferencesAction;
import com.mucommander.ui.action.ShowServerConnectionsAction;
import com.mucommander.ui.action.StopAction;
import com.mucommander.ui.action.SwapFoldersAction;
import com.mucommander.ui.action.UnmarkGroupAction;
import com.mucommander.ui.action.UnpackAction;

/**
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
