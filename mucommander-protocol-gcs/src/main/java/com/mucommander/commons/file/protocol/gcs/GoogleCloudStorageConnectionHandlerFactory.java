package com.mucommander.commons.file.protocol.gcs;

import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.connection.ConnectionHandler;
import com.mucommander.commons.file.connection.ConnectionHandlerFactory;
import com.mucommander.commons.file.connection.ConnectionPool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GoogleCloudStorageConnectionHandlerFactory implements ConnectionHandlerFactory {

    /**
     * Singleton instance
     */
    private static GoogleCloudStorageConnectionHandlerFactory instance;

    private final Map<String, GoogleCloudStorageConnectionHandler> handlerCache = new HashMap<>();

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

    public GoogleCloudStorageClient getCloudStorageClient(FileURL fileURL) throws IOException {
        // Checkout the cache for this host, i.e. project
        var projectId = fileURL.getHost();
        var handler = handlerCache.get(projectId);

        if (handler == null || !handler.isConnected()) {
            // Get connection handler using connection pool
            handler = (GoogleCloudStorageConnectionHandler) ConnectionPool.getConnectionHandler(
                    this, fileURL, false);
            // Cache the connection
            handlerCache.put(projectId, handler);
        }

        // Return client only when connection was checked
        handler.checkConnection();
        return handler.getClient();
    }

    @Override
    public ConnectionHandler createConnectionHandler(FileURL location) {
        return new GoogleCloudStorageConnectionHandler(location);
    }
}
