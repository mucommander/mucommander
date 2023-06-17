/**
 * This file is part of muCommander, http://www.mucommander.com
 * <p>
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.commons.file.protocol.gcs;

import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.connection.ConnectionHandler;
import com.mucommander.commons.file.connection.ConnectionHandlerFactory;

/**
 * Singleton instance implementation of the {@link ConnectionHandlerFactory} for the GCS connections.
 *
 * @author miroslav.spak
 */
public class GoogleCloudStorageConnectionHandlerFactory implements ConnectionHandlerFactory {

    /**
     * Singleton instance
     */
    private static GoogleCloudStorageConnectionHandlerFactory instance;

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
