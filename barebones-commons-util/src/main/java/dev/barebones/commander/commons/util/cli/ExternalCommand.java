/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.commons.util.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Runs a short-lived external CLI tool and captures (exitCode,
 * stdout, stderr) with a hard timeout.
 *
 * Replaces hand-rolled {@link ProcessBuilder} + {@link Process#waitFor}
 * code that previously lived in {@code MountExecutor} and
 * {@code TailscaleClient}. Two correctness improvements over those:
 *
 * <ol>
 *   <li><b>Concurrent stdout / stderr drain.</b> Both streams are
 *       drained on dedicated threads in parallel with the wait. The
 *       previous "waitFor then read" sequence deadlocked when the
 *       child filled either pipe buffer (~64 KB on Linux) — the
 *       child blocked on write, the parent blocked on waitFor.</li>
 *   <li><b>Always closes stdin.</b> CLI tools that read stdin (rare
 *       but possible) won't hang waiting on a closed parent.</li>
 * </ol>
 *
 * Always invokes {@code new ProcessBuilder(List<String> argv)} —
 * never a single command-string — so user-controlled input passed
 * as its own argv slot can't introduce shell metacharacters.
 */
public final class ExternalCommand {

    private ExternalCommand() {
    }

    /**
     * Runs {@code argv} with the given {@code timeout}. On timeout
     * the child is destroyed forcibly and an {@link IOException} is
     * thrown.
     *
     * @throws IOException          on I/O error or timeout.
     * @throws InterruptedException if the caller's thread is interrupted.
     */
    public static Result run(List<String> argv, long timeout, TimeUnit unit)
            throws IOException, InterruptedException {
        Objects.requireNonNull(argv, "argv");
        Objects.requireNonNull(unit, "unit");
        if (argv.isEmpty()) {
            throw new IllegalArgumentException("argv must not be empty");
        }
        if (timeout <= 0) {
            throw new IllegalArgumentException("timeout must be positive");
        }

        ProcessBuilder pb = new ProcessBuilder(argv);
        pb.redirectErrorStream(false);
        Process p = pb.start();
        // Children that read stdin would otherwise block forever.
        p.getOutputStream().close();

        AtomicReference<IOException> stdoutErr = new AtomicReference<>();
        AtomicReference<IOException> stderrErr = new AtomicReference<>();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        CountDownLatch streamsDone = new CountDownLatch(2);

        Thread outThread = startDrainer(p.getInputStream(), stdout, stdoutErr,
            streamsDone, "external-stdout-" + argv.get(0));
        Thread errThread = startDrainer(p.getErrorStream(), stderr, stderrErr,
            streamsDone, "external-stderr-" + argv.get(0));

        boolean exited;
        try {
            exited = p.waitFor(timeout, unit);
        } catch (InterruptedException ie) {
            p.destroyForcibly();
            outThread.interrupt();
            errThread.interrupt();
            throw ie;
        }
        if (!exited) {
            p.destroyForcibly();
            // Give the drainers a brief window to wrap up after
            // destroyForcibly closes the pipes.
            streamsDone.await(2, TimeUnit.SECONDS);
            throw new IOException(
                "external command timed out after " + timeout + " " + unit
                    + ": " + argv);
        }
        // Wait for drainers to finish — they should be done shortly
        // after the process exits because the pipes EOF.
        streamsDone.await();
        rethrowIfNotNull(stdoutErr.get(), "stdout drain failed");
        rethrowIfNotNull(stderrErr.get(), "stderr drain failed");

        return new Result(p.exitValue(),
            stdout.toString(StandardCharsets.UTF_8),
            stderr.toString(StandardCharsets.UTF_8));
    }

    private static Thread startDrainer(InputStream in, ByteArrayOutputStream sink,
                                       AtomicReference<IOException> errSlot,
                                       CountDownLatch done, String name) {
        Thread t = new Thread(() -> {
            try (in) {
                byte[] buf = new byte[8 * 1024];
                int n;
                while ((n = in.read(buf)) != -1) {
                    sink.write(buf, 0, n);
                }
            } catch (IOException e) {
                errSlot.set(e);
            } finally {
                done.countDown();
            }
        }, name);
        t.setDaemon(true);
        t.start();
        return t;
    }

    private static void rethrowIfNotNull(IOException e, String prefix) throws IOException {
        if (e != null) {
            throw new IOException(prefix + ": " + e.getMessage(), e);
        }
    }

    /** Outcome of a successful (no-timeout) invocation. */
    public record Result(int exitCode, String stdout, String stderr) {
        public boolean ok() {
            return exitCode == 0;
        }
    }
}
