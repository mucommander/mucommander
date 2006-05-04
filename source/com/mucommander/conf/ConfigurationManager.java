package com.mucommander.conf;

import java.util.*;
import java.io.*;
import java.awt.Color;

import com.mucommander.PlatformManager;


/**
 * Handles configuration.
 * <p>
 * When it is first accessed, this class will try to load the configuration file.
 * </p>
 * <p>
 * Once the configuration file has been loaded in memory, variables can be set or
 * retrieved through the {@link #getVariable(String)} and {@link #setVariable(String,String)}
 * methods.<br>
 * Configuration variable names follow the same syntax as the Java System properties. Each
 * variable is contained in a section, which can itself be contained in a section. Each section
 * name is separated by a '.' character. For example: <i>mucommander.someSection.someVariable</i> refers to
 * the <i>someVariable</i> variable found in the <i>someSection</i> section of <i>mucommander</i>.
 * </p>
 * <p>
 * It is possible to monitor configuration file changes with a system of listeners.<br>
 * Any class implementing the {com.mucommander.conf.ConfigurationListener} interface
 * can be registered through the {@link #addConfigurationListener(ConfigurationListener)}
 * method. It will then be warned as soon as a configuration variable has been modified.<br>
 * </p>
 * @author Nicolas Rinaudo, Maxence Bernard
 */
public class ConfigurationManager {
    /** Path to the configuraation file. */
    private static String configurationFile;

    /** Contains all registered configuration listeners, stored as weak references */
    private static WeakHashMap listeners = new WeakHashMap();

    /** Name of the configuration file */
    private static final String CONFIGURATION_FILENAME = "preferences.xml";

    /** Holds the content of the configuration file. */
    private static ConfigurationTree tree = new ConfigurationTree("root");

    static {setVariable("prefs.conf_version", com.mucommander.Launcher.MUCOMMANDER_VERSION);}

    /**
     * Prevents the class from being instanciated.
     */
    private ConfigurationManager() {}
	
	
    /* ------------------------ */
    /*       File handling      */
    /* ------------------------ */

    /**
     * Returns the path to the configuration file on the current platform.
     */
    private static String getConfigurationFilePath() {
        if(configurationFile == null)
            return new File(PlatformManager.getPreferencesFolder(), CONFIGURATION_FILENAME).getAbsolutePath();
        else
            return configurationFile;
    }
	
	
    /**
     * Loads the specified configuration file in memory.
     * @param path path to the configuration file to load in memory.
     */
    private static synchronized void parseConfiguration(String path) throws Exception {
        ConfigurationParser parser = new ConfigurationParser(new ConfigurationLoader());
        parser.parse(path);
    }

