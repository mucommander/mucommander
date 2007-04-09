package com.mucommander.ant.macosx;

import com.mucommander.xml.writer.XmlAttributes;
import com.mucommander.xml.writer.XmlWriter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.*;
import java.net.URL;

/**
 * Ant task used to generate Mac OS X application files.
 * <h3>Description</h3>
 * <p>
 * Creates a Mac OS X application file.<br/>
 * This Ant task has been written for Java applications, and is not usable
 * for anything else such as, for example, native ones.
 * </p>
 * <p>
 * Mac OS X Java applications are wrapped around some native code, which this
 * task will generate. It does, however, make it system dependant: the wrapper
 * application must be set to executable, which is only possible under Unix
 * system (or any system which supports the <code>chmod</code> command).
 * </p>
 * <p>
 * For the resulting application to behave properly, its JAR file must be made
 * executable. More information on how to do that can be found
 * <a href="http://java.sun.com/j2se/javadoc/">here</a>.
 * </p>
 * <h3>Parameters</h3>
 * <p>
 * <table border="1" cellpadding="2" cellspacing="0">
 *   <tr>
 *     <td valign="top"><b>Attribute</b></td>
 *     <td valign="top"><b>Description</b></td>
 *     <td valign="top"><b>Required</b></td>
 *   </tr>
 *   <tr>
 *     <td valign="top">{@link #setDest(File) dest}</td>
 *     <td valign="top">
 *       Where to store the application file. This path is expected to have the
 *       <code>.app</code> extension, although this will be added if not found.<br/>
 *       It is expected to be an non-existing or empty directory. The task will not
 *       fail if such is not the case, but results may be unpredictable.
 *     </td>
 *     <td valign="top">Yes</td>
 *   </tr>
 *   <tr>
 *     <td valign="top">{@link #setIcon(File) icon}</td>
 *     <td valign="top">Path to the aplication's icon file.</td>
 *     <td valign="top">Yes</td>
 *   </tr>
 *   <tr>
 *     <td valign="top">{@link #setJar(File) jar}</td>
 *     <td valign="top">
 *       Path to the application's executable JAR file.
 *     </td>
 *     <td valign="top">Yes</td>
 *   </tr>
 *   <tr>
 *     <td valign="top">{@link #setSignature(String) signature}</td>
 *     <td valign="top">Application's signature</td>
 *     <td valign="top">Yes</td>
 *   </tr>
 *   <tr>
 *     <td valign="top">{@link #setType(String) type}</td>
 *     <td valign="top">Application's type.</td>
 *     <td valign="top">No (defaults to {@link #TYPE_APPL APPL})</td>
 *   </tr>
 *   <tr>
 *     <td valign="top">{@link #setInfoVersion(String) infoversion}</td>
 *     <td valign="top">Version number of the <code>Info.plist</code> DTD.</td>
 *     <td valign="top">No (defaults to {@link #DEFAULT_VERSION 1.0})</td>
 *   </tr>
 * </table>
 * </p>
 * <h3>Nested elements</h3>
 * <p>
 * Nested elements are used to describe the <code>Info.plist</code>
 * Mac OS X file, in a format that we feel is slightly less dodgy
 * than Apple's own implementation.
 * </p>
 * <h4>array</h4>
 * <p>
 * Describes an unnamed property collection.<br/>
 * See the {@link com.mucommander.ant.macosx.ArrayKey documentation} for more information.
 * </p>
 * <h4>boolean</h4>
 * <p>
 * Describes a boolean property.<br/>
 * See the {@link com.mucommander.ant.macosx.BooleanKey documentation} for more information.
 * </p>
 * <h4>data</h4>
 * <p>
 * Describes a raw data property.<br/>
 * See the {@link com.mucommander.ant.macosx.DataKey documentation} for more information.
 * </p>
 * <h4>date</h4>
 * <p>
 * Describes a date property.<br/>
 * See the {@link com.mucommander.ant.macosx.DateKey documentation} for more information.
 * </p>
 * <h4>dict</h4>
 * <p>
 * Describes a named property collection.<br/>
 * See the {@link com.mucommander.ant.macosx.DictKey documentation} for more information.
 * </p>
 * <h4>integer</h4>
 * <p>
 * Describes an integer property.<br/>
 * See the {@link com.mucommander.ant.macosx.IntegerKey documentation} for more information.
 * </p>
 * <h4>real</h4>
 * <p>
 * Describes a floating point number property.<br/>
 * See the {@link com.mucommander.ant.macosx.RealKey documentation} for more information.
 * </p>
 * <h3><code>Info.plist</code> keys</h3>
 * <p>
 * Some <code>Info.plist</code> are generated dynamically by this task. They should not be overwritten,
 * as this will result in incoherent property lists.
 * </p>
 * <p>
 * The automatically generated keys are:
 * <ul>
 *   <li>
 *     {@link #KEY_EXECUTABLE CFBundleExecutable}: set to the path to the java application
 *     stub (<code>/Contents/MacOS/JavaApplicationStub</code>).
 *   </li>
 *   <li>
 *     {@link #KEY_PACKAGE_TYPE CfBundlePackageType}: set to the application's {@link #setType(String) type}.
 *   </li>
 *   <li>
 *     {@link #KEY_SIGNATURE CFBundleSignature}: set to the application's {@link #setSignature(String) signature}.
 *   </li>
 *   <li>
 *     {@link #KEY_ICON CfBundleIconFile}: set to the path to the application's {@link #setIcon(File) icon}.
 *   </li>
 *   <li>
 *     {@link #KEY_CLASSPATH Java/ClassPath}: set to the path to the application's {@link #setJar(File) jar}. Note that elements
 *     can be added to that key using {@link #setClasspath(String)}.
 *   </li>
 * </ul>
 * </p>
 * <h3>Examples</h3>
 * <blockquote>
 * <code>
 * &lt;app jar=&quot;bin/application.jar&quot; dest=&quot;dist/Application.app&quot; signature=&quot;TEST&quot; icon=&quot;res/Icons.icns&quot;/&gt;
 * </code>
 * </blockquote>
 * <p>
 * creates a MacOS X application in <code>dist/Application.app</code> which will run the <code>bin/application.jar</code>
 * executable JAR file, using <code>res/Icons.icns</code> as icons.
 * </p>
 * @author Nicolas Rinaudo
 */
