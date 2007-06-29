/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

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
