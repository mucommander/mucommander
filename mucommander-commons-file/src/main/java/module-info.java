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

module org.mucommander.commons.file {
    requires org.slf4j;
    requires org.mucommander.commons.io;
    requires org.mucommander.commons.runtime;
    requires org.mucommander.commons.util;
    requires org.apache.commons.collections4;
    requires org.apache.commons.lang3;
    requires java.desktop;
    requires com.ibm.icu; // ICU4J for locale-aware collation
    requires com.sun.jna;
    requires com.sun.jna.platform;

    // Core file system API
    exports com.mucommander.commons.file;
    exports com.mucommander.commons.file.archive;
    exports com.mucommander.commons.file.connection;
    exports com.mucommander.commons.file.filter;
    exports com.mucommander.commons.file.icon;
    exports com.mucommander.commons.file.icon.impl;
    exports com.mucommander.commons.file.protocol;
    exports com.mucommander.commons.file.protocol.local;
    exports com.mucommander.commons.file.protocol.search;
    exports com.mucommander.commons.file.util;

    // Service interfaces for plugin system
    exports com.mucommander.commons.file.module;

    // Service declarations for protocol and format providers
    uses com.mucommander.commons.file.module.FileProtocolService;
    uses com.mucommander.commons.file.module.FileFormatService;
}
