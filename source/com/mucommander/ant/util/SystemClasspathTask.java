/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.ant.util;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.util.StringTokenizer;

/**
 * Ant task meant to extract specific libraries from a given path.
 * @author Nicolas Rinaudo
 * @ant.task name="libpath" category="util"
 */
public class SystemClasspathTask extends Task {
    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Name(s) of the library we're looking for. */
    private String library;
    /** Path in which to look for the library. */
    private String path;
    /** Property in which to store the approved path. */
    private String antProperty;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Creates a new <code>SystemClasspathTask</code> instance.
     */
    public SystemClasspathTask() {}

    /**
     * Initialises the task.
     */
    public void init() {
        antProperty = null;
        path        = null;
    }


    // - Ant interaction -------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Sets the name(s) of the library to look for.
     * <p>
     * Some libraries have a different name depending on the OS. For example, Sun's <code>rt.jar</code>
     * is called <code>classes.jar</code> under MAC OS X. For these libraries, it's possible to specify
     * more than one name by separating each name by <code>:</code>.
     * </p>
     * @param        s name(s) of the library to look for.
     * @ant.required
     */
    public void setLibrary(String s) {library = s;}

    /**
     * Sets the name of the property in which to store the full library path.
     * @param        s name of the property in which to store the full library path.
     * @ant.required
     */
    public void setProperty(String s) {antProperty = s;}

    /**
     * Sets the path in which to look for the library.
     * <p>
     * This can be one or more files. If more than one file is specified, each path must be separated
     * by <code>path.separator</code>.
     * </p>
     * @param            s path in which to look for the library.
     * @ant.not-required   Defaults to the value of system property <code>sun.boot.class.path</code>.
     */
    public void setPath(String s) {path = s;}

    /**
     * Runs the task.
     * @throws BuildException if an error occurs.
     */
    public void execute() throws BuildException {
        StringTokenizer parser; // Used to parse the boot class path.
        File            file;
        String[]        names;

        // Makes sure that the output property has been set.
        if(antProperty == null)
            throw new BuildException("Unspecified output property - please fill in the property attribute.");
        if(path == null)
            path = System.getProperty("sun.boot.class.path");
        if(library == null)
            throw new BuildException("Unspecified library property - please fill in the library attribute.");

        parser = new StringTokenizer(path, System.getProperty("path.separator"));
        names  = library.split(":");
        while(parser.hasMoreTokens()) {
            file = new File(parser.nextToken());
            if(file.exists()) {
                for(int i = 0; i < names.length; i++) {
                    if(names[i].equalsIgnoreCase(file.getName())) {
                        getProject().setNewProperty(antProperty, file.getAbsolutePath());
                        return;
                    }
                }
            }
        }
    }
}
