package com.mucommander.conf;

import java.util.*;
import java.io.*;
//import org.xml.sax.*;

/**
 * Handles configuration.
 * <p>
 * When it is first accessed, this class will try to load the configuration file.
 * It will look for it in the <i>home</i> directory of the user who started the JVM
 * (or in the <i>user.home</i> System property if it has been modified).
 * </p>
 * <p>
 * Once the configuration file has been loaded in memory, variables can be set or
 * retrieved through the {@link #getVariable(String)} and {@link #setVariable(String,String)}
 * methods.<br>
 * Configuration variable names follow the same syntax as the Java System properties. Each
 * variable is contained in a section, which can itself be contained in a section. Each section
 * name is separated by a '.' character. For example: <i>muwire.someSection.someVariable</i> refers to
 * the <i>someVariable</i> variable found in the <i>someSection</i> section of <i>muwire</i>.
 * </p>
 * <p>
 * It is possible to monitor configuration file changes with a system of listeners.<br>
 * Any class implementing the {net.muwire.common.manager.ConfigurationListener} interface
 * can be registered through the {@link #addConfigurationListener(ConfigurationListener)}
 * method. It will then be warned as soon as a configuration variable has been modified.<br>
 * </p>
 * <p>
// * Upon system initialisation, the configuration manager will start a configuration monitoring daemon.
// * This daemon will check every {@link #CONTROL_DELAY_ENTRY} milliseconds for a modification of the configuration
// * file, and will reload it if necessary.
 * </p>
 * @author Nicolas Rinaudo, Maxence Bernard
 */
public class ConfigurationManager {
    /** Contains all the registered configuration listeners. */
    private static LinkedList listeners = new LinkedList();
    /** Name of the configuration file to parse. */
    public static final String CONFIGURATION_FILE = "preferences.xml";
    /** Instance of ConfigurationManager used to enfore configuration file loading. */
    private static ConfigurationManager singleton = new ConfigurationManager();
    /** Holds the content of the configuration file. */
    private static ConfigurationTree tree;
//    /** Configuration file monitoring daemon. */
//    private static Thread daemon;
//    public static final String CONTROL_DELAY_ENTRY = "muwire.conf.control";
//    private static final int DEFAULT_CONTROL_DELAY = 1000;
//    private static int controlDelay;

    /* ------------------------ */
    /*      Initialisation      */
    /* ------------------------ */
    /**
     * Constructor used to ensure that the configuration file is loaded at boot time.
     */
    private ConfigurationManager() {
        tree = new ConfigurationTree("root");
        try {
            loadConfiguration();
        }
        catch(Exception e) {
//			if(com.mucommander.Debug.TRACE)
            System.out.println("No configuration file found.");
            File homeFolder = getConfigurationFolder();
            if(!homeFolder.exists()) {
                System.out.println("Creating mucommander home folder (~/.mucommander)");
                if(!getConfigurationFolder().mkdir())
                    System.out.println("Warning: unable to create folder: "+homeFolder.getAbsolutePath()+"/.mucommander");
                }
        }
		
//        initConfiguration();
//        initDaemon();
    }
    /* End of contructor ConfigurationManager() */

//    /**
//     * Initialises the ConfigurationManager's configuration.
//     */
//    private void initConfiguration() {
//        try {controlDelay = Integer.parseInt(CONTROL_DELAY_ENTRY);}
//        catch(Exception e) {controlDelay = DEFAULT_CONTROL_DELAY;}
//    }
//    /* End of method initConfiguration() */
//
//    /**
//     * Initialises the configuration file monitoring daemon.
//     */
//    private void initDaemon() {
//        daemon = new Thread(this);
//        daemon.setDaemon(true);
//        daemon.setPriority(Thread.MIN_PRIORITY);
//        daemon.start();
//    }
//    /* End of method initDaemon() */

    /* ------------------------ */
    /*       File handling      */
    /* ------------------------ */
    /**
     * Returns the path to the configuration file.
     * @return the path to the configuration file.
     */
    public static String getConfigurationFilePath() {
        return new File(getConfigurationFolder(), CONFIGURATION_FILE).getAbsolutePath();
    }

