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

import com.mucommander.commons.file.AuthenticationType;
import com.mucommander.commons.file.DefaultSchemeHandler;
import com.mucommander.commons.file.DefaultSchemeParser;
import com.mucommander.commons.file.SchemeHandler;
import com.mucommander.commons.file.osgi.FileProtocolService;
import com.mucommander.commons.file.protocol.ProtocolProvider;
import com.mucommander.protocol.ui.ProtocolPanelProvider;
import com.mucommander.protocol.ui.ServerPanel;
import com.mucommander.protocol.ui.ServerPanelListener;
import javax.swing.JFrame;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * @author miroslav.spak
 */
public class Activator implements BundleActivator {
    static final String GCS_SCHEMA = "gcs";

    private ServiceRegistration<FileProtocolService> serviceRegistration;
    private ServiceRegistration<ProtocolPanelProvider> uiServiceRegistration;

    @Override
    public void start(BundleContext context) {
        var service = new FileProtocolService() {
            @Override
            public String getSchema() {
                return GCS_SCHEMA;
            }

            @Override
            public ProtocolProvider getProtocolProvider() {
                return new GoogleCloudStorageProtocolProvider();
            }

            @Override
            public SchemeHandler getSchemeHandler() {
                return new DefaultSchemeHandler(new DefaultSchemeParser(), 443, "/", AuthenticationType.NO_AUTHENTICATION, null);
            }
        };
        var panelProvider = new ProtocolPanelProvider() {
            @Override
            public String getSchema() {
                return GCS_SCHEMA;
            }

            @Override
            public ServerPanel get(ServerPanelListener listener, JFrame mainFrame) {
                return new GoogleCloudStoragePanel(listener, mainFrame);
            }

            @Override
            public int priority() {
                return 5000;
            }

            @Override
            public Class<? extends ServerPanel> getPanelClass() {
                return GoogleCloudStoragePanel.class;
            }
        };
        serviceRegistration = context.registerService(FileProtocolService.class, service, null);
        uiServiceRegistration = context.registerService(ProtocolPanelProvider.class, panelProvider, null);
    }

    @Override
    public void stop(BundleContext context) {
        serviceRegistration.unregister();
        uiServiceRegistration.unregister();
    }
}
