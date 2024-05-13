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

package com.mucommander.ui.dialog.debug;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.utils.MuLogging.LogLevel;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.LayoutBase;

/**
 * This <code>java.util.logging</code> <code>Handler</code> collects the last log messages that were published by
 * the different muCommander loggers, so they can be displayed at any time in the {@link DebugConsoleDialog}.
 * Log records are kept in memory as a sliding window. The number of log records is controlled by the
 * {@link MuPreferences#LOG_BUFFER_SIZE} configuration variable: the more records, the more memory is used.
 *
 * @see DebugConsoleDialog
 * @see MuPreferences#LOG_BUFFER_SIZE
 * @author Maxence Bernard, Arik Hadas
 */
public class DebugConsoleAppender extends AppenderBase<ILoggingEvent> {

    /** Maximum number of log records to keep in memory */
    private int bufferSize;

    /** Contains the last LogRecord instances. */
    private List<LogbackLoggingEvent> loggingEventsList;
    
    /** The layout of the logging event representation */
    private Layout<ILoggingEvent> loggingEventLayout;

    /**
     * Creates a new <code>DebugConsoleHandler</code>. This constructor is automatically by
     * <code>java.util.logging</code> when it is configured and should never be called directly.
     */
    public DebugConsoleAppender() {
        this.loggingEventLayout = new CustomLoggingLayout();

        bufferSize = MuConfigurations.getPreferences().getVariable(MuPreference.LOG_BUFFER_SIZE, MuPreferences.DEFAULT_LOG_BUFFER_SIZE);
        loggingEventsList = new LinkedList<>();
    }

    /**
     * Returns the last records that were collected by this handler.
     *
     * @return the last records that were collected by this handler.
     */
    public synchronized LoggingEvent[] getLogRecords() {
        LogbackLoggingEvent[] records = new LogbackLoggingEvent[0];
        records = loggingEventsList.toArray(records);

        return records;
    }


    /////////////////////////////
    // Appender implementation //
    /////////////////////////////

    @Override
    protected void append(ILoggingEvent record) {
        if (loggingEventsList.size() == bufferSize) {
            loggingEventsList.remove(0);
        }

        loggingEventsList.add(new LogbackLoggingEvent(record));
    }

    /**
     * Wraps a {@link ILoggingEvent} and overrides {@link #toString()} to have it return a properly formatted string
     * representation of it so that it can be displayed in a {@link javax.swing.JList} or {@link javax.swing.JTable} and
     * pasted to the clipboard.
     * It also implements the LoggingEvent interface so that the logging event can be presented in the debug console.
     */
    public class LogbackLoggingEvent implements LoggingEvent {

        /** The logging event */
        private ILoggingEvent loggingEvent;

        /** The log level of the event in mucommander's terms */
        private LogLevel logLevel;

        LogbackLoggingEvent(ILoggingEvent lr) {
            this.loggingEvent = lr;
        }

        /**
         * Returns a properly formatted string representation of the {@link ILoggingEvent}.
         * 
         * @return a properly formatted string representation of the {@link ILoggingEvent}.
         */
        @Override
        public String toString() {
            return loggingEventLayout.doLayout(loggingEvent);
        }
        
        
        ///////////////////////////////////////
        /// LogRecordListItem Implementation //
        ///////////////////////////////////////
        
        public boolean isLevelEqualOrHigherThan(LogLevel level) {
            return getLevel().compareTo(level) <= 0;
        }
        
        public LogLevel getLevel() {
            if (logLevel == null) {
                logLevel = getLogLevel(loggingEvent);
            }
            return logLevel;
        }
    }

    /**
     * Returns the log level, in mucommander terms, that match the level of a given logback logging event
     *
     * @param loggingEvent logback logging event
     * @return log level, in mucommander terms, that match the level of the given logback logging event
     */
    public static LogLevel getLogLevel(ILoggingEvent loggingEvent) {
        return LogLevel.valueOf(loggingEvent.getLevel());
    }

    private static class CustomLoggingLayout extends LayoutBase<ILoggingEvent> {

        private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        public String doLayout(ILoggingEvent event) {
            StackTraceElement stackTraceElement = event.getCallerData()[0];

            StringBuilder sbuf = new StringBuilder(128);
            sbuf.append("[");
            sbuf.append(SIMPLE_DATE_FORMAT.format(new Date(event.getTimeStamp())));
            sbuf.append("] ");
            sbuf.append(getLogLevel(event));
            sbuf.append(" ");
            sbuf.append(stackTraceElement.getFileName());
            sbuf.append("#");
            sbuf.append(stackTraceElement.getMethodName());
            sbuf.append(",");
            sbuf.append(stackTraceElement.getLineNumber());
            sbuf.append(" ");
            sbuf.append(event.getFormattedMessage());
            sbuf.append(CoreConstants.LINE_SEPARATOR);

            IThrowableProxy throwableProxy = event.getThrowableProxy();
            if (throwableProxy != null) {
                String throwableStr = ThrowableProxyUtil.asString(throwableProxy);
                sbuf.append(throwableStr);
                sbuf.append(CoreConstants.LINE_SEPARATOR);
            }
            return sbuf.toString();
        }
    }
}
