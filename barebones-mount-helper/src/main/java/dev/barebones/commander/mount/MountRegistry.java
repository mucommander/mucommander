/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.mount;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks active mounts so the UI can list / unmount them and so
 * shutdown can best-effort unmount everything we created.
 *
 * Keyed by mountpoint (an absolute Path); the same path can only
 * host one mount at a time, which matches OS behaviour.
 */
public final class MountRegistry {

    private static final MountRegistry INSTANCE = new MountRegistry();

    private final Map<Path, MountSpec> active = new ConcurrentHashMap<>();

    private MountRegistry() {
    }

    public static MountRegistry instance() {
        return INSTANCE;
    }

    public void recordMounted(MountSpec spec) {
        Objects.requireNonNull(spec, "spec");
        MountSpec previous = active.putIfAbsent(spec.mountpoint(), spec);
        if (previous != null) {
            throw new IllegalStateException(
                "mountpoint already in use by another spec: " + spec.mountpoint());
        }
    }

    public void recordUnmounted(MountSpec spec) {
        Objects.requireNonNull(spec, "spec");
        active.remove(spec.mountpoint(), spec);
    }

    public Collection<MountSpec> active() {
        return Map.copyOf(active).values();
    }
}
