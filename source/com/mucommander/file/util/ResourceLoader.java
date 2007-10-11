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

package com.mucommander.file.util;

import com.mucommander.file.AbstractArchiveFile;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;

import java.io.InputStream;
import java.net.URL;

/**
 * This class provides methods to load resources located within reach of a <code>ClassLoader</code>. Those resources
 * can reside either in a JAR file or in a local directory -- all methods of this class are agnostic to either type
 * of location.
 *
 * <p>The <code>ResourceAsURL</code> and <code>ResourceAsStream</code> methods are akin to those of <code>java.lang.Class</code>
 * and <code>java.lang.ClassLoader</code>, albeit easier to work with. But the real fun lies in the <code>ResourceAsFile</code>
 * methods which allow to manipulate resources as regular files -- again, whether they be in a regular directory or
 * in a JAR file. Finally, the {@link #getRootPackageAsFile(Class)} method allows to list and manipulate the resource
 * files contained in a particular classpath's location, including the .class files.
 *
 * @author Maxence Bernard
 */
public class ResourceLoader {

    /**
     * Finds the resource with the given path and returns a URL pointing to its location, or <code>null</code>
     * if the resource couldn't be located. The system <code>ClassLoader</code> is used for locating the resource.
     *
     * <p>The given path may or may not start with a leading slash character ('/'), this doesn't affect the way it
     * is interpreted.</p>
     *
     * @param path a path to the resource, relative to the system ClassLoader's classpath
     * @return a URL pointing to the resource, or null if the resource couldn't be located
     */
    public static URL getResourceAsURL(String path) {
        return ClassLoader.getSystemClassLoader().getResource(normalizePath(path));
    }

    /**
     * Finds the resource with the given path and returns a URL pointing to its location, or <code>null</code>
     * if the resource couldn't be located. The given <code>ClassLoader</code> is used for locating the resource.
     *
     * <p>The given path may or may not start with a leading slash character ('/'), this doesn't affect the way it
     * is interpreted.</p>
     *
     * @param path a path to the resource, relative to the system ClassLoader's classpath
     * @param classLoader the ClassLoader used for locating the resource
     * @return a URL pointing to the resource, or null if the resource couldn't be located
     */
    public static URL getResourceAsURL(String path, ClassLoader classLoader) {
        return classLoader.getResource(normalizePath(path));
    }


    /**
     * Finds the resource with the given path and returns an <code>InputStream</code> to read from it, or <code>null</code>
     * if the resource couldn't be located. The system <code>ClassLoader</code> is used for locating the resource.
     *
     * <p>The given path may or may not start with a leading slash character ('/'), this doesn't affect the way it
     * is interpreted.</p>

     * @param path a path to the resource, relative to the system ClassLoader's classpath
     * @return an InputStream that allows to read from the resource, or null if the resource couldn't be located
     */
    public static InputStream getResourceAsStream(String path) {
        return ClassLoader.getSystemClassLoader().getResourceAsStream(normalizePath(path));
    }

    /**
     * Finds the resource with the given path and returns an <code>InputStream</code> to read from it, or <code>null</code>
     * if the resource couldn't be located. The given <code>ClassLoader</code> is used for locating the resource.
     *
     * <p>The given path may or may not start with a leading slash character ('/'), this doesn't affect the way it
     * is interpreted.</p>

     * @param path a path to the resource, relative to the system ClassLoader's classpath
     * @param classLoader the Class whose ClassLoader is used for locating the resource
     * @return an InputStream that allows to read from the resource, or null if the resource couldn't be located
     */
    public static InputStream getResourceAsStream(String path, ClassLoader classLoader) {
        return classLoader.getResourceAsStream(normalizePath(path));
    }


