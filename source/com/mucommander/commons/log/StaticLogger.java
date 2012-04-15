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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides static <code>log</code> methods, that log a message using a given
 * {@link java.util.logging.Logger}. Those methods provide the following benefits, over the standard <code>Logger</code>
 * log methods:
 * <ul>
 *   <li>the line number is automatically appended to the caller method's name</li>
 *   <li>the message can be truncated if it exceeds a specified size</li>
 * </ul>
 *
 * @author Maxence Bernard
 */
public abstract class StaticLogger {

    /**
     * Equivalent to calling {@link #log(Logger, Level, String, Throwable, int, int)} with no limitation on the message
     * size (<code>-1</code>).
     *
     * @param logger the <code>Logger</code> instance to use for logging the message
     * @param level the log level
     * @param message the message to be printed, can be <code>null</code>
     * @param t a Throwable whose stack trace will be printed, can be <code>null</code>
     * @param callDepth number of method calls that separate this method with the originating log statement. This
     * parameter is used to add the class name, method name and line number of the caller to the log record.
     * If this method is called directly by the method that wishes to log the message, this parameter should be
     * <code>0</code>. From there, add 1 for each method call.
     */
    public static void log(Logger logger, Level level, String message, Throwable t, int callDepth) {
        log(logger, level, message, t, callDepth+1, -1);
    }

    /**
     * Logs the given information using the specified Logger.
     *
     * <p>This method generates the class and method names in a custom format, appending the source line
     * number to the method name.</p>
     *
     * <p>The log message is truncated if it exceeds the given number </p>
     *
     * @param logger the <code>Logger</code> instance to use for logging the message
     * @param level the log level
     * @param message the message to be printed, can be <code>null</code>
     * @param t a Throwable whose stack trace will be printed, can be <code>null</code>
     * @param maxMessageSize maximum number of characters the specified log message may contain, <code>-1</code> for
     * no limit. If the message is longer than the limit, it will be truncated before being sent to the underlying
     * <code>Logger</code>.
     * @param callDepth number of method calls that separate this method with the originating log statement. This
     * parameter is used to add the class name, method name and line number of the caller to the log record.
     * If this method is called directly by the method that wishes to log the message, this parameter should be
     * <code>0</code>. From there, add 1 for each method call.
     */
    public static void log(Logger logger, Level level, String message, Throwable t, int callDepth, int maxMessageSize) {
        // Return now if the message is not loggable
        if(!logger.isLoggable(level))
            return;

        // Generate a stack trace:
        // - #0 is java.lang.Thread
        // - #1 is this class/method
        // - #2 is the caller of this method
        // - ...
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2+callDepth];

        // Keep the raw class name without the package name
        String className = caller.getClassName();
        int pos = className.lastIndexOf('.');
        if(pos!=-1)
            className = className.substring(pos+1, className.length());

        // If the log message exceeds the character limit, truncate it
        if(maxMessageSize>=0 && message!=null && message.length()>maxMessageSize)
            message = message.substring(0, maxMessageSize)+" [...]";

        logger.logp(
                level,
                className,                                              // raw class name without the package name
                caller.getMethodName()+","+caller.getLineNumber(),      // append the source line number
                message,                                                // may be null
                t                                                       // may be null
        );
    }
}
