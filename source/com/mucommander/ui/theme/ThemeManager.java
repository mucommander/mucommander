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

package com.mucommander.ui.theme;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.RuntimeConstants;
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.filter.ExtensionFilenameFilter;
import com.mucommander.file.util.ResourceLoader;
import com.mucommander.io.BackupInputStream;
import com.mucommander.io.BackupOutputStream;
import com.mucommander.text.Translator;
import com.mucommander.util.StringUtils;

import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * Offers methods for accessing and modifying themes.
 * @author Nicolas Rinaudo
 */
public class ThemeManager {
    // - Class variables -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Path to the user defined theme file. */
    private static       AbstractFile userThemeFile;
    /** Default user defined theme file name. */
    private static final String       USER_THEME_FILE_NAME = "user_theme.xml";
    /** Path to the custom themes repository. */
    private static final String       CUSTOM_THEME_FOLDER  = "themes";
    /** List of all registered theme change listeners. */
    private static final WeakHashMap  listeners            = new WeakHashMap();



    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Whether or not the user theme was modified. */
    private static boolean       wasUserThemeModified;
    /** Theme that is currently applied to muCommander. */
    private static Theme         currentTheme;
    /** Used to listen on the current theme's modifications. */
    private static ThemeListener listener = new CurrentThemeListener();



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Prevents instanciation of the class.
     */
    private ThemeManager() {}

    /**
     * Loads the current theme.
     * <p>
     * This method goes through the following steps:
     * <ul>
     *  <li>Try to load the theme defined in the configuration.</li>
     *  <li>If that failed, try to load the default theme.</li>
     *  <li>If that failed, try to load the user theme if that hasn't been tried yet.</li>
     *  <li>If that failed, use an empty theme.</li>
     * </ul>
     * </p>
     */
    public static void loadCurrentTheme() {
        int     type;               // Current theme's type.
        String  name;               // Current theme's name.
        boolean wasUserThemeLoaded; // Whether we have tried loading the user theme or not.

        // Loads the current theme type as defined in configuration.
        try {type = getThemeTypeFromLabel(MuConfiguration.getVariable(MuConfiguration.THEME_TYPE, MuConfiguration.DEFAULT_THEME_TYPE));}
        catch(Exception e) {type = getThemeTypeFromLabel(MuConfiguration.DEFAULT_THEME_TYPE);}

        // Loads the current theme name as defined in configuration.
        if(type != Theme.USER_THEME) {
            wasUserThemeLoaded = false;
            name               = MuConfiguration.getVariable(MuConfiguration.THEME_NAME, MuConfiguration.DEFAULT_THEME_NAME);
	}
        else {
            name               = null;
            wasUserThemeLoaded = true;
        }

        // If the current theme couldn't be loaded, uses the default theme as defined in the configuration.
        currentTheme = null;
        try {currentTheme = readTheme(type, name);}
        catch(Exception e1) {
            type = getThemeTypeFromLabel(MuConfiguration.DEFAULT_THEME_TYPE);
            name = MuConfiguration.DEFAULT_THEME_NAME;

            if(type == Theme.USER_THEME)
                wasUserThemeLoaded = true;

            // If the default theme can be loaded, tries to load the user theme if we haven't done so yet.
            // If we have, or if it fails, defaults to an empty user theme.
            try {currentTheme = readTheme(type, name);}
            catch(Exception e2) {
                if(!wasUserThemeLoaded) {
                    try {currentTheme = readTheme(Theme.USER_THEME, null);}
                    catch(Exception e3) {}
                }
                if(currentTheme == null) {
                    currentTheme         = new Theme(listener);
                    wasUserThemeModified = true;
                }
            }
            setConfigurationTheme(currentTheme);
        }
    }



