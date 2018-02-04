/**
 * Provides classes to deal with software configuration.
 * <h3>Configuration variables</h3>
 * <p>
 * Configuration data is stored as a set of variables organised in sections. A typical variable
 * name is: <code>section.subsection.name</code> where:
 * <ul>
 * <li><code>section</code> and <code>subsection</code> are both sections.</li>
 * <li><code>name</code> is the variable's name.</li>
 * </ul>
 * </p>
 * <p>
 * Configuration data is stored in instances of {@link com.mucommander.commons.conf.Configuration}, which offers a set
 * of methods manipulate variables:
 * <ul>
 * <li>
 * {@link com.mucommander.commons.conf.Configuration#getVariable(String) Basic retrieval}, which returns a
 * variable's value if known.
 * </li>
 * <li>
 * {@link com.mucommander.commons.conf.Configuration#getVariable(String, String) Advanced retrieval}, which returns
 * a variable's value and set it to a default value if not known.
 * </li>
 * <li>
 * Existence {@link com.mucommander.commons.conf.Configuration#isVariableSet(String) checking}, which checks
 * whether a variable exists or not.
 * </li>
 * <li>
 * {@link com.mucommander.commons.conf.Configuration#renameVariable(String, String) Renaming}, which changes a
 * variable's name as well as the section it belongs to.
 * </li>
 * <li>
 * {@link com.mucommander.commons.conf.Configuration#removeVariable(String) Removal}, which deletes a variable from
 * the configuration.
 * </li>
 * <li>
 * {@link com.mucommander.commons.conf.Configuration#setVariable(String, String) Setting}, which sets a variable's
 * value.
 * </li>
 * </ul>
 * </p>
 * <h3>Loading and storing configuration</h3>
 * <p>
 * The <code>com.mucommander.commons.conf</code> package offers various ways of loading and storing configuration.<br>
 * The most obvious way is by using the {@link com.mucommander.commons.conf.Configuration#read(InputStream) read} and
 * {@link com.mucommander.commons.conf.Configuration#write(OutputStream) write} methods, but this has the disadvantage
 * of forcing application writers to manage streams themselves.<br>
 * The preferred method is to create a dedicated {@link com.mucommander.commons.conf.ConfigurationSource} class and
 * register it through {@link com.mucommander.commons.conf.Configuration#setSource(ConfigurationSource) setSource}.
 * This allows an instance of {@link com.mucommander.commons.conf.Configuration} to know how to read from and write to
 * its configuration file (or socket or any other medium that provides input and output streams).
 * </p>
 * <h3>Changing the default configuration format</h3>
 * <p>
 * The default configuration format is described in {@link com.mucommander.commons.conf.XmlConfigurationReader}.
 * Application writers who wish to change this can do so by:
 * <ul>
 * <li>
 * Creating custom {@link com.mucommander.commons.conf.ConfigurationBuilder writers} and
 * {@link com.mucommander.commons.conf.ConfigurationReader readers}.
 * </li>
 * <li>
 * Creating associated {@link com.mucommander.commons.conf.ConfigurationWriterFactory writer factories} and
 * {@link com.mucommander.commons.conf.ConfigurationReaderFactory reader factories}.
 * </li>
 * <li>
 * Registering them through
 * {@link com.mucommander.commons.conf.Configuration#setWriterFactory(ConfigurationWriterFactory) setWriterFactory}
 * and
 * {@link com.mucommander.commons.conf.Configuration#setReaderFactory(ConfigurationReaderFactory) setReaderFactory}.
 * </li>
 * </ul>
 * </p>
 * <h3>Listening to the configuration</h3>
 * <p>
 * Classes that need to be notified when the configuration has changed can do so by:
 * <ul>
 * <li>Implementing the {@link com.mucommander.commons.conf.ConfigurationListener} interface.</li>
 * <li>
 * Registering themselves through
 * {@link com.mucommander.commons.conf.Configuration#addConfigurationListener(ConfigurationListener)
 * addConfigurationLister}.
 * </li>
 * </ul>
 * </p>
 */
package com.mucommander.commons.conf;