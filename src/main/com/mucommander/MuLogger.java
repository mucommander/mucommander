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

import java.io.IOException;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreferences;
import com.mucommander.ui.dialog.debug.DebugConsoleHandler;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.OutputStreamAppender;

/**
 * This class provides logging facilities to the muCommander application using the <code>java.util.logging</code> API.
 * Despite the name, this class is not a <code>java.util.logging.Logger</code> but provides familiar log methods
 * that delegate to the underlying <code>Logger</code> instance returned by {@link #getLogger}.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class MuLogger {

	public enum Level {
		OFF(0),
		SEVERE(1),
		WARNING(2),
		INFO(3),
		CONFIG(4),
		FINE(5),
		FINER(6),
		FINEST(7);

		private int value;

		Level(int value) {
			this.value = value;
		}

		public boolean isInScopeOf(Level level) {
			return value <= level.value;
		}
	}

	private static void init() {
		ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

		//    	rootLogger.detachAndStopAllAppenders();

		// we are not interested in auto-configuration
		//        loggerContext.reset();
		//        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		//        encoder.setPattern("aa %caller{1} %message%n");
		//        encoder.start();

		ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<ILoggingEvent>();
		appender.start();

		AppenderBase<ILoggingEvent> debugConsoleDialogAppener = DebugConsoleHandler.getInstance();
		debugConsoleDialogAppener.start();

		rootLogger.addAppender(debugConsoleDialogAppener);
		//        rootLogger.addAppender(appender);
	}

	/**
	 * Sets the level of all muCommander loggers.
	 *
	 * @param level the new log level
	 */
	public static void updateLogLevel(Level level) {
		// TODO: re-implement that with the new logging API.
		ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

		ch.qos.logback.classic.Level logbackLevel = null;
		switch (level) {
		case OFF:
			logbackLevel = ch.qos.logback.classic.Level.OFF;
			break;
		case SEVERE:
			logbackLevel = ch.qos.logback.classic.Level.ERROR;
			break;
		case WARNING:
			logbackLevel = ch.qos.logback.classic.Level.WARN;
			break;
		case INFO:
		case CONFIG:
			logbackLevel = ch.qos.logback.classic.Level.INFO;
			break;
		case FINE:
		case FINER:
			logbackLevel = ch.qos.logback.classic.Level.DEBUG;
			break;
		case FINEST:
			logbackLevel = ch.qos.logback.classic.Level.TRACE;
			break;
		}

		logger.setLevel(logbackLevel);
	}


	/**
	 * Returns the current log level used by all <code>java.util.logging</code> loggers.
	 *
	 * @return the current log level used by all <code>java.util.logging</code> loggers.
	 */
	public static Level getLogLevel() {
		return Level.valueOf(MuConfigurations.getPreferences().getVariable(MuPreferences.LOG_LEVEL, MuPreferences.DEFAULT_LOG_LEVEL));
	}


	/**
	 * Sets the new log level to be used by all <code>java.util.logging</code> loggers, and persists it in the
	 * application preferences.
	 *
	 * @param level the new log level to be used by all <code>java.util.logging</code> loggers.
	 */
	public static void setLogLevel(Level level) {
		MuConfigurations.getPreferences().setVariable(MuPreferences.LOG_LEVEL, level.toString());
		updateLogLevel(level);
	}

	static void configureLogging() throws IOException {
		// We're no longer using LogManager and a logging.properties file to initialize java.util.logging, because of
		// a limitation with Webstart limiting the use of handlers and formatters residing in the system's classpath,
		// i.e. built-in ones.

		//	        // Read the java.util.logging configuration file bundled with the muCommander JAR, replacing the JRE's
		//	        // logging.properties configuration.
		//	        InputStream resIn = ResourceLoader.getRootPackageAsFile(Launcher.class).getChild("com/mucommander/logging.properties").getInputStream();
		//	        LogManager.getLogManager().readConfiguration(resIn);
		//	        resIn.close();

		// TODO: re-enable this.
		/*
	        // Remove default handlers
	        Logger rootLogger = LogManager.getLogManager().getLogger("");
	        Handler handlers[] = rootLogger.getHandlers();
	        for (Handler handler : handlers)
	            rootLogger.removeHandler(handler);

	        // and add ours
	        handlers = new Handler[] { new ConsoleHandler(), new DebugConsoleHandler()};
	        Formatter formatter = new SingleLineFormatter();
	        for (Handler handler : handlers) {
	            handler.setFormatter(formatter);
	            rootLogger.addHandler(handler);
	        }
		 */
		MuLogger.init();
		// Set the log level to the value defined in the configuration
		updateLogLevel(getLogLevel());

		//	        Logger fileLogger = FileLogger.getLogger();
		//	        fileLogger.finest("fileLogger finest");
		//	        fileLogger.finer("fileLogger finer");
		//	        fileLogger.fine("fileLogger fine");
		//	        fileLogger.config("fileLogger config");
		//	        fileLogger.info("fileLogger info");
		//	        fileLogger.warning("fileLogger warning");
		//	        fileLogger.severe("fileLogger severe");
	}


	/*public static class MySampleLayout extends LayoutBase<ILoggingEvent> {

		public String doLayout(ILoggingEvent event) {
			StringBuffer sbuf = new StringBuffer(128);
			sbuf.append(event.getTimeStamp());
			sbuf.append(" ");
			sbuf.append(event.getLevel());
			sbuf.append(" [");
			sbuf.append(event.getCallerData()[1].getClassName());
			sbuf.append("] ");
			sbuf.append(event.getCallerData()[1].getMethodName());
			sbuf.append(" - ");
			sbuf.append(event.getCallerData()[1].getLineNumber());
			sbuf.append(CoreConstants.LINE_SEPARATOR);
			return sbuf.toString();
		}
	}*/
}
