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
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;

/**
 * If there is more than one window currently open, this action disposes the currently active MainFrame
 * (i.e. the one this action is attached to). On the contrary, if there is only one MainFrame currently open, this
 * action performs {@link com.mucommander.ui.action.impl.QuitAction} to quit the application after confirmation by the user,
 * if the quit confirmation has not been disabled.
 *
 * @author Maxence Bernard
 */
public class CloseWindowAction extends MuAction {

    public CloseWindowAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        // Closing the last window is equivalent to quitting the application: perform QuitAction in that case
        if(WindowManager.getMainFrames().size()==1)
            ActionManager.performAction(QuitAction.Descriptor.ACTION_ID, mainFrame);
        // Simply dispose the MainFrame
        else
            mainFrame.dispose();
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "CloseWindow";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategory.WINDOW; }

		public KeyStroke getDefaultAltKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.META_DOWN_MASK); }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0); }
    }
}
