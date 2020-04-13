/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.ri.util;

import org.icepdf.core.pobjects.fonts.FontManager;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * <p>This class provides a very basic Font Properties Management system.  When this
 * class is initiated, the properites file "pdfviewerfontcache.properties" is
 * read from the default application file path.  If the file cannot be found then
 * all system fonts are read from the operating system and are written to the
 * "pdfviewerfontcache.properties" file.</p>
 * <p/>
 * <p>This class is designed to speed up the load time of the viewer application
 * by reading already parsed font information from the properties file.  If new
 * fonts are added to the system, the "pdfviewerfontcache.properties" file can
 * be deleted to trigger this class to re-read the System fonts and re-create
 * a new "pdfviewerfontcache.properties" properites file.
 *
 * // read/store the font cache.
 * ResourceBundle messageBundle = ResourceBundle.getBundle(
 * PropertiesManager.DEFAULT_MESSAGE_BUNDLE);
 * PropertiesManager properties = new PropertiesManager(System.getProperties(),
 * ResourceBundle.getBundle(PropertiesManager.DEFAULT_MESSAGE_BUNDLE));
 * new FontPropertiesManager(properties, System.getProperties(), messageBundle);
 *
 * @since 2.0
 */
public class FontPropertiesManager {

    private static final Logger logger =
            Logger.getLogger(FontPropertiesManager.class.toString());

    private static final String DEFAULT_HOME_DIR = ".icesoft/icepdf_viewer";
    private static final String LOCK_FILE = "_syslock";
    private final static String USER_FILENAME = "pdfviewerfontcache.properties";

    // format version number
    private final static String FORMAT_VERSION = "6.0";

    private FontManager fontManager;

    private Properties sysProps;
    private PropertiesManager props;

    private File userHome;

    //the swingri home directory
    private File dataDir;

    //not to save the bookmarks and properties if lockDir == null, that is
    //when we do not own the lock
    private File lockDir;

    private File propertyFile;

    private ResourceBundle messageBundle;

    /**
     * Create a new instance of the FontPropertiesManager class. This constructor will
     * automatically scan the system for the available fonts.
     * <p/>
     * Typical usage would look like this:<br />
     * <ul>
     * // read/store the font cache.
     * ResourceBundle messageBundle = ResourceBundle.getBundle(
     * PropertiesManager.DEFAULT_MESSAGE_BUNDLE);
     * PropertiesManager properties = new PropertiesManager(System.getProperties(),
     * ResourceBundle.getBundle(PropertiesManager.DEFAULT_MESSAGE_BUNDLE));
     * <p/>
     * // creates a new cache properties file, does not read system fonts.
     * FontPropertiesManager fontPropertiesManager =
     * new FontPropertiesManager(properties, System.getProperties(), messageBundle, false);
     * </ul>
     *
     * @param appProps      properties manager reference
     * @param sysProps      system properties.
     * @param messageBundle application message bundle.
     */
    public FontPropertiesManager(PropertiesManager appProps, Properties sysProps,
                                 ResourceBundle messageBundle) {
        this.sysProps = sysProps;
        this.props = appProps;
        this.messageBundle = messageBundle;
        // create a new Font Manager.
        this.fontManager = FontManager.getInstance();

        setupHomeDir(null);

        recordMofifTime();

        setupLock();
        // create the properties file and scan for font sif the
        propertyFile = new File(dataDir, USER_FILENAME);
        if (!propertyFile.exists()) {
            // scan the system for know font locations.
            readDefaulFontPaths(null);
            // save the file
            saveProperties();
        }else{
            loadProperties();
        }

    }