public class AppTask extends Task {
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



    // - Info.plist constants --------------------------------------------
    // -------------------------------------------------------------------
    private static final String ELEMENT_PLIST     = "plist";
    private static final String ATTRIBUTE_VERSION = "version";
    private static final String URL_PLIST_DTD     = "file://localhost/System/Library/DTDs/PropertyList.dtd";
    private static final String DEFAULT_VERSION   = "1.0";



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
    /** Default bundle type. */
    private static final String TYPE_APPL        = "APPL";



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
    private DictValue      properties;
    /** Path to the application's JAR file. */
    private File           jar;
    /** DTD version of the <code>Info.plist</code> file. */
    private String         infoVersion;
    /** Additional classpath elements. */
    private String         classpath;



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
        properties  = new DictValue();
        jar         = null;
        classpath   = null;
    }



    // - Ant interaction -------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Sets the path to which the application file should be generated.
     * <p>
     * <code>f</code> is expected to be a valid path to an either non-existing
     * or empty directory. While the task won't fail if such is not the case,
     * results are not predictable.<br/>
     * Note that the task <i>will</i> fail if <code>f</code> is an existing file.
     * </p>
     * @param f path to which the application file should be generated.
     */
    public void setDest(File f) {destination = f;}

    /**
     * Sets the bundle type of the app file.
     * <p>
     * This attribute is non compulsory, and will default to {@link #TYPE_APPL}.
     * </p>
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
     * <p>
     * In order for the application to start, the JAR file must be
     * executable. Click <a href="http://java.sun.com/j2se/javadoc/">here</a> to
     * learn more about making JAR files executable.
     * </p>
     * @param f path to the application's JAR file.
     */
    public void setJar(File f) {jar = f;}

    public void setClasspath(String classpath) {this.classpath = classpath;}

    /**
     * Sets the DTD version of the <code>Info.plist</code> file.
     * <p>
     * This parameter is non-compulsory and defaults to <code>1.0</code>
     * </p>
     * @param s DTD version of the <code>Info.plist</code> file.
     */
    public void setInfoVersion(String s) {infoVersion = s;}

    public ArrayKey createArray() {return properties.createArray();}
    public BooleanKey createBoolean() {return properties.createBoolean();}
    public StringKey createString() {return properties.createString();}
    public DictKey createDict() {return properties.createDict();}
    public IntegerKey createInteger() {return properties.createInteger();}
    public RealKey createReal() {return properties.createReal();}
    public DateKey createDate() {return properties.createDate();}
    public DataKey createData() {return properties.createData();}

    /**
     * Entry point of the task.
     * @exception BuildException thrown if any error occurs during application file generation.
     */
    public void execute() throws BuildException {
        File current; // Used to create the various directories needed by the .app.

        // Checks whether the proper parameters were passed to the application.
        if(destination == null)
            throw new BuildException("No destination folder specified. Please fill in the dest argument.");
        if(type == null)
            type = TYPE_APPL;
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
        StringKey buffer; // Used to create dynamicaly generated keys.
        DictKey   java;   // Java dictionary.

        // Adds the KEY_EXECUTABLE key.
        buffer = properties.createString();
        buffer.setName(KEY_EXECUTABLE);
        buffer.setValue(APPLICATION_STUB);

        // Adds the KEY_PACKAGE_TYPE key.
        buffer = properties.createString();
        buffer.setName(KEY_PACKAGE_TYPE);
        buffer.setValue(type);

        // Adds the KEY_SIGNATURE key.
        buffer = properties.createString();
        buffer.setName(KEY_SIGNATURE);
        buffer.setValue(signature);

        // Adds the KEY_ICON key.
        buffer = properties.createString();
        buffer.setName(KEY_ICON);
        buffer.setValue(icon.getName());

        // If the DICT_JAVA dictionary hasn't been created yet,
        // creates it.
        if((java = properties.getDict(DICT_JAVA)) == null) {
            java = properties.createDict();
            java.setName(DICT_JAVA);
        }

        // Adds the DICT_JAVA/KEY_CLASSPATH key.
        buffer = java.createString();
        buffer.setName(KEY_CLASSPATH);
        buffer.setValue("$JAVAROOT/" + jar.getName() + (classpath == null ? "" : ':' + classpath));

    }

    /**
     * Writes the application's <code>Info.plist</code> file.
     * @param     contents       path to the application's Contents folder.
     * @exception BuildException thrown if any error occurs.
     */
    private void writeInfo(File contents) throws BuildException {
        XmlWriter     out;
        XmlAttributes attr;

        // Initialises the Info.plist writing.
        out = null;
        addDefaultKeys();

        try {
            out = new XmlWriter(new File(contents, PROPERTIES_LIST));

            // Makes sure we have an Info.plist version.
            if(infoVersion == null)
                infoVersion = DEFAULT_VERSION;

            // Writes the DTD path.
            out.writeDocType(ELEMENT_PLIST, XmlWriter.AVAILABILITY_SYSTEM, null, URL_PLIST_DTD);

            // Writes the root tag.
            attr = new XmlAttributes();
            attr.add(ATTRIBUTE_VERSION, infoVersion);
            out.startElement(ELEMENT_PLIST, attr);
            out.println();

            // Writes the content of the Info.plist file.
            properties.write(out);

            out.endElement(ELEMENT_PLIST);
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


    // - PkgInfo generation ----------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Writes the Package Info file in the specified folder.
     * @param     contents       path to the application's Contents folder.
     * @exception BuildException thrown if anything goes tits up.
     */
    private void writePkgInfo(File contents) throws BuildException {
        PrintStream out;

        out = null;

        try {
            // Writes the applications PkgInfo file.
            out = new PrintStream(new FileOutputStream(new File(contents, PACKAGE_INFO)));
            out.print(type);
            out.print(signature);
            out.close();
        }
        catch(Exception e) {throw new BuildException("Could not write " + PACKAGE_INFO + " file", e);}

        // Releases resources.
        finally {
            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }
    }



    // - JavaApplicationStub generation ----------------------------------
    // -------------------------------------------------------------------
    /**
     * Writes the JavaApplicationStub in the proper folder.
     * <p>
     * This method is system dependant: the application stub file must be set
     * to executable, which cannot be done in a system independant way with any version
     * older than Java 1.6.
     * </p>
     * @param     file           Path to the .app's Contents folder.
     * @exception BuildException thrown if anything goes wrong.
     */
    private void writeJavaStub(File file) throws BuildException {
        // Makes sure the MacOS folder exists.
        mkdir(file = new File(file, MACOS_FOLDER));

        try {transfer(this.getClass().getResource('/' + APPLICATION_STUB),new File(file, APPLICATION_STUB));}
        catch(Exception e) {throw new BuildException("Could not generate " + APPLICATION_STUB, e);}

        // Tries to set the file's permissions for Unix like systems.
        // Since we're compiling something for Mac OS X here, this is
        // more than likely to be the case anyway.
        // TODO: Java 1.6 offers file attribute modification methods. Use those.
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
        try {transfer(icon.toURI().toURL(), new File(resources, icon.getName()));}
        catch(Exception e) {throw new BuildException("Could not generate application icon", e);}
    }



    // - JAR file generation ---------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Creates the application's JAR file.
     * @param     java           path to the application's Resources/Java folder.
     * @exception BuildException thrown if any error occurs.
     */
    private void writeJar(File java) throws BuildException {
        // Copies the jar.
        try {transfer(jar.toURI().toURL(), new File(java, jar.getName()));}
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
     * Transfers the content of the specified URL to the specified file.
     * @param     from        where to read data from.
     * @param     to          where to write data to.
     * @exception IOException thrown if any IO related error occurs.
     */
    private static void transfer(URL from, File to) throws IOException {
        InputStream  in;  // Stream on the input URL.
        OutputStream out; // Stream on the output file.
        int    count;     // Number of bytes read in the latest iteration.
        byte[] buffer;    // Stores bytes before they're transfered.

        // Initialises reading.
        buffer = new byte[1024];
        in     = null;
        out    = null;

        try {
            // Opens the streams.
            in  = from.openStream();
            out = new FileOutputStream(to);

            // Transfers the content of in to out.
            while(true) {
                if((count = in.read(buffer)) == -1)
                    break;
                out.write(buffer, 0, count);
            }
        }
        // Releases resources.
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
