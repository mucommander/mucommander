/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.mount.ui;

import dev.barebones.commander.protocol.ui.ProtocolPanelProvider;
import dev.barebones.commander.protocol.ui.ServerPanel;
import dev.barebones.commander.protocol.ui.ServerPanelListener;

import javax.swing.JFrame;

/**
 * {@link ProtocolPanelProvider} that contributes the {@link MountPanel}
 * tab to the Connect-to-server dialog. The schema "mount" is virtual —
 * {@link MountPanel#getServerURL()} returns a {@code file://} URL once
 * the OS mount has been issued, so this provider doesn't actually
 * register a new file-protocol scheme.
 */
public final class MountPanelProvider implements ProtocolPanelProvider {

    @Override
    public String getSchema() {
        return "mount";
    }

    @Override
    public ServerPanel get(ServerPanelListener listener, JFrame mainFrame) {
        return new MountPanel(listener, mainFrame);
    }

    @Override
    public int priority() {
        // Below the real remote protocols; this is a connectivity
        // helper, not a primary protocol.
        return 50;
    }
}
