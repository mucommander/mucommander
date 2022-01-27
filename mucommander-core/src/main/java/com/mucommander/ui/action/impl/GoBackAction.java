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
import com.mucommander.ui.main.MainFrame;

/**
 * This action recalls the previous folder in the current FolderPanel's history.
 *
 * @author Maxence Bernard
 */
public class GoBackAction extends ActiveTabAction {

    public GoBackAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);
    }


    @Override
    public void performAction() {
        mainFrame.getActivePanel().getFolderHistory().goBack();
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    /**
     * Enables or disables this action based on the history of the currently active FolderPanel: if there is a previous
     * folder in the history and the current tab is not locked, this action will be enabled, if not it will be disabled.
     */
    @Override
    protected void toggleEnabledState() {
        setEnabled(mainFrame.getActivePanel().getFolderHistory().hasBackFolder() &&
        		  !mainFrame.getActivePanel().getTabs().getCurrentTab().isLocked());
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "GoBack";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategory.NAVIGATION; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK); }
    }
}
