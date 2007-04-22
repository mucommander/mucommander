package com.mucommander.file.connection;

import com.mucommander.file.FileURL;

/**
 * This interface should be implemented by classes that are able to create ConnectionHandler instances for a given
 * server location, typically {@link com.mucommander.file.AbstractFile} implementations.
 *
 * <p>This interface allows to take advantage of {@link ConnectionPool} to share connections across
 * {@link com.mucommander.file.AbstractFile} instances.  
 *
 * @author Maxence Bernard
 */
public interface ConnectionHandlerFactory {

    /**
     * Creates and returns a {@link ConnectionHandler} instance for the given location.
     */
    public ConnectionHandler createConnectionHandler(FileURL location);
}
