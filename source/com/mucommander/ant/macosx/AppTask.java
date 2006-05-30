package com.mucommander.ant.macosx;

import org.apache.tools.ant.*;
import java.io.*;
import java.util.*;
import java.net.URL;
import com.mucommander.xml.*;

/**
 * @author Nicolas Rinaudo
 */
public class AppTask extends Task {
    // - Info.plist structure --------------------------------------------
    // -------------------------------------------------------------------
    /** Label of the dictionary tag in Info.plist. */
    private static final String TAG_DICT          = "dict";
    /** Label of the property list tag in Info.plist. */
    private static final String TAG_PLIST         = "plist";
    /** Label of they key tag in Info.plist. */
    private static final String TAG_KEY           = "key";
    /** Label of the string tag in Info.plist. */
    private static final String TAG_STRING        = "string";
    /** Label of the property list's version attribute in Info.plist. */
    private static final String ATTRIBUTE_VERSION = "version";
    /** URL of the property list DTD file. */
    private static final String URL_PLIST_DTD     = "file://localhost/System/Library/DTDs/PropertyList.dtd";



    // - Info.plist keys -------------------------------------------------
    // -------------------------------------------------------------------
    /** Label of the 'path to executable' key in Info.plist. */
    private static final String KEY_EXECUTABLE    = "CFBundleExecutable";
    /** Label of the 'package type' key in Info.plist. */
    private static final String KEY_PACKAGE_TYPE  = "CFBundlePackageType";
    /** Label of the 'bundle signature' key in Info.plist. */
    private static final String KEY_SIGNATURE     = "CFBundleSignature";
    /** Label of the 'bundle icon' in Info.plist. */
    private static final String KEY_ICON          = "CFBundleIconFile";
    /** Label of the 'java' list of properties in Info.plist. */
    private static final String DICT_JAVA         = "Java";
    /** Label of the 'classpath' key in Info.plist. */
    private static final String KEY_CLASSPATH     = "ClassPath";



    // - .app constants --------------------------------------------------
    // -------------------------------------------------------------------
    /** Name of the Java application stub file. */
    private static final String APPLICATION_STUB = "JavaApplicationStub";
    /** Name of the package info file. */
    private static final String PACKAGE_INFO     = "PkgInfo";
    /** Name of the Contents folder. */
    private static final String CONTENTS_FOLDER  = "Contents";
    /** Name of the Resources folder. */
    private static final String RESOURCES_FOLDER = "Resources";
    /** Name of the Java folder. */
    private static final String JAVA_FOLDER      = "Java";
    /** Name of the MacOS folder. */
    private static final String MACOS_FOLDER     = "MacOS";
    /** Name of the propery list file. */
    private static final String PROPERTIES_LIST  = "Info.plist";



    // - Instance fields -------------------------------------------------
    // -------------------------------------------------------------------
    /** Where to store the resulting .app. */
    private File           destination;
    /** Application bundle type. */
    private String         type;
    /** Application bundle signature. */
    private String         signature;
    /** Path to the application's icon. */
    private File           icon;
    /** Application's info description. */
    private RootDictionary info;
    /** Path to the application's JAR file. */
    private File           jar;



    // - Initialisation --------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Builds and initialises an Apptask.
     */
    public AppTask() {}

    /**
     * Initialises the Apptask.
     */
    public void init() {
        destination = null;
        type        = null;
        signature   = null;
        icon        = null;
        info        = new RootDictionary();
        jar         = null;
    }



    // - Ant interaction -------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Sets the path to which the app file should be generated.
     * @param f path to which the app file should be generated.
     */
    public void setDest(File f) {destination = f;}

    /**
     * Sets the bundle type of the app file.
     * @param s bundle type.
     */
    public void setType(String s) {type = s;}

    /**
     * Sets the application's signature.
     * @param s application's signature.
     */
    public void setSignature(String s) {signature = s;}

    /**
     * Sets the path to the application's icon.
     * @param f path to the application's icon.
     */
    public void setIcon(File f) {icon = f;}

    /**
     * Sets the path to the application's JAR file.
     * @param f path to the application's JAR file.
     */
    public void setJar(File f) {jar = f;}

    /**
     * Returns a fully initialised RootDictionary instance.
     * <p>
     * This instance is the one that will be used for the <code>Info.plist</code>
     * file generation.
     * </p>
     * @return a fully initialised RootDictionary instance.
     */
    public RootDictionary createInfo() {return info;}

