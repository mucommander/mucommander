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

package com.mucommander.commons;

import com.mucommander.commons.log.StaticLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides logging facilities to the muCommander commons API using the <code>java.util.logging</code> API.
 * Despite the name, this class is not a <code>java.util.logging.Logger</code> but provides familiar log methods
 * that delegate to the underlying <code>Logger</code> instance returned by {@link #getLogger}.
 *
 * @author Maxence Bernard
 */
public class CommonsLogger {

    /** Logger singleton */
    private final static Logger logger = Logger.getLogger(CommonsLogger.class.getName());

    /**
     * Returns the <code>java.util.logging.Logger</code> instance used by this class to log records.
     *
     * @return the Logger instance that this class uses to log
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Logs the given message and/or {@link Throwable} using {@link StaticLogger#log(Logger, Level, String, Throwable, int, int)}.
     *
     * @param level the log level
     * @param message the message to be printed, can be <code>null</code>
     * @param t a Throwable whose stack trace will be printed, can be <code>null</code>
     */
    private static void log(Level level, String message, Throwable t) {
        StaticLogger.log(logger, level, message, t, 2);
    }


    /////////////////////////////////////////////////
    // Methods deletegated to the Logger singleton //
    /////////////////////////////////////////////////

    public boolean isLoggable(Level level) {
        return logger.isLoggable(level);
    }

    public static void severe(String message) {
        log(Level.SEVERE, message, null);
    }

    public static void severe(String message, Throwable t) {
        log(Level.SEVERE, message, t);
    }

    public static void warning(String message) {
        log(Level.WARNING, message, null);
    }

    public static void warning(String message, Throwable t) {
        log(Level.WARNING, message, t);
    }

    public static void info(String message) {
        log(Level.INFO, message, null);
    }

    public static void info(String message, Throwable t) {
        log(Level.INFO, message, t);
    }

    public static void config(String message) {
        log(Level.CONFIG, message, null);
    }

    public static void config(String message, Throwable t) {
        log(Level.CONFIG, message, t);
    }

    public static void fine(String message) {
        log(Level.FINE, message, null);
    }

    public static void fine(String message, Throwable t) {
        log(Level.FINE, message, t);
    }

    public static void finer(String message) {
        log(Level.FINER, message, null);
    }

    public static void finer(String message, Throwable t) {
        log(Level.FINER, message, t);
    }

    public static void finest(String message) {
        log(Level.FINEST, message, null);
    }

    public static void finest(String message, Throwable t) {
        log(Level.FINEST, message, t);
    }
}
