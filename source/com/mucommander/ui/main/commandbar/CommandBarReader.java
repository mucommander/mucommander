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

package com.mucommander.ui.main.commandbar;

import com.mucommander.AppLogger;
import com.mucommander.RuntimeConstants;
import com.mucommander.file.AbstractFile;
import com.mucommander.io.BackupInputStream;
import com.mucommander.ui.action.ActionManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.swing.KeyStroke;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * This class parses the XML file describing the command bar's buttons and associated actions.
 *
 * @author Maxence Bernard, Arik Hadas
 */
class CommandBarReader extends CommandBarIO {

    /** Temporarily used for XML parsing */
    private Vector<String> actionsIdsV;
    /** Temporarily used for XML parsing */
    private Vector<String> alternateActionsIdsV;
    /** Temporarily used for XML parsing */
    private KeyStroke modifier;

    /** Parsed file */
    private AbstractFile file;

    /**
     * Starts parsing the XML description file.
     * 
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    CommandBarReader(AbstractFile file) throws SAXException, IOException, ParserConfigurationException {
    	this.file = file;
    	
    	InputStream in = null;
        try {SAXParserFactory.newInstance().newSAXParser().parse(in = new BackupInputStream(file), this);}
        finally {
            if(in!=null)
                try { in.close(); }
                catch(IOException e) {}
        }
    }

    ////////////////////
    ///// getters //////
    ////////////////////
    
    public String[] getActionsRead() {
    	int nbActions = actionsIdsV.size();
    	String[] actionIds = new String[nbActions];
        actionsIdsV.toArray(actionIds);
        return actionIds;
    }
    
    public String[] getAlternateActionsRead() {
    	int nbActions = alternateActionsIdsV.size();
    	String[] alternateActionIds = new String[nbActions];
        alternateActionsIdsV.toArray(alternateActionIds);
        return alternateActionIds;
    }
    
    public KeyStroke getModifierRead() {
    	return modifier;
    }
    
    ////////////////////////////
    // ContentHandler methods //
    ////////////////////////////

    @Override
    public void startDocument() {
        AppLogger.finest(file.getAbsolutePath()+" parsing started");

        actionsIdsV = new Vector<String>();
        alternateActionsIdsV = new Vector<String>();
        modifier = null;
    }

    @Override
    public void endDocument() {
        AppLogger.finest(file.getAbsolutePath()+" parsing finished");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(qName.equals(BUTTON_ELEMENT)) {
        	// Resolve action id
        	String actionIdAttribute = attributes.getValue(ACTION_ID_ATTRIBUTE);
        	if (actionIdAttribute != null) {
        		if (ActionManager.isActionExist(actionIdAttribute)) {
        			actionsIdsV.add(actionIdAttribute);

        			// Resolve alternate action id (if any)
        			actionIdAttribute = attributes.getValue(ALT_ACTION_ID_ATTRIBUTE);
        			alternateActionsIdsV.add(ActionManager.isActionExist(actionIdAttribute) ? actionIdAttribute : null);
        		}
        	}
        	else {
        		// Resolve action class
        		String actionClassAttribute = attributes.getValue(ACTION_ATTRIBUTE);
        		if (actionClassAttribute != null) {
        			String actionId = ActionManager.extrapolateId(actionClassAttribute);
        			if (ActionManager.isActionExist(actionId)) {
        				actionsIdsV.add(actionId);

        				// Resolve alternate action class (if any)
        				actionClassAttribute = attributes.getValue(ALT_ACTION_ATTRIBUTE);
        				if(actionClassAttribute == null)
        					alternateActionsIdsV.add(null);
        				else {
        					actionId = ActionManager.extrapolateId(actionClassAttribute);
        					if (ActionManager.isActionExist(actionId))
        						alternateActionsIdsV.add(actionId);
        					else {
        						AppLogger.warning("Error in "+DEFAULT_COMMAND_BAR_FILE_NAME+": action id for " + actionClassAttribute + " not found");
        						alternateActionsIdsV.add(null);
        					}
        				}
        			}
        			else
        				AppLogger.warning("Error in "+DEFAULT_COMMAND_BAR_FILE_NAME+": action id for " + actionClassAttribute + " not found");
        		}
        	}
        }
        else if(qName.equals(ROOT_ELEMENT)) {
        	// Retrieve modifier key (shift by default)
        	modifier = KeyStroke.getKeyStroke(attributes.getValue(MODIFIER_ATTRIBUTE));
            
        	// Note: early 0.8 beta3 nightly builds did not have version attribute, so the attribute may be null
            String fileVersion = attributes.getValue(VERSION_ATTRIBUTE);
            
            // if the file's version is not up-to-date, update the file to the current version at quitting.
    		if (!RuntimeConstants.VERSION.equals(fileVersion))
    			setModified();
        }
    }
}
