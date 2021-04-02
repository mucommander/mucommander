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

package com.mucommander.utils;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.ui.dialog.debug.DebugConsoleAppender;

import ch.qos.logback.classic.Level;

/**
 * This class manages logging issues within mucommander
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class MuLogging {

    /** Levels of log printings */
    public enum LogLevel {
        OFF,
        SEVERE,
        WARNING,
        INFO,
        CONFIG,
        FINE,
        FINER,
        FINEST;

        /**
         * This method maps logback levels to mucommander log levels
         *
         * @param logbackLevel logback log level
         * @return <code>LogLevel</code> corresponding to the given logback log level
         */
        public static LogLevel valueOf(Level logbackLevel) {
            switch(logbackLevel.toInt()) {
            case ch.qos.logback.classic.Level.OFF_INT:
                return LogLevel.OFF;
            case ch.qos.logback.classic.Level.ERROR_INT:
                return LogLevel.SEVERE;
            case ch.qos.logback.classic.Level.WARN_INT:
                return LogLevel.WARNING;
            case ch.qos.logback.classic.Level.INFO_INT:
                return LogLevel.INFO;
            case ch.qos.logback.classic.Level.DEBUG_INT:
                return LogLevel.FINE;
            case ch.qos.logback.classic.Level.TRACE_INT:
                return LogLevel.FINEST;
            default:
                return LogLevel.OFF;
            }
        }

        /**
         * This method maps mucommander log levels to logback levels
         *
         * @return logback level corresponding to this <code>LogLevel</code>
         */
        public Level toLogbackLevel() {
            switch (this) {
            case SEVERE:
                return ch.qos.logback.classic.Level.ERROR;
            case WARNING:
                return ch.qos.logback.classic.Level.WARN;
            case INFO:
            case CONFIG:
                return ch.qos.logback.classic.Level.INFO;
            case FINE:
            case FINER:
                return ch.qos.logback.classic.Level.DEBUG;
            case FINEST:
                return ch.qos.logback.classic.Level.TRACE;
            case OFF:
            default:
                return ch.qos.logback.classic.Level.OFF;
            }
        }
    }

    /** Appender that writes log printings to the debug console dialog */
    private static DebugConsoleAppender debugConsoleAppender;

    /**
     * Sets the level of all muCommander loggers.
     *
     * @param level the new log level
     */
    private static void updateLogLevel(LogLevel level) {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(level.toLogbackLevel());
    }

    /**
     * Returns the current log level used by all <code>org.slf4j</code> loggers.
     *
     * @return the current log level used by all <code>org.slf4j</code> loggers.
     */
    public static LogLevel getLogLevel() {
        return LogLevel.valueOf(MuConfigurations.getPreferences().getVariable(MuPreference.LOG_LEVEL, MuPreferences.DEFAULT_LOG_LEVEL));
    }

    /**
     * Sets the new log level to be used by all <code>org.slf4j</code> loggers, and persists it in the
     * application preferences.
     *
     * @param level the new log level to be used by all <code>org.slf4j</code> loggers.
     */
    public static void setLogLevel(LogLevel level) {
        MuConfigurations.getPreferences().setVariable(MuPreference.LOG_LEVEL, level.toString());
        updateLogLevel(level);
    }

    public static DebugConsoleAppender getDebugConsoleAppender() {
        return debugConsoleAppender;
    }

    public static void configureLogging() throws IOException {
        // We're no longer using LogManager and a logging.properties file to initialize java.util.logging, because of
        // a limitation with Webstart limiting the use of handlers and formatters residing in the system's classpath,
        // i.e. built-in ones.

        // Get root logger
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        // and debug-console-adapter
        debugConsoleAppender = createDebugConsoleAppender();
        rootLogger.addAppender(debugConsoleAppender);

        // Set the log level to the value defined in the configuration
        updateLogLevel(getLogLevel());
    }

    private static DebugConsoleAppender createDebugConsoleAppender() {
        DebugConsoleAppender debugConsoleAppender = new DebugConsoleAppender();
        debugConsoleAppender.start();
        return debugConsoleAppender;
    }
}
