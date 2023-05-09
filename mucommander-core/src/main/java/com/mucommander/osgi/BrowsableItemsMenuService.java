/**
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.osgi;

import javax.swing.JMenu;

import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

/**
 * Service that provides a {@link JMenu} that contains browsable items.
 * Those menus are added to the main menu-bar and the drive popup window.
 * @author Arik Hadas
 */
public interface BrowsableItemsMenuService {

    /**
     * Returns a menu with browsable items
     * @param frame MainFrame
     * @return JMenu that contains browsable items
     */
    JMenu getMenu(MainFrame frame, FolderPanel folderPanel);
}
