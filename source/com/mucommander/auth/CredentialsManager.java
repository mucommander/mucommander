
package com.mucommander.auth;

import com.mucommander.file.FileURL;
import com.mucommander.util.AlteredVector;
import com.mucommander.util.VectorChangeListener;
import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.io.BackupOutputStream;

import java.util.Vector;
import java.util.StringTokenizer;
import java.io.File;
import java.io.IOException;


/**
 * This class manages {@link MappedCredentials} instances (login/password pairs mapped to a location) used to connect to
 * authenticated file systems. It provides methods to find credentials matching a particular location and to
 * read and write credentials to an XML file.
 *
 * <p>Two types of {@link MappedCredentials} are used:
 * <ul>
 *  <li>persistent credentials: those are stored in an XML file when the application terminates, and loaded the next
 * time the application is started.
 *  <li>volatile credentials: those are lost when the application terminates
 * </ul>
 *
 * @author Maxence Bernard
 */
public class CredentialsManager implements VectorChangeListener {

    /** Volatile credentials, lost when the application terminates */
    private static Vector volatileCredentials = new Vector();

    /** Persistent credentials, stored to an XML file when the application terminates, and loaded the next
      * time the application is started */
    private static AlteredVector persistentCredentials = new AlteredVector();

    /** Credentials file location */
    private static File credentialsFile;

    /** Default credentials file name */
    private static final String DEFAULT_CREDENTIALS_FILENAME = "credentials.xml";

    /** True when changes were made after the credentials file was last saved */
    private static boolean saveNeeded;

    /** Create a singleton instance, needs to be referenced so that it's not garbage collected (AlteredVector
      * stores VectorChangeListener as weak references) */
    private static CredentialsManager singleton = new CredentialsManager();
    
    static {
        // Listen to changes made to the persistent entries vector
        persistentCredentials.addVectorChangeListener(singleton);
    }


    /**
     * Return a java.io.File instance that points to the credentials file location.
     */
    private static File getCredentialsFile() {
        if(credentialsFile == null)
            return new File(PlatformManager.getPreferencesFolder(), DEFAULT_CREDENTIALS_FILENAME);
        else
            return credentialsFile;
    }

    /**
     * Sets the path to the credentials file.
     *
     * @param path the path to the credentials file
     */
    public static void setCredentialsFile(String path) {
        credentialsFile = new File(path);
    }


    /**
     * Tries to load credentials from the credentials file if it exists, and reports any error that occur during parsing
     * to the standard output. Does nothing if the credentials file doesn't exist.
     */
    public static void loadCredentials() {
        File credentialsFile = getCredentialsFile();
        try {
            if(credentialsFile.exists()) {
                if(Debug.ON) Debug.trace("Found credentials file: "+credentialsFile.getAbsolutePath());
                // Parse the credentials file
                new CredentialsParser().parse(credentialsFile);
                if(Debug.ON) Debug.trace("Credentials file loaded.");
            }
            else {
                if(Debug.ON) Debug.trace("No credentials file found at "+credentialsFile.getAbsolutePath());
            }
        }
        catch(Exception e) {
            // Report on the standard output that something went wrong while parsing the credentials file
            // as this shouldn't normally happen
            System.out.println("An error occurred while loading credentials file "+credentialsFile.getAbsolutePath()+": "+e);			
        }
    }

    /**
     * Tries to write the credentials file. Unless the 'forceWrite' is set to true, the credentials file will be written
     * only if changes were made to persistent entries since last write.
     *
     * @param forceWrite if false, the credentials file will be written only if changes were made to persistent entries
     *  since last write, if true the file will always be written.
     */
    public static void writeCredentials(boolean forceWrite) {
        // Write credentials file only if changes were made to persistent entries since last write, or if write is forced
        if(!(forceWrite || saveNeeded))
            return;

        BackupOutputStream out = null;
        try {
            credentialsFile = getCredentialsFile();
            CredentialsWriter.write(out = new BackupOutputStream(credentialsFile));
            if(Debug.ON) Debug.trace("Credentials file saved successfully.");
            out.close();

            saveNeeded = false;
        }
        catch(IOException e) {
            if(out != null) {
                try {out.close(false);}
                catch(Exception e2) {}
            }

            // Notify user that something went wrong while writing the credentials file
            System.out.println("An error occurred while writing credentials file "+ credentialsFile.getAbsolutePath()+": "+e);
        }
    }


