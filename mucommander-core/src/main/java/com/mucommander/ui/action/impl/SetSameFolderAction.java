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

import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.KeyStroke;

import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.event.ActivePanelListener;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

/**
 * This action equalizes both FileTable's current folders: the 'inactive' FileTable's current folder becomes
 * the active FileTable's one.
 *
 * @author Maxence Bernard
 */
public class SetSameFolderAction extends MuAction implements ActivePanelListener {

    public SetSameFolderAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);

        mainFrame.addActivePanelListener(this);
        
        toggleEnabledState();
    }
    
    /**
     * Enables or disables this action based on the tab in the other panel being not lock,
     * this action will be enabled, if not it will be disabled.
     */
    private void toggleEnabledState() {
        setEnabled(!mainFrame.getInactivePanel().getTabs().getCurrentTab().isLocked());
    }
    
    public void activePanelChanged(FolderPanel folderPanel) {
    	toggleEnabledState();
	}

    @Override
    public void performAction() {
        mainFrame.setSameFolder();
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "SetSameFolder";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategory.VIEW; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK); }
    }
}
