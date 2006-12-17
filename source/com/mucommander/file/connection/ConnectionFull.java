package com.mucommander.file.connection;

import com.mucommander.file.FileURL;

/**
 * @author Maxence Bernard
 */
public interface ConnectionFull {

    public ConnectionHandler createConnectionHandler(FileURL location);

//    public void setConnectionHandler(ConnectionHandler connHandler);

    public ConnectionHandler getConnectionHandler();
}
