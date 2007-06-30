/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

import com.mucommander.Debug;
import com.mucommander.ui.MainFrame;

import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.WeakHashMap;

/**
 * ActionManager provides methods to retrieve {@link MucoAction} instances and invoke them. It keeps track of all the
 * action instances it has created and allows them to be reused whithin a {@link MainFrame}.
 *
 * <p>MucoAction subclasses should not be instanciated directly, <code>getActionInstance</code>
 * methods should be used instead. Using ActionManager to retrieve a MucoAction ensures that only one instance
 * exists for a given {@link MainFrame}. This is particularly important because actions are stateful and can be used
 * in several components of a MainFrame at the same time; if an action's state changes, the change must be reflected
 * everywhere the action is used. It is also important for performance reasons: sharing one action throughout a
 * {@link MainFrame} saves some memory and also CPU cycles as some actions listen to particular events to change
 * their state accordingly.</p>
 *
 * @see MucoAction
 * @see ActionDescriptor
 * @see ActionKeymap
 * @author Maxence Bernard
 */
public class ActionManager {

    /** MucoAction class -> constructor map */
    private static Hashtable actionConstructors = new Hashtable();

    /** MainFrame -> MucoAction map */
    private static WeakHashMap mainFrameActionsMap = new WeakHashMap();


    /**
     * Convenience method that returns an instance of the given MucoAction class, and associated with the specified
     * MainFrame. This method creates an ActionDescriptor with no initial property, passes it to
     * {@link #getActionInstance(ActionDescriptor, MainFrame)} and returns the MucoAction instance.
     *
     * @param actionClass the MucoAction class to instanciate
     * @param mainFrame the MainFrame instance the action belongs to
     * @return a MucoAction instance matching the given MucoAction Class and MainFrame, <code>null</code> if the
     * class could not be found or could not be instanciated.
     */
    public static MucoAction getActionInstance(Class actionClass, MainFrame mainFrame) {
        return getActionInstance(new ActionDescriptor(actionClass), mainFrame);
    }


    /**
     * Returns an instance of the MucoAction class denoted by the given ActionDescriptor, for the specified MainFrame.
     * If an existing instance corresponding to the same ActionDescriptor and MainFrame is found, it is simply returned.
     * If no matching instance could be found, a new instance is created, added to the internal action instances map
     * (for further use) and returned.
     * If the MucoAction denoted by the specified ActionDescriptor cannot be found or cannot be instanciated,
     * <code>null</code> is returned.
     *
     * @param actionDescriptor a descriptor of the action class to instanciate with initial properties
     * @param mainFrame the MainFrame instance the action belongs to
     * @return a MucoAction instance matching the given ActionDescriptor and MainFrame, <code>null</code> if the
     * MucoAction class denoted by the ActionDescriptor could not be found or could not be instanciated.
     */
    public static MucoAction getActionInstance(ActionDescriptor actionDescriptor, MainFrame mainFrame) {
//        if(Debug.ON) Debug.trace("called, actionDescriptor = "+actionDescriptor, 5);

        Hashtable mainFrameActions = (Hashtable)mainFrameActionsMap.get(mainFrame);
        if(mainFrameActions==null) {
//            if(Debug.ON) Debug.trace("creating MainFrame action map");

            mainFrameActions = new Hashtable();
            mainFrameActionsMap.put(mainFrame, mainFrameActions);
        }

        // Looks for an existing MucoAction instance used by the specified MainFrame
        MucoAction action = (MucoAction)mainFrameActions.get(actionDescriptor);
        if(action==null) {
            Class actionClass = actionDescriptor.getActionClass();

            try {
                // Looks for an existing cached Constructor instance
                Constructor actionConstructor = (Constructor)actionConstructors.get(actionClass);
                if(actionConstructor==null) {
//                    if(Debug.ON) Debug.trace("creating constructor");

                    // Not found, retrieve a Constructor instance and caches it
                    actionConstructor = actionClass.getConstructor(new Class[]{MainFrame.class, Hashtable.class});
                    actionConstructors.put(actionClass, actionConstructor);

//                    if(Debug.ON) Debug.trace("nb constructors = "+actionConstructors.size());
                }

//                if(Debug.ON) Debug.trace("creating instance");

                Hashtable properties = actionDescriptor.getInitProperties();
                // If no properties hashtable is specified in the action descriptor
                if(properties==null) {
                    properties = new Hashtable();
                }
                // else clone the hashtable to ensure that it doesn't get modified by action instances.
                // Since cloning is an expensive operation, this is done only if the hashtable is not empty.
                else if(!properties.isEmpty()) {
                    properties = (Hashtable)properties.clone();
                }

                // Instanciate the MucoAction class
                action = (MucoAction)actionConstructor.newInstance(new Object[]{mainFrame, properties});
                mainFrameActions.put(actionDescriptor, action);

//                if(Debug.ON) Debug.trace("nb action instances = "+mainFrameActions.size());
            }
            catch(Exception e) {   // Catches ClassNotFoundException, NoSuchMethodException, InstanciationException, IllegalAccessException, InvocateTargetException
                if(Debug.ON) {
                    Debug.trace("ERROR: caught exception "+e+" for class "+actionClass);
                    e.printStackTrace();
                }

                // Class / constructor could not be instanciated, return null
                return null;
            }
        }
//        else {
//            if(Debug.ON) Debug.trace("found existing action instance: "+action);
//        }

        return action;
    }


