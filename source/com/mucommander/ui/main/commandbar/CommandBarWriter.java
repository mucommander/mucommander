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
import com.mucommander.io.BackupOutputStream;
import com.mucommander.ui.text.KeyStrokeUtils;
import com.mucommander.xml.XmlAttributes;
import com.mucommander.xml.XmlWriter;

import javax.swing.KeyStroke;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is responsible for writing the command-bar attributes (actions and modifier).
 * 
 * @author Arik Hadas
 */
class CommandBarWriter extends CommandBarIO {

	// - Singleton -------------------------------------------------------
    // -------------------------------------------------------------------
	private static CommandBarWriter instance;
	
	public static CommandBarWriter create() {
		if (instance == null)
			instance = new CommandBarWriter();
		return instance;
	}
	
	private CommandBarWriter() {}
	
	void write() throws IOException {
		String[] commandBarActionIds = CommandBarAttributes.getActions();
		String[] commandBarAlterativeActionIds = CommandBarAttributes.getAlternateActions();
		KeyStroke commandBarModifier = CommandBarAttributes.getModifier();

		BackupOutputStream bos = new BackupOutputStream(getDescriptionFile());

		try {
			new Writer(bos).write(commandBarActionIds, commandBarAlterativeActionIds, commandBarModifier);
			wasCommandBarModified = false;
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
		
		private void write(String[] actionIds, String[] alternativeActionIds, KeyStroke modifier) throws IOException {
			try {
				writer.writeCommentLine("See http://trac.mucommander.com/wiki/CommandBar for information on how to customize this file");
				
				XmlAttributes rootElementAttributes = new XmlAttributes();
				rootElementAttributes.add(MODIFIER_ATTRIBUTE, KeyStrokeUtils.getKeyStrokeRepresentation(modifier));
				rootElementAttributes.add(VERSION_ATTRIBUTE, RuntimeConstants.VERSION);

    			writer.startElement(ROOT_ELEMENT, rootElementAttributes, true);    			
    			
    			int nbCommandBarActions = actionIds.length;
    			for (int i=0; i<nbCommandBarActions; ++i)
    				write(actionIds[i], alternativeActionIds[i]);

    		} finally {
    			writer.endElement(ROOT_ELEMENT);
    		}
		}
		
		private void write(String actionId, String alternativeActionId) throws IOException {
			XmlAttributes attributes = new XmlAttributes();
			attributes.add(ACTION_ID_ATTRIBUTE, actionId);
			if (alternativeActionId != null)
				attributes.add(ALT_ACTION_ID_ATTRIBUTE, alternativeActionId);
			
            AppLogger.finest("Writing button: action_id = "  + attributes.getValue(ACTION_ID_ATTRIBUTE) + ", alt_action_id = " + attributes.getValue(ALT_ACTION_ID_ATTRIBUTE));
			
			writer.writeStandAloneElement(BUTTON_ELEMENT, attributes);
		}
	}
}
