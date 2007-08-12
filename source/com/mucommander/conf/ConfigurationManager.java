/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.conf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * Used to access and modify the application's configuration.
 * <h3>Reading and writing the configuration</h3>
 * <h3>Accessing the configuration</h3>
 * @author Nicolas Rinaudo
 */
public class ConfigurationManager {
    // - Class variables -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Used to get access to the configuration source's input and output streams. */
    private static ConfigurationSource        source;
    /** Used to create objects that will read from the configuration source. */
    private static ConfigurationReaderFactory readerFactory;
    /** Used to create objects that will write to the configuration source. */
    private static ConfigurationWriterFactory writerFactory;
    /** Holds the content of the configuration file. */
    private static ConfigurationSection       root = new ConfigurationSection();



    // - Synchronisation locks -----------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Used to synchronise concurent access of the configuration source. */
    private static Object sourceLock = new Object();
    /** Used to synchronise concurent access of the reader factory. */
    private static Object readerLock = new Object();
    /** Used to synchronise concurent access of the writer factory. */
    private static Object writerLock = new Object();



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Prevents the class from being instanciated.
     */
    private ConfigurationManager() {}



    // - Configuration source --------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Sets the source that will be used to raed and write configuration information.
     * @param s new configuration source.
     */
    public static void setConfigurationSource(ConfigurationSource s) {synchronized(sourceLock) {source = s;}}

    /**
     * Returns the current configuration source.
     * @return the current configuration source.
     */
    public static ConfigurationSource getConfigurationSource() {synchronized(sourceLock) {return source;}}



    // - Reader handling -------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Sets the factory that will be used to create {@link ConfigurationReader reader} instances.
     * @param f factory that will be used to create reader instances.
     */
    public static void setConfigurationReaderFactory(ConfigurationReaderFactory f) {synchronized(readerLock) {readerFactory = f;}}

    /**
     * Returns the factory that is being used to create {@link ConfigurationReader reader} instances.
     * @return the factory that is being used to create reader instances.
     */
    public static ConfigurationReaderFactory getConfigurationReaderFactory() {synchronized(readerLock) {return readerFactory;}}

    /**
     * Returns an instance of the class that will be used to read configuration data.
     * <p>
     * By default, this method will return an instance of {@link XmlConfigurationReader}. However, this can be
     * modified by {@link #setConfigurationReaderFactory(ConfigurationReaderFactory)}.
     * </p>
     * @return an instance of the class that will be used to read configuration data.
     */
    public static ConfigurationReader getConfigurationReader() {
        ConfigurationReaderFactory factory;

        // If no factory has been set, return an XML configuration reader.
        if((factory = getConfigurationReaderFactory()) == null)
            return new XmlConfigurationReader();

        return factory.getReaderInstance();
    }



    // - Writer handling -------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Sets the factory that will be used to create {@link ConfigurationWriter writer} instances.
     * @param f factory that will be used to create writer instances.
     */
    public static void setConfigurationWriterFactory(ConfigurationWriterFactory f) {synchronized(writerLock) {writerFactory = f;}}

    /**
     * Returns the factory that is being used to create {@link ConfigurationWriter writer} instances.
     * @return the factory that is being used to create writer instances.
     */
    public static ConfigurationWriterFactory getConfigurationWriterFactory() {synchronized(writerLock) {return writerFactory;}}

    /**
     * Returns an instance of the class that will be used to write configuration data.
     * <p>
     * By default, this method will return an instance of {@link XmlConfigurationWriter}. However, this can be
     * modified by {@link #setConfigurationWriterFactory(ConfigurationWriterFactory)}.
     * </p>
     * @return an instance of the class that will be used to read configuration data.
     */
    public static ConfigurationWriter getConfigurationWriter() {
        ConfigurationWriterFactory factory;

        // If no factory was set, return an XML configuration writer.
        if((factory = getConfigurationWriterFactory()) == null)
            return new XmlConfigurationWriter();

        return factory.getWriterInstance();
    }



