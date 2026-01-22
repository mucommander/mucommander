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

package com.mucommander.main;

import com.beust.jcommander.JCommander;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.mucommander.preload.PreloadedJFrame;
import org.slf4j.LoggerFactory;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * muCommander launcher.
 * <p>
 * This class is used to start muCommander. It will analyse command line arguments and initialize the application.
 * </p>
 *
 * @author Arik Hadas
 */
public class muCommander {
    private final static long START_EPOCH = System.currentTimeMillis();

    /**
     * The property name used to specify an URL to the system property file.
     **/
    public static final String SYSTEM_PROPERTIES_PROP = "mucommander.system.properties";
    /**
     * The default name used for the system properties file.
     **/
    public static final String SYSTEM_PROPERTIES_FILE_VALUE = "system.properties";
    /**
     * The property name used to specify an URL to the configuration property file.
     **/
    public static final String CONFIG_PROPERTIES_PROP = "mucommander.config.properties";
    /**
     * The default name used for the configuration properties file.
     **/
    public static final String CONFIG_PROPERTIES_FILE_VALUE = "config.properties";
    /**
     * Name of the configuration directory.
     */
    public static final String CONFIG_DIRECTORY = "conf";

    /**
     * <p>
     * This method performs the main task of starting the muCommander application. The
     * following functions are performed when invoked:
     * </p>
     * <ol>
     *   <li><i><b>Parse command-line arguments.</b></i> Process command line options
     *       for version, help, preferences location, and other configuration options.
     *   </li>
     *   <li><i><b>Read the system properties file.</b></i> This is a file
     *       containing properties to be pushed into <tt>System.setProperty()</tt>
     *       before starting the application.
     *   </li>
     *   <li><i><b>Read the application configuration property file.</b></i> This is
     *       a file containing properties used to configure the application.
     *       The configuration property file is called <tt>config.properties</tt> by default
     *       and is located in the <tt>conf/</tt> directory.
     *   </li>
     *   <li><i><b>Setup preferences directory.</b></i> Determine and validate the
     *       preferences folder location (default, portable mode, or user-specified).
     *   </li>
     *   <li><i><b>Initialize muCommander core.</b></i> Trigger static initialization
     *       of the core module which loads plugins via ServiceLoader.
     *   </li>
     *   <li><i><b>Start the application.</b></i> The application starts and runs
     *       until the user exits.
     *   </li>
     * </ol>
     *
     * @param args
     *         Command line arguments for configuration and preferences.
     * @throws Exception
     *         If an error occurs.
     **/
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        JCommander jCommander = new JCommander(configuration);
        jCommander.parse(args);

        if (configuration.help) {
            jCommander.setProgramName(muCommander.class.getSimpleName());
            jCommander.usage();
            return;
        }

        if (configuration.version) {
            var version = "?";
            var classUrl = muCommander.class.getProtectionDomain().getCodeSource().getLocation();
            var jarFile = new File(classUrl.toURI());
            Attributes attributes = null;
            try (var jar = new JarFile(jarFile)) {
                Manifest manifest = jar.getManifest();
                attributes = manifest.getMainAttributes();
            }
            if (attributes != null)
                version = attributes.getValue("Specification-Version");
            jCommander.getConsole().println(version);
            return;
        }

