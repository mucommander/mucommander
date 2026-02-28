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

package com.mucommander.ui.action.impl;

import com.mucommander.Activator;
import com.mucommander.RuntimeConstants;
import com.mucommander.core.desktop.InternalBrowse;
import com.mucommander.desktop.ActionType;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.dialog.debug.LoggingEvent;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.utils.MuLogging;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

/**
 * This action opens the mucommander.com bug repository URL in the system's default browser.
 *
 * @author Maxence Bernard
 */
public class ReportBugAction extends OpenURLInBrowserAction {

    private static final String EXTENDED_BUG_FORMAT = "%s?" +
            "labels=bug&" +
            "template=bug_report.yml&" +
            "title=[Bug]%%20&" +
            "version=%s&" +
            "java-version=%s&" +
            "os-version=%s&" +
            "logs=%s";

    /**
     * A number of last log entries to be considered to be included in bug report
     * (see MAX_REPORTED_LOG_SIZE below).
     */
    private static final int LAST_LOG_ENTRIES = 5;

    /**
     * The max number of characters to be included in bug report (it may truncate
     * the log entries).
     */
    private static final int MAX_REPORTED_LOG_SIZE = 1000;

    public ReportBugAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        var url = com.mucommander.RuntimeConstants.REPORT_BUG_URL;
        try {
            var muCVersion = String.format(Activator.portable ? "%s (%s)" : "%s", RuntimeConstants.VERSION, "portable");
            var javaVersion = String.join(System.lineSeparator(),
                    System.getProperty("java.version"),
                    System.getProperty("java.vm.name"),
                    System.getProperty("java.vm.version"),
                    System.getProperty("java.vm.vendor"));
            var osVersion = String.join(System.lineSeparator(),
                    System.getProperty("os.name"),
                    System.getProperty("os.version"),
                    System.getProperty("os.arch"));

            var records = Arrays.asList(MuLogging.getDebugConsoleAppender().getLogRecords());
            var lastNRecords = records.subList(Math.max(records.size() - LAST_LOG_ENTRIES, 0), records.size());
            var logRecords = new StringBuilder();
            for (LoggingEvent record : lastNRecords) {
                logRecords.append(record.toString());
            }
            var extendedBugUrl = String.format(EXTENDED_BUG_FORMAT, url,
                    URLEncoder.encode(muCVersion, StandardCharsets.UTF_8),
                    URLEncoder.encode(javaVersion, StandardCharsets.UTF_8),
                    URLEncoder.encode(osVersion, StandardCharsets.UTF_8),
                    URLEncoder.encode("[✂]\n" + logRecords.toString()
                            .substring(0, Math.min(MAX_REPORTED_LOG_SIZE, logRecords.length())).trim() + "\n[✂]",
                            StandardCharsets.UTF_8));
            putValue(URL_PROPERTY_KEY, extendedBugUrl);

            // Force the use of InternalBrowse instead of CommandBrowse to avoid issues with shell escaping on some platforms (e.g. Linux).
            // See https://github.com/mucommander/mucommander/issues/1417 for details on this workaround.
            var internalBrowse = new InternalBrowse();
            if (internalBrowse.isAvailable()) {
                LOGGER.trace("Using InternalBrowse to open the bug report URL: {}", extendedBugUrl);
                InformationDialog.showErrorDialogIfNeeded(getMainFrame().getJFrame(),
                        internalBrowse.execute(new java.net.URI(extendedBugUrl).toURL()));
            } else {
                LOGGER.debug("Using safe fallback to open the bug report URL: {}", url);
                putValue(URL_PROPERTY_KEY, url);
                // uses DesktopManager.browse() as safe fallback and opens the generic bug report URL without pre-filling the form.
                super.performAction();
            }
        } catch (Exception e) {
            LOGGER.error("Error while preparing a bug report, falling back to a generic one", e);
            putValue(URL_PROPERTY_KEY, url);
            // uses DesktopManager.browse() as safe fallback and opens the generic bug report URL without pre-filling the form.
            super.performAction();
        }
    }

    public static class Descriptor extends AbstractActionDescriptor {
        public String getId() { return ActionType.ReportBug.getId(); }

        public ActionCategory getCategory() { return ActionCategory.MISC; }
    }
}
