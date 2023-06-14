package com.mucommander.commons.file.protocol.gcs;

import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.connection.ConnectionHandler;
import com.mucommander.commons.file.connection.ConnectionHandlerFactory;

public class GoogleCloudStorageConnectionHandlerFactory implements ConnectionHandlerFactory {

    /**
     * Singleton instance
     */
    private static GoogleCloudStorageConnectionHandlerFactory instance;

    /**
     * TODO
     *
     * @return
     */
    public static GoogleCloudStorageConnectionHandlerFactory getInstance() {
        if (instance == null) {
            instance = new GoogleCloudStorageConnectionHandlerFactory();
        }
        return instance;
    }

    @Override
    public ConnectionHandler createConnectionHandler(FileURL location) {
        return new GoogleCloudStorageConnectionHandler(location);
    }
}
