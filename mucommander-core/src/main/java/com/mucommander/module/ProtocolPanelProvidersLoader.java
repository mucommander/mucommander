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

package com.mucommander.module;

import com.mucommander.protocol.ui.ProtocolPanelProvider;
import com.mucommander.ui.dialog.server.ServerConnectDialog;
import com.mucommander.ui.main.DrivePopupButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public class ProtocolPanelProvidersLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolPanelProvidersLoader.class);

    public static void load() {
        for (ProtocolPanelProvider service : ServiceLoader.load(ProtocolPanelProvider.class)) {
            ServerConnectDialog.register(service);
            if (service.getPanelClass() != null)
                DrivePopupButton.register(service);
            LOGGER.info("ProtocolPanelProvider is registered: " + service);
        }
    }
}
