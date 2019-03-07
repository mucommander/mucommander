/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2019
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

package com.mucommander.commons.file.protocol.registry;

import java.io.IOException;

import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.connection.ConnectionHandler;

/**
 * 
 * @author Daniel Erez
 */
public class RegistryConnHandler extends ConnectionHandler {

	private FileURL location;
	private RegistryClient client;

	public RegistryClient getClient() {
		return client;
	}

	public RegistryConnHandler(FileURL serverURL) {
		super(serverURL);
		this.location = serverURL;
	}

	@Override
	public void startConnection() throws IOException {
		if (client == null) {
			client = new RegistryClient(location.getCredentials(), location.toString());
			client.connect();
		}
	}

	@Override
	public boolean isConnected() {
		return client != null;
	}

	@Override
	public void closeConnection() {
		try {
			client.close();
		} catch (IOException e) {
			// nothing we can do... ignore..
		}
		client = null;
	}

	@Override
	public void keepAlive() {
		// do nothing
	}

}
