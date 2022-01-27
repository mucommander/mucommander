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
 * This action unmarks all files in the current file table.
 *
 * @author Maxence Bernard
 */
public class UnmarkAllAction extends MarkAllAction {

    public UnmarkAllAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties, false);
    }

    @Override
    public ActionDescriptor getDescriptor() {
    	return new Descriptor();
    }

    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "UnmarkAll";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategory.SELECTION; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK); }
    }
}
