package com.mucommander;

import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;

/**
 * Retrieves information about the latest release of muCommander.
 * <p>
 * The {@link com.mucommander.RuntimeConstants#VERSION_URL} URL contains information
 * about the latest release of muCommander:<br/>
 * - date of latest release.<br/>
 * - latest official version.<br/>
 * - where to download the latest version from.<br/>
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
 * muCommander is considered up to date if:<br/>
 * - the {@link com.mucommander.RuntimeConstants#VERSION local version} is
 *   not smaller than the remote one.<br/>
 * - the {@link com.mucommander.RuntimeConstants#RELEASE_DATE local release date} is
 *   not smaller than the remote one.<br/>
 * While comparing release dates seems a bit odd - after all, if a new version is release,
 * a new version number should be created. However, it's possible to download development
 * versions of the current release, and those might be updated almost daily. Comparing dates
 * makes it possible to automate the whole process without having to worry about out version
 * numbers growing silly.
 * </p>
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class VersionChecker implements ContentHandler {
    // - XML parsing states -----------------------------------------------------
    // --------------------------------------------------------------------------
    /** Currently parsing the version tag. */
    public static final int STATE_VERSION = 1;
    /** Currently parsing the URL tag. */
    public static final int STATE_URL     = 2;
    /** Currently parsing the date tag. */
    public static final int STATE_DATE    = 3;
    /** We're not quite sure what we're parsing. */
    public static final int STATE_UNKNOWN = 0;



    // - Instance fields --------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Remote version number. */
    private String latestVersion;
    /** Where to download the latest version. */
    private String downloadURL;
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
        Parser         parser; // Used to parse the XML file.
        URLConnection  conn;   // Connection to the remote XML file.
        InputStream    in;     // Input stream on the remote XML file.
        VersionChecker instance;

        if(Debug.ON)
            Debug.trace("Opening connection to " + RuntimeConstants.VERSION_URL);

        // Initialisation.
        parser = new Parser();
        conn   = new URL(RuntimeConstants.VERSION_URL).openConnection();
        conn.setRequestProperty("user-agent", PlatformManager.USER_AGENT);

        // Parses the remote XML file using UTF-8 encoding.
        conn.connect();
        in = conn.getInputStream();
        parser.parse(in, instance = new VersionChecker(), "UTF-8");
        in.close();

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
            return releaseDate.compareTo(RuntimeConstants.RELEASE_DATE) > 0;
        }
        return false;
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
    public void startDocument() {
        latestVersion = "";
        downloadURL   = "";
        releaseDate   = "";
    }

    /**
     * Notifies the parser of CDATA.
     */
    public void characters(String s) {
        if(state == STATE_VERSION)
            latestVersion += s;
        else if(state == STATE_URL)
            downloadURL += s;
        else if(state == STATE_DATE)
            releaseDate += s;
    }

    /**
     * Notifies the parser that a new tag is starting.
     */
    public void startElement(String uri, String name, Hashtable attValues, Hashtable attURIs) {
        // Checks whether we know the tag and updates the current state.
        if(name.equals(com.mucommander.ant.version.VersionConstants.VERSION_TAG))
            state = STATE_VERSION;
        else if(name.equals(com.mucommander.ant.version.VersionConstants.URL_TAG))
            state = STATE_URL;
        else if(name.equals(com.mucommander.ant.version.VersionConstants.DATE_TAG))
            state = STATE_DATE;
        else
            state = STATE_UNKNOWN;
    }

    /**
     * Notifies the parser that the current element is finished.
     */
    public void endElement(String uri, String name) {state = STATE_UNKNOWN;}

    /**
     * Notifies the parser that XML parsing is finished.
     */
    public void endDocument() {
        // Make sure we're not keep meaningless whitecase characters in the data.
        latestVersion = latestVersion.toLowerCase().trim();
        downloadURL   = downloadURL.trim();
        releaseDate   = releaseDate.trim();

        // Logs the data if in debug mode.
        if(com.mucommander.Debug.ON) {
            com.mucommander.Debug.trace("download URL:  "  + downloadURL);
            com.mucommander.Debug.trace("latestVersion: " + latestVersion);
            com.mucommander.Debug.trace("releaseDate:   "   + releaseDate);
        }
    }
}
