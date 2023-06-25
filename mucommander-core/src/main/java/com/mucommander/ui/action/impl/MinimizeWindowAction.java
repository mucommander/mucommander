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

import javax.swing.JFrame;

import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.desktop.ActionType;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;

/**
 * Minimizes the {@link MainFrame} this action is associated with.
 *
 * @author Maxence Bernard
 * @see com.mucommander.ui.action.impl.MaximizeWindowAction
 */
public class MinimizeWindowAction extends MuAction {

    public MinimizeWindowAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        mainFrame.setExtendedState(JFrame.ICONIFIED);
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Descriptor extends AbstractActionDescriptor {
		public String getId() { return ActionType.MinimizeWindow.getId(); }

		public ActionCategory getCategory() { return ActionCategory.WINDOW; }

        @Override
        public String getLabel() {
            // Use a special label for Mac OS X, if it exists, use the standard action label otherwise
            String macLabelKey = ActionProperties.getActionLabelKey(ActionType.MinimizeWindow)+".mac_os_x";
            if(OsFamily.MAC_OS.isCurrent() && Translator.hasValue(macLabelKey, false))
                return Translator.get(macLabelKey);

            return super.getLabel();
        }
    }
}