    // - Configuration reading -------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Loads configuration from the specified input stream, using the specified configuration reader.
     * @param  in                           where to read the configuration from.
     * @param  reader                       reader that will be used to interpret the content of <code>in</code>.
     * @throws IOException                  if an I/O error occurs.
     * @throws ConfigurationException       if a configuration error occurs.
     * @throws ConfigurationFormatException if there is an error in the format of the configuration file.
     * @see                                 #writeConfiguration(OutputStream,ConfigurationWriter)
     */
    public static synchronized void readConfiguration(InputStream in, ConfigurationReader reader) throws ConfigurationException, IOException, ConfigurationFormatException {
        reader.read(in, new ConfigurationLoader(root));
    }

    /**
     * Loads configuration from the specified input stream.
     * <p>
     * This method will use the configuration reader set by {@link #setConfigurationReaderFactory(ConfigurationReaderFactory)} if any,
     * or an {@link com.mucommander.conf.XmlConfigurationReader} instance if not.
     * </p>
     * @param  in                           where to read the configuration from.
     * @throws ConfigurationException       if a configuration error occurs.
     * @throws ConfigurationFormatException if there is an error in the format of the configuration file.
     * @throws IOException                  if an I/O error occurs.
     * @see                                 #writeConfiguration(OutputStream)
     */
    public static void readConfiguration(InputStream in) throws ConfigurationException, IOException, ConfigurationFormatException {
        readConfiguration(in, getConfigurationReader());
    }