    /**
     * Entry point of the task.
     * @exception BuildException thrown if any error occurs during .app generation.
     */
    public void execute() throws BuildException {
        File current; // Used to create the various directories needed by the .app.

        // Checks whether the proper parameters were passed to the application.
        if(destination == null)
            throw new BuildException("No destination folder specified. Please fill in the dest argument.");
        if(type == null)
            throw new BuildException("No application bundle type specified. Please fill in the type argument.");
        if(signature == null)
            throw new BuildException("No application signature specified. Please fill in the signature argument.");
        if(icon == null)
            throw new BuildException("No application icon specified. Please fill in the icon argument.");
        else if(!icon.exists())
            throw new BuildException("File not found: " + icon);
        if(jar == null)
            throw new BuildException("No application jar specified. Please fill in the jar argument.");
        else if(!jar.exists())
            throw new BuildException("File not found: " + jar);

        // Makes sure we create the application in a proper .app directory.
        if(!destination.getName().endsWith(".app"))
            destination = new File(destination.getParent(), destination.getName() + ".app");

        // Creates all the necessary directories and files.
        mkdir(destination);
        mkdir(current = new File(destination, CONTENTS_FOLDER));
        writePkgInfo(current);
        writeJavaStub(current);
        writeInfo(current);
        mkdir(current = new File(current, RESOURCES_FOLDER));
        writeIcon(current);
        mkdir(current = new File(current, JAVA_FOLDER));
        writeJar(current);
    }



