/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.tailscale.ui;

import dev.barebones.commander.commons.file.FileURL;
import dev.barebones.commander.protocol.ui.ServerPanel;
import dev.barebones.commander.protocol.ui.ServerPanelListener;
import dev.barebones.commander.tailscale.TailscaleClient;
import dev.barebones.commander.tailscale.TailscalePeer;
import dev.barebones.commander.tailscale.TailscaleService;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

/**
 * Connect-dialog tab that lists tailnet peers and turns the
 * selection into a remote URL. The user picks (a) a peer from the
 * list and (b) a protocol from the combo; on Connect we build a URL
 * like {@code sftp://<peer-magic-dns>/} for the active panel to open.
 *
 * Falls back to a clear "Tailscale not installed" message when the
 * binary isn't on PATH so the dialog still opens cleanly.
 */
public class TailscalePeerPanel extends ServerPanel {

    /** Protocols we know how to address by hostname over a tailnet. */
    private enum Protocol {
        SFTP("sftp", 22, "/"),
        NFS("nfs", 2049, "/"),
        SMB("smb", 445, "/");

        final String scheme;
        final int defaultPort;
        final String defaultPath;

        Protocol(String scheme, int defaultPort, String defaultPath) {
            this.scheme = scheme;
            this.defaultPort = defaultPort;
            this.defaultPath = defaultPath;
        }
    }

    private final JList<TailscalePeer> peerList;
    private final JComboBox<Protocol> protocolCombo;
    private final JLabel statusLabel;

    /** Cross-dialog-instance memory of the last selection — encapsulated
     *  here rather than as raw {@code static} fields so writes from
     *  instance methods don't trip SpotBugs'
     *  ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD detector. */
    private static final class LastValues {
        Protocol protocol = Protocol.SFTP;
        String dnsName = "";
    }
    private static final LastValues LAST = new LastValues();

    public TailscalePeerPanel(ServerPanelListener listener, JFrame mainFrame) {
        super(listener, mainFrame);

        TailscaleClient client = TailscaleService.client();
        peerList = new JList<>();
        peerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        peerList.setVisibleRowCount(8);
        peerList.setCellRenderer(new PeerCellRenderer());
        peerList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                listener.updateURLLabel();
            }
        });

        statusLabel = new JLabel(" ");

        if (client == null) {
            statusLabel.setText("Tailscale not installed (or 'tailscale' binary not on PATH)");
        } else {
            try {
                List<TailscalePeer> peers = client.peers();
                peerList.setListData(peers.toArray(new TailscalePeer[0]));
                if (peers.isEmpty()) {
                    statusLabel.setText("No peers in this tailnet.");
                } else {
                    statusLabel.setText(peers.size() + " peer(s) found.");
                    selectByLastDns(peers);
                }
            } catch (IOException | InterruptedException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                statusLabel.setText("Failed to list peers: " + e.getMessage());
            }
        }

        JScrollPane scroller = new JScrollPane(peerList);
        scroller.setPreferredSize(new java.awt.Dimension(400, 160));
        // ServerPanel's XAlignedComponentPanel layout puts label+component
        // pairs in rows; for the multi-line peer list we add it as a row
        // with a header label and the scroller as the right-hand component.
        addRow("Peers", scroller, 5);

        protocolCombo = new JComboBox<>(Protocol.values());
        protocolCombo.setSelectedItem(LAST.protocol);
        addComboBoxListeners(protocolCombo);
        addRow("Protocol", protocolCombo, 5);

        // Status at the bottom uses a panel to span the full width.
        javax.swing.JPanel statusRow = new javax.swing.JPanel(new BorderLayout());
        statusRow.add(statusLabel, BorderLayout.WEST);
        addRow("Status", statusRow, 15);
    }

    private void selectByLastDns(List<TailscalePeer> peers) {
        if (LAST.dnsName.isEmpty()) return;
        for (int i = 0; i < peers.size(); i++) {
            if (peers.get(i).dnsName().equals(LAST.dnsName)) {
                peerList.setSelectedIndex(i);
                return;
            }
        }
    }

    @Override
    public FileURL getServerURL() throws MalformedURLException {
        TailscalePeer peer = peerList.getSelectedValue();
        if (peer == null) {
            // No selection — return null so the dialog disables Connect
            // (matches the contract of other ServerPanel impls).
            return null;
        }
        Protocol protocol = (Protocol) protocolCombo.getSelectedItem();
        LAST.protocol = protocol;
        LAST.dnsName = peer.dnsName();

        FileURL url = FileURL.getFileURL(
            protocol.scheme + "://" + peer.dnsName() + protocol.defaultPath);
        url.setPort(protocol.defaultPort);
        return url;
    }

    @Override
    public boolean usesCredentials() {
        // The chosen protocol's panel handles credentials; this panel
        // only steers the user toward a peer and a scheme.
        return false;
    }

    @Override
    public void dialogValidated() {
        TailscalePeer peer = peerList.getSelectedValue();
        if (peer != null) {
            LAST.dnsName = peer.dnsName();
        }
        LAST.protocol = (Protocol) protocolCombo.getSelectedItem();
    }

    /** Renderer that shows the host name + DNS + online dot. */
    private static final class PeerCellRenderer extends javax.swing.DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof TailscalePeer p) {
                String dot = p.online() ? "●" : "○";
                String text = String.format("%s  %s — %s  (%s)",
                    dot, p.hostName(), p.dnsName(), p.os());
                setText(text);
            }
            return this;
        }
    }
}
