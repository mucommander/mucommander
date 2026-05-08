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

package dev.barebones.commander.ui.action.impl;

import java.util.Map;

import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.ui.action.AbstractActionDescriptor;
import dev.barebones.commander.ui.action.ActionCategory;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.action.MuAction;
import dev.barebones.commander.ui.main.MainFrame;
import dev.barebones.commander.ui.main.WindowManager;
import dev.barebones.commander.ui.main.frame.ClonedMainFrameBuilder;

/**
 * This action creates a new muCommander window.
 *
 * @author Maxence Bernard
 */
public class NewWindowAction extends MuAction {

    public NewWindowAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);

        // This action must be performed in a separate thread as it will otherwise lock the AWT event thread
        setPerformActionInSeparateThread(true);
    }

    @Override
    public void performAction() {
        WindowManager.createNewMainFrame(new ClonedMainFrameBuilder());
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Descriptor extends AbstractActionDescriptor {
		public String getId() { return ActionType.NewWindow.getId(); }

		public ActionCategory getCategory() { return ActionCategory.WINDOW; }
    }
}