    /**
     * Finds the resource with the given path and returns an {@link AbstractFile} that gives full access to it,
     * or <code>null</code> if the resource couldn't be located. The system <code>ClassLoader</code> is used for
     * locating the resource.
     *
     * <p>The given path may or may not start with a leading slash character ('/'), this doesn't affect the way it
     * is interpreted. Also noteworthy is this method may be slower than {@link #getResourceAsStream(String)} if
     * the resource is located inside a JAR file, because the Zip file headers will have to be parsed the first time
     * the archive is accessed. So this latter method should be favored if the file is simply used for reading the
     * resource.</p>
     *
     * @param path a path to the resource, relative to the system ClassLoader's classpath
     * @return an AbstractFile that allows to access the resource, or null if the resource couldn't be located
     */
    public static AbstractFile getResourceAsFile(String path) {
        return getResourceAsFile(normalizePath(path), ClassLoader.getSystemClassLoader());
    }

    /**
     * Finds the resource with the given path and returns an {@link AbstractFile} that gives full access to it,
     * or <code>null</code> if the resource couldn't be located. The given <code>ClassLoader</code> is used for
     * locating the resource.
     *
     * <p>The given path may or may not start with a leading slash character ('/'), this doesn't affect the way it
     * is interpreted. Also noteworthy is this method may be slower than {@link #getResourceAsStream(String)} if
     * the resource is located inside a JAR file, because the Zip file headers will have to be parsed the first time
     * the archive is accessed. So this latter method should be favored if the file is simply used for reading the
     * resource.</p>

     * @param path a path to the resource, relative to the system ClassLoader's classpath
     * @param classLoader the ClassLoader is used for locating the resource
     * @return an AbstractFile that allows to access the resource, or null if the resource couldn't be located
     */
    public static AbstractFile getResourceAsFile(String path, ClassLoader classLoader) {
        path = normalizePath(path);

        URL aClassURL = getResourceAsURL(path, classLoader);
        if(aClassURL==null)
            return null;        // no resource under that path

        if("jar".equals(aClassURL.getProtocol())) {
            try {
                return ((AbstractArchiveFile)FileFactory.getFile(getJarPath(aClassURL))).getArchiveEntryFile(path);
            }
            catch(Exception e) {
                // Shouldn't normally happen, unless the JAR file is corrupt or cannot be parsed by the file API
                return null;
            }
        }

        return FileFactory.getFile(aClassURL.getPath());
    }


    /**
     * Returns an {@link AbstractFile} to the root package of the given <code>Class</code>. For example, if the
     * specified <code>Class</code> is <code>java.lang.Object</code>'s, the returned file will be the Java runtime
     * JAR file, which on most platforms is <code>$JAVA_HOME/lib/jre/rt.jar</code>.<br/>
     * The returned file can be used to list or manipulate all resource files contained in a particular classpath's
     * location, including the .class files.
     *
     * @param aClass the class for which to locate the root package.
     * @return an AbstractFile to the root package of the given <code>Class</code>
     */
    public static AbstractFile getRootPackageAsFile(Class aClass) {
        ClassLoader classLoader = aClass.getClassLoader();
        if(classLoader==null)
            classLoader = ClassLoader.getSystemClassLoader();

        String aClassRelPath = aClass.getName().replace('.', '/')+".class";
        URL aClassURL = getResourceAsURL(aClassRelPath, classLoader);

        if(aClassURL==null)
            return null;    // no resource under that path

        if("jar".equals(aClassURL.getProtocol()))
            return FileFactory.getFile(getJarPath(aClassURL));

        String aClassPath = aClassURL.getPath();
        return FileFactory.getFile(aClassPath.substring(0, aClassPath.length()-aClassRelPath.length()));
    }


    /**
     * Extracts and returns the path to the JAR file from a URL that points to a resource inside a JAR file.
     *
     * @param url a URL that points to a resource inside a JAR file
     * @return returns the path to the JAR file
     */
    private static String getJarPath(URL url) {
        String path = url.getPath();
        // Here's an example of a path:
        // file:/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Classes/classes.jar!/java/lang/Object.class

        int pos = path.indexOf(".jar!");
        if(pos==-1)
            return null;

        // Strip out the leading "file:" and trailing ".jar!"
        return path.substring(5, pos+4);
    }

    /**
     * Removes any leading slash from the given path and returns it. Does nothing if the path does not have a
     * leading path.
     *
     * @param path the path to normalize
     * @return the path without a leading slash
     */
    private static String normalizePath(String path) {
        return path.startsWith("/")?path.substring(1, path.length()):path;
    }

}
