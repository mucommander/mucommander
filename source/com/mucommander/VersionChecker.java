
package com.mucommander;

import java.net.URL;
import java.net.URLConnection;
import java.io.*;
import java.util.Hashtable;
import com.muxml.*;

/**
 * Checks latest version by parsing an XML document located at some remote URL.
 *
 * @author Maxence Bernard
 */
public class VersionChecker implements ContentHandler {

    // Was this URL until 0.6 release 2
    //    private final static String VERSION_DOCUMENT_URL = "http://mu-j.com/mucommander/version/version.xml";
    private final static String VERSION_DOCUMENT_URL = "http://mucommander.com/version/version.xml";

    private static String latestVersion;
    private static String downloadURL;
    private static String elementName;

    private VersionChecker() {
    }

    /**
     * Parses the remote XML document containing version information about muCommander.<br>
     * This method has to be called before any call to <code>getLatestVersion()</code> or
     * <code>getDownloadURL()</code> is made.
     *
     * @throw Exception if the document could not be parsed (connection down, 404...) or if the XML
     * document did not contain any version information.
     */
    public static void getVersionInformation() throws Exception {
        Parser parser = new Parser();
        URLConnection conn = new URL(VERSION_DOCUMENT_URL).openConnection();
		
        // Set user-agent header
        conn.setRequestProperty("user-agent", Launcher.USER_AGENT);

        // Establish connection
        conn.connect();

        InputStream in = conn.getInputStream();
        // Use UTF-8 encoding
        parser.parse(in, new VersionChecker(), "UTF-8");
        in.close();
        
        if(latestVersion==null || latestVersion.equals(""))
            throw new Exception();
    }
    
    
    /**
     * Returns latest version of muCommander.
     */
    public static String getLatestVersion() {
        return latestVersion;
    }
    
	
    /**
     * Returns true if a new version is available (a version with a greater number than the one currently running).
     */
    public static boolean newVersionAvailable() {
        String thisVersion = Launcher.MUCOMMANDER_VERSION.trim().toLowerCase();
	
        // Versions are perfectly equal (both version strings are trimmed and lower case) -> no new version
        if(latestVersion.equals(thisVersion))
            return false;

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("newVersionAvailable: latestVersion="+latestVersion+" ("+parseVersion(latestVersion)+") thisVersion="+thisVersion+" ("+parseVersion(thisVersion)+")");

        // This version number is greater than latestVersion (e.g. 0.7rc1 VS 0.6) -> no new version
        if(parseVersion(thisVersion)>parseVersion(latestVersion))
            return false;
		
        // For all other cases (this version number is lower than latest version: 0.6/0.7 or version strings differ: 0.6a/0.6b) -> new version available!
        return true;
    }
	
	
    /**
     * Parse version number contained in given version string, ignoring non-numerical characters, and returns it as a float.
     */
    private static float parseVersion(String version) {
        StringBuffer sb = new StringBuffer();
        int versionLength = version.length();
        char c;
        boolean dotAdded = false;
        for(int i=0; i<versionLength; i++) {
            c = version.charAt(i);
            if(c>='0' && c<='9')
                sb.append(c);
            else if(c=='.') {
                if(!dotAdded) {
                    sb.append('.');
                    dotAdded = true;
                }
            }
            else
                break;
        }

        try {
            return Float.parseFloat(sb.toString());
        }
        catch(NumberFormatException e) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("NumberFormatException while parsing version number: "+sb.toString()+" returning 0");
            return 0;
        }
    }
	
	
    /**
     * Returns muCommander download URL.
     */
    public static String getDownloadURL() {
        return downloadURL;
    }


    public void startDocument() {
        latestVersion = "";
        downloadURL = "";
    }
    
    public void characters(String s) {
        if(elementName.equals("latest_version"))
            latestVersion += s;
        else if(elementName.equals("download_url"))
            downloadURL += s;
    }

    public void startElement(String uri, String name, Hashtable attValues, Hashtable attURIs) {
        elementName = name;
    }

    public void endElement(String uri, String name) {
        elementName = "";
    }

    public void endDocument() {
        latestVersion = latestVersion.toLowerCase().trim();
        downloadURL = downloadURL.trim();
		
        if(com.mucommander.Debug.ON) {
            com.mucommander.Debug.trace("download URL -"+downloadURL+"-");
            com.mucommander.Debug.trace("latestVersion -"+latestVersion+"-");
        }
    }


    /** Test class to ensure that version number parsing works OK */
    public static void main(String args[]) {
        String versions[] = new String[]{"0.6", "0.6.1", "0.61", "0.7a", "0.7rc1", "1.1", "10.3.5"};
        for(int i=0; i<versions.length; i++)
            System.out.println("parseVersion("+versions[i]+") = "+parseVersion(versions[i]));
    }
}
