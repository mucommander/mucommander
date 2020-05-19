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

package com.mucommander.protocol.ui;

import javax.swing.JFrame;

/**
 * Provider of connectivity panel for a file protocol.
 * @author Arik Hadas
 */
public interface ProtocolPanelProvider {

	String getSchema();
	ServerPanel get(ServerPanelListener listener, JFrame mainFrame);
	int priority();
	/**
	 * This method should return the {@link Class} of the panel only when
	 * a shortcut for the panel should appear in the drive popup buttons.
	 * @return the {@link Class} of the panel in case a shortcut should
	 * appear in the drive popup button, null otherwise.
	 */
	default Class<? extends ServerPanel> getPanelClass() { return null; }
}
