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
import dev.barebones.commander.ui.action.InvokesDialog;
import dev.barebones.commander.ui.action.MuAction;
import dev.barebones.commander.ui.dialog.auth.EditCredentialsDialog;
import dev.barebones.commander.ui.main.MainFrame;

/**
 * This action brings up the 'Edit credentials' dialog that allows to edit persistent credentials (the ones stored
 * to disk).
 *
 * @author Maxence Bernard
 */
@InvokesDialog
public class EditCredentialsAction extends MuAction {

    public EditCredentialsAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        new EditCredentialsDialog(mainFrame);
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Descriptor extends AbstractActionDescriptor {
		public String getId() { return ActionType.EditCredentials.getId(); }

		public ActionCategory getCategory() { return ActionCategory.NAVIGATION; }
    }
}
