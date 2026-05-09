/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.commons.file.protocol.s3.ui;

import dev.barebones.commander.protocol.ui.ProtocolPanelProvider;
import dev.barebones.commander.protocol.ui.ServerPanel;
import dev.barebones.commander.protocol.ui.ServerPanelListener;

import javax.swing.JFrame;

public final class S3PanelProvider implements ProtocolPanelProvider {

    @Override
    public String getSchema() {
        return "s3";
    }

    @Override
    public ServerPanel get(ServerPanelListener listener, JFrame mainFrame) {
        return new S3Panel(listener, mainFrame);
    }

    @Override
    public int priority() {
        return 30; // remote protocol; sits with SFTP
    }
}
