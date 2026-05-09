/*
 * Copyright (C) 2002-2026 muCommander contributors
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package dev.barebones.commander.protocol.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Static registry for {@link ProtocolPanelProvider} instances
 * contributed by individual protocol modules.
 *
 * Protocol modules (sftp, s3, nfs, ...) call {@link #register(ProtocolPanelProvider)}
 * during their bootstrap. {@code barebones-core} reads back the
 * accumulated list later (in its own register()) and wires each
 * provider into ServerConnectDialog / DrivePopupButton.
 *
 * Lives in {@code barebones-protocol-api} because it's the only module
 * shared by both producers (protocol modules) and the consumer
 * ({@code barebones-core}) at compile time.
 */
public final class ProtocolPanelRegistry {

    private static final List<ProtocolPanelProvider> PROVIDERS = new ArrayList<>();

    private ProtocolPanelRegistry() {
    }

    public static void register(ProtocolPanelProvider provider) {
        PROVIDERS.add(provider);
    }

    public static List<ProtocolPanelProvider> all() {
        return Collections.unmodifiableList(PROVIDERS);
    }
}
