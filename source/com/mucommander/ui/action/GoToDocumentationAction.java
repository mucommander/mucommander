/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

import com.mucommander.runtime.OsFamilies;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.MainFrame;

import java.util.Hashtable;

/**
 * Opens the muCommander online documentation in the system's default browser. The {@link #TOPIC_PROPERTY_KEY}
 * property allows to specify a specific documentation topic which the browser will be sent to. If it is not defined,
 * the base documentation URL will be opened.
 *
 * @author Maxence Bernard
 */
public class GoToDocumentationAction extends OpenURLInBrowserAction {

    /** Key to the topic property */
    public final static String TOPIC_PROPERTY_KEY = "topic";

    public GoToDocumentationAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        // Construct the URL to sent the browser to, using the base URL defined in the runtime constants and
        // the optional topic defined as a property

        String url = com.mucommander.RuntimeConstants.DOCUMENTATION_URL;
        String topic = (String)properties.get(TOPIC_PROPERTY_KEY);

        // If there is a topic, append it to the URL
        if(topic!=null) {
            if(url.endsWith("/"))
                url += "/";

            url += topic;
        }

        putValue(URL_PROPERTY_KEY, url);

        setIcon(IconManager.getIcon(IconManager.COMMON_ICON_SET,
                OsFamilies.MAC_OS_X.isCurrent()?"help_mac.png":"help.png"));
    }
}
