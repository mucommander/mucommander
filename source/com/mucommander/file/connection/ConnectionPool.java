package com.mucommander.file.connection;

import com.mucommander.file.FileURL;
import com.mucommander.Debug;
import com.mucommander.auth.Credentials;

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


    public static synchronized ConnectionHandler getConnectionHandler(ConnectionFull connectionFull, FileURL url) {
        return getConnectionHandler(connectionFull, url, false);
    }


    public static synchronized ConnectionHandler getConnectionHandler(ConnectionFull connectionFull, FileURL url, boolean acquireLock) {
        FileURL realm = FileURL.resolveRealm(url);

        synchronized(connectionHandlers) {      // Ensures that monitor thread is not currently changing the list while we access it
            int nbConn = connectionHandlers.size();
            ConnectionHandler connHandler;
            Credentials urlCredentials = url.getCredentials();
            // Try and find an appropriate existing ConnectionHandler
            for(int i=0; i<nbConn; i++) {
                connHandler = getConnectionHandlerAt(i);
                synchronized(connHandler) {     // Ensures that lock remains unchanged while we access/update it
//                    if(connHandler!=null && realm.equals(connHandler.getRealm()) && !connHandler.isLocked()) {
                    if(realm.equals(connHandler.getRealm()) && !connHandler.isLocked()
                            && ((urlCredentials==null && connHandler.credentials==null)
                            || (urlCredentials!=null && urlCredentials.equals(connHandler.credentials))
                            || (connHandler.credentials!=null && connHandler.credentials.equals(urlCredentials)))) {

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
            connHandler = connectionFull.createConnectionHandler(url);

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
            rep += getConnectionHandlerAt(i)+" ";

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
                        if(!connHandler.isLocked()) {
                            long lastUsed = connHandler.getLastActivityTimestamp();

                            // If time-to-live has been reached without any connection activity, close connection
                            // and remove ConnectionHandler from registered ConnectionHandler list in a separate thread
                            long closePeriod = connHandler.getCloseOnInactivityPeriod();
                            if(closePeriod!=-1 && now-lastUsed>closePeriod*1000) {
                                // Bye bye ConnectionHandler!
                                connectionHandlers.removeElementAt(i);

                                // Stop monitor thread if there are no more ConnectionHandler
                                if(connectionHandlers.size()==0) {
                                    if(Debug.ON) Debug.trace("No more ConnectionHandler, stopping monitor thread");
                                    monitorThread = null;
                                }

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

            // Try to close connection, only if connected
            if(connHandler.isConnected())
                connHandler.closeConnection();
        }
    }


    /**
     * Keeps alive a specified ConnectionHandler's connection in a separate thread. If the connection is not currently
     * active, {@link com.mucommander.file.connection.ConnectionHandler#keepAlive()} will not be called.
     */
    private class KeepAliveConnectionThread extends Thread {

        private ConnectionHandler connHandler;

        private KeepAliveConnectionThread(ConnectionHandler connHandler) {
            this.connHandler = connHandler;
        }

        public void run() {
            if(Debug.ON) Debug.trace("keeping connection alive: "+connHandler);

            synchronized(connHandler) {
                // Ensures that lock was not grabbed in the meantime
                if(connHandler.isLocked())
                    return;

                // Keep alive connection only if it is active
                if(connHandler.isConnected())
                    connHandler.keepAlive();
            }
        }
    }

}
