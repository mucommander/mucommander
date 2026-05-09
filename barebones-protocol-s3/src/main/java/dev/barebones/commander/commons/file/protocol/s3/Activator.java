/*
 * Copyright (C) 2002-2026 muCommander contributors
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.commons.file.protocol.s3;

import dev.barebones.commander.commons.file.AuthenticationType;
import dev.barebones.commander.commons.file.DefaultSchemeHandler;
import dev.barebones.commander.commons.file.DefaultSchemeParser;
import dev.barebones.commander.commons.file.SchemeHandler;
import dev.barebones.commander.commons.file.osgi.FileProtocolService;
import dev.barebones.commander.commons.file.osgi.FileProtocolServiceTracker;
import dev.barebones.commander.commons.file.protocol.ProtocolProvider;
import dev.barebones.commander.commons.file.protocol.s3.ui.S3PanelProvider;
import dev.barebones.commander.protocol.ui.ProtocolPanelRegistry;

/**
 * Phase-2-style register entry point. Registers the s3 scheme with
 * the file factory and the {@link S3PanelProvider} with the
 * Connect-to-server dialog so the user gets an "S3" tab.
 */
public final class Activator {

    private Activator() {
    }

    public static void register() {
        FileProtocolServiceTracker.register(new FileProtocolService() {
            @Override
            public String getSchema() {
                return "s3";
            }

            @Override
            public ProtocolProvider getProtocolProvider() {
                return new S3ProtocolProvider();
            }

            @Override
            public SchemeHandler getSchemeHandler() {
                // S3 default port: 443 (HTTPS) for AWS; MinIO/Ceph
                // typically bind 9000 over HTTP — they go through
                // url.setPort(9000) which overrides the default.
                return new DefaultSchemeHandler(
                    new DefaultSchemeParser(),
                    443,
                    "/",
                    AuthenticationType.AUTHENTICATION_REQUIRED,
                    null);
            }
        });

        ProtocolPanelRegistry.register(new S3PanelProvider());
    }
}