    public static File getConfigurationFolder() {
        return new File(System.getProperty("user.home")+"/.mucommander");
    }

    /**
     * Loads the specified configuration file in memory.
     * @param path path to the configuration file to load in memory.
     */
    public static synchronized void loadConfiguration(String path) throws Exception {
        ConfigurationParser parser;
        ConfigurationLoader loader;

        parser = new ConfigurationParser(loader = new ConfigurationLoader());
        parser.parse(path);
    }
    /* End of method loadConfiguration(String) */

    /**
     * Loads the configuration file in memory.
     */
    public static synchronized void loadConfiguration() throws Exception {
		loadConfiguration(getConfigurationFilePath());
	}
    /* End of method loadConfiguration() */

    /**
     * Writes the configuration to the specified file.
     * <p>
     * This method is provided to enable softwares to save the current configuration elsewhere
     * than the system configuration file.
     * </p>
     * @param path path to the file in which the configuration should be written.
     */
    public static synchronized void writeConfiguration(String path) throws FileNotFoundException {
        ConfigurationWriter writer;
        PrintWriter out;

        writer = new ConfigurationWriter();
        writer.writeXML(out = new PrintWriter(new FileOutputStream(getConfigurationFilePath())));
        out.close();
    }
    /* End of method writeConfiguration(String) */

    /**
     * Writes the current variables to the configuration file.
     */
    public static synchronized void writeConfiguration() throws FileNotFoundException {
		writeConfiguration(getConfigurationFilePath());
	}
    /* End of method writeConfiguration() */

    /* ------------------------ */
    /*      Variable access     */
    /* ------------------------ */
    /**
     * Builds a configuration tree starting at the specified node.
     * @param builder builder used to create the tree.
     * @param node    root of the tree.
     */
    private static void buildConfigurationTree(ConfigurationTreeBuilder builder, ConfigurationTree node) {
        Iterator          iterator;
        ConfigurationLeaf leaf;

        builder.addNode(node.getName());
        iterator = node.getLeafs();
        while(iterator.hasNext()) {
            leaf = (ConfigurationLeaf)iterator.next();
            builder.addLeaf(leaf.getName(), leaf.getValue());
        }
        iterator = node.getNodes();
        while(iterator.hasNext())
            buildConfigurationTree(builder, (ConfigurationTree)iterator.next());
        builder.closeNode(node.getName());
    }
    /* End of method buildConfigurationTree(ConfigurationTreeBuilder, ConfigurationTree) */

    /**
     * Builds a configuration tree.
     * @param builder builder used to create the tree.
     */
    public static synchronized void buildConfigurationTree(ConfigurationTreeBuilder builder) {buildConfigurationTree(builder, tree);}

    /**
     * Retrieves the value of the specified configuration variable.
     * @param  var name of the variable to retrieve.
     * @return the value of the specified configuration variable.
     */
    public static synchronized String getVariable(String var) {
        StringTokenizer   parser;
        ConfigurationTree node;
        String            buffer;

        parser = new StringTokenizer(var, ".");
        node   = tree;
        while(parser.hasMoreTokens()) {
            buffer = parser.nextToken();
            if(parser.hasMoreTokens()) {
                if((node = node.getNode(buffer)) == null)
                    return null;
            }
            else
                return node.getLeaf(buffer);
        }
        return null;
    }
    /* End of method getVariable(String) */


    /**
     * Retrieves the value of the specified configuration variable and assigns it
	 * a given default value if the the value returned by {@link #getVariable(String)} is
	 * <code>null</code>.
     *
	 * @param var name of the variable to retrieve.
     * @param defaultValue defaultValue assigned if the variable's value is <code>null</code>.
	 *
	 * @return the value of the specified configuration variable.
     */
    public static synchronized String getVariable(String var, String defaultValue) {
    	String value = getVariable(var);
		
		if (value==null) {
			setVariable(var, defaultValue);
			return defaultValue;
		}
		return value;
	}
    /* End of method getVariable(String, String) */


