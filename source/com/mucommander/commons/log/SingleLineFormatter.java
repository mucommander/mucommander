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

package com.mucommander.commons.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Formats log record on a single line, using the following format:
 * <pre>
 * [ISO-8601_DATE] LEVEL CLASS_NAME#METHOD_NAME,LINE_NUMBER MESSAGE
 * THROWABLE
 * </pre>
 * Here's a sample line:
 * <pre>
 * [2009-07-18 12:59:57.691] INFO Launcher#main,550 Initializing window
 * </pre>
 *
 * @author Maxence Bernard
 */
public class SingleLineFormatter extends Formatter {

    /** Formats date in the ISO-8601 format */
    private final SimpleDateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /** String displayed when the source class information is not available */
    private final static String UNKNOWN_CLASS = "unknown_class";

    /** String displayed when the source method information is not available */
    private final static String UNKNOWN_METHOD = "unknown_method";

    /** The system's line separator */
    private final static String LINE_SEPARATOR = System.getProperty("line.separator");


    //////////////////////////////
    // Formatter implementation //
    //////////////////////////////

    @Override
    public String format(LogRecord record) {
        StringBuffer sb = new StringBuffer();

        sb.append("[");
        synchronized(iso8601DateFormat) {       // SimpleDateFormat is not thread-safe
            sb.append(iso8601DateFormat.format(new Date(record.getMillis())));
        }

        sb.append("] ");

        sb.append(record.getLevel().getName());
        sb.append(" ");

        String s = record.getSourceClassName();
        if(s==null)
            s = UNKNOWN_CLASS;

        sb.append(s);
        sb.append("#");

        s = record.getSourceMethodName();
        if(s==null)
            s = UNKNOWN_METHOD;
        sb.append(s);

        s = record.getMessage();
        if(s!=null) {
            sb.append(' ');
            sb.append(s);
        }

        sb.append(LINE_SEPARATOR);

        Throwable t = record.getThrown();
        if(t!=null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            }
            catch (Exception ex) {
            }
        }

        return sb.toString();
    }
}