    /**
     * Returns an array of {@link MappedCredentials} that match the location designated by the given {@link FileURL}
     * and which can be used to authenticate. The location is compared against all known credentials, both volatile and
     * persistent.
     *
     * <p>The returned credentials will match the given URL's protocol and host, but the path may differ so there is
     * no guarantee that the credentials will successfully authenticate the location.
     *
     * <p>The best match (credentials with the 'closest' path to the provided location's path) is returned at the first
     * position ([0]), if there is at least one matching credentials instance. The returned array can be empty
     * (zero length) but never null.
     * 
     * @param fileURL the location to be compared against known credentials instances, both volatile and persistent
     * @return an array of MappedCredentials matching the given URL's protocol and host, best match at the first position
     */
    public static MappedCredentials[] getMatchingCredentials(FileURL fileURL) {
        // Retrieve matches
        Vector matchesV = getMatchingCredentialsV(fileURL);

        // Transform vector into an array
        MappedCredentials matches[] = new MappedCredentials[matchesV.size()];
        matchesV.toArray(matches);

        return matches;
    }


    /**
     * Returns a Vector of MappedCredentials matching the given URL's protocol and host, best match at the first position.
     * The returned Vector may be empty but never null.
     *
     * @param fileURL the location to be compared against known credentials instances, both volatile and persistent
     * @return a Vector of MappedCredentials matching the given URL's protocol and host, best match at the first position
     */
    private static Vector getMatchingCredentialsV(FileURL fileURL) {
        Vector matchesV = new Vector();

        findMatches(fileURL, volatileCredentials, matchesV);
        findMatches(fileURL, persistentCredentials, matchesV);

        // Find the best match and move it at the first position in the vector
        int bestMatchIndex = getBestMatchIndex(fileURL, matchesV);
        if(bestMatchIndex!=-1) {
            matchesV.insertElementAt(matchesV.elementAt(bestMatchIndex), 0);
            matchesV.removeElementAt(bestMatchIndex+1);
        }

        return matchesV;
    }


    /**
     * Looks for the best credentials matching the given location (if any) and use it in the FileURL
     * by calling {@link FileURL#setCredentials(Credentials)}. Any credentials contained by the the given FileURL
     * will be lost and replaced by the new ones (if any).
     *
     * @param fileURL the FileURL instance to authenticate
     */
    public static void authenticate(FileURL fileURL) {
        Vector matchesV = new Vector();

        if(Debug.ON) Debug.trace("called, fileURL="+fileURL+" containsCredentials="+fileURL.containsCredentials());

        findMatches(fileURL, volatileCredentials, matchesV);
        findMatches(fileURL, persistentCredentials, matchesV);
        int bestMatchIndex = getBestMatchIndex(fileURL, matchesV);

        if(Debug.ON) Debug.trace("matchesV="+matchesV);
        if(Debug.ON) Debug.trace("bestMatch="+(bestMatchIndex>0?(MappedCredentials)matchesV.elementAt(bestMatchIndex):null));

        if(bestMatchIndex>-1)
            fileURL.setCredentials((MappedCredentials)matchesV.elementAt(bestMatchIndex));
    }


    /**
     * Adds the given {@link MappedCredentials} to the list of known credentials. Depending on the value returned by
     * {@link com.mucommander.auth.MappedCredentials#isPersistent()}, the credentials will either be stored in the
     * volatile credentials list or the persistent one. Any existing MappedCredentials instance pointing to the same
     * location will be replaced by the provided one.
     *
     * <p>This method should be called when new credentials have been entered by the user after they have been validated
     * by the application (access was granted to the designated location).
     *
     * @param credentials new MappedCredentials to be added
     */
    public static void addCredentials(MappedCredentials credentials) {
        boolean isPersistent = credentials.isPersistent();

        FileURL url = credentials.getMappedLocation();

        if(Debug.ON) Debug.trace("before, persistentCredentials="+persistentCredentials);
        if(Debug.ON) Debug.trace("before, volatileCredentials="+volatileCredentials);

        int index = indexOf(persistentCredentials, url);
        if(isPersistent || index!=-1) {
            if(index==-1)
                persistentCredentials.add(credentials);
            else
                persistentCredentials.setElementAt(credentials, index);

            index = indexOf(volatileCredentials, url);
            if(index!=-1)
                volatileCredentials.removeElementAt(index);
        }
        else {
            index = indexOf(volatileCredentials, url);
            if(index==-1)
                volatileCredentials.add(credentials);
            else
                volatileCredentials.setElementAt(credentials, index);
        }

        if(Debug.ON) Debug.trace("after, persistentCredentials="+persistentCredentials);
        if(Debug.ON) Debug.trace("after, volatileCredentials="+volatileCredentials);
    }


    private static int indexOf(Vector entries, FileURL fileURL) {
        int nbEntries = entries.size();
        for(int i=0; i<nbEntries; i++) {
            if(((MappedCredentials)entries.elementAt(i)).getMappedLocation().equals(fileURL))
                return i;
        }

        return -1;
    }