    /**
     * Returns a Vector of all MucoAction instances matching the specified Class.
     *
     * @param mucoActionClass the MucoAction class to compare instances against
     * @return  a Vector of all MucoAction instances matching the specified Class
     */
    public static Vector getActionInstances(Class mucoActionClass) {
        Vector actionInstances = new Vector();

        // Iterate on all MainFrame instances
        Iterator mainFrameActions = mainFrameActionsMap.values().iterator();
        while(mainFrameActions.hasNext()) {
            Iterator actions = ((Hashtable)mainFrameActions.next()).values().iterator();
            // Iterate on all the MainFrame's actions
            while(actions.hasNext()) {
                MucoAction action = (MucoAction)actions.next();
                if(action.getClass().equals(mucoActionClass)) {
                    // Found an action matching the specified class
                    actionInstances.add(action);
                    // Jump to the next MainFrame
                    break;
                }
            }
        }

// if(Debug.ON) Debug.trace("returning "+actionInstances);
        return actionInstances;
    }


    /**
     * Convenience method that retrieves an instance of the MucoAction denoted by the given Class and associated
     * with the given {@link MainFrame} and calls {@link MucoAction#performAction()} on it.
     * Returns <code>true</code> if an instance of the action could be retrieved and performed, <code>false</code>
     * if the MucoAction could not be found or could not be instanciated.
     *
     * @param actionClass the class of the MucoAction to perform
     * @param mainFrame the MainFrame the action belongs to
     * @return true if the action instance could be retrieved and the action performed, false otherwise 
     */
    public static boolean performAction(Class actionClass, MainFrame mainFrame) {
        return performAction(new ActionDescriptor(actionClass), mainFrame);
    }


    /**
     * Convenience method that retrieves an instance of the MucoAction denoted by the given {@link ActionDescriptor}
     * and associated with the given {@link MainFrame} and calls {@link MucoAction#performAction()} on it.
     * Returns <code>true</code> if an instance of the action could be retrieved and performed, <code>false</code>
     * if the MucoAction could not be found or could not be instanciated.
     *
     * @param actionDescriptor the ActionDescriptor of the action to perform
     * @param mainFrame the MainFrame the action belongs to
     * @return true if the action instance could be retrieved and the action performed, false otherwise
     */
    public static boolean performAction(ActionDescriptor actionDescriptor, MainFrame mainFrame) {
        MucoAction action = getActionInstance(actionDescriptor, mainFrame);

        if(action==null)
            return false;

        action.performAction();

        return true;
    }
}