        logTimeSinceStart("Main started");
        // Ensure that a graphics environment is available
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Error: no graphical environment detected.");
            return;
        }

        new Thread(() -> {
            PreloadedJFrame.init();
            /** Pre-load into JVM available fonts (as it is very slow to initialize):
             * https://www.mail-archive.com/java2d-interest@capra.eng.sun.com/msg02877.html,
             * https://stackoverflow.com/questions/3237941/swing-load-available-font-family-slow-down-the-performance
             */
            System.out.println("Preloading fonts into JVM...");
            var pre = System.currentTimeMillis();
            try {
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Preloading fonts completed in: " + (System.currentTimeMillis() - pre) + "ms");
        }, "Preload-Fonts").start();

        Path codeLocation = Paths.get(muCommander.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (codeLocation == null) {
            System.err.println("Failed to retrieve code location");
            return;
        }
        File codeParentFolder = codeLocation.getParent().toFile();

        // Load system properties.
        muCommander.loadSystemProperties();
        logTimeSinceStart("System properties loaded");

        // Read configuration properties.
        Map<String, String> configProps = muCommander.loadConfigProperties();
        // If no configuration properties were found, then create
        // an empty properties object.
        if (configProps == null) {
            System.err.println("No " + CONFIG_PROPERTIES_FILE_VALUE + " found.");
            configProps = new HashMap<>();
        }

        configProps.computeIfAbsent("mucommander.conf.dir",
                key -> new File(codeParentFolder, "conf").getAbsolutePath());

        logTimeSinceStart("Config properties loaded");

        File preferencesFolder;
        if (configuration.preferences != null) {
            try {
                preferencesFolder = UserPreferencesDir.getPreferencesFolder(configuration.preferences);
            } catch (RuntimeException e) {
                System.err.println("Failed to retrieve specified preferences folder: " + configuration.preferences);
                return;
            }
        } else if (new File(codeParentFolder, ".portable").exists()) {
            String portableDir = new File(codeParentFolder, ".mucommander").getAbsolutePath();
            configProps.put("app_mode", "portable");
            try {
                preferencesFolder = UserPreferencesDir.getPreferencesFolder(portableDir);
            } catch (RuntimeException e) {
                System.err.println("Failed to retrieve portable preferences folder: " + portableDir);
                return;
            }
        } else {
            try {
                preferencesFolder = UserPreferencesDir.getDefaultPreferencesFolder();
            } catch (RuntimeException e) {
                System.err.println("Failed to retrieve default preferences folder: " + e.getMessage());
                return;
            }
        }
        // override the specified preferences folder
        configuration.preferences = preferencesFolder.getAbsolutePath();
        // set a system property that determines where logs would be written (see logback.xml)
        // IMPORTANT: This must be set BEFORE configuring logback
        System.setProperty("MUCOMMANDER_USER_PREFERENCES", configuration.preferences);

        // Ensure logs directory exists
        File logsDir = new File(preferencesFolder, "logs");
        if (!logsDir.exists() && !logsDir.mkdirs()) {
            System.err.println("Warning: Failed to create logs directory: " + logsDir.getAbsolutePath());
        }

        String confDir = configProps.get("mucommander.conf.dir");
        System.setProperty("logback.configurationFile", new File(confDir, "logback.xml").getAbsolutePath());

        // Reconfigure logback now that system properties are set
        reconfigureLogback(new File(confDir, "logback.xml"));

        // Set system properties from configuration for muCommander core
        for (Map.Entry<String, String> entry : configProps.entrySet()) {
            if (entry.getKey().startsWith("mucommander.") && entry.getValue() != null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        }

        // Copy configuration provided by command line arguments
        configProps.putAll(new AbstractMap<String, String>() {
            @Override
            public java.util.Set<Map.Entry<String, String>> entrySet() {
                return configuration.entrySet();
            }
        });

        // Set command line configuration as system properties
        for (Map.Entry<String, String> entry : configuration.entrySet()) {
            if (entry.getValue() != null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        }

        logTimeSinceStart("Configuration applied");
        com.mucommander.conf.Activator.initialize(configuration.preferences);

        try {
            // Trigger static initialization of muCommander core
            // This loads all services via ServiceLoader
            com.mucommander.Activator.getInstance().start();
            logTimeSinceStart("muCommander core initialized");

            // The application is now running and will continue until the user exits
            // The Activator.start() method already called Application.run()

        } catch (Exception ex) {
            System.err.println("Could not start muCommander: " + ex);
            ex.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * <p>
     * Loads the properties in the system property file into <tt>System.setProperty()</tt>.
     * By default, the system property file is located in the <tt>conf/</tt> directory
     * and is called "<tt>system.properties</tt>". The precise file from which to load system
     * properties can be set by initializing the system property to an arbitrary URL.
     * </p>
     **/
    public static void loadSystemProperties() {
        // The system properties file is either specified by a system
        // property or it is in the conf directory.
        // Try to load it from one of these places.

        // See if the property URL was specified as a property.
        URL propURL = null;
        String custom = System.getProperty(SYSTEM_PROPERTIES_PROP);
        if (custom != null) {
            try {
                propURL = new URL(custom);
            } catch (MalformedURLException ex) {
                System.err.print("muCommander: " + ex);
                return;
            }
        } else {
            // Determine where the configuration directory is by looking at the classpath
            File confDir = null;
            String classpath = System.getProperty("java.class.path");
            int index = classpath.toLowerCase().indexOf("mucommander");
            int start = classpath.lastIndexOf(File.pathSeparator, index) + 1;
            if (index >= start) {
                // Get the path of the mucommander jar file.
                String jarLocation = classpath.substring(start, index);
                // Calculate the conf directory based on the parent
                // directory of the jar directory.
                confDir = new File(
                        new File(new File(jarLocation).getAbsolutePath()).getParent(),
                        CONFIG_DIRECTORY);
            } else {
                // Can't figure it out so use the current directory as default.
                confDir = new File(System.getProperty("user.dir"), CONFIG_DIRECTORY);
            }

            try {
                propURL = new File(confDir, SYSTEM_PROPERTIES_FILE_VALUE).toURL();
            } catch (MalformedURLException ex) {
                System.err.print("muCommander: " + ex);
                return;
            }
        }

        // Read the properties file.
        Properties props = new Properties();
        InputStream is = null;
        try {
            is = propURL.openConnection().getInputStream();
            props.load(is);
            is.close();
        } catch (FileNotFoundException ex) {
            // Ignore file not found.
        } catch (Exception ex) {
            System.err.println(
                    "muCommander: Error loading system properties from " + propURL);
            System.err.println("muCommander: " + ex);
            try {
                if (is != null)
                    is.close();
            } catch (IOException ex2) {
                // Nothing we can do.
            }
            return;
        }

        // Set system properties (with simple variable substitution)
        for (Enumeration<String> e = (Enumeration<String>) props.propertyNames(); e.hasMoreElements(); ) {
            String name = e.nextElement();
            String value = props.getProperty(name);
            // Simple variable substitution for ${property.name} patterns
            value = substituteVars(value);
            System.setProperty(name, value);
        }
    }

    /**
     * <p>
     * Loads the configuration properties in the configuration property file;
     * these properties are intended for application configuration purposes. By
     * default, the configuration property file is located in the <tt>conf/</tt> directory
     * and is called "<tt>config.properties</tt>". The precise file from
     * which to load configuration properties can be set by initializing the system
     * property to an arbitrary URL.
     * </p>
     *
     * @return A <tt>Properties</tt> instance or <tt>null</tt> if there was an error.
     **/
    public static Map<String, String> loadConfigProperties() {
        // The config properties file is either specified by a system
        // property or it is in the conf/ directory.
        // Try to load it from one of these places.

        // See if the property URL was specified as a property.
        URL propURL = null;
        String custom = System.getProperty(CONFIG_PROPERTIES_PROP);
        if (custom != null) {
            try {
                propURL = new URL(custom);
            } catch (MalformedURLException ex) {
                System.err.print("muCommander: " + ex);
                return null;
            }
        } else {
            // Determine where the configuration directory is by looking at the classpath
            File confDir = null;
            String classpath = System.getProperty("java.class.path");
            int index = classpath.toLowerCase().indexOf("mucommander");
            int start = classpath.lastIndexOf(File.pathSeparator, index) + 1;
            if (index >= start) {
                // Get the path of the mucommander jar file.
                String jarLocation = classpath.substring(start, index);
                // Calculate the conf directory based on the parent
                // directory of the jar directory.
                confDir = new File(
                        new File(new File(jarLocation).getAbsolutePath()).getParent(),
                        CONFIG_DIRECTORY);
            } else {
                // Can't figure it out so use the current directory as default.
                confDir = new File(System.getProperty("user.dir"), CONFIG_DIRECTORY);
            }

            try {
                propURL = new File(confDir, CONFIG_PROPERTIES_FILE_VALUE).toURL();
            } catch (MalformedURLException ex) {
                System.err.print("muCommander: " + ex);
                return null;
            }
        }

        // Read the properties file.
        Properties props = new Properties();
        InputStream is = null;
        try {
            // Try to load config.properties.
            is = propURL.openConnection().getInputStream();
            props.load(is);
            is.close();
        } catch (Exception ex) {
            // Try to close input stream if we have one.
            try {
                if (is != null)
                    is.close();
            } catch (IOException ex2) {
                // Nothing we can do.
            }

            return null;
        }

        // Perform variable substitution for system properties and
        // convert to map.
        Map<String, String> map = new HashMap<String, String>();
        for (Enumeration<String> e = (Enumeration<String>) props.propertyNames(); e.hasMoreElements(); ) {
            String name = e.nextElement();
            String value = props.getProperty(name);
            // Simple variable substitution for ${property.name} patterns
            value = substituteVars(value);
            map.put(name, value);
        }

        return map;
    }

    /**
     * Simple variable substitution method that replaces ${property.name} patterns
     * with the corresponding system property value.
     *
     * @param value The string to perform substitution on
     * @return The string with variables substituted
     */
    private static String substituteVars(String value) {
        if (value == null || !value.contains("${")) {
            return value;
        }

        StringBuilder result = new StringBuilder();
        int startIdx = 0;
        int endIdx;

        while ((startIdx = value.indexOf("${", startIdx)) >= 0) {
            endIdx = value.indexOf("}", startIdx + 2);
            if (endIdx < 0) {
                break;
            }

            result.append(value, 0, startIdx);
            String propName = value.substring(startIdx + 2, endIdx);
            String propValue = System.getProperty(propName);

            if (propValue != null) {
                result.append(propValue);
            } else {
                // Keep the placeholder if property not found
                result.append("${").append(propName).append("}");
            }

            value = value.substring(endIdx + 1);
            startIdx = 0;
        }

        result.append(value);
        return result.toString();
    }

    private static void logTimeSinceStart(String text) {
        System.out.println("[muEpoch+" + (System.currentTimeMillis() - START_EPOCH) + "ms] muCommander: " + text);
    }

    /**
     * Reconfigures logback with the specified configuration file.
     * This is necessary because logback may have already been initialized
     * before we set the system properties it depends on.
     *
     * @param configFile The logback configuration file
     */
    private static void reconfigureLogback(File configFile) {
        if (!configFile.exists()) {
            System.err.println("Warning: logback configuration file not found: " + configFile.getAbsolutePath());
            return;
        }

        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.reset();
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(loggerContext);
            configurator.doConfigure(configFile);
            System.out.println("Logback reconfigured successfully with: " + configFile.getAbsolutePath());
        } catch (JoranException e) {
            System.err.println("Failed to reconfigure logback: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
