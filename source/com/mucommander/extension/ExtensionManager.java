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
import com.mucommander.file.filter.DirectoryFileFilter;

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
    // - Extensions folder ------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Path to the extensions folder. */
    private static       AbstractFile            extensionsFolder;
    /** Default name of the extensions folder. */
    public  static final String                  DEFAULT_EXTENSIONS_FOLDER_NAME = "extensions";



    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
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
        File folder;

        // Makes sure we get the canonical path
        // (for 'dirty hacks' such as ./mucommander.sh/../.mucommander)
        try {setExtensionsFolder(new File(path).getCanonicalFile());}
        catch(Exception e) {throw new IllegalArgumentException(e);}
    }

    private static AbstractFile getDefaultExtensionsFolder() throws IOException {
        AbstractFile folder;

        //        folder = FileFactory.getFile(DEFAULT_EXTENSIONS_FOLDER_NAME, PlatformManager.getPreferencesFolder());
        folder = FileFactory.getFile(new File(PlatformManager.getPreferencesFolder(), DEFAULT_EXTENSIONS_FOLDER_NAME).getAbsolutePath());

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
     * Adds the specified file to the current classpath.
     * <p>
     * This method does its best to modify the current classpath, but this isn't always possible. Either of
     * the following conditions must be met for the method to be succesfull:
     * <ul>
     *   <li>
     *     The system classloader as returned by <code>ClassLoader.getSystemClassLoader()</code> is an instance
     *     of {@link AbstractFileClassLoader}. This can be achieved by setting the system property
     *     <code>java.system.class.loader</code> to <code>com.mucommander.file.AbstractFileClassLoader</code>
     *     when starting the Java VM.
     *   </li>
     *   <li>
     *     The system classloader as returned by <code>ClassLoader.getSystemClassLoader()</code> is an instance
     *     of <code>URLClassLoader</code> and no security manager prevents us from overriding its <code>addURL</code>
     *     method's protection.
     *   </li>
     * </ul>
     * </p>
     * @param  file                  file to add to the classpath.
     * @throws IllegalStateException if the VM is not in a state that allows this method to work.
     * @throws MalformedURLException if the specified <code>file</code> couldn't be converted to a <code>java.net.URL</code>.
     */
    public static void addToClassPath(AbstractFile file) throws IllegalStateException, MalformedURLException {
        ClassLoader loader;

        loader = ClassLoader.getSystemClassLoader();

        // If the system classloader is an AbstractFileClassLoader, we can just add the file.
        if(loader instanceof AbstractFileClassLoader)
            ((AbstractFileClassLoader)loader).addFile(file);

        // If the system classloader is an URLClassLoader, we need to override the addURL method's
        // protection.
        else if(loader instanceof URLClassLoader) {
            try {
                Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
                method.setAccessible(true);
                method.invoke(loader, new Object[]{file.getJavaNetURL()});
            }
            // We couldn't override addURL's protection, the JVM is not in a state that allows this method
            // to work.
            catch(IllegalAccessException e) {throw new IllegalStateException();}

            // Ignore these errors as they cannot happen.
            catch(NoSuchMethodException e) {}
            catch(InvocationTargetException e) {}
        }

        // The JVM is not in a state that allows this method to work.
        else
            throw new IllegalStateException();
    }

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
     * @throws IllegalStateException if the JVM is not in a state that allows us to modify the classpath at runtime.
     * @throws MalformedURLException if any of the new classpath entries couldn't be converted to a <code>java.net.URL</code>.
     */
    public static void addExtensionsToClasspath() throws IOException, IllegalStateException, MalformedURLException {
        AbstractFile[] files;

        // Adds the extensions folder to the classpath.
        addToClassPath(getExtensionsFolder());

        // Adds all JAR files contained by the extensions folder to the classpath.
        files = getExtensionsFolder().ls(new ExtensionFilenameFilter(".jar"));
        for(int i = 0; i < files.length; i++)
            addToClassPath(files[i]);
    }
}
