/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import java.io.InputStream;

import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.mucommander.commons.file.FileFactory;

/**
 * Retrieves information about the latest release of muCommander.
 * <p>
 * The {@link com.mucommander.RuntimeConstants#VERSION_URL} URL contains information
 * about the latest release of muCommander:<br>
 * - date of latest release.<br>
 * - latest official version.<br>
 * - where to download the latest version from.<br>
 * This class is used to access those informations and compare them with what is known
 * of the current one, making it possible to notify users of new releases.
 * </p>
 * <p>
 * Checking for new releases is a fairly straightforward process, and can be done
 * with a few lines of code:
 * <pre>
 * VersionChecker version;
 *
 * try {
 *     version = VersionChecker.getInstance();
 *     if(version.isNewVersionAvailable())
 *         System.out.println("A new version of muCommander is available");
 *     else
 *         System.out.println("You've got the latest muCommander version");
 *    }
 * catch(Exception e) {System.err.println("An error occured.");}
 * </pre>
 * </p>
 * <p>
 * muCommander is considered up to date if:<br>
 * - the {@link com.mucommander.RuntimeConstants#VERSION local version} is
 *   not smaller than the remote one.<br>
 * - the {@link com.mucommander.RuntimeConstants#BUILD_DATE local release date} is
 *   not smaller than the remote one.<br>
 * While comparing release dates seems a bit odd - after all, if a new version is release,
 * a new version number should be created. However, it's possible to download development
 * versions of the current release, and those might be updated almost daily. Comparing dates
 * makes it possible to automate the whole process without having to worry about out version
 * numbers growing silly.
 * </p>
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class VersionChecker extends DefaultHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(VersionChecker.class);
	
    // - XML structure ----------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Root XML element. */
    public static final String ROOT_ELEMENT    = "mucommander";
    /** Version XML element. */
    public static final String VERSION_ELEMENT = "latest_version";
    /** Download URL XML element. */
    public static final String DOWNLOAD_URL_ELEMENT = "download_url";
    /** JAR URL XML element. */
    public static final String JAR_URL_ELEMENT = "jar_url";
    /** Date XML element. */
    public static final String DATE_ELEMENT    = "release_date";



    // - XML parsing states -----------------------------------------------------
    // --------------------------------------------------------------------------
    /** Currently parsing the version tag. */
    public static final int STATE_VERSION      = 1;
    /** Currently parsing the download URL tag. */
    public static final int STATE_DOWNLOAD_URL = 2;
    /** Currently parsing the download URL tag. */
    public static final int STATE_JAR_URL      = 3;
    /** Currently parsing the date tag. */
    public static final int STATE_DATE         = 4;
    /** We're not quite sure what we're parsing. */
    public static final int STATE_UNKNOWN      = 5;



    // - Instance fields --------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Remote version number. */
    private String latestVersion;
    /** Where to download the latest version. */
    private String downloadURL;
    /** URL to the latest JAR file. */
    private String jarURL;
    /** Remote release date. */
    private String releaseDate;
    /** Current state the parser is in. */
    private int    state;



    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Creates a new version checker instance.
     */
    private VersionChecker() {}

    /**
     * Retrieves a description of the latest released version of muCommander.
     * @return              a description of the latest released version of muCommander.
     * @exception Exception thrown if any error happens while retrieving the remote version.
     */
    public static VersionChecker getInstance() throws Exception {
        VersionChecker instance;
        InputStream    in;     // Input stream on the remote XML file.

        LOGGER.debug("Opening connection to " + RuntimeConstants.VERSION_URL);

        // Parses the remote XML file using UTF-8 encoding.
        in = FileFactory.getFile(RuntimeConstants.VERSION_URL).getInputStream();
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(in, instance = new VersionChecker());
        }
        catch(Exception e) {
            LOGGER.debug("Failed to read version XML file at "+RuntimeConstants.VERSION_URL, e);
            throw e;
        }
        finally {
            in.close();
        }

        // Makes sure we retrieved the information we were looking for.
        // We're not checking the release date as older version of muCommander
        // didn't use it.
        if(instance.latestVersion == null || instance.latestVersion.equals("") ||
           instance.downloadURL == null   || instance.downloadURL.equals(""))
            throw new Exception();

        return instance;
    }


    // - Remote version information ---------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Checks whether the remote version is newer than the current one.
     * @return <code>true</code> if the remote version is newer than the current one,
     *         <code>false</code> otherwise.
     */
    public boolean isNewVersionAvailable() {
        // If the local and remote versions are the same, compares release dates.
        if(latestVersion.equals(RuntimeConstants.VERSION.trim().toLowerCase())) {
            // This ensures backward compatiblity - if the remote version file does not contain
            // release date information, ignore it.
            if(releaseDate.equals(""))
                return true;

            // Checks whether the remote release date is later than the current release date.
            return releaseDate.compareTo(RuntimeConstants.BUILD_DATE) > 0;
        }
        return true;
    }

    /**
     * Returns the version number of the latest muCommander release.
     * @return the version number of the latest muCommander release.
     */
    public String getLatestVersion() {return latestVersion;}

    /**
     * Returns the URL at which the latest version of muCommander can be downloaded.
     * @return the URL at which the latest version of muCommander can be downloaded.
     */
    public String getDownloadURL() {return downloadURL;}

    /**
     * Returns the URL to the latest JAR file, <code>null</code> if not available.
     * @return the URL to the latest JAR file.
     */
    public String getJarURL() {return jarURL;}

    /**
     * Returns the date at which the latest version of muCommander has been released.
     * <p>
     * The date format is YYYYMMDD.
     * </p>
     * @return the date at which the latest version of muCommander has been released.
     */
    public String getReleaseDate() {return releaseDate;}



    // - XML parsing ------------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Called when the XML document parsing has started.
     */
    @Override
    public void startDocument() {
        latestVersion = "";
        downloadURL   = "";
        jarURL        = "";
        releaseDate   = "";
    }

    /**
     * Notifies the parser of CDATA.
     */
    @Override
    public void characters(char[] ch, int offset, int length) {
        if(state == STATE_VERSION)
            latestVersion += new String(ch, offset, length);
        else if(state == STATE_DOWNLOAD_URL)
            downloadURL += new String(ch, offset, length);
        else if(state == STATE_JAR_URL)
            jarURL += new String(ch, offset, length);
        else if(state == STATE_DATE)
            releaseDate += new String(ch, offset, length);
    }

    /**
     * Notifies the parser that a new tag is starting.
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // Checks whether we know the tag and updates the current state.
        if(qName.equals(VERSION_ELEMENT))
            state = STATE_VERSION;
        else if(qName.equals(DOWNLOAD_URL_ELEMENT))
            state = STATE_DOWNLOAD_URL;
        else if(qName.equals(JAR_URL_ELEMENT))
            state = STATE_JAR_URL;
        else if(qName.equals(DATE_ELEMENT))
            state = STATE_DATE;
        else
            state = STATE_UNKNOWN;
    }

    /**
     * Notifies the parser that the current element is finished.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {state = STATE_UNKNOWN;}

    /**
     * Notifies the parser that XML parsing is finished.
     */
    @Override
    public void endDocument() {
        // Make sure we're not keep meaningless whitecase characters in the data.
        latestVersion = latestVersion.toLowerCase().trim();
        downloadURL   = downloadURL.trim();
        jarURL        = jarURL.trim();
        if("".equals(jarURL))
            jarURL = null;
        releaseDate   = releaseDate.trim();

        // Logs the data if in debug mode.
        LOGGER.debug("download URL: "  + downloadURL);
        LOGGER.debug("jar URL: "       + jarURL);
        LOGGER.debug("latestVersion: " + latestVersion);
        LOGGER.debug("releaseDate:   " + releaseDate);
    }
}
