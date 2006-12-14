
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
import java.net.MalformedURLException;


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

    /** Contains volatile user-entered MappedCredentials instances, lost when the application terminates */
    private static Vector volatileCredentials = new Vector();

    /** Contains persistent user-entered MappedCredentials instances, stored to an XML file when the application
     * terminates, and loaded the next time the application is started */
    private static AlteredVector persistentCredentials = new AlteredVector();

//    /** Contains MappedCredentials instances used for implicit authentication */
//    private static Vector implicitCredentials = new Vector();

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
     * @param location the location to be compared against known user credentials instances, both volatile and persistent
     * @return an array of MappedCredentials matching the given URL's protocol and host, best match at the first position
     */
    public static MappedCredentials[] getMatchingCredentials(FileURL location) {
        // Retrieve matches
        Vector matchesV = getMatchingCredentialsV(location);

        // Transform vector into an array
        MappedCredentials matches[] = new MappedCredentials[matchesV.size()];
        matchesV.toArray(matches);

        return matches;
    }


    /**
     * Returns a Vector of MappedCredentials matching the given URL's protocol and host, best match at the first position.
     * The returned Vector may be empty but never null.
     *
     * @param location the location to be compared against known user credentials instances, both volatile and persistent
     * @return a Vector of MappedCredentials matching the given URL's protocol and host, best match at the first position
     */
    private static Vector getMatchingCredentialsV(FileURL location) {
        Vector matchesV = new Vector();

        findMatches(location, volatileCredentials, matchesV);
        findMatches(location, persistentCredentials, matchesV);

        // Find the best match and move it at the first position in the vector
        int bestMatchIndex = getBestMatchIndex(location, matchesV);
        if(bestMatchIndex!=-1) {
            matchesV.insertElementAt(matchesV.elementAt(bestMatchIndex), 0);
            matchesV.removeElementAt(bestMatchIndex+1);
        }

        return matchesV;
    }


    /**
     * Adds the given credentials to the list of known user credentials.
     *
     * <p>Depending on value returned by {@link UserCredentials#isPersistent()}, the credentials will either be stored
     * in the volatile credentials list or the persistent one. Any existing credentials mapped to the same realm
     * will be replaced by the provided ones.
     *
     * <p>This method should be called when new credentials have been entered by the user, after they have been validated
     * by the application (i.e. access was granted to the location).
     *
     * @param userCredentials user credentials to be added to the list of known credentials
     */
    public static void addCredentials(UserCredentials userCredentials) {

        // Do not add credentials if their login and password are empty
        if(userCredentials.isEmpty())
            return;

        boolean persist = userCredentials.isPersistent();

        if(Debug.ON) Debug.trace("called, realm="+userCredentials.getRealm()+" isPersistent="+userCredentials.isPersistent());
        if(Debug.ON) Debug.trace("before, persistentCredentials="+persistentCredentials);
        if(Debug.ON) Debug.trace("before, volatileCredentials="+volatileCredentials);

        int index = persistentCredentials.indexOf(userCredentials);
        if(persist || index!=-1) {
            if(index==-1)
                persistentCredentials.add(userCredentials);
            else
                persistentCredentials.setElementAt(userCredentials, index);

            index = volatileCredentials.indexOf(userCredentials);
            if(index!=-1)
                volatileCredentials.removeElementAt(index);
        }
        else {
            index = volatileCredentials.indexOf(userCredentials);
            if(index==-1)
                volatileCredentials.add(userCredentials);
            else
                volatileCredentials.setElementAt(userCredentials, index);
        }

        if(Debug.ON) Debug.trace("after, persistentCredentials="+persistentCredentials);
        if(Debug.ON) Debug.trace("after, volatileCredentials="+volatileCredentials);
    }


    /**
     * Looks for the best implicit credentials matching the given location (if any) and use them in the FileURL
     * by calling {@link FileURL#setCredentials(Credentials)}. Any credentials contained by the the given FileURL
     * will be lost and replaced with the new ones (if any).
     *
     * @param location the location to authenticate
     */
    public static void authenticateImplicit(FileURL location) {
        if(Debug.ON) Debug.trace("called, fileURL="+ location +" containsCredentials="+ location.containsCredentials());

        MappedCredentials creds[] = getMatchingCredentials(location);
        if(creds.length>0)
            location.setCredentials(creds[0]);
    }


