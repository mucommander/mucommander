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
package dev.barebones.commander.commons.file.protocol.ovirt;

import javax.swing.JFrame;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import dev.barebones.commander.commons.file.AuthenticationType;
import dev.barebones.commander.commons.file.DefaultSchemeHandler;
import dev.barebones.commander.commons.file.DefaultSchemeParser;
import dev.barebones.commander.commons.file.SchemeHandler;
import dev.barebones.commander.commons.file.osgi.FileProtocolService;
import dev.barebones.commander.commons.file.protocol.ProtocolProvider;
import dev.barebones.commander.protocol.ui.ProtocolPanelProvider;
import dev.barebones.commander.protocol.ui.ServerPanel;
import dev.barebones.commander.protocol.ui.ServerPanelListener;

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
				return "ovirt";
			}

			@Override
			public ProtocolProvider getProtocolProvider() {
				return new OvirtProtocolProvider();
			}

			@Override
			public SchemeHandler getSchemeHandler() {
				return new DefaultSchemeHandler(new DefaultSchemeParser(true), 443, "/", AuthenticationType.AUTHENTICATION_REQUIRED, null);
			}
		};
		ProtocolPanelProvider panelProvider = new ProtocolPanelProvider() {
			@Override
			public String getSchema() {
				return "ovirt";
			}

			@Override
			public ServerPanel get(ServerPanelListener listener, JFrame mainFrame) {
				return new OvirtPanel(listener, mainFrame);
			}


		    @Override
		    public int priority() {
		        return 7000;
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
