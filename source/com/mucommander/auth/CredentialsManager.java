/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.auth;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileURL;
import com.mucommander.file.util.Chmod;
import com.mucommander.io.BackupOutputStream;
import com.mucommander.runtime.OsFamily;
import com.mucommander.util.AlteredVector;
import com.mucommander.util.VectorChangeListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;


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

    /** Contains volatile MappedCredentials instances, lost when the application terminates */
    private static Vector volatileCredentials = new Vector();

    /** Contains persistent MappedCredentials instances, stored to an XML file when the application
     * terminates, and loaded the next time the application is started */
    private static AlteredVector persistentCredentials = new AlteredVector();

//    /** Contains MappedCredentials instances used for implicit authentication */
//    private static Vector implicitCredentials = new Vector();

    /** Credentials file location */
    private static AbstractFile credentialsFile;

    /** Default credentials file name */
    private static final String DEFAULT_CREDENTIALS_FILE_NAME = "credentials.xml";

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
     * Returns the path to the credentials file.
     * @return the path to the credentials file.
     * @throws IOException if there was some problem locating the default credentials file.
     */
    private static AbstractFile getCredentialsFile() throws IOException {
        if(credentialsFile == null)
            return PlatformManager.getPreferencesFolder().getChild(DEFAULT_CREDENTIALS_FILE_NAME);
        return credentialsFile;
    }

    /**
     * Sets the path to the credentials file.
     * @param  path                  path to the credentials file
     * @throws FileNotFoundException if <code>path</code> is not available.
     */
    public static void setCredentialsFile(String path) throws FileNotFoundException {
        AbstractFile file;

        if((file = FileFactory.getFile(path)) == null)
            setCredentialsFile(new File(path));
        else
            setCredentialsFile(file);
    }

    /**
     * Sets the path to the credentials file.
     * @param  file                  path to the credentials file
     * @throws FileNotFoundException if <code>path</code> is not available.
     */
    public static void setCredentialsFile(File file) throws FileNotFoundException {setCredentialsFile(FileFactory.getFile(file.getAbsolutePath()));}

    /**
     * Sets the path to the credentials file.
     * @param  file                  path to the credentials file
     * @throws FileNotFoundException if <code>path</code> is not available.
     */
    public static void setCredentialsFile(AbstractFile file) throws FileNotFoundException {
        if(file.isBrowsable())
            throw new FileNotFoundException("Not a valid file: " + file);
        credentialsFile = file;
    }


    /**
     * Tries to load credentials from the credentials file if it exists. Does nothing if it doesn't.
     * @throws Exception if an error occurs while loading the credentials file.
     */
    public static void loadCredentials() throws Exception {
        AbstractFile credentialsFile = getCredentialsFile();
        if(credentialsFile.exists()) {
            if(Debug.ON) Debug.trace("Found credentials file: "+credentialsFile.getAbsolutePath());
            // Parse the credentials file
            new CredentialsParser().parse(credentialsFile);
            if(Debug.ON) Debug.trace("Credentials file loaded.");
        }
        else if(Debug.ON) Debug.trace("No credentials file found at "+credentialsFile.getAbsolutePath());
    }

    /**
     * Tries to write the credentials file. Unless the 'forceWrite' is set to true, the credentials file will be written
     * only if changes were made to persistent entries since last write.
     *
     * @param forceWrite if false, the credentials file will be written only if changes were made to persistent entries
     *  since last write, if true the file will always be written.
     * @throws IOException if an I/O error occurs.
     */
    public static void writeCredentials(boolean forceWrite) throws IOException{
        // Write credentials file only if changes were made to persistent entries since last write, or if write is forced
        if(!(forceWrite || saveNeeded))
            return;

        BackupOutputStream out = null;
        try {
            credentialsFile = getCredentialsFile();
            CredentialsWriter.write(out = new BackupOutputStream(credentialsFile));
            saveNeeded = false;
        }
        finally {
            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }

        // Under UNIX-based systems, change the credentials file's permissions so that the file can't be read by
        // 'group' and 'other'.
        boolean fileSecured = !OsFamily.getCurrent().isUnixBased() || Chmod.chmod(credentialsFile, 0600);     // rw-------

        if(Debug.ON) {
            if(fileSecured)
                Debug.trace("Credentials file saved successfully.");
            else
                Debug.trace("Warning: credentials file could not be chmod.");
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
     * @param location the location to be compared against known credentials instances, both volatile and persistent
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
     * @param location the location to be compared against known credentials instances, both volatile and persistent
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
     * Adds the given credentials to the list of known credentials.
     *
     * <p>Depending on value returned by {@link MappedCredentials#isPersistent()}, the credentials will either be stored
     * in the volatile credentials list or the persistent one. Any existing credentials mapped to the same realm
     * will be replaced by the provided ones.
     *
     * <p>This method should be called when new credentials have been entered by the user, after they have been validated
     * by the application (i.e. access was granted to the location).
     *
     * @param credentials credentials to be added to the list of known credentials
     */
    public static void addCredentials(MappedCredentials credentials) {

        // Do not add credentials if their login and password are empty
        if(credentials.isEmpty())
            return;

        boolean persist = credentials.isPersistent();

        if(Debug.ON) Debug.trace("called, realm="+credentials.getRealm()+" isPersistent="+credentials.isPersistent());
        if(Debug.ON) Debug.trace("before, persistentCredentials="+persistentCredentials);
        if(Debug.ON) Debug.trace("before, volatileCredentials="+volatileCredentials);

        int index = persistentCredentials.indexOf(credentials);
        if(persist || index!=-1) {
            if(index==-1)
                persistentCredentials.add(credentials);
            else
                persistentCredentials.setElementAt(credentials, index);

            index = volatileCredentials.indexOf(credentials);
            if(index!=-1)
                volatileCredentials.removeElementAt(index);
        }
        else {
            index = volatileCredentials.indexOf(credentials);
            if(index==-1)
                volatileCredentials.add(credentials);
            else
                volatileCredentials.setElementAt(credentials, index);
        }

        if(Debug.ON) Debug.trace("after, persistentCredentials="+persistentCredentials);
        if(Debug.ON) Debug.trace("after, volatileCredentials="+volatileCredentials);
    }


    /**
     * Uses the given credentials in the specified FileURL, calling {@link FileURL#setCredentials(Credentials)} with
     * the given credentials and setting the properties contained by the <code>MappedCredentials</code>'s realm (if any).

     * <p>Any credentials contained by the the given FileURL will be lost and replaced with the new ones.
     * If some properties are defined both in the realm and given FileURL, the ones from the FileURL will be preserved
     * (will not be replaced).
     *
     * @param location the FileURL to authenticate
     * @param credentials the credentials to use to authenticate the given FileURL
     */
    public static void authenticate(FileURL location, MappedCredentials credentials) {
        location.setCredentials(credentials);

        FileURL realm = credentials.getRealm();
        Enumeration propertyKeys = realm.getPropertyKeys();
        if(propertyKeys!=null) {
            String key;
            while(propertyKeys.hasMoreElements()) {
                key = (String)propertyKeys.nextElement();

                if(location.getProperty(key)==null)
                    location.setProperty(key, realm.getProperty(key));
            }
        }
    }


    /**
     * Looks for the best implicit credentials matching the given location (if any) and use them to authenticate the
     * location by calling {@link #authenticate(com.mucommander.file.FileURL, MappedCredentials)}.
     *
     * @param location the FileURL to authenticate
     */
    public static void authenticateImplicit(FileURL location) {
        if(Debug.ON) Debug.trace("called, fileURL="+ location +" containsCredentials="+ location.containsCredentials());

        MappedCredentials creds[] = getMatchingCredentials(location);
        if(creds.length>0)
            authenticate(location, creds[0]);
    }


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
        int port = location.getPort();
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

                if(tempRealm.getProtocol().equals(protocol)
                        && (tempRealm.getPort()==port)
                        && ((host!=null && tempHost!=null && host.equalsIgnoreCase(tempHost)) || (host!=null && host.equalsIgnoreCase(tempHost)) || (tempHost!=null && tempHost.equalsIgnoreCase(host)))) {
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
     * Returns the list of known volatile {@link MappedCredentials}, stored in a Vector.
     * <p>
     * The returned Vector instance is the one actually used by CredentialsManager, so use it with care.
     * </p>
     * @return the list of known volatile {@link MappedCredentials}.
     */
    public static Vector getVolatileCredentials() {
        return volatileCredentials;
    }


    /**
     * Returns the list of known persistent {@link MappedCredentials}, stored in an {@link AlteredVector}.
     * <p>
     * Any changes made to the Vector will be detected and will yield to writing the credentials file when
     * {@link #writeCredentials(boolean)} is called with false.
     * </p>
     * @return the list of known persistent {@link MappedCredentials}.
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
