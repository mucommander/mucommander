/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.RuntimeConstants;
import com.mucommander.io.backup.BackupOutputStream;
import com.mucommander.xml.XmlAttributes;
import com.mucommander.xml.XmlWriter;

/**
 * This class is responsible for writing the tool-bar attributes (buttons and separators).
 * 
 * @author Arik Hadas
 */
public class ToolBarWriter extends ToolBarIO {
	private static final Logger LOGGER = LoggerFactory.getLogger(ToolBarWriter.class);
	
	// - Singleton -------------------------------------------------------
    // -------------------------------------------------------------------
	private static ToolBarWriter instance;
	
	public static ToolBarWriter create() {
		if (instance == null)
			instance = new ToolBarWriter();
		return instance;
	}
	
	private ToolBarWriter() {}
	
	void write() throws IOException {
		String[] actionIds = ToolBarAttributes.getActions();
		
		BackupOutputStream bos = new BackupOutputStream(getDescriptionFile());
		try {
			new Writer(bos).write(actionIds);
			wasToolBarModified = false;
		} catch (Exception e) {
			LOGGER.debug("Caught exception", e);
		} finally {
			bos.close();
		}
	}
	
	private static class Writer {
		private XmlWriter writer = null;
		
		private Writer(OutputStream stream) throws IOException {
    		this.writer = new XmlWriter(stream);
    	}
		
		private void write(String[] actionIds) throws IOException {
			try {
				writer.writeCommentLine("See http://trac.mucommander.com/wiki/ToolBar for information on how to customize this file");
				
				XmlAttributes rootElementAttributes = new XmlAttributes();
				rootElementAttributes.add(VERSION_ATTRIBUTE, RuntimeConstants.VERSION);

    			writer.startElement(ROOT_ELEMENT, rootElementAttributes, true);    			
    			
    			int nbToolBarActions = actionIds.length;
    			for (int i=0; i<nbToolBarActions; ++i)
    				write(actionIds[i]);

    		} finally {
    			writer.endElement(ROOT_ELEMENT);
    		}
		}
		
		private void write(String actionId) throws IOException {
			if (actionId == null)
				writer.writeStandAloneElement(SEPARATOR_ELEMENT);
			else {
				XmlAttributes attributes = new XmlAttributes();
				attributes.add(ACTION_ID_ATTRIBUTE, actionId);

				// AppLogger.finest("Writing button: action_id = "  + attributes.getValue(ACTION_ATTRIBUTE_ID) + ", alt_action_id = " + attributes.getValue(ALT_ACTION_ATTRIBUTE_ID));

				writer.writeStandAloneElement(BUTTON_ELEMENT, attributes);
			}
		}
	}
}