    /**
     * Looks for credentials matching the specified location in the given credentials Vector and adds them to the given
     * matches Vector.
     *
     * @param fileURL the location to find matching credentials for
     * @param credentials the Vector containing the MappedCredentials instances to compare to the given location
     * @param matches the Vector where matching MappedCredentials instances will be added
     */
    private static void findMatches(FileURL fileURL, Vector credentials, Vector matches) {
        String protocol = fileURL.getProtocol();
        String host = fileURL.getHost();
        MappedCredentials tempCredentials;
        String tempHost;
        FileURL tempURL;

        int nbEntries = credentials.size();
        for(int i=0; i<nbEntries; i++) {
            tempCredentials = (MappedCredentials)credentials.elementAt(i);
            tempURL = tempCredentials.getMappedLocation();
            tempHost = tempURL.getHost();

            if(tempURL.getProtocol().equals(protocol) && ((host!=null && tempHost!=null && host.equalsIgnoreCase(tempHost)) || (host!=null && host.equalsIgnoreCase(tempHost)) || (tempHost!=null && tempHost.equalsIgnoreCase(host)))) {
                matches.add(tempCredentials);
            }
        }

        if(Debug.ON) Debug.trace("returning matches="+matches);
    }

    /**
     * Finds are returns the index of the MappedCredentials instance that best matches the given location
     * amongst the provided matching MappedCredentials Vector, or -1 if the matches Vector is empty.
     *
     * <p>The path of each matching MappedCredentials' location is compared to the provided location's path: the more
     * folder parts match, the better. If both paths are equal, then the MappedCredentials index is returned (perfect match).
     *
     * @param fileURL the location to be compared against MappedCredentials matches
     * @param matches MappedCredentials instances matching the given location
     * @return the MappedCredentials instance that best matches the given location, -1 if the given matches Vector is empty.
     */
    private static int getBestMatchIndex(FileURL fileURL, Vector matches) {
        if(matches.size()==0)
            return -1;

        // Splits the provided location's path into an array of folder tokens (e.g. "/home/maxence" -> ["home","maxence"])
        String path = fileURL.getPath();
        Vector pathTokensV = new Vector();
        StringTokenizer st = new StringTokenizer(path, "/\\");
        while(st.hasMoreTokens()) {
            pathTokensV.add(st.nextToken());
        }
        int nbTokens = pathTokensV.size();
        String pathTokens[] = new String[nbTokens];
        pathTokensV.toArray(pathTokens);

        MappedCredentials tempCredentials;
        FileURL tempURL;
        String tempPath;
        int nbMatchingToken;
        int maxTokens = 0;
        int bestMatchIndex = 0;

        // Compares the location's path against all the one of all MappedCredentials instances
        int nbMatches = matches.size();
        for(int i=0; i<nbMatches; i++) {
            tempCredentials = (MappedCredentials)matches.elementAt(i);
            tempURL = tempCredentials.getMappedLocation();
            tempPath = tempURL.getPath();

            // We found a perfect match (same path), it can't get any better than this, return the MappedCredentials' index
            if(tempPath.equalsIgnoreCase(path))
                return i;

            // Split the current MappedCredentials' location into folder tokens and count the ones that match
            // the target location's tokens.
            // A few examples to illustrate:
            // /home and /home/maxence -> nbMatchingToken = 1
            // /var/log and /usr -> nbMatchingToken = 0
            st = new StringTokenizer(tempPath, "/\\");
            nbMatchingToken = 0;
            for(int j=0; j<nbTokens && st.hasMoreTokens(); j++) {
                if(st.nextToken().equalsIgnoreCase(pathTokens[nbMatchingToken]))
                    nbMatchingToken++;
                else
                    break;
            }

            if (nbMatchingToken>maxTokens) {
                // We just found a better match 
                maxTokens = nbMatchingToken;
                bestMatchIndex = i;
            }
        }

        if(Debug.ON) Debug.trace("returning bestMatchIndex="+bestMatchIndex);

        return bestMatchIndex;
    }


    /**
     * Returns the list of known volatile MappedCredentials, stored in a Vector. The returned Vector instance is
     * the one actually used by CredentialsManager, so use it with care.
     */
    public static Vector getVolatileCredentials() {
        return volatileCredentials;
    }


    /**
     * Returns the list of known persistent MappedCredentials, stored in an AlteredVector.
     * Any changes made to the Vector will be detected and will yield to writing the credentials file when
     * {@link #writeCredentials(boolean)} is called with false.
     */
    public static AlteredVector getPersistentCredentials() {
        return persistentCredentials;
    }


    /////////////////////////////////////////
    // VectorChangeListener implementation //
    /////////////////////////////////////////

    // Detects changes made to the persistent credentials AlteredVector

    public void elementsAdded(int startIndex, int nbAdded) {
        saveNeeded = true;
    }

    public void elementsRemoved(int startIndex, int nbRemoved) {
        saveNeeded = true;
    }

    public void elementChanged(int index) {
        saveNeeded = true;
    }
}
