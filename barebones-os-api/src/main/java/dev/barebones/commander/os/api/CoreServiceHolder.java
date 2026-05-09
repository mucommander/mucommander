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
package dev.barebones.commander.os.api;

/**
 * Static holder for the singleton {@link CoreService}. Lives in the
 * os-api module — the only module both barebones-core (the producer)
 * and barebones-os-macos (the consumer) depend on at compile time.
 *
 * Pre-Phase-2 the macOS module ran a {@code CoreServiceTracker} OSGi
 * tracker that watched for core's CoreService registration. After
 * Phase 2 dropped OSGi, the producer simply calls {@link #set} and the
 * consumer calls {@link #get}.
 */
public final class CoreServiceHolder {

    private static volatile CoreService service;

    private CoreServiceHolder() {
    }

    public static void set(CoreService coreService) {
        service = coreService;
    }

    public static CoreService get() {
        return service;
    }
}
