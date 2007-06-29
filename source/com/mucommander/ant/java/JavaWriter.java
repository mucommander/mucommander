/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.ant.java;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Utility class for Java source code generation.
 * <p>
 * <b>Important note:</b> This class gives developers the ropes to hang themselves with.
 * It performs no syntax checking whatsoever, meaning that it's entirely possible to
 * create <code>public private protected synchronized final<code> classes.
 * </p>
 * @author Nicolas Rinaudo (nicolas@nrinaudo.com)
 */
class JavaWriter {
    // - Modifier definitions --------------------------------------------
    // -------------------------------------------------------------------
    // Defines what kind of modifiers are known to the writer

    public static final int MODIFIER_PUBLIC        = 1;
    public static final int MODIFIER_PRIVATE       = 2;
    public static final int MODIFIER_PROTECTED     = 4;
    public static final int MODIFIER_FINAL         = 8;
    public static final int MODIFIER_STATIC        = 16;
    public static final int MODIFIER_SYNCHRONIZED  = 32;
    public static final int MODIFIER_ABSTRACT      = 64;


    // - Type definitions ------------------------------------------------
    // -------------------------------------------------------------------
    // Defines the primitive types that are known to the writer

    public static final int TYPE_UNKNOWN           = 0;
    public static final int TYPE_INT               = 1;
    public static final int TYPE_BYTE              = 2;
    public static final int TYPE_LONG              = 3;
    public static final int TYPE_CHAR              = 4;
    public static final int TYPE_FLOAT             = 5;
    public static final int TYPE_DOUBLE            = 6;
    public static final int TYPE_STRING            = 7;
    public static final int TYPE_BOOLEAN           = 8;
    public static final int TYPE_SHORT             = 9;


    // - Type labels -----------------------------------------------------
    // -------------------------------------------------------------------
    // Defines the labels of the primitive types that are known to the writer

    public static final String LABEL_INT          = "int";
    public static final String LABEL_SHORT        = "short";
    public static final String LABEL_BYTE         = "byte";
    public static final String LABEL_LONG         = "long";
    public static final String LABEL_CHAR         = "char";
    public static final String LABEL_FLOAT        = "float";
    public static final String LABEL_DOUBLE       = "double";
    public static final String LABEL_STRING       = "String";
    public static final String LABEL_BOOLEAN      = "boolean";


    // - Keyword labels --------------------------------------------------
    // -------------------------------------------------------------------
    // Defines the labels of the keywords that are known to the writer

    private static final String LABEL_PACKAGE      = "package";
    private static final String LABEL_CLASS        = "class";
    private static final String LABEL_INTERFACE    = "interface";
    private static final String LABEL_ABSTRACT     = "abstract";
    private static final String LABEL_PUBLIC       = "public";
    private static final String LABEL_PROTECTED    = "protected";
    private static final String LABEL_PRIVATE      = "private";
    private static final String LABEL_FINAL        = "final";
    private static final String LABEL_STATIC       = "static";
    private static final String LABEL_SYNCHRONIZED = "synchronized";

    // - Writer fields ---------------------------------------------------
    // -------------------------------------------------------------------
    // Fields used by the instance to function.

    /** Where to write the java source code. */
    private PrintStream out;


    // - Initialisation --------------------------------------------------
    // -------------------------------------------------------------------
    // Misc. constructors.

    /**
     * Creates a new JavaWriter instance on the specified file.
     * @param     file                  file on which to open the stream.
     * @param     append                if set to <code>true</code>, the destination file will be appended to.
     * @exception FileNotFoundException if a stream to the specified file could not be opened.
     */
    public JavaWriter(File file, boolean append) throws FileNotFoundException {
        out = new PrintStream(new FileOutputStream(file, append));
    }

    /**
     * Creates a new JavaWriter instance on the specified file.
     * @param     file                  file on which to open the stream.
     * @exception FileNotFoundException if a stream to the specified file could not be opened.
     */
    public JavaWriter(File file) throws FileNotFoundException {out = new PrintStream(new FileOutputStream(file));}

