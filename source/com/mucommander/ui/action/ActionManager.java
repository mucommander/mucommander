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


    public static MucoAction getAction(String mucoActionclassName, MainFrame mainFrame) {
        if(Debug.ON) Debug.trace("called, mucoActionclassName="+mucoActionclassName);

        Hashtable mainFrameActions = (Hashtable)mainFrameActionsMap.get(mainFrame);
        if(mainFrameActions==null) {
            if(Debug.ON) Debug.trace("creating MainFrame action map");

            mainFrameActions = new Hashtable();
            mainFrameActionsMap.put(mainFrame, mainFrameActions);
        }

        // Looks for an existing MucoAction instance used the specified MainFrame
        MucoAction action = (MucoAction)mainFrameActionsMap.get(mucoActionclassName);
        if(action==null) {
            try {
                // Looks for an existing cached Constructor instance
                Constructor actionConstructor = (Constructor)actionConstructors.get(mucoActionclassName);
                if(actionConstructor==null) {
                    if(Debug.ON) Debug.trace("creating constructor");

                    // Not found, retrieve a Constructor instance and caches it
                    Class actionClass = Class.forName(mucoActionclassName);
                    actionConstructor = actionClass.getConstructor(new Class[]{MainFrame.class});
                    actionConstructors.put(mucoActionclassName, actionConstructor);
                }

                // Instanciate the MucoAction class
                action = (MucoAction)actionConstructor.newInstance(new Object[]{mainFrame});
                mainFrameActionsMap.put(mucoActionclassName, action);
            }
            catch(Exception e) {   // Catches ClassNotFoundException, NoSuchMethodException, InstanciationException, IllegalAccessException, InvocateTargetException
                if(Debug.ON) Debug.trace("WARNING: returning null !");

                // Class / constructor could not be instanciated, return null
                return null;
            }
        }

        return action;
    }
}
