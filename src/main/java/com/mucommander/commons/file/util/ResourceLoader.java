/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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


package com.mucommander.commons.file.util;

import com.mucommander.commons.file.AbstractArchiveFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.impl.local.LocalFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;

/**
 * This class provides methods to load resources located within the reach of a <code>ClassLoader</code>. Those resources
 * can reside either in a JAR file or in a local directory that are in the classpath -- all methods of this class are
 * agnostic to either type of location.
 *
 * <p>The <code>getResourceAsURL</code> and <code>getResourceAsStream</code> methods are akin to those of
 * <code>java.lang.Class</code> and <code>java.lang.ClassLoader</code>, except that they are not sensitive to the
 * presence of a leading forward-slash separator in the resource path, and that they allow the search to be limited
 * to a particular classpath location.</p>
 *
 * <p>But the real fun lies in the <code>getResourceAsFile</code> methods, which allow to manipulate resources as
 * regular files -- again, whether they be in a regular directory or in a JAR file. Likewise,
 * the {@link #getRootPackageAsFile(Class)} allows to dynamically explore and manipulate the resource files contained
 * in a particular classpath's location.</p>
 *
 * @author Maxence Bernard
 */
public class ResourceLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLoader.class);

    /** the default ClassLoader that is used by methods without a ClassLoader argument */
    private static ClassLoader defaultClassLoader = ResourceLoader.class.getClassLoader();

    /**
     * Returns the default <code>ClassLoader</code> that is used by methods without a <code>ClassLoader</code> argument.
     * This default <code>ClassLoader</code> is the one that loaded this class, and <b>not</b> the system <code>ClassLoader</code>. 
     *
     * @return the default <code>ClassLoader</code> that is used by methods without a <code>ClassLoader</code> argument
     */
    public static ClassLoader getDefaultClassLoader() {
        // We do not use the system class loader because it does not work with JNLP/Webstart applications.
        // A quote from a FAQ at java.sun.com:
        //
        // Java Web Start uses a user-level classloader to load all the application resources specified in the JNLP file.
        // This classloader implements the security model and the downloading model defined by the JNLP specification.
        // This is no different than how the AppletViewer or the Java Plug-In works.
        // This has the, unfortunate, side-effect that Class.forName will not find any resources that are defined in the
        // JNLP file. The same is true for looking up resources and classes using the system class loader
        // (ClassLoader.getSystemClassLoader).
        //
        return defaultClassLoader;
    }


    /**
     * This method is similar to {@link #getResourceAsURL(String)} except that it looks for a resource with a given
     * name in a specific package.
     *
     * @param ppackage package serving as a base folder for the resource to retrieve
     * @param name name of the resource in the package. This is a filename only, not a path.
     * @return a URL pointing to the resource, or <code>null</code> if the resource couldn't be located
     */
    public static URL getPackageResourceAsURL(Package ppackage, String name) {
        return getPackageResourceAsURL(ppackage, name, getDefaultClassLoader(), null);
    }

    /**
     * This method is similar to {@link #getResourceAsURL(String)} except that it looks for a resource with a given
     * name in a specific package.
     *
     * @param ppackage package serving as a base folder for the resource to retrieve
     * @param classLoader the ClassLoader used for locating the resource. May not be <code>null</code>.
     * @param rootPackageFile root package location (JAR file or directory) that limits the scope of the search,
     * <code>null</code> to look for the resource in the whole class path.
     * @param name name of the resource in the package. This is a filename only, not a path.
     * @return a URL pointing to the resource, or <code>null</code> if the resource couldn't be located
     * @see #getRootPackageAsFile(Class)
     */
    public static URL getPackageResourceAsURL(Package ppackage, String name, ClassLoader classLoader, AbstractFile rootPackageFile) {
        return ResourceLoader.getResourceAsURL(getRelativePackagePath(ppackage)+"/"+name, classLoader, rootPackageFile);
    }

    /**
     * Shorthand for {@link #getResourceAsURL(String, ClassLoader, AbstractFile)} called with the
     * {@link #getDefaultClassLoader() default class loader} and a <code>null</code> root package file.
     *
     * @param path forward slash-separated path to the resource to look for, relative to the parent classpath
     * location (directory or JAR file) that contains it.
     * @return a URL pointing to the resource, or <code>null</code> if the resource couldn't be located
     */
    public static URL getResourceAsURL(String path) {
        return getResourceAsURL(path, getDefaultClassLoader(), null);
    }

    /**
     * Finds the resource with the given path and returns a URL pointing to its location, or <code>null</code>
     * if the resource couldn't be located. The given <code>ClassLoader</code> is used for locating the resource.
     *
     * <p>The given resource path must be forward slash (<code>/</code>) separated. It may or may not start with a
     * leading forward slash character, this doesn't affect the way it is interpreted.</p>
     *
     * <p>The <code>rootPackageFile</code> argument can be used to limit the scope of the search to a specific
     * location (JAR file or directory) in the classpath: resources located outside of this location will not be matched.
     * This avoids potential ambiguities that can arise if the specified resource path exists in several locations.
     * If this parameter is <code>null</code>, the resource is looked up in the whole class path. In that case and if
     * several resources with the specified path exist, the choice of the resource to return is arbitrary.</p>
     *
     * @param path forward slash-separated path to the resource to look for, relative to the parent classpath
     * location (directory or JAR file) that contains it.
     * @param classLoader the ClassLoader used for locating the resource. May not be <code>null</code>.
     * @param rootPackageFile root package location (JAR file or directory) that limits the scope of the search,
     * <code>null</code> to look for the resource in the whole class path.
     * @return a URL pointing to the resource, or <code>null</code> if the resource couldn't be located
     */
    public static URL getResourceAsURL(String path, ClassLoader classLoader, AbstractFile rootPackageFile) {
        path = removeLeadingSlash(path);

        if(rootPackageFile==null)
            return classLoader.getResource(path);

        String separator = rootPackageFile.getSeparator();
        String nativePath;
        if(separator.equals("/"))
            nativePath = path;
        else
            nativePath = path.replace("/", separator);

        try {
            // Iterate through all resources that match the given path, and return the one located inside the
            // given root package file.
            Enumeration<URL> resourceEnum = classLoader.getResources(path);
            String rootPackagePath = rootPackageFile.getAbsolutePath();
            String resourcePath = rootPackageFile.getAbsolutePath(true)+nativePath;
            URL resourceURL;
            while(resourceEnum.hasMoreElements()) {
                resourceURL = resourceEnum.nextElement();

                if("jar".equals(resourceURL.getProtocol())) {
                    if(getJarFilePath(resourceURL).equals(rootPackagePath))
                        return resourceURL;
                }
                else {
                    if(normalizeUrlPath(getDecodedURLPath(resourceURL)).equals(resourcePath))
                        return resourceURL;
                }
            }
        }
        catch(IOException e) {
            LOGGER.info("Failed to lookup resource {}", path, e);
            return null;
        }

        return null;
    }

    /**
     * This method is similar to {@link #getResourceAsStream(String)} except that it looks for a resource with a given
     * name in a specific package.
     *
     * @param ppackage package serving as a base folder for the resource to retrieve
     * @param name name of the resource in the package. This is a filename only, not a path.
     * @return an InputStream that allows to read the resource, or <code>null</code> if the resource couldn't be located
     */
    public static InputStream getPackageResourceAsStream(Package ppackage, String name) {
        return getPackageResourceAsStream(ppackage, name, getDefaultClassLoader(), null);
    }

    /**
     * This method is similar to {@link #getResourceAsStream(String, ClassLoader, AbstractFile)} except that it looks
     * for a resource with a given name in a specific package.
     *
     * @param ppackage package serving as a base folder for the resource to retrieve
     * @param name name of the resource in the package. This is a filename only, not a path.
     * @param classLoader the ClassLoader used for locating the resource. May not be <code>null</code>.
     * @param rootPackageFile root package location (JAR file or directory) that limits the scope of the search,
     * <code>null</code> to look for the resource in the whole class path.
     * @return an InputStream that allows to read the resource, or <code>null</code> if the resource couldn't be located
     */
    public static InputStream getPackageResourceAsStream(Package ppackage, String name, ClassLoader classLoader, AbstractFile rootPackageFile) {
        return ResourceLoader.getResourceAsStream(getRelativePackagePath(ppackage)+"/"+name, classLoader, rootPackageFile);
    }

    /**
     * Shorthand for {@link #getResourceAsStream(String, ClassLoader, AbstractFile)} called with the
     * {@link #getDefaultClassLoader() default class loader} and a <code>null</code> root package file.
     *
     * @param path forward slash-separated path to the resource to look for, relative to the parent classpath
     * location (directory or JAR file) that contains it.
     * @return an InputStream that allows to read the resource, or <code>null</code> if the resource couldn't be located
     */
    public static InputStream getResourceAsStream(String path) {
        return getResourceAsStream(path, getDefaultClassLoader(), null);
    }

    /**
     * Finds the resource with the given path and returns an <code>InputStream</code> to read it, or <code>null</code>
     * if the resource couldn't be located. The given <code>ClassLoader</code> is used for locating the resource.
     *
     * <p>The given resource path must be forward slash (<code>/</code>) separated. It may or may not start with a
     * leading forward slash character, this doesn't affect the way it is interpreted.</p>
     *
     * <p>The <code>rootPackageFile</code> argument can be used to limit the scope of the search to a specific
     * location (JAR file or directory) in the classpath: resources located outside of this location will not be matched.
     * This avoids potential ambiguities that can arise if the specified resource path exists in several locations.
     * If this parameter is <code>null</code>, the resource is looked up in the whole class path. In that case and if
     * several resources with the specified path exist, the choice of the resource to return is arbitrary.</p>
     *
     * @param path forward slash-separated path to the resource to look for, relative to the parent classpath
     * location (directory or JAR file) that contains it.
     * @param classLoader the ClassLoader used for locating the resource. May not be <code>null</code>.
     * @param rootPackageFile root package location (JAR file or directory) that limits the scope of the search,
     * <code>null</code> to look for the resource in the whole class path.
     * @return an InputStream that allows to read the resource, or <code>null</code> if the resource couldn't be located
     */
    public static InputStream getResourceAsStream(String path, ClassLoader classLoader, AbstractFile rootPackageFile) {
        try {
            URL resourceURL = getResourceAsURL(path, classLoader, rootPackageFile);
            return resourceURL==null?null:resourceURL.openStream();
        }
        catch(IOException e) {
            return null;
        }
    }

    /**
     * This method is similar to {@link #getResourceAsFile(String)} except that it looks for a resource with a given
     * name in a specific package.
     *
     * @param ppackage package serving as a base folder for the resource to retrieve
     * @param name name of the resource in the package. This is a filename only, not a path.
     * @return an AbstractFile that represents the resource, or <code>null</code> if the resource couldn't be located
     */
    public static AbstractFile getPackageResourceAsFile(Package ppackage, String name) {
        return getPackageResourceAsFile(ppackage, name, getDefaultClassLoader(), null);
    }

    /**
     * This method is similar to {@link #getResourceAsFile(String, ClassLoader, AbstractFile)} except that it looks for
     * a resource with a given name in a specific package.
     *
     * @param ppackage package serving as a base folder for the resource to retrieve
     * @param name name of the resource in the package. This is a filename only, not a path.
     * @param classLoader the ClassLoader used for locating the resource. May not be <code>null</code>.
     * @param rootPackageFile root package location (JAR file or directory) that limits the scope of the search,
     * <code>null</code> to look for the resource in the whole class path.
     * @return an AbstractFile that represents the resource, or <code>null</code> if the resource couldn't be located
     */
    public static AbstractFile getPackageResourceAsFile(Package ppackage, String name, ClassLoader classLoader, AbstractFile rootPackageFile) {
        return ResourceLoader.getResourceAsFile(getRelativePackagePath(ppackage)+"/"+name, classLoader, rootPackageFile);
    }

    /**
     * Shorthand for {@link #getResourceAsFile(String, ClassLoader, AbstractFile)} called with the
     * {@link #getDefaultClassLoader() default class loader} and a <code>null</code> root package file.
     *
     * @param path forward slash-separated path to the resource to look for, relative to the parent classpath
     * location (directory or JAR file) that contains it.
     * @return an AbstractFile that represents the resource, or <code>null</code> if the resource couldn't be located
     */
    public static AbstractFile getResourceAsFile(String path) {
        return getResourceAsFile(removeLeadingSlash(path), getDefaultClassLoader(), null);
    }

    /**
     * Finds the resource with the given path and returns an {@link AbstractFile} that gives full access to it,
     * or <code>null</code> if the resource couldn't be located. The given <code>ClassLoader</code> is used for locating
     * the resource.
     *
     * <p>The given resource path must be forward slash (<code>/</code>) separated. It may or may not start with a 
     * leading forward slash character, this doesn't affect the way it is interpreted.</p>
     *
     * <p>It is worth noting that this method may be slower than {@link #getResourceAsStream(String)} if
     * the resource is located inside a JAR file, because the Zip file headers will have to be parsed the first time
     * the archive is accessed. Therefore, the latter approach should be favored if the file is simply used for
     * reading the resource.</p>
     *
     * <p>The <code>rootPackageFile</code> argument can be used to limit the scope of the search to a specific
     * location (JAR file or directory) in the classpath: resources located outside of this location will not be matched.
     * This avoids potential ambiguities that can arise if the specified resource path exists in several locations.
     * If this parameter is <code>null</code>, the resource is looked up in the whole class path. In that case and if
     * several resources with the specified path exist, the choice of the resource to return is arbitrary.</p>
     *
     * @param path forward slash-separated path to the resource to look for, relative to the parent classpath
     * location (directory or JAR file) that contains it.
     * @param classLoader the ClassLoader is used for locating the resource
     * @param rootPackageFile root package location (JAR file or directory) that limits the scope of the search,
     * <code>null</code> to look for the resource in the whole class path.
     * @return an AbstractFile that represents the resource, or <code>null</code> if the resource couldn't be located
     */
    public static AbstractFile getResourceAsFile(String path, ClassLoader classLoader, AbstractFile rootPackageFile) {
        if(classLoader==null)
            classLoader = getDefaultClassLoader();

        path = removeLeadingSlash(path);

        URL aClassURL = getResourceAsURL(path, classLoader, rootPackageFile);
        if(aClassURL==null)
            return null;        // no resource under that path

        if("jar".equals(aClassURL.getProtocol())) {
            try {
                return ((AbstractArchiveFile)FileFactory.getFile(getJarFilePath(aClassURL))).getArchiveEntryFile(path);
            }
            catch(Exception e) {
                // Shouldn't normally happen, unless the JAR file is corrupt or cannot be parsed by the file API
                return null;
            }
        }

        return FileFactory.getFile(getLocalFilePath(aClassURL));
    }


    /**
     * Returns an {@link AbstractFile} to the root package of the given <code>Class</code>. For example, if the
     * specified <code>Class</code> is <code>java.lang.Object</code>'s, the returned file will be the Java runtime
     * JAR file, which on most platforms is <code>$JAVA_HOME/lib/jre/rt.jar</code>.<br>
     * The returned file can be used to list or manipulate all resource files contained in a particular classpath's
     * location, including the .class files.
     *
     * @param aClass the class for which to locate the root package.
     * @return an AbstractFile to the root package of the given <code>Class</code>
     */
    public static AbstractFile getRootPackageAsFile(Class<?> aClass) {
        ClassLoader classLoader = aClass.getClassLoader();
        if(classLoader==null)
            classLoader = getDefaultClassLoader();

        String aClassRelPath = getRelativeClassPath(aClass);
        URL aClassURL = getResourceAsURL(aClassRelPath, classLoader, null);

        if(aClassURL==null)
            return null;    // no resource under that path

        if("jar".equals(aClassURL.getProtocol()))
            return FileFactory.getFile(getJarFilePath(aClassURL));

        String aClassPath = getLocalFilePath(aClassURL);
        return FileFactory.getFile(aClassPath.substring(0, aClassPath.length()-aClassRelPath.length()));
    }

    /**
     * Returns a path to the given package. The returned path is relative, forward slash-separated and does not end
     * with a trailing separator. For example, if the package <code>com.mucommander.commons.file</code> is passed, the returned
     * path will be <code>com/mucommander/commons/file</code>.
     *
     * @param ppackage the package for which to return a path
     * @return a path to the given package
     */
    public static String getRelativePackagePath(Package ppackage) {
        return ppackage.getName().replace('.', '/');
    }

    /**
     * Returns a path to the given class. The returned path is relative, forward slash-separated. For example, if the
     * class <code>com.mucommander.commons.file.AbstractFile</code> is passed,  the returned path will be
     * <code>com/mucommander/commons/file/AbstractFile.class</code>.
     *
     * @param cclass the class for which to return a path
     * @return a path to the given package
     */
    public static String getRelativeClassPath(Class<?> cclass) {
        return cclass.getName().replace('.', '/')+".class";
    }


    /////////////////////
    // Private methods //
    /////////////////////

    /**
     * Extracts and returns the path to the JAR file from a URL that points to a resource inside a JAR file.
     * The returned path is in a format that {@link FileFactory} can turn into an {@link AbstractFile}.
     *
     * @param url a URL that points to a resource inside a JAR file
     * @return returns the path to the JAR file
     */
    private static String getJarFilePath(URL url) {
        // URL-decode the path
        String path = getDecodedURLPath(url);

        // Here are a couple examples of such paths:
        // file:/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Classes/classes.jar!/java/lang/Object.class
        // http://www.mucommander.com/webstart/nightly/mucommander.jar!/com/mucommander/RuntimeConstants.class

        int pos = path.indexOf(".jar!");
        if(pos==-1)
            return path;

        // Strip out the part after ".jar" and normalize the path
        return normalizeUrlPath(path.substring(0, pos+4));
    }

    /**
     * Extracts and returns the path to a local file represented by the given URL.
     * The returned path is in a format that {@link FileFactory} can turn into an {@link AbstractFile}.
     *
     * @param url a URL that points to a resource inside a JAR file
     * @return returns the path to the JAR file
     */
    private static String getLocalFilePath(URL url) {
        // Here's an example of such a path under Windows:
        // /C:/cygwin/home/Administrator/mucommander/tmp/compile/classes/

        // URL-decode the path and normalize it
        return normalizeUrlPath(getDecodedURLPath(url));
    }

    /**
     * Removes any leading slash from the given path and returns it. Does nothing if the path does not have a
     * leading path.
     *
     * @param path the path to normalize
     * @return the path without a leading slash
     */
    private static String removeLeadingSlash(String path) {
        return PathUtils.removeLeadingSeparator(path, "/");
    }

    /**
     * Normalizes the specified path issued from a <code>java.net.URL</code> and returns it.
     * The returned path is in a format that {@link FileFactory} can turn into an {@link AbstractFile}.
     *
     * @param path the URL path to normalize
     * @return the normalized path
     */
    private static String normalizeUrlPath(String path) {
        // Don't touch http/https URLs
        if(path.startsWith("http:") || path.startsWith("https:"))
            return path;

        // Remove the leading "file:" (if any)
        if(path.startsWith("file:"))
            path = path.substring(5, path.length());

        // Under platforms that use root drives (Windows and OS/2), strip out the leading '/'
        if(LocalFile.hasRootDrives() && path.startsWith("/"))
            path = removeLeadingSlash(path);

        // Use the local file separator
        String separator = LocalFile.SEPARATOR;
        if(!"/".equals(separator))
            path = path.replace("/", separator);

        return path;
    }

    /**
     * Returns the URL-decoded path of the given <code>java.net.URL</code>. The encoding used for URL-decoding is
     * <code>UTF-8</code>.
     *
     * @param url the URL for which to decode the path
     * @return the URL-decoded path of the given URL
     */
    private static String getDecodedURLPath(URL url) {
        try {
            // Decode the URL's path which may contain URL-encoded characters such as %20 for spaces, or non-ASCII
            // characters.
            // Note: the Java API's javadoc doesn't specify which encoding has been used to encoded URL paths.
            // The only indication is in URLDecoder#decode(String, String) javadoc which says:
            // "The World Wide Web Consortium Recommendation states that UTF-8 should be used. Not doing so may
            // introduce incompatibilites."
            // Also Note that URLDecoder#decode(String) uses System.getProperty("file.encoding") as the default encoding,
            // using this value has been tested without luck under Mac OS X where the value equals "MacRoman" but
            // URL are actually encoded in UTF-8. The bottom line is that we blindly use UTF-8 to decode resource URLs.
            return URLDecoder.decode(url.getPath(), "UTF-8");
        }
        catch(UnsupportedEncodingException e) {
            // This should never happen, UTF-8 is necessarily supported by the Java runtime
            return null;
        }
    }
}
