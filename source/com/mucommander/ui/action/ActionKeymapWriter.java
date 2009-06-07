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
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.KeyStroke;

import com.mucommander.Debug;
import com.mucommander.io.BackupOutputStream;
import com.mucommander.ui.text.KeyStrokeUtils;
import com.mucommander.xml.XmlAttributes;
import com.mucommander.xml.XmlWriter;

/**
 * This class is responsible for writing the actions.
 * When actions are modified, they are written to the user's actions file. 
 * 
 * @author Maxence Bernard, Arik Hadas
 */
class ActionKeymapWriter extends ActionKeymapIO {
	
	ActionKeymapWriter() {}
	
	public void create() throws IOException {
		BackupOutputStream bos = null;

		try {
			bos = new BackupOutputStream(getActionsFile());
			new ActionKeyMapWriter(bos).writeKeyMap(null);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bos.close();
		}
	}
	
	public void write() throws IOException {
		Hashtable combinedMapping = new Hashtable();
		Iterator modifiedActionsIterator = ActionKeymap.getCustomizedActions();

		while(modifiedActionsIterator.hasNext()) {
			Class actionClass = (Class) modifiedActionsIterator.next();
			KeyStroke[] keyStrokes = new KeyStroke[2];
			keyStrokes[0] = ActionKeymap.getAccelerator(actionClass);
			keyStrokes[1] = ActionKeymap.getAlternateAccelerator(actionClass);

			combinedMapping.put(actionClass, keyStrokes);
		}
		
		BackupOutputStream bos = null;

		try {
			bos = new BackupOutputStream(getActionsFile());
			new ActionKeyMapWriter(bos).writeKeyMap(combinedMapping);
			wereActionsModified = false;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bos.close();
		}
	}
	
    private static class ActionKeyMapWriter {
    	private XmlWriter writer = null;

    	private ActionKeyMapWriter(OutputStream stream) throws IOException {
    		this.writer = new XmlWriter(stream);
    	}
    	
    	private void writeKeyMap(Hashtable actionMap) throws IOException {
    		try {
    			writer.writeCommentLine("See http://trac.mucommander.com/wiki/ActionKeyMap for information on how to customize this file");
    			
    			XmlAttributes rootElementAttributes = new XmlAttributes();
				rootElementAttributes.add(VERSION_ATTRIBUTE, "0.8.3"); // TODO: "0.8.3" <=> RuntimeConstants.VERSION
    			
    			writer.startElement(ROOT_ELEMENT, rootElementAttributes, true);

    			if (actionMap != null) {
    				Enumeration enumeration = actionMap.keys();
    				while (enumeration.hasMoreElements()) {
    					Class clazz = (Class) enumeration.nextElement();
    					addMapping(clazz, (KeyStroke[]) actionMap.get(clazz));
    				}
    			}

    		} finally {
    			writer.endElement(ROOT_ELEMENT);
    		}
    	}

    	private void addMapping(Class actionClass, KeyStroke[] keyStrokes) throws IOException {
    		XmlAttributes attributes = new XmlAttributes();
    		attributes.add(CLASS_ATTRIBUTE, actionClass.getCanonicalName());

    		if (Debug.ON)
    			Debug.trace("     Writing mapping of "  + actionClass.getSimpleName() + " to " + keyStrokes[0] + " and " + keyStrokes[1]);

    		if (keyStrokes[0] != null)
    			attributes.add(PRIMARY_KEYSTROKE_ATTRIBUTE, KeyStrokeUtils.getKeyStrokeRepresentation(keyStrokes[0]));

    		if (keyStrokes[1] != null)
    			attributes.add(ALTERNATE_KEYSTROKE_ATTRIBUTE, KeyStrokeUtils.getKeyStrokeRepresentation(keyStrokes[1]));
    		
    		writer.writeStandAloneElement(ACTION_ELEMENT, attributes);
    	}
    }
}
