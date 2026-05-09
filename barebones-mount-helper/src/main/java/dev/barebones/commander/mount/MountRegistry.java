/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.mount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(MountRegistry.class);

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

    /**
     * Best-effort unmount of every active mount, intended for the
     * JVM shutdown hook. Each unmount is bounded by the executor's
     * own timeout; failures are logged at WARN and never propagate
     * — shutdown must always complete.
     *
     * Drained entries are removed from the registry so a second
     * call is a no-op.
     */
    public void drainAtShutdown(MountExecutor executor) {
        Objects.requireNonNull(executor, "executor");
        List<MountSpec> snapshot = new ArrayList<>(active.values());
        for (MountSpec spec : snapshot) {
            try {
                MountResult r = executor.unmount(spec);
                if (!r.ok()) {
                    LOGGER.warn("shutdown unmount of {} exited {}: {}",
                        spec.mountpoint(), r.exitCode(), r.stderr().trim());
                }
            } catch (InterruptedException ie) {
                // Shutdown threads should not be interrupted, but
                // honour it by aborting the drain.
                Thread.currentThread().interrupt();
                LOGGER.warn("shutdown unmount interrupted at {}", spec.mountpoint());
                return;
            } catch (Exception e) {
                LOGGER.warn("shutdown unmount of {} failed: {}",
                    spec.mountpoint(), e.getMessage());
            } finally {
                active.remove(spec.mountpoint(), spec);
            }
        }
    }
}
