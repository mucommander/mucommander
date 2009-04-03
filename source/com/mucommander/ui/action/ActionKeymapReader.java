/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;

import javax.swing.KeyStroke;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.mucommander.Debug;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.ResourceLoader;
import com.mucommander.io.BackupInputStream;
import com.mucommander.io.StreamUtils;

/**
 * This class is responsible for reading the actions.
 * it read and parse the two action files - the default one 
 * (placed in the jar file) and the user's one.
 * 
 * @author Maxence Bernard, Arik Hadas
 */
class ActionKeymapReader extends ActionKeymapIO {
	
    /** True when default/JAR action keymap is being parsed */
    private static boolean isParsingDefaultActionKeymap;

    /** List of action Class which are defined in the user action keymap */
    private static HashSet definedUserActionClasses = new HashSet();
    
    /**
     * Loads the action file: loads the one contained in the JAR file first, and then the user's one.
     * This means any new action in the JAR action keymap (when a new version gets released) will have the default
     * keyboard mapping, but the keyboard mappings customized by the user in the user's action keymap will override
     * the ones from the JAR action keymap.
     */
    ActionKeymapReader() throws Exception {
    	try {
    		AbstractFile actionsfile;

    		// If the user hasn't yet defined an action keymap, copies the default one.
    		actionsfile = getActionsFile();
    		if(!actionsfile.exists()) {
    			InputStream in = null;
    			OutputStream out = null;

    			if(Debug.ON) Debug.trace("Copying "+ACTION_KEYMAP_RESOURCE_PATH+" JAR resource to "+actionsfile);

    			try {
    				in = ResourceLoader.getResourceAsStream(ACTION_KEYMAP_RESOURCE_PATH);
    				out = actionsfile.getOutputStream(false);

    				StreamUtils.copyStream(in, out);
    			}
    			catch(IOException e) {
    				if(Debug.ON) Debug.trace("Error: unable to copy "+ACTION_KEYMAP_RESOURCE_PATH+" resource to "+actionsfile+": "+e);
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
    			if(Debug.ON) Debug.trace("Loading user action keymap at " + actionsfile.getAbsolutePath());
    			parseActionKeymapFile(new BackupInputStream(actionsfile));
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
     * Starts parsing the XML actions file.
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
     */
    private void processKeystrokeAttribute(Class actionClass, Attributes attributes) {    	
    	String keyStrokeString;

    	// Parse the primary keystroke and retrieve the corresponding KeyStroke instance
    	keyStrokeString = attributes.getValue(PRIMARY_KEYSTROKE_ATTRIBUTE);
    	KeyStroke primaryKeyStroke = null;
    	
    	if(keyStrokeString!=null) {
    		if ((primaryKeyStroke = KeyStroke.getKeyStroke(keyStrokeString)) == null)
    			System.out.println("Error: action keymap file contains a keystroke which could not be resolved: "+keyStrokeString);
    		else if (ActionKeymap.isKeyStrokeRegistered(primaryKeyStroke))
    			System.out.println("Warning: action keymap file contains multiple associations for keystroke: "+keyStrokeString+" canceling mapping to "+actionClass.getName());
    	}

    	// Parse the alternate keystroke and retrieve the corresponding KeyStroke instance
    	keyStrokeString = attributes.getValue(ALTERNATE_KEYSTROKE_ATTRIBUTE);
    	KeyStroke alternateKeyStroke = null;
    	
    	// and return if the attribute's value is invalid.
    	if(keyStrokeString!=null) {
    		if ((alternateKeyStroke = KeyStroke.getKeyStroke(keyStrokeString)) == null)
    			System.out.println("Error: action keymap file contains a keystroke which could not be resolved: "+keyStrokeString);
    		else if (ActionKeymap.isKeyStrokeRegistered(alternateKeyStroke))
    			System.out.println("Warning: action keymap file contains multiple associations for keystroke: "+keyStrokeString+" canceling mapping to "+actionClass.getName());
    	}

    	// Add the action/keystroke mapping
    	ActionKeymap.registerActionAccelerators(actionClass, primaryKeyStroke, alternateKeyStroke);
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

            // Load the action's accelerators (if any)
            processKeystrokeAttribute(actionClass, attributes);
            
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
