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

/**
 * Android Debug Bridge (ADB) protocol implementation module.
 * <p>
 * Provides ADB access to Android devices via jadb library.
 * Note: jadb is non-modular, accessed via --add-reads
 */
module org.mucommander.protocol.adb {
    requires org.slf4j;
    requires org.mucommander.commons.file;
    requires org.mucommander.commons.runtime;
    requires org.mucommander.protocol.api;
    requires org.mucommander.translator;
    requires org.mucommander.core;
    requires java.desktop;

    exports com.mucommander.commons.file.protocol.adb;

    provides com.mucommander.commons.file.module.FileProtocolService
        with com.mucommander.commons.file.protocol.adb.AdbProtocolServiceProvider;
    provides BrowsableItemsMenuService
        with com.mucommander.commons.file.protocol.adb.AdbMenuServiceProvider;
}
