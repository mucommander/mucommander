/**
 * This file is part of muCommander, http://www.mucommander.com
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

package com.mucommander.commons.conf;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Base class for all configuration related tasks.
 * <p>
 * A <code>Configuration</code> instance's main goal is to act as a configuration data repository.
 * Once created, it can be used to {@link #getVariable(String) retrieve}, {@link #removeVariable(String) delete}
 * and {@link #setVariable(String,String) set} configuration variables.
 * </p>
 * <p>
 * <h3>Naming conventions</h3>
 * Configuration variable names follow the same convention as Java System properties: a serie of strings
 * separated by periods. By convention, all but the last string are called configuration sections, while
 * the last one is the variable's name. When we refer to a variable's fully qualified name, we're talking
 * about the whole period-separated name.<br>
 * For example, <code>startup_folder.right.last_folder</code> is interpreted as a variable called
 * <code>last_folder</code> contained in a section called <code>right</code>, itself contained in
 * another section called <code>startup_folder</code>.<br>
 * </p>
 * <p>
 * <h3>Variable types</h3>
 * While the <code>com.mucommander.commons.conf</code> really only handles one type of variables, strings, it offers
 * tools to cast them as primitive Java types (int, long, float, double, boolean). This is done through the use
 * of the various primitive types' class implementation <code>parseXXX</code> method.<br>
 * When a variable hasn't been set but ant attempt is made to cast it, the standard Java default value will
 * be returned:
 * <ul>
 *   <li>String: <code>null</code></li>
 *   <li>Integer: <code>0</code></li>
 *   <li>Long: <code>0</code></li>
 *   <li>Float: <code>0</code></li>
 *   <li>Double: <code>0</code></li>
 *   <li>Boolean: <code>false</code></li>
 * </ul>
 * </p>
 * <p>
 * <h3>Configuration file format</h3>
 * By default, configuration data is assumed to be in the standard muCommander file format (described in
 * {@link XmlConfigurationReader}). However, application writers can modify that to any format they want
 * through the {@link #setReaderFactory(ConfigurationReaderFactory) setReaderFactory} and
 * {@link #setWriterFactory(ConfigurationWriterFactory) setWriterFactory} methods.
 * </p>
 * <p>
 * <h3>Configuration data location</h3>
 * While <code>Configuration</code> provides read and write methods that accept streams as parameters, it's
 * also possible to set the data source once and for all and let the API deal with the details. This can
 * be achieved through the {@link #setSource(ConfigurationSource) setSource} method.<br>
 * Note that a default implementation, {@link FileConfigurationSource}, is provided. It covers the most
 * common case of configuration sources, a local configuration file.<br>
 * For application writers who wish to be able to retrieve configuration files through a variety of file systems,
 * we suggest creating a source using the <code>com.mucommander.file</code> API.
 * </p>
 * <p>
 * <h3>Monitoring configuration changes</h3>
 * Classes that need to monitor the state of the configuration in order, for example, to react to changes
 * dynamically rather than wait for an application reboot can implement the {@link ConfigurationListener}
 * interface and register themselves through
 * {@link #addConfigurationListener(ConfigurationListener) addConfigurationListener}. This guarantees that they
 * will receive configuration events whenever a modification occurs.<br>
 * Note that LISTENERS are stored as weak references, meaning that application writers must ensure that they keep
 * direct references to the listener instances they register if they do not want them to be garbaged collected
 * out of existence randomly.
 * </p>
 * @author Nicolas Rinaudo
 */
public class Configuration {
    // - Class variables -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Used to get access to the configuration source's input and output streams. */
    private ConfigurationSource                                source;
    /** Used to create objects that will read from the configuration source. */
    private ConfigurationReaderFactory                         readerFactory;
    /** Used to create objects that will write to the configuration source. */
    private ConfigurationWriterFactory                         writerFactory;
    /** Holds the content of the configuration file. */
    private final ConfigurationSection                         root = new ConfigurationSection();
    /** Contains all registered configuration LISTENERS, stored as weak references. */
    private final WeakHashMap<ConfigurationListener, ?> LISTENERS = new WeakHashMap<ConfigurationListener, Object>();



    // - Synchronisation locks -----------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Used to synchronise concurent access of the configuration source. */
    private final Object sourceLock = new Object();
    /** Used to synchronise concurent access of the reader factory. */
    private final Object readerLock = new Object();
    /** Used to synchronise concurent access of the writer factory. */
    private final Object writerLock = new Object();



    // - Initialisation ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    
    /**
     * Creates a new instance of <code>Configuration</code>.
     * <p>
     * The resulting instance will use default {@link XmlConfigurationReader readers} and
     * {@link XmlConfigurationWriter writers}.
     * </p>
     * <p>
     * Note that until the {@link #setSource(ConfigurationSource) setSource} method has been
     * invoked, calls to read or write methods without a stream parameter will fail.
     * </p>
     */
    public Configuration() {
    }

