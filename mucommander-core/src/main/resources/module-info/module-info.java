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

import com.mucommander.translator.TranslationService;

open module org.mucommander.core {
    // Java modules
    requires java.desktop;
    requires java.management;
    requires java.rmi;
    requires java.security.jgss;
    requires java.sql;
    requires java.transaction.xa;
    requires java.xml;
    requires java.logging;

    // Third-party modules (from unnamed module)
    requires org.slf4j;
    requires org.apache.commons.collections4;

    // Optional L&F modules (from unnamed module)
    requires static com.formdev.flatlaf;

    // Internal mucommander modules
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

    // Export all packages from the analysis
    // Note: com.mucommander.translator is exported by org.mucommander.translator
    // Note: com.mucommander.ui.encoding is exported by org.mucommander.encoding
    exports com.mucommander;
    exports com.mucommander.auth;
    exports com.mucommander.bookmark;
    exports com.mucommander.core.desktop;
    exports com.mucommander.job;
    exports com.mucommander.job.impl;
    exports com.mucommander.module;
    exports com.mucommander.search;
    exports com.mucommander.ui.action;
    exports com.mucommander.ui.action.impl;
    exports com.mucommander.ui.dialog;
    exports com.mucommander.ui.dialog.file;
    exports com.mucommander.ui.dialog.server;
    exports com.mucommander.ui.icon;
    exports com.mucommander.ui.text;
    exports com.mucommander.ui.theme;
    exports com.mucommander.snapshot;
    exports com.mucommander.ui.main;
    exports com.mucommander.ui.main.table;
    exports com.mucommander.ui.viewer;

    // Service consumption
    uses com.mucommander.viewer.FileViewerService;
    uses com.mucommander.viewer.FileEditorService;
    uses TranslationService;
    uses com.mucommander.protocol.ui.ProtocolPanelProvider;
    uses com.mucommander.module.OperatingSystemService;
    uses com.mucommander.module.BrowsableItemsMenuService;

    // Service provision
    provides com.mucommander.os.api.CoreService
        with com.mucommander.CoreServiceImpl;
    provides com.mucommander.commons.file.module.FileProtocolService
        with com.mucommander.bookmark.BookmarkProtocolServiceProvider;
}
