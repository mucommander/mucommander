/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

import com.mucommander.AppLogger;
import com.mucommander.RuntimeConstants;
import com.mucommander.file.AbstractFile;
import com.mucommander.io.BackupInputStream;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.swing.KeyStroke;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * This class is responsible for reading the actions.
 * it read and parse the two action files - the default one 
 * (placed in the jar file) and the user's one.
 * 
 * @author Maxence Bernard, Arik Hadas
 */
class ActionKeymapReader extends ActionKeymapIO {
	
	/** Maps action Class onto Keystroke instances*/
    private HashMap<String, KeyStroke> primaryActionsReadKeymap;
    /** Maps action Class instances onto Keystroke instances*/
    private HashMap<String, KeyStroke> alternateActionsReadKeymap;

    /** Parsed file */
    private AbstractFile file;
    
    /**
     * Loads the action file: loads the one contained in the JAR file first, and then the user's one.
     * This means any new action in the JAR action keymap (when a new version gets released) will have the default
     * keyboard mapping, but the keyboard mappings customized by the user in the user's action keymap will override
     * the ones from the JAR action keymap.
     *
     * Starts parsing the XML actions file.
     * 
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     */
    ActionKeymapReader(AbstractFile file) throws SAXException, IOException, ParserConfigurationException {
    	this.file = file;
    	
    	InputStream in = null;
    	try {SAXParserFactory.newInstance().newSAXParser().parse(in = new BackupInputStream(file), this);}
    	finally {
    		if(in!=null) {
    			try { in.close(); }
    			catch(IOException e) {}
    		}
    	}
    }
	
    /**
     * Parses the keystrokes defined in the given attribute map (if any) and associates them with the given action id.
     * The keystroke will not be associated in any of the following cases:
     * <ul>
     *  <li>the keystrokes attributes do not contain any value.</li>
     *  <li>the keystrokes attributes have values that do not represent a valid KeyStroke (syntax error).</li>
     * </ul>
     * If a given keystroke is already associated to an action, the existing association is replaced. 
     * If there is a valid alternative keystroke defined but there is no valid primary keystroke defined, the primary keystroke 
     * is replaced by the alternative keystroke.
     *
     * @param actionId the action id to associate the keystroke with
     * @param attributes the attributes map that holds the value
     */
    private void processKeystrokeAttribute(String actionId, Attributes attributes) {    	
    	String keyStrokeString;
    	KeyStroke alternateKeyStroke = null;
    	KeyStroke primaryKeyStroke = null;
    	
    	// Parse the primary keystroke and retrieve the corresponding KeyStroke instance
    	keyStrokeString = attributes.getValue(PRIMARY_KEYSTROKE_ATTRIBUTE);
    	
    	if (keyStrokeString != null) {
    		primaryKeyStroke = KeyStroke.getKeyStroke(keyStrokeString);
    		if (primaryKeyStroke == null)
    			AppLogger.info("Action keymap file contains a keystroke which could not be resolved: " + keyStrokeString);
    		else {
    			String prevAssignedActionId = ActionKeymap.getRegisteredActionIdForKeystroke(primaryKeyStroke);
    			if (prevAssignedActionId != null && !prevAssignedActionId.equals(actionId))
    				AppLogger.fine("Canceling previous association of keystroke " + keyStrokeString + ", reassign it to action: " + actionId);
    		}
    	}

    	// Parse the alternate keystroke and retrieve the corresponding KeyStroke instance
    	keyStrokeString = attributes.getValue(ALTERNATE_KEYSTROKE_ATTRIBUTE);
    	
    	if (keyStrokeString != null) {
    		alternateKeyStroke = KeyStroke.getKeyStroke(keyStrokeString);
    		if (alternateKeyStroke == null)
    			AppLogger.info("Action keymap file contains a keystroke which could not be resolved: " + keyStrokeString);
    		else {
    			String prevAssignedActionId = ActionKeymap.getRegisteredActionIdForKeystroke(alternateKeyStroke);
    			if (prevAssignedActionId != null && !prevAssignedActionId.equals(actionId))
    				AppLogger.fine("Canceling previous association of keystroke " + keyStrokeString + ", reassign it to action: " + actionId);
    		}
    	}

   		// If there is no primary shortcut defined for the action but there is an alternative shortcut defined,
   		// turn the alternative shortcut to the action's primary shortcut
   		if (primaryKeyStroke == null) {
   			AppLogger.fine("Action \"" + actionId +"\" has an alternative shortcut with no primary shortcut, so the alternative shortcut become primary");
   			primaryActionsReadKeymap.put(actionId, alternateKeyStroke);
   			alternateActionsReadKeymap.put(actionId, null);
   			// Mark that the actions keymap file should be updated
  			setModified();
   		}
   		else {
   			primaryActionsReadKeymap.put(actionId, primaryKeyStroke);
   			alternateActionsReadKeymap.put(actionId, alternateKeyStroke);    		
   		}
    }

    ///////////////////
    ///// getters /////
    ///////////////////
    
    public HashMap<String, KeyStroke> getPrimaryActionsKeymap() {return primaryActionsReadKeymap;}
    
    public HashMap<String, KeyStroke> getAlternateActionsKeymap() {return alternateActionsReadKeymap;}
    
    ///////////////////////////////////
    // ContentHandler implementation //
    ///////////////////////////////////

    @Override
    public void startDocument() {
    	AppLogger.finest(file.getAbsolutePath()+" parsing started");
    	
    	primaryActionsReadKeymap = new HashMap<String, KeyStroke>();
    	alternateActionsReadKeymap = new HashMap<String, KeyStroke>();
    }
    
    @Override
    public void endDocument() {
        AppLogger.finest(file.getAbsolutePath()+" parsing finished");
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    	if(qName.equals(ACTION_ELEMENT)) {
    		// Retrieve the action id
    		String actionId = attributes.getValue(ID_ATTRIBUTE);
    		// if id attribute not exits, read class attribute
    		if (actionId == null) {
    			String actionClassPath = attributes.getValue(CLASS_ATTRIBUTE);
    			
    			if(actionClassPath==null) {
        			AppLogger.warning("Error in action keymap file: no 'class' or 'id' attribute specified in 'action' element");
        			return;
        		}
    			// extrapolate the action id from its class path
    			actionId = ActionManager.extrapolateId(actionClassPath);
    		}
    		
    		if (!ActionManager.isActionExist(actionId)) {
    			AppLogger.warning("Error in action keymap file: could not resolve action "+actionId);
    			return;
    		}

    		// Load the action's accelerators (if any)
    		processKeystrokeAttribute(actionId, attributes);
    	}
    	else if (qName.equals(ROOT_ELEMENT)) {
    		// Note: early 0.8 beta3 nightly builds did not have version attribute, so the attribute may be null
            String fileVersion = attributes.getValue(VERSION_ATTRIBUTE);
    		
    		// if the file's version is not up-to-date, update the file to the current version at quitting.
    		if (!RuntimeConstants.VERSION.equals(fileVersion))
    			setModified();
    	}
    }
}
