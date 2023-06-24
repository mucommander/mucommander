/*
 * This file is part of muCommander, http://www.mucommander.com
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;

import javax.swing.KeyStroke;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import com.mucommander.ui.action.ActionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.mucommander.RuntimeConstants;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.io.backup.BackupInputStream;
import com.mucommander.ui.action.ActionManager;

/**
 * This class parses the XML file describing the command bar's buttons and associated actions.
 *
 * @author Maxence Bernard, Arik Hadas
 */
class CommandBarReader extends CommandBarIO {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandBarReader.class);
	
    /** Temporarily used for XML parsing */
    private List<ActionId> actionsIdsV;
    /** Temporarily used for XML parsing */
    private List<ActionId> alternateActionsIdsV;
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
    
    public ActionId[] getActionsRead() {
    	int nbActions = actionsIdsV.size();
        ActionId[] actionIds = new ActionId[nbActions];
        actionsIdsV.toArray(actionIds);
        return actionIds;
    }
    
    public ActionId[] getAlternateActionsRead() {
    	int nbActions = alternateActionsIdsV.size();
        ActionId[] alternateActionIds = new ActionId[nbActions];
        alternateActionsIdsV.toArray(alternateActionIds);
        return alternateActionIds;
    }

    public boolean actionsPresent() {
        return !actionsIdsV.isEmpty() && !alternateActionsIdsV.isEmpty();
    }

    public KeyStroke getModifierRead() {
    	return modifier;
    }
    
    ////////////////////////////
    // ContentHandler methods //
    ////////////////////////////

    @Override
    public void startDocument() {
    	LOGGER.trace(file.getAbsolutePath()+" parsing started");

        actionsIdsV = new Vector<ActionId>();
        alternateActionsIdsV = new Vector<ActionId>();
        modifier = null;
    }

    @Override
    public void endDocument() {
    	LOGGER.trace(file.getAbsolutePath()+" parsing finished");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(qName.equals(BUTTON_ELEMENT)) {
        	// Resolve action id
        	String actionIdAttribute = attributes.getValue(ACTION_ID_ATTRIBUTE);
        	if (actionIdAttribute != null) {
                ActionId actionId = ActionId.asCommandBarAction(actionIdAttribute);
        		if (ActionManager.isActionExist(actionId)) {
        			actionsIdsV.add(actionId);

        			// Resolve alternate action id (if any)
        			actionIdAttribute = attributes.getValue(ALT_ACTION_ID_ATTRIBUTE);
                    actionId = actionIdAttribute != null ? ActionId.asCommandBarAction(actionIdAttribute) : null;
                    alternateActionsIdsV.add(ActionManager.isActionExist(actionId) ? actionId : null);
        		}
        	}
        	else {
        		// Resolve action class
        		String actionClassAttribute = attributes.getValue(ACTION_ATTRIBUTE);
        		if (actionClassAttribute != null) {
                    ActionId actionId = ActionId.asCommandBarAction(ActionManager.extrapolateId(actionClassAttribute));
        			if (ActionManager.isActionExist(actionId)) {
        				actionsIdsV.add(actionId);

        				// Resolve alternate action class (if any)
        				actionClassAttribute = attributes.getValue(ALT_ACTION_ATTRIBUTE);
        				if(actionClassAttribute == null)
        					alternateActionsIdsV.add(null);
        				else {
        					actionId = ActionId.asCommandBarAction(ActionManager.extrapolateId(actionClassAttribute));
        					if (ActionManager.isActionExist(actionId))
        						alternateActionsIdsV.add(actionId);
        					else {
        						LOGGER.warn("Error in "+DEFAULT_COMMAND_BAR_FILE_NAME+": action id for " + actionClassAttribute + " not found");
        						alternateActionsIdsV.add(null);
        					}
        				}
        			}
        			else
        				LOGGER.warn("Error in "+DEFAULT_COMMAND_BAR_FILE_NAME+": action id for " + actionClassAttribute + " not found");
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
