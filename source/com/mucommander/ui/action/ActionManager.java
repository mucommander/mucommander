package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.Debug;

import java.util.Hashtable;
import java.util.WeakHashMap;
import java.lang.reflect.Constructor;

/**
 * @author Maxence Bernard
 */
public class ActionManager {

    private static Hashtable actionConstructors = new Hashtable();

    private static WeakHashMap mainFrameActionsMap = new WeakHashMap();


    public static MucoAction getActionInstance(String actionClassName, MainFrame mainFrame) {
        if(Debug.ON) Debug.trace("called, actionClassName="+actionClassName);

        try {
            return getActionInstance(Class.forName(actionClassName), mainFrame);
        }
        catch(ClassNotFoundException e) {
            if(Debug.ON) Debug.trace("WARNING: could not resolve class "+actionClassName+"returning null !");
            return null;
        }
    }


    public static MucoAction getActionInstance(Class actionClass, MainFrame mainFrame) {
        if(Debug.ON) Debug.trace("called, MucoAction class="+actionClass);

        Hashtable mainFrameActions = (Hashtable)mainFrameActionsMap.get(mainFrame);
        if(mainFrameActions==null) {
            if(Debug.ON) Debug.trace("creating MainFrame action map");

            mainFrameActions = new Hashtable();
            mainFrameActionsMap.put(mainFrame, mainFrameActions);
        }

        // Looks for an existing MucoAction instance used by the specified MainFrame
        MucoAction action = (MucoAction)mainFrameActions.get(actionClass);
        if(action==null) {
            try {
                // Looks for an existing cached Constructor instance
                Constructor actionConstructor = (Constructor)actionConstructors.get(actionClass);
                if(actionConstructor==null) {
                    if(Debug.ON) Debug.trace("creating constructor");

                    // Not found, retrieve a Constructor instance and caches it
                    actionConstructor = actionClass.getConstructor(new Class[]{MainFrame.class});
                    actionConstructors.put(actionClass, actionConstructor);

                    if(Debug.ON) Debug.trace("nb constructors = "+actionConstructors.size());
                }

                // Instanciate the MucoAction class
                if(Debug.ON) Debug.trace("creating instance");

                action = (MucoAction)actionConstructor.newInstance(new Object[]{mainFrame});
                mainFrameActions.put(actionClass, action);

                if(Debug.ON) Debug.trace("nb instances = "+mainFrameActions.size());
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

        return action;
    }


    public static void performAction(Class actionClass, MainFrame mainFrame) {
        performAction(getActionInstance(actionClass, mainFrame));
    }

    public static void performAction(MucoAction action) {
        action.performAction(action.getMainFrame());
    }
}
