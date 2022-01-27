/*
 * This file is part of muCommander, http://www.mucommander.com
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

import java.net.URL;
import java.util.Map;

import javax.swing.KeyStroke;

import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.main.MainFrame;

/**
 * This action opens a URL in the system's default browser. This action is enabled only if the OS/Window manager
 * is capable of doing do.
 *
 * @author Maxence Bernard
 */
public class OpenURLInBrowserAction extends MuAction {

    /** Key to the URL property */
    public final static String URL_PROPERTY_KEY = "url";

    public OpenURLInBrowserAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);

        // Enable this action only if the current platform is capable of opening URLs in the default browser.
        setEnabled(DesktopManager.canBrowse());
    }

    @Override
    public void performAction() {
        Object url = getValue(URL_PROPERTY_KEY);

        if(url instanceof String) {
            try {
                InformationDialog.showErrorDialogIfNeeded(getMainFrame(), DesktopManager.browse(new URL((String)url)));
            }
            catch(Exception e) {
                InformationDialog.showErrorDialog(mainFrame);
            }
        }
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "OpenURLInBrowser";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return null; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }
		
		public KeyStroke getDefaultKeyStroke() { return null; }

        @Override
        public boolean isParameterized() { return true; }
    }
}
