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

package com.mucommander.auth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.PlatformManager;
import com.mucommander.commons.collections.AlteredVector;
import com.mucommander.commons.collections.VectorChangeListener;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.Authenticator;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.util.Chmod;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.io.backup.BackupOutputStream;


/**
 * This class manages {@link CredentialsMapping} instances (login/password pairs associated with a server realm) used to
 * connect to authenticated file systems. It provides methods to find credentials matching a particular location and to
 * read and write credentials to an XML file.
 *
 * <p>
 * Two types of {@link CredentialsMapping} are used:
 * <ul>
 *  <li>persistent credentials: stored in an XML file when the application terminates, and loaded the next time the
 * application is started.</li>
 *  <li>volatile credentials: lost when the application terminates.</li>
 * </ul>
 * </p>
 *
 * @author Maxence Bernard
 */
public class CredentialsManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsManager.class);
	
    /** Contains volatile CredentialsMapping instances, lost when the application terminates */
    private static List<CredentialsMapping> volatileCredentialMappings = new Vector<CredentialsMapping>();

    /** Contains persistent CredentialsMapping instances, stored to an XML file when the application
     * terminates, and loaded the next time the application is started */
    private static AlteredVector<CredentialsMapping> persistentCredentialMappings = new AlteredVector<CredentialsMapping>();

    /** Singleton CredentialsManagerAuthenticator instance */
    private final static Authenticator AUTHENTICATOR = new CredentialsManagerAuthenticator();

    /** Credentials file location */
    private static AbstractFile credentialsFile;

    /** Default credentials file name */
    private static final String DEFAULT_CREDENTIALS_FILE_NAME = "credentials.xml";

    /** Tracks changes made to the persistent credentials vector.
     * We keep a reference to the listener so it doesn't get garbage collected. */
    private static final VectorChangeListener PERSISTENT_CREDENTIALS_VECTOR_CHANGE_LISTENER;    // Don't remove me!

    /** True when changes were made after the credentials file was last saved */
    private static boolean saveNeeded;

    /** Create a singleton instance, needs to be referenced so that it's not garbage collected (AlteredVector
      * stores VectorChangeListener as weak references) */
    private static CredentialsManager singleton = new CredentialsManager();

    static {
        // Listen to changes made to the persistent entries vector.
        // Note: we must keep a reference to the listener, as it would otherwise be garbage collected.
        persistentCredentialMappings.addVectorChangeListener(PERSISTENT_CREDENTIALS_VECTOR_CHANGE_LISTENER = new VectorChangeListener() {
            public void elementsAdded(int startIndex, int nbAdded) {
                saveNeeded = true;
            }

            public void elementsRemoved(int startIndex, int nbRemoved) {
                saveNeeded = true;
            }

            public void elementChanged(int index) {
                saveNeeded = true;
            }
        });
    }


    /**
     * Returns the path to the credentials file.
     *
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
     *
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
     *
     * @param  file                  path to the credentials file
     * @throws FileNotFoundException if <code>path</code> is not available.
     */
    public static void setCredentialsFile(File file) throws FileNotFoundException {
        setCredentialsFile(FileFactory.getFile(file.getAbsolutePath()));
    }

    /**
     * Sets the path to the credentials file.
     *
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
     *
     * @throws Exception if an error occurs while loading the credentials file.
     */
    public static void loadCredentials() throws Exception {
        AbstractFile credentialsFile = getCredentialsFile();
        if(credentialsFile.exists()) {
        	LOGGER.debug("Found credentials file: "+credentialsFile.getAbsolutePath());
            // Parse the credentials file
            new CredentialsParser().parse(credentialsFile);
            LOGGER.debug("Credentials file loaded.");
        }
        else
        	LOGGER.debug("No credentials file found at "+credentialsFile.getAbsolutePath());
    }

    /**
     * Tries to write the credentials file. Unless the 'forceWrite' is set to true, the credentials file will be written
     * only if changes were made to persistent entries since last write.
     *
     * @param forceWrite if false, the credentials file will be written only if changes were made to persistent entries
     *  since last write, if true the file will always be written.
     * @throws IOException if an I/O error occurs.
     */
    public static void writeCredentials(boolean forceWrite) throws IOException {
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

        if(fileSecured)
        	LOGGER.debug("Credentials file saved successfully.");
        else
        	LOGGER.warn("Credentials file could not be chmod!");
    }


    /**
     * Returns an array of {@link CredentialsMapping} that match the location designated by the given {@link FileURL}
     * and which can be used to authenticate. The location is compared against all known credentials, both volatile and
     * persistent.
     *
     * <p>The returned credentials will match the given URL's scheme and host, but the path may differ so there is
     * no guarantee that the credentials will successfully authenticate the location.</p>
     *
     * <p>The best match (credentials with the 'closest' path to the provided location's path) is returned at the first
     * position ([0]), if there is at least one matching credentials instance. The returned array can be empty
     * (zero length) but never null.</p>
     * 
     * @param location the location to be compared against known credentials instances, both volatile and persistent
     * @return an array of CredentialsMapping matching the given URL's scheme and host, best match at the first position
     */
    public static CredentialsMapping[] getMatchingCredentials(FileURL location) {
        // Retrieve matches
        List<CredentialsMapping> matchesV = getMatchingCredentialsV(location);

        // Transform vector into an array
        CredentialsMapping matches[] = new CredentialsMapping[matchesV.size()];
        matchesV.toArray(matches);

        return matches;
    }

    /**
     * Returns an {@link Authenticator} that authenticates {@link FileURL} instances using the credentials and realm
     * properties stored by {@link CredentialsManager}.
     *
     * @return an {@link Authenticator} that authenticates {@link FileURL} instances using the credentials and realm
     * properties stored by {@link CredentialsManager}.
     */
    public static Authenticator getAuthenticator() {
        return AUTHENTICATOR;
    }

    /**
     * Returns a Vector of CredentialsMapping matching the given URL's scheme and host, best match at the first position.
     * The returned Vector may be empty but never null.
     *
     * @param location the location to be compared against known credentials instances, both volatile and persistent
     * @return a Vector of CredentialsMapping matching the given URL's scheme and host, best match at the first position
     */
    private static List<CredentialsMapping> getMatchingCredentialsV(FileURL location) {
        List<CredentialsMapping> matchesV = new Vector<CredentialsMapping>();

        findMatches(location, volatileCredentialMappings, matchesV);
        findMatches(location, persistentCredentialMappings, matchesV);

        // Find the best match and move it at the first position in the vector
        int bestMatchIndex = getBestMatchIndex(location, matchesV);
        if(bestMatchIndex!=-1) {
            matchesV.add(0, matchesV.remove(bestMatchIndex));
        }

        return matchesV;
    }


    /**
     * Adds the given credentials to the list of known credentials.
     *
     * <p>Depending on value returned by {@link CredentialsMapping#isPersistent()}, the credentials will either be stored
     * in the volatile credentials list or the persistent one. Any existing credentials mapped to the same realm
     * will be replaced by the provided ones.</p>
     *
     * <p>This method should be called when new credentials have been entered by the user, after they have been validated
     * by the application (i.e. access was granted to the location).</p>
     *
     * @param credentialsMapping credentials to be added to the list of known credentials
     */
    public static void addCredentials(CredentialsMapping credentialsMapping) {

        // Do not add if the credentials are empty
        if(credentialsMapping.getCredentials().isEmpty())
            return;

        boolean persist = credentialsMapping.isPersistent();

        LOGGER.trace("called, realm="+ credentialsMapping.getRealm()+" isPersistent="+ credentialsMapping.isPersistent());
        LOGGER.trace("before, persistentCredentials="+ persistentCredentialMappings);
        LOGGER.trace("before, volatileCredentials="+ volatileCredentialMappings);

        if(persist) {
            replaceVectorElement(persistentCredentialMappings, credentialsMapping);
            volatileCredentialMappings.remove(credentialsMapping);
        }
        else {
            replaceVectorElement(volatileCredentialMappings, credentialsMapping);
            persistentCredentialMappings.removeElement(credentialsMapping);
        }

        LOGGER.trace("after, persistentCredentials="+ persistentCredentialMappings);
        LOGGER.trace("after, volatileCredentials="+ volatileCredentialMappings);
    }


    /**
     * Use the credentials and realm properties of the specified <code>CredentialsMapping</code> to authenticate the
     * given {@link FileURL}.

     * <p>Any credentials contained by the <code>FileURL</code> will be lost and replaced with the new ones.
     * If properties with the same key are defined both in the realm and the given FileURL, the ones from the FileURL
     * will be preserved.</p>
     *
     * @param location the FileURL to authenticate
     * @param credentialsMapping the credentials to use to authenticate the given FileURL
     */
    public static void authenticate(FileURL location, CredentialsMapping credentialsMapping) {
        location.setCredentials(credentialsMapping.getCredentials());

        FileURL realm = credentialsMapping.getRealm();
        Enumeration<String> propertyKeys = realm.getPropertyNames();
        String key;
        while(propertyKeys.hasMoreElements()) {
            key = propertyKeys.nextElement();

            if(location.getProperty(key)==null)
                location.setProperty(key, realm.getProperty(key));
        }
    }


    /**
     * Looks for the best set of credentials matching the given location (if any) and use it to authenticate the
     * URL by calling {@link #authenticate(com.mucommander.commons.file.FileURL, CredentialsMapping)}.
     * Returns <code>true</code> if a set of credentials was found and used to authenticate the URL, <code>false</code>
     * otherwise.
     *
     * <p>Credentials are first looked for using {@link #getMatchingCredentials(com.mucommander.commons.file.FileURL)}.
     * If there is no match, guest credentials are retrieved from the URL and used (if any).</p>
     *
     * @param location the FileURL to authenticate
     */
    private static void authenticateImplicit(FileURL location) {
    	LOGGER.trace("called, fileURL="+ location +" containsCredentials="+ location.containsCredentials());

        CredentialsMapping creds[] = getMatchingCredentials(location);
        if(creds.length>0) {
            authenticate(location, creds[0]);
        }
        else {
            Credentials guestCredentials = location.getGuestCredentials();
            if(guestCredentials!=null) {
                authenticate(location, new CredentialsMapping(guestCredentials, location.getRealm(), false));
            }
        }
    }


    /**
     * Looks for credentials matching the specified location in the given credentials Vector and adds them to the given
     * matches Vector.
     *
     * @param location the location to find matching credentials for
     * @param credentials the Vector containing the CredentialsMapping instances to compare to the given location
     * @param matches the Vector where matching CredentialsMapping instances will be added
     */
    private static void findMatches(FileURL location, List<CredentialsMapping> credentials, List<CredentialsMapping> matches) {
        FileURL tempRealm;

        int nbEntries = credentials.size();
        for(CredentialsMapping tempCredentialsMapping: credentials) {
            tempRealm = tempCredentialsMapping.getRealm();
            if(location.schemeEquals(tempRealm)
               && location.portEquals(tempRealm)
               && location.hostEquals(tempRealm))
                matches.add(tempCredentialsMapping);
        }

        LOGGER.trace("returning matches="+matches);
    }

    /**
     * Finds are returns the index of the CredentialsMapping instance that best matches the given location
     * amongst the provided matching CredentialsMapping Vector, or -1 if the matches Vector is empty.
     *
     * <p>
     * The path of each matching CredentialsMapping' location is compared to the provided location's path: the more
     * folder parts match, the better. If both paths are equal, then the CredentialsMapping index is returned (perfect match).
     * </p>
     *
     * @param location the location to be compared against CredentialsMapping matches
     * @param matches CredentialsMapping instances matching the given location
     * @return the CredentialsMapping instance that best matches the given location, -1 if the given matches Vector is empty.
     */
    private static int getBestMatchIndex(FileURL location, List<CredentialsMapping> matches) {
        if(matches.size()==0)
            return -1;

        // Splits the provided location's path into an array of folder tokens (e.g. "/home/maxence" -> ["home","maxence"])
        String path = location.getPath();
        List<String> pathTokensV = new Vector<String>();
        StringTokenizer st = new StringTokenizer(path, "/\\");
        while(st.hasMoreTokens()) {
            pathTokensV.add(st.nextToken());
        }
        int nbTokens = pathTokensV.size();
        String pathTokens[] = new String[nbTokens];
        pathTokensV.toArray(pathTokens);

        CredentialsMapping tempCredentialsMapping;
        FileURL tempURL;
        String tempPath;
        int nbMatchingToken;
        int maxTokens = 0;
        int bestMatchIndex = 0;

        // Compares the location's path against all the one of all CredentialsMapping instances
        int nbMatches = matches.size();
        for(int i=0; i<nbMatches; i++) {
            tempCredentialsMapping = matches.get(i);
            tempURL = tempCredentialsMapping.getRealm();
            tempPath = tempURL.getPath();

            // We found a perfect match (same path), it can't get any better than this, return the CredentialsMapping' index
            if(tempPath.equalsIgnoreCase(path))
                return i;

            // Split the current CredentialsMapping' location into folder tokens and count the ones that match
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

        LOGGER.trace("returning bestMatchIndex="+bestMatchIndex);

        return bestMatchIndex;
    }

    /**
     * Replaces any object that's equal to the given one in the <code>Vector</code>, preserving its position. If the
     * vector contains no such object, it is added to the end of the vector.
     *
     * @param vector the <code>Vector</code> to replace/add the object to
     * @param o the object to replace/add
     */
    private static void replaceVectorElement(List<CredentialsMapping> vector, CredentialsMapping o) {
        int index = vector.indexOf(o);
        if(index==-1)
            vector.add(o);
        else
            vector.set(index, o);
    }

    /**
     * Returns the list of known volatile {@link CredentialsMapping}, stored in a Vector.
     * <p>
     * The returned Vector instance is the one actually used by CredentialsManager, so use it with caution.
     * </p>
     * @return the list of known volatile {@link CredentialsMapping}.
     */
    public static List<CredentialsMapping> getVolatileCredentialMappings() {
        return volatileCredentialMappings;
    }


    /**
     * Returns the list of known persistent {@link CredentialsMapping}, stored in an {@link AlteredVector}.
     * <p>
     * Any changes made to the Vector will be detected and will yield to writing the credentials file when
     * {@link #writeCredentials(boolean)} is called with false.
     * </p>
     * @return the list of known persistent {@link CredentialsMapping}.
     */
    public static AlteredVector<CredentialsMapping> getPersistentCredentialMappings() {
        return persistentCredentialMappings;
    }


    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * An {@link Authenticator} implementation that uses {@link CredentialsManager#authenticateImplicit(FileURL)} to
     * authenticate the specified {@link FileURL} instances.
     *
     * @author Maxence Bernard
     */
    private static class CredentialsManagerAuthenticator implements Authenticator {

        public void authenticate(FileURL fileURL) {
            CredentialsManager.authenticateImplicit(fileURL);
        }
    }
}
