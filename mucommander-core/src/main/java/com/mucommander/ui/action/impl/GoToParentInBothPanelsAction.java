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

import java.util.Map;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.desktop.ActionType;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.main.MainFrame;

/**
 * Changes the current directory to its parent and tries to do the same in the inactive panel.
 * <p>
 * When possible, this action will open the active panel's current folder's parent. Additionally,
 * if the inactive panel's current folder has a parent, it will open that one as well.
 * </p>
 * <p>
 * Note that this action's behavior is strictly equivalent to that of {@link GoToParentAction} in the
 * active panel. Differences will only occur in the inactive panel, and then again only when possible.
 * </p>
 * <p>
 * This action opens both files synchronously: it will wait for the active panel location change confirmation
 * before performing the inactive one.
 * </p>
 * @author Nicolas Rinaudo
 */
public class GoToParentInBothPanelsAction extends ActiveTabAction {
    // - Initialization ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>GoToParentInBothPanelsAction</code> instance with the specified parameters.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public GoToParentInBothPanelsAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);

        // Perform this action in a separate thread, to avoid locking the event thread
        setPerformActionInSeparateThread(true);
    }

    /**
     * Enables or disables this action based on the currently active folder's
     * has a parent and both tabs in the two panel are not locked,
     * this action will be enabled, if not it will be disabled.
     */
    @Override
    protected void toggleEnabledState() {
        setEnabled(!mainFrame.getActivePanel().getTabs().getCurrentTab().isLocked() &&
        		   !mainFrame.getInactivePanel().getTabs().getCurrentTab().isLocked() &&
        		    mainFrame.getActivePanel().getCurrentFolder().getParent()!=null);
    }

    // - Action code ---------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Opens both the active and inactive folder panel's parent directories.
     */
    @Override
    public void performAction() {
        Thread       openThread;
        AbstractFile parent;

        // If the current panel has a parent file, navigate to it.
        if((parent = mainFrame.getActivePanel().getCurrentFolder().getParent()) != null) {
            openThread = mainFrame.getActivePanel().tryChangeCurrentFolder(parent);

            // If the inactive panel has a parent file, wait for the current panel change to be complete and navigate
            // to it.
            if((parent = mainFrame.getInactivePanel().getCurrentFolder().getParent()) != null) {
                if(openThread != null) {
                    while(openThread.isAlive()) {
                        try {openThread.join();}
                        catch(InterruptedException e) {}
                    }
                }
                mainFrame.getInactivePanel().tryChangeCurrentFolder(parent);
            }
        }
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Descriptor extends AbstractActionDescriptor {
		public String getId() { return ActionType.GoToParentInBothPanels.getId(); }

		public ActionCategory getCategory() { return ActionCategory.NAVIGATION; }
    }
}
