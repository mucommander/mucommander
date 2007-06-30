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

package com.mucommander.file.connection;

import com.mucommander.Debug;
import com.mucommander.auth.AuthException;
import com.mucommander.auth.Credentials;
import com.mucommander.file.FileURL;

import java.io.IOException;

/**
 * @author Maxence Bernard
 */
public abstract class ConnectionHandler {

    /** URL of the server this ConnectionHandler connects to */
    protected FileURL realm;

    /** Credentials that are used to connect to the server */
    protected Credentials credentials;

    /** True if this ConnectionHandler is currently locked */
    protected boolean isLocked;

    /** Time at which the connection managed by this ConnectionHandler was last used */
    protected long lastActivityTimestamp;

    /** Time at which the connection managed by this ConnectionHandler was last kept alive */
    protected long lastKeepAliveTimestamp;

    /** Number of seconds of inactivity after which this ConnectionHandler's connection will be closed by ConnectionPool */
    protected long closeOnInactivityPeriod = DEFAULT_CLOSE_ON_INACTIVITY_PERIOD;

    /** Number of seconds of inactivity after which this ConnectionHandler's connection will be kept alive by ConnectionPool */
    protected long keepAlivePeriod = DEFAULT_KEEP_ALIVE_PERIOD;

    /** Default 'close on inactivity' period */
    private final static long DEFAULT_CLOSE_ON_INACTIVITY_PERIOD = 300;

    /** Default keep alive period (-1, keep alive disabled) */
    private final static long DEFAULT_KEEP_ALIVE_PERIOD = -1;


    /**
     * Creates a new ConnectionHandler for the given server URL and using the Credentials included in the URL (if any).
     */
    public ConnectionHandler(FileURL serverURL) {
        realm = FileURL.resolveRealm(serverURL);
        this.credentials = serverURL.getCredentials();
    }


    /**
     * Returns the URL of the server this ConnectionHandler connects to.
     */
    public FileURL getRealm() {
        return realm;
    }


    /**
     * Returns the Credentials that are used to connect to the server, may be null if no credentials are used.
     */
    public Credentials getCredentials() {
        return credentials;
    }


    /**
     * Checks if the connection is currenty active (as returned by {@link #isConnected()} and if it isn't, starts it
     * by calling {@link #startConnection()}. Returns true if the connection was properly started, false if the
     * connection was already active, or throws an IOException if the connection could not be started.
     *
     * @return Returns true if the connection was properly started, false if the connection was already active
     * @throws IOException if the connection could not be started
     */
    public boolean checkConnection() throws IOException {
        if(!isConnected()) {
            if(Debug.ON) Debug.trace("not connected, starting connection, this="+this);
            startConnection();
            return true;
        }

        return false;
    }


    /**
     * Tries to lock this ConnectionHandler and returns true if it could be locked, false if it is already locked.
     *
     * @return true if it could be locked, false if it is already locked.
     */
    public synchronized boolean acquireLock() {
        if(isLocked) {
            if(Debug.ON) Debug.trace("!!!!! acquireLock() returning false, should not happen !!!!!", -1);
            return false;
        }

        isLocked = true;
        return true;
    }

    /**
     * Tries to release the lock on this ConnectionHandler and returns true if it could be locked, false if it
     * is not locked.
     *
     * @return true if it could be locked, false if it is not locked
     */
    public synchronized boolean releaseLock() {
        if(!isLocked) {
            if(Debug.ON) Debug.trace("!!!!! releaseLock() returning false, should not happen !!!!!", -1);
            return false;
        }

        isLocked = false;
        return true;
    }

    /**
     * Returns true if this ConnectionHandler is currently locked.
     */
    public synchronized boolean isLocked() {
        return isLocked;
    }


    /**
     * Updates the time at which the connection managed by this ConnectionHandler was last used to now (current time).
     */
    public void updateLastActivityTimestamp() {
        lastActivityTimestamp = System.currentTimeMillis();
    }

    /**
     * Returns the time at which the connection managed by this ConnectionHandler was last used.
     */
    public long getLastActivityTimestamp() {
        return lastActivityTimestamp;
    }


