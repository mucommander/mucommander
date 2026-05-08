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

/**
 * Recalls window number 8 (brings it to the front).
 *
 * @author Maxence Bernard
 */
public class RecallWindow8Action extends RecallWindowAction {

    public RecallWindow8Action(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties, 8);
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    @NoIcon
    public static class Descriptor extends RecallWindowAction.Descriptor {
        public Descriptor() {
            super(8);
        }
    }
}
