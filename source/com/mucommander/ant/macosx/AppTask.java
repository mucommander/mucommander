package com.mucommander.ant.macosx;

import org.apache.tools.ant.*;
import java.io.*;
import java.util.*;

/**
 * @author Nicolas Rinaudo
 */
public class AppTask extends Task {
    // - Info.plist constants --------------------------------------------
    // -------------------------------------------------------------------
    private static final String TAG_DICT         = "dict";
    private static final String TAG_KEY          = "key";
    private static final String TAG_STRING       = "string";
    private static final int    OFFSET_INCREMENT = 4;
    private static final String KEY_EXECUTABLE   = "CFBundleExecutable";
    private static final String KEY_PACKAGE_TYPE = "CFBundlePackageType";
    private static final String KEY_SIGNATURE    = "CFBundleSignature";
    private static final String KEY_ICON         = "CFBundleIconFile";
    private static final String DICT_JAVA        = "Java";
    private static final String KEY_CLASSPATH    = "ClassPath";



    // - .app constants --------------------------------------------------
    // -------------------------------------------------------------------
    private static final String APPLICATION_STUB = "JavaApplicationStub";
    private static final String PACKAGE_INFO     = "PkgInfo";
    private static final String CONTENTS_FOLDER  = "Contents";
    private static final String RESOURCES_FOLDER = "Resources";
    private static final String JAVA_FOLDER      = "Java";
    private static final String MACOS_FOLDER     = "MacOS";
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
    public void setDest(File f) {destination = f;}
    public void setType(String s) {type = s;}
    public void setSignature(String s) {signature = s;}
    public void setIcon(File f) {icon = f;}
    public void setJar(File f) {jar = f;}
    public RootDictionary createInfo() {return info;}

    public void execute() throws BuildException {
        InfoString buffer; // Used to create dynamicaly generated keys.
        Dictionary java;   // Java dictionary.

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

        buffer = info.createString();
        buffer.setName(KEY_EXECUTABLE);
        buffer.setValue(APPLICATION_STUB);

        buffer = info.createString();
        buffer.setName(KEY_PACKAGE_TYPE);
        buffer.setValue(type);

        buffer = info.createString();
        buffer.setName(KEY_SIGNATURE);
        buffer.setValue(signature);

        buffer = info.createString();
        buffer.setName(KEY_ICON);
        buffer.setValue(icon.getName());

        if((java = info.getDictionary(DICT_JAVA)) == null) {
            java = info.createDict();
            java.setName(DICT_JAVA);
        }

        buffer = java.createString();
        buffer.setName(KEY_CLASSPATH);
        buffer.setValue("$JAVAROOT/" + jar.getName());

        makeApp();
    }



