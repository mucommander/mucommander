/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.tailscale.ui;

import dev.barebones.commander.protocol.ui.ProtocolPanelProvider;
import dev.barebones.commander.protocol.ui.ServerPanel;
import dev.barebones.commander.protocol.ui.ServerPanelListener;

import javax.swing.JFrame;

/**
 * {@link ProtocolPanelProvider} that contributes the
 * {@link TailscalePeerPanel} tab to the Connect-to-server dialog.
 * Schema "tailscale" is virtual — selecting a peer + protocol there
 * produces an {@code sftp://} / {@code nfs://} / {@code smb://} URL
 * with the peer's MagicDNS hostname.
 */
public final class TailscalePanelProvider implements ProtocolPanelProvider {

    @Override
    public String getSchema() {
        return "tailscale";
    }

    @Override
    public ServerPanel get(ServerPanelListener listener, JFrame mainFrame) {
        return new TailscalePeerPanel(listener, mainFrame);
    }

    @Override
    public int priority() {
        return 60;
    }
}
