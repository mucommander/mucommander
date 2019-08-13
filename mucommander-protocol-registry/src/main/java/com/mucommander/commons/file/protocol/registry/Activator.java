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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.mucommander.commons.file.AuthenticationType;
import com.mucommander.commons.file.DefaultSchemeHandler;
import com.mucommander.commons.file.DefaultSchemeParser;
import com.mucommander.commons.file.SchemeHandler;
import com.mucommander.commons.file.osgi.FileProtocolService;
import com.mucommander.commons.file.protocol.ProtocolProvider;
import com.mucommander.ui.dialog.server.ProtocolPanelProvider;
import com.mucommander.ui.dialog.server.ServerConnectDialog;
import com.mucommander.ui.dialog.server.ServerPanel;
import com.mucommander.ui.main.MainFrame;

/**
 * @author Daniel Erez
 */
public class Activator implements BundleActivator {

	private ServiceRegistration<FileProtocolService> serviceRegistrationDocker;
	private ServiceRegistration<FileProtocolService> serviceRegistrationOCI;
	private ServiceRegistration<FileProtocolService> serviceRegistrationDir;
	private ServiceRegistration<FileProtocolService> serviceRegistrationRegistry;
	private ServiceRegistration<ProtocolPanelProvider> uiServiceRegistration;

	@Override
	public void start(BundleContext context) throws Exception {
		FileProtocolService serviceDocker = new FileProtocolService() {
			@Override
			public String getSchema() {
				return "docker";
			}

			@Override
			public ProtocolProvider getProtocolProvider() {
				return new RegistryProtocolProvider();
			}

			@Override
			public SchemeHandler getSchemeHandler() {
				return new DefaultSchemeHandler(new DefaultSchemeParser(true), 80, "/", AuthenticationType.NO_AUTHENTICATION, null);
			}
		};
		FileProtocolService serviceOCI = new FileProtocolService() {
			@Override
			public String getSchema() {
				return "oci";
			}

			@Override
			public ProtocolProvider getProtocolProvider() {
				return new RegistryProtocolProvider();
			}

			@Override
			public SchemeHandler getSchemeHandler() {
				return new DefaultSchemeHandler(new DefaultSchemeParser(true), 80, "/", AuthenticationType.NO_AUTHENTICATION, null);
			}
		};
		FileProtocolService serviceDir = new FileProtocolService() {
			@Override
			public String getSchema() {
				return "dir";
			}

			@Override
			public ProtocolProvider getProtocolProvider() {
				return new RegistryProtocolProvider();
			}

			@Override
			public SchemeHandler getSchemeHandler() {
				return new DefaultSchemeHandler(new DefaultSchemeParser(true), 80, "/", AuthenticationType.NO_AUTHENTICATION, null);
			}
		};
		FileProtocolService serviceRegistry = new FileProtocolService() {
			@Override
			public String getSchema() {
				return "registry";
			}

			@Override
			public ProtocolProvider getProtocolProvider() {
				return new RegistryProtocolProvider();
			}

			@Override
			public SchemeHandler getSchemeHandler() {
				return new DefaultSchemeHandler(new DefaultSchemeParser(true), 80, "/", AuthenticationType.NO_AUTHENTICATION, null);
			}
		};
		ProtocolPanelProvider panelProvider = new ProtocolPanelProvider() {
			@Override
			public String getSchema() {
				// TODO: better naming to reflect this refers to container images registry
				return "registry";
			}

			@Override
			public ServerPanel get(ServerConnectDialog dialog, MainFrame mainFrame) {
				boolean isSkopeoAvailable = SkopeoCommandExecutor.checkSkopeo();
				return new RegistryPanel(dialog, mainFrame, isSkopeoAvailable);
			}
		};
		serviceRegistrationDocker = context.registerService(FileProtocolService.class, serviceDocker, null);
		serviceRegistrationOCI = context.registerService(FileProtocolService.class, serviceOCI, null);
		serviceRegistrationDir = context.registerService(FileProtocolService.class, serviceDir, null);
		serviceRegistrationRegistry = context.registerService(FileProtocolService.class, serviceRegistry, null);
		uiServiceRegistration = context.registerService(ProtocolPanelProvider.class, panelProvider, null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		serviceRegistrationDocker.unregister();
		serviceRegistrationOCI.unregister();
		serviceRegistrationDir.unregister();
		serviceRegistrationRegistry.unregister();
		uiServiceRegistration.unregister();
	}

}
