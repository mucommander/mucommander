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
import com.mucommander.io.BackupOutputStream;
import com.mucommander.ui.text.KeyStrokeUtils;
import com.mucommander.xml.XmlAttributes;
import com.mucommander.xml.XmlWriter;

import javax.swing.KeyStroke;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * This class is responsible for writing the actions.
 * When actions are modified, they are written to the user's actions file. 
 * 
 * @author Maxence Bernard, Arik Hadas
 */
class ActionKeymapWriter extends ActionKeymapIO {
	
	ActionKeymapWriter() {}
	
	public void create() throws IOException {
		BackupOutputStream bos = new BackupOutputStream(getActionsFile());

		try {
			new Writer(bos).writeKeyMap(null);
		} catch (Exception e) {
			AppLogger.fine("Caught exception", e);
		} finally {
			bos.close();
		}
	}
	
	void write() throws IOException {
		Hashtable<String, KeyStroke[]> combinedMapping = new Hashtable<String, KeyStroke[]>();
		Iterator<String> modifiedActionsIterator = ActionKeymap.getCustomizedActions();

		while(modifiedActionsIterator.hasNext()) {
			String actionId = modifiedActionsIterator.next();
			KeyStroke[] keyStrokes = new KeyStroke[2];
			keyStrokes[0] = ActionKeymap.getAccelerator(actionId);
			keyStrokes[1] = ActionKeymap.getAlternateAccelerator(actionId);

			combinedMapping.put(actionId, keyStrokes);
		}
		
		BackupOutputStream bos = new BackupOutputStream(getActionsFile());

		try {
			new Writer(bos).writeKeyMap(combinedMapping);
			wereActionsModified = false;
		} catch (Exception e) {
            AppLogger.fine("Caught exception", e);
		} finally {
			bos.close();
		}
	}
	
    private static class Writer {
    	private XmlWriter writer = null;

    	private Writer(OutputStream stream) throws IOException {
    		this.writer = new XmlWriter(stream);
    	}
    	
    	private void writeKeyMap(Hashtable<String, KeyStroke[]> actionMap) throws IOException {
    		try {
    			writer.writeCommentLine("See http://trac.mucommander.com/wiki/ActionKeyMap for information on how to customize this file");
    			
    			XmlAttributes rootElementAttributes = new XmlAttributes();
				rootElementAttributes.add(VERSION_ATTRIBUTE, RuntimeConstants.VERSION);
    			
    			writer.startElement(ROOT_ELEMENT, rootElementAttributes, true);

    			if (actionMap != null) {
    				Enumeration<String> enumeration = actionMap.keys();
    				while (enumeration.hasMoreElements()) {
    					String actionId = enumeration.nextElement();
    					addMapping(actionId, actionMap.get(actionId));
    				}
    			}

    		} finally {
    			writer.endElement(ROOT_ELEMENT);
    		}
    	}

    	private void addMapping(String actionId, KeyStroke[] keyStrokes) throws IOException {
    		XmlAttributes attributes = new XmlAttributes();
    		attributes.add(ID_ATTRIBUTE, actionId);

    	    AppLogger.finest("     Writing mapping of "  + actionId + " to " + keyStrokes[0] + " and " + keyStrokes[1]);

    		if (keyStrokes[0] != null)
    			attributes.add(PRIMARY_KEYSTROKE_ATTRIBUTE, KeyStrokeUtils.getKeyStrokeRepresentation(keyStrokes[0]));

    		if (keyStrokes[1] != null)
    			attributes.add(ALTERNATE_KEYSTROKE_ATTRIBUTE, KeyStrokeUtils.getKeyStrokeRepresentation(keyStrokes[1]));
    		
    		writer.writeStandAloneElement(ACTION_ELEMENT, attributes);
    	}
    }
}
