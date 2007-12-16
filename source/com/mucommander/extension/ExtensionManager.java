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

package com.mucommander.extension;

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractFileClassLoader;
import com.mucommander.file.FileFactory;
import com.mucommander.file.filter.OrFileFilter;
import com.mucommander.file.filter.ExtensionFilenameFilter;

import java.io.IOException;
import java.io.File;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Manages muCommander's extensions.
 * @author Nicolas Rinaudo
 */
public class ExtensionManager {
    private static AbstractFileClassLoader loader;

    // - Extensions folder ------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Path to the extensions folder. */
    private static       AbstractFile            extensionsFolder;
    /** Default name of the extensions folder. */
    public  static final String                  DEFAULT_EXTENSIONS_FOLDER_NAME = "extensions";



    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
    static {
        ClassLoader temp;
        if((temp = ClassLoader.getSystemClassLoader()) instanceof AbstractFileClassLoader)
            loader = (AbstractFileClassLoader)temp;
        else
            loader = new AbstractFileClassLoader();
    }

    /**
     * Prevents instanciations of this class.
     */
    private ExtensionManager() {}



    // - Extension folder access ------------------------------------------------
    // --------------------------------------------------------------------------
    public static void setExtensionsFolder(File folder) throws IOException {setExtensionsFolder(FileFactory.getFile(folder.getAbsolutePath()));}

    public static void setExtensionsFolder(AbstractFile folder) throws IOException {
        if(!folder.exists())
            folder.mkdir();
        else if(!folder.isBrowsable())
            folder = folder.getParent();
        extensionsFolder = folder;
    }

    public static void setExtensionsFolder(String path) throws IOException {
        AbstractFile folder;

        if((folder = FileFactory.getFile(path)) == null)
            setExtensionsFolder(new File(path));
        else
            setExtensionsFolder(FileFactory.getFile(path));
    }

    private static AbstractFile getDefaultExtensionsFolder() throws IOException {
        AbstractFile folder;

        folder = PlatformManager.getPreferencesFolder().getChild(DEFAULT_EXTENSIONS_FOLDER_NAME);

        // Makes sure the folder exists.
        if(!folder.exists())
            folder.mkdir();

        return folder;
    }

    public static AbstractFile getExtensionsFolder() throws IOException {
        // If the extensions folder has been set, use it.
        if(extensionsFolder != null)
            return extensionsFolder;

        return getDefaultExtensionsFolder();
    }



    // - Classpath extension ----------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Adds the specified file to the extension's classpath.
     * @param file file to add to the classpath.
     */
    public static void addToClassPath(AbstractFile file) {loader.addFile(file);}

    /**
     * Adds all known extensions to the current classpath.
     * <p>
     * This method will create the following new classpath entries:
     * <ul>
     *   <li>{@link #getExtensionsFolder()}</li>.
     *   <li>All <code>JAR</code> files in {@link #getExtensionsFolder()}.</li>
     * </ul>
     * </p>
     * @throws IOException if the extensions folder is not accessible.
     */
    public static void addExtensionsToClasspath() throws IOException {
        AbstractFile[] files;

        // Adds the extensions folder to the classpath.
        addToClassPath(getExtensionsFolder());

        // Adds all JAR files contained by the extensions folder to the classpath.
        files = getExtensionsFolder().ls(new ExtensionFilenameFilter(".jar"));
        for(int i = 0; i < files.length; i++)
            addToClassPath(files[i]);
    }

    public static ClassLoader getClassLoader() {return loader;}
}
