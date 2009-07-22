/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

package com.mucommander;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * Defines various generic muCommander constants.
 * @author Nicolas Rinaudo
 */
public class RuntimeConstants {
    // - Constant paths ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Path to the muCommander dictionary. */
    public static final String DICTIONARY_FILE = "/dictionary.txt";
    /** Path to the themes directory. */
    public static final String THEMES_PATH     = "/themes";
    /** Path to the muCommander license file. */
    public static final String LICENSE         = "/license.txt";
    /** Default muCommander theme. */
    public static final String DEFAULT_THEME   = "Native";



    // - URLs ----------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Homepage URL. */
    public static final String HOMEPAGE_URL       = "http://www.mucommander.com";
    /** URL at which to download the latest version description. */
    public static final String VERSION_URL;
    /** URL of the muCommander forums. */
    public static final String FORUMS_URL         = HOMEPAGE_URL + "/forums/";
    /** URL at which to see the donation information. */
    public static final String DONATION_URL       = HOMEPAGE_URL + "/#donate";
    /** Bug tracker URL. */
    public static final String BUG_REPOSITORY_URL = HOMEPAGE_URL + "/bugs/";
    /** Documentation URL. */
    public static final String DOCUMENTATION_URL  = HOMEPAGE_URL + "/documentation/";



    // - Misc. ---------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Release date to use in case the JAR file doesn't contain the information.
     * This is guaranteed to trigger a software update - the JAR file is corrupt, so we might as well get the latest
     * version.
     */
    private static final String DEFAULT_RELEASE_DATE = "20020101";
    /** Current muCommander version (<code>MAJOR.MINOR.DEV</code>). */
    public  static final String VERSION;
    /** Date at which the build was generated (<code>YYYYMMDD</code>). */
    public  static final String RELEASE_DATE;
    /** Copyright information (<code>YYYY-YYYY</code>). */
    public  static final String COPYRIGHT;
    /** String describe the software (<code>muCommander vMAJOR.MINOR.DEV</code>). */
    public  static final String APP_STRING;



    // - Initialisation ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    static {
        JarFile    jar;        // Path to the muCommander JAR file.
        Attributes attributes; // JAR file's manifest's attributes.

        // Attempts to retrieve the MANIFEST.MF file.
        jar = getJarFile();
        try {attributes = jar == null ? null : jar.getManifest().getMainAttributes();}
        catch(IOException e) {attributes = null;}

        // No MANIFEST.MF found, use default values.
        if(attributes == null) {
            VERSION = "?";
            COPYRIGHT    = "2002-" + Calendar.getInstance().get(Calendar.YEAR);
            // We use a date that we are sure is later than the latest version to trigger the version checker.
            // After all, the JAR appears to be corrupt and should be upgraded.
            RELEASE_DATE = DEFAULT_RELEASE_DATE;
            VERSION_URL  = HOMEPAGE_URL + "/version/version.xml";
        }

        // A MANIFEST.MF file was found, extract data from it.
        else {
            VERSION      = getAttribute(attributes, "Specification-Version");
            RELEASE_DATE = getAttribute(attributes, "Build-Date");
            VERSION_URL  = getAttribute(attributes, "Build-URL");
            // Protection against corrupt manifest files.
            COPYRIGHT    = RELEASE_DATE.length() > 4 ? RELEASE_DATE.substring(0, 4) : DEFAULT_RELEASE_DATE;

        }
        APP_STRING = "muCommander v" + VERSION;
    }

    
    
    // - JAR file parsing ----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the application's jar file.
     * <p>
     * This code is also present in muCommander file API, but there's no guarantee that it has been properly initialised
     * at this point.
     * </p>
     * @return the application's jar file if found, <code>null</code> otherwise.
     */
    private static JarFile getJarFile() {
        URL url;

        // Retrieves the path to this class. If it's within a JAR, we're good.
        url = RuntimeConstants.class.getResource("/com/mucommander/RuntimeConstants.class");
        if(url.getProtocol().equals("jar")) {
            int    pos;
            String path;

            // Removes the bits of the path that are 'in' the jar.
            path = url.getPath();
            pos  = path.indexOf("!");
            if(pos == -1)
                return null;
            path = path.substring(0, pos);

            // Removes the file: bit of the URL.
            if(path.startsWith("file:"))
                path = path.substring(5);

            // Creates the actual JAR file instance.
            try {return new JarFile(path);}
            catch(IOException e) {return null;}
        }
        else
            return null;
    }

    /**
     * Extract the requested attribute value.
     * @param  attributes attributes from which to extract the requested value.
     * @param  name       name of the attribute to retrieve.
     * @return            the requested attribute value.
     */
    private static String getAttribute(Attributes attributes, String name) {
        String buffer;

        if((buffer = attributes.getValue(name)) == null)
            return "?";
        return buffer;
    }
}
