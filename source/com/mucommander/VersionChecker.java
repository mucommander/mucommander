
package com.mucommander;

import java.net.URL;
import java.net.URLConnection;
import java.io.*;
import java.util.Hashtable;
import com.muxml.*;

/**
 * Checks latest version by parsing an XML document located at some remote URL.
 */
public class VersionChecker implements ContentHandler {

// Until 0.6 release 2
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
		parser.parse(in, new VersionChecker());
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
        if(com.mucommander.Debug.ON) {
            System.out.println("download URL -"+downloadURL+"-");
            System.out.println("latestVersion -"+latestVersion+"-");
        }
    }
}
