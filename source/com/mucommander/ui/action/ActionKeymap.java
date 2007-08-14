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
import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.ResourceLoader;
import com.mucommander.io.BackupInputStream;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;

import javax.swing.*;
import java.io.*;
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
    private final static String DEFAULT_ACTION_KEYMAP_FILE_NAME = "action_keymap.xml";
    /** Path to the action keymap resource file within the application JAR file */
    private final static String ACTION_KEYMAP_RESOURCE_PATH = "/" + DEFAULT_ACTION_KEYMAP_FILE_NAME;

    /** Action keymap file used when calling {@link #loadActionKeyMap()} */
    private static File actionKeyMapFile;


    /**
     * Sets the path to the user action keymap file to be loaded when calling {@link #loadActionKeyMap()}.
     * By default, this file is {@link #DEFAULT_ACTION_KEYMAP_FILE_NAME} within the preferences folder.
     *
     * @param filePath path to the action keymap file
     */
    public static void setActionKeyMapFile(String filePath) throws FileNotFoundException {
        File tempFile;

        tempFile = new File(filePath);
        if(!(tempFile.exists() && tempFile.isFile() && tempFile.canRead()))
            throw new FileNotFoundException("Not a valid file: " + filePath);
        actionKeyMapFile = tempFile;
    }

    public static File getActionKeyMapFile() {
        if(actionKeyMapFile == null)
            return new File(PlatformManager.getPreferencesFolder(), DEFAULT_ACTION_KEYMAP_FILE_NAME);
        return actionKeyMapFile;
    }


    /**
     * Loads the action keymap files: loads the one contained in the JAR file first, and then the user's one.
     * This means any new action in the JAR action keymap (when a new version is released) will have the default
     * keyboard mapping, but the keyboard mappings customized by the user in the user's action keymap will override
     * the ones from the JAR action keymap.
     *
     * <p>This method must be called before requesting and registering any action.
     */
    public static void loadActionKeyMap() throws Exception {
        new ActionKeymap();
    }


    public static KeyStroke getAccelerator(Class muActionClass) {
        return (KeyStroke)primaryActionKeymap.get(muActionClass);
    }

    public static KeyStroke getAlternateAccelerator(Class muActionClass) {
        return (KeyStroke)alternateActionKeymap.get(muActionClass);
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
            MuAction action = ActionManager.getActionInstance((Class)actionClasses.nextElement(), mainFrame);
            ActionKeymap.registerActionAccelerators(action, table1, JComponent.WHEN_FOCUSED);
            ActionKeymap.registerActionAccelerators(action, table2, JComponent.WHEN_FOCUSED);
        }

        actionClasses = alternateActionKeymap.keys();
        while(actionClasses.hasMoreElements()) {
            MuAction action = ActionManager.getActionInstance((Class)actionClasses.nextElement(), mainFrame);
            ActionKeymap.registerActionAccelerators(action, table1, JComponent.WHEN_FOCUSED);
            ActionKeymap.registerActionAccelerators(action, table2, JComponent.WHEN_FOCUSED);
        }
    }


    public static void registerAction(MainFrame mainFrame, MuAction action) {
        registerActionAccelerators(action, mainFrame.getFolderPanel1().getFileTable(), JComponent.WHEN_FOCUSED);
        registerActionAccelerators(action, mainFrame.getFolderPanel2().getFileTable(), JComponent.WHEN_FOCUSED);
    }

    public static void unregisterAction(MainFrame mainFrame, MuAction action) {
        unregisterActionAccelerators(action, mainFrame.getFolderPanel1().getFileTable(), JComponent.WHEN_FOCUSED);
        unregisterActionAccelerators(action, mainFrame.getFolderPanel2().getFileTable(), JComponent.WHEN_FOCUSED);
    }


    public static void registerActionAccelerator(MuAction action, KeyStroke accelerator, JComponent comp, int condition) {
        if(accelerator==null)
            return;
        InputMap inputMap = comp.getInputMap(condition);
        ActionMap actionMap = comp.getActionMap();
        Class muActionClass = action.getClass();
        inputMap.put(accelerator, muActionClass);
        actionMap.put(muActionClass, action);
    }

    public static void unregisterActionAccelerator(MuAction action, KeyStroke accelerator, JComponent comp, int condition) {
        if(accelerator==null)
            return;
        InputMap inputMap = comp.getInputMap(condition);
        ActionMap actionMap = comp.getActionMap();
        Class muActionClass = action.getClass();
        inputMap.remove(accelerator);
        actionMap.remove(muActionClass);
    }


    public static void registerActionAccelerators(MuAction action, JComponent comp, int condition) {
        KeyStroke accelerator = action.getAccelerator();
        if(accelerator==null)
            return;

        registerActionAccelerator(action, accelerator, comp, condition);

        accelerator = action.getAlternateAccelerator();
        if(accelerator!=null)
            registerActionAccelerator(action, accelerator, comp, condition);
    }

    public static void unregisterActionAccelerators(MuAction action, JComponent comp, int condition) {
        KeyStroke accelerator = action.getAccelerator();
        if(accelerator==null)
            return;

        unregisterActionAccelerator(action, accelerator, comp, condition);

        accelerator = action.getAlternateAccelerator();
        if(accelerator!=null)
            unregisterActionAccelerator(action, accelerator, comp, condition);
    }


    public static void changeActionAccelerators(Class muActionClass, KeyStroke accelerator, KeyStroke alternateAccelerator) {
        // Remove old accelerators (primary and alternate) from accelerators map
        KeyStroke oldAccelator = (KeyStroke)primaryActionKeymap.get(muActionClass);
        if(oldAccelator!=null)
            acceleratorMap.remove(oldAccelator);

        oldAccelator = (KeyStroke)alternateActionKeymap.get(muActionClass);
        if(oldAccelator!=null)
            acceleratorMap.remove(oldAccelator);

        // Register new accelerators
        if(accelerator!=null) {
            primaryActionKeymap.put(muActionClass, accelerator);
            acceleratorMap.put(accelerator, muActionClass);
        }

        if(alternateAccelerator!=null) {
            alternateActionKeymap.put(muActionClass, alternateAccelerator);
            acceleratorMap.put(alternateAccelerator, muActionClass);
        }
        
        // Update each MainFrame's action instance and input map
        Vector actionInstances = ActionManager.getActionInstances(muActionClass);
        int nbActionInstances = actionInstances.size();
        for(int i=0; i<nbActionInstances; i++) {
            MuAction action = (MuAction)actionInstances.elementAt(i);
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
     * Loads the action keymap file: loads the one contained in the JAR file first, and then the user's one.
     * This means any new action in the JAR action keymap (when a new version gets released) will have the default
     * keyboard mapping, but the keyboard mappings customized by the user in the user's action keymap will override
     * the ones from the JAR action keymap.
     */
    private ActionKeymap() throws Exception {
        File file;

        // If the user hasn't yet defined an action keymap, copies the default one.
        file = getActionKeyMapFile();
        if(!file.exists()) {
            OutputStream out;

            out = null;
            if(Debug.ON) Debug.trace("Copying "+ACTION_KEYMAP_RESOURCE_PATH+" JAR resource to "+file);

            try {AbstractFile.copyStream(ResourceLoader.getResourceAsStream(ACTION_KEYMAP_RESOURCE_PATH), out = new FileOutputStream(file));}
            catch(IOException e) {if(Debug.ON) Debug.trace("Error: unable to copy "+ACTION_KEYMAP_RESOURCE_PATH+" resource to "+actionKeyMapFile+": "+e);}
            finally {
                if(out != null) {
                    try {out.close();}
                    catch(Exception e) {}
                }
            }

            // Loads the default action keymap.
            // Error at this point are fatal, are there are no other keymap file to load.
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Loading JAR action keymap file at "+ACTION_KEYMAP_RESOURCE_PATH);
            parseActionKeymapFile(ResourceLoader.getResourceAsStream(ACTION_KEYMAP_RESOURCE_PATH));
        }
        else {
            // Loads the default action keymap.
            // Error at this point are non-fatal, are there still is the user defined keyamp to fall back to.
            try {parseActionKeymapFile(ResourceLoader.getResourceAsStream(ACTION_KEYMAP_RESOURCE_PATH));}
            catch(Exception e) {}

            // Load the user's custom action keymap file.
            // This will override the mappings customized by the user while retaining any new mapping that the user's
            // action_keymap.xml doesn't yet have.
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Loading user action keymap file at " + file.getAbsolutePath());
            parseActionKeymapFile(new BackupInputStream(file));
        }
    }


    /**
     * Starts parsing the XML action keymap file.
     * @param in the file's input stream
     * @throws Exception if an error was caught while parsing the file
     */
    private void parseActionKeymapFile(InputStream in) throws Exception {
        // Parse action keymap file
        try {new Parser().parse(in, this, "UTF-8");}
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
                if(Debug.ON) Debug.trace("Error in action keymap file: no 'class' attribute specified in 'action' element");
                return;
            }

            Class actionClass;
            try {
                actionClass = Class.forName(actionClassName);
            }
            catch(ClassNotFoundException e) {
                if(Debug.ON) Debug.trace("Error in action keymap file: could not resolve class "+actionClassName);
                return;
            }

            // Primary keystroke
            String keyStrokeString = (String)attValues.get("keystroke");
            if(keyStrokeString==null) {
                if(Debug.ON) Debug.trace("Error in action keymap file: no 'keystroke' attribute specified in 'action' element");
                return;
            }

            KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeString);
            if(keyStroke==null) {
                if(Debug.ON) Debug.trace("Error in action keymap file: specified keystroke could not be resolved: "+keyStrokeString);
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
                    if(Debug.ON) Debug.trace("Error in action keymap file: specified alternate keystroke could not be resolved: "+keyStrokeString);
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
