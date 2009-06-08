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

package com.mucommander.ui.action.impl;

import com.mucommander.desktop.DesktopManager;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.MuActionFactory;
import com.mucommander.ui.dialog.ErrorDialog;
import com.mucommander.ui.main.MainFrame;

import java.net.URL;
import java.util.Hashtable;

/**
 * This action opens a URL in the system's default browser. This action is enabled only if the OS/Window manager
 * is capable of doing do.
 *
 * @author Maxence Bernard
 */
public class OpenURLInBrowserAction extends MuAction {

    /** Key to the URL property */
    public final static String URL_PROPERTY_KEY = "url";

    public OpenURLInBrowserAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        // Enable this action only if the current platform is capable of opening URLs in the default browser.
        setEnabled(DesktopManager.canBrowse());
    }

    public void performAction() {
        Object url = getValue(URL_PROPERTY_KEY);

        if(url!=null && (url instanceof String)) {
            try {
                DesktopManager.browse(new URL((String)url));
            }
            catch(Exception e) {
                ErrorDialog.showErrorDialog(mainFrame);
            }
        }
    }
    
    public static class Factory implements MuActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable properties) {
			return new OpenURLInBrowserAction(mainFrame, properties);
		}
    }
}
