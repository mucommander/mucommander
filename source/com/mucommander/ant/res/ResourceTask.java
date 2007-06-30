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

package com.mucommander.ant.res;

import com.mucommander.res.ResourceListWriter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.*;
import java.util.Iterator;
import java.util.Vector;

/**
 * Task used to generate resources list.
 * <p>
 * Java doesn't allow developers to dynamically explore the content of their
 * applications' JAR files, and they traditionaly have had to resort to rather
 * untidy techniques, such as figuring out the path of the JAR somehow and opening
 * a ZipInputStream on it.
 * </p>
 * <p>
 * The goal of this task is to list resources in an XML file which can be opened
 * through <code>Class.getResourceAsStream(String)</code>. This way, developers do
 * not have to rely on system dependent solutions: they can open the XML file, explore
 * its content and extract the path of all the resources it describes.
 * </p>
 * @ant.task name="mkresources" type="util"
 * @author Nicolas Rinaudo
 */
public class ResourceTask extends Task {
    // - Instance fields -------------------------------------------------
    // -------------------------------------------------------------------
    /** Path to the file in which the description will be written. */
    private File   outputFile;
    /** Path to the directory in which all the resources added to the task will be copied. */
    private File   outputDirectory;
    /** List of all the resources that should be copied and listed by this task. */
    private Vector resources;



    // - Initialisation --------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Initialises the the task.
     */
    public void init() {
        outputFile      = null;
        outputDirectory = null;
        resources       = new Vector();
    }



    // - Ant interaction -------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Sets the path to the file in which the resource list will be written.
     * @param out path to the file in which the resource list will be written.
     */
    public void setOutFile(File out) {outputFile = out;}

    /**
     * Sets the path to the directory in which to copy all of the resources.
     * @param out path to the directory in which to copy all of the resources.
     */
    public void setOutDir(File out) {outputDirectory = out;}

    /**
     * Used to let Ant manage the creation of resource files.
     */
    public ResourceFile createFile() {
        ResourceFile file;

        file = new ResourceFile();
        resources.add(file);

        return file;
    }



    // - Main code -------------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Executes the resource task.
     */
    public void execute() throws BuildException {
        ResourceListWriter out;      // Used to write the resource list XML file.
        Iterator           iterator; // Iterator on all the resources.
        ResourceFile       file;     // File we're currently working with.

        // Makes sure the task was properly initialised.
        if(outputFile == null)
            throw new BuildException("No output file was specified, please fill in the 'outfile' attribute.");
        if(outputDirectory == null)
            throw new BuildException("No output directory was specified, please fill in the 'outdir' attribute.");

        out = null;
        try {
            // Opens the resource list.
            out = new ResourceListWriter(outputFile);
            out.startList();

            // Adds each resource element to both the destination and the list.
            iterator = resources.iterator();
            while(iterator.hasNext()) {
                file = (ResourceFile)iterator.next();
                out.addFile(file.getOutputPath());
                copy(file.getInputFile(), new File(outputDirectory, file.getOutputPath()));
            }
            out.endList();
        }
        catch(Exception e) {throw new BuildException(e);}
        // Makes sure streams are closed.
        finally {
            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }
    }



    // - Misc. -----------------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Copies file <code>from</code> to file <code>to</code>.
     * @param  from        file to copy.
     * @param  to          where to copy the file.
     * @throws IOException thrown if any IO related error occurs.
     */
    private static void copy(File from, File to) throws IOException {
        InputStream  in;     // Input stream on from.
        OutputStream out;    // Output stream on to.
        byte[]       buffer; // Buffer for chunks of from
        int          count;  // Number bytes read in the last read operation.

        in  = null;
        out = null;
        try {
            // Makes sure the necessary directories have been created.
            to.getParentFile().mkdirs();

            // Initialises copy.
            in     = new FileInputStream(from);
            out    = new FileOutputStream(to);
            buffer = new byte[1024];

            // Copies the content of in to out.
            while((count = in.read(buffer)) != -1)
                out.write(buffer, 0, count);
        }
        // Makes sure streams are closed.
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
    }
}
