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

import java.io.InterruptedIOException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURL;


/**
 * @see com.mucommander.commons.file.connection.ConnectionHandler
 * @author Maxence Bernard
 */
public class ConnectionPool implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);

    /** Singleton instance */
    private static ConnectionPool instance = new ConnectionPool();

    /** List of registered ConnectionHandler */
    private final static Vector<ConnectionHandler> connectionHandlers = new Vector<ConnectionHandler>();

    /** The thread that monitors connections, null if there currently is no registered ConnectionHandler */
    private static Thread monitorThread;

    /** Controls how of often the thread monitor checks connections */
    private final static int MONITOR_SLEEP_PERIOD = 1000;

    /** Maximum number of simultaneous connections per realm/credentials combo */
    private final static int MAX_CONNECTIONS_PER_REALM = 4;


    public static ConnectionHandler getConnectionHandler(ConnectionHandlerFactory connectionHandlerFactory, FileURL url, boolean acquireLock) throws InterruptedIOException {
        FileURL realm = url.getRealm();

        while(true) {
            synchronized(connectionHandlers) {      // Ensures that monitor thread is not currently changing the list while we access it
                Credentials urlCredentials = url.getCredentials();
                int matchingConnHandlers = 0;

                // Try and find an appropriate existing ConnectionHandler
                for(ConnectionHandler connHandler : connectionHandlers) {
                	// ConnectionHandler must match the realm and credentials and must not be locked
                	if(connHandler.equals(realm, urlCredentials)) {
                		matchingConnHandlers++;
                		synchronized(connHandler) {     // Ensures that lock remains unchanged while we access/update it
                			if(!connHandler.isLocked()) {
                				// Try to acquire lock if a lock was requested
                				if(!acquireLock || connHandler.acquireLock()) {
                					LOGGER.info("returning ConnectionHandler {}, realm = {}", connHandler, realm);

                					// Update last activity timestamp to now
                					connHandler.updateLastActivityTimestamp();

                					return connHandler;
                				}
                			}
                		}
                	}
                    
                    if(matchingConnHandlers==MAX_CONNECTIONS_PER_REALM) {
                        LOGGER.info("Maximum number of connection per realm reached, waiting for one to be removed or released...");
                        try {
                            // Wait for a ConnectionHandler to be released or removed from the pool
                            connectionHandlers.wait();      // relinquishes the lock on connectionHandlers
                            break;
                        }
                        catch(InterruptedException e) {
                            LOGGER.info("Interrupted while waiting on a connection for {}", url, e);
                            throw new InterruptedIOException();
                        }
                    }
                }

                if(matchingConnHandlers==MAX_CONNECTIONS_PER_REALM)
                    continue;

                // No suitable ConnectionHandler found, create a new one
                ConnectionHandler connHandler = connectionHandlerFactory.createConnectionHandler(url);

                // Acquire lock if a lock was requested
                if(acquireLock)
                    connHandler.acquireLock();

                LOGGER.info("adding new ConnectionHandler {}, realm = {}", connHandler, connHandler.getRealm());

                // Insert new ConnectionHandler at first position as if it has more chances to be accessed again soon
                connectionHandlers.insertElementAt(connHandler, 0);

                // Start monitor thread if it is not currently running (if there previously was no registered ConnectionHandler)
                if(monitorThread==null) {
                    LOGGER.info("starting monitor thread");
                    monitorThread = new Thread(instance);
                    monitorThread.start();
                }

                // Update last activity timestamp to now
                connHandler.updateLastActivityTimestamp();

                return connHandler;
            }
        }
    }


    /**
     * Returns a list of registered ConnectionHandler instances. As the name of this method implies, the returned
     * list is only a snapshot and will not reflect the modifications that are made after this method has been called.
     * The Vector is a cloned one and thus can be safely modified. 
     *
     * @return a list of registered ConnectionHandler instances
     */
    public static Vector<ConnectionHandler> getConnectionHandlersSnapshot() {
        return (Vector<ConnectionHandler>)connectionHandlers.clone();
    }
    
    /**
     * Returns the ConnectionHandler instance located at the given position in the list.
     */
    private static ConnectionHandler getConnectionHandlerAt(int i) {
        return connectionHandlers.elementAt(i);
    }

    /**
     * Called by {@link ConnectionHandler#releaseLock()} to notify the <code>ConnectionHandler</code> that a
     * <code>ConnectionHandler</code> has been released.
     */
    static void notifyConnectionHandlerLockReleased() {
        synchronized(connectionHandlers) {
            // Notify any thread waiting for a ConnectionHandler to be released
            connectionHandlers.notify();
        }
    }

    /**
     * Monitors connections and periodically:
     * <ul>
     *   <li>keeps connections alive
     *   <li>closes and removes connections that have expired
     * </ul>
     */
    public void run() {

        while(monitorThread!=null) {        // Thread will be interrupted by CloseConnectionThread if there are no more ConnectionHandler
            long now = System.currentTimeMillis();

            synchronized(connectionHandlers) {      // Ensures that getConnectionHandler is not currently changing the list while we access it
                for(ConnectionHandler connHandler : connectionHandlers) {

                    synchronized(connHandler) {     // Ensures that no one is trying to acquire a lock on the connection while we access it 
                        if(!connHandler.isLocked()) {   // Do not touch ConnectionHandler if it is currently locked

                            // Remove ConnectionHandler instance from the list of registered ConnectionHandler
                            // if it is not connected
                            if(!connHandler.isConnected()) {
                                LOGGER.info("Removing unconnected ConnectionHandler {}", connHandler);

                                connectionHandlers.remove(connHandler);
                                // Notify any thread waiting for a ConnectionHandler to be released
                                connectionHandlers.notify();

                                continue;       // Skips close on inactivity and keep alive checks
                            }

                            long lastUsed = connHandler.getLastActivityTimestamp();

                            // If time-to-live has been reached without any connection activity, remove ConnectionHandler
                            // from the list of registered ConnectionHandler and close the connection in a separate thread
                            long closePeriod = connHandler.getCloseOnInactivityPeriod();
                            if(closePeriod!=-1 && now-lastUsed>closePeriod*1000) {
                                LOGGER.info("Removing timed-out ConnectionHandler {}",connHandler);

                                connectionHandlers.remove(connHandler);
                                // Notify any thread waiting for a ConnectionHandler to be released
                                connectionHandlers.notify();

                                // Close connection in a separate thread as it could lock this thread
                                new CloseConnectionThread(connHandler).start();

                                continue;       // Skips keep alive check
                            }

                            // If keep-alive period has been reached without any connection activity or a keep alive,
                            // keep connection alive in a separate thread
                            long keepAlivePeriod = connHandler.getKeepAlivePeriod(); 
                            if(keepAlivePeriod!=-1 && now-Math.max(lastUsed, connHandler.getLastKeepAliveTimestamp())>keepAlivePeriod*1000) {
                                // Update last keep alive timestamp to now
                                connHandler.updateLastKeepAliveTimestamp();

                                // Keep connection alive in a separate thread as it could lock this thread
                                new KeepAliveConnectionThread(connHandler).start();
                            }
                        }
                    }
                }

                // Stop monitor thread if there are no more ConnectionHandler
                if(connectionHandlers.size()==0) {
                    LOGGER.info("No more ConnectionHandler, stopping monitor thread");
                    monitorThread = null;
                }
            }

            // Sleep for MONITOR_SLEEP_PERIOD milliseconds, minus the processing time of this loop
            try {
                Thread.sleep(Math.max(0, MONITOR_SLEEP_PERIOD-(System.currentTimeMillis()-now)));
            }
            catch(InterruptedException e) {
                // Will loop again
            }
        }
    }


    /**
     * Closes a specified ConnectionHandler's connection in a separate thread and removes the ConnectionHandler from
     * the list of registered ConnectionHandler instances.
     */
    private static class CloseConnectionThread extends Thread {

        private ConnectionHandler connHandler;

        private CloseConnectionThread(ConnectionHandler connHandler) {
            this.connHandler = connHandler;
        }

        @Override
        public void run() {
            // Try to close connection, only if it is connected
            if(connHandler.isConnected()) {
                LOGGER.info("Closing connection held by {}", connHandler);
                connHandler.closeConnection();
            }
        }
    }


    /**
     * Keeps alive a specified ConnectionHandler's connection in a separate thread. If the connection is not currently
     * active, {@link com.mucommander.commons.file.connection.ConnectionHandler#keepAlive()} will not be called.
     */
    private static class KeepAliveConnectionThread extends Thread {

        private final ConnectionHandler connHandler;

        private KeepAliveConnectionThread(ConnectionHandler connHandler) {
            this.connHandler = connHandler;
        }

        @Override
        public void run() {
            LOGGER.info("keeping connection alive: {}", connHandler);

            synchronized(connHandler) {
                // Ensures that lock was not grabbed in the meantime
                if(connHandler.isLocked())
                    return;

                // Keep alive connection, only if it is connected
                if(connHandler.isConnected())
                    connHandler.keepAlive();
            }
        }
    }

}