    // - App generation --------------------------------------------------
    // -------------------------------------------------------------------
    private void writeInfo(File contents) throws BuildException {
        PrintStream out;

        // Initialises the Info.plist writing.
        out = null;
        info.check();

        try {
            out = new PrintStream(new FileOutputStream(new File(contents, PROPERTIES_LIST)));

            // Writes the Info.plist header.
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<!DOCTYPE plist SYSTEM \"file://localhost/System/Library/DTDs/PropertyList.dtd\">");
            out.print("<plist version=\"");
            out.print(info.getVersion());
            out.println("\">");

            writeDictContent(out, info, OFFSET_INCREMENT);

            out.println("</plist>");
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

    private static void writeDictContent(PrintStream out, AbstractDictionary dict, int offset) throws BuildException {
        Iterator elements;
        Object   element;

        openTag(out, TAG_DICT, offset, true);

        elements = dict.elements();
        while(elements.hasNext()) {
            element = elements.next();
            if(element instanceof InfoString)
                writeString(out, (InfoString)element, offset + OFFSET_INCREMENT);
            else if(element instanceof Dictionary)
                writeDict(out, (Dictionary)element, offset + OFFSET_INCREMENT);
        }

        closeTag(out, TAG_DICT, offset, true);
    }

    private static void writeDict(PrintStream out, Dictionary dict, int offset) throws BuildException {
        dict.check();
        openTag(out, TAG_KEY, offset, false);
        out.print(dict.getName());
        closeTag(out, TAG_KEY, offset, false);

        writeDictContent(out, dict, offset);
    }

    private static void writeString(PrintStream out, InfoString string, int offset) throws BuildException {
        string.check();
        openTag(out, TAG_KEY, offset, false);
        out.print(string.getName());
        closeTag(out, TAG_KEY, offset, false);

        openTag(out, TAG_STRING, offset, false);
        out.print(string.getValue());
        closeTag(out, TAG_STRING, offset, false);
    }

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

    /**
     * Writes the JavaApplicationStub in the proper folder.
     * @param     file           Path to the .app's Contents folder.
     * @exception BuildException thrown if anything goes wrong.
     */
    private void writeJavaStub(File file) throws BuildException {
        InputStream  in;     // Input stream on the stored JavaApplicationStub file.
        OutputStream out;    // Output stream on the .app's JavaApplicationStub file.

        // Makes sure the MacOS folder exists.
        mkdir(file = new File(file, MACOS_FOLDER));

        // Initialises the transfer.
        in     = null;
        out    = null;

        try {
            // Opens the streams.
            in  = this.getClass().getResourceAsStream('/' + APPLICATION_STUB);
            out = new FileOutputStream(new File(file, APPLICATION_STUB));

            transfer(in, out);
        }
        catch(Exception e) {throw new BuildException("Could not generate " + APPLICATION_STUB, e);}
        // Releases resources.
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
            if(out != null)
                try {out.close();}
                catch(Exception e) {}
        }

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

    /**
     * Writes the application's icon.
     * @param     resources      path to the application's Resources folder.
     * @exception BuildException thrown if any error occurs.
     */
    private void writeIcon(File resources) throws BuildException {
        InputStream  in;  // Where to read the content of the icons from.
        OutputStream out; // Where to write the content of the icons to.

        in  = null;
        out = null;

        // Copies the icon.
        try {
            in = new FileInputStream(icon);
            out = new FileOutputStream(new File(resources, icon.getName()));

            transfer(in, out);
        }
        catch(Exception e) {throw new BuildException("Could not generate application icon", e);}
        // Releases resources.
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
            if(out != null)
                try {out.close();}
                catch(Exception e) {}
        }
    }

    /**
     * Writes the application's jar file to jar.icns.
     * @param     java           path to the application's Resources/Java folder.
     * @exception BuildException thrown if any error occurs.
     */
    private void writeJar(File java) throws BuildException {
        InputStream  in;  // Where to read the content of the jars from.
        OutputStream out; // Where to write the content of the jars to.

        in  = null;
        out = null;

        // Copies the jar.
        try {
            in = new FileInputStream(jar);
            out = new FileOutputStream(new File(java, jar.getName()));

            transfer(in, out);
        }
        catch(Exception e) {throw new BuildException("Could not generate application jar", e);}
        // Releases resources.
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
            if(out != null)
                try {out.close();}
                catch(Exception e) {}
        }
    }


    /**
     * Creates the whole .app directory tree.
     * @exception BuildException thrown if anything went wrong.
     */
    private void makeApp() throws BuildException {
        File current; // Used to create the various directories needed by the .app.

        // Makes sure we create the application in a proper .app directory.
        if(!destination.getName().endsWith(".app"))
            destination = new File(destination.getParent(), destination.getName() + ".app");

        // Creates all the necessary directories and files.x
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
     * Transfers the content of <code>in</code> to <code>out</code>.
     * @param     in          where to read from.
     * @param     out         where to write to.
     * @exception IOException thrown if any IO related error occurs.
     */
    private static void transfer(InputStream in, OutputStream out) throws IOException {
        int    count;  // Number of bytes read in the latest iteration.
        byte[] buffer; // Stores bytes before they're transfered.

        buffer = new byte[1024];

        // Transfers the content of in to out.
        while(true) {
            if((count = in.read(buffer)) == -1)
                break;
            out.write(buffer, 0, count);
        }
    }

    private static void printOffset(PrintStream out, int offset) {
        for(int i = 0; i < offset; i++)
            out.print(' ');
    }

    private static void openTag(PrintStream out, String name, int offset, boolean lineBreak) {
        printOffset(out, offset);
        out.print('<');
        out.print(name);
        out.print('>');
        if(lineBreak)
            out.println();
    }

    private static void closeTag(PrintStream out, String name, int offset, boolean printOffset) {
        if(printOffset)
            printOffset(out, offset);
        out.print("</");
        out.print(name);
        out.println('>');
    }
}
