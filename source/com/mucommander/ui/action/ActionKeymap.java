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
import com.mucommander.file.FileFactory;
import com.mucommander.file.util.ResourceLoader;
import com.mucommander.io.BackupInputStream;
import com.mucommander.io.StreamUtils;
import com.mucommander.ui.main.MainFrame;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.swing.*;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;


/**
 * This class manages keyboard associations with {@link MuAction} classes.
 * Proper documentation and cleaning of this class is pending.  
 *
 * @author Maxence Bernard
 */
public class ActionKeymap extends DefaultHandler {

    /** Maps action Class onto Keystroke instances*/
    private static Hashtable primaryActionKeymap = new Hashtable();
    /** Maps action Class instances onto Keystroke instances*/
    private static Hashtable alternateActionKeymap = new Hashtable();

    /** Maps Keystroke instances onto action Class */
    private static Hashtable acceleratorMap = new Hashtable();

    /** Default action keymap filename */
    private final static String DEFAULT_ACTION_KEYMAP_FILE_NAME = "action_keymap.xml";
    /** Path to the action keymap resource file within the application JAR file */
    public final static String ACTION_KEYMAP_RESOURCE_PATH = "/" + DEFAULT_ACTION_KEYMAP_FILE_NAME;

    /** Action keymap file used when calling {@link #loadActionKeyMap()} */
    private static AbstractFile actionKeyMapFile;

    /* Variables used for XML parsing */

    private final static String ACTION_ELEMENT = "action";
    private final static String CLASS_ATTRIBUTE = "class";
    private final static String KEYSTROKE_ATTRIBUTE = "keystroke";
    private final static String ALTERNATE_KEYSTROKE_ATTRIBUTE = "alt_keystroke";

    /** True when default/JAR action keymap is being parsed */
    private static boolean isParsingDefaultActionKeymap;

    /** List of action Class which are defined in the user action keymap */
    private static HashSet definedUserActionClasses = new HashSet();


    /**
     * Sets the path to the user action keymap file to be loaded when calling {@link #loadActionKeyMap()}.
     * By default, this file is {@link #DEFAULT_ACTION_KEYMAP_FILE_NAME} within the preferences folder.
     * <p>
     * This is a convenience method and is strictly equivalent to calling <code>setActionKeyMapFile(FileFactory.getFile(file))</code>.
     * </p>
     * @param  path                  path to the action keymap file
     * @throws FileNotFoundException if <code>file</code> is not accessible.
     */
    public static void setActionKeyMapFile(String path) throws FileNotFoundException {
        AbstractFile file;

        if((file = FileFactory.getFile(path)) == null)
            setActionKeyMapFile(new File(path));
        else
            setActionKeyMapFile(file);
    }

    /**
     * Sets the path to the user action keymap file to be loaded when calling {@link #loadActionKeyMap()}.
     * By default, this file is {@link #DEFAULT_ACTION_KEYMAP_FILE_NAME} within the preferences folder.
     * <p>
     * This is a convenience method and is strictly equivalent to calling <code>setActionKeyMapFile(FileFactory.getFile(file.getAbsolutePath()))</code>.
     * </p>
     * @param  file                  path to the action keymap file
     * @throws FileNotFoundException if <code>file</code> is not accessible.
     */
    public static void setActionKeyMapFile(File file) throws FileNotFoundException {setActionKeyMapFile(FileFactory.getFile(file.getAbsolutePath()));}

    /**
     * Sets the path to the user action keymap file to be loaded when calling {@link #loadActionKeyMap()}.
     * By default, this file is {@link #DEFAULT_ACTION_KEYMAP_FILE_NAME} within the preferences folder.
     * @param  file                  path to the action keymap file
     * @throws FileNotFoundException if <code>file</code> is not accessible.
     */
    public static void setActionKeyMapFile(AbstractFile file) throws FileNotFoundException {
        if(file.isBrowsable())
            throw new FileNotFoundException("Not a valid file: " + file.getAbsolutePath());

        actionKeyMapFile = file;
    }

    /**
     * Returns the path to the action keymap file.
     * @return             the path to the action keymap file.
     * @throws IOException if an error occured while locating the default action keymap file.
     */
    public static AbstractFile getActionKeyMapFile() throws IOException {
        if(actionKeyMapFile == null)
            return PlatformManager.getPreferencesFolder().getChild(DEFAULT_ACTION_KEYMAP_FILE_NAME);
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
        if(accelerator!=null)
            registerActionAccelerator(action, accelerator, comp, condition);

        accelerator = action.getAlternateAccelerator();
        if(accelerator!=null)
            registerActionAccelerator(action, accelerator, comp, condition);
    }

