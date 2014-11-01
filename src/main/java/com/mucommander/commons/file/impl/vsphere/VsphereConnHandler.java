/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.commons.file.impl.vsphere;

import java.io.IOException;

import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.connection.ConnectionHandler;
import com.vmware.vim25.InvalidLocaleFaultMsg;
import com.vmware.vim25.InvalidLoginFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;

/**
 * Manage VSphere connections.
 * 
 * @author Yuval Kohavi <yuval.kohavi@intigua.com>
 * 
 */
public class VsphereConnHandler extends ConnectionHandler {

	private VSphereClient client = null;
	private FileURL location;

	public VSphereClient getClient() {
		return client;
	}

	public VsphereConnHandler(FileURL serverURL) {
		super(serverURL);
		location = serverURL;
	}

	private void initClientIfNeeded() throws RuntimeFaultFaultMsg,
			InvalidLocaleFaultMsg, InvalidLoginFaultMsg {
		if (client == null) {
			client = new VSphereClient(location.getHost(), location
					.getCredentials().getLogin(), location.getCredentials()
					.getPassword());
			client.connect();
		}
	}

	@Override
	public void startConnection() throws IOException, AuthException {
		try {
			initClientIfNeeded();
		} catch (RuntimeFaultFaultMsg e) {

			throw new IOException(e);
		} catch (InvalidLocaleFaultMsg e) {

			throw new IOException(e);
		} catch (InvalidLoginFaultMsg e) {
			throw new AuthException(location, e.getMessage());
		}
	}

	@Override
	public boolean isConnected() {
		return client != null && client.isConnected();
	}

	@Override
	public void closeConnection() {
		try {
			client.disconnect();
		} catch (RuntimeFaultFaultMsg e) {
			// nothing we can do... ignore..
			e.printStackTrace();
		}
		client = null;
	}

	@Override
	public void keepAlive() {

		if (client != null) {
			try {
				doKeepAlive();
			} catch (RuntimeFaultFaultMsg e) {
				client = null;
			}
		}
	}

	private void doKeepAlive() throws RuntimeFaultFaultMsg {
		// do nothing, to keep alive
		client.getVimPort().currentTime(client.getServiceInstance());
	}
}
