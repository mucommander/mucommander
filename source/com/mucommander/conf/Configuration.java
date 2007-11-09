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
import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.Stack;
import java.util.EmptyStackException;

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
 * While the <code>com.mucommander.conf</code> really only handles one type of variables, strings, it offers
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
 * Note that listeners are stored as weak references, meaning that application writers must ensure that they keep
 * direct references to the listener instances they register if they do not want them to be garbaged collected
 * out of existence randomly.
 * </p>
 * @author Nicolas Rinaudo
 */
public class Configuration {
    // - Class variables -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Used to get access to the configuration source's input and output streams. */
    private ConfigurationSource        source;
    /** Used to create objects that will read from the configuration source. */
    private ConfigurationReaderFactory readerFactory;
    /** Used to create objects that will write to the configuration source. */
    private ConfigurationWriterFactory writerFactory;
    /** Holds the content of the configuration file. */
    private ConfigurationSection       root = new ConfigurationSection();
    /** Contains all registered configuration listeners, stored as weak references */
    private static WeakHashMap         listeners = new WeakHashMap();



    // - Synchronisation locks -----------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Used to synchronise concurent access of the configuration source. */
    private Object sourceLock = new Object();
    /** Used to synchronise concurent access of the reader factory. */
    private Object readerLock = new Object();
    /** Used to synchronise concurent access of the writer factory. */
    private Object writerLock = new Object();



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new instance of <code>Configuration</code>.
     * <p>
     * The resulting instance will use the default reader and writer factories,
     * {@link XmlConfigurationReaderFactory} and {@link XmlConfigurationWriterFactory}.
     * </p>
     * <p>
     * Note that until the {@link #setSource(ConfigurationSource) setSource} method has been
     * invoked, calls to read or write methods without a stream parameter will fail.
     * </p>
     */
    public Configuration() {}

    /**
     * Creates a new instance of <code>Configuration</code> using the specified source.
     * <p>
     * The resulting instance will use the default reader and writer factories,
     * {@link XmlConfigurationReaderFactory} and {@link XmlConfigurationWriterFactory}.
     * </p>
     * @param source where the resulting instance will look for its configuration data.
     */
    public Configuration(ConfigurationSource source) {setSource(source);}

    /**
     * Creates a new instance of <code>Configuration</code> using the specified format.
     * <p>
     * Note that until the {@link #setSource(ConfigurationSource) setSource} method has been
     * invoked, calls to read or write methods without a stream parameter will fail.
     * </p>
     * @param reader object that will be used to create {@link ConfigurationReader} instances.
     * @param writer object that will be used to create {@link ConfigurationWriter} instances.
     */
    public Configuration(ConfigurationReaderFactory reader, ConfigurationWriterFactory writer) {
        setReaderFactory(reader);
        setWriterFactory(writer);
    }

    /**
     * Creates a new instance of <code>Configuration</code> using the specified source and format.
     * @param source where the resulting instance will look for its configuration data.
     * @param reader object that will be used to create {@link ConfigurationReader} instances.
     * @param writer object that will be used to create {@link ConfigurationWriter} instances.
     */
    public Configuration(ConfigurationSource source, ConfigurationReaderFactory reader, ConfigurationWriterFactory writer) {
        setSource(source);
        setReaderFactory(reader);
        setWriterFactory(writer);
    }



    // - Configuration source --------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Sets the source that will be used to read and write configuration information.
     * @param s new configuration source.
     */
    public void setSource(ConfigurationSource s) {synchronized(sourceLock) {source = s;}}

    /**
     * Returns the current configuration source.
     * @return the current configuration source, or <code>null</code> if it hasn't been set.
     */
    public ConfigurationSource getSource() {synchronized(sourceLock) {return source;}}



    // - Reader handling -------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Sets the factory that will be used to create {@link ConfigurationReader reader} instances.
     * <p>
     * In order to reset the configuration to its default reader factory ({@link XmlConfigurationReaderFactory}),
     * application writers can call <code>setReaderFactory(null);</code>.
     * </p>
     * @param f factory that will be used to create reader instances.
     * @see     #getReader()
     * @see     #getReaderFactory()
     */
    public void setReaderFactory(ConfigurationReaderFactory f) {synchronized(readerLock) {readerFactory = f;}}

    /**
     * Returns the factory that is being used to create {@link ConfigurationReader reader} instances.
     * <p>
     * By default, this method will return an instance of {@link XmlConfigurationReaderFactory}. However,
     * this can be modified by calling {@link #setReaderFactory(ConfigurationReaderFactory) setReaderFactory}.
     * </p>
     * @return the factory that is being used to create reader instances.
     * @see    #getReader()
     * @see    #setReaderFactory(ConfigurationReaderFactory)
     */
    public ConfigurationReaderFactory getReaderFactory() {
        synchronized(readerLock) {
            if(readerFactory == null)
                return new XmlConfigurationReaderFactory();
            return readerFactory;
        }
    }

    /**
     * Returns an instance of the class that will be used to read configuration data.
     * <p>
     * By default, this method will return an instance of {@link XmlConfigurationReader}. However, this can be
     * modified by calling {@link #setReaderFactory(ConfigurationReaderFactory) setReaderFactory}.
     * </p>
     * @return                              an instance of the class that will be used to read configuration data.
     * @throws ReaderConfigurationException any configuration reader error, possibly wrapping another exception.
     * @see                                 #setReaderFactory(ConfigurationReaderFactory)
     * @see                                 #getReaderFactory()
     */
    public ConfigurationReader getReader() throws ReaderConfigurationException {return getReaderFactory().getReaderInstance();}



    // - Writer handling -------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Sets the factory that will be used to create {@link ConfigurationWriter writer} instances.
     * <p>
     * In order to reset the configuration to its default writer factory ({@link XmlConfigurationWriterFactory}),
     * application writers can call <code>setWriterFactory(null);</code>.
     * </p>
     * @param f factory that will be used to create writer instances.
     * @see     #getWriterFactory()
     * @see    #getWriter()
     */
    public void setWriterFactory(ConfigurationWriterFactory f) {synchronized(writerLock) {writerFactory = f;}}

    /**
     * Returns the factory that is being used to create {@link ConfigurationWriter writer} instances.
     * <p>
     * By default, this method will return an instance of {@link XmlConfigurationWriterFactory}. However, this
     * can be modified by calling {@link #setWriterFactory(ConfigurationWriterFactory) setWriterFactory}.
     * </p>
     * @return the factory that is being used to create writer instances.
     * @see    #setWriterFactory(ConfigurationWriterFactory)
     * @see    #getWriter()
     */
    public ConfigurationWriterFactory getWriterFactory() {
        synchronized(writerLock) {
            if(writerFactory == null)
                return new XmlConfigurationWriterFactory();
            return writerFactory;
        }
    }

    /**
     * Returns an instance of the class that will be used to write configuration data.
     * <p>
     * By default, this method will return an instance of {@link XmlConfigurationWriter}. However, this can be
     * modified by {@link #setWriterFactory(ConfigurationWriterFactory) setWriterFactory}.
     * </p>
     * @return                              an instance of the class that will be used to read configuration data.
     * @throws ReaderConfigurationException any configuration writer error, possibly wrapping another exception.
     * @see                                 #setWriterFactory(ConfigurationWriterFactory)
     * @see                                 #getWriterFactory()
     */
    public ConfigurationWriter getWriter() throws WriterConfigurationException {return getWriterFactory().getWriterInstance();}



    // - Configuration reading -------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Loads configuration from the specified input stream, using the specified configuration reader.
     * @param  in                              where to read the configuration from.
     * @param  reader                          reader that will be used to interpret the content of <code>in</code>.
     * @throws IOException                     if an I/O error occurs.
     * @throws ConfigurationException          if a configuration error occurs.
     * @throws ConfigurationFormatException    if a syntax error occurs in the configuration data.
     * @throws ConfigurationStructureException if the configuration data doesn't describe a valid configuration tree.
     * @see                                    #write(OutputStream,ConfigurationWriter)
     * @see                                    #read(InputStream)
     * @see                                    #read(ConfigurationReader)
     * @see                                    #read()
     */
    public synchronized void read(InputStream in, ConfigurationReader reader) throws IOException, ConfigurationException {reader.read(in, new ConfigurationLoader(root));}

    /**
     * Loads configuration from the specified input stream.
     * <p>
     * This method will use the configuration reader set by {@link #setReaderFactory(ConfigurationReaderFactory)} if any,
     * or an {@link com.mucommander.conf.XmlConfigurationReader} instance if not.
     * </p>
     * @param  in                              where to read the configuration from.
     * @throws ConfigurationException          if a configuration error occurs.
     * @throws ConfigurationFormatException    if a syntax error occurs in the configuration data.
     * @throws ConfigurationStructureException if the configuration data doesn't describe a valid configuration tree.
     * @throws ReaderConfigurationException    if the {@link ConfigurationReaderFactory} isn't properly configured.
     * @throws IOException                     if an I/O error occurs.
     * @see                                    #write(OutputStream)
     * @see                                    #read()
     * @see                                    #read(ConfigurationReader)
     * @see                                    #read(InputStream,ConfigurationReader)
     */
    public void read(InputStream in) throws ConfigurationException, IOException {read(in, getReader());}

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
     * @see                                    #write(ConfigurationWriter)
     * @see                                    #read(InputStream)
     * @see                                    #read()
     * @see                                    #read(InputStream,ConfigurationReader)
     */
    public void read(ConfigurationReader reader) throws IOException, ConfigurationException {
        InputStream         in;     // Input stream on the configuration source.
        ConfigurationSource source; // Configuration source.

        in = null;

        // Makes sure the configuration source has been properly set.
        if((source = getSource()) == null)
            throw new SourceConfigurationException("Configuration source hasn't been set.");

        // Reads the configuration data.
        try {read(in = source.getInputStream(), reader);}
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
     * If a reader has been specified through {@link #setReaderFactory(ConfigurationReaderFactory)}, it
     * will be used to analyse the configuration. Otherwise, an {@link com.mucommander.conf.XmlConfigurationReader} instance
     * will be used.
     * </p>
     * <p>
     * If a configuration source has been specified through {@link #setSource(ConfigurationSource)}, it will be
     * used. Otherwise, this method will fail.
     * </p>
     * @throws IOException                     if an I/O error occurs.
     * @throws ConfigurationException          if a configuration error occurs.
     * @throws SourceConfigurationException    if no {@link ConfigurationSource} hasn been set.
     * @throws ConfigurationFormatException    if a syntax error occurs in the configuration data.
     * @throws ConfigurationStructureException if the configuration data doesn't describe a valid configuration tree.
     * @throws ReaderConfigurationException    if the {@link ConfigurationReaderFactory} isn't properly configured.
     * @see                                    #write()
     * @see                                    #read(InputStream)
     * @see                                    #read(ConfigurationReader)
     * @see                                    #read(InputStream,ConfigurationReader)
     */
    public void read() throws ConfigurationException, IOException {read(getReader());}



    // - Configuration writing -------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Writes configuration to the specified output stream using the specified writer.
     * @param out                              where to write the configuration to.
     * @param writer                           writer that will be used to format the configuration.
     * @throws ConfigurationException          if any error occurs.
     * @throws ConfigurationFormatException    if a syntax error occurs in the configuration data.
     * @throws ConfigurationStructureException if the configuration data doesn't describe a valid configuration tree.
     * @see                                    #read(InputStream,ConfigurationReader)
     * @see                                    #write(OutputStream)
     * @see                                    #write(ConfigurationWriter)
     * @see                                    #write()
     */
    public void write(OutputStream out, ConfigurationWriter writer) throws ConfigurationException {
        writer.setOutputStream(out);
        build(writer);
    }

    /**
     * Writes configuration to the specified output stream.
     * <p>
     * If a writer was specified through {@link #setWriterFactory(ConfigurationWriterFactory)}, this will be
     * used to format the configuration. Otherwise, an {@link XmlConfigurationWriter} will be used.
     * </p>
     * @param out                              where to write the configuration to.
     * @throws ConfigurationException          if any error occurs.
     * @throws ConfigurationFormatException    if a syntax error occurs in the configuration data.
     * @throws ConfigurationStructureException if the configuration data doesn't describe a valid configuration tree.
     * @throws WriterConfigurationException    if the {@link ConfigurationWriterFactory} isn't properly configured.
     * @see                                    #read(InputStream)
     * @see                                    #write(OutputStream,ConfigurationWriter)
     * @see                                    #write(ConfigurationWriter)
     * @see                                    #write()
     */
    public void write(OutputStream out) throws ConfigurationException {write(out, getWriter());}

    /**
     * Writes configuration using the specified writer.
     * <p>
     * If a configuration source was specified through {@link #setSource(ConfigurationSource)}, it will be used
     * to open an output stream. Otherwise, this method will fail.
     * </p>
     * @param writer                           writer that will be used to format the configuration.
     * @throws ConfigurationException          if any error occurs.
     * @throws SourceConfigurationException    if no {@link ConfigurationSource} has been set.
     * @throws ConfigurationFormatException    if a syntax error occurs in the configuration data.
     * @throws ConfigurationStructureException if the configuration data doesn't describe a valid configuration tree.
     * @throws IOException                     if any I/O error occurs.
     * @see                                    #read(ConfigurationReader)
     * @see                                    #write(OutputStream,ConfigurationWriter)
     * @see                                    #write(OutputStream)
     * @see                                    #write()
     */
    public void write(ConfigurationWriter writer) throws IOException, ConfigurationException {
        OutputStream        out;    // Where to write the configuration data.
        ConfigurationSource source; // Configuration source.

        out = null;

        // Makes sure the source has been set.
        if((source = getSource()) == null)
            throw new SourceConfigurationException("No configuration source has been set");

        // Writes the configuration data.
        try {write(out = getSource().getOutputStream(), writer);}
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
     * If a writer was specified through {@link #setWriterFactory(ConfigurationWriterFactory)}, this will be
     * used to format the configuration. Otherwise, an {@link XmlConfigurationWriter} will be used.
     * </p>
     * <p>
     * If a configuration source was specified through {@link #setSource(ConfigurationSource)}, it will be used
     * to open an output stream. Otherwise, this method will fail.
     * </p>
     * @throws SourceConfigurationException    if a {@link ConfigurationSource} hasn't been set.
     * @throws ConfigurationException          if any error occurs.
     * @throws ConfigurationFormatException    if a syntax error occurs in the configuration data.
     * @throws ConfigurationStructureException if the configuration data doesn't describe a valid configuration tree.
     * @throws WriterConfigurationException    if the {@link ConfigurationWriterFactory} isn't properly configured.
     * @throws IOException                     if any I/O error occurs.
     * @see                                    #read()
     * @see                                    #write(OutputStream,ConfigurationWriter)
     * @see                                    #write(OutputStream)
     * @see                                    #write(ConfigurationWriter)
     */
    public void write() throws IOException, ConfigurationException {write(getWriter());}



    // - Configuration building ------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Recursively explores the specified section and invokes the specified builder's callback methods.
     * @param  builder                object that will receive building events.
     * @param  root                   section to explore.
     * @throws ConfigurationException if any error occurs.
     */
    private synchronized void build(ConfigurationBuilder builder, ConfigurationSection root) throws ConfigurationException {
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
                build(builder, section);
                builder.endSection(name);
            }
        }
    }

    /**
     * Explores the whole configuration tree and invokes the specified builder's callback methods.
     * @param  builder                object that will receive configuration building messages.
     * @throws ConfigurationException if any error occurs while going through the configuration tree.
     */
    public void build(ConfigurationBuilder builder) throws ConfigurationException {
        builder.startConfiguration();
        build(builder, root);
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
     * The removal event will always be triggered first.
     * </p>
     * @param fromVar fully qualified name of the variable to rename.
     * @param toVar   fully qualified name of the variable that will receive <code>fromVar</code>'s value.
     */
    public void renameVariable(String fromVar, String toVar) {setVariable(toVar, removeVariable(fromVar));}

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. Note that this doesn't mean
     * the call failed, but that <code>name</code>'s value was already equal to <code>value</code>.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed to all
     * listeners.
     * </p>
     * @param  name  fully qualified name of the variable to set.
     * @param  value new value for the variable.
     * @return       <code>true</code> if this call resulted in a modification of the variable's value, <code>false</code> otherwise.
     */
    public synchronized boolean setVariable(String name, String value) {
        ConfigurationExplorer explorer; // Used to navigate to the variable's parent section.
        String                buffer;   // Buffer for the variable's name trimmed of section information.

        // Moves to the parent section.
        buffer = moveToParent(explorer = new ConfigurationExplorer(root), name, true);

        // If the variable's value was actually modified, triggers an event.
        if(explorer.getSection().setVariable(buffer, value)) {
            triggerEvent(new ConfigurationEvent(this, name, value));
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
    public boolean setVariable(String name, int value) {return setVariable(name, ConfigurationSection.getValue(value));}

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
    public boolean setVariable(String name, float value) {return setVariable(name, ConfigurationSection.getValue(value));}

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
    public boolean setVariable(String name, boolean value) {return setVariable(name, ConfigurationSection.getValue(value));}

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
    public boolean setVariable(String name, long value) {return setVariable(name, ConfigurationSection.getValue(value));}

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
    public boolean setVariable(String name, double value) {return setVariable(name, ConfigurationSection.getValue(value));}



    // - Variable retrieval ----------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the value of the specified variable.
     * @param  name fully qualified name of the variable whose value should be retrieved.
     * @return      the variable's value if set, <code>null</code> otherwise.
     */
    public synchronized String getVariable(String name) {
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
    public int getIntegerVariable(String name) {return ConfigurationSection.getIntegerValue(getVariable(name));}

    /**
     * Returns the value of the specified variable as a long.
     * @param                        name fully qualified name of the variable whose value should be retrieved.
     * @return                       the variable's value if set, <code>0</code> otherwise.
     * @throws NumberFormatException if the variable's value cannot be cast to a long.
     */
    public long getLongVariable(String name) {return ConfigurationSection.getLongValue(getVariable(name));}

    /**
     * Returns the value of the specified variable as a float.
     * @param                        name fully qualified name of the variable whose value should be retrieved.
     * @return                       the variable's value if set, <code>0</code> otherwise.
     * @throws NumberFormatException if the variable's value cannot be cast to a float.
     */
    public float getFloatVariable(String name) {return ConfigurationSection.getFloatValue(getVariable(name));}

    /**
     * Returns the value of the specified variable as a double.
     * @param                        name fully qualified name of the variable whose value should be retrieved.
     * @return                       the variable's value if set, <code>0</code> otherwise.
     * @throws NumberFormatException if the variable's value cannot be cast to a double.
     */
    public double getDoubleVariable(String name) {return ConfigurationSection.getDoubleValue(getVariable(name));}

    /**
     * Returns the value of the specified variable as a boolean.
     * @param  name fully qualified name of the variable whose value should be retrieved.
     * @return the variable's value if set, <code>false</code> otherwise.
     */
    public boolean getBooleanVariable(String name) {return ConfigurationSection.getBooleanValue(getVariable(name));}

    /**
     * Checks whether the specified variable has been set.
     * @param  name fully qualified name of the variable to check for.
     * @return      <code>true</code> if the variable is set, <code>false</code> otherwise.
     */
    public boolean isVariableSet(String name) {return getVariable(name) != null;}



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
    public synchronized String removeVariable(String name) {
        ConfigurationExplorer explorer; // Used to navigate to the variable's parent section.
        String                buffer;   // Buffer for the variable's name trimmed of section information.

        // If the variable's 'path' doesn't exist, return null.
        if((buffer = moveToParent(explorer = new ConfigurationExplorer(root), name, false)) == null)
            return null;

        // If the variable was actually set, triggers an event.
        if((buffer = explorer.getSection().removeVariable(buffer)) != null)
            triggerEvent(new ConfigurationEvent(this, name, null));

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
    public int removeIntegerVariable(String name) {return ConfigurationSection.getIntegerValue(removeVariable(name));}

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered listeners.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>0</code> if it wasn't set.
     */
    public long removeLongVariable(String name) {return ConfigurationSection.getLongValue(removeVariable(name));}

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered listeners.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>0</code> if it wasn't set.
     */
    public float removeFloatVariable(String name) {return ConfigurationSection.getFloatValue(removeVariable(name));}

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered listeners.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>0</code> if it wasn't set.
     */
    public double removeDoubleVariable(String name) {return ConfigurationSection.getDoubleValue(removeVariable(name));}

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered listeners.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>false</code> if it wasn't set.
     */
    public boolean removeBooleanVariable(String name) {return ConfigurationSection.getBooleanValue(removeVariable(name));}



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
    public synchronized String getVariable(String name, String defaultValue) {
        ConfigurationExplorer explorer; // Used to navigate to the variable's parent section.
        String                value;    // Buffer for the variable's value.
        String                buffer;   // Buffer for the variable's name trimmed of section information.

        // Navigates to the parent section. We do not have to check for null values here,
        // as the section will be created if it doesn't exist.
        buffer = moveToParent(explorer = new ConfigurationExplorer(root), name, true);

        // If the variable isn't set, set it to defaultValue and triggers an event.
        if((value = explorer.getSection().getVariable(buffer)) == null) {
            explorer.getSection().setVariable(buffer, defaultValue);
            triggerEvent(new ConfigurationEvent(this, name, defaultValue));
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
    public int getVariable(String name, int defaultValue) {
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
    public long getVariable(String name, long defaultValue) {
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
    public float getVariable(String name, float defaultValue) {
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
    public boolean getVariable(String name, boolean defaultValue) {
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
    public double getVariable(String name, double defaultValue) {
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
    private String moveToParent(ConfigurationExplorer root, String name, boolean create) {
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
    public static void addConfigurationListener(ConfigurationListener listener) {listeners.put(listener, null);}

    /**
     * Removes the specified object from the list of registered configuration listeners.
     * @param listener object to remove from the list of registered configuration listeners.
     */
    public static void removeConfigurationListener(ConfigurationListener listener) {listeners.remove(listener);}

    /**
     * Passes the specified event to all registered configuration listeners.
     * @param event event to propagate.
     */
    private static void triggerEvent(ConfigurationEvent event) {
        Iterator iterator;

        iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((ConfigurationListener)iterator.next()).configurationChanged(event);
    }



    // - Loading ---------------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * @author Nicolas Rinaudo
     */
    private class ConfigurationLoader implements ConfigurationBuilder {
        // - Instance variables ------------------------------------------------
        // ---------------------------------------------------------------------
        /** Parents of {@link #currentSection}. */
        private Stack                sections;
        /** Fully qualified names of {@link #currentSection}. */
        private Stack                sectionNames;
        /** Section that we're currently building. */
        private ConfigurationSection currentSection;



        // - Initialisation ----------------------------------------------------
        // ---------------------------------------------------------------------
        /**
         * Creates a new configuration loader.
         * @param root where to create the configuration in.
         */
        public ConfigurationLoader(ConfigurationSection root) {currentSection = root;}



        // - Building ----------------------------------------------------------
        // ---------------------------------------------------------------------
        /**
         * Initialises the configuration bulding.
         */
        public void startConfiguration() {
            sections     = new Stack();
            sectionNames = new Stack();
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
                sectionNames.push(((String)sectionNames.peek()) + name + '.');
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
                buffer = (ConfigurationSection)sections.pop();
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
                    triggerEvent(new ConfigurationEvent(Configuration.this, ((String)sectionNames.peek()) + name, value));
            }
        }
    }
}
