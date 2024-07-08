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

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Manifest;

import org.apache.felix.framework.util.Util;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import com.beust.jcommander.JCommander;

/**
 * muCommander launcher.
 * <p>
 * This class is used to start muCommander. It will analyse command line
 * arguments and initialize the OSGi framework.
 * </p>
 * @author Arik Hadas
 */
public class muCommander 
{
    private final static long START_EPOCH = System.currentTimeMillis();

    /**
     * Switch for specifying bundle directory.
    **/
    public static final String BUNDLE_DIR_SWITCH = "-b";

    /**
     * The property name used to specify whether the launcher should
     * install a shutdown hook.
    **/
    public static final String SHUTDOWN_HOOK_PROP = "felix.shutdown.hook";
    /**
     * The property name used to specify an URL to the system
     * property file.
    **/
    public static final String SYSTEM_PROPERTIES_PROP = "felix.system.properties";
    /**
     * The default name used for the system properties file.
    **/
    public static final String SYSTEM_PROPERTIES_FILE_VALUE = "system.properties";
    /**
     * The property name used to specify an URL to the configuration
     * property file to be used for the created the framework instance.
    **/
    public static final String CONFIG_PROPERTIES_PROP = "felix.config.properties";
    /**
     * The default name used for the configuration properties file.
    **/
    public static final String CONFIG_PROPERTIES_FILE_VALUE = "config.properties";
    /**
     * Name of the configuration directory.
     */
    public static final String CONFIG_DIRECTORY = "conf";

    private static Framework m_fwk = null;