    /**
     * Sets the value of the specified configuration variable.
     * @param var   name of the variable to set.
     * @param value value for the specified variable.
     */
    public static synchronized void setVariable(String var, String value) {
        // Maxence patch: ConfigurationParser does not handle empty elements
		// (containing whitespace characters) properly
		if(value.trim().equals(""))
			return;
		
		StringTokenizer   parser;
        String            buffer;
        ConfigurationTree node;
        ConfigurationTree temporaryNode;
        String            oldValue;
        
		parser = new StringTokenizer(var, ".");
        node   = tree;

        while(parser.hasMoreTokens()) {
            buffer = parser.nextToken();
            if(parser.hasMoreTokens()) {
                if((temporaryNode = node.getNode(buffer)) == null)
                    node = node.createNode(buffer);
                else
                    node = temporaryNode;
            }
            else {
                oldValue = node.getLeaf(buffer);
                if(node.setLeaf(buffer, value))
                    if(!fireConfigurationEvent(new ConfigurationEvent(var, value)))
                        node.setLeaf(buffer, oldValue);
            }
        }
    }
    /* End of method setVariable(String, String) */

//    /* ------------------------ */
//    /*      File monitoring     */
//    /* ------------------------ */
//    /**
//     * Main file monitoring deamon loop.
//     */
//    public void run() {
//        long lastAccessed;
//        long buffer;
//
//        lastAccessed = getAccessedTime();
//
//        while(true) {
//            try {daemon.sleep(controlDelay);}
//            catch(Exception e) {}
//
//            /* Checks whether the configuration file should be reloaded. */
//            buffer = getAccessedTime();
//            if(buffer != lastAccessed) {
//                try {loadConfiguration();}
//                catch(Exception e) {}
//                lastAccessed = buffer;
//            }
//        }
//    }
//    /* End of method run() */

//    /**
//     * Returns the last time the configuration file has been modified.
//     * @return the last time the configuration file has been modified.
//     */
//    private static final long getAccessedTime() {return new File(getConfigurationFilePath()).lastModified();}

//    /* ------------------------ */
//    /*    Listeners handling    */
//    /* ------------------------ */
//    /**
//     * Method called when a configuration variable has been modified.
//     * <p>
//     * This method will check for the {@link #CONTROL_DELAY_ENTRY}
//     * configuration variable, and return false if its value is not correct.
//     * </p>
//     * @return false if the event concerns {@link #CONTROL_DELAY_ENTRY} and the value is illegal, true otherwise.
//     */
//    public boolean configurationChanged(ConfigurationEvent event) {
//        if(event.getVariable().equals(CONTROL_DELAY_ENTRY)) {
//            try {controlDelay = Integer.parseInt(event.getValue());}
//            catch(Exception e) {return false;}
//        }
//        return true;
//    }
//    /* End of method configurationChanged(ConfigurationEvent) */

    /**
     * Adds the specified configuration listener to the list of registered listeners.
     * @param listener listener to insert in the list.
     */
    public static synchronized void addConfigurationListener(ConfigurationListener listener) {listeners.add(listener);}

    /**
     * Removes the specified configuration listener from the list of registered listeners.
     * @param listener listener to remove from the list.
     */
    public static synchronized void removeConfigurationListener(ConfigurationListener listener) {listeners.remove(listener);}

    /**
     * Notifies all the registered configuration listeners of a configuration change event.
     * @param  event describes the configuration change.
     * @return true if the change wasn't vetoed, false otherwise.
     */
    static synchronized boolean fireConfigurationEvent(ConfigurationEvent event) {
        Iterator iterator;

        iterator = listeners.iterator();
        while(iterator.hasNext())
            if(!((ConfigurationListener)iterator.next()).configurationChanged(event))
                return false;
        return true;
    }
    /* End of method fireConfigurationEvent(ConfigurationEvent) */
}
/* End of class ConfigurationManager */
