
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
 * This class centralizes login/password combinations ({@link com.mucommander.auth.Credentials Credentials} instances) used
 * to connect to remote file systems. It is used to manipulate file paths and show them to the end user
 * without the login and password information. Each supplied login/password is mapped to a path (or file URL) and
 * stored in a hashtable and can later be retrieved.
 *
 * @author Maxence Bernard
 */
public class CredentialsManager implements VectorChangeListener {
	
    private static Vector volatileCredentials = new Vector();

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


    public static MappedCredentials[] getMatchingCredentials(FileURL fileURL) {
        Vector matchesV = getMatchingCredentialsV(fileURL);
        MappedCredentials matches[] = new MappedCredentials[matchesV.size()];
        matchesV.toArray(matches);

        return matches;
    }


    private static Vector getMatchingCredentialsV(FileURL fileURL) {
        Vector matchesV = new Vector();

        findMatches(fileURL, volatileCredentials, matchesV);
        findMatches(fileURL, persistentCredentials, matchesV);
        int bestMatchIndex = getBestMatchIndex(fileURL, matchesV);

        if(bestMatchIndex!=-1) {
            matchesV.insertElementAt(matchesV.elementAt(bestMatchIndex), 0);
            matchesV.removeElementAt(bestMatchIndex+1);
        }

        return matchesV;
    }


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



    public static void addCredentials(MappedCredentials credentials) {
        boolean isPersistent = credentials.isPersistent();

        FileURL url = credentials.getURL();

//if(Debug.ON) Debug.trace("before, persistentCredentials="+persistentCredentials);
//if(Debug.ON) Debug.trace("before, volatileCredentials="+volatileCredentials);

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

//if(Debug.ON) Debug.trace("after, persistentCredentials="+persistentCredentials);
//if(Debug.ON) Debug.trace("fater, volatileCredentials="+volatileCredentials);
    }


    private static int indexOf(Vector entries, FileURL fileURL) {
        int nbEntries = entries.size();
        for(int i=0; i<nbEntries; i++) {
            if(((MappedCredentials)entries.elementAt(i)).getURL().equals(fileURL))
                return i;
        }

        return -1;
    }


    private static void findMatches(FileURL fileURL, Vector entries, Vector matches) {
        String protocol = fileURL.getProtocol();
        String host = fileURL.getHost();
        MappedCredentials tempCredentials;
        String tempHost;
        FileURL tempURL;

        int nbEntries = entries.size();
        for(int i=0; i<nbEntries; i++) {
            tempCredentials = (MappedCredentials)entries.elementAt(i);
            tempURL = tempCredentials.getURL();
            tempHost = tempURL.getHost();

            if(tempURL.getProtocol().equals(protocol) && ((host!=null && tempHost!=null && host.equalsIgnoreCase(tempHost)) || (host!=null && host.equalsIgnoreCase(tempHost)) || (tempHost!=null && tempHost.equalsIgnoreCase(host)))) {
                matches.add(tempCredentials);
            }
        }

        if(Debug.ON) Debug.trace("returning matches="+matches);
    }


    private static int getBestMatchIndex(FileURL fileURL, Vector matches) {
        if(matches.size()==0)
            return -1;

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

        int nbMatches = matches.size();
        for(int i=0; i<nbMatches; i++) {
            tempCredentials = (MappedCredentials)matches.elementAt(i);
            tempURL = tempCredentials.getURL();
            tempPath = tempURL.getPath();

            // Perfect match
            if(tempPath.equalsIgnoreCase(path))
                return i;

            st = new StringTokenizer(tempPath, "/\\");

            nbMatchingToken = 0;
            for(int j=0; j<nbTokens && st.hasMoreTokens(); j++) {
                if(st.nextToken().equalsIgnoreCase(pathTokens[nbMatchingToken]))
                    nbMatchingToken++;
            }

            if (nbMatchingToken>maxTokens) {
                maxTokens = nbMatchingToken;
                bestMatchIndex = i;
            }
        }

        if(Debug.ON) Debug.trace("returning bestMatchIndex="+bestMatchIndex);

        return bestMatchIndex;
    }


    private static MappedCredentials getBestMatch(FileURL fileURL, Vector matches) {
        int bestMatchIndex = getBestMatchIndex(fileURL, matches);

        if(Debug.ON) Debug.trace("returning bestMatch="+bestMatchIndex+(bestMatchIndex==-1?null:(MappedCredentials)matches.elementAt(bestMatchIndex)));

        return bestMatchIndex==-1?null:(MappedCredentials)matches.elementAt(bestMatchIndex);
    }


    public static Vector getVolatileCredentials() {
        return volatileCredentials;
    }


    public static AlteredVector getPersistentCredentials() {
        return persistentCredentials;
    }


    /////////////////////////////////////////
    // VectorChangeListener implementation //
    /////////////////////////////////////////

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