    /**
     * Creates a new instance of <code>Configuration</code> using the specified source.
     * <p>
     * The resulting instance will use the default {@link XmlConfigurationReader readers} and
     * {@link XmlConfigurationWriter writers}.
     * </p>
     * @param source where the resulting instance will look for its configuration data.
     */
    public Configuration(ConfigurationSource source) {
        setSource(source);
    }

    /**
     * Creates a new instance of <code>Configuration</code> using the specified format.
     * <p>
     * Note that until the {@link #setSource(ConfigurationSource) setSource} method has been
     * invoked, calls to read or write methods without a stream parameter will fail.
     * </p>
     * @param reader factory for configuration readers.
     * @param writer factory for configuration writers.
     */
    public Configuration(ConfigurationReaderFactory reader, ConfigurationWriterFactory writer) {
        setReaderFactory(reader);
        setWriterFactory(writer);
    }

    /**
     * Creates a new instance of <code>Configuration</code> using the specified source and format.
     * @param source where the resulting instance will look for its configuration data.
     * @param reader factory for configuration readers.
     * @param writer factory for configuration writers.
     */
    public Configuration(ConfigurationSource source, ConfigurationReaderFactory reader, ConfigurationWriterFactory writer) {
        setSource(source);
        setReaderFactory(reader);
        setWriterFactory(writer);
    }



    // - Configuration source ------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Sets the source that will be used to read and write configuration information.
     * @param s new configuration source.
     * @see     #getSource()
     */
    public void setSource(ConfigurationSource s) {
        synchronized(sourceLock) {source = s;}
    }

    /**
     * Returns the current configuration source.
     * @return the current configuration source, or <code>null</code> if it hasn't been set.
     * @see    #setSource(ConfigurationSource)
     */
    public ConfigurationSource getSource() {
        synchronized(sourceLock) {return source;}
    }



    // - Reader handling -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Sets the factory that will be used to create {@link ConfigurationReader reader} instances.
     * <p>
     * In order to reset the configuration to its default reader factory, application writers can call this method
     * with a <code>null</code> parameter.
     * </p>
     * @param f factory that will be used to create reader instances.
     * @see     #getReaderFactory()
     */
    public void setReaderFactory(ConfigurationReaderFactory f) {
        synchronized(readerLock) {readerFactory = f;}
    }

    /**
     * Returns the factory that is being used to create {@link ConfigurationReader reader} instances.
     * <p>
     * By default, this method will return an {@link XmlConfigurationReader XML reader} factory.
     * This can be modified by calling {@link #setReaderFactory(ConfigurationReaderFactory) setReaderFactory}.
     * </p>
     * @return the factory that is being used to create reader instances.
     * @see    #setReaderFactory(ConfigurationReaderFactory)
     */
    public ConfigurationReaderFactory getReaderFactory() {
        synchronized(readerLock) {
            if (readerFactory == null)
                return XmlConfigurationReader.FACTORY;
            return readerFactory;
        }
    }



    // - Writer handling -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Sets the factory that will be used to create writer instances.
     * <p>
     * In order to reset the configuration to its default writer factory, application writers can call
     * this method will a <code>null</code> parameter.
     * </p>
     * @param f factory that will be used to create writer instances.
     * @see     #getWriterFactory()
     */
    public void setWriterFactory(ConfigurationWriterFactory f) {
        synchronized(writerLock) {writerFactory = f;}
    }

    /**
     * Returns the factory that is being used to create writer instances.
     * <p>
     * By default, this method will return an {@link XmlConfigurationWriter} factory. However, this
     * can be modified by calling {@link #setWriterFactory(ConfigurationWriterFactory) setWriterFactory}.
     * </p>
     * @return the factory that is being used to create writer instances.
     * @see    #setWriterFactory(ConfigurationWriterFactory)
     */
    public ConfigurationWriterFactory getWriterFactory() {
        synchronized(writerLock) {
            if (writerFactory == null)
                return XmlConfigurationWriter.FACTORY;
            return writerFactory;
        }
    }



    // - Configuration reading -----------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Loads configuration from the specified input stream, using the specified configuration reader.
     * @param  in                              where to read the configuration from.
     * @param  reader                          reader that will be used to interpret the content of <code>in</code>.
     * @throws IOException                     if an I/O error occurs.
     * @throws ConfigurationException          if a configuration error occurs.
     * @throws ConfigurationFormatException    if a syntax error occurs in the configuration data.
     * @throws ConfigurationStructureException if the configuration data doesn't describe a valid configuration tree.
     * @see                                    #read(InputStream)
     * @see                                    #read(ConfigurationReader)
     * @see                                    #read()
     */
    synchronized void read(Reader in, ConfigurationReader reader) throws IOException, ConfigurationException {
        reader.read(in, new ConfigurationLoader(root));
    }

