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
package com.mucommander.commons.file.protocol.adb;

import javax.swing.JMenu;

import com.mucommander.module.BrowsableItemsMenuService;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

/**
 * JPMS service provider for ADB menu.
 *
 * @author Arik Hadas
 */
public class AdbMenuServiceProvider implements BrowsableItemsMenuService {

    @Override
    public JMenu getMenu(MainFrame frame, FolderPanel folderPanel) {
        return new AndroidMenu(frame, folderPanel);
    }
}
