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

import com.mucommander.module.OperatingSystemService;

/**
 * macOS operating system integration module.
 * <p>
 * Provides macOS-specific features: Trash, top-level menu, file metadata.
 * Note: dd-plist library is non-modular, accessed via --add-reads
 */
module org.mucommander.os.macos {
    requires org.slf4j;
    requires org.mucommander.command;
    requires org.mucommander.commons.file;
    requires org.mucommander.commons.runtime;
    requires org.mucommander.commons.util;
    requires org.mucommander.core;
    requires org.mucommander.os.api;
    requires org.mucommander.preferences;
    requires org.mucommander.process;
    requires org.mucommander.translator;
    requires java.desktop;
    requires com.sun.jna;
    requires com.sun.jna.platform;

    exports com.mucommander.desktop.macos;

    uses com.mucommander.os.api.CoreService;

    provides OperatingSystemService
        with com.mucommander.desktop.macos.MacOSOperatingSystemServiceProvider;
}
