package com.mucommander.file.connection;

import com.mucommander.file.FileURL;
import com.mucommander.Debug;

import java.util.Vector;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;


/**
 * @author Maxence Bernard
 */
public class ConnectionPool {

    private final static Vector connectionHandlers = new Vector();

    private final static ReferenceQueue refQueue = new ReferenceQueue();

    
//    public static void addConnectionHandler(ConnectionHandler connHandler) {
//        // Clean GCed references
//        cleanDeadReferences();
//
//        FileURL realm = connHandler.getRealm();
//        int i;
//        while((i=indexOf(realm))!=-1) {
//            if(Debug.ON) Debug.trace("Removing existing ConnectionHandler "+connectionHandlers.elementAt(i)+" for realm "+realm);
//
//            connectionHandlers.removeElementAt(i);
//        }
//
//        if(Debug.ON) Debug.trace("Adding ConnectionHandler "+connHandler+" for realm "+realm);
//
//        // Insert new ConnectionHandler at position as if has more chances to be retrieved
//        connectionHandlers.insertElementAt(new WeakReference(connHandler, refQueue), 0);
//    }


    public static ConnectionHandler getConnectionHandler(ConnectionFull connectionFull, FileURL url) {
        // Clean GCed references
        cleanDeadReferences();

        FileURL realm = FileURL.resolveRealm(url);

        int i = indexOf(realm);
        if(i!=-1)
            return getConnectionHandlerAt(i);

        ConnectionHandler connHandler = connectionFull.createConnectionHandler(url);

        if(Debug.ON) Debug.trace("adding ConnectionHandler "+connHandler+", realm="+connHandler.getRealm());

        // Insert new ConnectionHandler at first position as if has more chances to be retrieved
        connectionHandlers.insertElementAt(new WeakReference(connHandler, refQueue), 0);

        return connHandler;
    }


//    public static void removeConnectionHandler(ConnectionHandler connHandler) {
//        // Clean GCed references
//        cleanDeadReferences();
//
//        int i = indexOf(connHandler.getRealm());
//        if(i!=-1)
//            connectionHandlers.removeElementAt(i);
//
//    }


    private static int indexOf(FileURL realm) {
        int nbElements = connectionHandlers.size();
        ConnectionHandler connHandler;

        if(Debug.ON) Debug.trace("called, realm="+realm);

        for(int i=0; i<nbElements; i++) {
            connHandler = getConnectionHandlerAt(i);
            if(connHandler!=null && (realm.equals(connHandler.getRealm()))) {
                if(Debug.ON) Debug.trace("returning ConnectionHandler "+connHandler);
                return i;
            }
        }

        return -1;
    }


    private static ConnectionHandler getConnectionHandlerAt(int i) {
        return (ConnectionHandler)(((WeakReference)connectionHandlers.elementAt(i)).get());
    }


    private static void cleanDeadReferences() {
        Reference deadReference;

        while((deadReference=refQueue.poll())!=null) {
            if(Debug.ON) Debug.trace("found dead reference: "+deadReference);
            connectionHandlers.remove(deadReference);
        }
    }
}
