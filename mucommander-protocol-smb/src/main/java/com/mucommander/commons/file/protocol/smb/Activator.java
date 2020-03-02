/**
 * This file is part of muCommander, http://www.mucommander.com
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
package com.mucommander.commons.file.protocol.smb;

import javax.swing.JFrame;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.mucommander.commons.file.AuthenticationType;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.DefaultSchemeHandler;
import com.mucommander.commons.file.DefaultSchemeParser;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.SchemeHandler;
import com.mucommander.commons.file.osgi.FileProtocolService;
import com.mucommander.commons.file.protocol.ProtocolProvider;
import com.mucommander.protocol.ui.ProtocolPanelProvider;
import com.mucommander.protocol.ui.ServerPanel;
import com.mucommander.protocol.ui.ServerPanelListener;

/**
 * @author Arik Hadas
 */
public class Activator implements BundleActivator {

	private ServiceRegistration<FileProtocolService> serviceRegistration;
	private ServiceRegistration<ProtocolPanelProvider> uiServiceRegistration;

	@Override
	public void start(BundleContext context) throws Exception {
		FileProtocolService service = new FileProtocolService() {
			@Override
			public String getSchema() {
				return "smb";
			}

			@Override
			public ProtocolProvider getProtocolProvider() {
				return new SMBProtocolProvider();
			}

			@Override
			public SchemeHandler getSchemeHandler() {
				return new DefaultSchemeHandler(new DefaultSchemeParser(), -1, "/", AuthenticationType.AUTHENTICATION_REQUIRED, new Credentials("GUEST", "")) {
		            @Override
		            public FileURL getRealm(FileURL location) {
		                FileURL realm = new FileURL(this);

		                String newPath = location.getPath();
		                // Find first path token (share)
		                int pos = newPath.indexOf('/', 1);
		                newPath = newPath.substring(0, pos==-1?newPath.length():pos+1);

		                realm.setPath(newPath);
		                realm.setScheme(location.getScheme());
		                realm.setHost(location.getHost());
		                realm.setPort(location.getPort());

		                // Copy properties (if any)
		                realm.importProperties(location);

		                return realm;
		            }
		        };
			}
		};
		ProtocolPanelProvider panelProvider = new ProtocolPanelProvider() {
			@Override
			public String getSchema() {
				return "smb";
			}

			@Override
			public ServerPanel get(ServerPanelListener listener, JFrame mainFrame) {
				return new SMBPanel(listener, mainFrame);
			}

		    @Override
		    public int priority() {
		        return 4000;
		    }
		};
		serviceRegistration = context.registerService(FileProtocolService.class, service, null);
		uiServiceRegistration = context.registerService(ProtocolPanelProvider.class, panelProvider, null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		serviceRegistration.unregister();
		uiServiceRegistration.unregister();
	}

}