    /**
     * Loads configuration from the specified input stream.
     * <p>
     * This method will use the configuration reader set by {@link #setReaderFactory(ConfigurationReaderFactory)} if any,
     * or an {@link XmlConfigurationReader} instance if not.
     * </p>
     * @param  in                              where to read the configuration from.
     * @throws ConfigurationException          if a configuration error occurs.
     * @throws ConfigurationFormatException    if a syntax error occurs in the configuration data.
     * @throws ConfigurationStructureException if the configuration data doesn't describe a valid configuration tree.
     * @throws ReaderConfigurationException    if the {@link ConfigurationReaderFactory} isn't properly configured.
     * @throws IOException                     if an I/O error occurs.
     * @see                                    #read()
     * @see                                    #read(ConfigurationReader)
     * @see                                    #read(Reader,ConfigurationReader)
     * @deprecated Application developers should use {@link #read(Reader)} instead. This method assumes the specified
     *             {@link InputStream} to be <code>UTF-8</code> encoded.
     */
    @Deprecated
    public void read(InputStream in) throws ConfigurationException, IOException {
        read(new InputStreamReader(in, Charset.forName("utf-8")), getReaderFactory().getReaderInstance());
    }

    /**
     * Loads configuration from the specified reader.
     * <p>
     * This method will use the configuration reader set by {@link #setReaderFactory(ConfigurationReaderFactory)} if any,
     * or an {@link XmlConfigurationReader} instance if not.
     * </p>
     * @param  in                              where to read the configuration from.
     * @throws ConfigurationException          if a configuration error occurs.
     * @throws ConfigurationFormatException    if a syntax error occurs in the configuration data.
     * @throws ConfigurationStructureException if the configuration data doesn't describe a valid configuration tree.
     * @throws ReaderConfigurationException    if the {@link ConfigurationReaderFactory} isn't properly configured.
     * @throws IOException                     if an I/O error occurs.
     * @see                                    #read()
     * @see                                    #read(ConfigurationReader)
     * @see                                    #read(Reader,ConfigurationReader)
     */
    public void read(Reader in) throws ConfigurationException, IOException {
        read(in, getReaderFactory().getReaderInstance());
    }

