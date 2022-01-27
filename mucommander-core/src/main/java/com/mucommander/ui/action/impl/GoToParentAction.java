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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

/**
 * This action changes the current folder of the currently active FolderPanel to the current folder's parent.
 * This action only gets enabled when the current folder has a parent and current tab is not locked.
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class GoToParentAction extends ActiveTabAction {
    /**
     * Creates a new <code>GoToParentAction</code> with the specified parameters.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public GoToParentAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);
    }


    /**
     * Enables or disables this action based on the currently active folder's
     * has a parent and current tab is not locked, this action will be enabled,
     * if not it will be disabled.
     */
    @Override
    protected void toggleEnabledState() {
        setEnabled(!mainFrame.getActivePanel().getTabs().getCurrentTab().isLocked() &&
        		    mainFrame.getActivePanel().getCurrentFolder().getParent()!=null);
    }



    ///////////////////////
    // Protected methods //
    ///////////////////////

    /**
     * Updates <code>panel</code>'s location to its parent.
     *
     * @param  panel in which to change the location.
     * @return <code>true</code> if <code>panel</code> has a parent, <code>false</code> otherwise.
     */
    protected boolean goToParent(FolderPanel panel) {
    	AbstractFile parent;

        if((parent = panel.getCurrentFolder().getParent()) != null) {
        	panel.tryChangeCurrentFolder(parent, null, true);
            return true;
        }
        return false;
    }



    /////////////////////////////
    // MuAction implementation //
    /////////////////////////////
    /**
     * Goes to the current location's parent in the active panel.
     */
    @Override
    public void performAction() {
        // Changes the current folder to make it the current folder's parent.
        // Does nothing if the current folder doesn't have a parent.
        goToParent(mainFrame.getActivePanel());
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "GoToParent";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategory.NAVIGATION; }

		public KeyStroke getDefaultAltKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0); }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0); }
    }
}
