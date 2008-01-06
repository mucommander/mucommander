/*
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

package com.mucommander.ant.util;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.util.StringTokenizer;

/**
 * Ant task meant to extract the rt.jar file from a path.
 * @author Nicolas Rinaudo
 * @ant.task name="libpath" category="util"
 */
public class SystemClasspathTask extends Task {
    private String path;
    /** Property in which to store the approved path. */
    private String antProperty;

    public SystemClasspathTask() {}

    public void init() {
        antProperty = null;
        path        = null;
    }

    public void setProperty(String s) {antProperty = s;}
    public void setPath(String s) {path = s;}

    public void execute() throws BuildException {
        StringTokenizer parser; // Used to parse the boot class path.
        File            file;   // Current JAR file.

        // Makes sure that the output property has been set.
        if(antProperty == null)
            throw new BuildException("Unspecified output property - please fill in the out attribute.");
        if(path == null)
            path = System.getProperty("sun.boot.class.path");

        parser = new StringTokenizer(path, System.getProperty("path.separator"));
        while(parser.hasMoreTokens()) {
            file = new File(parser.nextToken());

            // If we've found a file that exists and has a legal 'rt.jar' name, use that.
            if(file.exists() && (file.getName().equalsIgnoreCase("rt.jar") || file.getName().equalsIgnoreCase("classes.jar"))) {
                getProject().setNewProperty(antProperty, file.getAbsolutePath());
                return;
            }
        }
    }
}
