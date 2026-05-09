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
import dev.barebones.commander.mount.MountRegistry;
import dev.barebones.commander.mount.MountResult;
import dev.barebones.commander.mount.MountService;
import dev.barebones.commander.mount.MountSpec;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;

/**
 * Modal "Active mounts" dialog. Lists every entry in
 * {@link MountRegistry#instance()} and offers per-row Unmount via the
 * platform's {@link MountExecutor}.
 *
 * Opened from the Mount tab in the Connect-to-server dialog, not via
 * a top-level Action — this keeps the dialog self-contained inside
 * the mount-helper module (no compile-time dep on barebones-core's
 * MuAction / ActionManager). When Phase 11+ wants a top-level menu
 * entry, it can add an Action subclass that just `new`s this dialog.
 */
public final class ActiveMountsDialog extends JDialog {

    private final DefaultListModel<MountSpec> listModel = new DefaultListModel<>();
    private final JList<MountSpec> list = new JList<>(listModel);
    private final JButton unmountButton = new JButton("Unmount");

    public ActiveMountsDialog(Frame parent) {
        super(parent, "Active mounts", true);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(10);
        list.setCellRenderer(new MountCellRenderer());
        list.addListSelectionListener(e -> updateButtonState());

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        content.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        unmountButton.addActionListener(e -> unmountSelected());
        unmountButton.setEnabled(false);
        buttonRow.add(unmountButton);
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonRow.add(closeButton);
        content.add(buttonRow, BorderLayout.SOUTH);

        setContentPane(content);
        setMinimumSize(new Dimension(480, 0));
        pack();
        setLocationRelativeTo(parent);

        refresh();
    }

    private void refresh() {
        listModel.clear();
        for (MountSpec spec : MountRegistry.instance().active()) {
            listModel.addElement(spec);
        }
        if (listModel.isEmpty()) {
            // Show a placeholder so the dialog isn't a confusing
            // empty box. JList doesn't render a placeholder natively;
            // borrow the disabled state on the unmount button to
            // signal "nothing to do".
            unmountButton.setEnabled(false);
        }
    }

    private void updateButtonState() {
        unmountButton.setEnabled(list.getSelectedValue() != null);
    }

    private void unmountSelected() {
        MountSpec spec = list.getSelectedValue();
        if (spec == null) return;
        MountExecutor executor = MountService.executor();
        if (executor == null) {
            JOptionPane.showMessageDialog(this,
                "Mount helper is not available on this OS.",
                "Unmount failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            MountResult result = executor.unmount(spec);
            if (result.ok()) {
                MountRegistry.instance().recordUnmounted(spec);
                refresh();
            } else {
                JOptionPane.showMessageDialog(this,
                    "umount exited " + result.exitCode() + ":\n" + result.stderr().strip(),
                    "Unmount failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            JOptionPane.showMessageDialog(this,
                "Unmount interrupted.",
                "Unmount failed", JOptionPane.WARNING_MESSAGE);
        } catch (java.io.IOException e) {
            JOptionPane.showMessageDialog(this,
                "Unmount failed: " + e.getMessage(),
                "Unmount failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static final class MountCellRenderer extends javax.swing.DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof MountSpec s) {
                setText(String.format("%s   %s:%s   →   %s",
                    s.kind(), s.host(), s.remotePath(), s.mountpoint()));
            } else if (value == null) {
                setText("(no active mounts)");
            }
            return this;
        }
    }

    /** Convenience entry point used by the {@link MountPanel} button. */
    public static void open(Frame parent) {
        new ActiveMountsDialog(parent).setVisible(true);
    }
}
