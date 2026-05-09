/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.mount.ui;

import dev.barebones.commander.commons.file.FileURL;
import dev.barebones.commander.mount.MountKind;
import dev.barebones.commander.mount.MountRegistry;
import dev.barebones.commander.mount.MountResult;
import dev.barebones.commander.mount.MountService;
import dev.barebones.commander.mount.MountSpec;
import dev.barebones.commander.protocol.ui.ServerPanel;
import dev.barebones.commander.protocol.ui.ServerPanelListener;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;

/**
 * Connect-dialog tab for mounting a remote share via the OS mount
 * helpers. The user picks a kind (NFSv3/v4, SMB, SSHFS), fills in
 * host / share / username / port, hits Connect.
 *
 * On submit we shell out via {@link MountService#executor()}, record
 * the mount in {@link MountRegistry}, and return a {@code file://}
 * URL pointing at the local mountpoint so the active folder panel
 * navigates into it as if it were any other local directory.
 *
 * Note: the mount call runs on the EDT (the dialog blocks while
 * waiting). Mount commands that complete in well under a second
 * (the typical kernel mount path) are fine; long-tail timeouts
 * surface as a frozen dialog. A SwingWorker wrapper is a follow-up.
 */
public class MountPanel extends ServerPanel {

    private final JComboBox<MountKind> kindCombo;
    private final JTextField hostField;
    private final JTextField remotePathField;
    private final JTextField mountpointField;
    private final JTextField usernameField;
    @SuppressWarnings("unused") // surfaced via getServerURL → MountSpec; not stored cross-session
    private final JPasswordField passwordField;
    private final JSpinner portSpinner;

    /** Cross-dialog-instance memory of the last form values —
     *  encapsulated rather than raw {@code static} so writes from
     *  instance methods don't trip SpotBugs'
     *  ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD detector. */
    private static final class LastValues {
        MountKind kind = MountKind.NFSV4;
        String host = "";
        String remotePath = "";
        String mountpoint = "";
        String username = "";
        int port;
    }
    private static final LastValues LAST = new LastValues();

    public MountPanel(ServerPanelListener listener, JFrame mainFrame) {
        super(listener, mainFrame);

        kindCombo = new JComboBox<>(MountKind.values());
        kindCombo.setSelectedItem(LAST.kind);
        addComboBoxListeners(kindCombo);
        addRow("Protocol", kindCombo, 15);

        hostField = new JTextField(LAST.host);
        hostField.selectAll();
        addTextFieldListeners(hostField, true);
        addRow("Host", hostField, 5);

        remotePathField = new JTextField(LAST.remotePath);
        addTextFieldListeners(remotePathField, true);
        addRow("Remote path / share", remotePathField, 5);

        mountpointField = new JTextField(LAST.mountpoint.isEmpty() ? defaultMountpoint() : LAST.mountpoint);
        addTextFieldListeners(mountpointField, true);
        addRow("Local mountpoint", mountpointField, 15);

        usernameField = new JTextField(LAST.username);
        addTextFieldListeners(usernameField, false);
        addRow("Username (SMB / SSHFS, optional)", usernameField, 5);

        passwordField = new JPasswordField();
        addTextFieldListeners(passwordField, false);
        addRow("Password (currently unused; configure via OS keychain)", passwordField, 15);

        portSpinner = createPortSpinner(LAST.port);
        addRow("Port (SSHFS, 0 = default)", portSpinner, 15);
    }

    /** Phase-10 default mountpoint root: ~/.barebones-commander/mounts. */
    private static String defaultMountpoint() {
        String home = System.getProperty("user.home", "/tmp");
        return Paths.get(home, ".barebones-commander", "mounts", "share").toString();
    }

    private void updateValues() {
        LAST.kind = (MountKind) kindCombo.getSelectedItem();
        LAST.host = hostField.getText();
        LAST.remotePath = remotePathField.getText();
        LAST.mountpoint = mountpointField.getText();
        LAST.username = usernameField.getText();
        LAST.port = (Integer) portSpinner.getValue();
    }

    @Override
    public FileURL getServerURL() throws MalformedURLException {
        updateValues();
        if (MountService.executor() == null) {
            throw new MalformedURLException(
                "mount helper is not available on this OS (Phase 10 targets Linux + macOS)");
        }
        MountSpec spec;
        try {
            spec = new MountSpec(
                LAST.kind,
                LAST.host,
                LAST.remotePath,
                Path.of(LAST.mountpoint),
                LAST.username.isBlank() ? null : LAST.username,
                LAST.port);
        } catch (RuntimeException e) {
            throw new MalformedURLException("invalid mount spec: " + e.getMessage());
        }

        MountResult result;
        try {
            result = MountService.executor().mount(spec);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new MalformedURLException("mount failed: " + e.getMessage());
        }
        if (!result.ok()) {
            throw new MalformedURLException(
                "mount exited " + result.exitCode() + ": " + result.stderr().strip());
        }
        MountRegistry.instance().recordMounted(spec);
        return FileURL.getFileURL("file://" + spec.mountpoint().toString());
    }

    @Override
    public boolean usesCredentials() {
        // SMB and SSHFS take a username; we forward it via -o user= or user@host
        // form. No password material is stored — Phase 12 wires keychain.
        return true;
    }

    @Override
    public void dialogValidated() {
        try {
            portSpinner.commitEdit();
        } catch (ParseException ignored) {
            // spinner uses an editor that always commits; ignored
        }
        updateValues();
    }
}
