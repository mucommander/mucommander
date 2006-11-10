package com.mucommander.ui.action;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileToolkit;
import com.mucommander.io.BackupInputStream;
import com.mucommander.ui.MainFrame;
import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


/**
 * @author Maxence Bernard
 */
public class ActionKeymap implements ContentHandler {

    private static Hashtable primaryActionKeymap = new Hashtable();
    private static Hashtable alternateActionKeymap = new Hashtable();

    private static Hashtable acceleratorMap = new Hashtable();

    /** Default action keymap filename */
    private final static String DEFAULT_ACTION_KEYMAP_FILENAME = "action_keymap.xml";
    /** Path to the action keymap resource file within the application JAR file */
    private final static String ACTION_KEYMAP_RESOURCE_PATH = "/"+DEFAULT_ACTION_KEYMAP_FILENAME;

    /** Action keymap file used when calling {@link #loadActionKeyMap()} */
    private static AbstractFile actionKeyMapFile = FileFactory.getFile(PlatformManager.getPreferencesFolder().getAbsolutePath()+"/"+DEFAULT_ACTION_KEYMAP_FILENAME);


    /**
     * Sets the path to the user action keymap file to be loaded when calling {@link #loadActionKeyMap()}.
     * By default, this file is {@link #DEFAULT_ACTION_KEYMAP_FILENAME} within the preferences folder.
     *
     * @param filePath path to the action keymap file
     */
    public static void setActionKeyMapFile(String filePath) {
        AbstractFile file = FileFactory.getFile(filePath);
        if(file!=null)
            actionKeyMapFile = file;
    }


    /**
     * Loads the action keymap files: loads the one contained in the JAR file first, and then the user's one.
     * This means any new action in the JAR action keymap (when a new version is released) will have the default
     * keyboard mapping, but the keyboard mappings customized by the user in the user's action keymap will override
     * the ones from the JAR action keymap.
     *
     * <p>This method must be called before requesting and registering any action.
     */
    public static void loadActionKeyMap() {
        new ActionKeymap();
    }


    public static KeyStroke getAccelerator(Class mucoActionClass) {
        return (KeyStroke)primaryActionKeymap.get(mucoActionClass);
    }

    public static KeyStroke getAlternateAccelerator(Class mucoActionClass) {
        return (KeyStroke)alternateActionKeymap.get(mucoActionClass);
    }


    public static boolean isKeyStrokeRegistered(KeyStroke ks) {
//        Collection keys = primaryActionKeymap.values();
//        if(keys.contains(ks))
//            return true;
//
//        keys = alternateActionKeymap.values();
//        if(keys.contains(ks))
//            return true;
//
//        return false;

        return getRegisteredActionClassForKeystroke(ks)!=null;
    }


    public static Class getRegisteredActionClassForKeystroke(KeyStroke ks) {
        return (Class)acceleratorMap.get(ks);
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
        // Remove old accelerators (primary and alternate) from accelerators map
        KeyStroke oldAccelator = (KeyStroke)primaryActionKeymap.get(mucoActionClass);
        if(oldAccelator!=null)
            acceleratorMap.remove(oldAccelator);

        oldAccelator = (KeyStroke)alternateActionKeymap.get(mucoActionClass);
        if(oldAccelator!=null)
            acceleratorMap.remove(oldAccelator);

        // Register new accelerators
        if(accelerator!=null) {
            primaryActionKeymap.put(mucoActionClass, accelerator);
            acceleratorMap.put(accelerator, mucoActionClass);
        }

        if(alternateAccelerator!=null) {
            alternateActionKeymap.put(mucoActionClass, alternateAccelerator);
            acceleratorMap.put(alternateAccelerator, mucoActionClass);
        }
        
        // Update each MainFrame's action instance and input map
        Vector actionInstances = ActionManager.getActionInstances(mucoActionClass);
        int nbActionInstances = actionInstances.size();
        for(int i=0; i<nbActionInstances; i++) {
            MucoAction action = (MucoAction)actionInstances.elementAt(i);
            MainFrame mainFrame = action.getMainFrame();

            // Remove action from MainFrame's action and input maps
            unregisterAction(mainFrame, action);

            // Change action's accelerators
            action.setAccelerator(accelerator);
            action.setAlternateAccelerator(alternateAccelerator);

            // Add updated action to MainFrame's action and input maps
            registerAction(mainFrame, action);
        }
    }