    // - Themes access -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    private static Iterator predefinedThemeNames() {
        return getThemeNames(ResourceLoader.getResourceAsFile(RuntimeConstants.THEMES_PATH));
    }

    private static Iterator customThemeNames() throws IOException {
        return getThemeNames(FileFactory.getFile(getCustomThemesFolder().getAbsolutePath()));
    }

    private static Iterator getThemeNames(AbstractFile themeFolder) {
        AbstractFile[] files;
        Vector         names;

        try {
            files = themeFolder.ls(new ExtensionFilenameFilter(".xml"));
            names = new Vector();
            for(int i = 0; i < files.length; i++)
                names.add(getThemeName(files[i]));
            return names.iterator();
        }
        catch(Exception e) {
            return new Vector().iterator();
        }
    }

    public static Vector getAvailableThemes() {
        Vector   themes;
        Iterator iterator;
        String   name;

        themes = new Vector();

        // Tries to load the user theme. If it's corrupt, uses an empty user theme.
        try {themes.add(readTheme(Theme.USER_THEME, null));}
        catch(Exception e) {themes.add(new Theme(listener));}

        // Loads predefined themes.
        iterator = predefinedThemeNames();
        while(iterator.hasNext()) {
            name = (String)iterator.next();
            try {themes.add(readTheme(Theme.PREDEFINED_THEME, name));}
            catch(Exception e) {if(Debug.ON) Debug.trace("Failed to load predefined theme " + name + ": " + e);}
        }

        // Loads custom themes.
        try {
            iterator = customThemeNames();
            while(iterator.hasNext()) {
                name = (String)iterator.next();
                try {themes.add(readTheme(Theme.CUSTOM_THEME, name));}
                catch(Exception e) {if(Debug.ON) Debug.trace("Failed to load custom theme " + name + ": " + e);}
            }
        }
        catch(Exception e) {if(Debug.ON) Debug.trace("Failed to load custom themes: " + e);}

        // Sorts the themes by name.
        Collections.sort(themes, new Comparator() {
                public int compare(Object o1, Object o2) {return (((Theme)o1).getName()).compareTo(((Theme)o2).getName());}
            });

        return themes;
    }

    public static Vector getAvailableThemeNames() {
        Vector   themes;
        Iterator iterator;

        themes = new Vector();

        // Adds the user theme name.
        themes.add(Translator.get("theme.custom_theme"));

        // Adds predefined theme names.
        iterator = predefinedThemeNames();
        while(iterator.hasNext())
            themes.add(iterator.next());

        // Adds custom theme names.
        try {
            iterator = customThemeNames();
            while(iterator.hasNext())
                themes.add(iterator.next());
        }
        catch(Exception e) {if(Debug.ON) Debug.trace("Failed to load custom theme names: " + e);}

        // Sorts the theme names.
        Collections.sort(themes);

        return themes;
    }

    public static Iterator availableThemeNames() {return getAvailableThemeNames().iterator();}

    public static synchronized Iterator availableThemes() {return getAvailableThemes().iterator();}



    // - Theme paths access --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the path to the user's theme file.
     * <p>
     * This method cannot guarantee the file's existence, and it's up to the caller
     * to deal with the fact that the user might not actually have created a user theme.
     * </p>
     * <p>
     * This method's return value can be modified through {@link #setUserThemeFile(String)}.
     * If this wasn't called, the default path will be used. This is generated by calling
     * <code>new java.io.File({@link com.mucommander.PlatformManager#getPreferencesFolder()}, {@link #USER_THEME_FILE_NAME})</code>.
     * </p>
     * @return             the path to the user's theme file.
     * @see                #setUserThemeFile(String)
     * @throws IOException if an error occured while locating the default user theme file.
     */
    public static AbstractFile getUserThemeFile() throws IOException {
        if(userThemeFile == null)
            return PlatformManager.getPreferencesFolder().getChild(USER_THEME_FILE_NAME);
        return userThemeFile;
    }

    /**
     * Sets the path to the user theme file.
     * <p>
     * The specified file does not have to exist. If it does, however, it must be accessible.
     * </p>
     * @param  file                  path to the user theme file.
     * @throws FileNotFoundException if <code>file</code> is not accessible.
     * @see                          #getUserThemeFile()
     */
    public static void setUserThemeFile(File file) throws FileNotFoundException {setUserThemeFile(FileFactory.getFile(file.getAbsolutePath()));}

    /**
     * Sets the path to the user theme file.
     * <p>
     * The specified file does not have to exist. If it does, however, it must be accessible.
     * </p>
     * @param  file                     path to the user theme file.
     * @throws IllegalArgumentException if <code>file</code> exists but is not accessible.
     * @see                             #getUserThemeFile()
     */
    public static void setUserThemeFile(AbstractFile file) throws FileNotFoundException {
        if(file.isBrowsable())
            throw new FileNotFoundException("Not a valid file: " + file.getAbsolutePath());
        userThemeFile = file;
    }

    /**
     * Sets the path to the user theme file.
     * <p>
     * The specified file does not have to exist. If it does, however, it must be accessible.
     * </p>
     * @param  path                  path to the user theme file.
     * @throws FileNotFoundException if <code>path</code> is not accessible.
     * @see                          #getUserThemeFile()
     */
    public static void setUserThemeFile(String path) throws FileNotFoundException {
        AbstractFile file;

        if((file = FileFactory.getFile(path)) == null)
            setUserThemeFile(new File(path));
        else
            setUserThemeFile(file);
    }

    /**
     * Returns the path to the custom themes' folder.
     * <p>
     * This method guarantees that the returned file actually exists.
     * </p>
     * @return the path to the custom themes' folder.
     * @throws IOException if an error occured while locating the default user themes folder.
     */
    public static AbstractFile getCustomThemesFolder() throws IOException {
        AbstractFile customFolder;

        // Retrieves the path to the custom themes folder and creates it if necessary.
        customFolder = PlatformManager.getPreferencesFolder().getChild(CUSTOM_THEME_FOLDER);
        if(!customFolder.exists())
            customFolder.mkdir();

        return customFolder;
    }


    // - Theme renaming / deleting -------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public static void deleteCustomTheme(String name) throws IOException {
        AbstractFile file;

        // Makes sure the specified theme is not the current one.
        if(isCurrentTheme(Theme.CUSTOM_THEME, name))
            throw new IllegalArgumentException("Cannot delete current theme.");

        // Deletes the theme.
        file = getCustomThemesFolder().getChild(name + ".xml");
        if(file.exists())
            file.delete();
    }

    public static boolean renameCustomTheme(Theme theme, String name) throws IOException {
        if(theme.getType() != Theme.CUSTOM_THEME)
            throw new IllegalArgumentException("Cannot rename non-custom themes.");

        // Makes sure the operation is necessary.
        if(theme.getName().equals(name))
            return true;

        // Computes a legal new name and renames theme.
        name = getAvailableCustomThemeName(name);
        if(getCustomThemesFolder().getChild(theme.getName() + ".xml").moveTo(getCustomThemesFolder().getChild(name + ".xml"))) {
            theme.setName(name);
            if(isCurrentTheme(theme))
                setConfigurationTheme(theme);
            return true;
        }
        return false;
    }



    // - Theme writing -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns an output stream on the specified custom theme.
     * @param  name        name of the custom theme on which to open an output stream.
     * @return             an output stream on the specified custom theme.
     * @throws IOException if an I/O related error occurs.
     */
    private static BackupOutputStream getCustomThemeOutputStream(String name) throws IOException {
        return new BackupOutputStream(getCustomThemesFolder().getChild(name + ".xml"));
    }

    /**
     * Returns an output stream on the user theme.
     * @return             an output stream on the user theme.
     * @throws IOException if an I/O related error occurs.
     */
    private static BackupOutputStream getUserThemeOutputStream() throws IOException {
        return new BackupOutputStream(getUserThemeFile());
    }

    /**
     * Returns an output stream on the requested theme.
     * <p>
     * This method is just a convenience, and wraps calls to {@link #getUserThemeInputStream()},
     * and {@link #getCustomThemeInputStream(String)}.
     * </p>
     * <p>
     * If <code>type</code> is equal to {@link Theme#USER_THEME}, the <code>name</code> argument
     * will be ignored: there is only one user theme.
     * </p>
     * <p>
     * If <code>type</code> is equal to {@link Theme#PREDEFINED_THEME}, an <code>IllegalArgumentException</code>
     * will be thrown: predefined themes are not editable.
     * </p>
     * @param  type        type of the theme on which to open an output stream.
     * @param  name        name of the theme on which to open an output stream.
     * @return             an output stream on the requested theme.
     * @throws IOException if an I/O related error occurs.
     */
    private static BackupOutputStream getOutputStream(int type, String name) throws IOException {
        switch(type) {
            // Predefined themes.
        case Theme.PREDEFINED_THEME:
            throw new IllegalArgumentException("Can not open output streams on predefined themes.");

            // Custom themes.
        case Theme.CUSTOM_THEME:
            return getCustomThemeOutputStream(name);

            // User theme.
        case Theme.USER_THEME:
            return getUserThemeOutputStream();
        }

        // Unknown theme.
        throw new IllegalArgumentException("Illegal theme type: " + type);
    }

    /**
     * Copies the content of <code>in</code> into <code>out</code>.
     * @param  in          where to read the data from.
     * @param  out         where to write the data to.
     * @throws IOException if an error occured.
     */
    private static void copyStreams(InputStream in, OutputStream out) throws IOException {
        byte[] buffer; // Used to store the data before transfering it.
        int    count;  // Number of bytes read during the last read operation.

        // Transfers the content of in to out.
        buffer = new byte[65536];
        while((count = in.read(buffer, 0, buffer.length)) != -1)
            out.write(buffer, 0, count);
    }

    /**
     * Writes the content of the specified theme data to the specified output stream.
     * <p>
     * This method differs from {@link #exportTheme(Theme,OutputStream)} in that it will
     * write the theme data only, skipping comments and other metadata.
     * </p>
     * @param  data        theme data to write.
     * @param  out         where to write the theme data.
     * @throws IOException if an I/O related error occurs.
     * @see                #exportTheme(Theme,OutputStream)
     * @see                #exportTheme(Theme,File)
     * @see                #writeThemeData(ThemeData,File).
     */
    public static void writeThemeData(ThemeData data, OutputStream out) throws IOException {ThemeWriter.write(data, out);}

    /**
     * Writes the content of the specified theme data to the specified file.
     * <p>
     * This method differs from {@link #exportTheme(Theme,File)} in that it will
     * write the theme data only, skipping comments and other metadata.
     * </p>
     * @param  data        theme data to write.
     * @param  file        file in which to write the theme data.
     * @throws IOException if an I/O related error occurs.
     * @see                #exportTheme(Theme,OutputStream)
     * @see                #exportTheme(Theme,File)
     * @see                #writeThemeData(ThemeData,OutputStream).
     */
    public static void writeThemeData(ThemeData data, File file) throws IOException {
        OutputStream out; // OutputStream on file.

        out = null;

        // Writes the theme data.
        try {writeThemeData(data, out = new FileOutputStream(file));}

        // Cleanup.
        finally {
            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }
    }

    /**
     * Writes the content of the specified theme to its description file.
     * @param  theme                    theme to write.
     * @throws IOException              if any I/O related error occurs.
     * @throws IllegalArgumentException if <code>theme</code> is a predefined theme.
     * @see                             #writeTheme(ThemeData,int,String)
     */
    public static void writeTheme(Theme theme) throws IOException {writeTheme(theme, theme.getType(), theme.getName());}

    /**
     * Writes the specified theme data over the theme described by <code>type</code> and <code>name</code>.
     * <p>
     * Note that this method doesn't check whether this will overwrite an existing theme.
     * </p>
     * <p>
     * If <code>type</code> equals {@link Theme#USER_THEME}, <code>name</code> will be ignored.
     * </p>
     * @param  data                     data to write.
     * @param  type                     type of the theme that is being written.
     * @param  name                     name of the theme that is being written.
     * @throws IOException              if any I/O related error occurs.
     * @throws IllegalArgumentException if <code>theme</code> is a predefined theme.
     * @see                             #writeTheme(Theme)
     */
    public static void writeTheme(ThemeData data, int type, String name) throws IOException {
        OutputStream out;

        out = null;
        try {writeThemeData(data, out = getOutputStream(type, name));}
        finally {
            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }
    }

    /**
     * Exports the specified theme to the specified output stream.
     * <p>
     * If <code>type</code> is equal to {@link Theme#USER_THEME}, the <code>name</code> argument will be ignored
     * as there is only one user theme.
     * </p>
     * <p>
     * This method differs from {@link #writeThemeData(ThemeData,OutputStream)} in that it doesn't only copy
     * the theme's data, but the whole content of the theme file, including comments. It also requires the theme
     * file to exist.
     * </p>
     * @param  type        type of the theme to export.
     * @param  name        name of the theme to export.
     * @param  out         where to write the theme.
     * @throws IOException if any I/O related error occurs.
     * @see                #exportTheme(int,String,File)
     * @see                #writeThemeData(ThemeData,OutputStream)
     */
    public static void exportTheme(int type, String name, OutputStream out) throws IOException {
        InputStream in; // Where to read the theme from.

        in = null;
        try {copyStreams(in = getInputStream(type, name), out);}
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
        }
    }

    /**
     * Exports the specified theme to the specified output stream.
     * <p>
     * If <code>type</code> is equal to {@link Theme#USER_THEME}, the <code>name</code> argument will be ignored
     * as there is only one user theme.
     * </p>
     * <p>
     * This method differs from {@link #writeThemeData(ThemeData,File)} in that it doesn't only copy
     * the theme's data, but the whole content of the theme file, including comments.
     * </p>
     * @param  type        type of the theme to export.
     * @param  name        name of the theme to export.
     * @param  file        where to write the theme.
     * @throws IOException if any I/O related error occurs
     * @see                #exportTheme(int,String,OutputStream)
     * @see                #writeThemeData(ThemeData,File).
     */
    public static void exportTheme(int type, String name, File file) throws IOException {
        OutputStream out; // Where to write the data to.

        out = null;
        try {exportTheme(type, name, out = new FileOutputStream(file));}
        finally {
            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }
    }

    /**
     * Exports the specified theme to the specified output stream.
     * <p>
     * This is a convenience method only and is strictly equivalent to calling
     * <code>{@link #exportTheme(int,String,OutputStream) exportTheme(}theme.getType(), theme.getName(), out);</code>
     * </p>
     * @param  theme       theme to export.
     * @param  out         where to write the theme.
     * @throws IOException if any I/O related error occurs.
     */
    public static void exportTheme(Theme theme, OutputStream out) throws IOException {exportTheme(theme.getType(), theme.getName(), out);}

    /**
     * Exports the specified theme to the specified output stream.
     * <p>
     * This is a convenience method only and is strictly equivalent to calling
     * <code>{@link #exportTheme(int,String,File) exportTheme(}theme.getType(), theme.getName(), file);</code>
     * </p>
     * @param  theme       theme to export.
     * @param  file        where to write the theme.
     * @throws IOException if any I/O related error occurs.
     */
    public static void exportTheme(Theme theme, File file) throws IOException {exportTheme(theme.getType(), theme.getName(), file);}

    private static String getAvailableCustomThemeName(File file) {
        String   name;

        // Retrieves the file's name, cutting the .xml extension off if
        // necessary.
        if(StringUtils.endsWithIgnoreCase(name = file.getName(), ".xml"))
            name = name.substring(0, name.length() - 4);

        return getAvailableCustomThemeName(name);
    }

    private static boolean isNameAvailable(String name, Iterator names) {
        while(names.hasNext())
            if(names.next().equals(name))
                return false;
        return true;
    }

    private static String getAvailableCustomThemeName(String name) {
        Vector names;
        int    i;
        String buffer;

        names = getAvailableThemeNames();

        // If the name is available, no need to suffix it with (xx).
        if(isNameAvailable(name, names.iterator()))
            return name;

        // Removes any trailing (x) construct, and adds a trailing space if necessary.
        name = name.replaceFirst("\\([0-9]+\\)$", "");
        if(name.charAt(name.length() - 1) != ' ')
            name = name + ' ';

        i = 1;
        do {buffer = name + '(' + (++i) + ')';}            
        while(!isNameAvailable(buffer, names.iterator()));

        return buffer;
    }

    public static Theme duplicateTheme(Theme theme) throws IOException, Exception {return importTheme(theme.cloneData(), theme.getName());}

    public static Theme importTheme(ThemeData data, String name) throws IOException, Exception {
        writeTheme(data, Theme.CUSTOM_THEME, name = getAvailableCustomThemeName(name));
        return new Theme(listener, data, Theme.CUSTOM_THEME, name);
    }

    public static Theme importTheme(File file) throws IOException, Exception {
        String       name; // Name of the new theme.
        OutputStream out;  // Where to write the theme data to.
        InputStream  in;   // Where to read the theme data from.
        ThemeData    data;

        // Makes sure the file contains a valid theme.
        data = readThemeData(file);

        // Initialisation.
        name = getAvailableCustomThemeName(file);
        out  = null;
        in   = null;

        // Imports the theme.
        try {copyStreams(in = new FileInputStream(file), out = getCustomThemeOutputStream(name));}

        // Cleanup.
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }

        return new Theme(listener, data, Theme.CUSTOM_THEME, name);
    }



    // - Theme reading -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns an input stream on the user theme.
     * @return             an input stream on the user theme.
     * @throws IOException if an I/O related error occurs.
     */
    private static InputStream getUserThemeInputStream() throws IOException {
        return new BackupInputStream(getUserThemeFile());
    }

    /**
     * Returns an input stream on the requested predefined theme.
     * @param  name        name of the predefined theme on which to open an input stream.
     * @return             an input stream on the requested predefined theme.
     * @throws IOException if an I/O related error occurs.
     */
    private static InputStream getPredefinedThemeInputStream(String name) throws IOException {
        return ResourceLoader.getResourceAsStream(RuntimeConstants.THEMES_PATH + "/" + name + ".xml");
    }

    /**
     * Returns an input stream on the requested custom theme.
     * @param  name        name of the custom theme on which to open an input stream.
     * @return             an input stream on the requested custom theme.
     * @throws IOException if an I/O related error occurs.
     */
    private static InputStream getCustomThemeInputStream(String name) throws IOException {
        return new BackupInputStream(getCustomThemesFolder().getChild(name + ".xml"));
    }

    /**
     * Opens an input stream on the requested theme.
     * <p>
     * This method is just a convenience, and wraps calls to {@link #getUserThemeInputStream()},
     * {@link #getPredefinedThemeInputStream(String)} and {@link #getCustomThemeInputStream(String)}.
     * </p>
     * @param  type                     type of the theme to open an input stream on.
     * @param  name                     name of the theme to open an input stream on.
     * @return                          an input stream opened on the requested theme.
     * @throws IOException              thrown if an IO related error occurs.
     * @throws IllegalArgumentException thrown if <code>type</code> is not a legal theme type.
     */
    private static InputStream getInputStream(int type, String name) throws IOException {
        switch(type) {
            // User theme.
        case Theme.USER_THEME:
            return getUserThemeInputStream();

            // Predefined theme.
        case Theme.PREDEFINED_THEME:
            return getPredefinedThemeInputStream(name);

            // Custom theme.
        case Theme.CUSTOM_THEME:
            return getCustomThemeInputStream(name);
        }

        // Error handling.
        throw new IllegalArgumentException("Illegal theme type: " + type);
    }

    /**
     * Returns the requested theme.
     * @param  type type of theme to retrieve.
     * @param  name name of the theme to retrieve.
     * @return the requested theme.
     */
    public static Theme readTheme(int type, String name) throws Exception {
        ThemeData   data; // Buffer for the theme data.
        InputStream in;   // Where to read the theme from.

        // Do not reload the current theme, both for optimisation purposes and because
        // it might cause user theme modifications to be lost.
        if(currentTheme != null && isCurrentTheme(type, name))
            return currentTheme;

        // Reads the theme data.
        in = null;
        try {data = readThemeData(in = getInputStream(type, name));}
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
        }

        // Creates the corresponding theme.
        return new Theme(listener, data, type, name);
    }

    /**
     * Reads theme data from the specified input stream.
     * @param  in        where to read the theme data from.
     * @return           the resulting theme data.
     * @throws Exception if an I/O or syntax error occurs.
     */
    public static ThemeData readThemeData(InputStream in) throws Exception {
        ThemeData data; // Buffer for the data.

        // Reads the theme data.
        ThemeReader.read(in, data = new ThemeData());

        return data;
    }

    /**
     * Reads theme data from the specified file.
     * @param  file      where to read the theme data from.
     * @return           the resulting theme data.
     * @throws Exception if an I/O or syntax error occurs.
     */
    public static ThemeData readThemeData(File file) throws Exception {
        InputStream in; // InputStream on file.

        in = null;

        // Loads the theme data.
        try {return readThemeData(in = new FileInputStream(file));}

        // Cleanup.
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
        }
    }



    // - Current theme access ------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    private static void setConfigurationTheme(int type, String name) {
        // Sets configuration depending on the new theme's type.
        switch(type) {
            // User defined theme.
        case Theme.USER_THEME:
            MuConfiguration.setVariable(MuConfiguration.THEME_TYPE, MuConfiguration.THEME_USER);
            MuConfiguration.setVariable(MuConfiguration.THEME_NAME, null);
            break;

            // Predefined themes.
        case Theme.PREDEFINED_THEME:
            MuConfiguration.setVariable(MuConfiguration.THEME_TYPE, MuConfiguration.THEME_PREDEFINED);
            MuConfiguration.setVariable(MuConfiguration.THEME_NAME, name);
            break;

            // Custom themes.
        case Theme.CUSTOM_THEME:
            MuConfiguration.setVariable(MuConfiguration.THEME_TYPE, MuConfiguration.THEME_CUSTOM);
            MuConfiguration.setVariable(MuConfiguration.THEME_NAME, name);
            break;

            // Error.
        default:
            throw new IllegalStateException("Illegal theme type: " + type);
        }
    }

    /**
     * Sets the specified theme as the current theme in configuration.
     * @param theme theme to set as current.
     */
    private static void setConfigurationTheme(Theme theme) {setConfigurationTheme(theme.getType(), theme.getName());}


    /**
     * Saves the current theme if necessary.
     */
    public static void saveCurrentTheme() throws IOException {
        // Makes sure no NullPointerException is raised if this method is called
        // before themes have been initialised.
        if(currentTheme == null)
            return;

        // Saves the user theme if it's the current one.
        if(currentTheme.getType() == Theme.USER_THEME && wasUserThemeModified) {
            writeTheme(currentTheme);
            wasUserThemeModified = false;
        }
    }

    public static Theme getCurrentTheme() {return currentTheme;}

    /**
     * Changes the current theme.
     * <p>
     * This method will change the current theme and trigger all the proper events.
     * </p>
     * @param  theme                    theme to use as the current theme.
     * @throws IllegalArgumentException thrown if the specified theme could not be loaded.
     */
    public synchronized static void setCurrentTheme(Theme theme) {
        Theme oldTheme;

        // Makes sure we're not doing something useless.
        if(isCurrentTheme(theme))
            return;

        // Saves the current theme if necessary.
        try {saveCurrentTheme();}
        catch(IOException e) {if(Debug.ON) Debug.trace("Couldn't save current theme: " + e);}

        // Updates muCommander's configuration.
        oldTheme = currentTheme;
        setConfigurationTheme(currentTheme = theme);

        // Triggers the events generated by the theme change.
        triggerThemeChange(oldTheme, currentTheme);
    }

    public synchronized static Font getCurrentFont(int id) {return currentTheme.getFont(id);}

    public synchronized static Color getCurrentColor(int id) {return currentTheme.getColor(id);}

    public synchronized static Theme overwriteUserTheme(ThemeData themeData) throws IOException {
        // If the current theme is the user one, we just need to import the new data.
        if(currentTheme.getType() == Theme.USER_THEME) {
            currentTheme.importData(themeData);
            writeTheme(currentTheme);
            return currentTheme;
        }

        else {
            writeTheme(themeData, Theme.USER_THEME, null);
            return new Theme(listener, themeData);
        }
    }

    /**
     * Checks whether setting the specified font would require overwriting of the user theme.
     * @param  fontId identifier of the font to set.
     * @param  font   value for the specified font.
     * @return        <code>true</code> if applying the specified font will overwrite the user theme,
     *                <code>false</code> otherwise.
     */
    public synchronized static boolean willOverwriteUserTheme(int fontId, Font font) {
        if(currentTheme.isFontDifferent(fontId, font))
            return currentTheme.getType() != Theme.USER_THEME;
        return false;
    }

    /**
     * Checks whether setting the specified color would require overwriting of the user theme.
     * @param  colorId identifier of the color to set.
     * @param  color   value for the specified color.
     * @return         <code>true</code> if applying the specified color will overwrite the user theme,
     *                 <code>false</code> otherwise.
     */
    public synchronized static boolean willOverwriteUserTheme(int colorId, Color color) {
        if(currentTheme.isColorDifferent(colorId, color))
            return currentTheme.getType() != Theme.USER_THEME;
        return false;
    }

    /**
     * Updates the current theme with the specified font.
     * <p>
     * This method might require to overwrite the user theme: custom and predefined themes are
     * read only. In order to modify them, the ThemeManager must overwrite the user theme with
     * the current theme and then set the font.<br/>
     * If necessary, this can be checked beforehand by a call to {@link #willOverwriteUserTheme(int,Font)}.
     * </p>
     * @param  id   identifier of the font to set.
     * @param  font font to set.
     */
    public synchronized static boolean setCurrentFont(int id, Font font) {
        // Only updates if necessary.
        if(currentTheme.isFontDifferent(id, font)) {
            // Checks whether we need to overwrite the user theme to perform this action.
            if(currentTheme.getType() != Theme.USER_THEME) {
                currentTheme.setType(Theme.USER_THEME);
                setConfigurationTheme(currentTheme);
            }

            currentTheme.setFont(id, font);
            return true;
        }
        return false;
    }

    /**
     * Updates the current theme with the specified color.
     * <p>
     * This method might require to overwrite the user theme: custom and predefined themes are
     * read only. In order to modify them, the ThemeManager must overwrite the user theme with
     * the current theme and then set the color.<br/>
     * If necessary, this can be checked beforehand by a call to {@link #willOverwriteUserTheme(int,Color)}.
     * </p>
     * @param  id   identifier of the color to set.
     * @param  color color to set.
     */
    public synchronized static boolean setCurrentColor(int id, Color color) {
        // Only updates if necessary.
        if(currentTheme.isColorDifferent(id, color)) {
            // Checks whether we need to overwrite the user theme to perform this action.
            if(currentTheme.getType() != Theme.USER_THEME) {
                currentTheme.setType(Theme.USER_THEME);
                setConfigurationTheme(currentTheme);
            }

            // Updates the color and notifies listeners.
            currentTheme.setColor(id, color);
            return true;
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the specified theme is the current one.
     * @param theme theme to check.
     * @return <code>true</code> if the specified theme is the current one, <code>false</code> otherwise.
     */
    public static boolean isCurrentTheme(Theme theme) {return theme == currentTheme;}

    private static boolean isCurrentTheme(int type, String name) {
        if(type != currentTheme.getType())
            return false;
        if(type == Theme.USER_THEME)
            return true;
        return name.equals(currentTheme.getName());
    }




    // - Events management ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Notifies all listeners that the current theme has changed.
     * <p>
     * This method is meant to be called when the current theme has been changed.
     * It will compare all fonts and colors in <code>oldTheme</code> and <code>newTheme</code> and,
     * if any is found to be different, trigger the corresponding event.
     * </p>
     * <p>
     * At the end of this method, all registered listeners will have been made aware of the new values
     * they should be using.
     * </p>
     * @param oldTheme previous current theme.
     * @param newTheme new current theme.
     * @see            #triggerFontEvent(FontChangedEvent)
     * @see            #triggerColorEvent(ColorChangedEvent)
     */
    private static void triggerThemeChange(Theme oldTheme, Theme newTheme) {
        // Triggers font events.
        for(int i = 0; i < Theme.FONT_COUNT; i++)
            if(oldTheme.isFontDifferent(i, newTheme.getFont(i)))
                triggerFontEvent(new FontChangedEvent(currentTheme, i, newTheme.getFont(i)));

        // Triggers color events.
        for(int i = 0; i < Theme.COLOR_COUNT; i++)
            if(oldTheme.isColorDifferent(i, newTheme.getColor(i)))
                triggerColorEvent(new ColorChangedEvent(currentTheme, i, newTheme.getColor(i)));
    }

    /**
     * Adds the specified object to the list of registered current theme listeners.
     * <p>
     * Any object registered through this method will received {@link ThemeListener#colorChanged(ColorChangedEvent) color}
     * and {@link ThemeListener#fontChanged(FontChangedEvent) font} events whenever the current theme changes.
     * </p>
     * <p>
     * Note that these events will not necessarily be fired as a result of a direct theme change: if, for example,
     * the current theme is using look&amp;feel dependant values and the current look&amp;feel changes, the corresponding
     * events will be passed to registered listeners.
     * </p>
     * <p>
     * Listeners are stored as weak references, to make sure that the API doesn't keep ghost copies of objects
     * whose usefulness is long since past. This forces callers to make sure they keep a copy of the listener's instance: if
     * they do not, the instance will be weakly linked and garbage collected out of existence.
     * </p>
     * @param listener new current theme listener.
     */
    public static void addCurrentThemeListener(ThemeListener listener) {synchronized (listeners) {listeners.put(listener, null);}}

    /**
     * Removes the specified object from the list of registered theme listeners.
     * <p>
     * Note that since listeners are stored as weak references, calling this method is not strictly necessary. As soon
     * as a listener instance is not referenced anymore, it will automatically be caught and destroyed by the garbage
     * collector.
     * </p>
     * @param listener current theme listener to remove.
     */
    public static void removeCurrentThemeListener(ThemeListener listener) {synchronized (listeners) {listeners.remove(listener);}}

    /**
     * Notifies all theme listeners of the specified font event.
     * @param event event to pass down to registered listeners.
     * @see         #triggerThemeChange(Theme,Theme)
     */
    private static void triggerFontEvent(FontChangedEvent event) {
        Iterator iterator;

        synchronized (listeners) {
            iterator = listeners.keySet().iterator();
            while(iterator.hasNext())
                ((ThemeListener)iterator.next()).fontChanged(event);
        }
    }

    /**
     * Notifies all theme listeners of the specified color event.
     * @param event event to pass down to registered listeners.
     * @see         #triggerThemeChange(Theme,Theme)
     */
    private static void triggerColorEvent(ColorChangedEvent event) {
        Iterator iterator;

        synchronized (listeners) {
            iterator = listeners.keySet().iterator();
            while(iterator.hasNext())
                ((ThemeListener)iterator.next()).colorChanged(event);
        }
    }



    // - Helper methods ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns a valid type identifier from the specified configuration type definition.
     * @param  label label of the theme type as defined in {@link com.mucommander.conf.MuConfiguration}.
     * @return       a valid theme type identifier.
     */
    private static int getThemeTypeFromLabel(String label) {
        if(label.equals(MuConfiguration.THEME_USER))
            return Theme.USER_THEME;
        else if(label.equals(MuConfiguration.THEME_PREDEFINED))
            return Theme.PREDEFINED_THEME;
        else if(label.equals(MuConfiguration.THEME_CUSTOM))
            return Theme.CUSTOM_THEME;
        throw new IllegalStateException("Unknown theme type: " + label);
    }

    private static String getThemeName(AbstractFile themeFile) {
        return themeFile.getNameWithoutExtension();
    }



    // - Listener methods ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * @author Nicolas Rinaudo
     */
    private static class CurrentThemeListener implements ThemeListener {
        public void fontChanged(FontChangedEvent event) {
            if(event.getSource().getType() == Theme.USER_THEME)
                wasUserThemeModified = true;

            if(event.getSource() == currentTheme)
                triggerFontEvent(event);
        }

        public void colorChanged(ColorChangedEvent event) {
            if(event.getSource().getType() == Theme.USER_THEME)
                wasUserThemeModified = true;

            if(event.getSource() == currentTheme)
                triggerColorEvent(event);
        }
    }
}