    public static void unregisterActionAccelerators(MuAction action, JComponent comp, int condition) {
        KeyStroke accelerator = action.getAccelerator();
        if(accelerator!=null)
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
        try {
            AbstractFile file;

            // If the user hasn't yet defined an action keymap, copies the default one.
            file = getActionKeyMapFile();
            if(!file.exists()) {
                InputStream in = null;
                OutputStream out = null;

                if(Debug.ON) Debug.trace("Copying "+ACTION_KEYMAP_RESOURCE_PATH+" JAR resource to "+file);

                try {
                    in = ResourceLoader.getResourceAsStream(ACTION_KEYMAP_RESOURCE_PATH);
                    out = file.getOutputStream(false);

                    StreamUtils.copyStream(in, out);
                }
                catch(IOException e) {
                    if(Debug.ON) Debug.trace("Error: unable to copy "+ACTION_KEYMAP_RESOURCE_PATH+" resource to "+actionKeyMapFile+": "+e);
                }
                finally {
                    if(in != null) {
                        try {in.close();}
                        catch(IOException e) {}
                    }

                    if(out != null) {
                        try {out.close();}
                        catch(IOException e) {}
                    }
                }

                // No need to load the user action keymap here as it is the same as the default keymap
            }
            else {
                // Load the user's custom action keymap file.
                if(Debug.ON) Debug.trace("Loading user action keymap at " + file.getAbsolutePath());
                parseActionKeymapFile(new BackupInputStream(file));
            }

            isParsingDefaultActionKeymap = true;

            // Loads the default action keymap.
            if(Debug.ON) Debug.trace("Loading default JAR action keymap at "+ACTION_KEYMAP_RESOURCE_PATH);
            parseActionKeymapFile(ResourceLoader.getResourceAsStream(ACTION_KEYMAP_RESOURCE_PATH));
        }
        finally {
            definedUserActionClasses = null;
        }
    }


    /**
     * Starts parsing the XML action keymap file.
     * @param in the file's input stream
     * @throws Exception if an error was caught while parsing the file
     */
    private void parseActionKeymapFile(InputStream in) throws Exception {
        // Parse action keymap file
        try {SAXParserFactory.newInstance().newSAXParser().parse(in, this);}
        finally {
            if(in!=null) {
                try { in.close(); }
                catch(IOException e) {}
            }
        }
    }


    /**
     * Parses the keystroke defined in the given attribute map (if any) and associates it with the given action class.
     * The keystroke will not be associated in any of the following cases:
     * <ul>
     *  <li>the keystroke attribute does not contain any value.</li>
     *  <li>the keystroke attribute has a value that does not represent a valid KeyStroke (syntax error).</li>
     *  <li>the keystroke is already associated with an action class. In this case, the existing association is preserved.</li>
     * </ul>
     *
     * @param actionClass the action class to associate the keystroke with
     * @param attributes the attributes map that holds the value
     * @param alternate true to process the alternate keystroke attribute, false for the primary one
     */
    private void processKeystrokeAttribute(Class actionClass, Attributes attributes, boolean alternate) {
        String keyStrokeString = attributes.getValue(alternate?ALTERNATE_KEYSTROKE_ATTRIBUTE:KEYSTROKE_ATTRIBUTE);
        KeyStroke keyStroke = null;

        // Parse the keystroke and retrieve the corresponding KeyStroke instance and return if the attribute's value
        // is invalid.
        if(keyStrokeString!=null) {
            keyStroke = KeyStroke.getKeyStroke(keyStrokeString);
            if(keyStroke==null)
                System.out.println("Error: action keymap file contains a keystroke which could not be resolved: "+keyStrokeString);
        }

        // Return if keystroke attribute is not defined or KeyStroke instance could not be resolved
        if(keyStroke==null)
            return;

        // Discard the mapping if the keystroke is already associated with an action
        Class existingActionClass = (Class)acceleratorMap.get(keyStroke);
        if(existingActionClass!=null) {
            System.out.println("Warning: action keymap file contains multiple associations for keystroke: "+keyStrokeString+", preserving association with action: "+existingActionClass.getName());
            return;
        }

        // Add the action/keystroke mapping
        (alternate?alternateActionKeymap:primaryActionKeymap).put(actionClass, keyStroke);
        acceleratorMap.put(keyStroke, actionClass);
    }

    
    ///////////////////////////////////
    // ContentHandler implementation //
    ///////////////////////////////////

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(qName.equals(ACTION_ELEMENT)) {
            // Retrieve the action classname
            String actionClassName = attributes.getValue(CLASS_ATTRIBUTE);
            if(actionClassName==null) {
                if(Debug.ON) Debug.trace("Error in action keymap file: no 'class' attribute specified in 'action' element");
                return;
            }

            // Resolve the action Class
            Class actionClass;
            try {
                actionClass = Class.forName(actionClassName);
            }
            catch(ClassNotFoundException e) {
                if(Debug.ON) Debug.trace("Error in action keymap file: could not resolve class "+actionClassName);
                return;
            }

            // When parsing the default/JAR action keymap:
            // Skip action classes that have already been encountered in the user action keymap.
            if(isParsingDefaultActionKeymap && definedUserActionClasses.contains(actionClass))
                return;

            // Load the action's primary accelator (if any)
            processKeystrokeAttribute(actionClass, attributes, false);
            // Load the action's secondary/alternate accelerator (if any)
            processKeystrokeAttribute(actionClass, attributes, true);

            // When parsing the user action keymap:
            if(!isParsingDefaultActionKeymap) {
                // Add the action Class to the list of actions that have already been encountered/defined in the user
                // action keymap. Note that action elements that do not define any accelerator will still be added to
                // this list ; this allows discarding accelerators defined in the default/JAR action keymap and having
                // an action with no associated accelerator.
                definedUserActionClasses.add(actionClass);
            }
       }
    }
}
