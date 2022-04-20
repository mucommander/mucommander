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

import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.desktop.AbstractTrash;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;

/**
 * Opens the trash in the default file manager of the current OS/Desktop manager. This action is enabled only
 * if the current platform has an {@link com.mucommander.desktop.AbstractTrash} implementation and if it is capable
 * of opening the trash, as reported by {@link com.mucommander.desktop.AbstractTrash#canOpen()}.
 *
 * @author Maxence Bernard
 */
public class OpenTrashAction extends MuAction {

    public OpenTrashAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);

        AbstractTrash trash = DesktopManager.getTrash();
        setEnabled(trash != null && trash.canOpen());
    }

    @Override
    public void performAction() {
        AbstractTrash trash = DesktopManager.getTrash();
        if (trash != null) {
            trash.open();
        }
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "OpenTrash";

        public String getId() {
            return ACTION_ID;
        }

        public ActionCategory getCategory() {
            return ActionCategory.NAVIGATION;
        }

        public KeyStroke getDefaultAltKeyStroke() {
            return null;
        }

        public KeyStroke getDefaultKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.ALT_MASK);
        }
    }
}
