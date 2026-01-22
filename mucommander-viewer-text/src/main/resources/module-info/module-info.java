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

module org.mucommander.viewer.text {
    requires org.slf4j;
    requires org.mucommander.viewer.api;
    requires org.mucommander.commons.file;
    requires org.mucommander.commons.util;
    requires org.mucommander.commons.io;
    requires org.mucommander.commons.runtime;
    requires org.mucommander.commons.conf;
    requires org.mucommander.translator;
    requires org.mucommander.encoding;
    requires org.mucommander.preferences;
    requires org.mucommander.os.api;
    requires org.mucommander.core;
    requires java.desktop;

    exports com.mucommander.viewer.text;

    provides com.mucommander.viewer.FileViewerService with com.mucommander.viewer.text.TextFileViewerServiceProvider;
    provides com.mucommander.viewer.FileEditorService with com.mucommander.viewer.text.TextFileEditorServiceProvider;
}
