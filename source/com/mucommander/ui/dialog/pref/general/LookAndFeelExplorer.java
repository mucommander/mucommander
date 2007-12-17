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

package com.mucommander.ui.dialog.pref.general;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractFileClassLoader;
import com.mucommander.file.filter.FileFilter;
import com.mucommander.file.filter.ExtensionFilenameFilter;
import com.mucommander.file.filter.OrFileFilter;
import com.mucommander.file.filter.AttributeFileFilter;

import java.lang.reflect.Modifier;
import java.lang.reflect.Constructor;
import java.util.Vector;
import java.io.IOException;
import javax.swing.LookAndFeel;

/**
 * Explores {@link AbstractFile} instances and looks for valid look and feels.
 * <p>
 * A look and feel is said to be legal if:
 * <ul>
 *   <li>It can be loaded.</li>
 *   <li>It extends <code>javax.swing.LookAndFeel.</li>
 *   <li>It has a public, no-arg constructor.</li>
 *   <li>Its <code>isSupportedLookAndFeel()</code> method returns <code>true</code>.</li>
 * </ul>
 * </p>
 * @author Nicolas Rinaudo
 */
class LookAndFeelExplorer {
    // - Instance fields -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** ClassLoader used to load classes from explored files. */
    private AbstractFileClassLoader loader;
    /** Used to filter out files that are neither classes nor directories. */
    private OrFileFilter            filter;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new look and feel explorer.
     */
    public LookAndFeelExplorer() {
        filter = new OrFileFilter();
        filter.addFileFilter(new ExtensionFilenameFilter(".class"));
        filter.addFileFilter(new AttributeFileFilter(AttributeFileFilter.DIRECTORY));
    }



    // - File exploring ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Makes sure the specified class is a valid look and feel for this system.
     * @param  c class to check.
     * @return   <code>true</code> if <code>c</code> is a valid look and feel, <code>false</code> otherwise.
     */
    private static boolean isLookAndFeel(Class c) {
        int         modifiers;   // Class' modifiers.
        Constructor constructor; // Public, no-arg constructor.
        Class       buffer;      // Used to explore c's ancestors.

        // Makes sure the class is public and non abstract.
        modifiers = c.getModifiers();
        if(!Modifier.isPublic(modifiers) || Modifier.isAbstract(modifiers))
            return false;

        // Makes sure the class has a public, no-arg constructor.
        try {constructor = c.getDeclaredConstructor(new Class[0]);}
        catch(Exception e) {return false;}
        if(!Modifier.isPublic(constructor.getModifiers()))
            return false;

        // Makes sure the class extends javax.swing.LookAndFeel and that if it does,
        // it's supported by the system.
        buffer = c;
        while(buffer != null) {
            // c is a LookAndFeel, makes sure it's supported.
            if(buffer.equals(LookAndFeel.class)) {
                try {return ((LookAndFeel)c.newInstance()).isSupportedLookAndFeel();}
                catch(Throwable e) {e.printStackTrace();return false;}
            }
            buffer = buffer.getSuperclass();
        }
        return false;
    }

    /**
     * Explores the content of the specified file.
     * @param currentPackage name of the current package (with a trailing .).
     * @param currentfile    file being explored.
     * @param lookAndFeels   buffer for all legal look and feels.
     * @throws IOException   if <code>currentFile</code> could not be accessed.
     */
    private void explore(String currentPackage, AbstractFile currentFile, Vector lookAndFeels) throws IOException {
        AbstractFile[] files;        // All subfolders or child class files of currentFile.
        Class          currentClass; // Buffer for the current class.

        files = currentFile.ls(filter);
        for(int i = 0; i < files.length; i++) {
            // Explores subdirectories recursively.
            if(files[i].isDirectory())
                explore(currentPackage + files[i].getName() + '.', files[i], lookAndFeels);

            // Checks whether class files are valid look and feels.
            // Errors are treated as meaning 'not a look and feel'.
            else {
                try {
                    if(isLookAndFeel(currentClass = Class.forName(currentPackage + files[i].getNameWithoutExtension(), false, loader)))
                        lookAndFeels.add(currentClass.getName());
                }
                catch(Throwable e) {}
            }
        }
    }



    // - Public code ---------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Explores the content of the specified file and looks for look and feels.
     * @param  jar         file to explore.
     * @return             the names of all discovered valid look and feels.
     * @throws IOException if an error occured while exploring <code>jar</code>
     */
    public Vector explore(AbstractFile jar) throws IOException {
        Vector lookAndFeels; // Buffer for all valid look and feels.

        // Ignore non-browsable files.
        if(!jar.isBrowsable())
            return new Vector();

        // Initialises exploring.
        lookAndFeels = new Vector();
        loader       = new AbstractFileClassLoader();
        loader.addFile(jar);

        // Looks for all look and feels in jar.
        explore("", jar, lookAndFeels);

        return lookAndFeels;
    }
}
