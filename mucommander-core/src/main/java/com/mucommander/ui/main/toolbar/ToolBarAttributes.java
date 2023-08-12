/*
 * This file is part of muCommander, http://www.mucommander.com
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

import java.util.WeakHashMap;

import com.mucommander.desktop.ActionType;
import com.mucommander.ui.action.ActionId;

/**
 * This class is responsible to handle the attributes of ToolBars - their actions and separators.
 * Every ToolBar should get its attributes from this class, and register in it for receiving attributes modifications.
 * 
 * @author Arik Hadas
 */
public class ToolBarAttributes {
	
	/** Command bar actions: Class instances or null to signify a separator */
    private static ActionId actionIds[];
    
    private static boolean useDefaultActions = true;
    
    /** Contains all registered toolbar-attributes listeners, stored as weak references */
    private final static WeakHashMap<ToolBarAttributesListener, ?> listeners = new WeakHashMap<>();
    
    /** Default command bar actions: Class instances or null to signify a separator */
    private final static ActionId[] DEFAULT_TOOLBAR_ACTIONS = new ActionId[] {
            ActionId.asToolBarAction(ActionType.NewWindow.getId()),
            ActionId.asToolBarAction(ActionType.NewTab.getId()),
            null,
            ActionId.asToolBarAction(ActionType.GoBack.getId()),
            ActionId.asToolBarAction(ActionType.GoForward.getId()),
            null,
            ActionId.asToolBarAction(ActionType.GoToParent.getId()),
            ActionId.asToolBarAction(ActionType.GoToHome.getId()),
            null,
            ActionId.asToolBarAction(ActionType.Stop.getId()),
            null,
            ActionId.asToolBarAction(ActionType.MarkGroup.getId()),
            ActionId.asToolBarAction(ActionType.UnmarkGroup.getId()),
            null,
            ActionId.asToolBarAction(ActionType.SwapFolders.getId()),
            ActionId.asToolBarAction(ActionType.SetSameFolder.getId()),
            null,
            ActionId.asToolBarAction(ActionType.Pack.getId()),
            ActionId.asToolBarAction(ActionType.Unpack.getId()),
            null,
            ActionId.asToolBarAction(ActionType.Find.getId()),
            null,
            ActionId.asToolBarAction(ActionType.AddBookmark.getId()),
            ActionId.asToolBarAction(ActionType.EditBookmarks.getId()),
            ActionId.asToolBarAction(ActionType.EditCredentials.getId()),
            null,
            ActionId.asToolBarAction(ActionType.ConnectToServer.getId()),
            ActionId.asToolBarAction(ActionType.ShowServerConnections.getId()),
            ActionId.asToolBarAction(ActionType.ToggleTerminal.getId()),
            ActionId.asToolBarAction(ActionType.Email.getId()),
            null,
            ActionId.asToolBarAction(ActionType.RevealInDesktop.getId()),
            ActionId.asToolBarAction(ActionType.ShowFileProperties.getId()),
            null,
            ActionId.asToolBarAction(ActionType.ShowPreferences.getId())
    };

    /**
     * Removes leading and trailing separators (<code>null</code> elements) from the given action Class array, and
     * returns the trimmed action array.
     *
     * @param actions the action Class array to trim.
     * @return the trimmed action Class array, free of leading and trailing separators.
     */
    private static ActionId[] trimActionsArray(ActionId[] actions) {
        int start = 0;
        int end = actions.length;

        while(start<end && actions[start]==null)
            start++;

        if(start==end)
            return new ActionId[]{};

        while(end>start && actions[end-1]==null)
            end--;

        int newLen = end-start;
        ActionId newActions[] = new ActionId[newLen];
        System.arraycopy(actions, start, newActions, 0, newLen);

        return newActions;
    }

    /**
     * Sets the toolbar actions to the given action classes. <code>null</code> elements are used to insert a separator
     * between buttons.
     *
     * @param actions the new toolbar actions classes
     */
    public static void setActions(ActionId[] actions) {
        ToolBarAttributes.actionIds = trimActionsArray(actions);
    	useDefaultActions = false;
    	fireActionsChanged();
    }
    
    /**
     * Check whether the default attributes are used.
     * 
     * @return true if the default attributes are used, false otherwise.
     */
    public static boolean areDefaultAttributes() {
    	if (useDefaultActions)
    		return true;
    		
    	int nbActions = actionIds.length;
    	
    	if (nbActions != DEFAULT_TOOLBAR_ACTIONS.length)
    		return false;
    	
    	for (int i=0; i<nbActions; ++i)
    		if (!equals(actionIds[i], DEFAULT_TOOLBAR_ACTIONS[i]))
    			return false;
    	
    	return true;
    }
    
    private static boolean equals(Object action1, Object action2) {
    	if (action1 == null)
    		return action2 == null;
    	return action1.equals(action2);
    }
    
    /**
     * Returns the actions classes that constitute the toolbar. <code>null</code> elements are used to insert a separator
     * between buttons.
     *
     * @return the action classes that constitute the toolbar.
     */
    public static ActionId[] getActions() {
    	return useDefaultActions ? DEFAULT_TOOLBAR_ACTIONS : actionIds;
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
            for(ToolBarAttributesListener listener : listeners.keySet())
                listener.toolBarActionsChanged();
        }
    }
}
