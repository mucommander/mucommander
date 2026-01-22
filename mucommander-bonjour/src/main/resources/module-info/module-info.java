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

import com.mucommander.module.BrowsableItemsMenuService;

module org.mucommander.bonjour {
    requires org.mucommander.core;
    requires org.mucommander.commons.file;
    requires org.mucommander.commons.conf;
    requires org.mucommander.translator;
    requires org.mucommander.preferences;
    requires java.desktop;
    requires jmdns;

    provides BrowsableItemsMenuService
        with com.mucommander.bonjour.BonjourMenuService;
}
