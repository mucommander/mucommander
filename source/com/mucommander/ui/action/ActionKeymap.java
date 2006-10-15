package com.mucommander.ui.action;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileToolkit;
import com.mucommander.ui.MainFrame;
import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;

import javax.swing.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.io.InputStream;
import java.io.IOException;


/**
 * @author Maxence Bernard
 */
public class ActionKeymap implements ContentHandler {

    private static Hashtable primaryActionKeymap;
    private static Hashtable alternateActionKeymap;

    /** Default action keymap filename */
    private final static String DEFAULT_ACTION_KEYMAP_FILENAME = "action_keymap.xml";
    /** Path to the action keymap resource file within the application JAR file */
    private final static String ACTION_KEYMAP_RESOURCE_PATH = "/"+DEFAULT_ACTION_KEYMAP_FILENAME;

    /** Action keymap file used when calling {@link #loadActionKeyMap()} */
    private static AbstractFile actionKeyMapFile = FileFactory.getFile(PlatformManager.getPreferencesFolder().getAbsolutePath()+"/"+DEFAULT_ACTION_KEYMAP_FILENAME);


    /**
     * Sets the path to the action keymap file to be loaded when calling {@link #loadActionKeyMap()}.
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
     * Loads the action keymap file. If the file doesn't exist yet, it is copied from the default resource file within the JAR.
     *
     * <p>This method must be called before requesting and registering any action.
     */
    public static void loadActionKeyMap() {
        // If the given file doesn't exist, copy the default one in the JAR file
        if(!actionKeyMapFile.exists()) {
            try {
                if(Debug.ON) Debug.trace("copying "+ACTION_KEYMAP_RESOURCE_PATH+" resource to "+actionKeyMapFile);

                FileToolkit.copyResource(ACTION_KEYMAP_RESOURCE_PATH, actionKeyMapFile);
            }
            catch(IOException e) {
                System.out.println("Error: unable to copy "+ACTION_KEYMAP_RESOURCE_PATH+" resource to "+actionKeyMapFile+": "+e);
                return;
            }
        }

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


    /**
     * Parses the action keymap file.
     */
    private ActionKeymap() {
        // Parse action keymap file
        InputStream in = null;
        try {
            in = actionKeyMapFile.getInputStream();
            new Parser().parse(in, this, "UTF-8");
        }
        catch(Exception e) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Error: unable to load action_keymap.xml file "+e);
        }
        finally {
            if(in!=null)
                try { in.close(); }
                catch(IOException e) {}
        }
    }


    ///////////////////////////////////
    // ContentHandler implementation //
    ///////////////////////////////////

    public void startDocument() throws Exception {
        if(Debug.ON) Debug.trace(actionKeyMapFile+" parsing started");

        primaryActionKeymap = new Hashtable();
        alternateActionKeymap = new Hashtable();
    }

    public void endDocument() throws Exception {
        if(Debug.ON) Debug.trace(actionKeyMapFile+" parsing finished");
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

//            if(Debug.ON) Debug.trace("associating "+keyStroke+" accelerator with "+actionClass);

            primaryActionKeymap.put(actionClass, keyStroke);

            // Alternate keystroke (if any)
            keyStrokeString = (String)attValues.get("alt_keystroke");
            if(keyStrokeString!=null) {
                keyStroke = KeyStroke.getKeyStroke(keyStrokeString);
                if(keyStroke==null) {
                    if(Debug.ON) Debug.trace("Error: specified alternate keystroke could not be resolved: "+keyStrokeString);
                    return;
                }

//                if(Debug.ON) Debug.trace("associating "+keyStroke+" alternate accelerator with "+actionClass);

                alternateActionKeymap.put(actionClass, keyStroke);
            }
        }
    }

    public void endElement(String uri, String name) throws Exception {
    }

    public void characters(String s) throws Exception {
    }
}
