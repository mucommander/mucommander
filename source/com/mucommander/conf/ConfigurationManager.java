/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.conf;

import com.mucommander.PlatformManager;
import com.mucommander.RuntimeConstants;
import com.mucommander.io.BackupInputStream;
import com.mucommander.io.BackupOutputStream;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

/**
 * Handles configuration.
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
    private static File configurationFile;

    /** Contains all registered configuration listeners, stored as weak references */
    private static WeakHashMap listeners = new WeakHashMap();

    /** Name of the configuration file */
    private static final String DEFAULT_CONFIGURATION_FILE_NAME = "preferences.xml";

    /** Holds the content of the configuration file. */
    private static ConfigurationTree tree = new ConfigurationTree("root");

    static {setVariable(ConfigurationVariables.VERSION, RuntimeConstants.VERSION);}

    /**
     * Prevents the class from being instanciated.
     */
    private ConfigurationManager() {}
	

    // - File handling ---------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Reads configuration from the specified file.
     */
    public static synchronized void loadConfiguration() throws Exception {
        ConfigurationParser parser;
        InputStream         in;

        in   = null;
        try {
            parser = new ConfigurationParser(new ConfigurationLoader());
            parser.parse(in = new BackupInputStream(getConfigurationFile()));

            // If version in configuration differs from current version, 
            // import and move variables which have moved in the configuration tree
            // and set new version string
            String confVersion = getVariable(ConfigurationVariables.VERSION);
            if(confVersion!=null && !confVersion.equals(RuntimeConstants.VERSION)) {
                migrateVariable("prefs.show_hidden_files", ConfigurationVariables.SHOW_HIDDEN_FILES);
                migrateVariable("prefs.auto_size_columns", ConfigurationVariables.AUTO_SIZE_COLUMNS);
                migrateVariable("prefs.show_toolbar",      ConfigurationVariables.TOOLBAR_VISIBLE);
                migrateVariable("prefs.show_status_bar",   ConfigurationVariables.STATUS_BAR_VISIBLE);
                migrateVariable("prefs.show_command_bar",  ConfigurationVariables.COMMAND_BAR_VISIBLE);
                setVariable(ConfigurationVariables.VERSION, RuntimeConstants.VERSION);
            }
        }
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
        }
    }

    /**
     * Moves the value of a variable to another.
     */
    private static void migrateVariable(String fromVar, String toVar) {
        String fromValue = getVariable(fromVar);
        if(fromValue!=null) {
            setVariable(toVar, fromValue);
            setVariable(fromVar, null);
        }
    }
	
	
    /**
     * Writes the configuration to the configuration file.
     */
    public static synchronized void writeConfiguration() {
        BackupOutputStream out;

        out = null;
        try {
            ConfigurationWriter writer = new ConfigurationWriter();

            writer.writeXML(out = new BackupOutputStream(getConfigurationFile()));
            out.close();
        }
        catch(IOException e) {
            // Cancels the backup operation if an error occured.
            if(out != null) {
                try {out.close(false);}
                catch(Exception e2) {}
            }
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
        ConfigurationTree buffer;

        builder.addNode(node.getName());
        iterator = node.getLeafs();
        while(iterator.hasNext()) {
            leaf = (ConfigurationLeaf)iterator.next();
            if(leaf.getValue() != null)
                builder.addLeaf(leaf.getName(), leaf.getValue());
        }
        iterator = node.getNodes();
        while(iterator.hasNext()) {
            buffer = (ConfigurationTree)iterator.next();
            if(buffer.hasNodes() || buffer.hasLeafs())
                buildConfigurationTree(builder, buffer);
        }
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
     * Returns the value of the given configuration variable, <code>-1</code>
     * if the variable has no value OR if the variable cannot be parsed as a long.
     *
     * @param  var name of the variable to retrieve.
     * @return the value of the specified configuration variable.
     */
    public static synchronized long getVariableLong(String var) {
        String val = getVariable(var);
        if(val==null)
            return -1;
		
        try {
            return Long.parseLong(val);
        }
        catch(NumberFormatException e) {
            return -1;
        }
    }

    public static synchronized long getVariableLong(String var, long defaultValue) {
    	String value = getVariable(var);
		
        if (value==null) {
            setVariable(var, Long.toString(defaultValue));
            return defaultValue;
        }

        try {
            return Long.parseLong(value);
        }
        catch(NumberFormatException e) {
            setVariable(var, Long.toString(defaultValue));
            return defaultValue;
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
            setVariable(var, Integer.toString(defaultValue));
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        }
        catch(NumberFormatException e) {
            setVariable(var, Integer.toString(defaultValue));
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
            setVariable(var, Float.toString(defaultValue));
            return defaultValue;
        }

        try {
            return Float.parseFloat(value);
        }
        catch(NumberFormatException e) {
            setVariable(var, Float.toString(defaultValue));
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
            setVariable(var, Boolean.toString(defaultValue));
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
     * @return true if the variable has been modified, false otherwise
     */
    public static synchronized boolean setVariable(String var, String value) {

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
                if((temporaryNode = node.getNode(buffer)) == null) {
                    // If the value is null, we're trying to delete a variable.
                    // It would be silly to create its parent node. Abort.
                    if(value == null)
                        return false;
                    node = node.createNode(buffer);
                }
                else
                    node = temporaryNode;
            }
            else {
                oldValue = node.getLeaf(buffer);
				
                // Since 0.8 beta2: do nothing (return) if value hasn't changed
                if((oldValue==null && value==null) || (oldValue!=null && oldValue.equals(value)))
                    return false;
					
                if(node.setLeaf(buffer, value)) {
                    if(!fireConfigurationEvent(new ConfigurationEvent(var, value))) {
                        // Value change vetoed by one of the ConfigurationListener
                        node.setLeaf(buffer, oldValue);
                        return false;
                    }
                    return true;
                }
            }
        }

        return false;
    }

	
    /**
     * Sets the value of the specified configuration variable.
     *
     * @param var name of the variable to set.
     * @param value value for the specified variable.
     * @return true if the variable has been modified, false otherwise
     */
    public static synchronized boolean setVariableInt(String var, int value) {
        return setVariable(var, Integer.toString(value));
    }


    /**
     * Sets the value of the specified configuration variable.
     *
     * @param var name of the variable to set.
     * @param value value for the specified variable.
     * @return true if the variable has been modified, false otherwise
     */
    public static synchronized boolean setVariableFloat(String var, float value) {
        return setVariable(var, Float.toString(value));
    }


    /**
     * Sets the value of the specified configuration variable.
     *
     * @param var name of the variable to set.
     * @param value value for the specified variable.
     * @return true if the variable has been modified, false otherwise
     */
    public static synchronized boolean setVariableBoolean(String var, boolean value) {
        return setVariable(var, Boolean.toString(value));
    }
	
	
    /**
     * Sets the value of the specified configuration variable.
     *
     * @param var name of the variable to set.
     * @param value value for the specified variable.
     * @return true if the variable has been modified, false otherwise
     */
    public static synchronized boolean setVariableColor(String var, Color value) {
        String colorString = Integer.toHexString(value.getRGB());
        return setVariable(var, colorString.substring(2, colorString.length()));
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
    }

    /**
     * Removes the specified configuration listener from the list of registered listeners.
     *
     * @param listener the listener to remove from the list of registered listeners.
     */
    public static synchronized void removeConfigurationListener(ConfigurationListener listener) {
        listeners.remove(listener);
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
    public static File getConfigurationFile() {
        if(configurationFile == null)
            return new File(PlatformManager.getPreferencesFolder(), DEFAULT_CONFIGURATION_FILE_NAME);
        return configurationFile;
    }

    /**
     * Sets the path to the configuration file.
     * @param     file                  path to the configuration file that should be loaded.
     * @exception FileNotFoundException if the file is not accessible.
     */
    public static void setConfigurationFile(String file) throws FileNotFoundException {
        File tempFile;

        // If the file exists, it must accessible and readable.
        tempFile = new File(file);
        if(!(tempFile.exists() && tempFile.isFile() && tempFile.canRead()))
            throw new FileNotFoundException("Not a valid file: " + file);

        configurationFile = tempFile;
    }
}
