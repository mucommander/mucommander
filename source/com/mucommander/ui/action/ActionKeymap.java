package com.mucommander.ui.action;

import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;
import com.mucommander.Debug;
import com.mucommander.ui.MainFrame;

import javax.swing.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;


/**
 * @author Maxence Bernard
 */
public class ActionKeymap implements ContentHandler {

    private static Hashtable primaryActionKeymap;
    private static Hashtable alternateActionKeymap;

    private final static String ACTION_KEYMAP_FILE_PATH = "/action_keymap.xml";

    static {
        // Todo: move this call to Launcher
        parseActionKeymap();
    }

    public static void parseActionKeymap() {
        new ActionKeymap();
    }


    public static KeyStroke getAccelerator(Class mucoActionClass) {
        return (KeyStroke)primaryActionKeymap.get(mucoActionClass);
    }

    public static KeyStroke getAlternateAccelerator(Class mucoActionClass) {
        return (KeyStroke)alternateActionKeymap.get(mucoActionClass);
    }


    public static void registerActions(MainFrame mainFrame) {
        JComponent table1 = mainFrame.getFolderPanel1().getFileTable();
        JComponent table2 = mainFrame.getFolderPanel2().getFileTable();

        Enumeration actionClasses = primaryActionKeymap.keys();
        while(actionClasses.hasMoreElements()) {
            MucoAction action = ActionManager.getActionInstance((Class)actionClasses.nextElement(), mainFrame);
            ActionKeymap.registerActionAccelerators(action, table1, JComponent.WHEN_FOCUSED);
            ActionKeymap.registerActionAccelerators(action, table2, JComponent.WHEN_FOCUSED);
        }

        actionClasses = alternateActionKeymap.keys();
        while(actionClasses.hasMoreElements()) {
            MucoAction action = ActionManager.getActionInstance((Class)actionClasses.nextElement(), mainFrame);
            ActionKeymap.registerActionAccelerators(action, table1, JComponent.WHEN_FOCUSED);
            ActionKeymap.registerActionAccelerators(action, table2, JComponent.WHEN_FOCUSED);
        }
    }


    public static void registerAction(MainFrame mainFrame, MucoAction action) {
        registerActionAccelerators(action, mainFrame.getFolderPanel1().getFileTable(), JComponent.WHEN_FOCUSED);
        registerActionAccelerators(action, mainFrame.getFolderPanel2().getFileTable(), JComponent.WHEN_FOCUSED);
    }

    public static void unregisterAction(MainFrame mainFrame, MucoAction action) {
        unregisterActionAccelerators(action, mainFrame.getFolderPanel1().getFileTable(), JComponent.WHEN_FOCUSED);
        unregisterActionAccelerators(action, mainFrame.getFolderPanel2().getFileTable(), JComponent.WHEN_FOCUSED);
    }


    public static void registerActionAccelerator(MucoAction action, KeyStroke accelerator, JComponent comp, int condition) {
        if(accelerator==null)
            return;
        InputMap inputMap = comp.getInputMap(condition);
        ActionMap actionMap = comp.getActionMap();
        Class mucoActionClass = action.getClass();
        inputMap.put(accelerator, mucoActionClass);
        actionMap.put(mucoActionClass, action);
    }

    public static void unregisterActionAccelerator(MucoAction action, KeyStroke accelerator, JComponent comp, int condition) {
        if(accelerator==null)
            return;
        InputMap inputMap = comp.getInputMap(condition);
        ActionMap actionMap = comp.getActionMap();
        Class mucoActionClass = action.getClass();
        inputMap.remove(accelerator);
        actionMap.remove(mucoActionClass);
    }


    public static void registerActionAccelerators(MucoAction action, JComponent comp, int condition) {
        KeyStroke accelerator = action.getAccelerator();
        if(accelerator==null)
            return;

        registerActionAccelerator(action, accelerator, comp, condition);

        accelerator = action.getAlternateAccelerator();
        if(accelerator!=null)
            registerActionAccelerator(action, accelerator, comp, condition);
    }

    public static void unregisterActionAccelerators(MucoAction action, JComponent comp, int condition) {
        KeyStroke accelerator = action.getAccelerator();
        if(accelerator==null)
            return;

        unregisterActionAccelerator(action, accelerator, comp, condition);

        accelerator = action.getAlternateAccelerator();
        if(accelerator!=null)
            unregisterActionAccelerator(action, accelerator, comp, condition);
    }


    public static void changeActionAccelerators(Class mucoActionClass, KeyStroke accelerator, KeyStroke alternateAccelerator) {
        primaryActionKeymap.put(mucoActionClass, accelerator);
        alternateActionKeymap.put(mucoActionClass, accelerator);

        Vector actionInstances = ActionManager.getActionInstances(mucoActionClass);
        int nbActionInstances = actionInstances.size();
        for(int i=0; i<nbActionInstances; i++) {
            MucoAction action = (MucoAction)actionInstances.elementAt(i);
            MainFrame mainFrame = action.getMainFrame();

            unregisterAction(mainFrame, action);

            action.setAccelerator(accelerator);
            action.setAlternateAccelerator(alternateAccelerator);

            registerAction(mainFrame, action);
        }
    }


    private ActionKeymap() {
        // Parse action keymap file
        try {
            new Parser().parse(getClass().getResourceAsStream(ACTION_KEYMAP_FILE_PATH), this, "UTF-8");
        }
        catch(Exception e) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Error: unable to load action_keymap.xml file "+e);
        }
    }


    ///////////////////////////////////
    // ContentHandler implementation //
    ///////////////////////////////////

    public void startDocument() throws Exception {
        primaryActionKeymap = new Hashtable();
        alternateActionKeymap = new Hashtable();
    }

    public void endDocument() throws Exception {
    }

    public void startElement(String uri, String name, Hashtable attValues, Hashtable attURIs) throws Exception {
        if(name.equals("action")) {
            String actionClassName = (String)attValues.get("class");
            if(actionClassName==null) {
                if(Debug.ON) Debug.trace("Error: no 'class' attribute specified in 'action' element");
                return;
            }

            Class actionClass;
            try {
                actionClass = Class.forName(actionClassName);
            }
            catch(ClassNotFoundException e) {
                if(Debug.ON) Debug.trace("Error: could not resolve class "+actionClassName);
                return;
            }

            // Primary keystroke
            String keyStrokeString = (String)attValues.get("keystroke");
            if(keyStrokeString==null) {
                if(Debug.ON) Debug.trace("Error: no 'keystroke' attribute specified in 'action' element");
                return;
            }

            KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeString);
            if(keyStroke==null) {
                if(Debug.ON) Debug.trace("Error: specified keystroke could not be resolved: "+keyStrokeString);
                return;
            }

            if(Debug.ON) Debug.trace("associating "+keyStroke+" accelerator with "+actionClass);

            primaryActionKeymap.put(actionClass, keyStroke);

            // Alternate keystroke (if any)
            keyStrokeString = (String)attValues.get("alt_keystroke");
            if(keyStrokeString!=null) {
                keyStroke = KeyStroke.getKeyStroke(keyStrokeString);
                if(keyStroke==null) {
                    if(Debug.ON) Debug.trace("Error: specified alternate keystroke could not be resolved: "+keyStrokeString);
                    return;
                }

                if(Debug.ON) Debug.trace("associating "+keyStroke+" alternate accelerator with "+actionClass);

                alternateActionKeymap.put(actionClass, keyStroke);
            }
        }
    }

    public void endElement(String uri, String name) throws Exception {
    }

    public void characters(String s) throws Exception {
    }
}
