package com.mucommander.file.connection;

import com.mucommander.Debug;
import com.mucommander.auth.Credentials;
import com.mucommander.file.FileURL;

import java.util.Vector;


/**
 * @author Maxence Bernard
 */
public class ConnectionPool implements Runnable {

    /** Singleton instance */
    private static ConnectionPool instance = new ConnectionPool();

    /** List of registered ConnectionHandler */
    private final static Vector connectionHandlers = new Vector();

    /** The thread that monitors connections, null if there currently is no registered ConnectionHandler */
    private static Thread monitorThread;

    /** Controls how of often the thread monitor checks connections */
    private final static int MONITOR_SLEEP_PERIOD = 1000;

    

    public static synchronized ConnectionHandler getConnectionHandler(ConnectionHandlerFactory connectionHandlerFactory, FileURL url) {
        return getConnectionHandler(connectionHandlerFactory, url, false);
    }


    public static synchronized ConnectionHandler getConnectionHandler(ConnectionHandlerFactory connectionHandlerFactory, FileURL url, boolean acquireLock) {
        FileURL realm = FileURL.resolveRealm(url);

        synchronized(connectionHandlers) {      // Ensures that monitor thread is not currently changing the list while we access it
            int nbConn = connectionHandlers.size();
            ConnectionHandler connHandler;
            Credentials urlCredentials = url.getCredentials();
            // Try and find an appropriate existing ConnectionHandler
            for(int i=0; i<nbConn; i++) {
                connHandler = getConnectionHandlerAt(i);
                synchronized(connHandler) {     // Ensures that lock remains unchanged while we access/update it
                    // ConnectionHandler must match the realm and credentials and must not be locked
                    if(connHandler.equals(realm, urlCredentials) && !connHandler.isLocked()) {

                        // Try to acquire lock if a lock was requested
                        if(!acquireLock || connHandler.acquireLock()) {
                            if(Debug.ON) Debug.trace("returning ConnectionHandler "+connHandler+", realm ="+realm);

                            // Update last activity timestamp to now
                            connHandler.updateLastActivityTimestamp();

                            return connHandler;
                        }
                    }
                }
            }

            // No suitable ConnectionHandler found, create a new one
            connHandler = connectionHandlerFactory.createConnectionHandler(url);

            // Acquire lock if a lock was requested
            if(acquireLock)
                connHandler.acquireLock();

            if(Debug.ON) Debug.trace("adding new ConnectionHandler "+connHandler+", realm="+connHandler.getRealm());

            // Insert new ConnectionHandler at first position as if it has more chances to be accessed again soon
            connectionHandlers.insertElementAt(connHandler, 0);

            // Start monitor thread if it is not currently running (if there previously was no registered ConnectionHandler) 
            if(monitorThread==null) {
                if(Debug.ON) Debug.trace("starting monitor thread");
                monitorThread = new Thread(instance);
                monitorThread.start();
            }

            // Update last activity timestamp to now
            connHandler.updateLastActivityTimestamp();

            return connHandler;
        }
    }


    /**
     * Returns a list of registered ConnectionHandler instances. As the name of this method implies, the returned
     * list is only a snapshot and will not reflect the modifications that are made after this method has been called.
     * The Vector is a cloned one and thus can be safely modified. 
     *
     * @return a list of registered ConnectionHandler instances
     */
    public static Vector getConnectionHandlersSnapshot() {
        synchronized(connectionHandlers) {
            return (Vector)connectionHandlers.clone();
        }
    }
    

    /**
     * Returns the ConnectionHandler instance located at the given position in the list.
     */
    private static ConnectionHandler getConnectionHandlerAt(int i) {
        return (ConnectionHandler)connectionHandlers.elementAt(i);
    }


    /**
     * For debugging purposes only: returns a String dump of all registered ConnectionHandler instances.
     */
    public static String dumpConnections() {
        int nbElements = connectionHandlers.size();

        String rep = "";
        for(int i=0; i<nbElements; i++)
            rep += getConnectionHandlerAt(i)+" isConnected()="+getConnectionHandlerAt(i).isConnected()+" ";

        return rep;
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
                for(int i=0; i<connectionHandlers.size(); i++) {
                    ConnectionHandler connHandler = getConnectionHandlerAt(i);

                    synchronized(connHandler) {     // Ensures that no one is trying to acquire a lock on the connection while we access it 
                        if(!connHandler.isLocked()) {   // Do not touch ConnectionHandler if it is currently locked

                            // Remove ConnectionHandler instance from the list of registered ConnectionHandler
                            // if it is not connected
                            if(!connHandler.isConnected()) {
                                connectionHandlers.removeElementAt(i);

                                continue;       // Skips close on inactivity and keep alive checks
                            }

                            long lastUsed = connHandler.getLastActivityTimestamp();

                            // If time-to-live has been reached without any connection activity, remove ConnectionHandler
                            // from the list of registered ConnectionHandler and close the connection in a separate thread
                            long closePeriod = connHandler.getCloseOnInactivityPeriod();
                            if(closePeriod!=-1 && now-lastUsed>closePeriod*1000) {
                                connectionHandlers.removeElementAt(i);

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
                    if(Debug.ON) Debug.trace("No more ConnectionHandler, stopping monitor thread");
                    monitorThread = null;
                }
            }

            // Sleep for MONITOR_SLEEP_PERIOD milliseconds, minus the processing time of this loop
            try {
                Thread.sleep(Math.max(0, MONITOR_SLEEP_PERIOD-(System.currentTimeMillis()-now)));
            }
            catch(InterruptedException e) {}
        }
    }


    /**
     * Closes a specified ConnectionHandler's connection in a separate thread and removes the ConnectionHandler from
     * the list of registered ConnectionHandler instances.
     */
    private class CloseConnectionThread extends Thread {

        private ConnectionHandler connHandler;

        private CloseConnectionThread(ConnectionHandler connHandler) {
            this.connHandler = connHandler;
        }

        public void run() {
            if(Debug.ON) Debug.trace("closing connection: "+connHandler);

            // Try to close connection, only if it is connected
            if(connHandler.isConnected())
                connHandler.closeConnection();
        }
    }


    /**
     * Keeps alive a specified ConnectionHandler's connection in a separate thread. If the connection is not currently
     * active, {@link com.mucommander.file.connection.ConnectionHandler#keepAlive()} will not be called.
     */
    private class KeepAliveConnectionThread extends Thread {

        private final ConnectionHandler connHandler;

        private KeepAliveConnectionThread(ConnectionHandler connHandler) {
            this.connHandler = connHandler;
        }

        public void run() {
            if(Debug.ON) Debug.trace("keeping connection alive: "+connHandler);

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
