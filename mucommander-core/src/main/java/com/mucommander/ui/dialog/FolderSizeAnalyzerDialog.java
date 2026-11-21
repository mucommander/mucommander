/*
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.ui.dialog;

import com.mucommander.commons.file.AbstractFile;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/** A first version of a visual storage analysis dialog for local folders. */
public final class FolderSizeAnalyzerDialog extends JDialog {
    private static final int MAX_BARS = 15;
    private static final Color[] COLORS = {
            new Color(56, 132, 211), new Color(41, 165, 140),
            new Color(210, 128, 43), new Color(151, 105, 196),
            new Color(208, 90, 113), new Color(67, 159, 192)
    };

    private final AbstractFile folder;
    private final JLabel summary = new JLabel("Scanning folder contents...");
    private final JProgressBar progress = new JProgressBar();
    private final JPanel results = new JPanel();
    private SwingWorker<Analysis, Void> worker;

    public static void showFor(JFrame owner, AbstractFile folder) {
        FolderSizeAnalyzerDialog dialog = new FolderSizeAnalyzerDialog(owner, folder);
        dialog.startScan();
        dialog.setVisible(true);
    }

    private FolderSizeAnalyzerDialog(JFrame owner, AbstractFile folder) {
        super(owner, "Analyze Folder Size - " + folder.getName(), false);
        this.folder = folder;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel header = new JPanel(new BorderLayout(6, 6));
        summary.setFont(summary.getFont().deriveFont(Font.BOLD));
        header.add(summary, BorderLayout.NORTH);
        progress.setIndeterminate(true);
        header.add(progress, BorderLayout.SOUTH);
        content.add(header, BorderLayout.NORTH);

        results.setLayout(new BoxLayout(results, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(results);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        content.add(scrollPane, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JButton close = new JButton("Cancel / Close");
        close.addActionListener(event -> dispose());
        buttons.add(close);
        content.add(buttons, BorderLayout.SOUTH);
        setContentPane(content);
        setSize(760, 540);
        setLocationRelativeTo(owner);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                if (worker != null && !worker.isDone()) {
                    worker.cancel(true);
                }
            }
        });
    }

    private void startScan() {
        worker = new SwingWorker<>() {
            private int unreadableEntries;

            @Override
            protected Analysis doInBackground() throws IOException {
                List<Item> items = new ArrayList<>();
                long total = 0;
                AbstractFile[] children = folder.ls();
                if (children != null) {
                    for (AbstractFile child : children) {
                        if (isCancelled()) {
                            break;
                        }
                        long size = calculateSize(child);
                        items.add(new Item(child.getName(), size));
                        total = safeAdd(total, size);
                    }
                }
                items.sort(Comparator.comparingLong((Item item) -> item.size).reversed());
                return new Analysis(items, total, unreadableEntries);
            }

            private long calculateSize(AbstractFile file) {
                if (isCancelled()) {
                    return 0;
                }
                try {
                    // Avoid following symbolic links, which can cause cycles.
                    if (file.isSymlink()) {
                        return 0;
                    }
                    if (!file.isDirectory()) {
                        return Math.max(0, file.getSize());
                    }
                    long total = 0;
                    AbstractFile[] children = file.ls();
                    if (children != null) {
                        for (AbstractFile child : children) {
                            if (isCancelled()) {
                                break;
                            }
                            total = safeAdd(total, calculateSize(child));
                        }
                    }
                    return total;
                } catch (IOException exception) {
                    // Continue scanning, but report that the total is incomplete.
                    unreadableEntries++;
                    return 0;
                }
            }

            @Override
            protected void done() {
                if (isCancelled() || !isDisplayable()) {
                    return;
                }
                progress.setIndeterminate(false);
                progress.setVisible(false);
                try {
                    display(get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    summary.setText("Analysis interrupted.");
                } catch (ExecutionException exception) {
                    summary.setText("Unable to analyze folder: "
                            + String.valueOf(exception.getCause()));
                }
            }
        };
        worker.execute();
    }

    private void display(Analysis analysis) {
        results.removeAll();
        String totalLabel = analysis.unreadableEntries == 0 ? "Total" : "Readable total";
        String text = String.format(Locale.ROOT,
                "%s: %s   |   %d items", totalLabel,
                formatBytes(analysis.total), analysis.items.size());
        if (analysis.unreadableEntries > 0) {
            text += "   |   " + analysis.unreadableEntries + " unreadable entries";
        }
        summary.setText(text);

        if (analysis.items.isEmpty()) {
            results.add(new JLabel("This folder is empty or cannot be read."));
        } else {
            int shown = Math.min(MAX_BARS, analysis.items.size());
            long rest = 0;
            for (int index = shown; index < analysis.items.size(); index++) {
                rest = safeAdd(rest, analysis.items.get(index).size);
            }

            // The combined Other group may be larger than any individual item.
            // Include it in the bar scale to avoid visually clipping its bar.
            long largest = Math.max(1, Math.max(analysis.items.get(0).size, rest));
            for (int index = 0; index < shown; index++) {
                addBar(analysis.items.get(index), analysis.total, largest, index);
            }
            if (analysis.items.size() > shown) {
                addBar(new Item("Other (" + (analysis.items.size() - shown) + " items)", rest),
                        analysis.total, largest, shown);
            }
        }
        results.revalidate();
        results.repaint();
    }

    private void addBar(Item item, long total, long largest, int index) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JLabel name = new JLabel(shorten(item.name, 29));
        name.setToolTipText(item.name);
        name.setPreferredSize(new Dimension(200, 28));
        row.add(name, BorderLayout.WEST);

        JProgressBar bar = new JProgressBar(0, 1000);
        bar.setValue((int) Math.round(1000.0 * item.size / largest));
        bar.setForeground(COLORS[index % COLORS.length]);
        bar.setStringPainted(true);
        bar.setString(String.format(Locale.ROOT, "%.1f%%",
                total == 0 ? 0.0 : 100.0 * item.size / total));
        row.add(bar, BorderLayout.CENTER);

        JLabel size = new JLabel(formatBytes(item.size));
        size.setPreferredSize(new Dimension(90, 28));
        row.add(size, BorderLayout.EAST);
        results.add(row);
    }

    private static long safeAdd(long a, long b) {
        return b > Long.MAX_VALUE - a ? Long.MAX_VALUE : a + b;
    }

    private static String shorten(String text, int limit) {
        return text.length() <= limit ? text : text.substring(0, limit - 3) + "...";
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        String[] units = {"KiB", "MiB", "GiB", "TiB", "PiB"};
        double value = bytes;
        int index = -1;
        do {
            value /= 1024;
            index++;
        } while (value >= 1024 && index < units.length - 1);
        return String.format(Locale.ROOT, "%.1f %s", value, units[index]);
    }

    private static final class Item {
        private final String name;
        private final long size;

        private Item(String name, long size) {
            this.name = name;
            this.size = size;
        }
    }

    private static final class Analysis {
        private final List<Item> items;
        private final long total;
        private final int unreadableEntries;

        private Analysis(List<Item> items, long total, int unreadableEntries) {
            this.items = items;
            this.total = total;
            this.unreadableEntries = unreadableEntries;
        }
    }
}