    /**
     * Reads configuration from the specified file.
     * <p>
     * The <code>path</code> parameter can be set to <code>null</code>, in which case the default
     * configuration file will used.
     * </p>
     * @param path path to the configuration file.
     */
    public static synchronized boolean loadConfiguration(String path) {
        try {
            if(path == null)
                parseConfiguration(getConfigurationFilePath());
            else {
                setConfigurationFile(path);
                parseConfiguration(path);
            }
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Found and loaded configuration file: "+getConfigurationFilePath(), -1);						
			
            // If version in configuration differs from current version, 
            // import and move variables which have moved in the configuration tree
            String confVersion = getVariable("prefs.conf_version");
            if(confVersion!=null && !confVersion.equals(com.mucommander.Launcher.MUCOMMANDER_VERSION)) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Version changed, looking for variables to migrate");
                migrateVariable("prefs.show_hidden_files", "prefs.file_table.show_hidden_files");
                migrateVariable("prefs.auto_size_columns", "prefs.file_table.auto_size_columns");
                migrateVariable("prefs.show_toolbar", "prefs.toolbar.visible");
                migrateVariable("prefs.show_status_bar", "prefs.status_bar.visible");
                migrateVariable("prefs.show_command_bar", "prefs.command_bar.visible");
            }
						
            return true;
        }
        catch(Exception e) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("No configuration file found at "+getConfigurationFilePath());			
        }

        return false;
    }

    /**
     * Opens and reads the configuration file.
     */
    public static synchronized boolean loadConfiguration() {return loadConfiguration(null);}


    /**
     * Moves the value of a variable to another.
     */
    private static void migrateVariable(String fromVar, String toVar) {
        String fromValue = getVariable(fromVar);
        if(fromValue!=null) {
            setVariable(toVar, fromValue);
            setVariable(fromVar, null);
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Variable "+fromVar+" migrated to "+toVar);
        }
    }
	
	
    /**
     * Writes the configuration to the configuration file.
     */
    public static synchronized void writeConfiguration() {
        PrintWriter out = null;
        try {
            ConfigurationWriter writer = new ConfigurationWriter();
            String filePath = getConfigurationFilePath();
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Writing preferences file: "+filePath);						
			
            // Use UTF-8 encoding
            out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
            writer.writeXML(out);
        }
        catch(IOException e) {
            // Notify user that preferences file could not be written
            System.out.println("muCommander was unable to write preferences file: "+e);
        }
        finally {
            if(out!=null)
                out.close();
        }
    }

		
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

    /**
     * Builds a configuration tree.
     * @param builder builder used to create the tree.
     */
    public static synchronized void buildConfigurationTree(ConfigurationTreeBuilder builder) {buildConfigurationTree(builder, tree);}


    /**
     * Returns true if the given variable has a value (not null and not equals to "" after being trimmed).
     * @param var the name of the variable to test.
     */
    public static boolean isVariableSet(String var) {
        String value = getVariable(var);
        return value!=null && !value.trim().equals("");
    }


    /**
     * Returns the value of the specified configuration variable.
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

	
    /**
     * Returns the value of the specified configuration variable and assigns it
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

	
    /**
     * Returns the value of the given configuration variable, <code>-1</code>
     * if the variable has no value OR if the variable cannot be parsed as an integer.
     *
     * @param  var name of the variable to retrieve.
     * @return the value of the specified configuration variable.
     */
    public static synchronized int getVariableInt(String var) {
        String val = getVariable(var);
        if(val==null)
            return -1;
		
        try {
            return Integer.parseInt(val);
        }
        catch(NumberFormatException e) {
            return -1;
        }
    }


    /**
     * Returns the value of the given configuration variable and assigns it
     * a given default int value if the the value returned by {@link #getVariable(String)} is
     * <code>null</code> or could not be parsed as an int. 
     *
     * <p>Returns <code>-1</code> if the variable cannot be parsed as an int.</p>
     *
     * @param  var name of the variable to retrieve.
     * @param defaultValue defaultValue assigned if the variable's value is <code>null</code> or could not be parsed as an int.
     * @return the value of the specified configuration variable.
     */
    public static synchronized int getVariableInt(String var, int defaultValue) {
    	String value = getVariable(var);
		
        if (value==null) {
            setVariable(var, ""+defaultValue);
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        }
        catch(NumberFormatException e) {
            setVariable(var, ""+defaultValue);
            return defaultValue;
        }
    }


    /**
     * Returns the value of the given configuration variable and assigns it
     * a given default float value if the the value returned by {@link #getVariable(String)} is
     * <code>null</code> or could not be parsed as a float. 
     *
     * <p>Returns <code>-1</code> if the variable cannot be parsed as a float.</p>
     *
     * @param var name of the variable to retrieve.
     * @param defaultValue defaultValue assigned if the variable's value is <code>null</code> or could not be parsed as a float.
     * @return the value of the specified configuration variable.
     */
    public static synchronized float getVariableFloat(String var, float defaultValue) {
    	String value = getVariable(var);
		
        if (value==null) {
            setVariable(var, ""+defaultValue);
            return defaultValue;
        }

        try {
            return Float.parseFloat(value);
        }
        catch(NumberFormatException e) {
            setVariable(var, ""+defaultValue);
            return defaultValue;
        }
    }
	

    /**
     * Retrieves the boolean value of the given configuration variable and assigns it
     * a given default value if the the value returned by {@link #getVariable(String)} is
     * <code>null</code>. 
     *
     * <p>Returns <code>false</code> if the variable cannot be parsed as a boolean.</p>
     *
     * @param var name of the variable to retrieve.
     * @param defaultValue defaultValue assigned if the variable's value is <code>null</code>.
     * @return the value of the specified configuration variable.
     */
    public static synchronized boolean getVariableBoolean(String var, boolean defaultValue) {
    	String value = getVariable(var);
		
        if (value==null) {
            setVariable(var, ""+defaultValue);
            return defaultValue;
        }

        return value.equals("true");
    }


    /*
     * Returns the pref color value if there is one, if not the default one.
     */
    public static Color getVariableColor(String colorVariableName, String defaultColorVal) {
        String colorValue = getVariable(colorVariableName, defaultColorVal);

        return colorValue==null?null:new Color(Integer.parseInt(colorValue, 16));
    }
	

    /**
     * Sets the value of the specified configuration variable.
     * @param var   name of the variable to set.
     * @param value value for the specified variable.
     */
    public static synchronized void setVariable(String var, String value) {

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
				
                // Since 0.8 beta2: do nothing (return) if value hasn't changed
                if((oldValue==null && value==null) || (oldValue!=null && oldValue.equals(value)))
                    return;
					
                if(node.setLeaf(buffer, value))
                    if(!fireConfigurationEvent(new ConfigurationEvent(var, value)))
                        node.setLeaf(buffer, oldValue);
            }
        }
    }

	
    /**
     * Sets the value of the specified configuration variable.
     *
     * @param var name of the variable to set.
     * @param value value for the specified variable.
     */
    public static synchronized void setVariableInt(String var, int value) {
        setVariable(var, ""+value);
    }


    /**
     * Sets the value of the specified configuration variable.
     *
     * @param var name of the variable to set.
     * @param value value for the specified variable.
     */
    public static synchronized void setVariableFloat(String var, float value) {
        setVariable(var, ""+value);
    }


    /**
     * Sets the value of the specified configuration variable.
     *
     * @param var name of the variable to set.
     * @param value value for the specified variable.
     */
    public static synchronized void setVariableBoolean(String var, boolean value) {
        setVariable(var, ""+value);
    }
	
	
    /**
     * Sets the value of the specified configuration variable.
     *
     * @param var name of the variable to set.
     * @param value value for the specified variable.
     */
    public static synchronized void setVariableColor(String var, Color value) {
        String colorString = Integer.toHexString(value.getRGB());
        setVariable(var, colorString.substring(2, colorString.length()));
    }

		
    /**
     * Adds the specified configuration listener to the list of registered listeners.
     *
     * <p>Listeners are stored as weak references so {@link #removeConfigurationListener(ConfigurationListener) removeConfigurationListener()}
     * doesn't need to be called for listeners to be garbage collected when they're not used anymore.</p>
     *
     * @param listener the listener to add to the list of registered listeners.
     */
    public static synchronized void addConfigurationListener(ConfigurationListener listener) {
        if(listener==null)
            return;
		
        listeners.put(listener, null);
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(listeners.size()+" listeners");
    }

    /**
     * Removes the specified configuration listener from the list of registered listeners.
     *
     * @param listener the listener to remove from the list of registered listeners.
     */
    public static synchronized void removeConfigurationListener(ConfigurationListener listener) {
        listeners.remove(listener);
			
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(listeners.size()+" listeners");
    }

    /**
     * Notifies all the registered configuration listeners of a configuration change event.
     *
     * @param  event describes the configuration change.
     * @return true if the change wasn't vetoed, false otherwise.
     */
    static synchronized boolean fireConfigurationEvent(ConfigurationEvent event) {
        Iterator iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            if(!((ConfigurationListener)iterator.next()).configurationChanged(event))
                return false;
        return true;
    }

    // - Custom configuration file handling -----------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Sets the path to the configuration file.
     * <p>
     * Note that this will not trigger a reloading of the configuration. In order for this
     * method to have any effect on the configuration file that is loaded, it must be called
     * before any call to {@link getVariable(String)}.
     * </p>
     * <p>
     * The <code>file</code> can be <code>null</code>. If such is the case, the configuration
     * file will revert to the default one.
     * </p>
     * @param file path to the configuration file that should be loaded.
     */
    public static void setConfigurationFile(String file) {configurationFile = file;}
}