    /**
     * <p>
     * This method performs the main task of constructing an framework instance
     * and starting its execution. The following functions are performed
     * when invoked:
     * </p>
     * <ol>
     *   <li><i><b>Examine and verify command-line arguments.</b></i> The launcher
     *       accepts a "<tt>-b</tt>" command line switch to set the bundle auto-deploy
     *       directory and a single argument to set the bundle cache directory.
     *   </li>
     *   <li><i><b>Read the system properties file.</b></i> This is a file
     *       containing properties to be pushed into <tt>System.setProperty()</tt>
     *       before starting the framework. This mechanism is mainly shorthand
     *       for people starting the framework from the command line to avoid having
     *       to specify a bunch of <tt>-D</tt> system property definitions.
     *       The only properties defined in this file that will impact the framework's
     *       behavior are the those concerning setting HTTP proxies, such as
     *       <tt>http.proxyHost</tt>, <tt>http.proxyPort</tt>, and
     *       <tt>http.proxyAuth</tt>. Generally speaking, the framework does
     *       not use system properties at all.
     *   </li>
     *   <li><i><b>Read the framework's configuration property file.</b></i> This is
     *       a file containing properties used to configure the framework
     *       instance and to pass configuration information into
     *       bundles installed into the framework instance. The configuration
     *       property file is called <tt>config.properties</tt> by default
     *       and is located in the <tt>conf/</tt> directory of the Felix
     *       installation directory, which is the parent directory of the
     *       directory containing the <tt>felix.jar</tt> file. It is possible
     *       to use a different location for the property file by specifying
     *       the desired URL using the <tt>felix.config.properties</tt>
     *       system property; this should be set using the <tt>-D</tt> syntax
     *       when executing the JVM. If the <tt>config.properties</tt> file
     *       cannot be found, then default values are used for all configuration
     *       properties. Refer to the
     *       <a href="Felix.html#Felix(java.util.Map)"><tt>Felix</tt></a>
     *       constructor documentation for more information on framework
     *       configuration properties.
     *   </li>
     *   <li><i><b>Copy configuration properties specified as system properties
     *       into the set of configuration properties.</b></i> Even though the
     *       Felix framework does not consult system properties for configuration
     *       information, sometimes it is convenient to specify them on the command
     *       line when launching Felix. To make this possible, the Felix launcher
     *       copies any configuration properties specified as system properties
     *       into the set of configuration properties passed into Felix.
     *   </li>
     *   <li><i><b>Add shutdown hook.</b></i> To make sure the framework shutdowns
     *       cleanly, the launcher installs a shutdown hook; this can be disabled
     *       with the <tt>felix.shutdown.hook</tt> configuration property.
     *   </li>
     *   <li><i><b>Create and initialize a framework instance.</b></i> The OSGi standard
     *       <tt>FrameworkFactory</tt> is retrieved from <tt>META-INF/services</tt>
     *       and used to create a framework instance with the configuration properties.
     *   </li>
     *   <li><i><b>Auto-deploy bundles.</b></i> All bundles in the auto-deploy
     *       directory are deployed into the framework instance.
     *   </li>
     *   <li><i><b>Start the framework.</b></i> The framework is started and
     *       the launcher thread waits for the framework to shutdown.
     *   </li>
     * </ol>
     * <p>
     * It should be noted that simply starting an instance of the framework is not
     * enough to create an interactive session with it. It is necessary to install
     * and start bundles that provide a some means to interact with the framework;
     * this is generally done by bundles in the auto-deploy directory or specifying
     * an "auto-start" property in the configuration property file. If no bundles
     * providing a means to interact with the framework are installed or if the
     * configuration property file cannot be found, the framework will appear to
     * be hung or deadlocked. This is not the case, it is executing correctly,
     * there is just no way to interact with it.
     * </p>
     * <p>
     * The launcher provides two ways to deploy bundles into a framework at
     * startup, which have associated configuration properties:
     * </p>
     * <ul>
     *   <li>Bundle auto-deploy - Automatically deploys all bundles from a
     *       specified directory, controlled by the following configuration
     *       properties:
     *     <ul>
     *       <li><tt>felix.auto.deploy.dir</tt> - Specifies the auto-deploy directory
     *           from which bundles are automatically deploy at framework startup.
     *           The default is the <tt>bundle/</tt> directory of the current directory.
     *       </li>
     *       <li><tt>felix.auto.deploy.action</tt> - Specifies the auto-deploy actions
     *           to be found on bundle JAR files found in the auto-deploy directory.
     *           The possible actions are <tt>install</tt>, <tt>update</tt>,
     *           <tt>start</tt>, and <tt>uninstall</tt>. If no actions are specified,
     *           then the auto-deploy directory is not processed. There is no default
     *           value for this property.
     *       </li>
     *     </ul>
     *   </li>
     *   <li>Bundle auto-properties - Configuration properties which specify URLs
     *       to bundles to install/start:
     *     <ul>
     *       <li><tt>felix.auto.install.N</tt> - Space-delimited list of bundle
     *           URLs to automatically install when the framework is started,
     *           where <tt>N</tt> is the start level into which the bundle will be
     *           installed (e.g., felix.auto.install.2).
     *       </li>
     *       <li><tt>felix.auto.start.N</tt> - Space-delimited list of bundle URLs
     *           to automatically install and start when the framework is started,
     *           where <tt>N</tt> is the start level into which the bundle will be
     *           installed (e.g., felix.auto.start.2).
     *       </li>
     *     </ul>
     *   </li>
     * </ul>
     * <p>
     * These properties should be specified in the <tt>config.properties</tt>
     * so that they can be processed by the launcher during the framework
     * startup process.
     * </p>
     * @param args Accepts arguments to set the auto-deploy directory and/or
     *        the bundle cache directory.
     * @throws Exception If an error occurs.
    **/
    public static void main(String[] args) throws Exception
    {
        logTimeSinceStart("Main started");
        Configuration configuration = new Configuration();
        JCommander jCommander = new JCommander(configuration);
        jCommander.parse(args);

        if (configuration.help) {
            jCommander.setProgramName(muCommander.class.getSimpleName());
            jCommander.usage();
            return;
        }

        if (configuration.version) {
//            printVersion();
//            return;
        }

        // Ensure that a graphics environment is available
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Error: no graphical environment detected.");
            return;
        }