    /**
     * Updates the time at which the connection managed by this ConnectionHandler was last kept alive to now (current time).
     */
    public void updateLastKeepAliveTimestamp() {
        lastKeepAliveTimestamp = System.currentTimeMillis();
    }

    /**
     * Returns the time at which the connection managed by this ConnectionHandler was last kept alive.
     */
    public long getLastKeepAliveTimestamp() {
        return lastKeepAliveTimestamp;
    }


    /**
     * Returns the number of seconds of inactivity after which this ConnectionHandler's connection will be closed by
     * {@link ConnectionPool} calling {@link #closeConnection()}, or -1 to indicate that the connection should not
     * be automatically closed.
     *
     * <p>By default, this value is 300 seconds (5 minutes).
     */
    public long getCloseOnInactivityPeriod() {
        return closeOnInactivityPeriod;
    }

    /**
     * Sets the number of seconds of inactivity after which this ConnectionHandler's connection will be closed by
     * {@link ConnectionPool} calling {@link #closeConnection()}, or -1 to prevent the connection from being
     * automatically closed.
     *
     * <p>By default, this value is 300 seconds (5 minutes).
     */
    public void setCloseOnInactivityPeriod(long nbSeconds) {
        closeOnInactivityPeriod = nbSeconds;
    }


    /**
     * Returns the number of seconds of inactivity after which this ConnectionHandler's connection will be kept alive by
     * {@link ConnectionPool} calling {@link #keepAlive()}, or -1 to indicate that this connection should not be kept
     * alive.
     *
     * <p>By default, this value is -1 (keep alive disabled).
     */
    public long getKeepAlivePeriod() {
        return keepAlivePeriod;
    }

    /**
     * Sets the number of seconds of inactivity after which this ConnectionHandler's connection will be kept alive by
     * {@link ConnectionPool} calling {@link #keepAlive()}, or -1 to indicate that this connection should not be kept
     * alive.
     *
     * <p>By default, this value is -1 (keep alive disabled).
     */
    public void setKeepAlivePeriod(long nbSeconds) {
        keepAlivePeriod = nbSeconds;
    }


    /**
     * Returns true if the given Object is a ConnectionHandler with a realm and credentials equal to those of
     * this ConnectionHandler.
     */
    public boolean equals(Object o) {
        if(o==null || !(o instanceof ConnectionHandler))
            return false;

        ConnectionHandler connHandler = (ConnectionHandler)o;

        return equals(connHandler.realm, connHandler.credentials);
    }


    /**
     * Returns true if both the given realm and credentials are equal to those of this ConnectionHandler.
     */
    public boolean equals(FileURL realm, Credentials credentials) {

        if(!this.realm.equals(realm))
            return false;

        // Compare credentials. One or both Credentials instances may be null.

        // Note: Credentials.equals() considers null as equal to empty Credentials (see Credentials#isEmpty())
        return (this.credentials==null && credentials==null)
            || (this.credentials!=null && this.credentials.equals(credentials))
            || (credentials!=null && credentials.equals(this.credentials));
    }



    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Starts the connection managed by this ConnectionHandler, and throws an IOException if the connection could not
     * be established. This method may be called several times during the life of this ConnectionHandler, if the
     * connection dropped and must be re-established.
     *
     * @throws IOException if an error occurred while trying to establish the connection
     * @throws AuthException if an authentication error occurred (incorrect login or password, insufficient privileges...)
     */
    public abstract void startConnection() throws IOException, AuthException;

    /**
     * Returns true if the connection managed by this ConnectionHandler is currently active/established, in an
     * state where it can serve client requests.
     *
     * <p>Implementation note: This method must not perform any I/O which could block the calling thread.
     */
    public abstract boolean isConnected();

    /**
     * Closes the connection managed by this ConnectionHandler.
     *
     * <p>Implementation note: the implementation must guarantee that any calls to {@link #isConnected()} after this
     * method has been called return false.
     */
    public abstract void closeConnection();

    /**
     * Keeps this connection alive.
     *
     * <p>Implementation note: if keep alive is not available in the underlying protocol or
     * simply unnecessary, this method should be implemented as a no-op (do nothing).
     */
    public abstract void keepAlive();
}
