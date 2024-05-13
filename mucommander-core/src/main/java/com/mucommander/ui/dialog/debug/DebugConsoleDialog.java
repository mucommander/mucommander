/*
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.dialog.debug;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import com.mucommander.commons.util.ui.dialog.FocusDialog;
import com.mucommander.desktop.ActionType;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.RefreshAction;
import com.mucommander.ui.action.impl.ShowDebugConsoleAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.utils.MuLogging;
import com.mucommander.utils.MuLogging.LogLevel;

/**
 * This dialog shows the last log messages collected by {@link DebugConsoleAppender} and allows them to be copied
 * to the clipboard. It also makes it possible to change the log level, the level combo box being preset to the
 * level returned by {@link MuLogging#getLogLevel()}.
 *
 * @see ShowDebugConsoleAction
 * @see DebugConsoleAppender
 * @see MuLogging#setLogLevel(LogLevel)
 * @author Maxence Bernard
 */
public class DebugConsoleDialog extends FocusDialog implements ItemListener {

    /** Displays log events, and allows to copy their values to the clipboard */
    private JTree loggingEventsTree;

    /** Root node of above tree (not visible) */
    private DefaultMutableTreeNode rootNode;

    /** Allows the log level to be changed */
    private JComboBox<LogLevel> levelComboBox;

    /** Closes the debug console when pressed */
    private JButton closeButton;

    /** Refreshes the tree with the latest log records when pressed */
    private JButton refreshButton;

    /** To control periodic auto-refresh */
    private JCheckBox autoRefreshCheckBox;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture periodicUpdater;

    /** Used to avoid refreshing logs if they haven't changed */
    private int lastRecordsHash = -1;

    /** Periodical auto-refresh - initial delay in ms */
    private final static int INITIAL_DELAY = 100;
    /** Periodical auto-refresh - refresh period in ms */
    private final static int REFRESH_PERIOD = 500;

    // Dialog size constraints
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(600,400);
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(700,500);

    /**
     * Creates a new {@link DebugConsoleDialog} using the given {@link MainFrame} as a parent.
     *
     * @param mainFrame the {@link MainFrame} to use as a parent
     */
    public DebugConsoleDialog(MainFrame mainFrame) {
        super((JFrame)null, ActionProperties.getActionLabel(ActionType.ShowDebugConsole),
                mainFrame.getJFrame());
        this.setModal(false);
        this.setAlwaysOnTop(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();

        rootNode = new DefaultMutableTreeNode("Root-hidden");
        refreshLogRecords();

        loggingEventsTree = new JTree(rootNode);
        loggingEventsTree.setAutoscrolls(true); // Autoscroll when dragged
        loggingEventsTree.setScrollsOnExpand(true);
        loggingEventsTree.setShowsRootHandles(true);
        loggingEventsTree.setRootVisible(false);
        var renderer = new DebugTreeCellRenderer();
        loggingEventsTree.setCellRenderer(renderer);
        renderer.setLeafIcon(null);
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);

        JScrollPane scrollPane = new JScrollPane(loggingEventsTree,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(createComboPanel(), BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        autoRefreshCheckBox = new JCheckBox(Translator.get("debug_console_dialog.auto_refresh"));
        autoRefreshCheckBox.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                refreshButton.setEnabled(false);
                periodicUpdater = executorService.scheduleAtFixedRate(
                        () -> SwingUtilities.invokeLater(this::refreshLogRecords),
                        INITIAL_DELAY, REFRESH_PERIOD, TimeUnit.MILLISECONDS);
            } else {
                refreshButton.setEnabled(true);
                if (periodicUpdater != null) {
                    periodicUpdater.cancel(false);
                    periodicUpdater = null;
                }
            }
        });
        buttonPanel.add(autoRefreshCheckBox);

        refreshButton = new JButton(Translator.get(new RefreshAction.Descriptor().getLabel()));
        refreshButton.addActionListener(e -> refreshLogRecords());
        buttonPanel.add(refreshButton);

        closeButton = new JButton(Translator.get("close"));
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        southPanel.add(buttonPanel, BorderLayout.EAST);
        contentPane.add(southPanel, BorderLayout.SOUTH);

