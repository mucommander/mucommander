/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import ch.qos.logback.classic.spi.ILoggingEvent;

import com.mucommander.MuLogger.Level;

/**
 * Wraps a {@link LogRecord} and overrides {@link #toString()} to have it return a properly formatted string
 * representation of it so that it can be displayed in a {@link javax.swing.JList} or {@link javax.swing.JTable} and
 * pasted to the clipboard.
 *
 * @author Maxence Bernard
 */
public class LogRecordListItem {

    /** The wrapped LogRecord */
	private ILoggingEvent lr;

    private Level logLevel;
    
    private SimpleDateFormat simpleDateFormat;

    /**
     * Creates a new {@link LogRecordListItem} wrapping the given {@link LogRecord} and using the given {@link Formatter}
     * to create a string representation of the <code>LogRecord</code>.
     *
     * @param lr the <code>LogRecord</code> to wrap
     * @param formatter the formatter to use for creating the <code>LogRecord</code>'s string representation 
     */
    LogRecordListItem(ILoggingEvent lr, Level logLevel) {
        this.lr = lr;
        this.logLevel = logLevel;
        
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
    }

    /**
     * Returns the wrapped {@link LogRecord}.
     *
     * @return the wrapped {@link LogRecord}
     */
    public ILoggingEvent getLogRecord() {
        return lr;
    }

    public Level getLevel() {
    	return logLevel;
    }
    
    /**
     * Returns a properly formatted string representation of the wrapped {@link LogRecord}.
     * @return a properly formatted string representation of the wrapped {@link LogRecord}.
     */
    public String toString() {
/*    	StringBuffer sbuf = new StringBuffer(128);
    	sbuf.append("[");
    	sbuf.append(simpleDateFormat.format(new Date(lr.getTimeStamp())));
    	sbuf.append("] ");
        sbuf.append(logLevel);
        sbuf.append(" ");
        sbuf.append(stackTraceElement.getClassName());
        sbuf.append("#");
        sbuf.append(stackTraceElement.getMethodName());
        sbuf.append(",");
        sbuf.append(stackTraceElement.getLineNumber());
        sbuf.append(" ");
        sbuf.append(lr.getFormattedMessage());
        sbuf.append(CoreConstants.LINE_SEPARATOR);
        return sbuf.toString();
*/
    	return lr.getFormattedMessage();
    }
}