    /**
     * Loads configuration using the specified configuration reader.
     * <p>
     * This method will use the input stream provided by {@link #setConfigurationSource(ConfigurationSource)} if any, or
     * fail otherwise.
     * </p>
     * @param  reader                       reader that will be used to interpret the content of <code>in</code>.
     * @throws IOException                  if an I/O error occurs.
     * @throws ConfigurationFormatException if there is an error in the format of the configuration file.
     * @throws ConfigurationException       if a configuration error occurs.
     * @see                                 #writeConfiguration(ConfigurationWriter)
     */
    public static void readConfiguration(ConfigurationReader reader) throws IOException, ConfigurationException, ConfigurationFormatException {
        InputStream in;

        in = null;
        try {readConfiguration(in = getConfigurationSource().getInputStream(), reader);}
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
        }
    }

    /**
     * Loads configuration.
     * <p>
     * If a reader has been specified through {@link #setConfigurationReaderFactory(ConfigurationReaderFactory)}, it
     * will be used to analyse the configuration. Otherwise, an {@link com.mucommander.conf.XmlConfigurationReader} instance
     * will be used.
     * </p>
     * <p>
     * If a configuration source has been specified through {@link #setConfigurationSource(ConfigurationSource)}, it will be
     * used. Otherwise, this method will fail.
     * </p>
     * @throws IOException                  if an I/O error occurs.
     * @throws ConfigurationFormatException if there is an error in the format of the configuration file.
     * @throws ConfigurationException       if a configuration error occurs.
     * @see                                 #writeConfiguration()
     */
    public static void readConfiguration() throws ConfigurationException, IOException, ConfigurationFormatException {
        readConfiguration(getConfigurationReader());
    }



    // - Configuration writing -------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Writes configuration to the specified output stream using the specified writer.
     * @param out                     where to write the configuration to.
     * @param writer                  writer that will be used to format the configuration.
     * @throws ConfigurationException if any error occurs.
     * @see                           #readConfiguration(InputStream,ConfigurationReader)
     */
    public static void writeConfiguration(OutputStream out, ConfigurationWriter writer) throws ConfigurationException {
        writer.setOutputStream(out);
        buildConfiguration(writer);
    }

    /**
     * Writes configuration to the specified output stream.
     * <p>
     * If a writer was specified through {@link #setConfigurationWriterFactory(ConfigurationWriterFactory)}, this will be
     * used to format the configuration. Otherwise, an {@link XmlConfigurationWriter} will be used.
     * </p>
     * @param out                     where to write the configuration to.
     * @throws ConfigurationException if any error occurs.
     * @see                           #readConfiguration(InputStream)
     */
    public static void writeConfiguration(OutputStream out) throws ConfigurationException {
        writeConfiguration(out, getConfigurationWriter());
    }

    /**
     * Writes configuration using the specified writer.
     * <p>
     * If a configuration source was specified through {@link #setConfigurationSource(ConfigurationSource)}, it will be used
     * to open an output stream. Otherwise, this method will fail.
     * </p>
     * @param writer                  writer that will be used to format the configuration.
     * @throws ConfigurationException if any error occurs.
     * @throws IOException            if any I/O error occurs.
     * @see                           #readConfiguration(ConfigurationReader)
     */
    public static void writeConfiguration(ConfigurationWriter writer) throws IOException, ConfigurationException {
        OutputStream out;

        out = null;
        try {writeConfiguration(out = getConfigurationSource().getOutputStream(), writer);}
        finally {
            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }
    }

    /**
     * Writes configuration.
     * <p>
     * If a writer was specified through {@link #setConfigurationWriterFactory(ConfigurationWriterFactory)}, this will be
     * used to format the configuration. Otherwise, an {@link XmlConfigurationWriter} will be used.
     * </p>
     * <p>
     * If a configuration source was specified through {@link #setConfigurationSource(ConfigurationSource)}, it will be used
     * to open an output stream. Otherwise, this method will fail.
     * </p>
     * @throws ConfigurationException if any error occurs.
     * @throws IOException            if any I/O error occurs.
     * @see                           #readConfiguration()
     */
    public static void writeConfiguration() throws IOException, ConfigurationException {
        writeConfiguration(getConfigurationWriter());
    }



    // - Configuration building ------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Recursively explores the specified section and sends messages to the specified builder.
     * @param  builder                object that will receive building events.
     * @param  root                   section to explore.
     * @throws ConfigurationException if any error occurs.
     */
    private static synchronized void buildConfiguration(ConfigurationBuilder builder, ConfigurationSection root) throws ConfigurationException {
        Enumeration          enumeration; // Enumeration on the section's variables, then subsections.
        String               name;        // Name of the current variable, then section.
        String               value;       // Value of the current variable.
        ConfigurationSection section;     // Current section.

        // Explores the section's variables.
        enumeration = root.variableNames();
        while(enumeration.hasMoreElements())
            builder.addVariable(name = (String)enumeration.nextElement(), root.getVariable(name));

        // Explores the section's subsections.
        enumeration = root.sectionNames();
        while(enumeration.hasMoreElements()) {
            name    = (String)enumeration.nextElement();
            section = root.getSection(name);

            // We only go through subsections if contain either variables or subsections of their own.
            if(section.hasSections() || section.hasVariables()) {
                builder.startSection(name);
                buildConfiguration(builder, section);
                builder.endSection(name);
            }
        }
    }

    /**
     * Explores the whole configuration tree and sends build messages to <code>builder</code>.
     * @param  builder                object that will receive configuration building messages.
     * @throws ConfigurationException if any error occurs while going through the configuration tree.
     */
    public static void buildConfiguration(ConfigurationBuilder builder) throws ConfigurationException {
        builder.startConfiguration();
        buildConfiguration(builder, root);
        builder.endConfiguration();
    }



    // - Variable setting ------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Moves the value of <code>fromVar</code> to <code>toVar</code>.
     * <p>
     * At the end of this call, <code>fromVar</code> will have been deleted. Note that if <code>fromVar</code> doesn't exist,
     * but <code>toVar</code> does, <code>toVar</code> will be deleted.
     * </p>
     * <p>
     * This method might trigger as many as two {@link ConfigurationEvent events}:
     * <ul>
     *  <li>One when <code>fromVar</code> is removed.</li>
     *  <li>One when <code>toVar</code> is set.</li>
     * </ul>
     * </p>
     * @param fromVar fully qualified name of the variable to rename.
     * @param toVar   fully qualified name of the variable that will receive <code>fromVar</code>'s value.
     */
    public static void renameVariable(String fromVar, String toVar) {setVariable(toVar, removeVariable(fromVar));}

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. This, however, is not a way
     * of indicating that the call failed: <code>false</code> is only ever returned if the previous value is equal to the
     * new value.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed to all
     * listeners.
     * </p>
     * @param  name  fully qualified name of the variable to set.
     * @param  value new value for the variable.
     * @return       <code>true</code> if this call resulted in a modification of the variable's value, <code>false</code> otherwise.
     */
    public static synchronized boolean setVariable(String name, String value) {
        ConfigurationExplorer explorer; // Used to navigate to the variable's parent section.
        String                buffer;   // Buffer for the variable's name trimmed of section information.

        // Moves to the parent section.
        buffer = moveToParent(explorer = new ConfigurationExplorer(root), name, true);

        // If the variable's value was actually modified, triggers an event.
        if(explorer.getSection().setVariable(buffer, value)) {
            ConfigurationEvent.triggerEvent(new ConfigurationEvent(name, value));
            return true;
        }
        return false;
    }

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. This, however, is not a way
     * of indicating that the call failed: <code>false</code> is only ever returned if the previous value is equal to the
     * new value.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed to all
     * listeners.
     * </p>
     * @param  name  fully qualified name of the variable to set.
     * @param  value new value for the variable.
     * @return       <code>true</code> if this call resulted in a modification of the variable's value, <code>false</code> otherwise.
     */
    public static boolean setVariable(String name, int value) {return setVariable(name, ConfigurationSection.getValue(value));}

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. This, however, is not a way
     * of indicating that the call failed: <code>false</code> is only ever returned if the previous value is equal to the
     * new value.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed to all
     * listeners.
     * </p>
     * @param  name  fully qualified name of the variable to set.
     * @param  value new value for the variable.
     * @return       <code>true</code> if this call resulted in a modification of the variable's value, <code>false</code> otherwise.
     */
    public static boolean setVariable(String name, float value) {return setVariable(name, ConfigurationSection.getValue(value));}

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. This, however, is not a way
     * of indicating that the call failed: <code>false</code> is only ever returned if the previous value is equal to the
     * new value.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed to all
     * listeners.
     * </p>
     * @param  name  fully qualified name of the variable to set.
     * @param  value new value for the variable.
     * @return       <code>true</code> if this call resulted in a modification of the variable's value, <code>false</code> otherwise.
     */
    public static boolean setVariable(String name, boolean value) {return setVariable(name, ConfigurationSection.getValue(value));}

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. This, however, is not a way
     * of indicating that the call failed: <code>false</code> is only ever returned if the previous value is equal to the
     * new value.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed to all
     * listeners.
     * </p>
     * @param  name  fully qualified name of the variable to set.
     * @param  value new value for the variable.
     * @return       <code>true</code> if this call resulted in a modification of the variable's value, <code>false</code> otherwise.
     */
    public static boolean setVariable(String name, long value) {return setVariable(name, ConfigurationSection.getValue(value));}

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. This, however, is not a way
     * of indicating that the call failed: <code>false</code> is only ever returned if the previous value is equal to the
     * new value.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed to all
     * listeners.
     * </p>
     * @param  name  fully qualified name of the variable to set.
     * @param  value new value for the variable.
     * @return       <code>true</code> if this call resulted in a modification of the variable's value, <code>false</code> otherwise.
     */
    public static boolean setVariable(String name, double value) {return setVariable(name, ConfigurationSection.getValue(value));}



    // - Variable retrieval ----------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the value of the specified variable.
     * @param  name fully qualified name of the variable whose value should be retrieved.
     * @return      the variable's value if set, <code>null</code> otherwise.
     */
    public static synchronized String getVariable(String name) {
        ConfigurationExplorer explorer; // Used to navigate to the variable's parent section.

        // If the variable's 'path' doesn't exist, return null.
        if((name = moveToParent(explorer = new ConfigurationExplorer(root), name, false)) == null)
            return null;
        return explorer.getSection().getVariable(name);
    }

    /**
     * Returns the value of the specified variable as an integer.
     * @param                        name fully qualified name of the variable whose value should be retrieved.
     * @return                       the variable's value if set, <code>0</code> otherwise.
     * @throws NumberFormatException if the variable's value cannot be cast to an integer.
     */
    public static int getIntegerVariable(String name) {return ConfigurationSection.getIntegerValue(getVariable(name));}

    /**
     * Returns the value of the specified variable as a long.
     * @param                        name fully qualified name of the variable whose value should be retrieved.
     * @return                       the variable's value if set, <code>0</code> otherwise.
     * @throws NumberFormatException if the variable's value cannot be cast to a long.
     */
    public static long getLongVariable(String name) {return ConfigurationSection.getLongValue(getVariable(name));}

    /**
     * Returns the value of the specified variable as a float.
     * @param                        name fully qualified name of the variable whose value should be retrieved.
     * @return                       the variable's value if set, <code>0</code> otherwise.
     * @throws NumberFormatException if the variable's value cannot be cast to a float.
     */
    public static float getFloatVariable(String name) {return ConfigurationSection.getFloatValue(getVariable(name));}

    /**
     * Returns the value of the specified variable as a double.
     * @param                        name fully qualified name of the variable whose value should be retrieved.
     * @return                       the variable's value if set, <code>0</code> otherwise.
     * @throws NumberFormatException if the variable's value cannot be cast to a double.
     */
    public static double getDoubleVariable(String name) {return ConfigurationSection.getDoubleValue(getVariable(name));}

    /**
     * Returns the value of the specified variable as a boolean.
     * @param  name fully qualified name of the variable whose value should be retrieved.
     * @return the variable's value if set, <code>false</code> otherwise.
     */
    public static boolean getBooleanVariable(String name) {return ConfigurationSection.getBooleanValue(getVariable(name));}

    /**
     * Checks whether the specified variable has been set.
     * @param  name fully qualified name of the variable to check for.
     * @return      <code>true</code> if the variable is set, <code>false</code> otherwise.
     */
    public static boolean isVariableSet(String name) {return getVariable(name) != null;}



    // - Variable removal ------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered listeners.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>null</code> if it wasn't set.
     */
    public static synchronized String removeVariable(String name) {
        ConfigurationExplorer explorer; // Used to navigate to the variable's parent section.
        String                buffer;   // Buffer for the variable's name trimmed of section information.

        // If the variable's 'path' doesn't exist, return null.
        if((buffer = moveToParent(explorer = new ConfigurationExplorer(root), name, false)) == null)
            return null;

        // If the variable was actually set, triggers an event.
        if((buffer = explorer.getSection().removeVariable(buffer)) != null)
            ConfigurationEvent.triggerEvent(new ConfigurationEvent(name, null));

        return buffer;
    }

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered listeners.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>0</code> if it wasn't set.
     */
    public static int removeIntegerVariable(String name) {return ConfigurationSection.getIntegerValue(removeVariable(name));}

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered listeners.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>0</code> if it wasn't set.
     */
    public static long removeLongVariable(String name) {return ConfigurationSection.getLongValue(removeVariable(name));}

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered listeners.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>0</code> if it wasn't set.
     */
    public static float removeFloatVariable(String name) {return ConfigurationSection.getFloatValue(removeVariable(name));}

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered listeners.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>0</code> if it wasn't set.
     */
    public static double removeDoubleVariable(String name) {return ConfigurationSection.getDoubleValue(removeVariable(name));}

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered listeners.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>false</code> if it wasn't set.
     */
    public static boolean removeBooleanVariable(String name) {return ConfigurationSection.getBooleanValue(removeVariable(name));}



    // - Advanced variable retrieval -------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Retrieves the value of the specified variable.
     * <p>
     * If the variable isn't set, this method will set it to <code>defaultValue</code> before
     * returning it. If this happens, a configuration {@link ConfigurationEvent event} will
     * be sent to all registered listeners.
     * </p>
     * @param  name         name of the variable to retrieve.
     * @param  defaultValue value to use if <code>name</code> is not set.
     * @return              the specified variable's value.
     */
    public static synchronized String getVariable(String name, String defaultValue) {
        ConfigurationExplorer explorer; // Used to navigate to the variable's parent section.
        String                value;    // Buffer for the variable's value.
        String                buffer;   // Buffer for the variable's name trimmed of section information.

        // Navigates to the parent section. We do not have to check for null values here,
        // as the section will be created if it doesn't exist.
        buffer = moveToParent(explorer = new ConfigurationExplorer(root), name, true);

        // If the variable isn't set, set it to defaultValue and triggers an event.
        if((value = explorer.getSection().getVariable(buffer)) == null) {
            explorer.getSection().setVariable(buffer, defaultValue);
            ConfigurationEvent.triggerEvent(new ConfigurationEvent(name, defaultValue));
            return defaultValue;
        }
        return value;
    }

    /**
     * Retrieves the value of the specified variable as an integer.
     * <p>
     * If the variable isn't set, this method will set it to <code>defaultValue</code> before
     * returning it. If this happens, a configuration {@link ConfigurationEvent event} will
     * be sent to all registered listeners.
     * </p>
     * @param  name                  name of the variable to retrieve.
     * @param  defaultValue          value to use if <code>name</code> is not set.
     * @return                       the specified variable's value.
     * @throws NumberFormatException if the variable's value cannot be cast to an integer.
     */
    public static int getVariable(String name, int defaultValue) {
        return ConfigurationSection.getIntegerValue(getVariable(name, ConfigurationSection.getValue(defaultValue)));
    }

    /**
     * Retrieves the value of the specified variable as a long.
     * <p>
     * If the variable isn't set, this method will set it to <code>defaultValue</code> before
     * returning it. If this happens, a configuration {@link ConfigurationEvent event} will
     * be sent to all registered listeners.
     * </p>
     * @param  name                  name of the variable to retrieve.
     * @param  defaultValue          value to use if <code>name</code> is not set.
     * @return                       the specified variable's value.
     * @throws NumberFormatException if the variable's value cannot be cast to a long.
     */
    public static long getVariable(String name, long defaultValue) {
        return ConfigurationSection.getLongValue(getVariable(name, ConfigurationSection.getValue(defaultValue)));
    }

    /**
     * Retrieves the value of the specified variable as a float.
     * <p>
     * If the variable isn't set, this method will set it to <code>defaultValue</code> before
     * returning it. If this happens, a configuration {@link ConfigurationEvent event} will
     * be sent to all registered listeners.
     * </p>
     * @param  name                  name of the variable to retrieve.
     * @param  defaultValue          value to use if <code>name</code> is not set.
     * @return                       the specified variable's value.
     * @throws NumberFormatException if the variable's value cannot be cast to a float.
     */
    public static float getVariable(String name, float defaultValue) {
        return ConfigurationSection.getFloatValue(getVariable(name, ConfigurationSection.getValue(defaultValue)));
    }

    /**
     * Retrieves the value of the specified variable as a boolean.
     * <p>
     * If the variable isn't set, this method will set it to <code>defaultValue</code> before
     * returning it. If this happens, a configuration {@link ConfigurationEvent event} will
     * be sent to all registered listeners.
     * </p>
     * @param  name                  name of the variable to retrieve.
     * @param  defaultValue          value to use if <code>name</code> is not set.
     * @return                       the specified variable's value.
     */
    public static boolean getVariable(String name, boolean defaultValue) {
        return ConfigurationSection.getBooleanValue(getVariable(name, ConfigurationSection.getValue(defaultValue)));
    }

    /**
     * Retrieves the value of the specified variable as a double.
     * <p>
     * If the variable isn't set, this method will set it to <code>defaultValue</code> before
     * returning it. If this happens, a configuration {@link ConfigurationEvent event} will
     * be sent to all registered listeners.
     * </p>
     * @param  name                  name of the variable to retrieve.
     * @param  defaultValue          value to use if <code>name</code> is not set.
     * @return                       the specified variable's value.
     * @throws NumberFormatException if the variable's value cannot be cast to a double.
     */
    public static double getVariable(String name, double defaultValue) {
        return ConfigurationSection.getDoubleValue(getVariable(name, ConfigurationSection.getValue(defaultValue)));
    }



    // - Helper methods --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Navigates the specified explorer to the parent section of the specified variable.
     * @param  root where to start exploring from.
     * @param  name name of the variable to seek.
     * @param  create whether or not the path to the variable should be created if it doesn't exist.
     * @return the name of the variable trimmed of section information, <code>null</code> if not found.
     */
    private static String moveToParent(ConfigurationExplorer root, String name, boolean create) {
        StringTokenizer parser; // Used to parse the variable's path.

        // Goes through each element of the path.
        parser = new StringTokenizer(name, ".");
        while(parser.hasMoreTokens()) {
            // If we've reached the variable's name, return it.
            name = (String)parser.nextToken();
            if(!parser.hasMoreTokens())
                return name;

            // If we've reached a dead-end, return null.
            if(!root.moveTo(name, create))
                return null;
        }
        return name;
    }



    // - Configuration listening -----------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Adds the specified object to the list of registered configuration listeners.
     * @param listener object to register as a configuration listener.
     */
    public static void addConfigurationListener(ConfigurationListener listener) {ConfigurationEvent.addConfigurationListener(listener);}

    /**
     * Removes the specified object from the list of registered configuration listeners.
     * @param listener object to remove from the list of registered configuration listeners.
     */
    public static void removeConfigurationListener(ConfigurationListener listener) {ConfigurationEvent.removeConfigurationListener(listener);}
}