        new Thread(() -> {
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
        if (configProps == null)
        {
            System.err.println("No " + CONFIG_PROPERTIES_FILE_VALUE + " found.");
            configProps = new HashMap<>();
        }

        configProps.putIfAbsent("org.osgi.framework.storage.clean", "onFirstInit");
        configProps.putIfAbsent("org.osgi.framework.startlevel.beginning", "2");
        configProps.putIfAbsent(AutoProcessor.AUTO_DEPLOY_ACTION_PROPERTY, "install, start");
        configProps.putIfAbsent("felix.log.level", "1");

        configProps.computeIfAbsent("org.osgi.framework.system.packages.extra",
                key -> "sun.net.www," +
                        "sun.misc," +
                        "sun.plugin.protocol," + //optional
                        "com.sun.java.browser.net," + //optional
                        "javax.activation," +
                        "javax.jms," +
                        "javax.servlet," +
                        "javax.servlet.http," +
                        "javax.script," +
                        "sun.security.action," +
                        "com.apple.eawt," +
                        "com.apple.eio," +
                        "com.sun.image.codec.jpeg," +
                        "sun.awt.image," +
                        "sun.awt.image.codec," +
                        "com.apple.laf," +
                        "com.formdev.flatlaf," +
                        "org.violetlib.aqua"
        );

        configProps.computeIfAbsent(AutoProcessor.AUTO_DEPLOY_DIR_PROPERTY, key -> new File(codeParentFolder, "bundle").getAbsolutePath());

        configProps.computeIfAbsent("mucommander.conf.dir", key -> new File(codeParentFolder, "conf").getAbsolutePath());

        String confDir = configProps.get("mucommander.conf.dir");
        System.setProperty("logback.configurationFile", new File(confDir, "logback.xml").getAbsolutePath());

        configProps.computeIfAbsent("mucommander.app.dir", key -> new File(codeParentFolder, "app").getAbsolutePath());

        final String appDir = configProps.get("mucommander.app.dir");

        var manifest = new Manifest(muCommander.class.getClassLoader().getResource("META-INF/MANIFEST.MF").openStream());
        var attributes = manifest.getMainAttributes();
        var coreBundleName = String.format("mucommander-core-%s.jar", attributes.getValue("Specification-Version"));
        configProps.computeIfAbsent("felix.auto.start.2", key -> "file:" + new File(appDir, coreBundleName).getAbsolutePath());

        Path cacheDir = Paths.get(System.getProperty("java.io.tmpdir"), "mucommander-felix-cache-"+System.getProperty("user.name"));
        configProps.put(Constants.FRAMEWORK_STORAGE, cacheDir.toFile().getAbsolutePath());

        // Copy framework properties from the system properties.
        muCommander.copySystemProperties(configProps);
        logTimeSinceStart("Config properties loaded");

        File preferencesFolder;
        if (configuration.preferences != null) {
            try {
                preferencesFolder = UserPreferencesDir.getPreferencesFolder(configuration.preferences);
            } catch(RuntimeException e) {
                System.err.println("Failed to retrieve specified preferences folder: " + configuration.preferences);
                return;
            }
        } else if (new File(codeParentFolder, ".portable").exists()) {
            String portableDir = new File(codeParentFolder, ".mucommander").getAbsolutePath();
            configProps.put("app_mode", "portable");
            try {
                preferencesFolder = UserPreferencesDir.getPreferencesFolder(portableDir);
            } catch(RuntimeException e) {
                System.err.println("Failed to retrieve portable preferences folder: " + portableDir);
                return;
            }
        } else {
            try {
                preferencesFolder = UserPreferencesDir.getDefaultPreferencesFolder();
            } catch(RuntimeException e) {
                System.err.println("Failed to retrieve default preferences folder: " + e.getMessage());
                return;
            }
        }
        // override the specified preferences folder
        configuration.preferences = preferencesFolder.getAbsolutePath();
        // set a system property that determines where logs would be written (see logback.xml)
        System.setProperty("MUCOMMANDER_USER_PREFERENCES", configuration.preferences);

        // Copy configuration provided by command line arguments
        configProps.putAll(new AbstractMap<String,String>() {
            @Override
            public java.util.Set<Map.Entry<String,String>> entrySet() {
                return configuration.entrySet();
            }
        });

        // If enabled, register a shutdown hook to make sure the framework is
        // cleanly shutdown when the VM exits.
        String enableHook = configProps.get(SHUTDOWN_HOOK_PROP);
        if ((enableHook == null) || !enableHook.equalsIgnoreCase("false"))
        {
            Runtime.getRuntime().addShutdownHook(new Thread("Felix Shutdown Hook") {
                public void run()
                {
                    try
                    {
                        if (m_fwk != null)
                        {
                            m_fwk.stop();
                            m_fwk.waitForStop(0);
                        }
                    }
                    catch (Exception ex)
                    {
                        System.err.println("Error stopping framework: " + ex);
                    }
                }
            });
        }
        logTimeSinceStart("Hooks configured");
        try
        {
            // Create an instance of the framework.
            FrameworkFactory factory = getFrameworkFactory();
            m_fwk = factory.newFramework(configProps);
            logTimeSinceStart("OSGi framework created");

            // Initialize the framework, but don't start it yet.
            m_fwk.init();
            logTimeSinceStart("OSGi framework init'd");

            // Use the system bundle context to process the auto-deploy
            // and auto-install/auto-start properties.
            AutoProcessor.process(configProps, m_fwk.getBundleContext());
            logTimeSinceStart("Bundles deployed and started");

            FrameworkEvent event;
            do
            {
                // Start the framework.
                m_fwk.start();
                logTimeSinceStart("OSGi framework with muC fully started");

                // Wait for framework to stop to exit the VM.
                event = m_fwk.waitForStop(0);
            }
            // If the framework was updated, then restart it.
            while (event.getType() == FrameworkEvent.STOPPED_UPDATE);
            // Otherwise, exit.
            System.exit(0);
        }
        catch (Exception ex)
        {
            System.err.println("Could not create framework: " + ex);
            ex.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Simple method to parse META-INF/services file for framework factory.
     * Currently, it assumes the first non-commented line is the class name
     * of the framework factory implementation.
     * @return The created <tt>FrameworkFactory</tt> instance.
     * @throws Exception if any errors occur.
    **/
    private static FrameworkFactory getFrameworkFactory() throws Exception
    {
        URL url = muCommander.class.getClassLoader().getResource(
            "META-INF/services/org.osgi.framework.launch.FrameworkFactory");
        if (url != null)
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            try
            {
                for (String s = br.readLine(); s != null; s = br.readLine())
                {
                    s = s.trim();
                    // Try to load first non-empty, non-commented line.
                    if ((s.length() > 0) && (s.charAt(0) != '#'))
                    {
                        return (FrameworkFactory) Class.forName(s).getDeclaredConstructor().newInstance();
                    }
                }
            }
            finally
            {
                if (br != null) br.close();
            }
        }

        throw new Exception("Could not find framework factory.");
    }

    /**
     * <p>
     * Loads the properties in the system property file associated with the
     * framework installation into <tt>System.setProperty()</tt>. These properties
     * are not directly used by the framework in anyway. By default, the system
     * property file is located in the <tt>conf/</tt> directory of the Felix
     * installation directory and is called "<tt>system.properties</tt>". The
     * installation directory of Felix is assumed to be the parent directory of
     * the <tt>felix.jar</tt> file as found on the system class path property.
     * The precise file from which to load system properties can be set by
     * initializing the "<tt>felix.system.properties</tt>" system property to an
     * arbitrary URL.
     * </p>
    **/
    public static void loadSystemProperties()
    {
        // The system properties file is either specified by a system
        // property or it is in the same directory as the Felix JAR file.
        // Try to load it from one of these places.

        // See if the property URL was specified as a property.
        URL propURL = null;
        String custom = System.getProperty(SYSTEM_PROPERTIES_PROP);
        if (custom != null)
        {
            try
            {
                propURL = new URL(custom);
            }
            catch (MalformedURLException ex)
            {
                System.err.print("muCommander: " + ex);
                return;
            }
        }
        else
        {
            // Determine where the configuration directory is by figuring
            // out where felix.jar is located on the system class path.
            File confDir = null;
            String classpath = System.getProperty("java.class.path");
            int index = classpath.toLowerCase().indexOf("felix.jar");
            int start = classpath.lastIndexOf(File.pathSeparator, index) + 1;
            if (index >= start)
            {
                // Get the path of the felix.jar file.
                String jarLocation = classpath.substring(start, index);
                // Calculate the conf directory based on the parent
                // directory of the felix.jar directory.
                confDir = new File(
                    new File(new File(jarLocation).getAbsolutePath()).getParent(),
                    CONFIG_DIRECTORY);
            }
            else
            {
                // Can't figure it out so use the current directory as default.
                confDir = new File(System.getProperty("user.dir"), CONFIG_DIRECTORY);
            }

            try
            {
                propURL = new File(confDir, SYSTEM_PROPERTIES_FILE_VALUE).toURL();
            }
            catch (MalformedURLException ex)
            {
                System.err.print("muCommander: " + ex);
                return;
            }
        }

        // Read the properties file.
        Properties props = new Properties();
        InputStream is = null;
        try
        {
            is = propURL.openConnection().getInputStream();
            props.load(is);
            is.close();
        }
        catch (FileNotFoundException ex)
        {
            // Ignore file not found.
        }
        catch (Exception ex)
        {
            System.err.println(
                "muCommander: Error loading system properties from " + propURL);
            System.err.println("muCommander: " + ex);
            try
            {
                if (is != null) is.close();
            }
            catch (IOException ex2)
            {
                // Nothing we can do.
            }
            return;
        }

        // Perform variable substitution on specified properties.
        for (Enumeration<String> e = (Enumeration<String>) props.propertyNames(); e.hasMoreElements(); )
        {
            String name = e.nextElement();
            System.setProperty(name,
                Util.substVars(props.getProperty(name), name, null, null));
        }
    }

    /**
     * <p>
     * Loads the configuration properties in the configuration property file
     * associated with the framework installation; these properties
     * are accessible to the framework and to bundles and are intended
     * for configuration purposes. By default, the configuration property
     * file is located in the <tt>conf/</tt> directory of the Felix
     * installation directory and is called "<tt>config.properties</tt>".
     * The installation directory of Felix is assumed to be the parent
     * directory of the <tt>felix.jar</tt> file as found on the system class
     * path property. The precise file from which to load configuration
     * properties can be set by initializing the "<tt>felix.config.properties</tt>"
     * system property to an arbitrary URL.
     * </p>
     * @return A <tt>Properties</tt> instance or <tt>null</tt> if there was an error.
    **/
    public static Map<String, String> loadConfigProperties()
    {
        // The config properties file is either specified by a system
        // property or it is in the conf/ directory of the Felix
        // installation directory.  Try to load it from one of these
        // places.

        // See if the property URL was specified as a property.
        URL propURL = null;
        String custom = System.getProperty(CONFIG_PROPERTIES_PROP);
        if (custom != null)
        {
            try
            {
                propURL = new URL(custom);
            }
            catch (MalformedURLException ex)
            {
                System.err.print("muCommander: " + ex);
                return null;
            }
        }
        else
        {
            // Determine where the configuration directory is by figuring
            // out where felix.jar is located on the system class path.
            File confDir = null;
            String classpath = System.getProperty("java.class.path");
            int index = classpath.toLowerCase().indexOf("felix.jar");
            int start = classpath.lastIndexOf(File.pathSeparator, index) + 1;
            if (index >= start)
            {
                // Get the path of the felix.jar file.
                String jarLocation = classpath.substring(start, index);
                // Calculate the conf directory based on the parent
                // directory of the felix.jar directory.
                confDir = new File(
                    new File(new File(jarLocation).getAbsolutePath()).getParent(),
                    CONFIG_DIRECTORY);
            }
            else
            {
                // Can't figure it out so use the current directory as default.
                confDir = new File(System.getProperty("user.dir"), CONFIG_DIRECTORY);
            }

            try
            {
                propURL = new File(confDir, CONFIG_PROPERTIES_FILE_VALUE).toURL();
            }
            catch (MalformedURLException ex)
            {
                System.err.print("muCommander: " + ex);
                return null;
            }
        }

        // Read the properties file.
        Properties props = new Properties();
        InputStream is = null;
        try
        {
            // Try to load config.properties.
            is = propURL.openConnection().getInputStream();
            props.load(is);
            is.close();
        }
        catch (Exception ex)
        {
            // Try to close input stream if we have one.
            try
            {
                if (is != null) is.close();
            }
            catch (IOException ex2)
            {
                // Nothing we can do.
            }

            return null;
        }

        // Perform variable substitution for system properties and
        // convert to dictionary.
        Map<String, String> map = new HashMap<String, String>();
        for (Enumeration<String> e = (Enumeration<String>) props.propertyNames(); e.hasMoreElements(); )
        {
            String name = e.nextElement();
            map.put(name,
                Util.substVars(props.getProperty(name), name, null, props));
        }

        return map;
    }

    public static void copySystemProperties(Map<String, String> configProps)
    {
        for (Enumeration<String> e = (Enumeration<String>) System.getProperties().propertyNames();
             e.hasMoreElements(); )
        {
            String key = e.nextElement();
            if (key.startsWith("felix.") || key.startsWith("org.osgi.framework."))
            {
                configProps.put(key, System.getProperty(key));
            }
        }
    }

    private static void logTimeSinceStart(String text) {
        System.out.println("[muEpoch+" + (System.currentTimeMillis() - START_EPOCH) + "ms] muCommander: " + text);
    }

}
