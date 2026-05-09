/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.mount;

import dev.barebones.commander.commons.util.cli.ExternalCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Runs argv lists from a {@link MountCommand} as real OS processes,
 * capturing stdout / stderr / exit code into a {@link MountResult}.
 *
 * The executor never invokes a shell — everything is
 * {@code new ProcessBuilder(List<String>)}. Any user-supplied input
 * (host, share, username) was already validated by {@link MountSpec}
 * and is passed as its own argv entry by {@link MountCommand}.
 */
public final class MountExecutor {

    /** Hard ceiling on a single mount/unmount invocation. */
    private static final long DEFAULT_TIMEOUT_SECONDS = 30L;

    private final MountCommand command;
    private final long timeoutSeconds;

    public MountExecutor(MountCommand command) {
        this(command, DEFAULT_TIMEOUT_SECONDS);
    }

    public MountExecutor(MountCommand command, long timeoutSeconds) {
        this.command = Objects.requireNonNull(command, "command");
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("timeoutSeconds must be positive");
        }
        this.timeoutSeconds = timeoutSeconds;
    }

    public MountResult mount(MountSpec spec) throws IOException, InterruptedException {
        ensureMountpointExists(spec);
        return run(command.mountArgv(spec));
    }

    public MountResult unmount(MountSpec spec) throws IOException, InterruptedException {
        return run(command.unmountArgv(spec));
    }

    private static void ensureMountpointExists(MountSpec spec) throws IOException {
        Files.createDirectories(spec.mountpoint());
    }

    private MountResult run(List<String> argv) throws IOException, InterruptedException {
        ExternalCommand.Result r = ExternalCommand.run(argv, timeoutSeconds, TimeUnit.SECONDS);
        return new MountResult(r.exitCode(), r.stdout(), r.stderr());
    }
}