    /**
     * Loads configuration using the specified configuration reader.
     * <p>
     * This method will use the input stream provided by {@link #setSource(ConfigurationSource)} if any, or
     * fail otherwise.
     * </p>
     * @param  reader                          reader that will be used to interpret the content of <code>in</code>.
     * @throws IOException                     if an I/O error occurs.
     * @throws ConfigurationException          if a configuration error occurs.
     * @throws SourceConfigurationException    if no {@link ConfigurationSource} has been set.
     * @throws ConfigurationFormatException    if a syntax error occurs in the configuration data.
     * @throws ConfigurationStructureException if the configuration data doesn't describe a valid configuration tree.
     * @see                                    #read(InputStream)
     * @see                                    #read()
     * @see                                    #read(Reader,ConfigurationReader)
     */
    public void read(ConfigurationReader reader) throws IOException, ConfigurationException {
        Reader in = null; // Input stream on the configuration source.

        ConfigurationSource source = getSource();
        // Makes sure the configuration source has been properly set.
        if (source == null)
            throw new SourceConfigurationException("Configuration source hasn't been set.");

        // Reads the configuration data.
        try {read(in = source.getReader(), reader);}
        finally {
            if (in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
        }
    }

    /**
     * Loads configuration.
     * <p>
     * If a reader has been specified through {@link #setReaderFactory(ConfigurationReaderFactory)}, it
     * will be used to analyse the configuration. Otherwise, an {@link XmlConfigurationReader}
     * instance will be used.
     * </p>
     * <p>
     * If a configuration source has been specified through {@link #setSource(ConfigurationSource)}, it will be
     * used. Otherwise, this method will fail.
     * </p>
     * @throws IOException                     if an I/O error occurs.
     * @throws ConfigurationException          if a configuration error occurs.
     * @throws SourceConfigurationException    if no {@link ConfigurationSource} hasn't been set.
     * @throws ConfigurationFormatException    if a syntax error occurs in the configuration data.
     * @throws ConfigurationStructureException if the configuration data doesn't describe a valid configuration tree.
     * @throws ReaderConfigurationException    if the {@link ConfigurationReaderFactory} isn't properly configured.
     * @see                                    #write()
     * @see                                    #read(InputStream)
     * @see                                    #read(ConfigurationReader)
     * @see                                    #read(Reader,ConfigurationReader)
     */
    public void read() throws ConfigurationException, IOException {
        read(getReaderFactory().getReaderInstance());
    }



    // - Configuration writing -----------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Writes the configuration to the specified {@link Writer}.
     * <p>
     * This method will use {@link #getWriterFactory()} to create instances of configuration writer.
     * </p>
     * @param out                              where to write the configuration to.
     * @throws ConfigurationException          if any error occurs.
     * @throws ConfigurationFormatException    if a syntax error occurs in the configuration data.
     * @throws ConfigurationStructureException if the configuration data doesn't describe a valid configuration tree.
     * @throws WriterConfigurationException    if the {@link ConfigurationWriterFactory} isn't properly configured.
     * @see                                    #read(InputStream)
     * @see                                    #write()
     */
    public void write(Writer out) throws ConfigurationException {
        write(getWriterFactory().getWriterInstance(out));
    }

    /**
     * Writes the configuration.
     * <p>
     * If a configuration source was specified through {@link #setSource(ConfigurationSource)}, it will be used
     * to open an output stream. Otherwise, this method will fail.
     * </p>
     * @throws ConfigurationException          if any error occurs.
     * @throws SourceConfigurationException    if no {@link ConfigurationSource} has been set.
     * @throws ConfigurationFormatException    if a syntax error occurs in the configuration data.
     * @throws ConfigurationStructureException if the configuration data doesn't describe a valid configuration tree.
     * @throws IOException                     if any I/O error occurs.
     * @see                                    #read(ConfigurationReader)
     * @see                                    #write()
     */
    public void write() throws IOException, ConfigurationException {
        Writer out = null; // Where to write the configuration data.

        ConfigurationSource source = getSource();
        // Makes sure the source has been set.
        if (source == null)
            throw new SourceConfigurationException("No configuration source has been set");

        // Writes the configuration data.
        try {write(out = source.getWriter());}
        finally {
            if (out != null) {
                try {out.close();}
                catch(Exception e) {
                    // Ignores errors here, nothing we can do about them.
                }
            }
        }
    }

    /**
     * Writes the configuration data to the specified builder.
     * @param  builder                object that will receive configuration building messages.
     * @throws ConfigurationException if any error occurs while going through the configuration tree.
     */
    public void write(ConfigurationBuilder builder) throws ConfigurationException {
        builder.startConfiguration();
        build(builder, root);
        builder.endConfiguration();
    }



    // - Configuration building ----------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Recursively explores the specified section and invokes the specified builder's callback methods.
     * @param  builder                object that will receive building events.
     * @param  root                   section to explore.
     * @throws ConfigurationException if any error occurs.
     */
    private synchronized void build(ConfigurationBuilder builder, ConfigurationSection root) throws ConfigurationException {
        String name; // Name of the current variable, then section.

        // Explores the section's variables.
        Iterator<String> enumeration = root.variableNames();
        while (enumeration.hasNext())
            builder.addVariable(name = enumeration.next(), root.getVariable(name));

        // Explores the section's subsections.
        enumeration = root.sectionNames();
        while (enumeration.hasNext()) {
            name = enumeration.next();
            ConfigurationSection section = root.getSection(name);

            // We only go through subsections if contain either variables or subsections of their own.
            if (section.hasSections() || section.hasVariables()) {
                builder.startSection(name);
                build(builder, section);
                builder.endSection(name);
            }
        }
    }



    // - Variable setting ----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Moves the value of <code>fromVar</code> to <code>toVar</code>.
     * <p>
     * At the end of this call, <code>fromVar</code> will have been deleted. Note that if <code>fromVar</code> doesn't
     * exist, but <code>toVar</code> does, <code>toVar</code> will be deleted.
     * </p>
     * <p>
     * This method might trigger as many as two {@link ConfigurationEvent events}:
     * <ul>
     *  <li>One when <code>fromVar</code> is removed.</li>
     *  <li>One when <code>toVar</code> is set.</li>
     * </ul>
     * The removal event will always be triggered first.
     * </p>
     * @param fromVar fully qualified name of the variable to rename.
     * @param toVar   fully qualified name of the variable that will receive <code>fromVar</code>'s value.
     */
    public void renameVariable(String fromVar, String toVar) {
        String val = removeVariable(fromVar);
        if (val != null)
            setVariable(toVar, val);
    }

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. Note that this doesn't
     * mean the call failed, but that <code>name</code>'s value was already equal to <code>value</code>.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed
     * to all LISTENERS.
     * </p>
     * @param  name  fully qualified name of the variable to set.
     * @param  value new value for the variable.
     * @return       <code>true</code> if this call resulted in a modification of the variable's value,
     *               <code>false</code> otherwise.
     * @see          #getVariable(String)
     * @see          #getVariable(String,String)
     */
    public synchronized boolean setVariable(String name, String value) {
        ConfigurationExplorer explorer = new ConfigurationExplorer(root);

        // Moves to the parent section.
        String buffer = moveToParent(explorer, name, true);

        // If the variable's value was actually modified, triggers an event.
        if (explorer.getSection().setVariable(buffer, value)) {
            triggerEvent(new ConfigurationEvent(this, name, value));
            return true;
        }
        return false;
    }

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. This, however, is not a
     * way of indicating that the call failed: <code>false</code> is only ever returned if the previous value is equal
     * to the new value.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed
     * to all LISTENERS.
     * </p>
     * @param  name  fully qualified name of the variable to set.
     * @param  value new value for the variable.
     * @return       <code>true</code> if this call resulted in a modification of the variable's value,
     *               <code>false</code> otherwise.
     * @see          #getIntegerVariable(String)
     * @see          #getVariable(String,int)
     */
    public boolean setVariable(String name, int value) {
        return setVariable(name, ConfigurationSection.getValue(value));
    }

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. This, however, is not a
     * way of indicating that the call failed: <code>false</code> is only ever returned if the previous value is equal
     * to the new value.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed to all
     * LISTENERS.
     * </p>
     * @param  name      fully qualified name of the variable to set.
     * @param  value     new value for the variable.
     * @param  separator string used to separate each element of the list.
     * @return           <code>true</code> if this call resulted in a modification of the variable's value,
     *                   <code>false</code> otherwise.
     * @see              #getListVariable(String,String)
     * @see              #getVariable(String,List,String)
     */
    public boolean setVariable(String name, List<String> value, String separator) {
        return setVariable(name, ConfigurationSection.getValue(value, separator));
    }

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. This, however, is not
     * a way of indicating that the call failed: <code>false</code> is only ever returned if the previous value is equal
     * to the new value.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed
     * to all LISTENERS.
     * </p>
     * @param  name  fully qualified name of the variable to set.
     * @param  value new value for the variable.
     * @return       <code>true</code> if this call resulted in a modification of the variable's value,
     *               <code>false</code> otherwise.
     * @see          #getFloatVariable(String)
     * @see          #getVariable(String,float)
     */
    public boolean setVariable(String name, float value) {return setVariable(name, ConfigurationSection.getValue(value));}

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. This, however, is not a
     * way of indicating that the call failed: <code>false</code> is only ever returned if the previous value is equal
     * to the new value.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed
     * to all LISTENERS.
     * </p>
     * @param  name  fully qualified name of the variable to set.
     * @param  value new value for the variable.
     * @return       <code>true</code> if this call resulted in a modification of the variable's value,
     *               <code>false</code> otherwise.
     * @see          #getBooleanVariable(String)
     * @see          #getVariable(String,boolean)
     */
    public boolean setVariable(String name, boolean value) {
        return setVariable(name, ConfigurationSection.getValue(value));
    }

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. This, however, is not a
     * way of indicating that the call failed: <code>false</code> is only ever returned if the previous value is equal
     * to the new value.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed
     * to all LISTENERS.
     * </p>
     * @param  name  fully qualified name of the variable to set.
     * @param  value new value for the variable.
     * @return       <code>true</code> if this call resulted in a modification of the variable's value,
     *               <code>false</code> otherwise.
     * @see          #getLongVariable(String)
     * @see          #getVariable(String,long)
     */
    public boolean setVariable(String name, long value) {
        return setVariable(name, ConfigurationSection.getValue(value));
    }

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. This, however, is not a
     * way of indicating that the call failed: <code>false</code> is only ever returned if the previous value is equal
     * to the new value.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed
     * to all LISTENERS.
     * </p>
     * @param  name  fully qualified name of the variable to set.
     * @param  value new value for the variable.
     * @return       <code>true</code> if this call resulted in a modification of the variable's value,
     *               <code>false</code> otherwise.
     * @see          #getDoubleVariable(String)
     * @see          #getVariable(String,double)
     */
    public boolean setVariable(String name, double value) {return setVariable(name, ConfigurationSection.getValue(value));}



    // - Variable retrieval --------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the value of the specified variable.
     * @param  name fully qualified name of the variable whose value should be retrieved.
     * @return      the variable's value if set, <code>null</code> otherwise.
     * @see         #setVariable(String,String)
     * @see         #getVariable(String,String)
     */
    public synchronized String getVariable(String name) {
        ConfigurationExplorer explorer = new ConfigurationExplorer(root);

        // If the variable's 'path' doesn't exist, return null.
        if ((name = moveToParent(explorer, name, false)) == null)
            return null;
        return explorer.getSection().getVariable(name);
    }

    /**
     * Returns the value of the specified variable as a {@link ValueList}.
     * @param  name      fully qualified name of the variable whose value should be retrieved.
     * @param  separator character used to split the variable's value into a list.
     * @return           the variable's value if set, <code>null</code> otherwise.
     * @see              #setVariable(String,List,String)
     * @see              #getVariable(String,List,String)
     */
    public ValueList getListVariable(String name, String separator) {
        return ConfigurationSection.getListValue(getVariable(name), separator);
    }

    /**
     * Returns the value of the specified variable as an integer.
     * @param                        name fully qualified name of the variable whose value should be retrieved.
     * @return                       the variable's value if set, <code>0</code> otherwise.
     * @throws NumberFormatException if the variable's value cannot be cast to an integer.
     * @see                          #setVariable(String,int)
     * @see                          #getVariable(String,int)
     */
    public int getIntegerVariable(String name) {
        return ConfigurationSection.getIntegerValue(getVariable(name));
    }

    /**
     * Returns the value of the specified variable as a long.
     * @param                        name fully qualified name of the variable whose value should be retrieved.
     * @return                       the variable's value if set, <code>0</code> otherwise.
     * @throws NumberFormatException if the variable's value cannot be cast to a long.
     * @see                          #setVariable(String,long)
     * @see                          #getVariable(String,long)
     */
    public long getLongVariable(String name) {
        return ConfigurationSection.getLongValue(getVariable(name));
    }

    /**
     * Returns the value of the specified variable as a float.
     * @param                        name fully qualified name of the variable whose value should be retrieved.
     * @return                       the variable's value if set, <code>0</code> otherwise.
     * @throws NumberFormatException if the variable's value cannot be cast to a float.
     * @see                          #setVariable(String,float)
     * @see                          #getVariable(String,float)
     */
    public float getFloatVariable(String name) {
        return ConfigurationSection.getFloatValue(getVariable(name));
    }

    /**
     * Returns the value of the specified variable as a double.
     * @param                        name fully qualified name of the variable whose value should be retrieved.
     * @return                       the variable's value if set, <code>0</code> otherwise.
     * @throws NumberFormatException if the variable's value cannot be cast to a double.
     * @see                          #setVariable(String,double)
     * @see                          #getVariable(String,double)
     */
    public double getDoubleVariable(String name) {
        return ConfigurationSection.getDoubleValue(getVariable(name));
    }

    /**
     * Returns the value of the specified variable as a boolean.
     * @param  name fully qualified name of the variable whose value should be retrieved.
     * @return the variable's value if set, <code>false</code> otherwise.
     * @see                          #setVariable(String,boolean)
     * @see                          #getVariable(String,boolean)
     */
    public boolean getBooleanVariable(String name) {
        return ConfigurationSection.getBooleanValue(getVariable(name));
    }

    /**
     * Checks whether the specified variable has been set.
     * @param  name fully qualified name of the variable to check for.
     * @return      <code>true</code> if the variable is set, <code>false</code> otherwise.
     */
    public boolean isVariableSet(String name) {
        return getVariable(name) != null;
    }



    // - Variable removal ----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Prunes dead branches from the specified configuration tree.
     * @param explorer used to backtrack through the configuration tree.
     */
    private void prune(BufferedConfigurationExplorer explorer) {
        // If we're at the root level, nothing to prune.
        if (!explorer.hasSections())
            return;

        ConfigurationSection current = explorer.popSection();

        // Look for branches to prune until we've either found a non-empty one
        // or reached the root of the three.
        while (current.isEmpty() && current != root) {
            // Gets the current section's parent and prune.
            ConfigurationSection parent = explorer.hasSections() ? explorer.popSection() : root;
            parent.removeSection(current);
            current = parent;
        }
    }

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered LISTENERS.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>null</code> if it wasn't set.
     */
    public synchronized String removeVariable(String name) {
        BufferedConfigurationExplorer explorer = new BufferedConfigurationExplorer(root);
        String buffer = moveToParent(explorer , name, false);

        // If the variable's 'path' doesn't exist, return null.
        if (buffer == null)
            return null;

        // If the variable was actually set, triggers an event.
        if ((buffer = explorer.getSection().removeVariable(buffer)) != null) {
            prune(explorer);
            triggerEvent(new ConfigurationEvent(this, name, null));
        }

        return buffer;
    }

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered LISTENERS.
     * </p>
     * @param  name name of the variable to remove.
     * @param  separator character used to split the variable's value into a list.
     * @return      the variable's old value, or <code>null</code> if it wasn't set.
     */
    public ValueList removeListVariable(String name, String separator) {
        return ConfigurationSection.getListValue(removeVariable(name), separator);
    }

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered LISTENERS.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>0</code> if it wasn't set.
     */
    public int removeIntegerVariable(String name) {
        return ConfigurationSection.getIntegerValue(removeVariable(name));
    }

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered LISTENERS.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>0</code> if it wasn't set.
     */
    public long removeLongVariable(String name) {
        return ConfigurationSection.getLongValue(removeVariable(name));
    }

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered LISTENERS.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>0</code> if it wasn't set.
     */
    public float removeFloatVariable(String name) {
        return ConfigurationSection.getFloatValue(removeVariable(name));
    }

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered LISTENERS.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>0</code> if it wasn't set.
     */
    public double removeDoubleVariable(String name) {
        return ConfigurationSection.getDoubleValue(removeVariable(name));
    }

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered LISTENERS.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>false</code> if it wasn't set.
     */
    public boolean removeBooleanVariable(String name) {
        return ConfigurationSection.getBooleanValue(removeVariable(name));
    }

    /**
     * Remove all variables & sub-sections under the root section 
     */
    public void clear() {
		root.clear();
	}


    // - Advanced variable retrieval -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Retrieves the value of the specified variable.
     * <p>
     * If the variable isn't set, this method will set it to <code>defaultValue</code> before
     * returning it. If this happens, a configuration {@link ConfigurationEvent event} will
     * be sent to all registered LISTENERS.
     * </p>
     * @param  name         name of the variable to retrieve.
     * @param  defaultValue value to use if <code>name</code> is not set.
     * @return              the specified variable's value.
     * @see                 #setVariable(String,String)
     * @see                 #getVariable(String)
     */
    public synchronized String getVariable(String name, String defaultValue) {
        // Used to navigate to the variable's parent section.
        ConfigurationExplorer explorer = new ConfigurationExplorer(root);

        // Navigates to the parent section. We do not have to check for null values here,
        // as the section will be created if it doesn't exist.
        String buffer = moveToParent(explorer, name, true);
        // Buffer for the variable's value.
        String value = explorer.getSection().getVariable(buffer);

        // If the variable isn't set, set it to defaultValue and triggers an event.
        if (value == null) {
            explorer.getSection().setVariable(buffer, defaultValue);
            triggerEvent(new ConfigurationEvent(this, name, defaultValue));
            return defaultValue;
        }
        return value;
    }

    /**
     * Retrieves the value of the specified variable as a {@link ValueList}.
     * <p>
     * If the variable isn't set, this method will set it to <code>defaultValue</code> before
     * returning it. If this happens, a configuration {@link ConfigurationEvent event} will
     * be sent to all registered LISTENERS.
     * </p>
     * @param  name         name of the variable to retrieve.
     * @param  defaultValue value to use if variable <code>name</code> is not set.
     * @param  separator    separator to use for <code>defaultValue</code> if variable <code>name</code> is not set.
     * @return              the specified variable's value.
     * @see                 #setVariable(String,List,String)
     * @see                 #getListVariable(String,String)
     */
    public ValueList getVariable(String name, List<String> defaultValue, String separator) {
        return ConfigurationSection.getListValue(getVariable(name,
                                                             ConfigurationSection.getValue(defaultValue, separator)),
                                                 separator);
    }

    /**
     * Retrieves the value of the specified variable as an integer.
     * <p>
     * If the variable isn't set, this method will set it to <code>defaultValue</code> before
     * returning it. If this happens, a configuration {@link ConfigurationEvent event} will
     * be sent to all registered LISTENERS.
     * </p>
     * @param  name                  name of the variable to retrieve.
     * @param  defaultValue          value to use if <code>name</code> is not set.
     * @return                       the specified variable's value.
     * @throws NumberFormatException if the variable's value cannot be cast to an integer.
     * @see                          #setVariable(String,int)
     * @see                          #getIntegerVariable(String)
     */
    public int getVariable(String name, int defaultValue) {
        return ConfigurationSection.getIntegerValue(getVariable(name, ConfigurationSection.getValue(defaultValue)));
    }

    /**
     * Retrieves the value of the specified variable as a long.
     * <p>
     * If the variable isn't set, this method will set it to <code>defaultValue</code> before
     * returning it. If this happens, a configuration {@link ConfigurationEvent event} will
     * be sent to all registered LISTENERS.
     * </p>
     * @param  name                  name of the variable to retrieve.
     * @param  defaultValue          value to use if <code>name</code> is not set.
     * @return                       the specified variable's value.
     * @throws NumberFormatException if the variable's value cannot be cast to a long.
     * @see                          #setVariable(String,long)
     * @see                          #getLongVariable(String)
     */
    public long getVariable(String name, long defaultValue) {
        return ConfigurationSection.getLongValue(getVariable(name, ConfigurationSection.getValue(defaultValue)));
    }

    /**
     * Retrieves the value of the specified variable as a float.
     * <p>
     * If the variable isn't set, this method will set it to <code>defaultValue</code> before
     * returning it. If this happens, a configuration {@link ConfigurationEvent event} will
     * be sent to all registered LISTENERS.
     * </p>
     * @param  name                  name of the variable to retrieve.
     * @param  defaultValue          value to use if <code>name</code> is not set.
     * @return                       the specified variable's value.
     * @throws NumberFormatException if the variable's value cannot be cast to a float.
     * @see                          #setVariable(String,float)
     * @see                          #getFloatVariable(String)
     */
    public float getVariable(String name, float defaultValue) {
        return ConfigurationSection.getFloatValue(getVariable(name, ConfigurationSection.getValue(defaultValue)));
    }

    /**
     * Retrieves the value of the specified variable as a boolean.
     * <p>
     * If the variable isn't set, this method will set it to <code>defaultValue</code> before
     * returning it. If this happens, a configuration {@link ConfigurationEvent event} will
     * be sent to all registered LISTENERS.
     * </p>
     * @param  name                  name of the variable to retrieve.
     * @param  defaultValue          value to use if <code>name</code> is not set.
     * @return                       the specified variable's value.
     * @see                          #setVariable(String,boolean)
     * @see                          #getBooleanVariable(String)
     */
    public boolean getVariable(String name, boolean defaultValue) {
        return ConfigurationSection.getBooleanValue(getVariable(name, ConfigurationSection.getValue(defaultValue)));
    }

    /**
     * Retrieves the value of the specified variable as a double.
     * <p>
     * If the variable isn't set, this method will set it to <code>defaultValue</code> before
     * returning it. If this happens, a configuration {@link ConfigurationEvent event} will
     * be sent to all registered LISTENERS.
     * </p>
     * @param  name                  name of the variable to retrieve.
     * @param  defaultValue          value to use if <code>name</code> is not set.
     * @return                       the specified variable's value.
     * @throws NumberFormatException if the variable's value cannot be cast to a double.
     * @see                          #setVariable(String,double)
     * @see                          #getDoubleVariable(String)
     */
    public double getVariable(String name, double defaultValue) {
        return ConfigurationSection.getDoubleValue(getVariable(name, ConfigurationSection.getValue(defaultValue)));
    }



    // - Helper methods ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Navigates the specified explorer to the parent section of the specified variable.
     * @param  root where to start exploring from.
     * @param  name name of the variable to seek.
     * @param  create whether or not the path to the variable should be created if it doesn't exist.
     * @return        the name of the variable trimmed of section information, <code>null</code> if not found.
     */
    private String moveToParent(ConfigurationExplorer root, String name, boolean create) {
        StringTokenizer parser; // Used to parse the variable's path.

        // Goes through each element of the path.
        parser = new StringTokenizer(name, ".");
        while(parser.hasMoreTokens()) {
            // If we've reached the variable's name, return it.
            name = parser.nextToken();
            if(!parser.hasMoreTokens())
                return name;

            // If we've reached a dead-end, return null.
            if(!root.moveTo(name, create))
                return null;
        }
        return name;
    }



    // - Configuration listening ---------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Adds the specified object to the list of registered configuration LISTENERS.
     * @param listener object to register as a configuration listener.
     * @see            #removeConfigurationListener(ConfigurationListener)
     */
    public void addConfigurationListener(ConfigurationListener listener) {
        LISTENERS.put(listener, null);}

    /**
     * Removes the specified object from the list of registered configuration LISTENERS.
     * @param listener object to remove from the list of registered configuration LISTENERS.
     * @see            #addConfigurationListener(ConfigurationListener)
     */
    public void removeConfigurationListener(ConfigurationListener listener) {
        LISTENERS.remove(listener);}

    /**
     * Passes the specified event to all registered configuration LISTENERS.
     * @param event event to propagate.
     */
    private void triggerEvent(ConfigurationEvent event) {
        for(ConfigurationListener listener : LISTENERS.keySet())
            listener.configurationChanged(event);
    }



    // - Misc. ---------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the configuration's root section.
     * @return the configuration's root section.
     */
    ConfigurationSection getRoot() {return root;}



    // - Loading -------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Used to load configuration.
     * @author Nicolas Rinaudo
     */
    private class ConfigurationLoader implements ConfigurationBuilder {
        // - Instance variables ----------------------------------------------------------------------------------------
        // -------------------------------------------------------------------------------------------------------------
        /** Parents of {@link #currentSection}. */
        private Stack<ConfigurationSection> sections;
        /** Fully qualified names of {@link #currentSection}. */
        private Stack<String>               sectionNames;
        /** Section that we're currently building. */
        private ConfigurationSection        currentSection;



        // - Initialisation --------------------------------------------------------------------------------------------
        // -------------------------------------------------------------------------------------------------------------
        /**
         * Creates a new configuration loader.
         * @param root where to create the configuration in.
         */
        public ConfigurationLoader(ConfigurationSection root) {currentSection = root;}



        // - Building --------------------------------------------------------------------------------------------------
        // -------------------------------------------------------------------------------------------------------------
        /**
         * Initialises the configuration building.
         */
        public void startConfiguration() {
            sections     = new Stack<ConfigurationSection>();
            sectionNames = new Stack<String>();
        }

        /**
         * Ends the configuration building.
         * @throws ConfigurationException if not all opened sections have been closed.
         */
        public void endConfiguration() throws ConfigurationException {
            // Makes sure currentSection is the root section.
            if(!sections.empty())
                throw new ConfigurationStructureException("Not all sections have been closed.");
            sections     = null;
            sectionNames = null;
        }

        /**
         * Creates a new sub-section to the current section.
         * @param name name of the new section.
         */
        public void startSection(String name) throws ConfigurationException {
            ConfigurationSection buffer;

            buffer = currentSection.addSection(name);
            sections.push(currentSection);
            if(sectionNames.empty())
                sectionNames.push(name + '.');
            else
                sectionNames.push(sectionNames.peek() + name + '.');
            currentSection = buffer;
        }

        /**
         * Ends the current section.
         * @param  name                   name of the section that's being closed.
         * @throws ConfigurationException if we're not closing a legal section.
         */
        public void endSection(String name) throws ConfigurationException {
            ConfigurationSection buffer;

            // Makes sure there is a section to close.
            try {
                buffer = sections.pop();
                sectionNames.pop();
            }
            catch(EmptyStackException e) {throw new ConfigurationStructureException("Section " + name + " was already closed.");}

            // Makes sure we're closing the right section.
            if(buffer.getSection(name) != currentSection)
                throw new ConfigurationStructureException("Section " + name + " is not the currently opened section.");
            currentSection = buffer;
        }

        /**
         * Adds the specified variable to the current section.
         * @param name  name of the variable.
         * @param value value of the variable.
         */
        public void addVariable(String name, String value) {
            // If the variable's value was modified, trigger an event.
            if(currentSection.setVariable(name, value)) {
                if(sectionNames.empty())
                    triggerEvent(new ConfigurationEvent(Configuration.this, name, value));
                else
                    triggerEvent(new ConfigurationEvent(Configuration.this, sectionNames.peek() + name, value));
            }
        }
    }
}
