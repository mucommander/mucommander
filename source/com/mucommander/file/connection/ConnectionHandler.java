package com.mucommander.file.connection;

import com.mucommander.file.FileURL;
import com.mucommander.Debug;

import java.io.IOException;

/**
 * @author Maxence Bernard
 */
public abstract class ConnectionHandler {

    protected FileURL realm;

    protected boolean connectionStarted;


    public ConnectionHandler(FileURL location) {
        realm = FileURL.resolveRealm(location);
        realm.setCredentials(location.getCredentials());
    }


    public FileURL getRealm() {
        return realm;
    }


    public void checkConnection() throws IOException {
        if(!isConnected()) {
            if(Debug.ON) Debug.trace("not connected, this="+this);

            if(connectionStarted) {
                if(Debug.ON) Debug.trace("closing connection, this="+this);
                closeConnection();
            }

            if(Debug.ON) Debug.trace("starting connection, this="+this);
            startConnection();
        }
    }


    protected void finalize() throws Throwable {
        if(Debug.ON) Debug.trace("closing connection, this="+this);
        closeConnection();

        super.finalize();
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    public abstract void startConnection() throws IOException;

    public abstract boolean isConnected();

    public abstract void closeConnection();
}
