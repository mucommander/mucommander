package com.mucommander.ui.action;

import com.mucommander.Debug;
import com.mucommander.ui.MainFrame;

import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.WeakHashMap;

/**
 * @author Maxence Bernard
 */
public class ActionManager {

    private static Hashtable actionConstructors = new Hashtable();

    private static WeakHashMap mainFrameActionsMap = new WeakHashMap();


    public static MucoAction getActionInstance(Class actionClass, MainFrame mainFrame) {
        return getActionInstance(new ActionDescriptor(actionClass), mainFrame);
    }


    public static MucoAction getActionInstance(ActionDescriptor actionDescriptor, MainFrame mainFrame) {
//        if(Debug.ON) Debug.trace("called, actionDescriptor = "+actionDescriptor, 5);

        Hashtable mainFrameActions = (Hashtable)mainFrameActionsMap.get(mainFrame);
        if(mainFrameActions==null) {
            if(Debug.ON) Debug.trace("creating MainFrame action map");

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
                    if(Debug.ON) Debug.trace("creating constructor");

                    // Not found, retrieve a Constructor instance and caches it
                    actionConstructor = actionClass.getConstructor(new Class[]{MainFrame.class, Hashtable.class});
                    actionConstructors.put(actionClass, actionConstructor);

                    if(Debug.ON) Debug.trace("nb constructors = "+actionConstructors.size());
                }

                if(Debug.ON) Debug.trace("creating instance");

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

                if(Debug.ON) Debug.trace("nb action instances = "+mainFrameActions.size());
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


    public static boolean performAction(Class actionClass, MainFrame mainFrame) {
        return performAction(new ActionDescriptor(actionClass), mainFrame);
    }

    public static boolean performAction(ActionDescriptor actionDescriptor, MainFrame mainFrame) {
        MucoAction action = getActionInstance(actionDescriptor, mainFrame);

        if(action==null)
            return false;

        action.performAction();

        return true;
    }
}