    /**
     * Create a new instance of the FontPropertiesManager class.  This constructor will not scan
     * the system for fonts.  The users must call one of the following methods to scan for fonts;
     * {@link #readFontPaths} or {@link #readDefaulFontPaths(String[])}
     *
     * <p/>
     * Typical usage would look like this:<br />
     * <ul>
     * // read/store the font cache.
     * ResourceBundle messageBundle = ResourceBundle.getBundle(
     * PropertiesManager.DEFAULT_MESSAGE_BUNDLE);
     * PropertiesManager properties = new PropertiesManager(System.getProperties(),
     * ResourceBundle.getBundle(PropertiesManager.DEFAULT_MESSAGE_BUNDLE));
     * <p/>
     * // creates a new cache properties file, does not read system fonts.
     * FontPropertiesManager fontPropertiesManager = new FontPropertiesManager(properties, messageBundle, false);
     * fontPropertiesManager.readFontPaths(null);
     * </ul>
     *
     * @param appProps      properties manager reference
     * @param messageBundle application message bundle.
     */
    public FontPropertiesManager(PropertiesManager appProps, ResourceBundle messageBundle) {
        this.sysProps = appProps.getSystemProperties();
        this.props = appProps;
        this.messageBundle = messageBundle;
        // create a new Font Manager.
        this.fontManager = FontManager.getInstance();

        setupHomeDir(null);

        recordMofifTime();

        setupLock();
        // create the properties file
        if (ownLock()) {
            propertyFile = new File(dataDir, USER_FILENAME);
        }
    }

    /**
     * Removes the the properties file from the file system.
     */
    public synchronized void removeFontCacheFile() {
        if (ownLock()) {
            propertyFile = new File(dataDir, USER_FILENAME);
            // load font properties from last invocation
            boolean deleted = false;
            if (propertyFile.exists()) {
                try {
                    deleted = propertyFile.delete();
                } catch (SecurityException ex) {
                    // log the error
                    if (!deleted && logger.isLoggable(Level.WARNING)) {
                        logger.log(Level.WARNING, "Error removing font properties file.", ex);
                    }
                }
            }
        }
    }

    /**
     * Clears any font references from the font managers internal cache.
     */
    public synchronized void clearProperties() {
        fontManager.clearFontList();
    }

    /**
     * Loads the properties file and loads any font data that it contains. If no font
     * cache file is found then a new one is created and false is returned.  If a file
     * is found the properties in it are loaded and added to the fontManager class, true is
     * returned.
     *
     * @return true if font file has been found and loaded, false otherwise.
     */
    public synchronized boolean loadProperties() {

        if (ownLock()) {
            // load font properties from last invocation
            if (propertyFile != null && propertyFile.exists()) {
                try {
                    InputStream in = new FileInputStream(propertyFile);
                    try {
                        Properties fontProps = fontManager.getFontProperties();
                        fontProps.load(in);
                        fontManager.setFontProperties(fontProps);
                    } finally {
                        in.close();
                    }
                } catch (IOException ex) {
                    // check to make sure the storage relate dialogs can be shown
                    if (getBoolean("application.showLocalStorageDialogs", true)) {
                        Resources.showMessageDialog(null,
                                JOptionPane.ERROR_MESSAGE, messageBundle,
                                "fontManager.properties.title",
                                "manager.properties.session.readError",
                                ex);
                    }
                    // log the error
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.log(Level.WARNING, "Error loading font properties cache", ex);
                    }
                    return false;
                } catch (IllegalArgumentException e) {
                    return false;
                }
                return true;
            }
            // If no font data, then read font data and save the new file.
            else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Reads the specified file paths and loads any found font fonts in the font Manager.
     * In order to persist the results a call to {@link #saveProperties()} needs to be called.
     *
     * @param fontPaths array of paths containing folders
     */
    public void readFontPaths(String[] fontPaths) {
        // create program properties with default
        try {
            // If you application needs to look at other font directories
            // they can be added via the readSystemFonts method.
            fontManager.readFonts(fontPaths);
        } catch (Exception ex) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Error reading system paths:", ex);
            }
        }
    }