    /**
     * Creates a new JavaWriter instance on the specified output stream.
     * @param stream stream in which to print Java instructions.
     */
    public JavaWriter(OutputStream stream) {out = new PrintStream(stream);}


    // - Code writing ----------------------------------------------------
    // -------------------------------------------------------------------
    // Methods used to perform some java output

    /**
     * Returns the label of the specified type.
     * @param     type                     type whose label should be returned.
     * @return                             the label of the specified type.
     * @exception IllegalArgumentException thrown if <code>type</code> is not known.
     */
    private String getTypeLabel(int type) throws IllegalArgumentException {
        switch(type) {
        case TYPE_INT:
            return LABEL_INT;
        case TYPE_SHORT:
            return LABEL_SHORT;
        case TYPE_BYTE:
            return LABEL_BYTE;
        case TYPE_LONG:
            return LABEL_LONG;
        case TYPE_CHAR:
            return LABEL_CHAR;
        case TYPE_FLOAT:
            return LABEL_FLOAT;
        case TYPE_DOUBLE:
            return LABEL_DOUBLE;
        case TYPE_STRING:
            return LABEL_STRING;
        case TYPE_BOOLEAN:
            return LABEL_BOOLEAN;
        default:
            throw new IllegalArgumentException("Unknown type");
        }
    }

    /**
     * Print the specified modifier's label.
     * @param modifier description of the modifiers to print.
     */
    private void printModifier(int modifier) {
        // Prints the public modifier if necessary
        if((modifier & MODIFIER_PUBLIC) != 0) {
            out.print(LABEL_PUBLIC);
            out.print(' ');
        }

        // Prints the private modifier if necessary
        if((modifier & MODIFIER_PRIVATE) != 0) {
            out.print(LABEL_PRIVATE);
            out.print(' ');
        }

        // Prints the protected modifier if necessary
        if((modifier & MODIFIER_PROTECTED) != 0) {
            out.print(LABEL_PROTECTED);
            out.print(' ');
        }

        // Prints the static modifier if necessary
        if((modifier & MODIFIER_STATIC) != 0) {
            out.print(LABEL_STATIC);
            out.print(' ');
        }

        // Prints the final modifier if necessary
        if((modifier & MODIFIER_FINAL) != 0) {
            out.print(LABEL_FINAL);
            out.print(' ');
        }

        // Prints the final modifier if necessary
        if((modifier & MODIFIER_SYNCHRONIZED) != 0) {
            out.print(LABEL_SYNCHRONIZED);
            out.print(' ');
        }

        // Prints the abstract modifier if necessary
        if((modifier & MODIFIER_ABSTRACT) != 0) {
            out.print(LABEL_ABSTRACT);
            out.print(' ');
        }
    }

    /**
     * Prints a package declaration.
     * @param packageName name of the package to declare.
     */
    public void printPackage(String packageName) {
        out.print(LABEL_PACKAGE);
        out.print(' ');
        out.print(packageName);
        out.println(';');
    }

    public void printClass(String className, int modifiers) {
        printModifier(modifiers);
        out.print(LABEL_CLASS);
        out.print(' ');
        out.print(className);
        out.println(" {");
    }

    public void printInterface(String interfaceName, int modifiers) {
        printModifier(modifiers);
        out.print(LABEL_INTERFACE);
        out.print(' ');
        out.print(interfaceName);
        out.println(" {");
    }

    public void printField(String name, int type, Object value, int modifiers) {
        if(type == TYPE_STRING)
            value = '\"' + value.toString() + '\"';
        printField(name, getTypeLabel(type), value, modifiers);
    }

    public void printField(String name, String type, Object value, int modifiers) {
        printModifier(modifiers);
        out.print(type);
        out.print(' ');
        out.print(name);
        if(value != null) {
            out.print(" = ");
            out.print(value.toString());
        }
        out.println(';');
    }


    /**
     * Prints a class footer.
     */
    public void printClassFooter() {out.println('}');}


    // - Misc. -----------------------------------------------------------
    // -------------------------------------------------------------------
    // Miscellaneous methods

    /**
     * Closes the output stream.
     */
    public void close() {out.close();}


