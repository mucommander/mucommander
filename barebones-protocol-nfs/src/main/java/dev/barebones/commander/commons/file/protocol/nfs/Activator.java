/*
 * Copyright (C) 2002-2026 muCommander contributors
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */
package dev.barebones.commander.commons.file.protocol.nfs;

import javax.swing.JFrame;

import dev.barebones.commander.commons.file.AuthenticationType;
import dev.barebones.commander.commons.file.DefaultSchemeHandler;
import dev.barebones.commander.commons.file.DefaultSchemeParser;
import dev.barebones.commander.commons.file.SchemeHandler;
import dev.barebones.commander.commons.file.osgi.FileProtocolService;
import dev.barebones.commander.commons.file.osgi.FileProtocolServiceTracker;
import dev.barebones.commander.commons.file.protocol.ProtocolProvider;
import dev.barebones.commander.protocol.ui.ProtocolPanelProvider;
import dev.barebones.commander.protocol.ui.ServerPanel;
import dev.barebones.commander.protocol.ui.ServerPanelListener;
import dev.barebones.commander.protocol.ui.ProtocolPanelRegistry;

public final class Activator {

    private Activator() {
    }

    public static void register() {
        FileProtocolServiceTracker.register(new FileProtocolService() {
            @Override public String getSchema() { return "nfs"; }
            @Override public ProtocolProvider getProtocolProvider() { return new NFSProtocolProvider(); }
            @Override public SchemeHandler getSchemeHandler() {
                return new DefaultSchemeHandler(new DefaultSchemeParser(), 2049, "/",
                        AuthenticationType.NO_AUTHENTICATION, null);
            }
        });

        ProtocolPanelRegistry.register(new ProtocolPanelProvider() {
            @Override public String getSchema() { return "nfs"; }
            @Override public ServerPanel get(ServerPanelListener listener, JFrame mainFrame) {
                return new NFSPanel(listener, mainFrame);
            }
            @Override public int priority() { return 2000; }
            @Override public Class<? extends ServerPanel> getPanelClass() { return NFSPanel.class; }
        });
    }
}