    /**
     * Loads the action keymap files: loads the one contained in the JAR file first, and then the user's one. 
     * This means any new action in the JAR action keymap (when a new version is released) will have the default
     * keyboard mapping, but the keyboard mappings customized by the user in the user's action keymap will override
     * the ones from the JAR action keymap.
     */
    private ActionKeymap() {

        // Load the default action_keymap.xml file contained in the application JAR
        try {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Loading JAR action keymap file at "+ACTION_KEYMAP_RESOURCE_PATH);

            parseActionKeymapFile(getClass().getResourceAsStream(ACTION_KEYMAP_RESOURCE_PATH));
        }
        catch(Exception e) {
            // Report the error to the user
            System.out.println("Error: unable to load JAR action keymap file: "+e);
        }

        // Load the user's custom action keymap file if it exists.
        // This will override the mappings customized by the user while retaining any new mapping that the user's
        // action_keymap.xml doesn't yet have.
        if(actionKeyMapFile.exists()) {
            try {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Loading user action keymap file at "+actionKeyMapFile.getAbsolutePath());

                parseActionKeymapFile(new BackupInputStream(actionKeyMapFile));
            } catch(Exception e) {
                // Report the error to the user
                System.out.println("Error: unable to load user action keymap file at "+actionKeyMapFile.getAbsolutePath()+" : "+e);
            }

        }
        // If the given file doesn't exist, copy the default one contained in the application JAR
        else {
            try {
                if(Debug.ON) Debug.trace("Copying "+ACTION_KEYMAP_RESOURCE_PATH+" JAR resource to "+actionKeyMapFile);

                FileToolkit.copyResource(ACTION_KEYMAP_RESOURCE_PATH, actionKeyMapFile);
            }
            catch(IOException e) {
                // Report the error to the user
                System.out.println("Error: unable to copy "+ACTION_KEYMAP_RESOURCE_PATH+" resource to "+actionKeyMapFile+": "+e);
            }
        }
    }


    private void parseActionKeymapFile(InputStream in) throws Exception {
        // Parse action keymap file
        try {
            new Parser().parse(in, this, "UTF-8");
        }
        finally {
            if(in!=null) {
                try { in.close(); }
                catch(IOException e) {}
            }
        }
    }


    ///////////////////////////////////
    // ContentHandler implementation //
    ///////////////////////////////////

    public void startDocument() throws Exception {
    }

    public void endDocument() throws Exception {
    }

    public void startElement(String uri, String name, Hashtable attValues, Hashtable attURIs) throws Exception {
        if(name.equals("action")) {
            String actionClassName = (String)attValues.get("class");
            if(actionClassName==null) {
                // Report the error to the user
                System.out.println("Error in action keymap file: no 'class' attribute specified in 'action' element");
                return;
            }

            Class actionClass;
            try {
                actionClass = Class.forName(actionClassName);
            }
            catch(ClassNotFoundException e) {
                // Report the error to the user
                System.out.println("Error in action keymap file: could not resolve class "+actionClassName);
                return;
            }

            // Primary keystroke
            String keyStrokeString = (String)attValues.get("keystroke");
            if(keyStrokeString==null) {
                // Report the error to the user
                System.out.println("Error in action keymap file: no 'keystroke' attribute specified in 'action' element");
                return;
            }

            KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeString);
            if(keyStroke==null) {
                // Report the error to the user
                System.out.println("Error in action keymap file: specified keystroke could not be resolved: "+keyStrokeString);
                return;
            }

            if(Debug.ON) {
                KeyStroke existingKeystroke = (KeyStroke)primaryActionKeymap.get(actionClass);
                if(existingKeystroke!=null && !existingKeystroke.equals(keyStroke))
                    Debug.trace("Overridding keystroke "+existingKeystroke+" for "+actionClass+" with "+keyStroke);
            }

            primaryActionKeymap.put(actionClass, keyStroke);
            acceleratorMap.put(keyStroke, actionClass);

            // Alternate keystroke (if any)
            keyStrokeString = (String)attValues.get("alt_keystroke");
            if(keyStrokeString!=null) {
                keyStroke = KeyStroke.getKeyStroke(keyStrokeString);
                if(keyStroke==null) {
                    // Report the error to the user
                    System.out.println("Error in action keymap file: specified alternate keystroke could not be resolved: "+keyStrokeString);
                    return;
                }

                if(Debug.ON) {
                    KeyStroke existingKeystroke = (KeyStroke)alternateActionKeymap.get(actionClass);
                    if(existingKeystroke!=null && !existingKeystroke.equals(keyStroke))
                        Debug.trace("Overridding alternate keystroke "+existingKeystroke+" for "+actionClass+" with "+keyStroke);
                }

                alternateActionKeymap.put(actionClass, keyStroke);
                acceleratorMap.put(keyStroke, actionClass);
            }
        }
    }

    public void endElement(String uri, String name) throws Exception {
    }

    public void characters(String s) throws Exception {
    }
}
