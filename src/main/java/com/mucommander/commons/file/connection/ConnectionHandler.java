/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.file.connection;

import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * ConnectionHandler is a an abstract class that provides the basic operations for to interact with a server: establish
 * the connection, keep it alive and close it.
 *
 * @see com.mucommander.commons.file.connection.ConnectionPool
 * @author Maxence Bernard
 */
public abstract class ConnectionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionHandler.class);

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
     * Creates a new ConnectionHandler for the given server URL using the Credentials included in the URL (potentially
     * <code>null</code>).
     *
     * @param serverURL URL of the server to connect to
     */
    public ConnectionHandler(FileURL serverURL) {
        realm = serverURL.getRealm();
        this.credentials = serverURL.getCredentials();
    }


    /**
     * Returns the URL of the server this ConnectionHandler connects to.
     *
     * @return the URL of the server this ConnectionHandler connects to
     */
    public FileURL getRealm() {
        return realm;
    }


    /**
     * Returns the Credentials that are used to connect to the server, <code>null</code> if no credentials are used.
     *
     * @return the Credentials that are used to connect to the server, <code>null</code> if no credentials are used
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
            LOGGER.info("not connected, starting connection, this="+this);
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
            LOGGER.info("!!!!! acquireLock() returning false, should not happen !!!!!", new Throwable());
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
    public boolean releaseLock() {
        synchronized(this) {
            if(!isLocked) {
                LOGGER.info("!!!!! releaseLock() returning false, should not happen !!!!!", new Throwable());
                return false;
            }

            isLocked = false;
        }

        ConnectionPool.notifyConnectionHandlerLockReleased();

        return true;
    }

    /**
     * Returns <code>true</code> if this ConnectionHandler is currently locked.
     *
     * @return <code>true</code> if this ConnectionHandler is currently locked
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
     *
     * @return the time at which the connection managed by this ConnectionHandler was last used
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
     *
     * @return the time at which the connection managed by this ConnectionHandler was last kept alive
     */
    public long getLastKeepAliveTimestamp() {
        return lastKeepAliveTimestamp;
    }


    /**
     * Returns the number of seconds of inactivity after which {@link ConnectionPool} will close the connection by
     * calling {@link #closeConnection()}, <code>-1</code> to indicate that the connection should not be automatically
     * closed.
     *
     * <p>By default, this value is 300 seconds (5 minutes).</p>
     *
     * @return the number of seconds of inactivity after which {@link ConnectionPool} will close the connection,
     * <code>-1</code> to indicate that the connection should not be automatically closed
     */
    public long getCloseOnInactivityPeriod() {
        return closeOnInactivityPeriod;
    }

    /**
     * Sets the number of seconds of inactivity after which {@link ConnectionPool} will close the connection by calling
     * {@link #closeConnection()}, <code>-1</code> to prevent the connection from being automatically closed.
     *
     * <p>By default, this value is 300 seconds (5 minutes).</p>
     *
     * @param nbSeconds the number of seconds of inactivity after which {@link ConnectionPool} will close the connection,
     * <code>-1</code> to indicate that the connection should not be automatically closed
     */
    public void setCloseOnInactivityPeriod(long nbSeconds) {
        closeOnInactivityPeriod = nbSeconds;
    }


    /**
     * Returns the number of seconds of inactivity after which {@link ConnectionPool} will keep the connection alive
     * by calling {@link #keepAlive()}, <code>-1</code> to indicate that this connection should not be kept alive.
     *
     * <p>By default, this value is -1 (keep alive disabled).</p>
     *
     * @return the number of seconds of inactivity after which {@link ConnectionPool} will keep the connection alive
     * by calling {@link #keepAlive()}, <code>-1</code> to indicate that this connection should not be kept alive
     */
    public long getKeepAlivePeriod() {
        return keepAlivePeriod;
    }

    /**
     * Returns the number of seconds of inactivity after which {@link ConnectionPool} will keep the connection alive
     * by calling {@link #keepAlive()}, <code>-1</code> to indicate that this connection should not be kept alive.
     *
     * <p>By default, this value is -1 (keep alive disabled).</p>
     *
     * @param nbSeconds the number of seconds of inactivity after which {@link ConnectionPool} will keep the connection
     * alive by calling {@link #keepAlive()}, <code>-1</code> to indicate that this connection should not be kept alive
     */
    public void setKeepAlivePeriod(long nbSeconds) {
        keepAlivePeriod = nbSeconds;
    }


    /**
     * Returns <code>true</code> if the given Object is a ConnectionHandler whose realm and credentials are equal to
     * those of this ConnectionHandler. The credentials comparison is password-sensitive.
     *
     * @param o the Object to compare for equality
     * @see Credentials#equals(Object, boolean)
     */
    public boolean equals(Object o) {
        if(o==null || !(o instanceof ConnectionHandler))
            return false;

        ConnectionHandler connHandler = (ConnectionHandler)o;

        return equals(connHandler.realm, connHandler.credentials);
    }


    /**
     * Returns <code>true</code> if both the given realm and credentials are equal to those of this ConnectionHandler.
     * The credentials comparison is password-sensitive.
     *
     * @param realm the FileURL to compare against this ConnectionHandler's
     * @param credentials the Credentials to compare against this ConnectionHandler's
     * @return true if both the given realm and credentials are equal to those of this ConnectionHandler
     * @see Credentials#equals(Object, boolean)
     */
    public boolean equals(FileURL realm, Credentials credentials) {

        if(!this.realm.equals(realm, false, true))
            return false;

        // Compare credentials. One or both Credentials instances may be null.

        // Note: Credentials.equals() considers null as equal to empty Credentials (see Credentials#isEmpty())
        return (this.credentials==null && credentials==null)
            || (this.credentials!=null && this.credentials.equals(credentials, true))
            || (credentials!=null && credentials.equals(this.credentials, true));
    }


    /**
     * Throws an {@link AuthException} using this connection handler's realm, credentials and the message passed as
     * an argument (can be <code>null</code>). The FileURL instance representing the realm that is used to create
     * the <code>AuthException</code> is a clone of this realm, making it safe for modification.
     *
     * @param message the message to pass to AuthException's constructor, can be <code>null</code>
     * @throws AuthException always throws the created AuthException
     */
    public void throwAuthException(String message) throws AuthException {
        FileURL clonedRealm = (FileURL)realm.clone();
        clonedRealm.setCredentials(credentials);

        throw new AuthException(clonedRealm, message);
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
     * Returns <code>true</code> if the connection managed by this ConnectionHandler is currently active/established,
     * in a state that makes it possible to serve client requests.
     *
     * <p>Implementation note: This method must not perform any I/O which could block the calling thread.</p>
     *
     * @return <code>true</code> if the connection managed by this ConnectionHandler is currently active/established
     */
    public abstract boolean isConnected();

    /**
     * Closes the connection managed by this ConnectionHandler.
     *
     * <p>Implementation note: the implementation must guarantee that any calls to {@link #isConnected()} after this
     * method has been called return false.</p>
     */
    public abstract void closeConnection();

    /**
     * Keeps this connection alive.
     *
     * <p>Implementation note: if keep alive is not available in the underlying protocol or
     * simply unnecessary, this method should be implemented as a no-op (do nothing).</p>
     */
    public abstract void keepAlive();
}
