package com.mucommander.file.util;

import java.io.InputStream;
import java.net.URL;

/**
 * This class provides convience methods to load resources located within the classpath.
 * These methods can be used indifferently to load resources from a local filesystem or from a JAR file.
 *
 * @author Maxence Bernard
 */
public class ResourceLoader {

    /**
     * Finds a resource file with a given path, relative to the classpath.
     * Returns a URL to the resource or null if no resource file with this path is found.
     * 
     * @param path a path to a resource file, relative to the classpath
     * @return a URL to the resource file or null if no resource file with this path is found
     */
    public static URL getResource(String path) {
        return ResourceLoader.class.getResource(path);
    }


    /**
     * Finds a resource file with a given path, relative to the classpath.
     * Returns an InputStream to read the resource, or null if no resource file with this path is found.
     * @param path a path to a resource file, relative to the classpath
     * @return an InputStream to read the resource file, or null if no resource file with this path is found
     */
    public static InputStream getResourceAsStream(String path) {
        return ResourceLoader.class.getResourceAsStream(path);
    }
}