    // - Info.plist generation -------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Adds default keys to the property list.
     * <p>
     * Default keys are:<br/>
     * - {@link #KEY_EXECUTABLE}: this will always be {@link #APPLICATION_STUB}.<br/>
     * - {@link #KEY_PACKAGE_TYPE}: value specified by {@link #setType(String)}.<br/>
     * - {@link #KEY_SIGNATURE}: value specified by {@link #setSignature(String)}.<br/>
     * - {@link #KEY_ICON}: path to the copy of the file specified in {@link #setIcon(File)}.<br/>
     * - {@link #KEY_CLASSPATH}: path to the copy of the file specified in {@link #setJar(File)}.<br/>
     * </p>
     * <p>
     * Note that the {@link #KEY_CLASSPATH} needs to be stored in the {@link #DICT_JAVA} dictionary.
     * If this has not been created by the user yet, a new dictionary will be added.
     * </p>
     */
    private void addDefaultKeys() {
        InfoString buffer; // Used to create dynamicaly generated keys.
        Dictionary java;   // Java dictionary.

        // Adds the KEY_EXECUTABLE key.
        buffer = info.createString();
        buffer.setName(KEY_EXECUTABLE);
        buffer.setValue(APPLICATION_STUB);

        // Adds the KEY_PACKAGE_TYPE key.
        buffer = info.createString();
        buffer.setName(KEY_PACKAGE_TYPE);
        buffer.setValue(type);

        // Adds the KEY_SIGNATURE key.
        buffer = info.createString();
        buffer.setName(KEY_SIGNATURE);
        buffer.setValue(signature);

        // Adds the KEY_ICON key.
        buffer = info.createString();
        buffer.setName(KEY_ICON);
        buffer.setValue(icon.getName());

        // If the DICT_JAVA dictionary hasn't been created yet,
        // creates it.
        if((java = info.getDictionary(DICT_JAVA)) == null) {
            java = info.createDict();
            java.setName(DICT_JAVA);
        }

        // Adds the DICT_JAVA/KEY_CLASSPATH key.
        buffer = java.createString();
        buffer.setName(KEY_CLASSPATH);
        buffer.setValue("$JAVAROOT/" + jar.getName());

    }

    private void writeInfo(File contents) throws BuildException {
        XmlWriter     out;
        XmlAttributes attr;

        // Initialises the Info.plist writing.
        out = null;
        addDefaultKeys();
        info.check();

        try {
            out = new XmlWriter(new File(contents, PROPERTIES_LIST));

            // Writes the Info.plist header.
            out.writeDocType(TAG_PLIST, XmlWriter.AVAILABILITY_SYSTEM, null, URL_PLIST_DTD);
            attr = new XmlAttributes();
            attr.add(ATTRIBUTE_VERSION, info.getVersion());
            out.openTag(TAG_PLIST, attr);
            out.println();

            writeDictContent(out, info);

            out.closeTag(TAG_PLIST);
        }
        catch(IOException e) {throw new BuildException("Could not open " + PROPERTIES_LIST + " for writing", e);}

        // Releases resources.
        finally {
            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }
    }

    private static void writeDictContent(XmlWriter out, AbstractDictionary dict) throws BuildException {
        Iterator elements;
        Object   element;

        out.openTag(TAG_DICT);
        out.println();
        
        elements = dict.elements();
        while(elements.hasNext()) {
            element = elements.next();
            if(element instanceof InfoString) {
                InfoString string = (InfoString)element;
                string.check();
                out.openTag(TAG_KEY);
                out.writeCData(string.getName());
                out.closeTag(TAG_KEY);

                out.openTag(TAG_STRING);
                out.writeCData(string.getValue());
                out.closeTag(TAG_STRING);
            }
            else if(element instanceof Dictionary) {
                Dictionary dictionary = (Dictionary)element;
                dictionary.check();
                out.openTag(TAG_KEY);
                out.writeCData(dictionary.getName());
                out.closeTag(TAG_KEY);
                writeDictContent(out, dictionary);
            }
        }
        out.closeTag(TAG_DICT);
    }



    // - PkgInfo generation ----------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Writes the Package Info file in the specified folder.
     * @param     contents       path to the .app's Contents folder.
     * @exception BuildException thrown if anything goes tits up.
     */
    private void writePkgInfo(File contents) throws BuildException {
        PrintStream out;

        try {
            out = new PrintStream(new FileOutputStream(new File(contents, PACKAGE_INFO)));
            out.print(type);
            out.print(signature);
            out.close();
        }
        catch(Exception e) {throw new BuildException("Could not write " + PACKAGE_INFO + " file", e);}
    }



    // - JavaApplicationStub generation ----------------------------------
    // -------------------------------------------------------------------
    /**
     * Writes the JavaApplicationStub in the proper folder.
     * @param     file           Path to the .app's Contents folder.
     * @exception BuildException thrown if anything goes wrong.
     */
    private void writeJavaStub(File file) throws BuildException {
        // Makes sure the MacOS folder exists.
        mkdir(file = new File(file, MACOS_FOLDER));

        try {
            transfer(this.getClass().getResource('/' + APPLICATION_STUB),
                     new File(file, APPLICATION_STUB));
        }
        catch(Exception e) {throw new BuildException("Could not generate " + APPLICATION_STUB, e);}

        // Tries to set the file's permissions for Unix like systems.
        // Since we're compiling something for Mac OS X here, this is
        // more than likely to be the case anyway.
        boolean failed;
        Process process;
        try {
            process = Runtime.getRuntime().exec(new String[] {"chmod", "+x", APPLICATION_STUB}, null, file);
            process.waitFor();
            failed = process.exitValue() != 0;
        }
        catch(Exception e) {failed = true;}

        // Could not find chmod. Prints a helpful message, and tries to get on with the build.
        if(failed) {
            System.out.println("Could not make " + APPLICATION_STUB + " executable.");
            System.out.println("You're probably not running an Unix system.");
            System.out.println("This error is non-fatal, but you must make sure to make " + APPLICATION_STUB +
                               " executable before you can distribute this file.");
        }
    }



    // - Icon.icns generation --------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Writes the application's icon.
     * @param     resources      path to the application's Resources folder.
     * @exception BuildException thrown if any error occurs.
     */
    private void writeIcon(File resources) throws BuildException {
        // Copies the icon.
        try {transfer(icon.toURL(), new File(resources, icon.getName()));}
        catch(Exception e) {throw new BuildException("Could not generate application icon", e);}
    }



    // - JAR file generation ---------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Writes the application's jar file to jar.icns.
     * @param     java           path to the application's Resources/Java folder.
     * @exception BuildException thrown if any error occurs.
     */
    private void writeJar(File java) throws BuildException {
        // Copies the jar.
        try {transfer(jar.toURL(), new File(java, jar.getName()));}
        catch(Exception e) {throw new BuildException("Could not generate application jar", e);}
    }



    // - Helper methods --------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Makes sure the specified directory exists, or creates it if necessary.
     * @param     dir            directory to check for.
     * @exception BuildException thrown if it was impossible to create the directory.
     */
    private static void mkdir(File dir) throws BuildException {
        // If the path exists, makes sure it's not a file.
        if(dir.exists()) {
            if(!dir.isDirectory())
                throw new BuildException(dir + " is not a valid directory path.");
        }
        // Otherwise, tries to create the directory.
        else if(!dir.mkdirs())
            throw new BuildException("Could not create directory " + dir);
    }

    /**
     */
    private static void transfer(URL from, File to) throws IOException {
        InputStream  in;
        OutputStream out;
        int    count;  // Number of bytes read in the latest iteration.
        byte[] buffer; // Stores bytes before they're transfered.

        buffer = new byte[1024];
        in     = null;
        out    = null;

        try {
            in  = from.openStream();
            out = new FileOutputStream(to);
            // Transfers the content of in to out.
            while(true) {
                if((count = in.read(buffer)) == -1)
                    break;
                out.write(buffer, 0, count);
            }
        }
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
