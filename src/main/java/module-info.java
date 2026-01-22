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
import com.mucommander.translator.TranslationService;

module org.mucommander {
    // Java platform modules
    requires java.desktop;
    requires java.logging;
    requires java.management;

    // muCommander modules
    requires org.mucommander.core;
    requires org.mucommander.commons.file;
    requires org.mucommander.commons.conf;
    requires org.mucommander.commons.collections;
    requires org.mucommander.commons.io;
    requires org.mucommander.commons.runtime;
    requires org.mucommander.commons.util;
    requires org.mucommander.command;
    requires org.mucommander.encoding;
    requires org.mucommander.preferences;
    requires org.mucommander.process;
    requires org.mucommander.translator;
    requires org.mucommander.protocol.api;
    requires org.mucommander.os.api;
    requires org.mucommander.viewer.api;
    requires org.mucommander.core.preload;

    // External dependencies (automatic modules)
    requires jcommander;
    requires com.formdev.flatlaf;
    requires vaqua;
    requires jetbrains.jediterm;

    // Logging dependencies
    requires org.slf4j;
    requires logback.classic;
    requires logback.core;

    // Open main package to jcommander for deep reflection access to Configuration
    opens com.mucommander.main to jcommander;

    // Service consumption - load all plugin services
    uses com.mucommander.commons.file.module.FileProtocolService;
    uses com.mucommander.commons.file.module.FileFormatService;
    uses com.mucommander.viewer.FileViewerService;
    uses com.mucommander.viewer.FileEditorService;
    uses OperatingSystemService;
    uses com.mucommander.protocol.ui.ProtocolPanelProvider;
    uses TranslationService;
}
