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

import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.action.NoIcon;
import dev.barebones.commander.ui.main.MainFrame;
import dev.barebones.commander.ui.main.table.Column;

/**
 * Shows/hides the 'Group' column of the currently active FileTable. If the column is currently visible, this action
 * will hide it and vice-versa.
 *
 * @author Maxence Bernard
 */
public class ToggleGroupColumnAction extends ToggleColumnAction {

    public ToggleGroupColumnAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties, Column.GROUP);
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    @NoIcon
    public static class Descriptor extends ToggleColumnAction.Descriptor {
        public Descriptor() {
            super(Column.GROUP);
        }
    }
}
