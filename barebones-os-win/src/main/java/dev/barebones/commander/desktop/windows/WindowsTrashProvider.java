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

package dev.barebones.commander.desktop.windows;

import dev.barebones.commander.commons.file.util.Shell32;
import dev.barebones.commander.commons.runtime.OsFamily;
import dev.barebones.commander.desktop.AbstractTrash;
import dev.barebones.commander.desktop.TrashProvider;

/**
 * This class is a trash provider for the {@link WindowsTrash Windows trash}.
 *
 * @see WindowsTrash
 * @author Maxence Bernard
 */
public class WindowsTrashProvider implements TrashProvider {

    public AbstractTrash getTrash() {
        return new WindowsTrash();
    }

    /**
     * Returns <code>true</code> if the Windows Trash can be used on the current runtime environment.
     *
     * @return <code>true</code> if the Windows Trash can be used on the current runtime environment.
     */
    public static boolean isAvailable() {
        return OsFamily.WINDOWS.isCurrent() && Shell32.isAvailable();
    }
}