//    /**
//     * Looks for the best implicit credentials matching the given location (if any) and use them in the FileURL
//     * by calling {@link FileURL#setCredentials(Credentials)}. Any credentials contained by the the given FileURL
//     * will be lost and replaced by the new ones (if any).
//     *
//     * @param location the location to authenticate
//     */
//    public static void authenticateImplicit(FileURL location) {
//        if(Debug.ON) Debug.trace("called, fileURL="+ location +" containsCredentials="+ location.containsCredentials());
//
//        try {
//            int match = indexOfRealm(implicitCredentials, MappedCredentials.resolveRealm(location));
//
//            if(match!=-1)
//                location.setCredentials((MappedCredentials)implicitCredentials.elementAt(match));
//        }
//        catch(MalformedURLException e) {
//            // Should never happen, report the error if it does
//            if(Debug.ON) Debug.trace("Error: realm could not be resolved for location: "+location);
//        }
//    }

    
//    public static void addImplicitCredentials(Credentials credentials, FileURL location) {
//        MappedCredentials mappedCredentials = (new MappedCredentials(credentials, location));
//
//        FileURL realm = mappedCredentials.getRealm();
//
//        int index = indexOfRealm(implicitCredentials, realm);
//        if(index==-1)
//            implicitCredentials.add(mappedCredentials);
//        else
//            implicitCredentials.setElementAt(mappedCredentials, index);
//
//        if(Debug.ON) Debug.trace("called, realm="+realm+" implicitCredentials="+implicitCredentials);
//    }
//
//
//    private static int indexOfRealm(Vector entries, FileURL realm) {
//        int nbEntries = entries.size();
//        for(int i=0; i<nbEntries; i++) {
//            if(((MappedCredentials)entries.elementAt(i)).getRealm().equals(realm))
//                return i;
//        }
//
//        return -1;
//    }


//    /**
//     * Looks for credentials matching the specified realm in the given credentials Vector and adds them to the given
//     * matches Vector.
//     *
//     * @param realm the realm to find matching credentials for
//     * @param credentials the Vector containing the MappedCredentials instances to compare to the given realm
//     * @param matches the Vector where matching MappedCredentials instances will be added
//     */
//    private static void findMatches(FileURL realm, Vector credentials, Vector matches) {
//        int nbEntries = credentials.size();
//        MappedCredentials tempCredentials;
//        for(int i=0; i<nbEntries; i++) {
//            tempCredentials = (MappedCredentials)credentials.elementAt(i);
//            if(tempCredentials.getRealm().equals(realm))
//                matches.add(tempCredentials);
//        }
//
//        if(Debug.ON) Debug.trace("returning matches="+matches);
//    }

    /**
     * Looks for credentials matching the specified location in the given credentials Vector and adds them to the given
     * matches Vector.
     *
     * @param location the location to find matching credentials for
     * @param credentials the Vector containing the MappedCredentials instances to compare to the given location
     * @param matches the Vector where matching MappedCredentials instances will be added
     */
    private static void findMatches(FileURL location, Vector credentials, Vector matches) {
        String protocol = location.getProtocol();
        String host = location.getHost();
        MappedCredentials tempCredentials;
        String tempHost;
        FileURL tempRealm;

        int nbEntries = credentials.size();
        for(int i=0; i<nbEntries; i++) {
            tempCredentials = (MappedCredentials)credentials.elementAt(i);
            tempRealm = tempCredentials.getRealm();

            if(tempRealm.equals(location)) {
                matches.add(tempCredentials);
            }
            else {
                tempHost = tempRealm.getHost();

                if(tempRealm.getProtocol().equals(protocol) && ((host!=null && tempHost!=null && host.equalsIgnoreCase(tempHost)) || (host!=null && host.equalsIgnoreCase(tempHost)) || (tempHost!=null && tempHost.equalsIgnoreCase(host)))) {
                    matches.add(tempCredentials);
                }
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
     * @param location the location to be compared against MappedCredentials matches
     * @param matches MappedCredentials instances matching the given location
     * @return the MappedCredentials instance that best matches the given location, -1 if the given matches Vector is empty.
     */
    private static int getBestMatchIndex(FileURL location, Vector matches) {
        if(matches.size()==0)
            return -1;

        // Splits the provided location's path into an array of folder tokens (e.g. "/home/maxence" -> ["home","maxence"])
        String path = location.getPath();
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
            tempURL = tempCredentials.getRealm();
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
    public static Vector getVolatileUserCredentials() {
        return volatileCredentials;
    }


    /**
     * Returns the list of known persistent MappedCredentials, stored in an AlteredVector.
     * Any changes made to the Vector will be detected and will yield to writing the credentials file when
     * {@link #writeCredentials(boolean)} is called with false.
     */
    public static AlteredVector getPersistentUserCredentials() {
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