    // - Tools -----------------------------------------------------------
    // -------------------------------------------------------------------
    // The following methods are used to perform actions that are not directly
    // related to Java writing, but are still usefull for that task - package directory
    // structure creation, qualified name analysis, ...

    /**
     * Returns the type description of the specified label.
     * @param label Java type label to analyze.
     * @return the field's type if found, {@link #TYPE_UNKNOWN} if not.
     */
    public static int getTypeForLabel(String label) {
        if(label.equals(LABEL_INT))
            return TYPE_INT;
        else if(label.equals(LABEL_SHORT))
            return TYPE_SHORT;
        else if(label.equals(LABEL_BYTE))
            return TYPE_BYTE;
        else if(label.equals(LABEL_LONG))
            return TYPE_LONG;
        else if(label.equals(LABEL_CHAR))
            return TYPE_CHAR;
        else if(label.equals(LABEL_FLOAT))
            return TYPE_FLOAT;
        else if(label.equals(LABEL_DOUBLE))
            return TYPE_DOUBLE;
        else if(label.equals(LABEL_STRING))
            return TYPE_STRING;
        else if(label.equals(LABEL_BOOLEAN))
            return TYPE_BOOLEAN;
        else
            return TYPE_UNKNOWN;
    }

    /**
     * Extracts the stripped name of the specified fully qualified class name.
     * <p>
     * This method will remove the package part of the specified fully qualified class name, if
     * any.
     * </p>
     * @param  className fully qualified class name to analyse.
     * @return           the class name, stripped of its package.
     */
    public static String getClassName(String className) {
        int index; // Index of the last occurence of .

        // Splits the name into package / class
        index = className.lastIndexOf('.');

        // If there is no occurence of ., then className is already a class name
        if(index == -1)
            return className;

        // Returns the class name
        return className.substring(index + 1, className.length());
    }

    /**
     * Extracts the package name of the specified fully qualified class name.
     * <p>
     * This method will remove the class part of the specified fully qualified class name.
     * </p>
     * @param  className fully qualified class name to analyse.
     * @return           the package name, stripped of its class part.
     */
    public static String getPackageName(String className) {
        int index; // Index of the last occurence of .

        // Splits the name into package / class
        index = className.lastIndexOf('.');

        // If there is no occurence of ., then className does not contain a package
        if(index == -1)
            return null;

        // Returns the package name
        return className.substring(0, index);
    }

    /**
     * Returns the path to the specified fully qualified class name.
     * <p>
     * The value returned by this method is a path to the Java file that would
     * contain the definition of the specified class. For example, should the
     * <code>className</code> parameter be <code>com.nrinaudo.tools.JavaWriter</code>,
     * then the returned value would be <code>rootDirectory/com/nrinaudo/toos/JavaWriter.java</code>.
     * </p>
     * @param rootDirectory directory from which the path should be built.
     * @param className     fully qualified name of the class.
     * @return              the path to the specified fully qualified class name.
     * @see                 #getPathToPackage(File,String)
     */
    public static File getPathToClass(File rootDirectory, String className) {
        String packageName;

        packageName = getPackageName(className);
        if(packageName != null)
            rootDirectory = getPathToPackage(rootDirectory, packageName);

        return new File(rootDirectory, getClassName(className) + ".java");
    }

    /**
     * Returns a path to the specified package.
     * <p>
     * This method will return a path to the directory in which to store classes that belong
     * to the specified package. For example, should the <code>packageName</code> parameter be
     * <code>com.nrinaudo.tools</code>, the returned value would be
     * <code>rootDirectory/com/nrinaudo/tools</code>.
     * </p>
     * @param  rootDirectory directory from which the path should be built.
     * @param  packageName   name of the package that will be analyzed.
     * @return               a path to the specified package.
     * @see                  #getPathToClass(File,String)
     */
    public static File getPathToPackage(File rootDirectory, String packageName) {
        StringTokenizer path; // Used to extract each path item from the package name

        // Creates a full path to the package
        path = new StringTokenizer(packageName, ".");
        while(path.hasMoreTokens())
            rootDirectory = new File(rootDirectory, path.nextToken());

        return rootDirectory;
    }
}
