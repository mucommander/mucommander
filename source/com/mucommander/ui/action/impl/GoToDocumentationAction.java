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

package com.mucommander.ui.action.impl;

import com.mucommander.ui.action.*;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.MainFrame;

import javax.swing.KeyStroke;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;

/**
 * Opens the muCommander online documentation in the system's default browser. The {@link #TOPIC_PROPERTY_KEY}
 * property allows to specify a specific documentation topic which the browser will be sent to. If it is not defined,
 * the base documentation URL will be opened.
 *
 * @author Maxence Bernard
 */
public class GoToDocumentationAction extends OpenURLInBrowserAction implements PropertyChangeListener {

    /** Key to the topic property */
    public final static String TOPIC_PROPERTY_KEY = "topic";

    public GoToDocumentationAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
        super(mainFrame, properties);

        setIcon(IconManager.getIcon(IconManager.COMMON_ICON_SET, "help.png"));

        // Set the URL
        updateURL();

        // Listen to changes made to the topic property
        addPropertyChangeListener(this);
    }

    /**
     * Sets the URL to sent the browser to, using the base URL defined in the runtime constants and
     * the optional topic defined in the {@link #TOPIC_PROPERTY_KEY}. The URL is stored in the {@link #URL_PROPERTY_KEY}
     * property.
     */
    private void updateURL() {
        String url = com.mucommander.RuntimeConstants.DOCUMENTATION_URL;
        String topic = (String)getValue(TOPIC_PROPERTY_KEY);

        // If there is a topic, append it to the URL
        if(topic!=null) {
            if(url.endsWith("/"))
                url += "/";

            url += topic;
        }

        putValue(URL_PROPERTY_KEY, url);
    }


    ///////////////////////////////////////////
    // PropertyChangeListener implementation //
    ///////////////////////////////////////////

    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

        if(propertyChangeEvent.getPropertyName().equals(TOPIC_PROPERTY_KEY)) {
            updateURL();
        }
    }
    
    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
			return new GoToDocumentationAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "GoToDocumentation";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.MISC; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return null; }
    }
}