    /**
     * Touches the properties file, writing out any font properties.
     */
    public synchronized void saveProperties() {
        if (ownLock()) {
            try {
                FileOutputStream out = new FileOutputStream(propertyFile);
                try {
                    Properties fontProps = fontManager.getFontProperties();
                    fontProps.store(out, "-- ICEpdf Font properties --\n " + FORMAT_VERSION);
                } finally {
                    out.close();
                }
                recordMofifTime();
            } catch (IOException ex) {
                // check to make sure the storage relate dialogs can be shown
                if (getBoolean("application.showLocalStorageDialogs", true)) {
                    Resources.showMessageDialog(null,
                            JOptionPane.ERROR_MESSAGE, messageBundle,
                            "fontManager.properties.title",
                            "manager.properties.saveError", ex);
                }
                // log the error
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "Error saving font properties cache", ex);
                }
            }
        }
    }

    private boolean ownLock() {
        return lockDir != null;
    }

    private void recordMofifTime() {
        Calendar c = new GregorianCalendar();
        c.setTime(new Date());
        c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + 1);
        c.set(Calendar.SECOND, 0);
    }

    private void setupLock() {
        if (dataDir == null) {
            lockDir = null;
        } else {
            File dir = new File(dataDir, LOCK_FILE);
            if (!dir.mkdir()) {

                dir.delete();
                if (!dir.mkdir()) {
                    dir = null;
                    if (getBoolean("application.showLocalStorageDialogs", true)) {
                        Resources.showMessageDialog(null,
                                JOptionPane.ERROR_MESSAGE, messageBundle,
                                "fontManager.properties.title",
                                "manager.properties.session.nolock", LOCK_FILE);
                    }
                }

            }
            lockDir = dir;
        }
    }

    /**
     * Sets the default font properties files by readying available system font paths.
     *
     * @param extraFontPaths extra font paths to load on top of the default paths.
     * @return true if system font search returned without error, otherwise false.
     */
    public boolean readDefaulFontPaths(String[] extraFontPaths) {
        // create program properties with default
        try {
            // If you application needs to look at other font directories
            // they can be added via the readSystemFonts method.
            fontManager.readSystemFonts(extraFontPaths);
        } catch (Exception ex) {
            if (getBoolean("application.showLocalStorageDialogs", true)) {
                Resources.showMessageDialog(null,
                        JOptionPane.ERROR_MESSAGE, messageBundle,
                        "fontManager.properties.title",
                        "manager.properties.session.readError",
                        ex);
            }// log the error
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Error loading default properties", ex);
            }
            return false;
        }
        return true;
    }

    private void setupHomeDir(String homeString) {
        if (homeString == null) {
            homeString = sysProps.getProperty("swingri.home");
        }

        if (homeString != null) {
            dataDir = new File(homeString);
        } else {
            userHome = new File(sysProps.getProperty("user.home"));
            String dataDirStr = props.getString("application.datadir", DEFAULT_HOME_DIR);
            dataDir = new File(userHome, dataDirStr);
        }

        if (!dataDir.isDirectory()) {
            String path = dataDir.getAbsolutePath();
            boolean create;
            if (props.hasUserRejectedCreatingLocalDataDir()) {
                create = false;
            } else if (getBoolean("application.showLocalStorageDialogs", true)) {
                create = Resources.showConfirmDialog(null,
                        messageBundle, "fontManager.properties.title",
                        "manager.properties.createNewDirectory", path);
                if (!create)
                    props.setUserRejectedCreatingLocalDataDir();
            } else {
                // Always create local-storage directory if show user prompt dialog setting is false.
                create = true;
            }

            if (!create) {
                dataDir = null;
            } else {
                dataDir.mkdirs();
                if (!dataDir.isDirectory()) {
                    // check to make sure that dialog should be shown on the error.
                    if (getBoolean("application.showLocalStorageDialogs", true)) {
                        Resources.showMessageDialog(null,
                                JOptionPane.ERROR_MESSAGE, messageBundle,
                                "fontManager.properties.title",
                                "manager.properties.failedCreation",
                                dataDir.getAbsolutePath());
                    }
                    dataDir = null;
                }
            }
        }
    }

    public boolean getBoolean(String propertyName, boolean defaultValue) {
        Boolean result = getBooleanImpl(propertyName);
        if (result == null) {
            return defaultValue;
        }
        return result == Boolean.TRUE;
    }

    private Boolean getBooleanImpl(String propertyName) {
        String value = props.getString(propertyName);
        if (value != null) {
            Boolean result = Parse.parseBoolean(value, messageBundle);
            if (result != null) {
                return result;
            }
            props.remove(propertyName);
        }
        value = props.getString(propertyName);
        if (value != null) {
            Boolean result = Parse.parseBoolean(value, null);
            if (result != null) {
                return result;
            }
            throwBrokenDefault(propertyName, value);
        }
        return null;
    }

    private void throwBrokenDefault(String propertyName, String value) {
        throw new IllegalStateException("Broken default property '" + propertyName + "' value: '" + value + "'");
    }

}
