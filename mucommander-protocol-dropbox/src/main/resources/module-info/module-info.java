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

module org.mucommander.protocol.dropbox {
    requires org.slf4j;
    requires org.mucommander.commons.file;
    requires org.mucommander.commons.io;
    requires org.mucommander.protocol.api;
    requires org.mucommander.encoding;
    requires org.mucommander.translator;
    requires org.mucommander.preferences;
    requires java.desktop;

    // Export protocol implementation
    exports com.mucommander.commons.file.protocol.dropbox;

    // Provide protocol services
    provides com.mucommander.commons.file.module.FileProtocolService
        with com.mucommander.commons.file.protocol.dropbox.DropboxProtocolServiceProvider;

    provides com.mucommander.protocol.ui.ProtocolPanelProvider
        with com.mucommander.commons.file.protocol.dropbox.DropboxProtocolPanelProvider;
}
