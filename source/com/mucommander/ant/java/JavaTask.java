package com.mucommander.ant.java;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

/**
 * Ant task used to write dynamic Java constants.
 * <p>
 * Checks tools documentation for more information on how to use this task.
 * </p>
 * @author Nicolas Rinaudo (nicolas@nrinaudo.com)
 */
public class JavaTask extends Task {
    // - Instance fields -------------------------------------------------
    // -------------------------------------------------------------------
    /** Fully qualified name of the Java file in which constants should be written. */
    private String name;
    /** List of all the fields that should be written to the Java file. */
    private Vector fields;


    // - Initialization --------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Builds a new Java task.
     */
    public JavaTask() {}

    /**
     * Resets the task, in case of instance re-use.
     */
    public void init() {
        name   = null;
        fields = new Vector();
    }


    // - Error checking --------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Checks wether the task was properly initialized.
     * @exception BuildException thrown if the task was not properly initialized.
     */
    private void check() throws BuildException {
        Iterator iterator;

        // Checks wether the Java file name was specified.
        if(name == null)
            throw new BuildException("No java file name specified. Fill in the 'name' attribute.");

        // Checks each individual field to make sure they were properly initialized.
        iterator = fields.iterator();
        while(iterator.hasNext())
            ((JavaField)iterator.next()).check();
    }


    // - Ant interaction -------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Sets the name of the Java file in which to write.
     * @param value name of the Java file in which to write.
     */
    public void setName(String value) {name = value;}

    /**
     * Adds a field to the Java file.
     * @return an instance of JavaField for Ant to use.
     */
    public JavaField createField() {
        JavaField field;

        field = new JavaField();
        fields.add(field);

        return field;
    }

    /**
     * Executes the task.
     * @exception BuildException thrown if any error occurs.
     */
    public void execute() throws BuildException {
        JavaWriter out;      // Where to write the Java constants.
        Iterator   iterator; // Iterator on the Java fields.

        // Makes sure the task was properly initialized.
        check();

        try {
            // Opens the output.
            out = openJavaOutput();

            // Writes every field to the java file.
            iterator = fields.iterator();
            while(iterator.hasNext())
                ((JavaField)iterator.next()).print(out);

            // Cleaning-up
            out.printClassFooter();
            out.close();
        }
        catch(Exception e) {throw new BuildException(e);}
    }


    // - Misc. -----------------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Opens a java output stream on the requested file.
     * @return    a java output stream on the requested file.
     * @exception IOException thrown if the output stream could not be opened.
     */
    private JavaWriter openJavaOutput() throws IOException {
        int        index;       // Index of the last / character in name.
        File       root;        // Directory in which to create the package and java file.
        String     packageName; // Name of the package in which the class will be writen.
        String     className;   // Name of the class in which the constants will be writen.
        JavaWriter writer;      // Java writer that will be returned by this method.

        // Extracts the path and class name parts of the java file name.
        index = name.lastIndexOf('/');
        if(index == -1)
            root = new File(System.getProperty("user.dir"));
        else {
            root     = new File(name.substring(0, index));
            name = name.substring(index + 1, name.length());
        }

        // Makes sure the directory structure to the class exists
        packageName = JavaWriter.getPackageName(name);
        root.mkdirs();
        if(packageName != null)
            JavaWriter.getPathToPackage(root, packageName).mkdirs();

        // Extracts the class name from the package
        className = JavaWriter.getClassName(name);

        // Opens the stream, and applies proper 'exists' behavior.
        root = JavaWriter.getPathToClass(root, name);
        writer = new JavaWriter(root, false);

        // Writes the header of the class
        if(packageName != null)
            writer.printPackage(packageName);
        writer.printClass(className, JavaWriter.MODIFIER_PUBLIC);

        return writer;
    }
}
