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

package com.mucommander.ui.main.toolbar;

import com.mucommander.AppLogger;
import com.mucommander.file.AbstractFile;
import com.mucommander.io.BackupInputStream;
import com.mucommander.ui.action.ActionManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * This class parses the XML file describing the toolbar's buttons and associated actions.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class ToolBarReader extends ToolBarIO {

    /** Temporarily used for XML parsing */
    private Vector actionsV;
    /** Temporarily used for XML parsing */
    private String fileVersion;
    
    /**
     * Starts parsing the XML description file.
     */
    ToolBarReader(AbstractFile descriptionFile) throws Exception {
        InputStream in;

        in = null;
        try {SAXParserFactory.newInstance().newSAXParser().parse(in = new BackupInputStream(descriptionFile), this);}
        finally {
            if(in != null) {
                try {in.close();}
                catch(IOException e) {}
            }
        }
    }
    

    ////////////////////////////
    // ContentHandler methods //
    ////////////////////////////

    public void startDocument() {
        actionsV = new Vector();
    }

    public void endDocument() {
        int nbActions = actionsV.size();
        String[] actionIds = new String[nbActions];
        actionsV.toArray(actionIds);        
        actionsV = null;

        ToolBarAttributes.setActions(actionIds);
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(qName.equals(BUTTON_ELEMENT)) {
        	// Resolve action class
            String actionAttribute = attributes.getValue(ACTION_ATTRIBUTE);
            // TODO: read action ids
            String actionId = ActionManager.extrapolateId(actionAttribute);
            if (actionId != null)
            	actionsV.add(actionId);
            else
            	AppLogger.warning("Error in "+DEFAULT_TOOLBAR_FILE_NAME+": action class "+actionId+" not found");
        }
        else if(qName.equals(SEPARATOR_ELEMENT)) {
            actionsV.add(null);
        }
        else if (qName.equals(ROOT_ELEMENT)) {
        	// Note: early 0.8 beta3 nightly builds did not have version attribute, so the attribute may be null
            fileVersion = attributes.getValue(VERSION_ATTRIBUTE);
        }
    }
}