        setInitialFocusComponent(closeButton);
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);

        setInitialFocusComponent(closeButton);
    }

    /**
     * Creates and returns a panel containing the level combo box and a leading localized label describing it.
     *
     * @return a panel containing the level combo box and a leading localized label describing it
     */
    private JPanel createComboPanel() {
        JPanel comboPanel = new JPanel(new FlowLayout());
        comboPanel.add(new JLabel(Translator.get("debug_console_dialog.level") + ":"));
        LogLevel logLevel = MuLogging.getLogLevel();

        levelComboBox = new JComboBox<>();
        for (LogLevel level:LogLevel.values()) {
            levelComboBox.addItem(level);
        }

        levelComboBox.setSelectedItem(logLevel);

        levelComboBox.addItemListener(this);

        comboPanel.add(levelComboBox);

        return comboPanel;
    }

    /**
     * Refreshes the JList with the log records contained by {@link DebugConsoleAppender}.
     */
    private void refreshLogRecords() {
        DebugConsoleAppender handler = MuLogging.getDebugConsoleAppender();
        final LoggingEvent[] records = handler.getLogRecords();
        var newRecordsHash = Arrays.hashCode(records);
        if (lastRecordsHash == newRecordsHash) {
            // don't refresh as nothing has changed in the logs.
            return;
        }
        lastRecordsHash = newRecordsHash;

        final LogLevel currentLogLevel = MuLogging.getLogLevel();

        rootNode.removeAllChildren();
        for (LoggingEvent record : records) {
            if (record.isLevelEqualOrHigherThan(currentLogLevel)) {
                var logEntry = record.toString();
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(record);
                logEntry.lines().skip(1).forEach(line -> {
                    DefaultMutableTreeNode sub = new DefaultMutableTreeNode(line);
                    node.add(sub);
                });
                rootNode.add(node);
            }
        }
        if (loggingEventsTree != null) {
            ((DefaultTreeModel)loggingEventsTree.getModel()).reload();
        }
        SwingUtilities.invokeLater(() -> {
            loggingEventsTree.scrollRowToVisible(records.length - 1);
        });
    }
    
    /**
     * Changes the log level to the selected combo box value.
     */
    private void updateLogLevel() {
        LogLevel newLevel = (LogLevel) levelComboBox.getSelectedItem();
        MuLogging.setLogLevel(newLevel);
    }

    /////////////////////////////////
    // ItemListener implementation //
    /////////////////////////////////

    public void itemStateChanged(ItemEvent e) {
        // Refresh the log records displayed in the JList whenever the selected level has been changed.
        int selectedIndex = levelComboBox.getSelectedIndex();
        if (selectedIndex != -1) {
            updateLogLevel();
            refreshLogRecords();
        }
    }

    @Override
    public void dispose() {
        if (periodicUpdater != null) {
            periodicUpdater.cancel(false);
            periodicUpdater = null;
        }
        executorService.shutdown();
        super.dispose();
    }

    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * Custom {@link ListCellRenderer} that renders {@link LoggingEvent} instances.
     */
    private class DebugTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree list, Object value,
                    boolean sel, boolean exp, boolean leaf,
                    int row, boolean cellHasFocus) {
            if (value == null) {
                return super.getTreeCellRendererComponent(list, value, sel, exp,
                        leaf, row, cellHasFocus);
            }

            // TODO: line-wrap log items when the text is too long to fit on a single line
            // A single-column JTable may be the easiest way to go, see:
            // http://javaspecialists.co.za/archive/newsletter.do?issue=106&locale=en_US
            // http://forums.sun.com/thread.jspa?threadID=702740&start=0&tstart=0

            // Using a JTextArea with line-wrapping enabled does not work as a JList has by design a fixed height
            // for cells

            // or https://stackoverflow.com/questions/7861724/is-there-a-word-wrap-property-for-jlabel
            var justFirstLine = value.toString().lines().findFirst().orElse(value.toString());
            var label = (JLabel)super.getTreeCellRendererComponent(list,
                    justFirstLine, sel, exp, leaf, row, cellHasFocus);

            var node = (DefaultMutableTreeNode)value;
            LogLevel level;
            if (node.getUserObject() instanceof LoggingEvent) {
                level = ((LoggingEvent)node.getUserObject()).getLevel();
            } else {
                var parent = (DefaultMutableTreeNode)node.getParent();
                if (parent != null && parent.getUserObject() instanceof LoggingEvent) {
                    level = ((LoggingEvent)parent.getUserObject()).getLevel();
                } else {
                    level = null;
                }
            }

            // Change the label's foreground color to match the level of the log record
            label.setForeground(getColorForLevel(level));

            // TODO: remove this when line-wrapping has been implemented
            // If component's preferred width is larger than the list's width then the component is not entirely
            // visible. In that case, we set a tooltip text that will display the whole text when mouse is over the
            // component

            /** tooltip doesn't work for me in JTree :/
            if (loggingEventsTree.getVisibleRect().getWidth() < label.getPreferredSize().getWidth()) {
                label.setToolTipText(label.getText());
            } else {
                // Have to set it to null because of the rubber-stamp rendering scheme (last value is kept)
                label.setToolTipText(null);
            }*/
            return label;
        }

        private Color getColorForLevel(LogLevel level) {
            if (level == null) {
                return Color.BLACK;
            }
            Color color;
            switch (level) {
                case SEVERE:
                    color = Color.RED;
                    break;
                case WARNING:
                    // Dark orange
                    color = new Color(255, 100, 0);
                    break;
                case CONFIG:
                    color = Color.BLUE;
                    break;
                case INFO:
                    color = Color.BLACK;
                    break;
                case FINE:
                    color = Color.DARK_GRAY;
                    break;
                default:
                    // Between Color.GRAY and Color.DARK_GRAY
                    color = new Color(110, 110, 110);
            };
            return color;
        }
    }
}
