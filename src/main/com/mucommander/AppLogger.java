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

package com.mucommander;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides logging facilities to the muCommander application using the <code>java.util.logging</code> API.
 * Despite the name, this class is not a <code>java.util.logging.Logger</code> but provides familiar log methods
 * that delegate to the underlying <code>Logger</code> instance returned by {@link #getLogger}.
 *
 * @author Maxence Bernard
 */
public class AppLogger {

    /** Logger singleton */
    private final static Logger logger = LoggerFactory.getLogger(AppLogger.class);

    /**
     * Returns the <code>java.util.logging.Logger</code> instance used by this class to log records.
     *
     * @return the Logger instance that this class uses to log
     */
    public static Logger getLogger() {
        return logger;
    }


    /////////////////////////////////////////////////
    // Methods deletegated to the Logger singleton //
    /////////////////////////////////////////////////
    public static void severe(String message) {
        logger.error(message);
    }

    public static void severe(String message, Throwable t) {
        logger.error(message, t);
    }

    public static void warning(String message) {
        logger.warn(message);
    }

    public static void warning(String message, Throwable t) {
        logger.warn(message, t);
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void info(String message, Throwable t) {
        logger.info(message, t);
    }

    public static void config(String message) {
        logger.info(message);
    }

    public static void config(String message, Throwable t) {
        logger.info(message, t);
    }

    public static void fine(String message) {
        logger.debug(message);
    }

    public static void fine(String message, Throwable t) {
        logger.debug(message, t);
    }

    public static void finer(String message) {
        logger.debug(message);
    }

    public static void finer(String message, Throwable t) {
        logger.debug(message, t);
    }

    public static void finest(String message) {
        logger.trace(message);
    }

    public static void finest(String message, Throwable t) {
        logger.trace(message, t);
    }
}
