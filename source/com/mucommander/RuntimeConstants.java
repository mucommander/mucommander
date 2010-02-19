/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

import com.mucommander.file.util.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

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
    public  static final String BUILD_DATE;
    /** Copyright information (<code>YYYY-YYYY</code>). */
    public  static final String COPYRIGHT;
    /** String describing the software (<code>muCommander vMAJOR.MINOR.DEV</code>). */
    public  static final String APP_STRING;
    /** String describing the muCommander build number. */
    public  static final String BUILD_NUMBER;

    

    // - Initialisation ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    static {
        Attributes  attributes; // JAR file's manifest's attributes.
        InputStream in;

        in         = null;
        attributes = null;
        try {
            if((in = ResourceLoader.getResourceAsStream("META-INF/MANIFEST.MF", ResourceLoader.getDefaultClassLoader(), ResourceLoader.getRootPackageAsFile(RuntimeConstants.class))) != null) {
                Manifest manifest;

                manifest = new Manifest();
                manifest.read(in);
                attributes = manifest.getMainAttributes();
            }
            else {
                AppLogger.warning("MANIFEST.MF not found, default values will be used");
            }
        }
        catch(Exception e) {
            AppLogger.warning("Failed to read MANIFEST.MF, default values will be used", e);
            // Ignore this, attributes is already set to null.
        }
        finally {
            if(in != null) {
                try {in.close();}
                catch(IOException e) {
                    // Ignore this, we don't really care if we can't close this stream.
                }
            }
        }
        
        // No MANIFEST.MF found, use default values.
        if(attributes == null) {
            VERSION = "?";
            COPYRIGHT    = "2002-" + Calendar.getInstance().get(Calendar.YEAR);
            // We use a date that we are sure is later than the latest version to trigger the version checker.
            // After all, the JAR appears to be corrupt and should be upgraded.
            BUILD_DATE = DEFAULT_RELEASE_DATE;
            VERSION_URL  = HOMEPAGE_URL + "/version/version.xml";
            BUILD_NUMBER = "?";
        }

        // A MANIFEST.MF file was found, extract data from it.
        else {
            VERSION      = getAttribute(attributes, "Specification-Version");
            BUILD_DATE = getAttribute(attributes, "Build-Date");
            VERSION_URL  = getAttribute(attributes, "Build-URL");
            BUILD_NUMBER = getAttribute(attributes, "Implementation-Version");
            // Protection against corrupt manifest files.
            COPYRIGHT    = BUILD_DATE.length() > 4 ? BUILD_DATE.substring(0, 4) : DEFAULT_RELEASE_DATE;

        }
        APP_STRING = "muCommander v" + VERSION;
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
