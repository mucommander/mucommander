package com.mucommander.file.connection;

import com.mucommander.file.FileURL;

/**
 * This interface should be implemented by {@link com.mucommander.file.AbstractFile} classes that are 'connection-full'
 * (by opposition to connection-less) and have to manage a connection (e.g. FTP). This allows to take advantage of
 * {@link ConnectionPool} to share connections across {@link com.mucommander.file.AbstractFile} instances.  
 *
 * @author Maxence Bernard
 */
public interface ConnectionFull {

    public ConnectionHandler createConnectionHandler(FileURL location);

    public ConnectionHandler getConnectionHandler();
}
