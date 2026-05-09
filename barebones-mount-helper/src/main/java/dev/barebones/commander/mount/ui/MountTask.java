/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.mount.ui;

import dev.barebones.commander.mount.MountExecutor;
import dev.barebones.commander.mount.MountResult;
import dev.barebones.commander.mount.MountSpec;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.util.concurrent.ExecutionException;

/**
 * SwingWorker that runs {@link MountExecutor#mount(MountSpec)} off
 * the EDT and surfaces progress via a small modal "Mounting…"
 * dialog. The dialog blocks the user from interacting with the rest
 * of the app while the kernel mount call is in flight, but the EDT
 * keeps pumping events (so the spinner animates and Cancel works).
 *
 * Caller pattern:
 * <pre>{@code
 *   MountResult r = MountTask.executeBlocking(parent, executor, spec);
 *   // r.ok() == true on success; r.stderr() carries the failure message
 * }</pre>
 */
final class MountTask extends SwingWorker<MountResult, Void> {

    private final MountExecutor executor;
    private final MountSpec spec;

    private MountTask(MountExecutor executor, MountSpec spec) {
        this.executor = executor;
        this.spec = spec;
    }

    @Override
    protected MountResult doInBackground() throws Exception {
        return executor.mount(spec);
    }

    /**
     * Runs the mount on a worker thread, showing a modal progress
     * dialog. Returns the {@link MountResult} on success; throws on
     * I/O error or interruption (matching {@link MountExecutor#mount}).
     */
    static MountResult executeBlocking(Frame parent, MountExecutor executor, MountSpec spec)
            throws InterruptedException, ExecutionException {
        MountTask task = new MountTask(executor, spec);

        JDialog progress = new JDialog(parent, "Mounting…", true);
        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        content.add(new JLabel(
            "Mounting " + spec.host() + ":" + spec.remotePath() +
            " → " + spec.mountpoint()),
            BorderLayout.NORTH);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        content.add(bar, BorderLayout.CENTER);
        progress.setContentPane(content);
        progress.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progress.setMinimumSize(new Dimension(380, 0));
        progress.pack();
        progress.setLocationRelativeTo(parent);

        task.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if ("state".equals(evt.getPropertyName())
                    && SwingWorker.StateValue.DONE.equals(evt.getNewValue())) {
                progress.dispose();
            }
        });

        task.execute();
        progress.setVisible(true); // blocks EDT, dispatches events,
                                   // returns when worker disposes the dialog
        return task.get();
    }
}
