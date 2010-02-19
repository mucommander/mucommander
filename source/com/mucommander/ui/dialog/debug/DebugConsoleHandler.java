/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

import com.mucommander.commons.log.SingleLineFormatter;
import com.mucommander.conf.impl.MuConfiguration;

import java.util.LinkedList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * This <code>java.util.logging</code> <code>Handler</code> collects the last log messages that were published by
 * the different muCommander loggers, so they can be displayed at any time in the {@link DebugConsoleDialog}.
 * Log records are kept in memory as a sliding window. The number of log records is controlled by the
 * {@link MuConfiguration#LOG_BUFFER_SIZE} configuration variable: the more records, the more memory is used.
 *
 * @see DebugConsoleDialog
 * @see MuConfiguration#LOG_BUFFER_SIZE
 * @author Maxence Bernard
 */
public class DebugConsoleHandler extends Handler {

    /** Maximum number of log records to keep in memory */
    private int bufferSize;

    /** Contains the last LogRecord instances. */
    private LinkedList<LogRecord> logRecords;

    /** Singleton instance of DebugConsoleHandler */
    private static DebugConsoleHandler INSTANCE;

    /**
     * Creates a new <code>DebugConsoleHandler</code>. This constructor is automatically by
     * <code>java.util.logging</code> when it is configured and should never be called directly.
     */
    public DebugConsoleHandler() {
        setFormatter(new SingleLineFormatter());

        bufferSize = MuConfiguration.getVariable(MuConfiguration.LOG_BUFFER_SIZE, MuConfiguration.DEFAULT_LOG_BUFFER_SIZE);
        logRecords = new LinkedList<LogRecord>();

        INSTANCE = this;
    }

    /**
     * Returns a singleton instance of {@link }DebugConsoleHandler}.
     *
     * @return a singleton instance of {@link }DebugConsoleHandler}.
     */
    public static DebugConsoleHandler getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the last records that were collected by this handler.
     *
     * @return the last records that were collected by this handler.
     */
    public synchronized LogRecord[] getLogRecords() {
        LogRecord[] records = new LogRecord[logRecords.size()];
        logRecords.toArray(records);

        return records;
    }


    ////////////////////////////
    // Handler implementation //
    ////////////////////////////

    @Override
    public synchronized void publish(LogRecord record) {
        if(logRecords.size()== bufferSize)
            logRecords.removeFirst();

        logRecords.add(record);
    }

    @Override
    public synchronized void flush() {
    }

    @Override
    public synchronized void close() throws SecurityException {
        logRecords.clear();
    }
}
