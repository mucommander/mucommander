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

package dev.barebones.commander.desktop;

/**
 * TrashProvider provides a way to instantiate {@link AbstractTrash} implementations.
 *
 * <p>Trash providers can be registered with {@link dev.barebones.commander.core.desktop.DesktopManager#setTrashProvider(TrashProvider)}
 * for them to become the default trash one.</p>
 *
 * @see dev.barebones.commander.desktop.AbstractTrash
 * @see dev.barebones.commander.core.desktop.DesktopManager#setTrashProvider(TrashProvider)
 * @author Nicolas Rinaudo
 */
public interface TrashProvider {

    /**
     * Returns a trash instance.
     * 
     * @return a trash instance
     */
    public AbstractTrash getTrash();

}
