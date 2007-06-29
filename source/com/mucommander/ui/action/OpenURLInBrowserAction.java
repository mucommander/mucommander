/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.ui.action;

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * This action opens a URL in the system's default browser. This action is enabled only if the OS/Window manager
 * is capable of doing do.
 * The URL to open must
 *
 *
 *
 * @author Maxence Bernard
 */
public class OpenURLInBrowserAction extends MucoAction {

    public final static String URL_PROPERTY_KEY = "url";

    
    public OpenURLInBrowserAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        // Enable this action only if the current platform is capable of opening URLs in the default browser.
        setEnabled(PlatformManager.canOpenUrl());
    }

    
    public void performAction() {
        Object url = getValue(URL_PROPERTY_KEY);

        if(url!=null && (url instanceof String)) {
            AbstractFile file = FileFactory.getFile((String)url);

            if(file!=null)
                PlatformManager.openUrl(file);
        }
    }
}
