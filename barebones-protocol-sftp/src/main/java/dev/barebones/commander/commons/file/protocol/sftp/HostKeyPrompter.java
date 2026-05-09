/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.commons.file.protocol.sftp;

/**
 * Asked by the SFTP layer whenever JSch encounters an unknown or
 * changed host key and the active {@link HostKeyPolicy} is
 * {@link HostKeyPolicy#ASK}. Returns true to accept (and persist
 * to {@code ~/.ssh/known_hosts}), false to reject.
 *
 * Default impl shows a Swing {@code JOptionPane} with the JSch
 * message verbatim (which already includes host + fingerprint).
 * Tests inject a non-Swing impl that auto-accepts or auto-rejects.
 */
@FunctionalInterface
public interface HostKeyPrompter {

    boolean shouldAcceptHostKey(String jschMessage);

    /** Process-wide default prompter — set once at startup, read by every connection. */
    HostKeyPrompter[] DEFAULT = new HostKeyPrompter[]{ swingPrompter() };

    static HostKeyPrompter current() {
        return DEFAULT[0];
    }

    static void setDefault(HostKeyPrompter prompter) {
        DEFAULT[0] = prompter;
    }

    static HostKeyPrompter swingPrompter() {
        return message -> {
            int choice = javax.swing.JOptionPane.showConfirmDialog(
                null,
                message,
                "SFTP host-key verification",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE);
            return choice == javax.swing.JOptionPane.YES_OPTION;
        };
    }
}
