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

package com.mucommander.ui.main.commandbar;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.swing.KeyStroke;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.mucommander.Debug;
import com.mucommander.io.BackupInputStream;
import com.mucommander.ui.action.ActionManager;

/**
 * This class parses the XML file describing the command bar's buttons and associated actions.
 *
 * @author Maxence Bernard
 */
public class CommandBarReader extends CommandBarIO {

    /** Temporarily used for XML parsing */
    private Vector actionsV;
    /** Temporarily used for XML parsing */
    private Vector alternateActionsV;


    /**
     * Starts parsing the XML description file.
     */
    CommandBarReader() throws Exception {
        InputStream in;

        in = null;
        try {SAXParserFactory.newInstance().newSAXParser().parse(in = new BackupInputStream(getDescriptionFile()), this);}
        finally {
            if(in!=null)
                try { in.close(); }
                catch(IOException e) {}
        }
    }

    ////////////////////////////
    // ContentHandler methods //
    ////////////////////////////

    public void startDocument() {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(COMMAND_BAR_RESOURCE_PATH+" parsing started");

        actionsV = new Vector();
        /** Temporarily used for alternate actions parsing */
        alternateActionsV = new Vector();
    }

    public void endDocument() {
        int nbActions = actionsV.size();

        Class[] actions = new Class[nbActions];
        actionsV.toArray(actions);
        actionsV = null;

        Class[] alternateActions = new Class[nbActions];
        alternateActionsV.toArray(alternateActions);
        alternateActionsV = null;
        
        CommandBar.setActions(actions);
        CommandBar.setAlternateActions(alternateActions);
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(COMMAND_BAR_RESOURCE_PATH+" parsing finished");
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(qName.equals(BUTTON_ELEMENT)) {
        	// Resolve action class
            String actionClassName = attributes.getValue(ACTION_ATTRIBUTE);
            Class actionClass = ActionManager.getActionClass(actionClassName);
            if (actionClass != null)
            	actionsV.add(actionClass);
            else
            	if(Debug.ON) Debug.trace("Error in "+COMMAND_BAR_RESOURCE_PATH+": action class "+actionClassName+" not found");

            // Resolve alternate action class (if any)
            actionClassName = attributes.getValue(ALT_ACTION_ATTRIBUTE);
            if(actionClassName==null)
                alternateActionsV.add(null);
            else
                if ((actionClass = ActionManager.getActionClass(actionClassName)) != null)
                	alternateActionsV.add(actionClass);
                else if(Debug.ON) Debug.trace("Error in "+COMMAND_BAR_RESOURCE_PATH+": action class "+actionClassName+" not found");
        }
        else if(qName.equals(ROOT_ELEMENT)) {
            // Retrieve modifier key (shift by default)
            // Note: early 0.8 beta3 nightly builds did not have this attribute, so the attribute may be null
            String modifierString = attributes.getValue(MODIFIER_ATTRIBUTE);

            KeyStroke modifier;
            if(modifierString==null || (modifier=KeyStroke.getKeyStroke(modifierString))==null)
                modifier = KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0);
            CommandBar.setModifier(modifier);
        }
    }
}
