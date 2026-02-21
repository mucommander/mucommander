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

import java.util.Arrays;
import java.util.Map;

import com.mucommander.Activator;
import com.mucommander.RuntimeConstants;
import com.mucommander.desktop.ActionType;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.dialog.debug.LoggingEvent;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.utils.MuLogging;

/**
 * This action opens the mucommander.com bug repository URL in the system's default browser.
 *
 * @author Maxence Bernard
 */
public class ReportBugAction extends OpenURLInBrowserAction {

    private static final String NEW_BUG_FORMAT = "%s?" +
            "labels=bug&" +
            "template=bug_report.yml&" +
            "title=[Bug] &" +
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
            var newBugUrl = String.format(NEW_BUG_FORMAT, url,
                    muCVersion,
                    javaVersion,
                    osVersion,
                    "[✂]\n" + logRecords.toString()
                            .substring(0, Math.min(MAX_REPORTED_LOG_SIZE, logRecords.length())) + "\n[✂]");
            putValue(URL_PROPERTY_KEY, newBugUrl);
        } catch (Exception e) {
            LOGGER.error("Error while preparing a bug report, falling back to a generic one", e);
            putValue(URL_PROPERTY_KEY, url);
        }
        super.performAction();
    }

    public static class Descriptor extends AbstractActionDescriptor {
        public String getId() { return ActionType.ReportBug.getId(); }

        public ActionCategory getCategory() { return ActionCategory.MISC; }
    }
}
